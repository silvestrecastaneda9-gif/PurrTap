package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PurrGameRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureUserProfile_authenticatedOwner_createsProfileAndLeaderboardEntry() = runBlocking {
    val uid = signInTestUser(uniqueEmail("ensure"))
    val repository = PurrGameRepository(firestore)

    val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.ensureUserProfile(uid, "Alice Cat")
    }
    assertEquals(uid, profile.userId)
    assertEquals("Alice Cat", profile.displayName)

    val observed = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile(uid).first { it != null }
    }
    assertNotNull(observed)
    assertEquals(uid, observed?.userId)

    val leaderboard = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeLeaderboard(10L).first { list -> list.any { it.userId == uid } }
    }
    assertTrue(leaderboard.any { it.userId == uid })
  }

  @Test
  fun saveProfileAndLeaderboard_authenticatedOwner_persistsUpdates() = runBlocking {
    val uid = signInTestUser(uniqueEmail("save"))
    val repository = PurrGameRepository(firestore)

    withTimeout(DEFAULT_TIMEOUT_MS) { repository.ensureUserProfile(uid, "Alice Cat") }
    val loaded = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile(uid).first { it != null && it.createdAt != null }
    }!!

    val updated =
      loaded.copy(
        purrs = loaded.purrs + 150L,
        totalPurrsEarned = loaded.totalPurrsEarned + 150L,
        totalPets = loaded.totalPets + 12L,
        selectedPatternId = "calico",
        selectedAccessoryId = "bell_collar",
      )

    withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveProfileAndLeaderboard(updated) }

    val reloaded = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile(uid).first { it != null && it.totalPets >= loaded.totalPets + 12L }
    }!!
    assertEquals("calico", reloaded.selectedPatternId)
    assertEquals("bell_collar", reloaded.selectedAccessoryId)
  }

  @Test
  fun observeUserProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(uniqueEmail("alice"))
    val aliceRepo = PurrGameRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.ensureUserProfile(aliceUid, "Alice") }

    signInTestUser(uniqueEmail("bob"))
    val bobRepo = PurrGameRepository(firestore)
    try {
      withTimeout(FLOW_TIMEOUT_MS) { bobRepo.observeUserProfile(aliceUid).first() }
      fail("Expected PERMISSION_DENIED when Bob observes Alice's private profile")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }
  }

  @Test
  fun observeLeaderboard_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = PurrGameRepository(firestore)
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repository.observeLeaderboard().first() }
      fail("Expected PERMISSION_DENIED when unauthenticated caller queries leaderboard")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }
  }

  private fun uniqueEmail(prefix: String): String {
    return "${prefix}_${java.util.UUID.randomUUID()}@test.com"
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
