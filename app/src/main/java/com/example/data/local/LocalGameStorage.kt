package com.example.data.local

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.audio.CustomMusicTrack
import com.example.audio.MusicTrack
import com.example.audio.RepeatMode
import com.example.data.model.BattlePassState
import com.example.data.model.ChallengeProgressState
import com.example.data.model.GameCatalog
import com.example.data.model.NavigationLayoutFormat
import com.example.data.model.PlayerStatsState
import com.example.data.model.UserProfile
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.ThemeMode
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.purrDataStore: DataStore<Preferences> by preferencesDataStore(name = "purrtap_local_state")

data class NightRollRecord(
  val epochDay: Long = -1L,
  val isBloodmoon: Boolean = false,
)

data class AppSettingsState(
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val colorTheme: AppColorTheme = AppColorTheme.TERRACOTTA,
  val unlockedColorThemes: List<String> = AppColorTheme.freeThemes.map { it.id },
  val sfxEnabled: Boolean = true,
  val sfxVolume: Float = 0.8f,
  val isMusicPlaying: Boolean = false,
  val musicVolume: Float = 0.55f,
  val selectedTrackId: String = MusicTrack.SUNLIT_WHISKERS.id,
  val customTracks: List<CustomMusicTrack> = emptyList(),
  val repeatMode: RepeatMode = RepeatMode.ALL,
  val shuffleEnabled: Boolean = false,
  val playbackSpeed: Float = 1.0f,
  val navigationFormat: NavigationLayoutFormat = NavigationLayoutFormat.BOTTOM_BAR,
  val accountMode: String = "guest",
  val localAccountCode: String = "",
  val localAccountPin: String = "",
) {
  fun currentTrackTitleAndSubtitle(): Pair<String, String> {
    val custom = customTracks.find { it.id == selectedTrackId }
    if (custom != null) {
      return custom.title to "Uploaded Audio File"
    }
    val builtIn = MusicTrack.fromId(selectedTrackId)
    return builtIn.title to builtIn.subtitle
  }
}

data class DecodedSanctuarySnapshot(
  val profile: UserProfile,
  val challenge: ChallengeProgressState,
  val unlockedThemes: List<String>,
)

data class WidgetSnapshotData(
  val catName: String = "Mochi",
  val coatName: String = "Classic Tabby",
  val activeBalance: Long = 0L,
  val currencyShortLabel: String = "Purrs",
  val isHardcore: Boolean = false,
  val clickPower: Long = 1L,
  val pps: Long = 0L,
  val gems: Long = 0L,
  val dailyStreak: Long = 0L,
  val canClaimDaily: Boolean = true,
  val goldenRainActive: Boolean = false,
  val gemFrenzyActive: Boolean = false,
  val weatherLabel: String = "Sunny",
  val weatherTempC: Int = 22,
  val totalPets: Long = 0L,
)

class LocalGameStorage(context: Context) {
  private val dataStore = context.applicationContext.purrDataStore

  suspend fun loadProfile(): UserProfile {
    val prefs = dataStore.data.first()
    return UserProfile(
      userId = prefs[KEY_LOCAL_UID] ?: "local_caretaker",
      displayName = prefs[KEY_DISPLAY_NAME] ?: "Feline Friend",
      purrs = prefs[KEY_PURRS] ?: 0L,
      totalPurrsEarned = prefs[KEY_TOTAL_PURRS] ?: 0L,
      totalPets = prefs[KEY_TOTAL_PETS] ?: 0L,
      selectedCatName = prefs[KEY_CAT_NAME] ?: "Mochi",
      selectedPatternId = prefs[KEY_PATTERN_ID] ?: "tabby",
      selectedAccessoryId = prefs[KEY_ACCESSORY_ID] ?: "none",
      selectedBackgroundId = prefs[KEY_BACKGROUND_ID] ?: "cozy_parlor",
      unlockedPatterns = decodeStringList(prefs[KEY_UNLOCKED_PATTERNS], listOf("tabby")),
      unlockedAccessories = decodeStringList(prefs[KEY_UNLOCKED_ACCESSORIES], listOf("none")),
      unlockedBackgrounds = decodeStringList(prefs[KEY_UNLOCKED_BACKGROUNDS], listOf("cozy_parlor")),
      upgradeLevels = decodeUpgradeMap(prefs[KEY_UPGRADE_LEVELS]),
      dailyStreak = prefs[KEY_DAILY_STREAK] ?: 0L,
      lastDailyClaimEpochDay = prefs[KEY_LAST_DAILY_DAY] ?: 0L,
      claimedQuestIds = decodeStringList(prefs[KEY_CLAIMED_QUESTS], emptyList()),
      unlockedBadgeIds = decodeStringList(prefs[KEY_UNLOCKED_BADGES], emptyList()),
      equippedBadgeId = prefs[KEY_EQUIPPED_BADGE] ?: "",
      eventPurrsEarned = prefs[KEY_EVENT_PURRS] ?: 0L,
      preferredCity = prefs[KEY_PREFERRED_CITY] ?: "Sanctuary Sky",
      weatherCheckedCount = prefs[KEY_WEATHER_COUNT] ?: 0L,
      statsPublic = prefs[KEY_STATS_PUBLIC] ?: true,
      bio = prefs[KEY_BIO] ?: "",
      avatarBase64 = prefs[KEY_AVATAR_BASE64] ?: "",
      bannerBase64 = prefs[KEY_BANNER_BASE64] ?: "",
    )
  }

  suspend fun saveProfile(profile: UserProfile) {
    dataStore.edit { prefs ->
      if (profile.userId.isNotBlank()) prefs[KEY_LOCAL_UID] = profile.userId
      prefs[KEY_DISPLAY_NAME] = profile.displayName
      prefs[KEY_PURRS] = profile.purrs.coerceAtLeast(0L)
      prefs[KEY_TOTAL_PURRS] = profile.totalPurrsEarned.coerceAtLeast(0L)
      prefs[KEY_TOTAL_PETS] = profile.totalPets.coerceAtLeast(0L)
      prefs[KEY_CAT_NAME] = profile.selectedCatName
      prefs[KEY_PATTERN_ID] = profile.selectedPatternId
      prefs[KEY_ACCESSORY_ID] = profile.selectedAccessoryId
      prefs[KEY_BACKGROUND_ID] = profile.selectedBackgroundId
      prefs[KEY_UNLOCKED_PATTERNS] = encodeStringList(profile.unlockedPatterns)
      prefs[KEY_UNLOCKED_ACCESSORIES] = encodeStringList(profile.unlockedAccessories)
      prefs[KEY_UNLOCKED_BACKGROUNDS] = encodeStringList(profile.unlockedBackgrounds)
      prefs[KEY_UPGRADE_LEVELS] = encodeUpgradeMap(profile.upgradeLevels)
      prefs[KEY_DAILY_STREAK] = profile.dailyStreak.coerceAtLeast(0L)
      prefs[KEY_LAST_DAILY_DAY] = profile.lastDailyClaimEpochDay.coerceAtLeast(0L)
      prefs[KEY_CLAIMED_QUESTS] = encodeStringList(profile.claimedQuestIds)
      prefs[KEY_UNLOCKED_BADGES] = encodeStringList(profile.unlockedBadgeIds)
      prefs[KEY_EQUIPPED_BADGE] = profile.equippedBadgeId
      prefs[KEY_EVENT_PURRS] = profile.eventPurrsEarned.coerceAtLeast(0L)
      prefs[KEY_PREFERRED_CITY] = profile.preferredCity
      prefs[KEY_WEATHER_COUNT] = profile.weatherCheckedCount.coerceAtLeast(0L)
      prefs[KEY_STATS_PUBLIC] = profile.statsPublic
      prefs[KEY_BIO] = profile.bio.take(140)
      prefs[KEY_AVATAR_BASE64] = profile.avatarBase64.take(60_000)
      prefs[KEY_BANNER_BASE64] = profile.bannerBase64.take(120_000)
    }
  }

