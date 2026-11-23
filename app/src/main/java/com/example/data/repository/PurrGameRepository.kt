package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.GlobalAnnouncement
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardPublicStats
import com.example.data.model.LimitedItemCounter
import com.example.data.model.ModerationRecord
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

  fun observeOwnModeration(userId: String): Flow<ModerationRecord?> {
    val docRef = db.collection(MODERATION_COLLECTION).document(userId)
    return docRef
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(
            ModerationRecord::class.java,
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

  fun observeAllModerationForAdmin(limitCount: Long = 100L): Flow<List<ModerationRecord>> {
    val query = db.collection(MODERATION_COLLECTION).limit(limitCount)
    return query
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          ModerationRecord::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, MODERATION_COLLECTION)
        throw error
      }
  }

  fun observeGlobalAnnouncements(limitCount: Long = 10L): Flow<List<GlobalAnnouncement>> {
    val query =
      db.collection(GLOBAL_MESSAGES_COLLECTION)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(limitCount)
    return query
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          GlobalAnnouncement::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, GLOBAL_MESSAGES_COLLECTION)
        throw error
      }
  }

  fun observeLimitedItemCounters(limitCount: Long = 100L): Flow<Map<String, Long>> {
    val query = db.collection(LIMITED_STATS_COLLECTION).limit(limitCount)
    return query
      .snapshots()
      .map { snapshot ->
        snapshot
          .toObjects(
            LimitedItemCounter::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
          .associate { counter -> counter.itemId to counter.existCount }
      }
      .catch { error ->
        handleFirestoreError(error, OperationType.LIST, LIMITED_STATS_COLLECTION)
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
          selectedBackgroundId = "cozy_parlor",
          unlockedPatterns = listOf("tabby"),
          unlockedAccessories = listOf("none"),
          unlockedBackgrounds = listOf("cozy_parlor"),
          upgradeLevels = emptyMap(),
          dailyStreak = 0L,
          lastDailyClaimEpochDay = 0L,
          claimedQuestIds = emptyList(),
          unlockedBadgeIds = emptyList(),
          equippedBadgeId = "",
          eventPurrsEarned = 0L,
          preferredCity = "San Francisco",
          weatherCheckedCount = 0L,
          statsPublic = true,
          bio = "",
          avatarBase64 = "",
          bannerBase64 = "",
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
          "selectedBackgroundId" to initialProfile.selectedBackgroundId,
          "unlockedPatterns" to initialProfile.unlockedPatterns,
          "unlockedAccessories" to initialProfile.unlockedAccessories,
          "unlockedBackgrounds" to initialProfile.unlockedBackgrounds,
          "upgradeLevels" to initialProfile.upgradeLevels,
          "dailyStreak" to initialProfile.dailyStreak,
          "lastDailyClaimEpochDay" to initialProfile.lastDailyClaimEpochDay,
          "claimedQuestIds" to initialProfile.claimedQuestIds,
          "unlockedBadgeIds" to initialProfile.unlockedBadgeIds,
          "equippedBadgeId" to initialProfile.equippedBadgeId,
          "eventPurrsEarned" to initialProfile.eventPurrsEarned,
          "preferredCity" to initialProfile.preferredCity,
          "weatherCheckedCount" to initialProfile.weatherCheckedCount,
          "statsPublic" to initialProfile.statsPublic,
          "bio" to initialProfile.bio,
          "avatarBase64" to initialProfile.avatarBase64,
          "bannerBase64" to initialProfile.bannerBase64,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )

      userDocRef.set(profilePayload).await()

      val lbDocRef = db.collection(LEADERBOARD_COLLECTION).document(userId)
      val lbPayload = buildLeaderboardPayload(initialProfile, LeaderboardPublicStats())
      lbDocRef.set(lbPayload).await()

      return initialProfile
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, userDocRef.path)
      throw e
    }
  }

  suspend fun saveProfileAndLeaderboard(
    profile: UserProfile,
    publicStats: LeaderboardPublicStats = LeaderboardPublicStats(dailyStreak = profile.dailyStreak),
  ) {
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
        "selectedBackgroundId" to profile.selectedBackgroundId,
        "unlockedPatterns" to profile.unlockedPatterns,
        "unlockedAccessories" to profile.unlockedAccessories,
        "unlockedBackgrounds" to profile.unlockedBackgrounds,
        "upgradeLevels" to profile.upgradeLevels,
        "dailyStreak" to profile.dailyStreak.coerceIn(0L, 36500L),
        "lastDailyClaimEpochDay" to profile.lastDailyClaimEpochDay.coerceAtLeast(0L),
        "claimedQuestIds" to profile.claimedQuestIds,
        "unlockedBadgeIds" to profile.unlockedBadgeIds,
        "equippedBadgeId" to profile.equippedBadgeId.take(40),
        "eventPurrsEarned" to profile.eventPurrsEarned.coerceAtLeast(0L),
        "preferredCity" to profile.preferredCity.take(80),
        "weatherCheckedCount" to profile.weatherCheckedCount.coerceAtLeast(0L),
        "statsPublic" to profile.statsPublic,
        "bio" to profile.bio.trim().take(140),
        "avatarBase64" to profile.avatarBase64.take(60_000),
        "bannerBase64" to profile.bannerBase64.take(120_000),
        "updatedAt" to FieldValue.serverTimestamp(),
      )

    try {
      try {
        userDocRef.update(updateFields).await()
      } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
        if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
          val createPayload =
            updateFields +
              mapOf(
                "userId" to userId,
                "createdAt" to FieldValue.serverTimestamp(),
              )
          userDocRef.set(createPayload).await()
        } else {
          throw e
        }
      }
      lbDocRef.set(buildLeaderboardPayload(profile, publicStats)).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, userDocRef.path)
      throw e
    }
  }

  suspend fun deleteUserCloudData(userId: String) {
    require(userId.isNotBlank()) { "userId must not be blank" }
    val userDocRef = db.collection(USERS_COLLECTION).document(userId)
    val lbDocRef = db.collection(LEADERBOARD_COLLECTION).document(userId)
    try {
      userDocRef.delete().await()
      lbDocRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, userDocRef.path)
      throw e
    }
  }

  suspend fun setPlayerModeration(
    targetUserId: String,
    status: String,
    reason: String,
    timeoutUntilMillis: Long,
    incrementWarning: Boolean,
    adminEmail: String,
  ) {
    require(targetUserId.isNotBlank()) { "targetUserId must not be blank" }
    val docRef = db.collection(MODERATION_COLLECTION).document(targetUserId)
    try {
      val existingSnap = docRef.get().await()
      val existingWarnings =
        if (existingSnap.exists()) {
          (existingSnap.getLong("warningCount") ?: 0L).coerceAtLeast(0L)
        } else {
          0L
        }
      val nextWarnings =
        if (incrementWarning) (existingWarnings + 1L).coerceAtMost(1000L) else existingWarnings
      val payload =
        mapOf(
          "userId" to targetUserId,
          "status" to status,
          "reason" to reason.trim().take(240),
          "timeoutUntilMillis" to timeoutUntilMillis.coerceAtLeast(0L),
          "warningCount" to nextWarnings,
          "updatedByEmail" to adminEmail.trim().take(120),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun clearPlayerModeration(targetUserId: String) {
    val docRef = db.collection(MODERATION_COLLECTION).document(targetUserId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun sendGlobalAnnouncement(
    messageId: String,
    title: String,
    body: String,
    severity: String,
    senderEmail: String,
  ) {
    val docRef = db.collection(GLOBAL_MESSAGES_COLLECTION).document(messageId)
    try {
      val payload =
        mapOf(
          "messageId" to messageId,
          "title" to title.trim().ifBlank { "Sanctuary Notice" }.take(80),
          "body" to body.trim().ifBlank { "Notice from Sanctuary Admin" }.take(320),
          "severity" to severity,
          "senderEmail" to senderEmail.trim().take(120),
          "createdAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteGlobalAnnouncement(messageId: String) {
    val docRef = db.collection(GLOBAL_MESSAGES_COLLECTION).document(messageId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun incrementLimitedItemExistCount(statId: String, itemType: String) {
    val docRef = db.collection(LIMITED_STATS_COLLECTION).document(statId)
    try {
      db.runTransaction { tx ->
        val snap = tx.get(docRef)
        if (!snap.exists()) {
          tx.set(
            docRef,
            mapOf(
              "itemId" to statId,
              "itemType" to itemType,
              "existCount" to 1L,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        } else {
          val current = (snap.getLong("existCount") ?: 0L).coerceAtLeast(0L)
          tx.update(
            docRef,
            mapOf(
              "existCount" to (current + 1L),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      }.await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  private fun buildLeaderboardPayload(
    profile: UserProfile,
    publicStats: LeaderboardPublicStats,
  ): Map<String, Any> {
    return mapOf(
      "userId" to profile.userId,
      "displayName" to sanitizeDisplayName(profile.displayName),
      "catName" to profile.selectedCatName.trim().ifBlank { "Mochi" }.take(24),
      "catPatternId" to profile.selectedPatternId,
      "catAccessoryId" to profile.selectedAccessoryId,
      "equippedBadgeId" to profile.equippedBadgeId.take(40),
      "totalPurrsEarned" to profile.totalPurrsEarned.coerceAtLeast(0L),
      "totalPets" to profile.totalPets.coerceAtLeast(0L),
      "badgesCount" to profile.unlockedBadgeIds.size.toLong().coerceIn(0L, 128L),
      "statsPublic" to profile.statsPublic,
      "bio" to profile.bio.trim().take(140),
      "avatarBase64" to profile.avatarBase64.take(60_000),
      "bannerBase64" to profile.bannerBase64.take(120_000),
      "publicRebirths" to publicStats.rebirths.coerceAtLeast(0L),
      "publicAscensions" to publicStats.ascensions.coerceAtLeast(0L),
      "publicDailyStreak" to publicStats.dailyStreak.coerceAtLeast(0L),
      "publicHighestComboX100" to publicStats.highestComboX100.coerceIn(0L, 100_000L),
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
    const val MODERATION_COLLECTION = "moderation"
    const val GLOBAL_MESSAGES_COLLECTION = "global_messages"
    const val LIMITED_STATS_COLLECTION = "limited_item_stats"
  }
}
