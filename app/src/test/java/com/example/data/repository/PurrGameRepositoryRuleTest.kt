package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardPublicStats
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
  fun saveProfileAndLeaderboard_authenticatedOwner_persistsProfileCustomizationAndStats() = runBlocking {
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
        statsPublic = false,
        bio = "Collector of rare cats",
        avatarBase64 = "ZmFrZV9hdmF0YXI=",
        bannerBase64 = "ZmFrZV9iYW5uZXI=",
      )

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveProfileAndLeaderboard(
        updated,
        LeaderboardPublicStats(rebirths = 3L, ascensions = 1L, dailyStreak = 9L, highestComboX100 = 240L),
      )
    }

    val reloaded = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile(uid).first { it != null && it.totalPets >= loaded.totalPets + 12L }
    }!!
    assertEquals("calico", reloaded.selectedPatternId)
    assertEquals("bell_collar", reloaded.selectedAccessoryId)
    assertFalse(reloaded.statsPublic)
    assertEquals("Collector of rare cats", reloaded.bio)

    val lbEntry = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeLeaderboard(20L).first { list -> list.any { it.userId == uid && !it.statsPublic } }
    }.first { it.userId == uid }
    assertEquals(3L, lbEntry.publicRebirths)
    assertEquals(1L, lbEntry.publicAscensions)
    assertEquals(9L, lbEntry.publicDailyStreak)
    assertEquals(240L, lbEntry.publicHighestComboX100)
  }

  @Test
  fun deleteUserCloudData_ownerCanWipeOwnProfileForDangerZoneReset() = runBlocking {
    val uid = signInTestUser(uniqueEmail("danger"))
    val repository = PurrGameRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) { repository.ensureUserProfile(uid, "Reset Cat") }

    withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteUserCloudData(uid) }
    val afterDelete = withTimeout(FLOW_TIMEOUT_MS) { repository.observeUserProfile(uid).first() }
    assertNull(afterDelete)
  }

  @Test
  fun limitedItemExistCounter_signedInUserCanIncrementAtomically() = runBlocking {
    val repository = PurrGameRepository(firestore)
    val statId = GameCatalog.existStatId("PATTERN", "golden_touch_cat_${java.util.UUID.randomUUID().toString().take(6)}")

    signInTestUser(uniqueEmail("counter1"))
    withTimeout(DEFAULT_TIMEOUT_MS) { repository.incrementLimitedItemExistCount(statId, "PATTERN") }

    signInTestUser(uniqueEmail("counter2"))
    withTimeout(DEFAULT_TIMEOUT_MS) { repository.incrementLimitedItemExistCount(statId, "PATTERN") }

    val counters = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeLimitedItemCounters().first { (it[statId] ?: 0L) >= 2L }
    }
    assertEquals(2L, counters[statId])
  }

  @Test
  fun adminModerationAndAnnouncements_rejectsUnverifiedEmailAndAllowsVerifiedAdmin() = runBlocking {
    val repository = PurrGameRepository(firestore)
    val aliceUid = signInTestUser(uniqueEmail("target_alice"))
    val adminPassword = "password123"

    // Even using the admin email, unverified emulator account must be denied by Firestore Rules
    signInWithUnverifiedAdminEmail(adminPassword)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        repository.setPlayerModeration(
          targetUserId = aliceUid,
          status = "WARNED",
          reason = "Spoof attempt",
          timeoutUntilMillis = 0L,
          incrementWarning = true,
          adminEmail = GameCatalog.ADMIN_EMAIL,
        )
      }
      fail("Expected PERMISSION_DENIED for unverified admin email")
    } catch (e: Exception) {
      val fsEx = e.unwrapFirestoreException()
      assertNotNull(fsEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    // Verify the email in the local Auth Emulator and re-authenticate for a fresh email_verified claim
    setAdminEmailVerifiedInEmulator(adminPassword, verified = true)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.setPlayerModeration(
        targetUserId = aliceUid,
        status = "TIMED_OUT",
        reason = "Test timeout",
        timeoutUntilMillis = System.currentTimeMillis() + 60_000L,
        incrementWarning = true,
        adminEmail = GameCatalog.ADMIN_EMAIL,
      )
    }

    val allModeration = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeAllModerationForAdmin().first { list -> list.any { it.userId == aliceUid } }
    }
    val record = allModeration.first { it.userId == aliceUid }
    assertEquals("TIMED_OUT", record.status)
    assertEquals(1L, record.warningCount)

    val msgId = "msg_${java.util.UUID.randomUUID().toString().take(8)}"
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.sendGlobalAnnouncement(
        messageId = msgId,
        title = "Admin Live Notice",
        body = "Double purrs soon!",
        severity = "EVENT",
        senderEmail = GameCatalog.ADMIN_EMAIL,
      )
    }

    val announcements = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeGlobalAnnouncements().first { list -> list.any { it.messageId == msgId } }
    }
    assertTrue(announcements.any { it.title == "Admin Live Notice" })

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.clearPlayerModeration(aliceUid)
      repository.deleteGlobalAnnouncement(msgId)
    }
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

  private suspend fun signInWithUnverifiedAdminEmail(password: String) {
    val existed =
      withContext(Dispatchers.IO) {
        try {
          auth.signInWithEmailAndPassword(GameCatalog.ADMIN_EMAIL, password).await()
          true
        } catch (_: Exception) {
          auth.createUserWithEmailAndPassword(GameCatalog.ADMIN_EMAIL, password).await()
          false
        }
      }
    if (existed && auth.currentUser?.isEmailVerified == true) {
      setAdminEmailVerifiedInEmulator(password, verified = false)
    }
  }

  private suspend fun setAdminEmailVerifiedInEmulator(password: String, verified: Boolean) =
    withContext(Dispatchers.IO) {
      val localId = auth.currentUser?.uid ?: error("Missing signed-in admin UID")
      val url = URL("http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/projects/demo-no-project/accounts:update")
      val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Authorization", "Bearer owner")
        doOutput = true
      }
      OutputStreamWriter(conn.outputStream).use {
        it.write("""{"localId":"$localId","emailVerified":$verified}""")
      }
      val code = conn.responseCode
      if (code !in 200..299) {
        val err = conn.errorStream?.bufferedReader()?.readText().orEmpty()
        error("Auth emulator accounts:update returned $code: $err")
      }
      conn.disconnect()
      auth.signOut()
      auth.signInWithEmailAndPassword(GameCatalog.ADMIN_EMAIL, password).await()
      auth.currentUser?.getIdToken(true)?.await()
    }

  private fun uniqueEmail(prefix: String): String {
    return "${prefix}_${java.util.UUID.randomUUID()}@test.com"
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