  suspend fun loadLastSeenBlogVersion(): String {
    val prefs = dataStore.data.first()
    return prefs[KEY_LAST_SEEN_BLOG_VERSION] ?: ""
  }

  suspend fun saveLastSeenBlogVersion(version: String) {
    dataStore.edit { prefs ->
      prefs[KEY_LAST_SEEN_BLOG_VERSION] = version
    }
  }

  suspend fun loadNightRoll(): NightRollRecord {
    val prefs = dataStore.data.first()
    return NightRollRecord(
      epochDay = prefs[KEY_NIGHT_ROLL_DAY] ?: -1L,
      isBloodmoon = prefs[KEY_NIGHT_IS_BLOODMOON] ?: false,
    )
  }

  suspend fun saveNightRoll(epochDay: Long, isBloodmoon: Boolean) {
    dataStore.edit { prefs ->
      prefs[KEY_NIGHT_ROLL_DAY] = epochDay
      prefs[KEY_NIGHT_IS_BLOODMOON] = isBloodmoon
    }
  }

  suspend fun loadChallengeProgress(): ChallengeProgressState {
    val prefs = dataStore.data.first()
    val rebirthCount = (prefs[KEY_REBIRTH_COUNT] ?: 0L).coerceAtLeast(0L)
    val bonusSteps = (prefs[KEY_REBIRTH_BONUS_STEPS] ?: rebirthCount).coerceAtLeast(0L)
    return ChallengeProgressState(
      rebirthCount = rebirthCount,
      rebirthBonusSteps = bonusSteps,
      isHardcoreMode = prefs[KEY_HARDCORE_MODE] ?: false,
      hardcoreUnlocked = prefs[KEY_HARDCORE_UNLOCKED] ?: false,
      hardcorePurrs = (prefs[KEY_HARDCORE_PURRS] ?: 0L).coerceAtLeast(0L),
      hardcoreUpgradeLevels = decodeUpgradeMap(prefs[KEY_HARDCORE_UPGRADES]),
      ascensionCount = (prefs[KEY_ASCENSION_COUNT] ?: 0L).coerceAtLeast(0L),
      gatoShards = (prefs[KEY_GATO_SHARDS] ?: 0L).coerceAtLeast(0L),
      unlockedAscensionIds = decodeStringList(prefs[KEY_ASCENSION_NODES], emptyList()),
      gems = (prefs[KEY_GEMS] ?: 0L).coerceAtLeast(0L),
      totalGemsEarned = (prefs[KEY_TOTAL_GEMS_EARNED] ?: 0L).coerceAtLeast(0L),
      gemCharmLevel = (prefs[KEY_GEM_CHARM_LEVEL] ?: 0L).coerceAtLeast(0L),
      prismLensUnlocked = prefs[KEY_PRISM_LENS_UNLOCKED] ?: false,
      gemFrenzyRemainingMillis = (prefs[KEY_GEM_FRENZY_MS] ?: 0L).coerceAtLeast(0L),
      hackerVaultUnlocked = prefs[KEY_VAULT_UNLOCKED] ?: false,
      vaultKickoutUntilMillis = (prefs[KEY_VAULT_KICKOUT_UNTIL_MS] ?: 0L).coerceAtLeast(0L),
      dogCrashOmenSeen = prefs[KEY_DOG_CRASH_OMEN_SEEN] ?: false,
      trillionCutsceneSeen = prefs[KEY_TRILLION_CUTSCENE_SEEN] ?: false,
      citadelBossGlitchSeen = prefs[KEY_CITADEL_BOSS_GLITCH_SEEN] ?: false,
      catComputerUnlocked = prefs[KEY_CAT_COMPUTER_UNLOCKED] ?: false,
      dogComputerSearched = prefs[KEY_DOG_COMPUTER_SEARCHED] ?: false,
      turkeys = (prefs[KEY_TURKEYS] ?: 0L).coerceAtLeast(0L),
      totalTurkeysEarned = (prefs[KEY_TOTAL_TURKEYS] ?: 0L).coerceAtLeast(0L),
      festivalHourEpoch = (prefs[KEY_FESTIVAL_HOUR] ?: 0L).coerceAtLeast(0L),
      festivalIsMega = prefs[KEY_FESTIVAL_MEGA] ?: false,
      turkeyGatoTalkCount = (prefs[KEY_GATO_TALKS] ?: 0L).toInt().coerceAtLeast(0),
      part2MapUnlocked = prefs[KEY_PART2_MAP] ?: false,
      festivalQuestHourEpoch = (prefs[KEY_FEST_Q_HOUR] ?: 0L).coerceAtLeast(0L),
      claimedFestivalQuestIds = decodeStringList(prefs[KEY_FEST_Q_CLAIMED], emptyList()),
      festHourPets = (prefs[KEY_FEST_H_PETS] ?: 0L).coerceAtLeast(0L),
      festHourTurkeysTapped = (prefs[KEY_FEST_H_TURKEYS] ?: 0L).coerceAtLeast(0L),
      festHourUpgradesBought = (prefs[KEY_FEST_H_UPGRADES] ?: 0L).coerceAtLeast(0L),
      festHourWeatherShifts = (prefs[KEY_FEST_H_WEATHER] ?: 0L).coerceAtLeast(0L),
    )
  }

