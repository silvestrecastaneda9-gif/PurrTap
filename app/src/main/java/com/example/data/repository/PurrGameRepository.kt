package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PurrGameRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  fun observeUserProfile(userId: String): Flow<UserProfile?> {
    val docRef = db.collection(USERS_COLLECTION).document(userId)
    return docRef
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(
            UserProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        } else {
          null
        }
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.GET, docRef.path)
        throw error
      }
  }

  fun observeLeaderboard(limitCount: Long = 50L): Flow<List<LeaderboardEntry>> {
    val query =
      db.collection(LEADERBOARD_COLLECTION)
        .orderBy("totalPurrsEarned", Query.Direction.DESCENDING)
        .limit(limitCount)
    return query
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          LeaderboardEntry::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, LEADERBOARD_COLLECTION)
        throw error
      }
  }

  suspend fun ensureUserProfile(userId: String, rawDisplayName: String?): UserProfile {
    val safeName = sanitizeDisplayName(rawDisplayName)
    val userDocRef = db.collection(USERS_COLLECTION).document(userId)
    try {
      val existing = userDocRef.get().await()
      if (existing.exists()) {
        val loaded =
          existing.toObject(
            UserProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        if (loaded != null) {
          return loaded
        }
      }

      val initialProfile =
        UserProfile(
          userId = userId,
          displayName = safeName,
          purrs = 0L,
          totalPurrsEarned = 0L,
          totalPets = 0L,
          selectedCatName = "Mochi",
          selectedPatternId = "tabby",
          selectedAccessoryId = "none",
          unlockedPatterns = listOf("tabby"),
          unlockedAccessories = listOf("none"),
          upgradeLevels = emptyMap(),
          dailyStreak = 0L,
          lastDailyClaimEpochDay = 0L,
          claimedQuestIds = emptyList(),
          unlockedBadgeIds = emptyList(),
          equippedBadgeId = "",
          eventPurrsEarned = 0L,
          preferredCity = "San Francisco",
          weatherCheckedCount = 0L,
        )

      val profilePayload =
        mapOf(
          "userId" to initialProfile.userId,
          "displayName" to initialProfile.displayName,
          "purrs" to initialProfile.purrs,
          "totalPurrsEarned" to initialProfile.totalPurrsEarned,
          "totalPets" to initialProfile.totalPets,
          "selectedCatName" to initialProfile.selectedCatName,
          "selectedPatternId" to initialProfile.selectedPatternId,
          "selectedAccessoryId" to initialProfile.selectedAccessoryId,
          "unlockedPatterns" to initialProfile.unlockedPatterns,
          "unlockedAccessories" to initialProfile.unlockedAccessories,
          "upgradeLevels" to initialProfile.upgradeLevels,
          "dailyStreak" to initialProfile.dailyStreak,
          "lastDailyClaimEpochDay" to initialProfile.lastDailyClaimEpochDay,
          "claimedQuestIds" to initialProfile.claimedQuestIds,
          "unlockedBadgeIds" to initialProfile.unlockedBadgeIds,
          "equippedBadgeId" to initialProfile.equippedBadgeId,
          "eventPurrsEarned" to initialProfile.eventPurrsEarned,
          "preferredCity" to initialProfile.preferredCity,
          "weatherCheckedCount" to initialProfile.weatherCheckedCount,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )

      userDocRef.set(profilePayload).await()

      val lbDocRef = db.collection(LEADERBOARD_COLLECTION).document(userId)
      val lbPayload = buildLeaderboardPayload(initialProfile)
      lbDocRef.set(lbPayload).await()

      return initialProfile
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, userDocRef.path)
      throw e
    }
  }

  suspend fun saveProfileAndLeaderboard(profile: UserProfile) {
    val userId = profile.userId
    require(userId.isNotBlank()) { "userId must not be blank" }
    val userDocRef = db.collection(USERS_COLLECTION).document(userId)
    val lbDocRef = db.collection(LEADERBOARD_COLLECTION).document(userId)

    val updateFields =
      mapOf(
        "displayName" to sanitizeDisplayName(profile.displayName),
        "purrs" to profile.purrs.coerceAtLeast(0L),
        "totalPurrsEarned" to profile.totalPurrsEarned.coerceAtLeast(0L),
        "totalPets" to profile.totalPets.coerceAtLeast(0L),
        "selectedCatName" to profile.selectedCatName.trim().ifBlank { "Mochi" }.take(24),
        "selectedPatternId" to profile.selectedPatternId,
        "selectedAccessoryId" to profile.selectedAccessoryId,
        "unlockedPatterns" to profile.unlockedPatterns,
        "unlockedAccessories" to profile.unlockedAccessories,
        "upgradeLevels" to profile.upgradeLevels,
        "dailyStreak" to profile.dailyStreak.coerceIn(0L, 3650L),
        "lastDailyClaimEpochDay" to profile.lastDailyClaimEpochDay.coerceAtLeast(0L),
        "claimedQuestIds" to profile.claimedQuestIds,
        "unlockedBadgeIds" to profile.unlockedBadgeIds,
        "equippedBadgeId" to profile.equippedBadgeId.take(40),
        "eventPurrsEarned" to profile.eventPurrsEarned.coerceAtLeast(0L),
        "preferredCity" to profile.preferredCity.take(80),
        "weatherCheckedCount" to profile.weatherCheckedCount.coerceAtLeast(0L),
        "updatedAt" to FieldValue.serverTimestamp(),
      )

    try {
      userDocRef.update(updateFields).await()
      lbDocRef.set(buildLeaderboardPayload(profile)).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, userDocRef.path)
      throw e
    }
  }

  private fun buildLeaderboardPayload(profile: UserProfile): Map<String, Any> {
    return mapOf(
      "userId" to profile.userId,
      "displayName" to sanitizeDisplayName(profile.displayName),
      "catName" to profile.selectedCatName.trim().ifBlank { "Mochi" }.take(24),
      "catPatternId" to profile.selectedPatternId,
      "catAccessoryId" to profile.selectedAccessoryId,
      "equippedBadgeId" to profile.equippedBadgeId.take(40),
      "totalPurrsEarned" to profile.totalPurrsEarned.coerceAtLeast(0L),
      "totalPets" to profile.totalPets.coerceAtLeast(0L),
      "badgesCount" to profile.unlockedBadgeIds.size.toLong().coerceIn(0L, 64L),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
  }

  private fun sanitizeDisplayName(raw: String?): String {
    val cleaned = raw?.trim().orEmpty()
    if (cleaned.isBlank() || cleaned.contains("@")) {
      return cleaned.substringBefore("@").ifBlank { "Feline Friend" }.take(30)
    }
    return cleaned.take(30)
  }

  companion object {
    const val USERS_COLLECTION = "users"
    const val LEADERBOARD_COLLECTION = "leaderboard"
  }
}