  suspend fun saveChallengeProgress(state: ChallengeProgressState) {
    dataStore.edit { prefs ->
      prefs[KEY_REBIRTH_COUNT] = state.rebirthCount.coerceAtLeast(0L)
      prefs[KEY_REBIRTH_BONUS_STEPS] = state.rebirthBonusSteps.coerceAtLeast(0L)
      prefs[KEY_HARDCORE_MODE] = state.isHardcoreMode
      prefs[KEY_HARDCORE_UNLOCKED] = state.hardcoreUnlocked
      prefs[KEY_HARDCORE_PURRS] = state.hardcorePurrs.coerceAtLeast(0L)
      prefs[KEY_HARDCORE_UPGRADES] = encodeUpgradeMap(state.hardcoreUpgradeLevels)
      prefs[KEY_ASCENSION_COUNT] = state.ascensionCount.coerceAtLeast(0L)
      prefs[KEY_GATO_SHARDS] = state.gatoShards.coerceAtLeast(0L)
      prefs[KEY_ASCENSION_NODES] = encodeStringList(state.unlockedAscensionIds)
      prefs[KEY_GEMS] = state.gems.coerceAtLeast(0L)
      prefs[KEY_TOTAL_GEMS_EARNED] = state.totalGemsEarned.coerceAtLeast(0L)
      prefs[KEY_GEM_CHARM_LEVEL] = state.gemCharmLevel.coerceAtLeast(0L)
      prefs[KEY_PRISM_LENS_UNLOCKED] = state.prismLensUnlocked
      prefs[KEY_GEM_FRENZY_MS] = state.gemFrenzyRemainingMillis.coerceAtLeast(0L)
      prefs[KEY_VAULT_UNLOCKED] = state.hackerVaultUnlocked
      prefs[KEY_VAULT_KICKOUT_UNTIL_MS] = state.vaultKickoutUntilMillis.coerceAtLeast(0L)
      prefs[KEY_DOG_CRASH_OMEN_SEEN] = state.dogCrashOmenSeen
      prefs[KEY_TRILLION_CUTSCENE_SEEN] = state.trillionCutsceneSeen
      prefs[KEY_CITADEL_BOSS_GLITCH_SEEN] = state.citadelBossGlitchSeen
      prefs[KEY_CAT_COMPUTER_UNLOCKED] = state.catComputerUnlocked
      prefs[KEY_DOG_COMPUTER_SEARCHED] = state.dogComputerSearched
      prefs[KEY_TURKEYS] = state.turkeys.coerceAtLeast(0L)
      prefs[KEY_TOTAL_TURKEYS] = state.totalTurkeysEarned.coerceAtLeast(0L)
      prefs[KEY_FESTIVAL_HOUR] = state.festivalHourEpoch.coerceAtLeast(0L)
      prefs[KEY_FESTIVAL_MEGA] = state.festivalIsMega
      prefs[KEY_GATO_TALKS] = state.turkeyGatoTalkCount.toLong().coerceAtLeast(0L)
      prefs[KEY_PART2_MAP] = state.part2MapUnlocked
      prefs[KEY_FEST_Q_HOUR] = state.festivalQuestHourEpoch.coerceAtLeast(0L)
      prefs[KEY_FEST_Q_CLAIMED] = encodeStringList(state.claimedFestivalQuestIds)
      prefs[KEY_FEST_H_PETS] = state.festHourPets.coerceAtLeast(0L)
      prefs[KEY_FEST_H_TURKEYS] = state.festHourTurkeysTapped.coerceAtLeast(0L)
      prefs[KEY_FEST_H_UPGRADES] = state.festHourUpgradesBought.coerceAtLeast(0L)
      prefs[KEY_FEST_H_WEATHER] = state.festHourWeatherShifts.coerceAtLeast(0L)
    }
  }

  suspend fun loadPlayerStats(nowMillis: Long): PlayerStatsState {
    val prefs = dataStore.data.first()
    val storedJoined = prefs[KEY_STATS_JOINED_MS] ?: 0L
    val joinedMs = if (storedJoined > 0L) storedJoined else nowMillis
    val existingSessions = prefs[KEY_STATS_SESSIONS] ?: 0L
    val nextSessions = existingSessions + 1L
    if (storedJoined <= 0L) {
      dataStore.edit { mutable ->
        mutable[KEY_STATS_JOINED_MS] = joinedMs
        mutable[KEY_STATS_SESSIONS] = nextSessions
      }
    } else {
      dataStore.edit { mutable -> mutable[KEY_STATS_SESSIONS] = nextSessions }
    }
    return PlayerStatsState(
      joinedTimestampMillis = joinedMs,
      lastActiveTimestampMillis = (prefs[KEY_STATS_LAST_ACTIVE_MS] ?: 0L).coerceAtLeast(0L),
      totalTimePlayedSeconds = (prefs[KEY_STATS_TIME_PLAYED_SEC] ?: 0L).coerceAtLeast(0L),
      totalOfflineSeconds = (prefs[KEY_STATS_OFFLINE_SEC] ?: 0L).coerceAtLeast(0L),
      totalOfflinePurrsEarned = (prefs[KEY_STATS_OFFLINE_PURRS] ?: 0L).coerceAtLeast(0L),
      highestComboMultiplier = (prefs[KEY_STATS_HIGHEST_COMBO] ?: 1.0).coerceAtLeast(1.0),
      biggestSingleTapPurrs = (prefs[KEY_STATS_BIGGEST_TAP] ?: 0L).coerceAtLeast(0L),
      totalUpgradesPurchased = (prefs[KEY_STATS_UPGRADES_BOUGHT] ?: 0L).coerceAtLeast(0L),
      totalDailyRewardsClaimed = (prefs[KEY_STATS_DAILY_CLAIMED] ?: 0L).coerceAtLeast(0L),
      longestDailyStreak = (prefs[KEY_STATS_LONGEST_STREAK] ?: 0L).coerceAtLeast(0L),
      lastWeeklyRewardWeekId = (prefs[KEY_STATS_LAST_WEEKLY_ID] ?: 0L).coerceAtLeast(0L),
      weeklyRewardsClaimedCount = (prefs[KEY_STATS_WEEKLY_CLAIMED] ?: 0L).coerceAtLeast(0L),
      totalWeeklyPurrsEarned = (prefs[KEY_STATS_WEEKLY_PURRS] ?: 0L).coerceAtLeast(0L),
      totalPlaySessions = nextSessions,
    )
  }

  suspend fun savePlayerStats(stats: PlayerStatsState) {
    dataStore.edit { prefs ->
      if (stats.joinedTimestampMillis > 0L) {
        prefs[KEY_STATS_JOINED_MS] = stats.joinedTimestampMillis
      }
      prefs[KEY_STATS_LAST_ACTIVE_MS] = stats.lastActiveTimestampMillis.coerceAtLeast(0L)
      prefs[KEY_STATS_TIME_PLAYED_SEC] = stats.totalTimePlayedSeconds.coerceAtLeast(0L)
      prefs[KEY_STATS_OFFLINE_SEC] = stats.totalOfflineSeconds.coerceAtLeast(0L)
      prefs[KEY_STATS_OFFLINE_PURRS] = stats.totalOfflinePurrsEarned.coerceAtLeast(0L)
      prefs[KEY_STATS_HIGHEST_COMBO] = stats.highestComboMultiplier.coerceAtLeast(1.0)
      prefs[KEY_STATS_BIGGEST_TAP] = stats.biggestSingleTapPurrs.coerceAtLeast(0L)
      prefs[KEY_STATS_UPGRADES_BOUGHT] = stats.totalUpgradesPurchased.coerceAtLeast(0L)
      prefs[KEY_STATS_DAILY_CLAIMED] = stats.totalDailyRewardsClaimed.coerceAtLeast(0L)
      prefs[KEY_STATS_LONGEST_STREAK] = stats.longestDailyStreak.coerceAtLeast(0L)
      prefs[KEY_STATS_LAST_WEEKLY_ID] = stats.lastWeeklyRewardWeekId.coerceAtLeast(0L)
      prefs[KEY_STATS_WEEKLY_CLAIMED] = stats.weeklyRewardsClaimedCount.coerceAtLeast(0L)
      prefs[KEY_STATS_WEEKLY_PURRS] = stats.totalWeeklyPurrsEarned.coerceAtLeast(0L)
      prefs[KEY_STATS_SESSIONS] = stats.totalPlaySessions.coerceAtLeast(1L)
    }
  }

  suspend fun updateLastActiveTimestamp(timestampMillis: Long) {
    dataStore.edit { prefs ->
      prefs[KEY_STATS_LAST_ACTIVE_MS] = timestampMillis.coerceAtLeast(0L)
    }
  }

  suspend fun loadBattlePassState(): BattlePassState {
    val prefs = dataStore.data.first()
    return BattlePassState(
      seasonId = "november_pass",
      passXp = (prefs[KEY_PASS_XP] ?: 0L).coerceAtLeast(0L),
      vipUnlocked = prefs[KEY_PASS_VIP_UNLOCKED] ?: false,
      passCompletions = (prefs[KEY_PASS_COMPLETIONS] ?: 0L).coerceAtLeast(0L),
      claimedFreeTiers = decodeIntList(prefs[KEY_PASS_CLAIMED_FREE]),
      claimedVipTiers = decodeIntList(prefs[KEY_PASS_CLAIMED_VIP]),
      goldenTouchEquippedMillis = (prefs[KEY_PASS_GOLDEN_EQUIPPED_MS] ?: 0L).coerceAtLeast(0L),
      goldenRainRemainingMillis = (prefs[KEY_PASS_GOLDEN_RAIN_MS] ?: 0L).coerceAtLeast(0L),
      questHourEpoch = (prefs[KEY_PASS_QUEST_HOUR] ?: 0L).coerceAtLeast(0L),
      activeQuestIds = decodeStringList(prefs[KEY_PASS_ACTIVE_QUESTS], emptyList()),
      claimedPassQuestIds = decodeStringList(prefs[KEY_PASS_CLAIMED_QUESTS], emptyList()),
      hourPets = (prefs[KEY_PASS_HOUR_PETS] ?: 0L).coerceAtLeast(0L),
      hourPurrsEarned = (prefs[KEY_PASS_HOUR_PURRS] ?: 0L).coerceAtLeast(0L),
      hourUpgradesBought = (prefs[KEY_PASS_HOUR_UPGRADES] ?: 0L).coerceAtLeast(0L),
      hourWeatherShifts = (prefs[KEY_PASS_HOUR_WEATHER] ?: 0L).coerceAtLeast(0L),
      hourComboCrits = (prefs[KEY_PASS_HOUR_CRITS] ?: 0L).coerceAtLeast(0L),
    )
  }

  suspend fun saveBattlePassState(state: BattlePassState) {
    dataStore.edit { prefs ->
      prefs[KEY_PASS_XP] = state.passXp.coerceAtLeast(0L)
      prefs[KEY_PASS_VIP_UNLOCKED] = state.vipUnlocked
      prefs[KEY_PASS_COMPLETIONS] = state.passCompletions.coerceAtLeast(0L)
      prefs[KEY_PASS_CLAIMED_FREE] = encodeIntList(state.claimedFreeTiers)
      prefs[KEY_PASS_CLAIMED_VIP] = encodeIntList(state.claimedVipTiers)
      prefs[KEY_PASS_GOLDEN_EQUIPPED_MS] = state.goldenTouchEquippedMillis.coerceAtLeast(0L)
      prefs[KEY_PASS_GOLDEN_RAIN_MS] = state.goldenRainRemainingMillis.coerceAtLeast(0L)
      prefs[KEY_PASS_QUEST_HOUR] = state.questHourEpoch.coerceAtLeast(0L)
      prefs[KEY_PASS_ACTIVE_QUESTS] = encodeStringList(state.activeQuestIds)
      prefs[KEY_PASS_CLAIMED_QUESTS] = encodeStringList(state.claimedPassQuestIds)
      prefs[KEY_PASS_HOUR_PETS] = state.hourPets.coerceAtLeast(0L)
      prefs[KEY_PASS_HOUR_PURRS] = state.hourPurrsEarned.coerceAtLeast(0L)
      prefs[KEY_PASS_HOUR_UPGRADES] = state.hourUpgradesBought.coerceAtLeast(0L)
      prefs[KEY_PASS_HOUR_WEATHER] = state.hourWeatherShifts.coerceAtLeast(0L)
      prefs[KEY_PASS_HOUR_CRITS] = state.hourComboCrits.coerceAtLeast(0L)
    }
  }

  suspend fun loadSettings(): AppSettingsState {
    val prefs = dataStore.data.first()
    val defaultFreeThemes = AppColorTheme.freeThemes.map { it.id }
    val unlockedThemes =
      (defaultFreeThemes + decodeStringList(prefs[KEY_UNLOCKED_THEMES], defaultFreeThemes)).distinct()
    val candidateTheme = AppColorTheme.fromId(prefs[KEY_COLOR_THEME] ?: AppColorTheme.TERRACOTTA.id)
    val safeTheme =
      if (candidateTheme.isFree || candidateTheme.id in unlockedThemes) candidateTheme
      else AppColorTheme.TERRACOTTA
    val customTracks = decodeCustomTracks(prefs[KEY_CUSTOM_TRACKS])
    val rawTrackId = prefs[KEY_MUSIC_TRACK] ?: MusicTrack.SUNLIT_WHISKERS.id
    val validTrackId =
      if (customTracks.any { it.id == rawTrackId }) rawTrackId
      else MusicTrack.fromId(rawTrackId).id
    return AppSettingsState(
      themeMode = ThemeMode.fromId(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.id),
      colorTheme = safeTheme,
      unlockedColorThemes = unlockedThemes,
      sfxEnabled = prefs[KEY_SFX_ENABLED] ?: true,
      sfxVolume = (prefs[KEY_SFX_VOLUME] ?: 0.8f).coerceIn(0f, 1f),
      isMusicPlaying = prefs[KEY_MUSIC_ENABLED] ?: false,
      musicVolume = (prefs[KEY_MUSIC_VOLUME] ?: 0.55f).coerceIn(0f, 1f),
      selectedTrackId = validTrackId,
      customTracks = customTracks,
      repeatMode = RepeatMode.fromId(prefs[KEY_MUSIC_REPEAT_MODE] ?: RepeatMode.ALL.id),
      shuffleEnabled = prefs[KEY_MUSIC_SHUFFLE] ?: false,
      playbackSpeed = (prefs[KEY_MUSIC_SPEED] ?: 1.0f).coerceIn(0.75f, 1.5f),
      navigationFormat =
        NavigationLayoutFormat.fromId(
          prefs[KEY_NAVIGATION_FORMAT] ?: NavigationLayoutFormat.BOTTOM_BAR.id
        ),
      accountMode = prefs[KEY_ACCOUNT_MODE] ?: "guest",
      localAccountCode = prefs[KEY_LOCAL_ACCOUNT_CODE] ?: "",
      localAccountPin = prefs[KEY_LOCAL_ACCOUNT_PIN] ?: "",
    )
  }

  suspend fun saveSettings(settings: AppSettingsState) {
    dataStore.edit { prefs ->
      prefs[KEY_THEME_MODE] = settings.themeMode.id
      prefs[KEY_COLOR_THEME] = settings.colorTheme.id
      prefs[KEY_UNLOCKED_THEMES] = encodeStringList(settings.unlockedColorThemes)
      prefs[KEY_SFX_ENABLED] = settings.sfxEnabled
      prefs[KEY_SFX_VOLUME] = settings.sfxVolume
      prefs[KEY_MUSIC_ENABLED] = settings.isMusicPlaying
      prefs[KEY_MUSIC_VOLUME] = settings.musicVolume
      prefs[KEY_MUSIC_TRACK] = settings.selectedTrackId
      prefs[KEY_CUSTOM_TRACKS] = encodeCustomTracks(settings.customTracks)
      prefs[KEY_MUSIC_REPEAT_MODE] = settings.repeatMode.id
      prefs[KEY_MUSIC_SHUFFLE] = settings.shuffleEnabled
      prefs[KEY_MUSIC_SPEED] = settings.playbackSpeed
      prefs[KEY_NAVIGATION_FORMAT] = settings.navigationFormat.id
      prefs[KEY_ACCOUNT_MODE] = settings.accountMode
      prefs[KEY_LOCAL_ACCOUNT_CODE] = settings.localAccountCode
      prefs[KEY_LOCAL_ACCOUNT_PIN] = settings.localAccountPin
    }
  }

  suspend fun loadLimitedExistCounts(): Map<String, Long> {
    val prefs = dataStore.data.first()
    return decodeUpgradeMap(prefs[KEY_LOCAL_EXIST_COUNTS])
  }

  suspend fun saveLimitedExistCounts(counts: Map<String, Long>) {
    dataStore.edit { prefs ->
      prefs[KEY_LOCAL_EXIST_COUNTS] = encodeUpgradeMap(counts)
    }
  }

  suspend fun loadDismissedAnnouncementId(): String {
    val prefs = dataStore.data.first()
    return prefs[KEY_DISMISSED_ANNOUNCEMENT_ID] ?: ""
  }

  suspend fun saveDismissedAnnouncementId(messageId: String) {
    dataStore.edit { prefs ->
      prefs[KEY_DISMISSED_ANNOUNCEMENT_ID] = messageId
    }
  }

  suspend fun saveWidgetWeatherSnapshot(label: String, tempC: Int) {
    dataStore.edit { prefs ->
      prefs[KEY_WIDGET_WEATHER_LABEL] = label
      prefs[KEY_WIDGET_WEATHER_TEMP] = tempC.toLong()
    }
  }

  suspend fun loadWidgetSnapshot(nowMillis: Long = System.currentTimeMillis()): WidgetSnapshotData {
    val prefs = dataStore.data.first()
    val profile = loadProfile()
    val challenge = loadChallengeProgress()
    val pass = loadBattlePassState()
    val weatherLabel = prefs[KEY_WIDGET_WEATHER_LABEL] ?: "Sanctuary Sky"
    val weatherTemp = (prefs[KEY_WIDGET_WEATHER_TEMP] ?: 22L).toInt()
    return buildWidgetSnapshot(profile, challenge, pass, weatherLabel, weatherTemp, nowMillis)
  }

  suspend fun recordWidgetPetTap(nowMillis: Long = System.currentTimeMillis()): WidgetSnapshotData {
    val prefs = dataStore.data.first()
    val profile = loadProfile()
    val challenge = loadChallengeProgress()
    val pass = loadBattlePassState()
    val weatherLabel = prefs[KEY_WIDGET_WEATHER_LABEL] ?: "Sanctuary Sky"
    val weatherTemp = (prefs[KEY_WIDGET_WEATHER_TEMP] ?: 22L).toInt()
    val preSnap = buildWidgetSnapshot(profile, challenge, pass, weatherLabel, weatherTemp, nowMillis)
    val tapGain = preSnap.clickPower.coerceAtLeast(1L)

    val nextProfile =
      profile.copy(
        purrs = if (challenge.isHardcoreMode) profile.purrs else profile.purrs + tapGain,
        totalPurrsEarned = profile.totalPurrsEarned + tapGain,
        totalPets = profile.totalPets + 1L,
        eventPurrsEarned = profile.eventPurrsEarned + tapGain,
      )
    val nextChallenge =
      if (challenge.isHardcoreMode) challenge.copy(hardcorePurrs = challenge.hardcorePurrs + tapGain)
      else challenge
    val hourEpoch = GameCatalog.currentEpochHour(nowMillis)
    val alignedPass =
      if (pass.questHourEpoch != hourEpoch || pass.activeQuestIds.isEmpty()) {
        pass.copy(
          questHourEpoch = hourEpoch,
          activeQuestIds = GameCatalog.selectHourlyPassQuestIds(hourEpoch),
          claimedPassQuestIds = emptyList(),
          hourPets = 0L,
          hourPurrsEarned = 0L,
          hourUpgradesBought = 0L,
          hourWeatherShifts = 0L,
          hourComboCrits = 0L,
        )
      } else {
        pass
      }
    val nextPass =
      alignedPass.copy(
        passXp = alignedPass.passXp + 2L,
        hourPets = alignedPass.hourPets + 1L,
        hourPurrsEarned = alignedPass.hourPurrsEarned + tapGain,
      )

    saveProfile(nextProfile)
    if (challenge.isHardcoreMode) {
      saveChallengeProgress(nextChallenge)
    }
    saveBattlePassState(nextPass)
    return buildWidgetSnapshot(nextProfile, nextChallenge, nextPass, weatherLabel, weatherTemp, nowMillis)
  }

  private fun buildWidgetSnapshot(
    profile: UserProfile,
    challenge: ChallengeProgressState,
    pass: BattlePassState,
    weatherLabel: String,
    weatherTemp: Int,
    nowMillis: Long,
  ): WidgetSnapshotData {
    val isHardcore = challenge.isHardcoreMode
    val upgrades = if (isHardcore) challenge.hardcoreUpgradeLevels else profile.upgradeLevels
    val basePps = GameCatalog.computeBasePps(upgrades)
    val baseClick =
      GameCatalog.computeClickPower(upgrades) +
        if ("asc_click_supernova" in challenge.unlockedAscensionIds) (basePps * 0.10).roundToLong()
        else 0L
    val rebirthMult = GameCatalog.rebirthMultiplier(challenge.rebirthBonusSteps, challenge.unlockedAscensionIds)
    val ascMult = if ("asc_golden_whiskers" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val sovereignMult = if ("asc_cosmic_sovereign" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val supernovaMult = if ("asc_click_supernova" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val symphonyMult = if ("asc_passive_symphony" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val charmMult = GameCatalog.gemCharmMultiplier(challenge.gemCharmLevel)
    val frenzyMult = if (challenge.gemFrenzyRemainingMillis > 0L) GameCatalog.GEM_FRENZY_MULTIPLIER else 1.0
    val goldenRainActive = pass.goldenRainRemainingMillis > 0L
    val goldenMult = if (goldenRainActive) GameCatalog.GOLDEN_RAIN_MULTIPLIER else 1.0
    val hcMult = if (isHardcore) GameCatalog.HARDCORE_EARNINGS_MULTIPLIER else 1.0
    val eventMult = GameCatalog.currentPeriodicEvent(nowMillis).first.clickMultiplier
    val totalMult = rebirthMult * ascMult * sovereignMult * charmMult * frenzyMult * goldenMult * hcMult * eventMult
    val clickPower = (baseClick * totalMult * supernovaMult).roundToLong().coerceAtLeast(1L)
    val pps = if (basePps <= 0L) 0L else (basePps * totalMult * symphonyMult).roundToLong().coerceAtLeast(1L)
    val epochDay = nowMillis / 86_400_000L
    val coatName = GameCatalog.patterns.find { it.id == profile.selectedPatternId }?.name ?: "Classic Tabby"
    return WidgetSnapshotData(
      catName = profile.selectedCatName,
      coatName = coatName,
      activeBalance = if (isHardcore) challenge.hardcorePurrs else profile.purrs,
      currencyShortLabel = if (isHardcore) "HC Purrs" else "Purrs",
      isHardcore = isHardcore,
      clickPower = clickPower,
      pps = pps,
      gems = challenge.gems,
      dailyStreak = profile.dailyStreak,
      canClaimDaily = profile.lastDailyClaimEpochDay < epochDay,
      goldenRainActive = goldenRainActive,
      gemFrenzyActive = challenge.gemFrenzyRemainingMillis > 0L,
      weatherLabel = weatherLabel,
      weatherTempC = weatherTemp,
      totalPets = profile.totalPets,
    )
  }

  suspend fun clearAllLocalData(context: Context) {
    dataStore.edit { prefs -> prefs.clear() }
    runCatching {
      val customMusicDir = File(context.applicationContext.filesDir, "custom_music")
      if (customMusicDir.exists()) {
        customMusicDir.deleteRecursively()
      }
    }
  }

  fun generateSanctuaryCode(): String {
    val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    val part1 = (1..4).map { alphabet[kotlin.random.Random.nextInt(alphabet.length)] }.joinToString("")
    val part2 = (1..4).map { alphabet[kotlin.random.Random.nextInt(alphabet.length)] }.joinToString("")
    return "PURR-$part1-$part2"
  }

  fun exportSaveCode(
    profile: UserProfile,
    challenge: ChallengeProgressState,
    unlockedThemes: List<String>,
  ): String {
    val root =
      JSONObject()
        .put("v", 9)
        .put("displayName", profile.displayName)
        .put("purrs", profile.purrs)
        .put("totalPurrs", profile.totalPurrsEarned)
        .put("totalPets", profile.totalPets)
        .put("catName", profile.selectedCatName)
        .put("patternId", profile.selectedPatternId)
        .put("accessoryId", profile.selectedAccessoryId)
        .put("patterns", JSONArray(profile.unlockedPatterns))
        .put("accessories", JSONArray(profile.unlockedAccessories))
        .put("upgrades", encodeUpgradeMap(profile.upgradeLevels))
        .put("streak", profile.dailyStreak)
        .put("lastDaily", profile.lastDailyClaimEpochDay)
        .put("quests", JSONArray(profile.claimedQuestIds))
        .put("badges", JSONArray(profile.unlockedBadgeIds))
        .put("equippedBadge", profile.equippedBadgeId)
        .put("eventPurrs", profile.eventPurrsEarned)
        .put("weatherCount", profile.weatherCheckedCount)
        .put("rebirths", challenge.rebirthCount)
        .put("rebirthSteps", challenge.rebirthBonusSteps)
        .put("hcMode", challenge.isHardcoreMode)
        .put("hcUnlocked", challenge.hardcoreUnlocked)
        .put("hcPurrs", challenge.hardcorePurrs)
        .put("hcUpgrades", encodeUpgradeMap(challenge.hardcoreUpgradeLevels))
        .put("ascensions", challenge.ascensionCount)
        .put("shards", challenge.gatoShards)
        .put("ascNodes", JSONArray(challenge.unlockedAscensionIds))
        .put("gems", challenge.gems)
        .put("totalGems", challenge.totalGemsEarned)
        .put("gemCharm", challenge.gemCharmLevel)
        .put("prismLens", challenge.prismLensUnlocked)
        .put("vaultUnlocked", challenge.hackerVaultUnlocked)
        .put("dogCrashOmenSeen", challenge.dogCrashOmenSeen)
        .put("trillionCutsceneSeen", challenge.trillionCutsceneSeen)
        .put("citadelBossGlitchSeen", challenge.citadelBossGlitchSeen)
        .put("catComputerUnlocked", challenge.catComputerUnlocked)
        .put("dogComputerSearched", challenge.dogComputerSearched)
        .put("turkeys", challenge.turkeys)
        .put("totalTurkeys", challenge.totalTurkeysEarned)
        .put("gatoTalks", challenge.turkeyGatoTalkCount)
        .put("part2Map", challenge.part2MapUnlocked)
        .put("themes", JSONArray(unlockedThemes))
    val rawBytes = root.toString().toByteArray(StandardCharsets.UTF_8)
    val encoded = Base64.encodeToString(rawBytes, Base64.NO_WRAP)
    return "PURRSAVE1:$encoded"
  }

  fun parseSaveCode(rawCode: String): DecodedSanctuarySnapshot? {
    val trimmed = rawCode.trim()
    if (!trimmed.startsWith("PURRSAVE1:")) return null
    val payload = trimmed.removePrefix("PURRSAVE1:").trim()
    if (payload.isBlank()) return null
    return runCatching {
      val jsonBytes = Base64.decode(payload, Base64.DEFAULT)
      val obj = JSONObject(String(jsonBytes, StandardCharsets.UTF_8))
      val profile =
        UserProfile(
          userId = "local_caretaker",
          displayName = obj.optString("displayName", "Feline Friend").take(24).ifBlank { "Feline Friend" },
          purrs = obj.optLong("purrs", 0L).coerceAtLeast(0L),
          totalPurrsEarned = obj.optLong("totalPurrs", 0L).coerceAtLeast(0L),
          totalPets = obj.optLong("totalPets", 0L).coerceAtLeast(0L),
          selectedCatName = obj.optString("catName", "Mochi").take(24).ifBlank { "Mochi" },
          selectedPatternId = obj.optString("patternId", "tabby"),
          selectedAccessoryId = obj.optString("accessoryId", "none"),
          unlockedPatterns =
            (listOf("tabby") + decodeStringList(obj.optJSONArray("patterns")?.toString(), listOf("tabby"))).distinct(),
          unlockedAccessories =
            (listOf("none") + decodeStringList(obj.optJSONArray("accessories")?.toString(), listOf("none"))).distinct(),
          upgradeLevels = decodeUpgradeMap(obj.optString("upgrades")),
          dailyStreak = obj.optLong("streak", 0L).coerceAtLeast(0L),
          lastDailyClaimEpochDay = obj.optLong("lastDaily", 0L).coerceAtLeast(0L),
          claimedQuestIds = decodeStringList(obj.optJSONArray("quests")?.toString(), emptyList()),
          unlockedBadgeIds = decodeStringList(obj.optJSONArray("badges")?.toString(), emptyList()),
          equippedBadgeId = obj.optString("equippedBadge", ""),
          eventPurrsEarned = obj.optLong("eventPurrs", 0L).coerceAtLeast(0L),
          weatherCheckedCount = obj.optLong("weatherCount", 0L).coerceAtLeast(0L),
        )
      val challenge =
        ChallengeProgressState(
          rebirthCount = obj.optLong("rebirths", 0L).coerceAtLeast(0L),
          rebirthBonusSteps = obj.optLong("rebirthSteps", 0L).coerceAtLeast(0L),
          isHardcoreMode = obj.optBoolean("hcMode", false),
          hardcoreUnlocked = obj.optBoolean("hcUnlocked", false),
          hardcorePurrs = obj.optLong("hcPurrs", 0L).coerceAtLeast(0L),
          hardcoreUpgradeLevels = decodeUpgradeMap(obj.optString("hcUpgrades")),
          ascensionCount = obj.optLong("ascensions", 0L).coerceAtLeast(0L),
          gatoShards = obj.optLong("shards", 0L).coerceAtLeast(0L),
          unlockedAscensionIds = decodeStringList(obj.optJSONArray("ascNodes")?.toString(), emptyList()),
          gems = obj.optLong("gems", 0L).coerceAtLeast(0L),
          totalGemsEarned = obj.optLong("totalGems", 0L).coerceAtLeast(0L),
          gemCharmLevel = obj.optLong("gemCharm", 0L).coerceAtLeast(0L),
          prismLensUnlocked = obj.optBoolean("prismLens", false),
          hackerVaultUnlocked = obj.optBoolean("vaultUnlocked", false),
          dogCrashOmenSeen = obj.optBoolean("dogCrashOmenSeen", false),
          trillionCutsceneSeen = obj.optBoolean("trillionCutsceneSeen", false),
          citadelBossGlitchSeen = obj.optBoolean("citadelBossGlitchSeen", false),
          catComputerUnlocked = obj.optBoolean("catComputerUnlocked", false),
          dogComputerSearched = obj.optBoolean("dogComputerSearched", false),
          turkeys = obj.optLong("turkeys", 0L).coerceAtLeast(0L),
          totalTurkeysEarned = obj.optLong("totalTurkeys", 0L).coerceAtLeast(0L),
          turkeyGatoTalkCount = obj.optInt("gatoTalks", 0).coerceAtLeast(0),
          part2MapUnlocked = obj.optBoolean("part2Map", false),
        )
      val themes =
        (AppColorTheme.freeThemes.map { it.id } +
            decodeStringList(obj.optJSONArray("themes")?.toString(), emptyList()))
          .distinct()
      DecodedSanctuarySnapshot(profile = profile, challenge = challenge, unlockedThemes = themes)
    }.getOrNull()
  }

  private fun encodeStringList(list: List<String>): String {
    return JSONArray(list).toString()
  }

  private fun decodeStringList(raw: String?, fallback: List<String>): List<String> {
    if (raw.isNullOrBlank()) return fallback
    return runCatching {
      val arr = JSONArray(raw)
      buildList {
        for (i in 0 until arr.length()) {
          val v = arr.optString(i)
          if (v.isNotBlank()) add(v)
        }
      }.ifEmpty { fallback }
    }.getOrDefault(fallback)
  }

  private fun encodeIntList(list: List<Int>): String {
    return JSONArray(list.distinct().sorted()).toString()
  }

  private fun decodeIntList(raw: String?): List<Int> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
      val arr = JSONArray(raw)
      buildList {
        for (i in 0 until arr.length()) {
          add(arr.optInt(i))
        }
      }.distinct().sorted()
    }.getOrDefault(emptyList())
  }

  private fun encodeUpgradeMap(map: Map<String, Long>): String {
    val obj = JSONObject()
    map.forEach { (k, v) -> obj.put(k, v) }
    return obj.toString()
  }

  private fun decodeUpgradeMap(raw: String?): Map<String, Long> {
    if (raw.isNullOrBlank()) return emptyMap()
    return runCatching {
      val obj = JSONObject(raw)
      buildMap {
        obj.keys().forEach { key ->
          put(key, obj.optLong(key, 0L))
        }
      }
    }.getOrDefault(emptyMap())
  }

  private fun encodeCustomTracks(tracks: List<CustomMusicTrack>): String {
    val arr = JSONArray()
    tracks.forEach { t ->
      arr.put(
        JSONObject()
          .put("id", t.id)
          .put("title", t.title)
          .put("filePath", t.filePath)
      )
    }
    return arr.toString()
  }

  private fun decodeCustomTracks(raw: String?): List<CustomMusicTrack> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
      val arr = JSONArray(raw)
      buildList {
        for (i in 0 until arr.length()) {
          val obj = arr.optJSONObject(i) ?: continue
          val id = obj.optString("id")
          val title = obj.optString("title")
          val filePath = obj.optString("filePath")
          if (id.isNotBlank() && title.isNotBlank() && filePath.isNotBlank()) {
            add(CustomMusicTrack(id = id, title = title, filePath = filePath))
          }
        }
      }
    }.getOrDefault(emptyList())
  }

  companion object {
    private val KEY_LOCAL_UID = stringPreferencesKey("local_uid")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    private val KEY_PURRS = longPreferencesKey("purrs")
    private val KEY_TOTAL_PURRS = longPreferencesKey("total_purrs")
    private val KEY_TOTAL_PETS = longPreferencesKey("total_pets")
    private val KEY_CAT_NAME = stringPreferencesKey("cat_name")
    private val KEY_PATTERN_ID = stringPreferencesKey("pattern_id")
    private val KEY_ACCESSORY_ID = stringPreferencesKey("accessory_id")
    private val KEY_BACKGROUND_ID = stringPreferencesKey("background_id")
    private val KEY_UNLOCKED_PATTERNS = stringPreferencesKey("unlocked_patterns")
    private val KEY_UNLOCKED_ACCESSORIES = stringPreferencesKey("unlocked_accessories")
    private val KEY_UNLOCKED_BACKGROUNDS = stringPreferencesKey("unlocked_backgrounds")
    private val KEY_LAST_SEEN_BLOG_VERSION = stringPreferencesKey("last_seen_blog_version")
    private val KEY_UPGRADE_LEVELS = stringPreferencesKey("upgrade_levels")
    private val KEY_DAILY_STREAK = longPreferencesKey("daily_streak")
    private val KEY_LAST_DAILY_DAY = longPreferencesKey("last_daily_day")
    private val KEY_CLAIMED_QUESTS = stringPreferencesKey("claimed_quests")
    private val KEY_UNLOCKED_BADGES = stringPreferencesKey("unlocked_badges")
    private val KEY_EQUIPPED_BADGE = stringPreferencesKey("equipped_badge")
    private val KEY_EVENT_PURRS = longPreferencesKey("event_purrs")
    private val KEY_PREFERRED_CITY = stringPreferencesKey("preferred_city")
    private val KEY_WEATHER_COUNT = longPreferencesKey("weather_count")
    private val KEY_NIGHT_ROLL_DAY = longPreferencesKey("night_roll_day")
    private val KEY_NIGHT_IS_BLOODMOON = booleanPreferencesKey("night_is_bloodmoon")

    private val KEY_THEME_MODE = stringPreferencesKey("settings_theme_mode")
    private val KEY_COLOR_THEME = stringPreferencesKey("settings_color_theme")
    private val KEY_UNLOCKED_THEMES = stringPreferencesKey("settings_unlocked_themes")
    private val KEY_SFX_ENABLED = booleanPreferencesKey("settings_sfx_enabled")
    private val KEY_SFX_VOLUME = floatPreferencesKey("settings_sfx_volume")
    private val KEY_MUSIC_ENABLED = booleanPreferencesKey("settings_music_enabled")
    private val KEY_MUSIC_VOLUME = floatPreferencesKey("settings_music_volume")
    private val KEY_MUSIC_TRACK = stringPreferencesKey("settings_music_track")
    private val KEY_CUSTOM_TRACKS = stringPreferencesKey("settings_custom_tracks")
    private val KEY_REBIRTH_COUNT = longPreferencesKey("challenge_rebirth_count")
    private val KEY_REBIRTH_BONUS_STEPS = longPreferencesKey("challenge_rebirth_bonus_steps")
    private val KEY_HARDCORE_MODE = booleanPreferencesKey("challenge_hardcore_mode")
    private val KEY_HARDCORE_UNLOCKED = booleanPreferencesKey("challenge_hardcore_unlocked")
    private val KEY_HARDCORE_PURRS = longPreferencesKey("challenge_hardcore_purrs")
    private val KEY_HARDCORE_UPGRADES = stringPreferencesKey("challenge_hardcore_upgrades")
    private val KEY_ASCENSION_COUNT = longPreferencesKey("challenge_ascension_count")
    private val KEY_GATO_SHARDS = longPreferencesKey("challenge_gato_shards")
    private val KEY_ASCENSION_NODES = stringPreferencesKey("challenge_ascension_nodes")

    private val KEY_STATS_JOINED_MS = longPreferencesKey("stats_joined_ms")
    private val KEY_STATS_LAST_ACTIVE_MS = longPreferencesKey("stats_last_active_ms")
    private val KEY_STATS_TIME_PLAYED_SEC = longPreferencesKey("stats_time_played_sec")
    private val KEY_STATS_OFFLINE_SEC = longPreferencesKey("stats_offline_sec")
    private val KEY_STATS_OFFLINE_PURRS = longPreferencesKey("stats_offline_purrs")
    private val KEY_STATS_HIGHEST_COMBO = doublePreferencesKey("stats_highest_combo")
    private val KEY_STATS_BIGGEST_TAP = longPreferencesKey("stats_biggest_tap")
    private val KEY_STATS_UPGRADES_BOUGHT = longPreferencesKey("stats_upgrades_bought")
    private val KEY_STATS_DAILY_CLAIMED = longPreferencesKey("stats_daily_claimed")
    private val KEY_STATS_LONGEST_STREAK = longPreferencesKey("stats_longest_streak")
    private val KEY_STATS_LAST_WEEKLY_ID = longPreferencesKey("stats_last_weekly_id")
    private val KEY_STATS_WEEKLY_CLAIMED = longPreferencesKey("stats_weekly_claimed")
    private val KEY_STATS_WEEKLY_PURRS = longPreferencesKey("stats_weekly_purrs")
    private val KEY_STATS_SESSIONS = longPreferencesKey("stats_sessions")

    private val KEY_PASS_XP = longPreferencesKey("pass_november_xp")
    private val KEY_PASS_VIP_UNLOCKED = booleanPreferencesKey("pass_november_vip")
    private val KEY_PASS_CLAIMED_FREE = stringPreferencesKey("pass_november_claimed_free")
    private val KEY_PASS_CLAIMED_VIP = stringPreferencesKey("pass_november_claimed_vip")
    private val KEY_PASS_GOLDEN_EQUIPPED_MS = longPreferencesKey("pass_golden_equipped_ms")
    private val KEY_PASS_GOLDEN_RAIN_MS = longPreferencesKey("pass_golden_rain_ms")
    private val KEY_PASS_QUEST_HOUR = longPreferencesKey("pass_quest_hour_epoch")
    private val KEY_PASS_ACTIVE_QUESTS = stringPreferencesKey("pass_active_quests")
    private val KEY_PASS_CLAIMED_QUESTS = stringPreferencesKey("pass_claimed_quests")
    private val KEY_PASS_HOUR_PETS = longPreferencesKey("pass_hour_pets")
    private val KEY_PASS_HOUR_PURRS = longPreferencesKey("pass_hour_purrs")
    private val KEY_PASS_HOUR_UPGRADES = longPreferencesKey("pass_hour_upgrades")
    private val KEY_PASS_HOUR_WEATHER = longPreferencesKey("pass_hour_weather")
    private val KEY_PASS_HOUR_CRITS = longPreferencesKey("pass_hour_crits")

    private val KEY_GEMS = longPreferencesKey("gems_balance")
    private val KEY_TOTAL_GEMS_EARNED = longPreferencesKey("gems_total_earned")
    private val KEY_GEM_CHARM_LEVEL = longPreferencesKey("gems_charm_level")
    private val KEY_PRISM_LENS_UNLOCKED = booleanPreferencesKey("gems_prism_lens")
    private val KEY_GEM_FRENZY_MS = longPreferencesKey("gems_frenzy_ms")
    private val KEY_VAULT_UNLOCKED = booleanPreferencesKey("vault_hacker_unlocked")
    private val KEY_DOG_CRASH_OMEN_SEEN = booleanPreferencesKey("dog_crash_omen_seen")
    private val KEY_TRILLION_CUTSCENE_SEEN = booleanPreferencesKey("trillion_cutscene_seen")
    private val KEY_CITADEL_BOSS_GLITCH_SEEN = booleanPreferencesKey("citadel_boss_glitch_seen")
    private val KEY_CAT_COMPUTER_UNLOCKED = booleanPreferencesKey("cat_computer_unlocked")
    private val KEY_DOG_COMPUTER_SEARCHED = booleanPreferencesKey("dog_computer_searched")
    private val KEY_VAULT_KICKOUT_UNTIL_MS = longPreferencesKey("vault_kickout_until_ms")

    private val KEY_ACCOUNT_MODE = stringPreferencesKey("account_mode")
    private val KEY_LOCAL_ACCOUNT_CODE = stringPreferencesKey("local_account_code")
    private val KEY_LOCAL_ACCOUNT_PIN = stringPreferencesKey("local_account_pin")
    private val KEY_STATS_PUBLIC = booleanPreferencesKey("profile_stats_public")
    private val KEY_BIO = stringPreferencesKey("profile_bio")
    private val KEY_AVATAR_BASE64 = stringPreferencesKey("profile_avatar_base64")
    private val KEY_BANNER_BASE64 = stringPreferencesKey("profile_banner_base64")
    private val KEY_MUSIC_REPEAT_MODE = stringPreferencesKey("music_repeat_mode")
    private val KEY_MUSIC_SHUFFLE = booleanPreferencesKey("music_shuffle_enabled")
    private val KEY_MUSIC_SPEED = floatPreferencesKey("music_playback_speed")
    private val KEY_NAVIGATION_FORMAT = stringPreferencesKey("settings_navigation_format")
    private val KEY_PASS_COMPLETIONS = longPreferencesKey("pass_november_completions")
    private val KEY_LOCAL_EXIST_COUNTS = stringPreferencesKey("local_limited_exist_counts")
    private val KEY_DISMISSED_ANNOUNCEMENT_ID = stringPreferencesKey("dismissed_announcement_id")
    private val KEY_WIDGET_WEATHER_LABEL = stringPreferencesKey("widget_weather_label")
    private val KEY_WIDGET_WEATHER_TEMP = longPreferencesKey("widget_weather_temp")

    private val KEY_TURKEYS = longPreferencesKey("thanksgiving_turkeys")
    private val KEY_TOTAL_TURKEYS = longPreferencesKey("thanksgiving_total_turkeys")
    private val KEY_FESTIVAL_HOUR = longPreferencesKey("thanksgiving_festival_hour")
    private val KEY_FESTIVAL_MEGA = booleanPreferencesKey("thanksgiving_festival_mega")
    private val KEY_GATO_TALKS = longPreferencesKey("thanksgiving_gato_talks")
    private val KEY_PART2_MAP = booleanPreferencesKey("thanksgiving_part2_map")
    private val KEY_FEST_Q_HOUR = longPreferencesKey("thanksgiving_q_hour")
    private val KEY_FEST_Q_CLAIMED = stringPreferencesKey("thanksgiving_q_claimed")
    private val KEY_FEST_H_PETS = longPreferencesKey("thanksgiving_h_pets")
    private val KEY_FEST_H_TURKEYS = longPreferencesKey("thanksgiving_h_turkeys")
    private val KEY_FEST_H_UPGRADES = longPreferencesKey("thanksgiving_h_upgrades")
    private val KEY_FEST_H_WEATHER = longPreferencesKey("thanksgiving_h_weather")
  }
}
