package com.example.ui.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CatSoundEffect
import com.example.audio.CustomMusicTrack
import com.example.audio.MusicPlaybackStatus
import com.example.audio.MusicTrack
import com.example.audio.RepeatMode
import com.example.audio.SoundManager
import com.example.data.local.AppSettingsState
import com.example.data.local.LocalGameStorage
import com.example.data.model.ActiveBattlePassQuest
import com.example.data.model.ActiveFestivalQuest
import com.example.data.model.BattlePassRewardItem
import com.example.data.model.BattlePassState
import com.example.data.model.ChallengeProgressState
import com.example.data.model.DailyBonusPrizeType
import com.example.data.model.EndlessDailyPayout
import com.example.data.model.FestivalTier
import com.example.data.model.BlogPostDef
import com.example.data.model.GameCatalog
import com.example.data.model.GlobalAnnouncement
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardPublicStats
import com.example.data.model.LimitedEventDef
import com.example.data.model.ModerationRecord
import com.example.data.model.MoonlightEventState
import com.example.data.model.MoonlightPhase
import com.example.data.model.NavigationLayoutFormat
import com.example.data.model.OfflineEarningsReport
import com.example.data.model.PassRewardKind
import com.example.data.model.PlayerStatsState
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyRewardReceipt
import com.example.data.repository.PurrGameRepository
import com.example.data.weather.LiveWeatherState
import com.example.data.weather.WeatherConditionKind
import com.example.data.weather.WeatherRepository
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.ThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.File
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class FloatingPurrParticle(
  val id: Long,
  val text: String,
  val xFraction: Float,
  val yFraction: Float,
  val isComboCrit: Boolean,
  val isGemDrop: Boolean = false,
)

data class SpawnedTurkey(
  val id: Long,
  val xFraction: Float,
  val yFraction: Float,
  val spawnedAtMillis: Long,
  val isGoldenGobbler: Boolean = false,
)

data class PurrGameUiState(
  val isLoading: Boolean = true,
  val isSignedIn: Boolean = false,
  val errorMessage: String? = null,
  val profile: UserProfile = UserProfile(),
  val weather: LiveWeatherState = LiveWeatherState(),
  val settings: AppSettingsState = AppSettingsState(),
  val challengeState: ChallengeProgressState = ChallengeProgressState(),
  val playerStats: PlayerStatsState = PlayerStatsState(),
  val activeEvent: LimitedEventDef = GameCatalog.periodicEvents.first(),
  val festivalTier: FestivalTier = FestivalTier.REGULAR,
  val festivalRerollCountdownText: String = "59m 59s",
  val isTurkeyRushActive: Boolean = GameCatalog.isTurkeySpawnWindowActive(),
  val turkeyRushCountdownText: String = GameCatalog.turkeyRushStatusText(),
  val hourlyFestivalQuests: List<ActiveFestivalQuest> = emptyList(),
  val spawnedTurkeys: List<SpawnedTurkey> = emptyList(),
  val eventCountdownText: String = "4d 23h",
  val weeklyResetCountdownText: String = "6d 23h",
  val moonlightState: MoonlightEventState = MoonlightEventState(),
  val comboCharge: Float = 0f,
  val comboMultiplier: Double = 1.0,
  val effectiveClickPower: Long = 1L,
  val effectivePps: Long = 0L,
  val canClaimDailyReward: Boolean = false,
  val nextDailyDayIndex: Int = 1,
  val nextDailyPayout: EndlessDailyPayout = GameCatalog.computeEndlessDailyPayout(1L),
  val canClaimWeeklyReward: Boolean = false,
  val currentPlayerGlobalRank: Int? = null,
  val pendingOfflineReport: OfflineEarningsReport? = null,
  val pendingWeeklyReward: WeeklyRewardReceipt? = null,
  val nextRebirthCost: Long = GameCatalog.REBIRTH_BASE_COST,
  val rebirthMultiplier: Double = 1.0,
  val hardcoreEligible: Boolean = false,
  val battlePassState: BattlePassState = BattlePassState(),
  val currentPassTier: Int = 0,
  val currentVipPassTier: Int = 0,
  val freePassStepProgress: Pair<Long, Long> = 0L to GameCatalog.NOVEMBER_PASS_FREE_BASE_XP,
  val vipPassStepProgress: Pair<Long, Long> = 0L to GameCatalog.NOVEMBER_PASS_VIP_BASE_XP,
  val hourlyPassQuests: List<ActiveBattlePassQuest> = emptyList(),
  val hourlyQuestRefreshText: String = "59m 59s",
  val claimablePassQuestsCount: Int = 0,
  val claimablePassCount: Int = 0,
  val goldenRainActive: Boolean = false,
  val goldenRainCountdownText: String = "",
  val goldenTouchProgressText: String = "0m / 30m",
  val gemFrenzyActive: Boolean = false,
  val gemFrenzyCountdownText: String = "",
  val gemCharmMultiplier: Double = 1.0,
  val nextGemCharmCost: Long = GameCatalog.gemCharmCost(0L),
  val vipRequirementUpgradeCount: Long = 0L,
  val vaultKeeperTapCount: Int = 0,
  val vaultCooldownRemainingMillis: Long = 0L,
  val vaultCooldownText: String = "",
  val showDogCrashOmen: Boolean = false,
  val showTrillionCutscene: Boolean = false,
  val activeFeastZoneId: String? = null,
  val showBossGlitchCutscene: Boolean = false,
  val showWindowGatoDesktop: Boolean = false,
  val showWindowGatoExitConfrontation: Boolean = false,
  val isAdmin: Boolean = false,
  val activeModeration: ModerationRecord? = null,
  val adminModerationList: List<ModerationRecord> = emptyList(),
  val globalAnnouncements: List<GlobalAnnouncement> = emptyList(),
  val dismissedAnnouncementId: String = "",
  val limitedExistCounts: Map<String, Long> = emptyMap(),
  val pendingUpdateBlogPost: BlogPostDef? = null,
  val particles: List<FloatingPurrParticle> = emptyList(),
) {
  val activeCurrencyBalance: Long
    get() = if (challengeState.isHardcoreMode) challengeState.hardcorePurrs else profile.purrs

  val activeCurrencyLabel: String
    get() = if (challengeState.isHardcoreMode) "Hardcore Purrs" else "Purrs"

  val activeCurrencyShortLabel: String
    get() = if (challengeState.isHardcoreMode) "HC Purrs" else "Purrs"

  val activeUpgradeLevels: Map<String, Long>
    get() =
      if (challengeState.isHardcoreMode) challengeState.hardcoreUpgradeLevels
      else profile.upgradeLevels

  val canAscend: Boolean
    get() = challengeState.rebirthCount >= GameCatalog.ASCENSION_REQUIRED_REBIRTHS

  val isBanned: Boolean
    get() = activeModeration?.status == "BANNED"

  val isTimedOut: Boolean
    get() =
      activeModeration?.status == "TIMED_OUT" &&
        activeModeration.timeoutUntilMillis > System.currentTimeMillis()

  val activeAnnouncementBanner: GlobalAnnouncement?
    get() =
      globalAnnouncements.firstOrNull {
        it.messageId.isNotBlank() && it.messageId != dismissedAnnouncementId
      }

  fun existCountFor(itemType: String, itemId: String): Long {
    val statId = GameCatalog.existStatId(itemType, itemId)
    return limitedExistCounts[statId] ?: 0L
  }
}

class PurrGameViewModel(
  private val repository: PurrGameRepository?,
  private val localStorage: LocalGameStorage,
  private val weatherRepository: WeatherRepository,
  private val soundManager: SoundManager,
  private val currentUserId: String?,
  private val initialDisplayName: String,
  private val currentUserEmail: String? = null,
  private val onSyncWidgets: () -> Unit = {},
) : ViewModel() {

  private val _uiState =
    MutableStateFlow(
      PurrGameUiState(
        isSignedIn = currentUserId != null,
        isAdmin = GameCatalog.isAdminEmail(currentUserEmail),
        weather = weatherRepository.rollRandomWeather(previous = null, shiftCount = 1L),
      )
    )
  val uiState: StateFlow<PurrGameUiState> = _uiState.asStateFlow()

  val musicPlaybackStatus: StateFlow<MusicPlaybackStatus> = soundManager.playbackStatus

  private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

  val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> =
    if (repository != null && currentUserId != null) {
      repository
        .observeLeaderboard(50L)
        .map<List<LeaderboardEntry>, UiState<List<LeaderboardEntry>>> { UiState.Success(it) }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val rootError = firestoreError ?: error
          Log.w(TAG, "Error observing leaderboard", rootError)
          emit(UiState.Error(rootError.message ?: "Failed to load global leaderboard"))
        }
        .stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5000L),
          initialValue = UiState.Loading,
        )
    } else {
      flowOf<UiState<List<LeaderboardEntry>>>(UiState.Success(emptyList()))
        .stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5000L),
          initialValue = UiState.Success(emptyList()),
        )
    }

  private var isDirty = false
  private var isCloudSaveInFlight = false
  private var challengeDirty = false
  private var statsDirty = false
  private var passDirty = false
  private var particleCounter = 0L
  private var hasInitializedProfile = false
  private var isAppInForeground = true
  private var backgroundPauseTimestampMillis = 0L
  private var rolledNightEpochDay: Long = -1L
  private var isBloodmoonTonight: Boolean = false
  private var secondsUntilNextWeather: Int = WeatherRepository.WEATHER_INTERVAL_SECONDS
  private var turkeySpawnSeconds: Int = 0
  private var turkeyIdCounter: Long = 0L

  private var remoteExistCounts: Map<String, Long> = emptyMap()

  init {
    soundManager.setPlaybackCallbacks(
      onTrackEnded = { handleTrackNaturallyEnded() },
      onSleepTimerExpired = {
        updateSettings { it.copy(isMusicPlaying = false) }
        _toastEvents.tryEmit("Sleep timer ended — Sanctuary music paused.")
      },
    )
    bootstrapPlayer()
    startGameTicker()
    observeGlobalRank()
    observeGlobalAnnouncements()
    observeLimitedItemStats()
    observeOwnModeration()
    observeAdminModerationIfAuthorized()
  }

  private fun observeGlobalRank() {
    val uid = currentUserId ?: return
    viewModelScope.launch {
      leaderboardState.collect { lbState ->
        if (lbState is UiState.Success) {
          val index = lbState.data.indexOfFirst { it.userId == uid }
          val rank = if (index >= 0) index + 1 else null
          _uiState.update { it.copy(currentPlayerGlobalRank = rank) }
        }
      }
    }
  }

  private fun observeGlobalAnnouncements() {
    val repo = repository ?: return
    if (currentUserId == null) return
    viewModelScope.launch {
      repo.observeGlobalAnnouncements(10L)
        .catch { e ->
          if (e is CancellationException) throw e
          Log.w(TAG, "Error observing global announcements: ${e.message}")
        }
        .collect { list ->
          _uiState.update { it.copy(globalAnnouncements = list) }
        }
    }
  }

  private fun observeLimitedItemStats() {
    val repo = repository ?: return
    if (currentUserId == null) return
    viewModelScope.launch {
      repo.observeLimitedItemCounters(120L)
        .catch { e ->
          if (e is CancellationException) throw e
          Log.w(TAG, "Error observing limited item counters: ${e.message}")
        }
        .collect { counts ->
          remoteExistCounts = counts
          _uiState.update { state ->
            state.copy(limitedExistCounts = mergeExistMaps(state.limitedExistCounts, counts))
          }
        }
    }
  }

  private fun observeOwnModeration() {
    val repo = repository ?: return
    val uid = currentUserId ?: return
    viewModelScope.launch {
      repo.observeOwnModeration(uid)
        .catch { e ->
          if (e is CancellationException) throw e
          Log.w(TAG, "Error observing own moderation: ${e.message}")
        }
        .collect { mod ->
          _uiState.update { it.copy(activeModeration = mod) }
        }
    }
  }

  private fun observeAdminModerationIfAuthorized() {
    val repo = repository ?: return
    if (currentUserId == null || !GameCatalog.isAdminEmail(currentUserEmail)) return
    viewModelScope.launch {
      repo.observeAllModerationForAdmin(100L)
        .catch { e ->
          if (e is CancellationException) throw e
          Log.w(TAG, "Error observing admin moderation list: ${e.message}")
        }
        .collect { records ->
          _uiState.update { it.copy(adminModerationList = records) }
        }
    }
  }

  private fun bootstrapPlayer() {
    viewModelScope.launch {
      try {
        val now = System.currentTimeMillis()
        val loadedSettings = localStorage.loadSettings()
        applySettingsToAudio(loadedSettings)
        val rawChallenge = localStorage.loadChallengeProgress()
        val (loadedChallenge, festChanged, _) = ensureHourlyFestivalAndQuests(rawChallenge, now)
        if (festChanged) {
          localStorage.saveChallengeProgress(loadedChallenge)
        }
        val loadedStats = localStorage.loadPlayerStats(now)
        val loadedPass = localStorage.loadBattlePassState()
        val (syncedPass, passChanged) = ensureHourlyPassQuests(loadedPass, now)
        if (passChanged) {
          localStorage.saveBattlePassState(syncedPass)
        }
        val currentWeek = GameCatalog.currentEpochWeek(now)
        val passTier = GameCatalog.freeTierFromXp(syncedPass.passXp)
        val vipTier = GameCatalog.vipTierFromXp(syncedPass.passXp)
        val evaluatedQuests = GameCatalog.evaluateHourlyPassQuests(syncedPass)
        val initialFestTier = GameCatalog.festivalTierFor(loadedChallenge.festivalIsMega)
        val evaluatedFestQuests = GameCatalog.evaluateHourlyFestivalQuests(loadedChallenge)
        val localExist = localStorage.loadLimitedExistCounts().toMutableMap()
        val dismissedId = localStorage.loadDismissedAnnouncementId()
        val lastSeenBlogVersion = localStorage.loadLastSeenBlogVersion()
        val latestBlogPost = GameCatalog.blogPosts.firstOrNull()
        val pendingBlog =
          if (latestBlogPost != null && lastSeenBlogVersion != latestBlogPost.version) {
            latestBlogPost
          } else {
            null
          }
        val localProfile = localStorage.loadProfile()
        ensureOwnedLimitedFloor(
          counts = localExist,
          patterns = localProfile.unlockedPatterns,
          accessories = localProfile.unlockedAccessories,
          themes = loadedSettings.unlockedColorThemes,
          backgrounds = localProfile.unlockedBackgrounds,
        )
        localStorage.saveLimitedExistCounts(localExist)

        _uiState.update {
          it.copy(
            settings = loadedSettings,
            pendingUpdateBlogPost = pendingBlog,
            challengeState = loadedChallenge,
            festivalTier = initialFestTier,
            festivalRerollCountdownText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
            isTurkeyRushActive = GameCatalog.isTurkeySpawnWindowActive(now),
            turkeyRushCountdownText = GameCatalog.turkeyRushStatusText(now),
            hourlyFestivalQuests = evaluatedFestQuests,
            playerStats = loadedStats,
            battlePassState = syncedPass,
            currentPassTier = passTier,
            currentVipPassTier = vipTier,
            freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(syncedPass.passXp),
            vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(syncedPass.passXp),
            hourlyPassQuests = evaluatedQuests,
            hourlyQuestRefreshText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
            claimablePassQuestsCount =
              evaluatedQuests.count { q -> !q.isClaimed && q.isCompleted },
            claimablePassCount = countClaimablePassRewards(syncedPass, passTier, vipTier),
            goldenRainActive = syncedPass.goldenRainRemainingMillis > 0L,
            goldenRainCountdownText = formatDuration(syncedPass.goldenRainRemainingMillis),
            goldenTouchProgressText = formatGoldenCharge(syncedPass.goldenTouchEquippedMillis),
            gemFrenzyActive = loadedChallenge.gemFrenzyRemainingMillis > 0L,
            gemFrenzyCountdownText = formatDuration(loadedChallenge.gemFrenzyRemainingMillis),
            gemCharmMultiplier = GameCatalog.gemCharmMultiplier(loadedChallenge.gemCharmLevel),
            nextGemCharmCost = GameCatalog.gemCharmCost(loadedChallenge.gemCharmLevel),
            weeklyResetCountdownText = formatDuration(GameCatalog.millisUntilNextWeeklyReset(now)),
            canClaimWeeklyReward = loadedStats.lastWeeklyRewardWeekId < currentWeek,
            nextRebirthCost =
              GameCatalog.rebirthCost(loadedChallenge.rebirthCount, loadedChallenge.isHardcoreMode),
            rebirthMultiplier =
              GameCatalog.rebirthMultiplier(
                loadedChallenge.rebirthBonusSteps,
                loadedChallenge.unlockedAscensionIds,
              ),
            hardcoreEligible = loadedChallenge.hardcoreUnlocked,
            vaultCooldownRemainingMillis =
              (loadedChallenge.vaultKickoutUntilMillis - now).coerceAtLeast(0L),
            vaultCooldownText =
              formatDuration((loadedChallenge.vaultKickoutUntilMillis - now).coerceAtLeast(0L)),
            dismissedAnnouncementId = dismissedId,
            limitedExistCounts = mergeExistMaps(localExist, remoteExistCounts),
          )
        }

        val roll = localStorage.loadNightRoll()
        rolledNightEpochDay = roll.epochDay
        isBloodmoonTonight = roll.isBloodmoon
        ensureNightRollForCurrentTime(now)

        if (repository != null && currentUserId != null) {
          val remote = repository.ensureUserProfile(currentUserId, initialDisplayName)
          val merged = mergeLocalAndRemote(localProfile, remote, currentUserId, initialDisplayName)
          localStorage.saveProfile(merged)
          val mergedExist = localExist.toMutableMap()
          ensureOwnedLimitedFloor(
            counts = mergedExist,
            patterns = merged.unlockedPatterns,
            accessories = merged.unlockedAccessories,
            themes = loadedSettings.unlockedColorThemes,
            backgrounds = merged.unlockedBackgrounds,
          )
          localStorage.saveLimitedExistCounts(mergedExist)
          _uiState.update { s ->
            s.copy(limitedExistCounts = mergeExistMaps(mergedExist, remoteExistCounts))
          }
          if (merged != remote) {
            val pubStats =
              LeaderboardPublicStats(
                rebirths = loadedChallenge.rebirthCount,
                ascensions = loadedChallenge.ascensionCount,
                dailyStreak = merged.dailyStreak,
                highestComboX100 =
                  (loadedStats.highestComboMultiplier * 100.0).roundToLong().coerceAtLeast(100L),
              )
            repository.saveProfileAndLeaderboard(merged, pubStats)
          }
          applyProfile(merged, isLoaded = true)
          hasInitializedProfile = true
          processOfflineReturn(loadedStats.lastActiveTimestampMillis, System.currentTimeMillis())
          checkWeeklyAutoRollover(System.currentTimeMillis())

          repository
            .observeUserProfile(currentUserId)
            .catch { error ->
              val firestoreError =
                generateSequence(error) { it.cause }
                  .filterIsInstance<FirebaseFirestoreException>()
                  .firstOrNull()
              if (firestoreError == null && error is CancellationException) throw error
              Log.w(TAG, "Profile listener error", firestoreError ?: error)
            }
            .collect { remoteUpdate ->
              if (remoteUpdate != null && !isDirty && !isCloudSaveInFlight) {
                val current = _uiState.value.profile
                if (
                  remoteUpdate.totalPurrsEarned >= current.totalPurrsEarned &&
                    remoteUpdate.totalPets >= current.totalPets
                ) {
                  localStorage.saveProfile(remoteUpdate)
                  applyProfile(remoteUpdate, isLoaded = true)
                }
              }
            }
        } else {
          applyProfile(localProfile, isLoaded = true)
          hasInitializedProfile = true
          processOfflineReturn(loadedStats.lastActiveTimestampMillis, System.currentTimeMillis())
          checkWeeklyAutoRollover(System.currentTimeMillis())
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize player profile", e)
        _uiState.update {
          it.copy(
            isLoading = false,
            errorMessage = e.localizedMessage ?: "Failed to load sanctuary profile.",
          )
        }
      }
    }
  }

  private fun startGameTicker() {
    viewModelScope.launch {
      var ticksSinceSave = 0
      while (true) {
        delay(1000L)
        try {
        val now = System.currentTimeMillis()
        val (event, remainingMillis) = GameCatalog.currentPeriodicEvent(now)
        val countdown = formatDuration(remainingMillis)

        ensureNightRollForCurrentTime(now)

        secondsUntilNextWeather--
        val shouldRollWeather = secondsUntilNextWeather <= 0
        if (shouldRollWeather) {
          secondsUntilNextWeather = WeatherRepository.WEATHER_INTERVAL_SECONDS
        }

        val turkeyWindowOpen = GameCatalog.isTurkeySpawnWindowActive(now)
        turkeySpawnSeconds++
        var newSpawnedTurkey: SpawnedTurkey? = null
        if (turkeySpawnSeconds >= GameCatalog.FESTIVAL_SPAWN_INTERVAL_SECONDS) {
          turkeySpawnSeconds = 0
          val snap = _uiState.value
          val curTier = GameCatalog.festivalTierFor(snap.challengeState.festivalIsMega)
          if (
            turkeyWindowOpen &&
              !snap.isLoading &&
              isAppInForeground &&
              GameCatalog.shouldSpawnTurkey(curTier)
          ) {
            newSpawnedTurkey =
              SpawnedTurkey(
                id = ++turkeyIdCounter,
                xFraction = 0.14f + Random.nextFloat() * 0.70f,
                yFraction = 0.20f + Random.nextFloat() * 0.58f,
                spawnedAtMillis = now,
                isGoldenGobbler = GameCatalog.shouldSpawnGoldenGobbler(curTier),
              )
          }
        }

        _uiState.update { state ->
          val nextWeather =
            if (shouldRollWeather) {
              weatherRepository.rollRandomWeather(
                previous = state.weather.condition,
                shiftCount = state.weather.weatherShiftCount + 1L,
              )
            } else {
              state.weather.copy(nextShiftSeconds = secondsUntilNextWeather)
            }

          val localMinute = WeatherRepository.currentLocalMinuteOfDay(now)
          val moonlight =
            GameCatalog.evaluateMoonlightWindow(
              minuteOfDay = localMinute,
              morningEndMinute = 360, // 6:00 AM
              isBloodmoonNight = isBloodmoonTonight,
            )

          val baseProfile =
            if (!state.isLoading && shouldRollWeather) {
              isDirty = true
              state.profile.copy(weatherCheckedCount = state.profile.weatherCheckedCount + 1L)
            } else {
              state.profile
            }

          val (festChallenge, festChanged, festHourRerolled) =
            ensureHourlyFestivalAndQuests(state.challengeState, now)
          if (festChanged) {
            challengeDirty = true
          }
          val festTier = GameCatalog.festivalTierFor(festChallenge.festivalIsMega)
          if (festHourRerolled && !state.isLoading) {
            turkeySpawnSeconds = GameCatalog.FESTIVAL_SPAWN_INTERVAL_SECONDS - 2
            _toastEvents.tryEmit(
              if (festTier == FestivalTier.MEGA) {
                "🔥 MEGA GRAVY RUSH! 3.0x Purrs, 45% Turkeys & +3 Golden Gobblers spawning for 5m!"
              } else {
                "🦃 Thanksgiving Part 2 Reroll: Turkeys & +3 Gobblers spawning for 5m (${festTier.turkeySpawnChancePercent}%/10s)!"
              }
            )
          }

          val currentChallenge =
            let {
              var next = festChallenge
              if (!state.isLoading && shouldRollWeather) {
                challengeDirty = true
                next = next.copy(festHourWeatherShifts = next.festHourWeatherShifts + 1L)
              }
              if (!state.isLoading && isAppInForeground && next.gemFrenzyRemainingMillis > 0L) {
                challengeDirty = true
                val left = (next.gemFrenzyRemainingMillis - 1000L).coerceAtLeast(0L)
                next = next.copy(gemFrenzyRemainingMillis = left)
                if (left == 0L) {
                  _toastEvents.tryEmit("Starlight Gem Frenzy ended!")
                }
              }
              next
            }
          val meetsHardcoreNow =
            currentChallenge.hardcoreUnlocked ||
              GameCatalog.meetsHardcoreRequirements(baseProfile, currentChallenge.rebirthCount)

          // Advance Hourly Battle Pass Quests, weather shift XP & Golden Touch / Golden Rain timers
          val (rolledPass, hourChanged) = ensureHourlyPassQuests(state.battlePassState, now)
          var nextPass = rolledPass
          if (hourChanged) {
            passDirty = true
          }
          if (!state.isLoading && shouldRollWeather) {
            passDirty = true
            val weatherXp =
              if ("asc_pass_scholar" in currentChallenge.unlockedAscensionIds) 23L else 15L
            nextPass =
              nextPass.copy(
                passXp = nextPass.passXp + weatherXp,
                hourWeatherShifts = nextPass.hourWeatherShifts + 1L,
              )
          }
          if (!state.isLoading && isAppInForeground) {
            if (nextPass.goldenRainRemainingMillis > 0L) {
              passDirty = true
              val left = (nextPass.goldenRainRemainingMillis - 1000L).coerceAtLeast(0L)
              nextPass = nextPass.copy(goldenRainRemainingMillis = left)
              if (left == 0L) {
                _toastEvents.tryEmit("Golden Rain ended! Golden Touch recharging...")
              }
            } else if (baseProfile.selectedPatternId == GameCatalog.GOLDEN_TOUCH_SKIN_ID) {
              passDirty = true
              val charged = nextPass.goldenTouchEquippedMillis + 1000L
              if (charged >= GameCatalog.GOLDEN_TOUCH_INTERVAL_MS) {
                nextPass =
                  nextPass.copy(
                    goldenTouchEquippedMillis = 0L,
                    goldenRainRemainingMillis = GameCatalog.GOLDEN_RAIN_DURATION_MS,
                  )
                _toastEvents.tryEmit("GOLDEN RAIN! Golden Touch triggered 4.0x Purrs for 3m!")
              } else {
                nextPass = nextPass.copy(goldenTouchEquippedMillis = charged)
              }
            }
          }
          val isGoldenRainNow = nextPass.goldenRainRemainingMillis > 0L

          val pps =
            computeEffectivePps(
              baseProfile,
              nextWeather,
              event,
              moonlight.phase,
              currentChallenge,
              isGoldenRainNow,
              festTier,
            )
          val updatedProfile =
            if (!state.isLoading && pps > 0L) {
              isDirty = true
              baseProfile.copy(
                purrs =
                  if (currentChallenge.isHardcoreMode) baseProfile.purrs else baseProfile.purrs + pps,
                totalPurrsEarned = baseProfile.totalPurrsEarned + pps,
                eventPurrsEarned = baseProfile.eventPurrsEarned + pps,
              )
            } else {
              baseProfile
            }
          if (!state.isLoading && pps > 0L) {
            passDirty = true
            nextPass = nextPass.copy(hourPurrsEarned = nextPass.hourPurrsEarned + pps)
          }

          val updatedChallenge =
            let {
              var next =
                if (meetsHardcoreNow && !currentChallenge.hardcoreUnlocked) {
                  challengeDirty = true
                  currentChallenge.copy(hardcoreUnlocked = true)
                } else {
                  currentChallenge
                }
              if (!state.isLoading && pps > 0L && next.isHardcoreMode) {
                challengeDirty = true
                next = next.copy(hardcorePurrs = next.hardcorePurrs + pps)
              }
              next
            }

          val baseDecay = if (updatedChallenge.isHardcoreMode) 0.36f else 0.22f
          val decayRate =
            if ("asc_frenzy_aura" in updatedChallenge.unlockedAscensionIds) baseDecay * 0.5f
            else baseDecay
          val decayedCombo = (state.comboCharge - decayRate).coerceAtLeast(0f)
          val maxComboScale =
            if ("asc_frenzy_aura" in updatedChallenge.unlockedAscensionIds) 1.10 else 0.60
          val comboMult = 1.0 + (decayedCombo * maxComboScale)
          val clickPow =
            computeEffectiveClick(
              updatedProfile,
              nextWeather,
              event,
              moonlight.phase,
              comboMult,
              updatedChallenge,
              isGoldenRainNow,
              festTier,
            )
          val epochDay = currentUtcEpochDay(now)
          val currentWeek = GameCatalog.currentEpochWeek(now)
          val weeklyCountdown = formatDuration(GameCatalog.millisUntilNextWeeklyReset(now))

          val nextStats =
            if (!state.isLoading && isAppInForeground) {
              statsDirty = true
              state.playerStats.copy(
                lastActiveTimestampMillis = now,
                totalTimePlayedSeconds = state.playerStats.totalTimePlayedSeconds + 1L,
                longestDailyStreak =
                  maxOf(state.playerStats.longestDailyStreak, updatedProfile.dailyStreak),
              )
            } else {
              state.playerStats
            }

          val activeTurkeys =
            state.spawnedTurkeys.filter { (now - it.spawnedAtMillis) <= 18_000L }
          val nextTurkeys =
            if (newSpawnedTurkey != null && activeTurkeys.size < 4 && activeTurkeys.none { it.id == newSpawnedTurkey.id }) {
              activeTurkeys + newSpawnedTurkey
            } else {
              activeTurkeys
            }

          val nextStreakPreview = computeNextStreakNumber(updatedProfile, epochDay)
          val nextFreeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
          val nextVipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
          val nextQuests = GameCatalog.evaluateHourlyPassQuests(nextPass)
          val nextFestQuests = GameCatalog.evaluateHourlyFestivalQuests(updatedChallenge)

          state.copy(
            profile = updatedProfile,
            weather = nextWeather,
            challengeState = updatedChallenge,
            festivalTier = festTier,
            festivalRerollCountdownText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
            isTurkeyRushActive = turkeyWindowOpen,
            turkeyRushCountdownText = GameCatalog.turkeyRushStatusText(now),
            hourlyFestivalQuests = nextFestQuests,
            spawnedTurkeys = nextTurkeys,
            playerStats = nextStats,
            battlePassState = nextPass,
            currentPassTier = nextFreeTier,
            currentVipPassTier = nextVipTier,
            freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(nextPass.passXp),
            vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(nextPass.passXp),
            hourlyPassQuests = nextQuests,
            hourlyQuestRefreshText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
            claimablePassQuestsCount = nextQuests.count { q -> !q.isClaimed && q.isCompleted },
            claimablePassCount = countClaimablePassRewards(nextPass, nextFreeTier, nextVipTier),
            goldenRainActive = isGoldenRainNow,
            goldenRainCountdownText = formatDuration(nextPass.goldenRainRemainingMillis),
            goldenTouchProgressText = formatGoldenCharge(nextPass.goldenTouchEquippedMillis),
            gemFrenzyActive = updatedChallenge.gemFrenzyRemainingMillis > 0L,
            gemFrenzyCountdownText = formatDuration(updatedChallenge.gemFrenzyRemainingMillis),
            gemCharmMultiplier = GameCatalog.gemCharmMultiplier(updatedChallenge.gemCharmLevel),
            nextGemCharmCost = GameCatalog.gemCharmCost(updatedChallenge.gemCharmLevel),
            vipRequirementUpgradeCount =
              GameCatalog.totalUpgradesForPassVip(updatedProfile, updatedChallenge, nextStats),
            activeEvent = event,
            eventCountdownText = countdown,
            weeklyResetCountdownText = weeklyCountdown,
            moonlightState = moonlight,
            comboCharge = decayedCombo,
            comboMultiplier = comboMult,
            effectiveClickPower = clickPow,
            effectivePps = pps,
            canClaimDailyReward = updatedProfile.lastDailyClaimEpochDay < epochDay,
            nextDailyDayIndex = computeNextDailyDay(updatedProfile, epochDay),
            nextDailyPayout = GameCatalog.computeEndlessDailyPayout(nextStreakPreview),
            canClaimWeeklyReward = nextStats.lastWeeklyRewardWeekId < currentWeek,
            nextRebirthCost =
              GameCatalog.rebirthCost(updatedChallenge.rebirthCount, updatedChallenge.isHardcoreMode),
            rebirthMultiplier =
              GameCatalog.rebirthMultiplier(
                updatedChallenge.rebirthBonusSteps,
                updatedChallenge.unlockedAscensionIds,
              ),
            hardcoreEligible = meetsHardcoreNow,
            vaultCooldownRemainingMillis =
              (updatedChallenge.vaultKickoutUntilMillis - now).coerceAtLeast(0L),
            vaultCooldownText =
              formatDuration((updatedChallenge.vaultKickoutUntilMillis - now).coerceAtLeast(0L)),
            particles = state.particles.takeLast(6),
          )
        }

        if (hasInitializedProfile) {
          checkWeeklyAutoRollover(now)
        }
        } catch (e: CancellationException) {
          throw e
        } catch (_: Exception) {
          // Keep 1-second tick loop alive so countdowns & Purr Frenzy decay never freeze
        }

        ticksSinceSave++
        if (
          ticksSinceSave >= 4 &&
            hasInitializedProfile &&
            (isDirty || challengeDirty || statsDirty || passDirty)
        ) {
          ticksSinceSave = 0
          if (isDirty) persistCurrentProfile()
          if (challengeDirty) {
            challengeDirty = false
            localStorage.saveChallengeProgress(_uiState.value.challengeState)
          }
          if (statsDirty) {
            statsDirty = false
            localStorage.savePlayerStats(_uiState.value.playerStats)
          }
          if (passDirty) {
            passDirty = false
            localStorage.saveBattlePassState(_uiState.value.battlePassState)
          }
          val w = _uiState.value.weather
          localStorage.saveWidgetWeatherSnapshot(w.condition.label, w.temperatureC.toInt())
          onSyncWidgets()
        }
      }
    }
  }

  private suspend fun ensureNightRollForCurrentTime(nowMillis: Long) {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val localEpochDay = cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR)
    if (rolledNightEpochDay != localEpochDay) {
      rolledNightEpochDay = localEpochDay
      // 33% chance each night for a Bloodmoon!
      isBloodmoonTonight = Random.nextInt(100) < 33
      localStorage.saveNightRoll(localEpochDay, isBloodmoonTonight)
    }
  }

  fun onCatPetted(xFraction: Float, yFraction: Float) {
    val state = _uiState.value
    if (state.isLoading) return
    if (enforceModerationBlock(state)) return

    val challenge = state.challengeState
    val newCombo = 0f
    val critChancePercent =
      if ("asc_frenzy_aura" in challenge.unlockedAscensionIds) 20 else 12
    val isCriticalPet = Random.nextInt(100) < critChancePercent
    val comboMult = if (isCriticalPet) 2.5 else 1.0

    val luckyJackpot =
      "asc_lucky_catnip" in challenge.unlockedAscensionIds && Random.nextInt(100) < 12
    val isCrit = luckyJackpot || isCriticalPet
    val rawClickGain =
      computeEffectiveClick(
        state.profile,
        state.weather,
        state.activeEvent,
        state.moonlightState.phase,
        comboMult,
        challenge,
        state.goldenRainActive,
        state.festivalTier,
      )
    val clickGain = if (luckyJackpot) (rawClickGain * 3.5).toLong() else rawClickGain

    soundManager.playCatTapSound(isComboCrit = isCrit)

    val boostedSky =
      state.goldenRainActive ||
        state.moonlightState.phase == MoonlightPhase.BLOODMOON ||
        state.weather.condition == WeatherConditionKind.AURORA_BOREALIS ||
        state.weather.condition == WeatherConditionKind.METEOR_SHOWER
    val gemThreshold =
      GameCatalog.gemDropRatePer1000(boostedSky, challenge.unlockedAscensionIds)
    val foundGem = isCrit && Random.nextInt(1000) < gemThreshold

    val particleId = ++particleCounter
    val particlePrefix =
      when {
        luckyJackpot -> "★ +"
        isCriticalPet -> "CRIT! +"
        else -> "+"
      }
    val particle =
      FloatingPurrParticle(
        id = particleId,
        text = "$particlePrefix${GameCatalog.formatCompactPurrs(clickGain)}",
        xFraction = xFraction.coerceIn(0.15f, 0.85f),
        yFraction = yFraction.coerceIn(0.15f, 0.80f),
        isComboCrit = isCrit,
      )
    val gemParticleId = if (foundGem) ++particleCounter else -1L
    val gemParticle =
      if (foundGem) {
        FloatingPurrParticle(
          id = gemParticleId,
          text = "💎 +1 Gem",
          xFraction = (xFraction + 0.10f).coerceIn(0.18f, 0.85f),
          yFraction = (yFraction - 0.08f).coerceIn(0.12f, 0.78f),
          isComboCrit = true,
          isGemDrop = true,
        )
      } else {
        null
      }

    if (foundGem) {
      _toastEvents.tryEmit("💎 RARE DROP! Found +1 Gem while petting!")
    }

    val shouldTriggerDogOmen = !challenge.dogCrashOmenSeen && !state.showDogCrashOmen

    val now = System.currentTimeMillis()
    isDirty = true
    statsDirty = true
    passDirty = true
    challengeDirty = true
    var challengeToPersistImmediately: ChallengeProgressState? = null
    _uiState.update { s ->
      val (syncedFestChallenge, _, _) = ensureHourlyFestivalAndQuests(s.challengeState, now)
      val nextChallenge =
        syncedFestChallenge.let { c ->
          var updated = c.copy(festHourPets = c.festHourPets + 1L)
          if (shouldTriggerDogOmen) {
            updated = updated.copy(dogCrashOmenSeen = true)
          }
          if (c.isHardcoreMode) {
            updated = updated.copy(hardcorePurrs = c.hardcorePurrs + clickGain)
          }
          if (foundGem) {
            updated = updated.copy(gems = c.gems + 1L, totalGemsEarned = c.totalGemsEarned + 1L)
          }
          updated
        }
      if (shouldTriggerDogOmen) {
        challengeToPersistImmediately = nextChallenge
      }
      val festTier = GameCatalog.festivalTierFor(nextChallenge.festivalIsMega)
      val updatedProfile =
        s.profile.copy(
          purrs = if (nextChallenge.isHardcoreMode) s.profile.purrs else s.profile.purrs + clickGain,
          totalPurrsEarned = s.profile.totalPurrsEarned + clickGain,
          totalPets = s.profile.totalPets + 1L,
          eventPurrsEarned = s.profile.eventPurrsEarned + clickGain,
        )
      val nextStats =
        s.playerStats.copy(
          highestComboMultiplier = maxOf(s.playerStats.highestComboMultiplier, comboMult),
          biggestSingleTapPurrs = maxOf(s.playerStats.biggestSingleTapPurrs, clickGain),
        )
      val (syncedPass, _) = ensureHourlyPassQuests(s.battlePassState, now)
      val petPassXp = if ("asc_pass_scholar" in nextChallenge.unlockedAscensionIds) 3L else 2L
      val nextPass =
        syncedPass.copy(
          passXp = syncedPass.passXp + petPassXp,
          hourPets = syncedPass.hourPets + 1L,
          hourPurrsEarned = syncedPass.hourPurrsEarned + clickGain,
          hourComboCrits = syncedPass.hourComboCrits + (if (isCrit) 1L else 0L),
        )
      val nextFreeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
      val nextVipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
      val nextQuests = GameCatalog.evaluateHourlyPassQuests(nextPass)
      val appended = buildList {
        addAll(s.particles)
        add(particle)
        if (gemParticle != null) add(gemParticle)
      }
      s.copy(
        profile = updatedProfile,
        challengeState = nextChallenge,
        showDogCrashOmen = s.showDogCrashOmen || shouldTriggerDogOmen,
        festivalTier = festTier,
        hourlyFestivalQuests = GameCatalog.evaluateHourlyFestivalQuests(nextChallenge),
        playerStats = nextStats,
        battlePassState = nextPass,
        currentPassTier = nextFreeTier,
        currentVipPassTier = nextVipTier,
        freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(nextPass.passXp),
        vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(nextPass.passXp),
        hourlyPassQuests = nextQuests,
        claimablePassQuestsCount = nextQuests.count { q -> !q.isClaimed && q.isCompleted },
        claimablePassCount = countClaimablePassRewards(nextPass, nextFreeTier, nextVipTier),
        comboCharge = newCombo,
        comboMultiplier = comboMult,
        effectiveClickPower =
          computeEffectiveClick(
            updatedProfile,
            s.weather,
            s.activeEvent,
            s.moonlightState.phase,
            comboMult,
            nextChallenge,
            s.goldenRainActive,
            festTier,
          ),
        particles = appended.takeLast(12),
      )
    }

    challengeToPersistImmediately?.let { persisted ->
      viewModelScope.launch {
        localStorage.saveChallengeProgress(persisted)
      }
    }

    viewModelScope.launch {
      delay(750L)
      _uiState.update { s ->
        s.copy(
          particles =
            s.particles.filterNot { it.id == particleId || (gemParticleId > 0L && it.id == gemParticleId) }
        )
      }
    }
  }

  fun dismissDogCrashOmen() {
    _uiState.update { it.copy(showDogCrashOmen = false) }
  }

  fun completeTrillionCutscene() {
    val state = _uiState.value
    val nextChallenge = state.challengeState.copy(trillionCutsceneSeen = true)
    saveChallengeAndRefresh(nextChallenge)
    val currentProfile = state.profile
    val nextBadges = (currentProfile.unlockedBadgeIds + "royal_miga_decree").distinct()
    val updatedProfile =
      currentProfile.copy(
        unlockedBadgeIds = nextBadges,
        equippedBadgeId = currentProfile.equippedBadgeId.ifBlank { "royal_miga_decree" },
      )
    _uiState.update { it.copy(showTrillionCutscene = false) }
    applyAndSaveImmediate(updatedProfile)
    soundManager.playUiConfirm()
    _toastEvents.tryEmit("👑 Royal Achievement Granted: Miga's Royal Decree!")
  }

  fun teleportToFeastZone(landmarkId: String) {
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    if (GameCatalog.part2MapLandmarks.none { it.id == landmarkId }) return
    soundManager.playUiConfirm()
    _uiState.update { it.copy(activeFeastZoneId = landmarkId) }
  }

  fun exitFeastZone() {
    soundManager.playUiTap()
    _uiState.update { it.copy(activeFeastZoneId = null) }
  }

  fun grantFeastZoneReward(
    zoneTitle: String,
    basePurrs: Long,
    turkeys: Long,
    passXp: Long,
    bonusGems: Long = 0L,
  ): Long {
    val state = _uiState.value
    if (enforceModerationBlock(state)) return 0L
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val scaledPurrs =
      maxOf(
        basePurrs.coerceAtLeast(0L),
        (state.effectivePps * 6L + state.effectiveClickPower * 16L).coerceAtMost(2_500_000_000L),
      )
    val safeTurkeys = turkeys.coerceAtLeast(0L)
    val safeGems = bonusGems.coerceAtLeast(0L)
    val nextChallenge =
      challenge.copy(
        hardcorePurrs =
          if (isHardcore) challenge.hardcorePurrs + scaledPurrs else challenge.hardcorePurrs,
        turkeys = challenge.turkeys + safeTurkeys,
        totalTurkeysEarned = challenge.totalTurkeysEarned + safeTurkeys,
        gems = challenge.gems + safeGems,
        totalGemsEarned = challenge.totalGemsEarned + safeGems,
      )
    saveChallengeAndRefresh(nextChallenge)

    val current = state.profile
    val updatedProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else current.purrs + scaledPurrs,
        totalPurrsEarned = current.totalPurrsEarned + scaledPurrs,
        eventPurrsEarned = current.eventPurrsEarned + scaledPurrs,
      )
    applyAndSaveImmediate(updatedProfile)
    if (passXp > 0L) {
      grantPassXpImmediate(passXp)
    }
    soundManager.playUiConfirm()

    val parts = buildList {
      if (scaledPurrs > 0L) add("+${GameCatalog.formatCompactPurrs(scaledPurrs)} ${state.activeCurrencyShortLabel}")
      if (safeTurkeys > 0L) add("+$safeTurkeys 🦃")
      if (safeGems > 0L) add("+$safeGems 💎")
    }
    _toastEvents.tryEmit("$zoneTitle reward: ${parts.joinToString(" • ")}!")
    return scaledPurrs
  }

  fun triggerBossGlitchCutscene() {
    soundManager.playWindowGatoErrorBeep()
    _uiState.update { it.copy(showBossGlitchCutscene = true) }
  }

  fun completeBossGlitchCutscene() {
    val state = _uiState.value
    val challenge = state.challengeState
    val profile = state.profile
    val rewardId = "melted_solstice_crown"
    val firstTimeCrown = rewardId !in profile.unlockedAccessories
    val turkeyGain = if (firstTimeCrown) 5L else 3L
    val gemGain = if (firstTimeCrown) 1L else 0L
    val bonusPurrs = maxOf(25_000L, (state.effectivePps * 12L + state.effectiveClickPower * 30L).coerceAtMost(5_000_000_000L))
    val isHardcore = challenge.isHardcoreMode

    val nextChallenge =
      challenge.copy(
        citadelBossGlitchSeen = true,
        hardcorePurrs =
          if (isHardcore) challenge.hardcorePurrs + bonusPurrs else challenge.hardcorePurrs,
        turkeys = challenge.turkeys + turkeyGain,
        totalTurkeysEarned = challenge.totalTurkeysEarned + turkeyGain,
        gems = challenge.gems + gemGain,
        totalGemsEarned = challenge.totalGemsEarned + gemGain,
      )
    saveChallengeAndRefresh(nextChallenge)

    val updatedAccessories = (profile.unlockedAccessories + rewardId).distinct()
    val updatedProfile =
      profile.copy(
        purrs = if (isHardcore) profile.purrs else profile.purrs + bonusPurrs,
        totalPurrsEarned = profile.totalPurrsEarned + bonusPurrs,
        eventPurrsEarned = profile.eventPurrsEarned + bonusPurrs,
        unlockedAccessories = updatedAccessories,
        selectedAccessoryId = rewardId,
      )
    _uiState.update {
      it.copy(
        showBossGlitchCutscene = false,
        activeFeastZoneId = null,
      )
    }
    applyAndSaveImmediate(updatedProfile)
    if (firstTimeCrown) {
      recordLimitedItemUnlock("ACCESSORY", rewardId)
    }
    grantPassXpImmediate(if (firstTimeCrown) 100L else 45L)
    soundManager.playUiConfirm()
    _toastEvents.tryEmit(
      if (firstTimeCrown) {
        "❄️ Claimed Limited-Time Reward: Melting Solstice Crown (+5 🦃 & +1 💎)!"
      } else {
        "❄️ Citadel Boss anomaly cleared again! +3 🦃 & +${GameCatalog.formatCompactPurrs(bonusPurrs)} ${state.activeCurrencyShortLabel}!"
      }
    )
  }

  fun playUiTap() {
    soundManager.playUiTap()
  }

  fun playUiConfirm() {
    soundManager.playUiConfirm()
  }

  fun playPillSlideTick() {
    soundManager.playPillSlideTick()
  }

  fun buyUpgrade(upgradeId: String) {
    val def = GameCatalog.upgrades.find { it.id == upgradeId } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val currentLevel =
      if (isHardcore) (challenge.hardcoreUpgradeLevels[upgradeId] ?: 0L)
      else (current.upgradeLevels[upgradeId] ?: 0L)
    val cost =
      GameCatalog.effectiveUpgradeCost(def, currentLevel, isHardcore, challenge.unlockedAscensionIds)
    val balance = state.activeCurrencyBalance

    if (balance < cost) {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} for ${def.name}!")
      return
    }

    val nextStats =
      state.playerStats.copy(totalUpgradesPurchased = state.playerStats.totalUpgradesPurchased + 1L)
    _uiState.update { it.copy(playerStats = nextStats) }
    statsDirty = false
    viewModelScope.launch { localStorage.savePlayerStats(nextStats) }
    grantPassXpImmediate(15L, incrementHourUpgrade = true)

    val (syncedFest, _, _) = ensureHourlyFestivalAndQuests(challenge, System.currentTimeMillis())
    if (isHardcore) {
      val updatedLevels =
        syncedFest.hardcoreUpgradeLevels.toMutableMap().apply { put(upgradeId, currentLevel + 1L) }
      val nextChallenge =
        syncedFest.copy(
          hardcorePurrs = balance - cost,
          hardcoreUpgradeLevels = updatedLevels,
          festHourUpgradesBought = syncedFest.festHourUpgradesBought + 1L,
        )
      saveChallengeAndRefresh(nextChallenge)
    } else {
      saveChallengeAndRefresh(
        syncedFest.copy(festHourUpgradesBought = syncedFest.festHourUpgradesBought + 1L)
      )
      val updatedLevels =
        current.upgradeLevels.toMutableMap().apply { put(upgradeId, currentLevel + 1L) }
      val updatedProfile = current.copy(purrs = balance - cost, upgradeLevels = updatedLevels)
      applyAndSaveImmediate(updatedProfile)
    }
    _toastEvents.tryEmit("Upgraded ${def.name} to Lv.${currentLevel + 1}!")
  }

  fun selectOrUnlockPattern(patternId: String) {
    val def = GameCatalog.patterns.find { it.id == patternId } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val cost =
      GameCatalog.effectiveItemCost(def.cost, isHardcore, challenge.unlockedAscensionIds)
    val balance = state.activeCurrencyBalance

    if (patternId in current.unlockedPatterns) {
      applyAndSaveImmediate(current.copy(selectedPatternId = patternId))
      val extra =
        if (patternId == GameCatalog.GOLDEN_TOUCH_SKIN_ID) " (30m -> Golden Rain active!)" else ""
      _toastEvents.tryEmit("Equipped ${def.name} coat!$extra")
    } else if (def.cost == 0L && (def.unlockSourceLabel?.startsWith("Day") == true || def.unlockSourceLabel?.contains("November Pass") == true)) {
      _toastEvents.tryEmit("Unlock ${def.name} via ${def.unlockSourceLabel}!")
    } else if (balance >= cost) {
      val nextBalance = balance - cost
      if (isHardcore) {
        saveChallengeAndRefresh(challenge.copy(hardcorePurrs = nextBalance))
      }
      val updated =
        current.copy(
          purrs = if (isHardcore) current.purrs else nextBalance,
          unlockedPatterns = current.unlockedPatterns + patternId,
          selectedPatternId = patternId,
        )
      applyAndSaveImmediate(updated)
      if (def.isEventExclusive) {
        recordLimitedItemUnlock("PATTERN", patternId)
      }
      _toastEvents.tryEmit("Unlocked & equipped ${def.name}!")
    } else {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} for ${def.name}.")
    }
  }

  fun selectOrUnlockAccessory(accessoryId: String) {
    val def = GameCatalog.accessories.find { it.id == accessoryId } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val cost =
      GameCatalog.effectiveItemCost(def.cost, isHardcore, challenge.unlockedAscensionIds)
    val balance = state.activeCurrencyBalance

    if (accessoryId in current.unlockedAccessories) {
      applyAndSaveImmediate(current.copy(selectedAccessoryId = accessoryId))
      _toastEvents.tryEmit("Equipped ${def.name}!")
    } else if (def.cost == 0L && (def.unlockSourceLabel?.startsWith("Day") == true || def.unlockSourceLabel?.contains("November Pass") == true)) {
      _toastEvents.tryEmit("Unlock ${def.name} via ${def.unlockSourceLabel}!")
    } else if (balance >= cost) {
      val nextBalance = balance - cost
      if (isHardcore) {
        saveChallengeAndRefresh(challenge.copy(hardcorePurrs = nextBalance))
      }
      val updated =
        current.copy(
          purrs = if (isHardcore) current.purrs else nextBalance,
          unlockedAccessories = current.unlockedAccessories + accessoryId,
          selectedAccessoryId = accessoryId,
        )
      applyAndSaveImmediate(updated)
      if (def.isEventExclusive) {
        recordLimitedItemUnlock("ACCESSORY", accessoryId)
      }
      _toastEvents.tryEmit("Unlocked & equipped ${def.name}!")
    } else {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} for ${def.name}.")
    }
  }

  fun selectOrUnlockBackground(backgroundId: String) {
    val def = GameCatalog.backgrounds.find { it.id == backgroundId } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val cost =
      GameCatalog.effectiveItemCost(def.cost, isHardcore, challenge.unlockedAscensionIds)
    val balance = state.activeCurrencyBalance

    if (backgroundId in current.unlockedBackgrounds) {
      applyAndSaveImmediate(current.copy(selectedBackgroundId = backgroundId))
      soundManager.playUiConfirm()
      _toastEvents.tryEmit("Equipped ${def.name} room!")
    } else if (balance >= cost) {
      val nextBalance = balance - cost
      if (isHardcore) {
        saveChallengeAndRefresh(challenge.copy(hardcorePurrs = nextBalance))
      }
      val updated =
        current.copy(
          purrs = if (isHardcore) current.purrs else nextBalance,
          unlockedBackgrounds = (current.unlockedBackgrounds + backgroundId).distinct(),
          selectedBackgroundId = backgroundId,
        )
      applyAndSaveImmediate(updated)
      soundManager.playUiConfirm()
      _toastEvents.tryEmit("Unlocked & equipped ${def.name}!")
    } else {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} for ${def.name}.")
    }
  }

  fun dismissUpdateBlogPost() {
    val version =
      _uiState.value.pendingUpdateBlogPost?.version
        ?: GameCatalog.blogPosts.firstOrNull()?.version
        ?: return
    _uiState.update { it.copy(pendingUpdateBlogPost = null) }
    viewModelScope.launch {
      localStorage.saveLastSeenBlogVersion(version)
    }
  }

  fun selectOrUnlockColorTheme(theme: AppColorTheme, allowPurchase: Boolean = false) {
    val state = _uiState.value
    val currentSettings = state.settings
    val currentProfile = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val isUnlocked = theme.isFree || theme.id in currentSettings.unlockedColorThemes
    if (isUnlocked) {
      updateSettings { it.copy(colorTheme = theme) }
      _toastEvents.tryEmit("Applied ${theme.displayName} theme!")
      return
    }
    if (!allowPurchase) {
      _toastEvents.tryEmit("Locked! Requirement: ${theme.requirementSummary}")
      return
    }
    val (reqMet, failReason) = GameCatalog.evaluateThemeRequirement(theme, currentProfile)
    if (!reqMet) {
      _toastEvents.tryEmit(failReason ?: "Requirement not met: ${theme.requirementSummary}")
      return
    }
    val cost =
      GameCatalog.effectiveItemCost(theme.eventCost, isHardcore, challenge.unlockedAscensionIds)
    val balance = state.activeCurrencyBalance
    if (balance < cost) {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit(
        "Need $missing more ${state.activeCurrencyShortLabel} for ${theme.displayName} theme!"
      )
      return
    }
    val nextBalance = balance - cost
    if (isHardcore) {
      saveChallengeAndRefresh(challenge.copy(hardcorePurrs = nextBalance))
    } else {
      applyAndSaveImmediate(currentProfile.copy(purrs = nextBalance))
    }
    val isFirstUnlock = theme.id !in currentSettings.unlockedColorThemes
    updateSettings { s ->
      s.copy(
        unlockedColorThemes = (s.unlockedColorThemes + theme.id).distinct(),
        colorTheme = theme,
      )
    }
    if (isFirstUnlock && !theme.isFree) {
      recordLimitedItemUnlock("THEME", theme.id)
    }
    _toastEvents.tryEmit("Unlocked & applied ${theme.displayName} theme!")
  }

  fun toggleHardcoreMode() {
    val state = _uiState.value
    val eligible =
      state.challengeState.hardcoreUnlocked ||
        GameCatalog.meetsHardcoreRequirements(state.profile, state.challengeState.rebirthCount)
    if (!eligible) {
      _toastEvents.tryEmit(
        "Need 1M Lifetime Purrs, 5K Pets, 50 Upgrades & 5 Badges to unlock Hardcore Mode!"
      )
      return
    }
    val nextMode = !state.challengeState.isHardcoreMode
    val nextChallenge =
      state.challengeState.copy(isHardcoreMode = nextMode, hardcoreUnlocked = true)
    _uiState.update { s ->
      s.copy(
        challengeState = nextChallenge,
        hardcoreEligible = true,
        comboCharge = 0f,
        comboMultiplier = 1.0,
        nextRebirthCost = GameCatalog.rebirthCost(nextChallenge.rebirthCount, nextMode),
      )
    }
    challengeDirty = false
    viewModelScope.launch { localStorage.saveChallengeProgress(nextChallenge) }
    applyProfile(_uiState.value.profile, isLoaded = true)
    _toastEvents.tryEmit(
      if (nextMode) "HARDCORE MODE: Separate Hardcore Purrs active (3.5x Prices & 0.75x Gains)!"
      else "Returned to Standard Mode (Hardcore Purrs safely stowed)."
    )
  }

  fun performRebirth() {
    val state = _uiState.value
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val cost = GameCatalog.rebirthCost(challenge.rebirthCount, isHardcore)
    val balance = state.activeCurrencyBalance
    if (balance < cost) {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} to Rebirth!")
      return
    }

    val exemptPatterns = current.unlockedPatterns.filter { GameCatalog.isEventItemExemptPattern(it) }
    val preservedPatterns = (listOf("tabby") + exemptPatterns).distinct()
    val exemptAccessories =
      current.unlockedAccessories.filter { GameCatalog.isEventItemExemptAccessory(it) }
    val preservedAccessories = (listOf("none") + exemptAccessories).distinct()

    val nextPattern =
      if (current.selectedPatternId in preservedPatterns) current.selectedPatternId else "tabby"
    val nextAccessory =
      if (current.selectedAccessoryId in preservedAccessories) current.selectedAccessoryId else "none"

    val starterPurrs =
      if ("asc_rebirth_echo" in challenge.unlockedAscensionIds) 1_000L else 0L
    val starterUpgrades =
      if ("asc_cosmic_sovereign" in challenge.unlockedAscensionIds) {
        mapOf("feather_wand" to 3L, "cardboard_box" to 3L)
      } else {
        emptyMap()
      }

    // Rebirths DO NOT reset dailyStreak or unlocked Color Themes!
    val resetProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else starterPurrs,
        upgradeLevels = if (isHardcore) current.upgradeLevels else starterUpgrades,
        unlockedPatterns = preservedPatterns,
        unlockedAccessories = preservedAccessories,
        selectedPatternId = nextPattern,
        selectedAccessoryId = nextAccessory,
      )

    val nextRebirthCount = challenge.rebirthCount + 1L
    val nextBonusSteps = challenge.rebirthBonusSteps + 1L
    val nextHardcoreUnlocked =
      challenge.hardcoreUnlocked ||
        GameCatalog.meetsHardcoreRequirements(resetProfile, nextRebirthCount)
    val nextChallenge =
      challenge.copy(
        rebirthCount = nextRebirthCount,
        rebirthBonusSteps = nextBonusSteps,
        hardcoreUnlocked = nextHardcoreUnlocked,
        hardcorePurrs = if (isHardcore) starterPurrs else challenge.hardcorePurrs,
        hardcoreUpgradeLevels =
          if (isHardcore) starterUpgrades else challenge.hardcoreUpgradeLevels,
        gems = challenge.gems + GameCatalog.REBIRTH_GEM_REWARD,
        totalGemsEarned = challenge.totalGemsEarned + GameCatalog.REBIRTH_GEM_REWARD,
      )
    val newMult =
      GameCatalog.rebirthMultiplier(nextBonusSteps, nextChallenge.unlockedAscensionIds)
    _uiState.update { s ->
      s.copy(
        challengeState = nextChallenge,
        comboCharge = 0f,
        comboMultiplier = 1.0,
        nextRebirthCost = GameCatalog.rebirthCost(nextRebirthCount, isHardcore),
        rebirthMultiplier = newMult,
        hardcoreEligible = nextHardcoreUnlocked,
      )
    }
    challengeDirty = false
    viewModelScope.launch { localStorage.saveChallengeProgress(nextChallenge) }
    applyAndSaveImmediate(resetProfile)
    _toastEvents.tryEmit(
      "Rebirth #$nextRebirthCount Complete! ${newMult}x multiplier & +${GameCatalog.REBIRTH_GEM_REWARD} Gem (Streak, Themes & Gems kept!)"
    )
  }

  fun performGatoAscension() {
    val state = _uiState.value
    val current = state.profile
    val challenge = state.challengeState
    if (challenge.rebirthCount < GameCatalog.ASCENSION_REQUIRED_REBIRTHS) {
      _toastEvents.tryEmit(
        "Need ${GameCatalog.ASCENSION_REQUIRED_REBIRTHS} Rebirths to perform Gato Ascensión!"
      )
      return
    }

    val exemptPatterns = current.unlockedPatterns.filter { GameCatalog.isEventItemExemptPattern(it) }
    val preservedPatterns = (listOf("tabby") + exemptPatterns).distinct()
    val exemptAccessories =
      current.unlockedAccessories.filter { GameCatalog.isEventItemExemptAccessory(it) }
    val preservedAccessories = (listOf("none") + exemptAccessories).distinct()

    val nextPattern =
      if (current.selectedPatternId in preservedPatterns) current.selectedPatternId else "tabby"
    val nextAccessory =
      if (current.selectedAccessoryId in preservedAccessories) current.selectedAccessoryId else "none"

    val starterPurrs =
      if ("asc_rebirth_echo" in challenge.unlockedAscensionIds) 1_000L else 0L
    val starterUpgrades =
      if ("asc_cosmic_sovereign" in challenge.unlockedAscensionIds) {
        mapOf("feather_wand" to 3L, "cardboard_box" to 3L)
      } else {
        emptyMap()
      }
    val isHardcore = challenge.isHardcoreMode

    val resetProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else starterPurrs,
        upgradeLevels = if (isHardcore) current.upgradeLevels else starterUpgrades,
        unlockedPatterns = preservedPatterns,
        unlockedAccessories = preservedAccessories,
        selectedPatternId = nextPattern,
        selectedAccessoryId = nextAccessory,
      )

    val nextShards = challenge.gatoShards + 1L
    val nextAscensionCount = challenge.ascensionCount + 1L
    val ascensionGemGain =
      GameCatalog.ASCENSION_GEM_REWARD +
        (if ("asc_gem_seeker" in challenge.unlockedAscensionIds) 1L else 0L)
    val nextChallenge =
      challenge.copy(
        rebirthCount = GameCatalog.ASCENSION_RESET_REBIRTH_COUNT,
        rebirthBonusSteps = 0L,
        ascensionCount = nextAscensionCount,
        gatoShards = nextShards,
        gems = challenge.gems + ascensionGemGain,
        totalGemsEarned = challenge.totalGemsEarned + ascensionGemGain,
        hardcorePurrs = if (isHardcore) starterPurrs else challenge.hardcorePurrs,
        hardcoreUpgradeLevels =
          if (isHardcore) starterUpgrades else challenge.hardcoreUpgradeLevels,
      )
    val resetMult = GameCatalog.rebirthMultiplier(0L, nextChallenge.unlockedAscensionIds)
    _uiState.update { s ->
      s.copy(
        challengeState = nextChallenge,
        comboCharge = 0f,
        comboMultiplier = 1.0,
        nextRebirthCost =
          GameCatalog.rebirthCost(GameCatalog.ASCENSION_RESET_REBIRTH_COUNT, isHardcore),
        rebirthMultiplier = resetMult,
      )
    }
    challengeDirty = false
    viewModelScope.launch { localStorage.saveChallengeProgress(nextChallenge) }
    applyAndSaveImmediate(resetProfile)
    _toastEvents.tryEmit(
      "Gato Ascensión #$nextAscensionCount! Earned +1 Gato Shard & +$ascensionGemGain Gems!"
    )
  }

  fun unlockAscensionUpgrade(nodeId: String) {
    val node = GameCatalog.ascensionUpgrades.find { it.id == nodeId } ?: return
    val state = _uiState.value
    val challenge = state.challengeState
    val (canUnlock, reason) = GameCatalog.canUnlockAscensionNode(node, challenge)
    if (!canUnlock) {
      _toastEvents.tryEmit(reason ?: "Cannot unlock ${node.name} yet!")
      return
    }
    val nextChallenge =
      challenge.copy(
        gatoShards = challenge.gatoShards - node.shardCost,
        unlockedAscensionIds = (challenge.unlockedAscensionIds + node.id).distinct(),
      )
    saveChallengeAndRefresh(nextChallenge)
    _toastEvents.tryEmit("Ascension Tree unlocked: ${node.name}!")
  }

  private fun saveChallengeAndRefresh(nextChallenge: ChallengeProgressState) {
    challengeDirty = false
    _uiState.update { it.copy(challengeState = nextChallenge) }
    viewModelScope.launch {
      localStorage.saveChallengeProgress(nextChallenge)
      onSyncWidgets()
    }
    applyProfile(_uiState.value.profile, isLoaded = true)
  }

  fun renameCat(rawName: String) {
    val cleaned = rawName.trim().take(24)
    if (cleaned.isEmpty()) {
      _toastEvents.tryEmit("Cat name cannot be empty!")
      return
    }
    val updated = _uiState.value.profile.copy(selectedCatName = cleaned)
    applyAndSaveImmediate(updated)
    _toastEvents.tryEmit("Renamed your cat to $cleaned!")
  }

  fun claimDailyReward() {
    val state = _uiState.value
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    val today = currentUtcEpochDay()
    if (current.lastDailyClaimEpochDay >= today) {
      _toastEvents.tryEmit("Today's Daily Treat is already claimed! Come back tomorrow.")
      return
    }

    val newStreak =
      if (current.lastDailyClaimEpochDay == today - 1L) {
        current.dailyStreak + 1L
      } else {
        1L
      }
    val payout = GameCatalog.computeEndlessDailyPayout(newStreak)
    val reward = payout.baseReward

    var unlockedPatterns = current.unlockedPatterns
    var unlockedAccessories = current.unlockedAccessories
    var upgradeLevels = current.upgradeLevels
    var hardcoreUpgradeLevels = challenge.hardcoreUpgradeLevels
    var bonusLabelSuffix = ""

    when (reward.bonusType) {
      DailyBonusPrizeType.PATTERN -> {
        if (reward.bonusItemId.isNotBlank() && reward.bonusItemId !in unlockedPatterns) {
          unlockedPatterns = unlockedPatterns + reward.bonusItemId
          bonusLabelSuffix = " & ${reward.bonusPrizeLabel}"
          if (GameCatalog.patterns.any { it.id == reward.bonusItemId && it.isEventExclusive }) {
            recordLimitedItemUnlock("PATTERN", reward.bonusItemId)
          }
        }
      }
      DailyBonusPrizeType.ACCESSORY -> {
        if (reward.bonusItemId.isNotBlank() && reward.bonusItemId !in unlockedAccessories) {
          unlockedAccessories = unlockedAccessories + reward.bonusItemId
          bonusLabelSuffix = " & ${reward.bonusPrizeLabel}"
          if (GameCatalog.accessories.any { it.id == reward.bonusItemId && it.isEventExclusive }) {
            recordLimitedItemUnlock("ACCESSORY", reward.bonusItemId)
          }
        }
      }
      DailyBonusPrizeType.COLOR_THEME -> {
        if (reward.bonusItemId.isNotBlank() && reward.bonusItemId !in state.settings.unlockedColorThemes) {
          updateSettings { s ->
            s.copy(unlockedColorThemes = (s.unlockedColorThemes + reward.bonusItemId).distinct())
          }
          bonusLabelSuffix = " & ${reward.bonusPrizeLabel}"
          if (AppColorTheme.entries.any { it.id == reward.bonusItemId && !it.isFree }) {
            recordLimitedItemUnlock("THEME", reward.bonusItemId)
          }
        }
      }
      DailyBonusPrizeType.UPGRADE_LEVEL,
      DailyBonusPrizeType.NONE -> Unit
    }

    if (payout.bonusUpgradeLevel) {
      val targetUpgradeId =
        reward.bonusItemId.takeIf {
          reward.bonusType == DailyBonusPrizeType.UPGRADE_LEVEL && it.isNotBlank()
        } ?: GameCatalog.upgrades[(newStreak.toInt()) % GameCatalog.upgrades.size].id
      if (isHardcore) {
        val mutable = hardcoreUpgradeLevels.toMutableMap()
        mutable[targetUpgradeId] = (mutable[targetUpgradeId] ?: 0L) + 1L
        hardcoreUpgradeLevels = mutable
      } else {
        val mutable = upgradeLevels.toMutableMap()
        mutable[targetUpgradeId] = (mutable[targetUpgradeId] ?: 0L) + 1L
        upgradeLevels = mutable
      }
      val upgName = GameCatalog.upgrades.find { it.id == targetUpgradeId }?.name ?: "Upgrade"
      bonusLabelSuffix += " & +1 $upgName Lv"
    }

    if (payout.bonusShards > 0L) {
      bonusLabelSuffix += " & +${payout.bonusShards} Gato Shard!"
    }
    if (payout.bonusGems > 0L) {
      bonusLabelSuffix += " & +${payout.bonusGems} Gems!"
    }

    val nextChallenge =
      challenge.copy(
        hardcorePurrs =
          if (isHardcore) challenge.hardcorePurrs + payout.totalPurrs else challenge.hardcorePurrs,
        hardcoreUpgradeLevels = hardcoreUpgradeLevels,
        gatoShards = challenge.gatoShards + payout.bonusShards,
        gems = challenge.gems + payout.bonusGems,
        totalGemsEarned = challenge.totalGemsEarned + payout.bonusGems,
      )
    if (isHardcore || payout.bonusShards > 0L || payout.bonusGems > 0L) {
      saveChallengeAndRefresh(nextChallenge)
    }

    val nextStats =
      state.playerStats.copy(
        totalDailyRewardsClaimed = state.playerStats.totalDailyRewardsClaimed + 1L,
        longestDailyStreak = maxOf(state.playerStats.longestDailyStreak, newStreak),
      )
    _uiState.update { it.copy(playerStats = nextStats) }
    statsDirty = false
    viewModelScope.launch { localStorage.savePlayerStats(nextStats) }

    val updated =
      current.copy(
        purrs = if (isHardcore) current.purrs else current.purrs + payout.totalPurrs,
        totalPurrsEarned = current.totalPurrsEarned + payout.totalPurrs,
        unlockedPatterns = unlockedPatterns,
        unlockedAccessories = unlockedAccessories,
        upgradeLevels = upgradeLevels,
        dailyStreak = newStreak,
        lastDailyClaimEpochDay = today,
      )
    applyAndSaveImmediate(updated)
    grantPassXpImmediate(60L)
    _toastEvents.tryEmit(
      "Day $newStreak Streak! +${GameCatalog.formatCompactPurrs(payout.totalPurrs)} ${state.activeCurrencyShortLabel}$bonusLabelSuffix"
    )
  }

  fun claimWeeklyLeaderboardReward(isAutoRollover: Boolean = false) {
    val state = _uiState.value
    val now = System.currentTimeMillis()
    val currentWeek = GameCatalog.currentEpochWeek(now)
    if (state.playerStats.lastWeeklyRewardWeekId >= currentWeek) {
      if (!isAutoRollover) {
        _toastEvents.tryEmit(
          "This week's Leaderboard Reward is already claimed! Next payout in ${state.weeklyResetCountdownText}."
        )
      }
      return
    }

    val tier = GameCatalog.weeklyTierForRank(state.currentPlayerGlobalRank)
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode

    var upgradeLevels = current.upgradeLevels
    var hardcoreUpgradeLevels = challenge.hardcoreUpgradeLevels
    var bonusUpgradeName: String? = null
    if (tier.bonusUpgradeLevels > 0L) {
      val chosen = GameCatalog.upgrades.first()
      bonusUpgradeName = chosen.name
      if (isHardcore) {
        val mutable = hardcoreUpgradeLevels.toMutableMap()
        mutable[chosen.id] = (mutable[chosen.id] ?: 0L) + tier.bonusUpgradeLevels
        hardcoreUpgradeLevels = mutable
      } else {
        val mutable = upgradeLevels.toMutableMap()
        mutable[chosen.id] = (mutable[chosen.id] ?: 0L) + tier.bonusUpgradeLevels
        upgradeLevels = mutable
      }
    }

    val nextChallenge =
      challenge.copy(
        hardcorePurrs =
          if (isHardcore) challenge.hardcorePurrs + tier.purrsReward else challenge.hardcorePurrs,
        hardcoreUpgradeLevels = hardcoreUpgradeLevels,
        gatoShards = challenge.gatoShards + tier.shardsReward,
        gems = challenge.gems + tier.gemsReward,
        totalGemsEarned = challenge.totalGemsEarned + tier.gemsReward,
      )
    if (isHardcore || tier.shardsReward > 0L || tier.gemsReward > 0L || tier.bonusUpgradeLevels > 0L) {
      saveChallengeAndRefresh(nextChallenge)
    }

    val receipt =
      WeeklyRewardReceipt(
        weekId = currentWeek,
        rankAchieved = state.currentPlayerGlobalRank,
        tierTitle = tier.title,
        purrsEarned = tier.purrsReward,
        shardsEarned = tier.shardsReward,
        gemsEarned = tier.gemsReward,
        bonusUpgradeName = bonusUpgradeName?.let { "+${tier.bonusUpgradeLevels} $it" },
      )

    val nextStats =
      state.playerStats.copy(
        lastWeeklyRewardWeekId = currentWeek,
        weeklyRewardsClaimedCount = state.playerStats.weeklyRewardsClaimedCount + 1L,
        totalWeeklyPurrsEarned = state.playerStats.totalWeeklyPurrsEarned + tier.purrsReward,
      )
    _uiState.update {
      it.copy(
        playerStats = nextStats,
        canClaimWeeklyReward = false,
        pendingWeeklyReward = receipt,
      )
    }
    statsDirty = false
    viewModelScope.launch { localStorage.savePlayerStats(nextStats) }

    val updatedProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else current.purrs + tier.purrsReward,
        totalPurrsEarned = current.totalPurrsEarned + tier.purrsReward,
        upgradeLevels = upgradeLevels,
      )
    applyAndSaveImmediate(updatedProfile)
    _toastEvents.tryEmit(
      "Weekly Reward (${tier.title}): +${GameCatalog.formatCompactPurrs(tier.purrsReward)} ${state.activeCurrencyShortLabel}!"
    )
  }

  fun dismissWeeklyRewardReceipt() {
    _uiState.update { it.copy(pendingWeeklyReward = null) }
  }

  fun dismissOfflineEarningsReport() {
    _uiState.update { it.copy(pendingOfflineReport = null) }
  }

  private fun checkWeeklyAutoRollover(nowMillis: Long) {
    val state = _uiState.value
    val currentWeek = GameCatalog.currentEpochWeek(nowMillis)
    if (state.playerStats.lastWeeklyRewardWeekId in 1L until currentWeek) {
      claimWeeklyLeaderboardReward(isAutoRollover = true)
    }
  }

  private fun processOfflineReturn(lastActiveMs: Long, nowMs: Long) {
    if (lastActiveMs <= 0L) {
      val updatedStats = _uiState.value.playerStats.copy(lastActiveTimestampMillis = nowMs)
      _uiState.update { it.copy(playerStats = updatedStats) }
      viewModelScope.launch { localStorage.savePlayerStats(updatedStats) }
      return
    }
    val elapsedMs = (nowMs - lastActiveMs).coerceAtLeast(0L)
    val state = _uiState.value
    val challenge = state.challengeState
    val pps =
      computeEffectivePps(
        state.profile,
        state.weather,
        state.activeEvent,
        state.moonlightState.phase,
        challenge,
        state.goldenRainActive,
        state.festivalTier,
      )
    val report =
      GameCatalog.computeOfflineEarnings(
        elapsedMillis = elapsedMs,
        effectivePps = pps,
        isHardcore = challenge.isHardcoreMode,
        hasAscended = challenge.ascensionCount > 0L,
        unlockedAscensionIds = challenge.unlockedAscensionIds,
      )
    if (report == null) {
      val updatedStats = state.playerStats.copy(lastActiveTimestampMillis = nowMs)
      _uiState.update { it.copy(playerStats = updatedStats) }
      viewModelScope.launch { localStorage.savePlayerStats(updatedStats) }
      return
    }

    val nextStats =
      state.playerStats.copy(
        lastActiveTimestampMillis = nowMs,
        totalOfflineSeconds = state.playerStats.totalOfflineSeconds + report.cappedSeconds,
        totalOfflinePurrsEarned = state.playerStats.totalOfflinePurrsEarned + report.purrsEarned,
      )
    statsDirty = false
    viewModelScope.launch { localStorage.savePlayerStats(nextStats) }

    if (report.purrsEarned > 0L) {
      if (report.isHardcore) {
        saveChallengeAndRefresh(
          challenge.copy(hardcorePurrs = challenge.hardcorePurrs + report.purrsEarned)
        )
      }
      val updatedProfile =
        state.profile.copy(
          purrs =
            if (report.isHardcore) state.profile.purrs
            else state.profile.purrs + report.purrsEarned,
          totalPurrsEarned = state.profile.totalPurrsEarned + report.purrsEarned,
          eventPurrsEarned = state.profile.eventPurrsEarned + report.purrsEarned,
        )
      applyAndSaveImmediate(updatedProfile)
    }

    _uiState.update {
      it.copy(
        playerStats = nextStats,
        pendingOfflineReport = report,
      )
    }
  }

  fun claimQuest(questId: String) {
    val quest = GameCatalog.quests.find { it.id == questId } ?: return
    val state = _uiState.value
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode
    if (questId in current.claimedQuestIds) return
    val progress = quest.progressExtractor(current, challenge, state.playerStats)
    if (progress < quest.target) return

    val newBadges =
      if (quest.badgeId != null && quest.badgeId !in current.unlockedBadgeIds) {
        current.unlockedBadgeIds + quest.badgeId
      } else {
        current.unlockedBadgeIds
      }
    val equipped =
      if (current.equippedBadgeId.isBlank() && quest.badgeId != null) {
        quest.badgeId
      } else {
        current.equippedBadgeId
      }

    if (isHardcore || quest.rewardGems > 0L) {
      saveChallengeAndRefresh(
        challenge.copy(
          hardcorePurrs =
            if (isHardcore) challenge.hardcorePurrs + quest.rewardPurrs else challenge.hardcorePurrs,
          gems = challenge.gems + quest.rewardGems,
          totalGemsEarned = challenge.totalGemsEarned + quest.rewardGems,
        )
      )
    }

    val updated =
      current.copy(
        purrs = if (isHardcore) current.purrs else current.purrs + quest.rewardPurrs,
        totalPurrsEarned = current.totalPurrsEarned + quest.rewardPurrs,
        claimedQuestIds = current.claimedQuestIds + questId,
        unlockedBadgeIds = newBadges,
        equippedBadgeId = equipped,
      )
    applyAndSaveImmediate(updated)
    grantPassXpImmediate(50L)
    val badgeMsg =
      quest.badgeId?.let { id -> GameCatalog.badges.find { it.id == id }?.name }?.let {
        " & $it badge"
      } ?: ""
    val gemMsg = if (quest.rewardGems > 0L) " & +${quest.rewardGems} Gems" else ""
    _toastEvents.tryEmit(
      "Quest complete! +${GameCatalog.formatCompactPurrs(quest.rewardPurrs)} ${state.activeCurrencyShortLabel}$gemMsg$badgeMsg"
    )
  }

  fun unlockNovemberPassVip() {
    val state = _uiState.value
    if (state.battlePassState.vipUnlocked) return
    val (meets, failReason) =
      GameCatalog.meetsNovemberVipRequirements(
        state.profile,
        state.challengeState,
        state.playerStats,
      )
    if (!meets) {
      _toastEvents.tryEmit(failReason ?: "VIP requirements not met yet!")
      return
    }
    val nextChallenge =
      state.challengeState.copy(
        gatoShards = state.challengeState.gatoShards - GameCatalog.NOVEMBER_PASS_VIP_SHARD_COST
      )
    val nextPass = state.battlePassState.copy(vipUnlocked = true)
    val freeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
    val quests = GameCatalog.evaluateHourlyPassQuests(nextPass)
    passDirty = false
    _uiState.update {
      it.copy(
        battlePassState = nextPass,
        currentPassTier = freeTier,
        currentVipPassTier = vipTier,
        freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(nextPass.passXp),
        vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(nextPass.passXp),
        hourlyPassQuests = quests,
        claimablePassQuestsCount = quests.count { q -> !q.isClaimed && q.isCompleted },
        claimablePassCount = countClaimablePassRewards(nextPass, freeTier, vipTier),
      )
    }
    viewModelScope.launch { localStorage.saveBattlePassState(nextPass) }
    saveChallengeAndRefresh(nextChallenge)
    _toastEvents.tryEmit("November Pass VIP Unlocked! Exclusive Golden Touch awaits at Tier 30!")
  }

  fun claimPassTierReward(tier: Int, isVip: Boolean) {
    val tierDef = GameCatalog.novemberPassTiers.find { it.tier == tier } ?: return
    val state = _uiState.value
    val pass = state.battlePassState
    val freeTier = GameCatalog.freeTierFromXp(pass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(pass.passXp)
    val requiredTrackTier = if (isVip) vipTier else freeTier
    if (isVip && !pass.vipUnlocked) {
      _toastEvents.tryEmit("Unlock the VIP Pass first (1 Gato Shard + 100+ Upgrades)!")
      return
    }
    if (requiredTrackTier < tier) {
      val trackName = if (isVip) "VIP Track" else "Free Track"
      _toastEvents.tryEmit("Reach Tier $tier on $trackName first!")
      return
    }
    if (isVip && tier in pass.claimedVipTiers) return
    if (!isVip && tier in pass.claimedFreeTiers) return

    val reward = if (isVip) tierDef.vipReward else tierDef.freeReward
    applyPassRewards(
      rewards = listOf(reward),
      newClaimedFree = if (!isVip) (pass.claimedFreeTiers + tier).distinct() else pass.claimedFreeTiers,
      newClaimedVip = if (isVip) (pass.claimedVipTiers + tier).distinct() else pass.claimedVipTiers,
    )
    _toastEvents.tryEmit("November Pass Tier $tier claimed: ${reward.label}!")
  }

  fun claimAllPassRewards() {
    val state = _uiState.value
    val pass = state.battlePassState
    val freeTier = GameCatalog.freeTierFromXp(pass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(pass.passXp)
    val toGrant = mutableListOf<BattlePassRewardItem>()
    val newFree = pass.claimedFreeTiers.toMutableList()
    val newVip = pass.claimedVipTiers.toMutableList()
    GameCatalog.novemberPassTiers.forEach { def ->
      if (def.tier <= freeTier && def.tier !in newFree) {
        newFree.add(def.tier)
        toGrant.add(def.freeReward)
      }
      if (pass.vipUnlocked && def.tier <= vipTier && def.tier !in newVip) {
        newVip.add(def.tier)
        toGrant.add(def.vipReward)
      }
    }
    if (toGrant.isEmpty()) {
      _toastEvents.tryEmit("No unclaimed November Pass tiers ready yet.")
      return
    }
    applyPassRewards(
      rewards = toGrant,
      newClaimedFree = newFree.distinct(),
      newClaimedVip = newVip.distinct(),
    )
    _toastEvents.tryEmit("Claimed ${toGrant.size} November Pass reward(s)!")
  }

  fun restartNovemberPass() {
    val state = _uiState.value
    val pass = state.battlePassState
    if (!GameCatalog.isNovemberPassCompleted(pass)) {
      _toastEvents.tryEmit("Reach Tier 30 on your active tracks to restart the November Pass!")
      return
    }
    val freeTier = GameCatalog.freeTierFromXp(pass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(pass.passXp)
    if (countClaimablePassRewards(pass, freeTier, vipTier) > 0) {
      _toastEvents.tryEmit("Claim all remaining November Pass rewards first before restarting!")
      return
    }
    val nextPass =
      pass.copy(
        passXp = 0L,
        claimedFreeTiers = emptyList(),
        claimedVipTiers = emptyList(),
        passCompletions = pass.passCompletions + 1L,
      )
    val nextFreeTier = GameCatalog.freeTierFromXp(0L)
    val nextVipTier = GameCatalog.vipTierFromXp(0L)
    val quests = GameCatalog.evaluateHourlyPassQuests(nextPass)
    passDirty = false
    _uiState.update {
      it.copy(
        battlePassState = nextPass,
        currentPassTier = nextFreeTier,
        currentVipPassTier = nextVipTier,
        freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(0L),
        vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(0L),
        hourlyPassQuests = quests,
        claimablePassQuestsCount = quests.count { q -> !q.isClaimed && q.isCompleted },
        claimablePassCount = 0,
      )
    }
    viewModelScope.launch { localStorage.saveBattlePassState(nextPass) }
    _toastEvents.tryEmit(
      "November Pass restarted! Entering Cycle #${nextPass.passCompletions + 1L}!"
    )
  }

  fun claimHourlyPassQuest(questId: String) {
    val now = System.currentTimeMillis()
    val state = _uiState.value
    val (syncedPass, _) = ensureHourlyPassQuests(state.battlePassState, now)
    val quest = GameCatalog.evaluateHourlyPassQuests(syncedPass).find { it.template.id == questId } ?: return
    if (quest.isClaimed) return
    if (!quest.isCompleted) {
      _toastEvents.tryEmit("${quest.template.title}: ${quest.progress}/${quest.template.target}")
      return
    }

    val hasScholar = "asc_pass_scholar" in state.challengeState.unlockedAscensionIds
    val boostedXp =
      if (hasScholar) (quest.xpReward * 1.5).roundToLong() else quest.xpReward
    val nextPass =
      syncedPass.copy(
        passXp = syncedPass.passXp + boostedXp,
        claimedPassQuestIds = (syncedPass.claimedPassQuestIds + questId).distinct(),
      )
    val freeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
    val nextQuests = GameCatalog.evaluateHourlyPassQuests(nextPass)
    passDirty = false
    _uiState.update {
      it.copy(
        battlePassState = nextPass,
        currentPassTier = freeTier,
        currentVipPassTier = vipTier,
        freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(nextPass.passXp),
        vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(nextPass.passXp),
        hourlyPassQuests = nextQuests,
        hourlyQuestRefreshText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
        claimablePassQuestsCount = nextQuests.count { q -> !q.isClaimed && q.isCompleted },
        claimablePassCount = countClaimablePassRewards(nextPass, freeTier, vipTier),
      )
    }
    viewModelScope.launch { localStorage.saveBattlePassState(nextPass) }
    _toastEvents.tryEmit("Pass Quest Complete! +$boostedXp Pass XP")
  }

  private fun applyPassRewards(
    rewards: List<BattlePassRewardItem>,
    newClaimedFree: List<Int>,
    newClaimedVip: List<Int>,
  ) {
    val state = _uiState.value
    val current = state.profile
    val challenge = state.challengeState
    val isHardcore = challenge.isHardcoreMode

    var purrGain = 0L
    var shardGain = 0L
    var gemGain = 0L
    val patterns = current.unlockedPatterns.toMutableList()
    val accessories = current.unlockedAccessories.toMutableList()
    val upgrades = current.upgradeLevels.toMutableMap()
    val hcUpgrades = challenge.hardcoreUpgradeLevels.toMutableMap()

    rewards.forEach { reward ->
      when (reward.kind) {
        PassRewardKind.PURRS -> purrGain += reward.amount
        PassRewardKind.GATO_SHARD -> shardGain += reward.amount
        PassRewardKind.GEM -> gemGain += reward.amount
        PassRewardKind.UPGRADE_LEVEL -> {
          if (reward.itemId.isNotBlank() && reward.amount > 0L) {
            if (isHardcore) {
              hcUpgrades[reward.itemId] = (hcUpgrades[reward.itemId] ?: 0L) + reward.amount
            } else {
              upgrades[reward.itemId] = (upgrades[reward.itemId] ?: 0L) + reward.amount
            }
          }
        }
        PassRewardKind.ACCESSORY -> {
          if (reward.itemId.isNotBlank() && reward.itemId !in accessories) {
            accessories.add(reward.itemId)
            if (GameCatalog.accessories.any { it.id == reward.itemId && it.isEventExclusive }) {
              recordLimitedItemUnlock("ACCESSORY", reward.itemId)
            }
          }
        }
        PassRewardKind.SKIN -> {
          if (reward.itemId.isNotBlank() && reward.itemId !in patterns) {
            patterns.add(reward.itemId)
            if (GameCatalog.patterns.any { it.id == reward.itemId && it.isEventExclusive }) {
              recordLimitedItemUnlock("PATTERN", reward.itemId)
            }
          }
        }
      }
    }

    val nextPass =
      state.battlePassState.copy(
        claimedFreeTiers = newClaimedFree,
        claimedVipTiers = newClaimedVip,
      )
    val freeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
    val quests = GameCatalog.evaluateHourlyPassQuests(nextPass)
    passDirty = false
    _uiState.update {
      it.copy(
        battlePassState = nextPass,
        currentPassTier = freeTier,
        currentVipPassTier = vipTier,
        freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(nextPass.passXp),
        vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(nextPass.passXp),
        hourlyPassQuests = quests,
        claimablePassQuestsCount = quests.count { q -> !q.isClaimed && q.isCompleted },
        claimablePassCount = countClaimablePassRewards(nextPass, freeTier, vipTier),
      )
    }
    viewModelScope.launch { localStorage.saveBattlePassState(nextPass) }

    val nextChallenge =
      challenge.copy(
        hardcorePurrs = if (isHardcore) challenge.hardcorePurrs + purrGain else challenge.hardcorePurrs,
        hardcoreUpgradeLevels = hcUpgrades,
        gatoShards = challenge.gatoShards + shardGain,
        gems = challenge.gems + gemGain,
        totalGemsEarned = challenge.totalGemsEarned + gemGain,
      )
    if (isHardcore || shardGain > 0L || gemGain > 0L) {
      saveChallengeAndRefresh(nextChallenge)
    }

    val updatedProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else current.purrs + purrGain,
        totalPurrsEarned = current.totalPurrsEarned + purrGain,
        unlockedPatterns = patterns.distinct(),
        unlockedAccessories = accessories.distinct(),
        upgradeLevels = upgrades,
      )
    applyAndSaveImmediate(updatedProfile)
  }

  private fun grantPassXpImmediate(xpGain: Long, incrementHourUpgrade: Boolean = false) {
    if (xpGain <= 0L && !incrementHourUpgrade) return
    val now = System.currentTimeMillis()
    val hasScholar = "asc_pass_scholar" in _uiState.value.challengeState.unlockedAscensionIds
    val effectiveXp = if (hasScholar && xpGain > 0L) (xpGain * 1.5).roundToLong() else xpGain
    val (syncedPass, _) = ensureHourlyPassQuests(_uiState.value.battlePassState, now)
    val nextPass =
      syncedPass.copy(
        passXp = syncedPass.passXp + effectiveXp,
        hourUpgradesBought =
          syncedPass.hourUpgradesBought + (if (incrementHourUpgrade) 1L else 0L),
      )
    val freeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
    val vipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
    val quests = GameCatalog.evaluateHourlyPassQuests(nextPass)
    passDirty = false
    _uiState.update {
      it.copy(
        battlePassState = nextPass,
        currentPassTier = freeTier,
        currentVipPassTier = vipTier,
        freePassStepProgress = GameCatalog.freeXpProgressInCurrentTier(nextPass.passXp),
        vipPassStepProgress = GameCatalog.vipXpProgressInCurrentTier(nextPass.passXp),
        hourlyPassQuests = quests,
        claimablePassQuestsCount = quests.count { q -> !q.isClaimed && q.isCompleted },
        claimablePassCount = countClaimablePassRewards(nextPass, freeTier, vipTier),
      )
    }
    viewModelScope.launch { localStorage.saveBattlePassState(nextPass) }
  }

  private fun ensureHourlyPassQuests(
    pass: BattlePassState,
    nowMillis: Long,
  ): Pair<BattlePassState, Boolean> {
    val epochHour = GameCatalog.currentEpochHour(nowMillis)
    if (pass.questHourEpoch != epochHour || pass.activeQuestIds.isEmpty()) {
      val updated =
        pass.copy(
          questHourEpoch = epochHour,
          activeQuestIds = GameCatalog.selectHourlyPassQuestIds(epochHour),
          claimedPassQuestIds = emptyList(),
          hourPets = 0L,
          hourPurrsEarned = 0L,
          hourUpgradesBought = 0L,
          hourWeatherShifts = 0L,
          hourComboCrits = 0L,
        )
      return updated to true
    }
    return pass to false
  }

  private fun countClaimablePassRewards(
    pass: BattlePassState,
    freeTier: Int,
    vipTier: Int,
  ): Int {
    var count = 0
    for (tier in 1..freeTier.coerceAtMost(GameCatalog.NOVEMBER_PASS_MAX_TIER)) {
      if (tier !in pass.claimedFreeTiers) count++
    }
    if (pass.vipUnlocked) {
      for (tier in 1..vipTier.coerceAtMost(GameCatalog.NOVEMBER_PASS_MAX_TIER)) {
        if (tier !in pass.claimedVipTiers) count++
      }
    }
    return count
  }

  private fun formatGoldenCharge(equippedMillis: Long): String {
    val totalSec = (equippedMillis / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return "${mins}m ${secs}s / 30m"
  }

  fun equipBadge(badgeId: String) {
    val current = _uiState.value.profile
    if (badgeId !in current.unlockedBadgeIds && badgeId.isNotEmpty()) return
    val updated = current.copy(equippedBadgeId = badgeId)
    applyAndSaveImmediate(updated)
    val badgeName = GameCatalog.badges.find { it.id == badgeId }?.name ?: "None"
    _toastEvents.tryEmit("Equipped badge: $badgeName")
  }

  // Gem Vault (Rare Currency) Upgrades, Boosts & Cosmetics
  fun buyGemCharm() {
    val challenge = _uiState.value.challengeState
    val cost = GameCatalog.gemCharmCost(challenge.gemCharmLevel)
    if (challenge.gems < cost) {
      _toastEvents.tryEmit("Need ${cost - challenge.gems} more Gems for Gemstone Purr Charm!")
      return
    }
    val nextLevel = challenge.gemCharmLevel + 1L
    val nextChallenge =
      challenge.copy(
        gems = challenge.gems - cost,
        gemCharmLevel = nextLevel,
      )
    saveChallengeAndRefresh(nextChallenge)
    _toastEvents.tryEmit(
      "Gemstone Purr Charm upgraded to Lv.$nextLevel (${GameCatalog.gemCharmMultiplier(nextLevel)}x Purrs)!"
    )
  }

  fun activateGemFrenzy() {
    val challenge = _uiState.value.challengeState
    if (challenge.gemFrenzyRemainingMillis > 0L) {
      _toastEvents.tryEmit("Starlight Gem Frenzy is already active!")
      return
    }
    if (challenge.gems < GameCatalog.GEM_FRENZY_COST) {
      _toastEvents.tryEmit(
        "Need ${GameCatalog.GEM_FRENZY_COST - challenge.gems} more Gems for Starlight Gem Frenzy!"
      )
      return
    }
    val nextChallenge =
      challenge.copy(
        gems = challenge.gems - GameCatalog.GEM_FRENZY_COST,
        gemFrenzyRemainingMillis = GameCatalog.GEM_FRENZY_DURATION_MS,
      )
    saveChallengeAndRefresh(nextChallenge)
    _toastEvents.tryEmit("💎 Starlight Gem Frenzy! 3.0x All Purrs for 2 minutes!")
  }

  fun summonCosmicWeatherWithGems() {
    val state = _uiState.value
    val challenge = state.challengeState
    if (challenge.gems < GameCatalog.GEM_WEATHER_SUMMON_COST) {
      _toastEvents.tryEmit(
        "Need ${GameCatalog.GEM_WEATHER_SUMMON_COST - challenge.gems} more Gems to summon Cosmic Sky!"
      )
      return
    }
    val cosmicPool =
      listOf(
        WeatherConditionKind.AURORA_BOREALIS,
        WeatherConditionKind.METEOR_SHOWER,
        WeatherConditionKind.RAINBOW_SHOWER,
      ).filter { it != state.weather.condition }.ifEmpty { listOf(WeatherConditionKind.AURORA_BOREALIS) }
    val chosen = cosmicPool[Random.nextInt(cosmicPool.size)]
    val newWeather =
      LiveWeatherState(
        cityName = "Sanctuary Sky",
        temperatureC = Random.nextInt(chosen.tempRangeC.first, chosen.tempRangeC.last + 1).toDouble(),
        condition = chosen,
        nextShiftSeconds = WeatherRepository.WEATHER_INTERVAL_SECONDS,
        weatherShiftCount = state.weather.weatherShiftCount + 1L,
      )
    secondsUntilNextWeather = WeatherRepository.WEATHER_INTERVAL_SECONDS
    val now = System.currentTimeMillis()
    val (syncedFest, _, _) = ensureHourlyFestivalAndQuests(challenge, now)
    val nextChallenge =
      syncedFest.copy(
        gems = syncedFest.gems - GameCatalog.GEM_WEATHER_SUMMON_COST,
        festHourWeatherShifts = syncedFest.festHourWeatherShifts + 1L,
      )
    val (syncedPass, _) = ensureHourlyPassQuests(state.battlePassState, now)
    val nextPass =
      syncedPass.copy(
        passXp = syncedPass.passXp + 15L,
        hourWeatherShifts = syncedPass.hourWeatherShifts + 1L,
      )
    _uiState.update { it.copy(weather = newWeather, battlePassState = nextPass) }
    viewModelScope.launch { localStorage.saveBattlePassState(nextPass) }
    saveChallengeAndRefresh(nextChallenge)
    val updatedProfile =
      state.profile.copy(weatherCheckedCount = state.profile.weatherCheckedCount + 1L)
    applyAndSaveImmediate(updatedProfile)
    _toastEvents.tryEmit("Summoned ${chosen.label} sky (${chosen.buffTitle})!")
  }

  fun unlockPrismLens() {
    val challenge = _uiState.value.challengeState
    if (challenge.prismLensUnlocked) {
      _toastEvents.tryEmit("Prismatic Weather Lens is already unlocked!")
      return
    }
    if (challenge.gems < GameCatalog.GEM_PRISM_LENS_COST) {
      _toastEvents.tryEmit(
        "Need ${GameCatalog.GEM_PRISM_LENS_COST - challenge.gems} more Gems for Prismatic Lens!"
      )
      return
    }
    saveChallengeAndRefresh(
      challenge.copy(
        gems = challenge.gems - GameCatalog.GEM_PRISM_LENS_COST,
        prismLensUnlocked = true,
      )
    )
    _toastEvents.tryEmit("Unlocked Prismatic Weather Lens (+20% stronger all-weather buffs)!")
  }

  fun unlockGemPattern(patternId: String) {
    val def = GameCatalog.patterns.find { it.id == patternId && it.gemCost > 0L } ?: return
    val state = _uiState.value
    val profile = state.profile
    if (patternId in profile.unlockedPatterns) {
      applyAndSaveImmediate(profile.copy(selectedPatternId = patternId))
      _toastEvents.tryEmit("Equipped ${def.name} coat!")
      return
    }
    val challenge = state.challengeState
    if (challenge.gems < def.gemCost) {
      _toastEvents.tryEmit("Need ${def.gemCost - challenge.gems} more Gems for ${def.name}!")
      return
    }
    saveChallengeAndRefresh(challenge.copy(gems = challenge.gems - def.gemCost))
    applyAndSaveImmediate(
      profile.copy(
        unlockedPatterns = (profile.unlockedPatterns + patternId).distinct(),
        selectedPatternId = patternId,
      )
    )
    if (def.isEventExclusive) {
      recordLimitedItemUnlock("PATTERN", patternId)
    }
    _toastEvents.tryEmit("💎 Unlocked & equipped ${def.name}!")
  }

  fun unlockGemAccessory(accessoryId: String) {
    val def = GameCatalog.accessories.find { it.id == accessoryId && it.gemCost > 0L } ?: return
    val state = _uiState.value
    val profile = state.profile
    if (accessoryId in profile.unlockedAccessories) {
      applyAndSaveImmediate(profile.copy(selectedAccessoryId = accessoryId))
      _toastEvents.tryEmit("Equipped ${def.name}!")
      return
    }
    val challenge = state.challengeState
    if (challenge.gems < def.gemCost) {
      _toastEvents.tryEmit("Need ${def.gemCost - challenge.gems} more Gems for ${def.name}!")
      return
    }
    saveChallengeAndRefresh(challenge.copy(gems = challenge.gems - def.gemCost))
    applyAndSaveImmediate(
      profile.copy(
        unlockedAccessories = (profile.unlockedAccessories + accessoryId).distinct(),
        selectedAccessoryId = accessoryId,
      )
    )
    if (def.isEventExclusive) {
      recordLimitedItemUnlock("ACCESSORY", accessoryId)
    }
    _toastEvents.tryEmit("💎 Unlocked & equipped ${def.name}!")
  }

  fun unlockGemColorTheme(theme: AppColorTheme) {
    if (theme.gemCost <= 0L) return
    val state = _uiState.value
    if (theme.id in state.settings.unlockedColorThemes) {
      updateSettings { it.copy(colorTheme = theme) }
      _toastEvents.tryEmit("Applied ${theme.displayName} theme!")
      return
    }
    val challenge = state.challengeState
    if (challenge.gems < theme.gemCost) {
      _toastEvents.tryEmit(
        "Need ${theme.gemCost - challenge.gems} more Gems for ${theme.displayName}!"
      )
      return
    }
    saveChallengeAndRefresh(challenge.copy(gems = challenge.gems - theme.gemCost))
    updateSettings { s ->
      s.copy(
        unlockedColorThemes = (s.unlockedColorThemes + theme.id).distinct(),
        colorTheme = theme,
      )
    }
    if (!theme.isFree) {
      recordLimitedItemUnlock("THEME", theme.id)
    }
    _toastEvents.tryEmit("💎 Unlocked & applied ${theme.displayName} theme!")
  }

  fun forgeGatoShardFromGems() {
    val challenge = _uiState.value.challengeState
    if (challenge.gems < GameCatalog.GEM_SHARD_FORGE_COST) {
      _toastEvents.tryEmit(
        "Need ${GameCatalog.GEM_SHARD_FORGE_COST - challenge.gems} more Gems to forge a Gato Shard!"
      )
      return
    }
    saveChallengeAndRefresh(
      challenge.copy(
        gems = challenge.gems - GameCatalog.GEM_SHARD_FORGE_COST,
        gatoShards = challenge.gatoShards + 1L,
      )
    )
    _toastEvents.tryEmit("💎 Forged +1 Gato Shard from ${GameCatalog.GEM_SHARD_FORGE_COST} Gems!")
  }

  // Thanksgiving Event Part Two: Hourly Festival (67% Regular / 33% Gravy Rush), Turkeys, Golden Gobblers, Turkey Gato & Quests
  private fun ensureHourlyFestivalAndQuests(
    challenge: ChallengeProgressState,
    nowMillis: Long,
  ): Triple<ChallengeProgressState, Boolean, Boolean> {
    val epochHour = GameCatalog.currentEpochHour(nowMillis)
    var next = challenge
    var changed = false
    var hourRerolled = false
    if (next.festivalHourEpoch != epochHour) {
      val rollMega = GameCatalog.rollHourlyFestivalIsMega()
      next = next.copy(festivalHourEpoch = epochHour, festivalIsMega = rollMega)
      changed = true
      hourRerolled = true
    }
    if (next.festivalQuestHourEpoch != epochHour) {
      next =
        next.copy(
          festivalQuestHourEpoch = epochHour,
          claimedFestivalQuestIds = emptyList(),
          festHourPets = 0L,
          festHourTurkeysTapped = 0L,
          festHourUpgradesBought = 0L,
          festHourWeatherShifts = 0L,
        )
      changed = true
    }
    return Triple(next, changed, hourRerolled)
  }

  fun collectSpawnedTurkey(turkeyId: Long) {
    val state = _uiState.value
    if (state.isLoading) return
    if (enforceModerationBlock(state)) return
    val targetTurkey = state.spawnedTurkeys.find { it.id == turkeyId } ?: return

    val now = System.currentTimeMillis()
    val (syncedFest, _, _) = ensureHourlyFestivalAndQuests(state.challengeState, now)
    val rewardAmount =
      if (targetTurkey.isGoldenGobbler) GameCatalog.GOLDEN_GOBBLER_REWARD else 1L
    val passXp = if (targetTurkey.isGoldenGobbler) 12L else 5L
    val nextChallenge =
      syncedFest.copy(
        turkeys = syncedFest.turkeys + rewardAmount,
        totalTurkeysEarned = syncedFest.totalTurkeysEarned + rewardAmount,
        festHourTurkeysTapped = syncedFest.festHourTurkeysTapped + 1L,
      )
    _uiState.update { s ->
      s.copy(spawnedTurkeys = s.spawnedTurkeys.filterNot { it.id == turkeyId })
    }
    soundManager.playCatTapSound(isComboCrit = true)
    saveChallengeAndRefresh(nextChallenge)
    grantPassXpImmediate(passXp)
    _toastEvents.tryEmit(
      if (targetTurkey.isGoldenGobbler) {
        "✨🦃 Caught a GOLDEN GOBBLER! (+$rewardAmount Turkeys • Total: ${nextChallenge.turkeys})"
      } else {
        "🦃 Caught a Turkey! (+1 Turkey • Total: ${nextChallenge.turkeys})"
      }
    )
  }

  fun talkToTurkeyGato() {
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val challenge = state.challengeState
    val nextTalkCount = (challenge.turkeyGatoTalkCount + 1).coerceAtMost(GameCatalog.TURKEY_GATO_MAP_TALKS + 10)
    val unlockedNow =
      !challenge.part2MapUnlocked && nextTalkCount >= GameCatalog.TURKEY_GATO_MAP_TALKS
    val nextChallenge =
      challenge.copy(
        turkeyGatoTalkCount = nextTalkCount,
        part2MapUnlocked = challenge.part2MapUnlocked || unlockedNow,
      )
    saveChallengeAndRefresh(nextChallenge)
    if (unlockedNow) {
      _toastEvents.tryEmit("🗺️ Turkey Gato gifted you the Part 2 Feast Map! View it anytime!")
    }
  }

  fun unlockTurkeyPattern(patternId: String) {
    val def = GameCatalog.patterns.find { it.id == patternId && it.turkeyCost > 0L } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val profile = state.profile
    if (patternId in profile.unlockedPatterns) {
      applyAndSaveImmediate(profile.copy(selectedPatternId = patternId))
      _toastEvents.tryEmit("Equipped ${def.name} coat!")
      return
    }
    val challenge = state.challengeState
    if (challenge.turkeys < def.turkeyCost) {
      _toastEvents.tryEmit("Need ${def.turkeyCost - challenge.turkeys} more 🦃 Turkeys for ${def.name}!")
      return
    }
    saveChallengeAndRefresh(challenge.copy(turkeys = challenge.turkeys - def.turkeyCost))
    applyAndSaveImmediate(
      profile.copy(
        unlockedPatterns = (profile.unlockedPatterns + patternId).distinct(),
        selectedPatternId = patternId,
      )
    )
    recordLimitedItemUnlock("PATTERN", patternId)
    _toastEvents.tryEmit("🦃 Unlocked & equipped ${def.name}!")
  }

  fun unlockTurkeyAccessory(accessoryId: String) {
    val def = GameCatalog.accessories.find { it.id == accessoryId && it.turkeyCost > 0L } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val profile = state.profile
    if (accessoryId in profile.unlockedAccessories) {
      applyAndSaveImmediate(profile.copy(selectedAccessoryId = accessoryId))
      _toastEvents.tryEmit("Equipped ${def.name}!")
      return
    }
    val challenge = state.challengeState
    if (challenge.turkeys < def.turkeyCost) {
      _toastEvents.tryEmit("Need ${def.turkeyCost - challenge.turkeys} more 🦃 Turkeys for ${def.name}!")
      return
    }
    saveChallengeAndRefresh(challenge.copy(turkeys = challenge.turkeys - def.turkeyCost))
    applyAndSaveImmediate(
      profile.copy(
        unlockedAccessories = (profile.unlockedAccessories + accessoryId).distinct(),
        selectedAccessoryId = accessoryId,
      )
    )
    recordLimitedItemUnlock("ACCESSORY", accessoryId)
    _toastEvents.tryEmit("🦃 Unlocked & equipped ${def.name}!")
  }

  fun unlockTurkeyTheme(theme: AppColorTheme) {
    if (theme.turkeyCost <= 0L) return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    if (theme.id in state.settings.unlockedColorThemes) {
      updateSettings { it.copy(colorTheme = theme) }
      _toastEvents.tryEmit("Applied ${theme.displayName} theme!")
      return
    }
    val challenge = state.challengeState
    if (challenge.turkeys < theme.turkeyCost) {
      _toastEvents.tryEmit(
        "Need ${theme.turkeyCost - challenge.turkeys} more 🦃 Turkeys for ${theme.displayName}!"
      )
      return
    }
    saveChallengeAndRefresh(challenge.copy(turkeys = challenge.turkeys - theme.turkeyCost))
    updateSettings { s ->
      s.copy(
        unlockedColorThemes = (s.unlockedColorThemes + theme.id).distinct(),
        colorTheme = theme,
      )
    }
    recordLimitedItemUnlock("THEME", theme.id)
    _toastEvents.tryEmit("🦃 Unlocked & applied ${theme.displayName} theme!")
  }

  fun unlockTurkeyBackground(backgroundId: String) {
    val def = GameCatalog.backgrounds.find { it.id == backgroundId && it.turkeyCost > 0L } ?: return
    val state = _uiState.value
    if (enforceModerationBlock(state)) return
    val current = state.profile
    if (backgroundId in current.unlockedBackgrounds) {
      applyAndSaveImmediate(current.copy(selectedBackgroundId = backgroundId))
      soundManager.playUiConfirm()
      _toastEvents.tryEmit("Equipped ${def.name} room!")
      return
    }
    val challenge = state.challengeState
    if (challenge.turkeys < def.turkeyCost) {
      _toastEvents.tryEmit(
        "Need ${def.turkeyCost - challenge.turkeys} more 🦃 Turkeys for ${def.name}!"
      )
      return
    }
    saveChallengeAndRefresh(challenge.copy(turkeys = challenge.turkeys - def.turkeyCost))
    val updated =
      current.copy(
        unlockedBackgrounds = (current.unlockedBackgrounds + backgroundId).distinct(),
        selectedBackgroundId = backgroundId,
      )
    applyAndSaveImmediate(updated)
    recordLimitedItemUnlock("BACKGROUND", backgroundId)
    soundManager.playUiConfirm()
    _toastEvents.tryEmit("🦃 Unlocked & equipped ${def.name} room!")
  }

  fun claimFestivalQuest(questId: String) {
    val now = System.currentTimeMillis()
    val state = _uiState.value
    val (syncedFest, _, _) = ensureHourlyFestivalAndQuests(state.challengeState, now)
    val quest = GameCatalog.evaluateHourlyFestivalQuests(syncedFest).find { it.template.id == questId } ?: return
    if (quest.isClaimed) return
    if (!quest.isCompleted) {
      _toastEvents.tryEmit("${quest.template.title}: ${quest.progress}/${quest.template.target}")
      return
    }
    val reward = quest.template.turkeyReward
    val nextChallenge =
      syncedFest.copy(
        turkeys = syncedFest.turkeys + reward,
        totalTurkeysEarned = syncedFest.totalTurkeysEarned + reward,
        claimedFestivalQuestIds = (syncedFest.claimedFestivalQuestIds + questId).distinct(),
      )
    saveChallengeAndRefresh(nextChallenge)
    grantPassXpImmediate(40L)
    _toastEvents.tryEmit("🦃 Festival Quest Complete! +$reward Turkeys earned!")
  }

  // Alternative Sign-In Options & Backup Save Code Export/Import
  fun saveLocalCaretakerAccount(displayName: String, pin: String) {
    val cleanedName = displayName.trim().take(20).ifBlank { "Caretaker" }
    val cleanedPin = pin.filter { it.isDigit() }.take(4)
    val existingSettings = _uiState.value.settings
    val code =
      existingSettings.localAccountCode.ifBlank { localStorage.generateSanctuaryCode() }
    val resolvedPin =
      if (cleanedPin.length == 4) cleanedPin else existingSettings.localAccountPin
    updateSettings { s ->
      s.copy(
        accountMode = "local",
        localAccountCode = code,
        localAccountPin = resolvedPin,
      )
    }
    val updatedProfile = _uiState.value.profile.copy(displayName = cleanedName)
    applyAndSaveImmediate(updatedProfile)
    _toastEvents.tryEmit("Local Caretaker saved ($code)!")
  }

  fun switchToGuestCaretaker() {
    updateSettings { it.copy(accountMode = "guest") }
    _toastEvents.tryEmit("Switched to Instant Guest Play (saved locally).")
  }

  fun exportSanctuarySaveCode(): String {
    val s = _uiState.value
    return localStorage.exportSaveCode(
      profile = s.profile,
      challenge = s.challengeState,
      unlockedThemes = s.settings.unlockedColorThemes,
    )
  }

  fun importSanctuarySaveCode(rawCode: String): Boolean {
    val snapshot = localStorage.parseSaveCode(rawCode) ?: return false
    val mergedThemes =
      (AppColorTheme.freeThemes.map { it.id } + snapshot.unlockedThemes).distinct()
    updateSettings { s ->
      s.copy(
        accountMode = if (currentUserId != null) s.accountMode else "local",
        localAccountCode =
          s.localAccountCode.ifBlank { localStorage.generateSanctuaryCode() },
        unlockedColorThemes = mergedThemes,
      )
    }
    saveChallengeAndRefresh(snapshot.challenge)
    val preservedUid = _uiState.value.profile.userId
    applyAndSaveImmediate(snapshot.profile.copy(userId = preservedUid))
    _toastEvents.tryEmit("Restored Sanctuary for ${snapshot.profile.displayName}!")
    return true
  }

  /**
   * Interacts with the Dog Vault Keeper inside the Secret Code Vault.
   * Returns true if excessive clicks triggered the 3-minute kickout + Hacker unlock.
   */
  fun talkToVaultKeeper(): Boolean {
    val now = System.currentTimeMillis()
    val state = _uiState.value
    val challenge = state.challengeState
    if (challenge.dogComputerSearched) {
      _uiState.update { it.copy(showWindowGatoExitConfrontation = false) }
      _toastEvents.tryEmit("The Dog Vault Keeper stays silent and leaves you alone.")
      return false
    }
    val remaining = (challenge.vaultKickoutUntilMillis - now).coerceAtLeast(0L)
    if (remaining > 0L) {
      val formatted = formatDuration(remaining)
      _uiState.update {
        it.copy(vaultCooldownRemainingMillis = remaining, vaultCooldownText = formatted)
      }
      _toastEvents.tryEmit("Dog Vault Keeper kicked you out! Wait $formatted to enter again.")
      return false
    }

    val nextTaps = state.vaultKeeperTapCount + 1
    if (nextTaps >= GameCatalog.VAULT_KEEPER_KICKOUT_TAPS) {
      val kickoutUntil = now + GameCatalog.VAULT_KICKOUT_DURATION_MS
      val formatted = formatDuration(GameCatalog.VAULT_KICKOUT_DURATION_MS)
      val nextChallenge =
        challenge.copy(
          hackerVaultUnlocked = true,
          vaultKickoutUntilMillis = kickoutUntil,
        )
      saveChallengeAndRefresh(nextChallenge)

      val alreadyHadTheme =
        AppColorTheme.HACKER_PROTOCOL.id in state.settings.unlockedColorThemes
      updateSettings { s ->
        s.copy(
          unlockedColorThemes = (s.unlockedColorThemes + AppColorTheme.HACKER_PROTOCOL.id).distinct(),
          colorTheme = AppColorTheme.HACKER_PROTOCOL,
        )
      }
      if (!alreadyHadTheme) {
        recordLimitedItemUnlock("THEME", AppColorTheme.HACKER_PROTOCOL.id)
      }

      val profile = _uiState.value.profile
      val badgeId = "vault_hacker"
      if (badgeId !in profile.unlockedBadgeIds) {
        val updatedBadges = profile.unlockedBadgeIds + badgeId
        val nextEquipped = profile.equippedBadgeId.ifBlank { badgeId }
        applyAndSaveImmediate(
          profile.copy(unlockedBadgeIds = updatedBadges, equippedBadgeId = nextEquipped)
        )
      }

      _uiState.update {
        it.copy(
          vaultKeeperTapCount = 0,
          vaultCooldownRemainingMillis = GameCatalog.VAULT_KICKOUT_DURATION_MS,
          vaultCooldownText = formatted,
        )
      }
      _toastEvents.tryEmit(
        if (alreadyHadTheme) {
          "Kicked out for 3m by the Dog Vault Keeper!"
        } else {
          "KICKED OUT FOR 3M! Unlocked Hacker Protocol theme & Vault Intruder achievement!"
        }
      )
      return true
    } else {
      _uiState.update { it.copy(vaultKeeperTapCount = nextTaps) }
      return false
    }
  }

  fun unlockCatComputer(attempt: String): Boolean {
    if (GameCatalog.isValidCatComputerPassword(attempt)) {
      val state = _uiState.value
      if (!state.challengeState.catComputerUnlocked) {
        saveChallengeAndRefresh(state.challengeState.copy(catComputerUnlocked = true))
      }
      soundManager.playWindowGatoStartupChime()
      _uiState.update {
        it.copy(showWindowGatoDesktop = true, showWindowGatoExitConfrontation = false)
      }
      _toastEvents.tryEmit("Access granted — Booting Window Gato OS...")
      return true
    } else {
      soundManager.playWindowGatoErrorBeep()
      _toastEvents.tryEmit("Incorrect password! Read the riddle & hint below.")
      return false
    }
  }

  fun openWindowGatoDesktop() {
    val state = _uiState.value
    if (!state.challengeState.catComputerUnlocked) {
      soundManager.playWindowGatoErrorBeep()
      _toastEvents.tryEmit("Enter the Cat Computer password first!")
      return
    }
    soundManager.playWindowGatoStartupChime()
    _uiState.update {
      it.copy(showWindowGatoDesktop = true, showWindowGatoExitConfrontation = false)
    }
  }

  fun closeWindowGatoDesktop() {
    val state = _uiState.value
    if (!state.challengeState.dogComputerSearched) {
      saveChallengeAndRefresh(state.challengeState.copy(dogComputerSearched = true))
    }
    _uiState.update {
      it.copy(
        showWindowGatoDesktop = false,
        showWindowGatoExitConfrontation = true,
        vaultKeeperTapCount = 0,
      )
    }
  }

  fun dismissWindowGatoConfrontation() {
    _uiState.update { it.copy(showWindowGatoExitConfrontation = false) }
  }

  fun playCorruptedAudioSample(clipId: String) {
    soundManager.playCorruptedAudioSample(clipId)
  }

  fun playWindowGatoErrorSound() {
    soundManager.playWindowGatoErrorBeep()
  }

  fun playCatScoldSound() {
    soundManager.playSpecificSfx(CatSoundEffect.FRENZY_EXCITED_MEOW)
  }

  // Settings & Built-In Music Player Controls
  fun setThemeMode(mode: ThemeMode) {
    updateSettings { it.copy(themeMode = mode) }
  }

  fun setNavigationFormat(format: NavigationLayoutFormat) {
    updateSettings { it.copy(navigationFormat = format) }
    _toastEvents.tryEmit("Navigation layout set to ${format.label}")
  }

  fun setSfxEnabled(enabled: Boolean) {
    updateSettings { it.copy(sfxEnabled = enabled) }
    if (enabled) {
      soundManager.playSpecificSfx(CatSoundEffect.GENTLE_MEW)
    }
  }

  fun setSfxVolume(volume: Float) {
    updateSettings { it.copy(sfxVolume = volume.coerceIn(0f, 1f)) }
  }

  fun testCatSfx() {
    soundManager.playCatTapSound(isComboCrit = false)
  }

  fun toggleMusicPlaying() {
    updateSettings { it.copy(isMusicPlaying = !it.isMusicPlaying) }
  }

  fun selectMusicTrackById(trackId: String) {
    updateSettings { it.copy(selectedTrackId = trackId, isMusicPlaying = true) }
  }

  fun skipMusicTrack(step: Int) {
    val settings = _uiState.value.settings
    val allIds = MusicTrack.entries.map { it.id } + settings.customTracks.map { it.id }
    if (allIds.isEmpty()) return
    val currentIndex = allIds.indexOf(settings.selectedTrackId).coerceAtLeast(0)
    val nextIndex =
      if (settings.shuffleEnabled && allIds.size > 1) {
        val candidates = allIds.indices.filter { it != currentIndex }
        candidates[Random.nextInt(candidates.size)]
      } else {
        (currentIndex + step + allIds.size) % allIds.size
      }
    updateSettings { it.copy(selectedTrackId = allIds[nextIndex], isMusicPlaying = true) }
  }

  private fun handleTrackNaturallyEnded() {
    val settings = _uiState.value.settings
    when (settings.repeatMode) {
      RepeatMode.OFF -> updateSettings { it.copy(isMusicPlaying = false) }
      RepeatMode.ONE -> Unit
      RepeatMode.ALL -> skipMusicTrack(1)
    }
  }

  fun seekMusicTo(positionMs: Long) {
    soundManager.seekTo(positionMs)
  }

  fun cycleMusicRepeatMode() {
    val nextMode = _uiState.value.settings.repeatMode.next()
    updateSettings { it.copy(repeatMode = nextMode) }
    _toastEvents.tryEmit("Repeat: ${nextMode.label}")
  }

  fun toggleMusicShuffle() {
    val nextShuffle = !_uiState.value.settings.shuffleEnabled
    updateSettings { it.copy(shuffleEnabled = nextShuffle) }
    _toastEvents.tryEmit(if (nextShuffle) "Shuffle Enabled" else "Shuffle Disabled")
  }

  fun setMusicPlaybackSpeed(speed: Float) {
    val clamped = speed.coerceIn(0.75f, 1.5f)
    updateSettings { it.copy(playbackSpeed = clamped) }
    _toastEvents.tryEmit("Playback speed set to ${clamped}x")
  }

  fun setMusicSleepTimer(minutes: Int) {
    soundManager.setSleepTimer(minutes)
    _toastEvents.tryEmit(
      if (minutes <= 0) "Sleep timer cleared." else "Music sleep timer set for ${minutes}m."
    )
  }

  fun setMusicVolume(volume: Float) {
    updateSettings { it.copy(musicVolume = volume.coerceIn(0f, 1f)) }
  }

  fun importCustomMusicFile(context: Context, uri: Uri) {
    val appContext = context.applicationContext
    viewModelScope.launch(Dispatchers.IO) {
      val displayName =
        runCatching {
          appContext.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
              if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx) else null
              } else {
                null
              }
            }
        }.getOrNull()
      val cleanTitle =
        (displayName ?: "Custom Track")
          .substringBeforeLast('.')
          .trim()
          .ifBlank { "Custom Track" }
          .take(32)

      val musicDir = File(appContext.filesDir, "custom_music").apply { mkdirs() }
      val destFile = File(musicDir, "track_${UUID.randomUUID()}.audio")

      val copied =
        runCatching {
          appContext.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
          }
          destFile.exists() && destFile.length() > 0L
        }.getOrDefault(false)

      if (!copied) {
        destFile.delete()
        _toastEvents.tryEmit("Could not read selected audio file.")
        return@launch
      }

      val playable =
        runCatching {
          val mp = MediaPlayer()
          mp.setDataSource(destFile.absolutePath)
          mp.prepare()
          mp.release()
          true
        }.getOrDefault(false)

      if (!playable) {
        destFile.delete()
        _toastEvents.tryEmit("Unsupported audio format. Please choose MP3, WAV, M4A, or OGG.")
        return@launch
      }

      val newTrack =
        CustomMusicTrack(
          id = "custom_${UUID.randomUUID().toString().take(8)}",
          title = cleanTitle,
          filePath = destFile.absolutePath,
        )
      updateSettings { s ->
        s.copy(
          customTracks = s.customTracks + newTrack,
          selectedTrackId = newTrack.id,
          isMusicPlaying = true,
        )
      }
      _toastEvents.tryEmit("Uploaded & playing \"$cleanTitle\"!")
    }
  }

  fun removeCustomMusicTrack(trackId: String) {
    val existing = _uiState.value.settings.customTracks.find { it.id == trackId } ?: return
    viewModelScope.launch(Dispatchers.IO) {
      runCatching { File(existing.filePath).delete() }
    }
    updateSettings { s ->
      val remaining = s.customTracks.filterNot { it.id == trackId }
      val nextSelected =
        if (s.selectedTrackId == trackId) MusicTrack.SUNLIT_WHISKERS.id else s.selectedTrackId
      s.copy(customTracks = remaining, selectedTrackId = nextSelected)
    }
    _toastEvents.tryEmit("Removed \"${existing.title}\" from player.")
  }

  fun setAppForeground(inForeground: Boolean) {
    isAppInForeground = inForeground
    soundManager.setForegroundState(inForeground)
    val now = System.currentTimeMillis()
    if (!inForeground) {
      backgroundPauseTimestampMillis = now
      if (hasInitializedProfile) {
        val nextStats =
          _uiState.value.playerStats.copy(
            lastActiveTimestampMillis = now,
            totalPlaySessions = _uiState.value.playerStats.totalPlaySessions,
          )
        _uiState.update { it.copy(playerStats = nextStats) }
        val profileSnapshot = _uiState.value.profile
        val challengeSnapshot = _uiState.value.challengeState
        val passSnapshot = _uiState.value.battlePassState
        val weatherSnapshot = _uiState.value.weather
        viewModelScope.launch {
          runCatching { localStorage.savePlayerStats(nextStats) }
          runCatching {
            localStorage.saveWidgetWeatherSnapshot(
              weatherSnapshot.condition.label,
              weatherSnapshot.temperatureC.toInt(),
            )
          }
          if (passDirty) {
            passDirty = false
            runCatching { localStorage.saveBattlePassState(passSnapshot) }
          }
          if (isDirty) persistCurrentProfile()
          if (challengeDirty) {
            challengeDirty = false
            runCatching { localStorage.saveChallengeProgress(challengeSnapshot) }
          }
          if (!isDirty) {
            runCatching { localStorage.saveProfile(profileSnapshot) }
          }
          onSyncWidgets()
        }
      }
    } else {
      if (hasInitializedProfile && backgroundPauseTimestampMillis > 0L) {
        val pauseAt = backgroundPauseTimestampMillis
        backgroundPauseTimestampMillis = 0L
        val nextStats =
          _uiState.value.playerStats.copy(
            totalPlaySessions = _uiState.value.playerStats.totalPlaySessions + 1L
          )
        _uiState.update { it.copy(playerStats = nextStats) }
        viewModelScope.launch {
          runCatching {
            val diskProfile = localStorage.loadProfile()
            val diskChallenge = localStorage.loadChallengeProgress()
            val diskPass = localStorage.loadBattlePassState()
            val memPets = _uiState.value.profile.totalPets
            if (diskProfile.totalPets > memPets) {
              val mergedProfile =
                _uiState.value.profile.copy(
                  purrs = maxOf(_uiState.value.profile.purrs, diskProfile.purrs),
                  totalPurrsEarned =
                    maxOf(_uiState.value.profile.totalPurrsEarned, diskProfile.totalPurrsEarned),
                  totalPets = diskProfile.totalPets,
                  eventPurrsEarned =
                    maxOf(_uiState.value.profile.eventPurrsEarned, diskProfile.eventPurrsEarned),
                )
              val mergedChallenge =
                _uiState.value.challengeState.copy(
                  hardcorePurrs =
                    maxOf(_uiState.value.challengeState.hardcorePurrs, diskChallenge.hardcorePurrs)
                )
              val mergedPass =
                _uiState.value.battlePassState.let { memPass ->
                  if (diskPass.passXp > memPass.passXp) {
                    memPass.copy(
                      passXp = diskPass.passXp,
                      hourPets = maxOf(memPass.hourPets, diskPass.hourPets),
                      hourPurrsEarned = maxOf(memPass.hourPurrsEarned, diskPass.hourPurrsEarned),
                    )
                  } else {
                    memPass
                  }
                }
              _uiState.update { s ->
                s.copy(
                  profile = mergedProfile,
                  challengeState = mergedChallenge,
                  battlePassState = mergedPass,
                )
              }
            }
          }
          processOfflineReturn(pauseAt, now)
          checkWeeklyAutoRollover(now)
          onSyncWidgets()
        }
      }
    }
  }

  private fun updateSettings(transform: (AppSettingsState) -> AppSettingsState) {
    val updated = transform(_uiState.value.settings)
    _uiState.update { it.copy(settings = updated) }
    applySettingsToAudio(updated)
    viewModelScope.launch { localStorage.saveSettings(updated) }
  }

  private fun applySettingsToAudio(settings: AppSettingsState) {
    soundManager.configureAudio(
      sfxEnabled = settings.sfxEnabled,
      sfxVolume = settings.sfxVolume,
      musicEnabled = settings.isMusicPlaying,
      musicVolume = settings.musicVolume,
      trackId = settings.selectedTrackId,
      customTracks = settings.customTracks,
      repeatMode = settings.repeatMode,
      shuffleEnabled = settings.shuffleEnabled,
      playbackSpeed = settings.playbackSpeed,
    )
  }

  private fun applyAndSaveImmediate(updatedProfile: UserProfile) {
    val currentState = _uiState.value
    val shouldTriggerTrillion =
      updatedProfile.totalPurrsEarned >= 1_000_000_000_000L &&
        !currentState.challengeState.trillionCutsceneSeen &&
        !currentState.showTrillionCutscene
    if (shouldTriggerTrillion) {
      _uiState.update { it.copy(showTrillionCutscene = true) }
    }
    applyProfile(updatedProfile, isLoaded = true)
    isDirty = true
    viewModelScope.launch { persistCurrentProfile() }
  }

  private fun applyProfile(profile: UserProfile, isLoaded: Boolean) {
    val now = System.currentTimeMillis()
    val (event, remaining) = GameCatalog.currentPeriodicEvent(now)
    val epochDay = currentUtcEpochDay(now)
    val currentWeek = GameCatalog.currentEpochWeek(now)
    _uiState.update { state ->
      val weather = state.weather
      val localMinute = WeatherRepository.currentLocalMinuteOfDay(now)
      val moonlight =
        GameCatalog.evaluateMoonlightWindow(localMinute, 360, isBloodmoonTonight)
      val challenge = state.challengeState
      val festTier = GameCatalog.festivalTierFor(challenge.festivalIsMega)
      val nextStreakPreview = computeNextStreakNumber(profile, epochDay)
      val goldenActive = state.battlePassState.goldenRainRemainingMillis > 0L
      state.copy(
        isLoading = !isLoaded,
        errorMessage = null,
        profile = profile,
        weather = weather,
        activeEvent = event,
        festivalTier = festTier,
        festivalRerollCountdownText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
        isTurkeyRushActive = GameCatalog.isTurkeySpawnWindowActive(now),
        turkeyRushCountdownText = GameCatalog.turkeyRushStatusText(now),
        hourlyFestivalQuests = GameCatalog.evaluateHourlyFestivalQuests(challenge),
        eventCountdownText = formatDuration(remaining),
        weeklyResetCountdownText = formatDuration(GameCatalog.millisUntilNextWeeklyReset(now)),
        moonlightState = moonlight,
        goldenRainActive = goldenActive,
        goldenRainCountdownText = formatDuration(state.battlePassState.goldenRainRemainingMillis),
        goldenTouchProgressText = formatGoldenCharge(state.battlePassState.goldenTouchEquippedMillis),
        gemFrenzyActive = challenge.gemFrenzyRemainingMillis > 0L,
        gemFrenzyCountdownText = formatDuration(challenge.gemFrenzyRemainingMillis),
        gemCharmMultiplier = GameCatalog.gemCharmMultiplier(challenge.gemCharmLevel),
        nextGemCharmCost = GameCatalog.gemCharmCost(challenge.gemCharmLevel),
        vipRequirementUpgradeCount =
          GameCatalog.totalUpgradesForPassVip(profile, challenge, state.playerStats),
        effectiveClickPower =
          computeEffectiveClick(
            profile,
            weather,
            event,
            moonlight.phase,
            state.comboMultiplier,
            challenge,
            goldenActive,
            festTier,
          ),
        effectivePps =
          computeEffectivePps(
            profile,
            weather,
            event,
            moonlight.phase,
            challenge,
            goldenActive,
            festTier,
          ),
        canClaimDailyReward = profile.lastDailyClaimEpochDay < epochDay,
        nextDailyDayIndex = computeNextDailyDay(profile, epochDay),
        nextDailyPayout = GameCatalog.computeEndlessDailyPayout(nextStreakPreview),
        canClaimWeeklyReward = state.playerStats.lastWeeklyRewardWeekId < currentWeek,
        nextRebirthCost = GameCatalog.rebirthCost(challenge.rebirthCount, challenge.isHardcoreMode),
        rebirthMultiplier =
          GameCatalog.rebirthMultiplier(
            challenge.rebirthBonusSteps,
            challenge.unlockedAscensionIds,
          ),
        hardcoreEligible =
          challenge.hardcoreUnlocked ||
            GameCatalog.meetsHardcoreRequirements(profile, challenge.rebirthCount),
      )
    }
  }

  private fun buildPublicStatsSnapshot(
    state: PurrGameUiState = _uiState.value,
    profile: UserProfile = state.profile,
  ): LeaderboardPublicStats {
    return LeaderboardPublicStats(
      rebirths = state.challengeState.rebirthCount,
      ascensions = state.challengeState.ascensionCount,
      dailyStreak = profile.dailyStreak,
      highestComboX100 =
        (state.playerStats.highestComboMultiplier * 100.0).roundToLong().coerceAtLeast(100L),
    )
  }

  private suspend fun persistCurrentProfile() {
    if (!hasInitializedProfile) return
    val stateSnapshot = _uiState.value
    val snapshot = stateSnapshot.profile
    val publicStats = buildPublicStatsSnapshot(stateSnapshot, snapshot)
    isDirty = false
    localStorage.saveProfile(snapshot)
    onSyncWidgets()
    val repo = repository
    if (repo != null && currentUserId != null) {
      isCloudSaveInFlight = true
      try {
        repo.saveProfileAndLeaderboard(snapshot, publicStats)
      } catch (e: CancellationException) {
        isDirty = true
        throw e
      } catch (e: Exception) {
        Log.w(TAG, "Cloud save deferred due to error: ${e.message}")
        isDirty = true
      } finally {
        isCloudSaveInFlight = false
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    soundManager.release()
    val now = System.currentTimeMillis()
    val stateSnapshot = _uiState.value
    val snapshot = stateSnapshot.profile
    val challengeSnapshot = stateSnapshot.challengeState
    val statsSnapshot = stateSnapshot.playerStats.copy(lastActiveTimestampMillis = now)
    val publicStats =
      LeaderboardPublicStats(
        rebirths = challengeSnapshot.rebirthCount,
        ascensions = challengeSnapshot.ascensionCount,
        dailyStreak = snapshot.dailyStreak,
        highestComboX100 =
          (statsSnapshot.highestComboMultiplier * 100.0).roundToLong().coerceAtLeast(100L),
      )
    val passSnapshot = stateSnapshot.battlePassState
    val saveProfileNow = isDirty && hasInitializedProfile
    val saveChallengeNow = challengeDirty && hasInitializedProfile
    val savePassNow = passDirty && hasInitializedProfile
    if (hasInitializedProfile) {
      val repo = repository
      val uid = currentUserId
      CoroutineScope(Dispatchers.IO).launch {
        runCatching { localStorage.savePlayerStats(statsSnapshot) }
        if (savePassNow) {
          runCatching { localStorage.saveBattlePassState(passSnapshot) }
        }
        if (saveProfileNow) {
          runCatching { localStorage.saveProfile(snapshot) }
          if (repo != null && uid != null) {
            runCatching { repo.saveProfileAndLeaderboard(snapshot, publicStats) }
          }
        }
        if (saveChallengeNow) {
          runCatching { localStorage.saveChallengeProgress(challengeSnapshot) }
        }
      }
    }
  }

  // Public Profile Customization, Announcements, Limited Exist Counters, Admin Panel & Danger Zone
  fun updatePublicProfileCustomization(
    statsPublic: Boolean,
    bio: String,
    avatarBase64: String,
    bannerBase64: String,
  ) {
    val updated =
      _uiState.value.profile.copy(
        statsPublic = statsPublic,
        bio = bio.trim().take(140),
        avatarBase64 = avatarBase64.take(60_000),
        bannerBase64 = bannerBase64.take(120_000),
      )
    applyProfile(updated, isLoaded = true)
    isDirty = true
    viewModelScope.launch {
      localStorage.saveProfile(updated)
      val repo = repository
      if (repo != null && currentUserId != null) {
        isCloudSaveInFlight = true
        try {
          repo.saveProfileAndLeaderboard(updated, buildPublicStatsSnapshot(_uiState.value, updated))
          if (_uiState.value.profile == updated) {
            isDirty = false
          }
          _toastEvents.tryEmit("Public profile & artwork updated!")
        } catch (e: CancellationException) {
          isDirty = true
          throw e
        } catch (e: Exception) {
          Log.w(TAG, "Profile artwork cloud save failed: ${e.message}")
          isDirty = true
          _toastEvents.tryEmit("Saved locally — cloud sync will retry shortly.")
        } finally {
          isCloudSaveInFlight = false
        }
      } else {
        isDirty = false
        _toastEvents.tryEmit("Public profile updated!")
      }
    }
  }

  fun dismissGlobalAnnouncement(messageId: String) {
    if (messageId.isBlank()) return
    _uiState.update { it.copy(dismissedAnnouncementId = messageId) }
    viewModelScope.launch { localStorage.saveDismissedAnnouncementId(messageId) }
  }

  fun adminGrantSelfCurrency(
    purrs: Long,
    hardcorePurrs: Long,
    gems: Long,
    shards: Long,
    passXp: Long,
  ) {
    val state = _uiState.value
    if (!state.isAdmin) return
    val current = state.profile
    val challenge = state.challengeState
    val nextChallenge =
      challenge.copy(
        hardcorePurrs = (challenge.hardcorePurrs + hardcorePurrs).coerceAtLeast(0L),
        gems = (challenge.gems + gems).coerceAtLeast(0L),
        totalGemsEarned = (challenge.totalGemsEarned + gems).coerceAtLeast(0L),
        gatoShards = (challenge.gatoShards + shards).coerceAtLeast(0L),
      )
    saveChallengeAndRefresh(nextChallenge)
    if (passXp > 0L) {
      grantPassXpImmediate(passXp)
    }
    if (purrs > 0L) {
      applyAndSaveImmediate(
        current.copy(
          purrs = current.purrs + purrs,
          totalPurrsEarned = current.totalPurrsEarned + purrs,
        )
      )
    }
    _toastEvents.tryEmit("Admin currency grant applied!")
  }

  fun adminSetWeather(condition: WeatherConditionKind) {
    val state = _uiState.value
    if (!state.isAdmin) return
    val newWeather =
      LiveWeatherState(
        cityName = "Sanctuary Sky",
        temperatureC =
          Random.nextInt(condition.tempRangeC.first, condition.tempRangeC.last + 1).toDouble(),
        condition = condition,
        nextShiftSeconds = WeatherRepository.WEATHER_INTERVAL_SECONDS,
        weatherShiftCount = state.weather.weatherShiftCount + 1L,
      )
    secondsUntilNextWeather = WeatherRepository.WEATHER_INTERVAL_SECONDS
    _uiState.update { it.copy(weather = newWeather) }
    applyProfile(_uiState.value.profile, isLoaded = true)
    _toastEvents.tryEmit("Admin Weather set to ${condition.label}!")
  }

  fun adminModeratePlayer(
    targetUserId: String,
    status: String,
    reason: String,
    timeoutMinutes: Long,
  ) {
    val state = _uiState.value
    if (!state.isAdmin) return
    val repo = repository ?: return
    val adminEmail = currentUserEmail?.trim().orEmpty().ifBlank { GameCatalog.ADMIN_EMAIL }
    val until =
      if (status == "TIMED_OUT" && timeoutMinutes > 0L) {
        System.currentTimeMillis() + (timeoutMinutes * 60_000L)
      } else {
        0L
      }
    viewModelScope.launch {
      runCatching {
        repo.setPlayerModeration(
          targetUserId = targetUserId.trim(),
          status = status,
          reason = reason,
          timeoutUntilMillis = until,
          incrementWarning = status == "WARNED" || status == "TIMED_OUT",
          adminEmail = adminEmail,
        )
      }.onSuccess {
        _toastEvents.tryEmit("Applied $status to $targetUserId")
      }.onFailure { e ->
        _toastEvents.tryEmit("Moderation failed: ${e.message}")
      }
    }
  }

  fun adminClearModeration(targetUserId: String) {
    val state = _uiState.value
    if (!state.isAdmin) return
    val repo = repository ?: return
    viewModelScope.launch {
      runCatching { repo.clearPlayerModeration(targetUserId.trim()) }
        .onSuccess { _toastEvents.tryEmit("Cleared moderation for $targetUserId") }
        .onFailure { e -> _toastEvents.tryEmit("Clear failed: ${e.message}") }
    }
  }

  fun adminSendGlobalMessage(title: String, body: String, severity: String) {
    val state = _uiState.value
    if (!state.isAdmin) return
    val repo = repository ?: return
    val adminEmail = currentUserEmail?.trim().orEmpty().ifBlank { GameCatalog.ADMIN_EMAIL }
    val messageId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
    viewModelScope.launch {
      runCatching {
        repo.sendGlobalAnnouncement(
          messageId = messageId,
          title = title,
          body = body,
          severity = severity,
          senderEmail = adminEmail,
        )
      }.onSuccess {
        _toastEvents.tryEmit("Global message broadcasted!")
      }.onFailure { e ->
        _toastEvents.tryEmit("Broadcast failed: ${e.message}")
      }
    }
  }

  fun adminDeleteGlobalMessage(messageId: String) {
    val state = _uiState.value
    if (!state.isAdmin) return
    val repo = repository ?: return
    viewModelScope.launch {
      runCatching { repo.deleteGlobalAnnouncement(messageId) }
        .onSuccess { _toastEvents.tryEmit("Deleted global announcement.") }
        .onFailure { e -> _toastEvents.tryEmit("Delete failed: ${e.message}") }
    }
  }

  fun deleteAllPlayerData(context: Context) {
    val appCtx = context.applicationContext
    soundManager.setSleepTimer(0)
    isDirty = false
    challengeDirty = false
    statsDirty = false
    passDirty = false
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      val freshSettings = AppSettingsState()
      applySettingsToAudio(freshSettings)
      localStorage.clearAllLocalData(appCtx)

      val uid = currentUserId
      val repo = repository
      val freshProfile =
        if (repo != null && uid != null) {
          runCatching { repo.deleteUserCloudData(uid) }
          runCatching { repo.ensureUserProfile(uid, initialDisplayName) }
            .getOrElse { UserProfile(userId = uid, displayName = initialDisplayName) }
        } else {
          UserProfile(userId = "local_caretaker", displayName = initialDisplayName)
        }

      rolledNightEpochDay = -1L
      isBloodmoonTonight = false
      secondsUntilNextWeather = WeatherRepository.WEATHER_INTERVAL_SECONDS
      val freshStats =
        PlayerStatsState(
          joinedTimestampMillis = now,
          lastActiveTimestampMillis = now,
          totalPlaySessions = 1L,
        )
      localStorage.savePlayerStats(freshStats)
      val (freshChallenge, _, _) = ensureHourlyFestivalAndQuests(ChallengeProgressState(), now)
      localStorage.saveChallengeProgress(freshChallenge)
      val freshFestTier = GameCatalog.festivalTierFor(freshChallenge.festivalIsMega)
      val (freshPass, _) = ensureHourlyPassQuests(BattlePassState(), now)
      localStorage.saveBattlePassState(freshPass)
      val freshWeather = weatherRepository.rollRandomWeather(previous = null, shiftCount = 1L)
      val currentWeek = GameCatalog.currentEpochWeek(now)

      _uiState.update { state ->
        state.copy(
          isLoading = false,
          errorMessage = null,
          profile = freshProfile,
          weather = freshWeather,
          settings = freshSettings,
          challengeState = freshChallenge,
          festivalTier = freshFestTier,
          festivalRerollCountdownText = formatDuration(GameCatalog.millisUntilNextEpochHour(now)),
          isTurkeyRushActive = GameCatalog.isTurkeySpawnWindowActive(now),
          turkeyRushCountdownText = GameCatalog.turkeyRushStatusText(now),
          hourlyFestivalQuests = GameCatalog.evaluateHourlyFestivalQuests(freshChallenge),
          spawnedTurkeys = emptyList(),
          playerStats = freshStats,
          battlePassState = freshPass,
          currentPassTier = 0,
          currentVipPassTier = 0,
          freePassStepProgress = 0L to GameCatalog.NOVEMBER_PASS_FREE_BASE_XP,
          vipPassStepProgress = 0L to GameCatalog.NOVEMBER_PASS_VIP_BASE_XP,
          hourlyPassQuests = GameCatalog.evaluateHourlyPassQuests(freshPass),
          claimablePassQuestsCount = 0,
          claimablePassCount = 0,
          goldenRainActive = false,
          goldenRainCountdownText = "",
          goldenTouchProgressText = "0m 0s / 30m",
          gemFrenzyActive = false,
          gemFrenzyCountdownText = "",
          gemCharmMultiplier = 1.0,
          nextGemCharmCost = GameCatalog.gemCharmCost(0L),
          vipRequirementUpgradeCount = 0L,
          comboCharge = 0f,
          comboMultiplier = 1.0,
          canClaimDailyReward = true,
          nextDailyDayIndex = 1,
          nextDailyPayout = GameCatalog.computeEndlessDailyPayout(1L),
          canClaimWeeklyReward = freshStats.lastWeeklyRewardWeekId < currentWeek,
          currentPlayerGlobalRank = null,
          pendingOfflineReport = null,
          pendingWeeklyReward = null,
          nextRebirthCost = GameCatalog.REBIRTH_BASE_COST,
          rebirthMultiplier = 1.0,
          hardcoreEligible = false,
          vaultKeeperTapCount = 0,
          vaultCooldownRemainingMillis = 0L,
          vaultCooldownText = "",
          dismissedAnnouncementId = "",
          limitedExistCounts = remoteExistCounts,
          particles = emptyList(),
        )
      }
      applyProfile(freshProfile, isLoaded = true)
      _toastEvents.tryEmit("All Sanctuary data wiped — Fresh start complete!")
    }
  }

  private fun enforceModerationBlock(state: PurrGameUiState): Boolean {
    if (state.isBanned) {
      _toastEvents.tryEmit(
        "Account Banned: ${state.activeModeration?.reason?.ifBlank { "Suspended by Admin" }}"
      )
      return true
    }
    if (state.isTimedOut) {
      val rem =
        ((state.activeModeration?.timeoutUntilMillis ?: 0L) - System.currentTimeMillis())
          .coerceAtLeast(1000L)
      _toastEvents.tryEmit(
        "Timed Out (${formatDuration(rem)} left): ${state.activeModeration?.reason ?: "Wait before interacting"}"
      )
      return true
    }
    return false
  }

  private fun recordLimitedItemUnlock(itemType: String, itemId: String) {
    val statId = GameCatalog.existStatId(itemType, itemId)
    var updatedMap: Map<String, Long> = emptyMap()
    _uiState.update { state ->
      val nextVal = (state.limitedExistCounts[statId] ?: 0L) + 1L
      val mutable = state.limitedExistCounts.toMutableMap()
      mutable[statId] = nextVal
      updatedMap = mutable.toMap()
      state.copy(limitedExistCounts = updatedMap)
    }
    viewModelScope.launch {
      runCatching { localStorage.saveLimitedExistCounts(updatedMap) }
      val repo = repository
      if (repo != null && currentUserId != null) {
        runCatching { repo.incrementLimitedItemExistCount(statId, itemType) }
          .onFailure { e -> Log.w(TAG, "Exist counter cloud increment skipped: ${e.message}") }
      }
    }
  }

  private fun ensureOwnedLimitedFloor(
    counts: MutableMap<String, Long>,
    patterns: List<String>,
    accessories: List<String>,
    themes: List<String>,
    backgrounds: List<String> = emptyList(),
  ) {
    patterns.forEach { patId ->
      if (GameCatalog.patterns.any { it.id == patId && it.isEventExclusive }) {
        val k = GameCatalog.existStatId("PATTERN", patId)
        counts[k] = maxOf(counts[k] ?: 0L, 1L)
      }
    }
    accessories.forEach { accId ->
      if (GameCatalog.accessories.any { it.id == accId && it.isEventExclusive }) {
        val k = GameCatalog.existStatId("ACCESSORY", accId)
        counts[k] = maxOf(counts[k] ?: 0L, 1L)
      }
    }
    themes.forEach { thmId ->
      if (AppColorTheme.entries.any { it.id == thmId && !it.isFree }) {
        val k = GameCatalog.existStatId("THEME", thmId)
        counts[k] = maxOf(counts[k] ?: 0L, 1L)
      }
    }
    backgrounds.forEach { bgId ->
      if (GameCatalog.backgrounds.any { it.id == bgId && it.isEventExclusive }) {
        val k = GameCatalog.existStatId("BACKGROUND", bgId)
        counts[k] = maxOf(counts[k] ?: 0L, 1L)
      }
    }
  }

  private fun mergeExistMaps(a: Map<String, Long>, b: Map<String, Long>): Map<String, Long> {
    if (a.isEmpty()) return b
    if (b.isEmpty()) return a
    val merged = a.toMutableMap()
    b.forEach { (k, v) ->
      merged[k] = maxOf(merged[k] ?: 0L, v)
    }
    return merged
  }

  private fun mergeLocalAndRemote(
    local: UserProfile,
    remote: UserProfile,
    uid: String,
    displayName: String,
  ): UserProfile {
    if (local.totalPurrsEarned <= remote.totalPurrsEarned && local.totalPets <= remote.totalPets) {
      return remote.copy(
        bio = remote.bio.ifBlank { local.bio },
        avatarBase64 = remote.avatarBase64.ifBlank { local.avatarBase64 },
        bannerBase64 = remote.bannerBase64.ifBlank { local.bannerBase64 },
      )
    }
    val mergedUpgrades =
      (remote.upgradeLevels.keys + local.upgradeLevels.keys).associateWith { key ->
        maxOf(remote.upgradeLevels[key] ?: 0L, local.upgradeLevels[key] ?: 0L)
      }
    return remote.copy(
      userId = uid,
      displayName = remote.displayName.ifBlank { displayName },
      purrs = maxOf(remote.purrs, local.purrs),
      totalPurrsEarned = maxOf(remote.totalPurrsEarned, local.totalPurrsEarned),
      totalPets = maxOf(remote.totalPets, local.totalPets),
      selectedCatName =
        if (local.totalPets > remote.totalPets) local.selectedCatName else remote.selectedCatName,
      selectedPatternId =
        if (local.totalPets > remote.totalPets) local.selectedPatternId
        else remote.selectedPatternId,
      selectedAccessoryId =
        if (local.totalPets > remote.totalPets) local.selectedAccessoryId
        else remote.selectedAccessoryId,
      selectedBackgroundId =
        if (local.selectedBackgroundId != "cozy_parlor") local.selectedBackgroundId
        else remote.selectedBackgroundId,
      unlockedPatterns = (remote.unlockedPatterns + local.unlockedPatterns).distinct(),
      unlockedAccessories = (remote.unlockedAccessories + local.unlockedAccessories).distinct(),
      unlockedBackgrounds = (remote.unlockedBackgrounds + local.unlockedBackgrounds).distinct(),
      upgradeLevels = mergedUpgrades,
      dailyStreak = maxOf(remote.dailyStreak, local.dailyStreak),
      lastDailyClaimEpochDay = maxOf(remote.lastDailyClaimEpochDay, local.lastDailyClaimEpochDay),
      claimedQuestIds = (remote.claimedQuestIds + local.claimedQuestIds).distinct(),
      unlockedBadgeIds = (remote.unlockedBadgeIds + local.unlockedBadgeIds).distinct(),
      equippedBadgeId = remote.equippedBadgeId.ifBlank { local.equippedBadgeId },
      eventPurrsEarned = maxOf(remote.eventPurrsEarned, local.eventPurrsEarned),
      preferredCity = "Sanctuary Sky",
      weatherCheckedCount = maxOf(remote.weatherCheckedCount, local.weatherCheckedCount),
      statsPublic = local.statsPublic,
      bio = local.bio.ifBlank { remote.bio },
      avatarBase64 = local.avatarBase64.ifBlank { remote.avatarBase64 },
      bannerBase64 = local.bannerBase64.ifBlank { remote.bannerBase64 },
    )
  }

  private fun computeEffectiveClick(
    profile: UserProfile,
    weather: LiveWeatherState,
    event: LimitedEventDef,
    moonlightPhase: MoonlightPhase,
    comboMult: Double,
    challenge: ChallengeProgressState,
    goldenRainActive: Boolean = false,
    festivalTier: FestivalTier = FestivalTier.REGULAR,
  ): Long {
    val activeUpgrades =
      if (challenge.isHardcoreMode) challenge.hardcoreUpgradeLevels else profile.upgradeLevels
    val basePps = GameCatalog.computeBasePps(activeUpgrades)
    val base =
      GameCatalog.computeClickPower(activeUpgrades) +
        if ("asc_click_supernova" in challenge.unlockedAscensionIds) {
          (basePps * 0.06).roundToLong()
        } else {
          0L
        }
    val rebirthMult =
      GameCatalog.rebirthMultiplier(challenge.rebirthBonusSteps, challenge.unlockedAscensionIds)
    val goldenMult =
      if ("asc_golden_whiskers" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val supernovaMult =
      if ("asc_click_supernova" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val sovereignMult =
      if ("asc_cosmic_sovereign" in challenge.unlockedAscensionIds) 1.25 else 1.0
    val harmonyBoost =
      if ("asc_lunar_harmony" in challenge.unlockedAscensionIds) 1.25 else 1.0
    val weatherHarmonyBoost =
      harmonyBoost + (if (challenge.prismLensUnlocked) 0.15 else 0.0)
    val weatherMult = 1.0 + ((weather.condition.clickMultiplier - 1.0) * weatherHarmonyBoost)
    val eventClickBase = maxOf(event.clickMultiplier, festivalTier.clickMultiplier)
    val eventMult = 1.0 + ((eventClickBase - 1.0) * harmonyBoost)
    val moonMult = 1.0 + ((moonlightPhase.clickMultiplier - 1.0) * harmonyBoost)
    val goldenRainMult = if (goldenRainActive) GameCatalog.GOLDEN_RAIN_MULTIPLIER else 1.0
    val gemCharmMult = GameCatalog.gemCharmMultiplier(challenge.gemCharmLevel)
    val gemFrenzyMult =
      if (challenge.gemFrenzyRemainingMillis > 0L) GameCatalog.GEM_FRENZY_MULTIPLIER else 1.0
    val hardcoreMult =
      if (challenge.isHardcoreMode) GameCatalog.HARDCORE_EARNINGS_MULTIPLIER else 1.0
    return (base *
        comboMult *
        weatherMult *
        eventMult *
        moonMult *
        goldenRainMult *
        gemCharmMult *
        gemFrenzyMult *
        rebirthMult *
        goldenMult *
        supernovaMult *
        sovereignMult *
        hardcoreMult)
      .roundToLong()
      .coerceAtLeast(1L)
  }

  private fun computeEffectivePps(
    profile: UserProfile,
    weather: LiveWeatherState,
    event: LimitedEventDef,
    moonlightPhase: MoonlightPhase,
    challenge: ChallengeProgressState,
    goldenRainActive: Boolean = false,
    festivalTier: FestivalTier = FestivalTier.REGULAR,
  ): Long {
    val activeUpgrades =
      if (challenge.isHardcoreMode) challenge.hardcoreUpgradeLevels else profile.upgradeLevels
    val base = GameCatalog.computeBasePps(activeUpgrades)
    if (base <= 0L) return 0L
    val rebirthMult =
      GameCatalog.rebirthMultiplier(challenge.rebirthBonusSteps, challenge.unlockedAscensionIds)
    val goldenMult =
      if ("asc_golden_whiskers" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val symphonyMult =
      if ("asc_passive_symphony" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val sovereignMult =
      if ("asc_cosmic_sovereign" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val harmonyBoost =
      if ("asc_lunar_harmony" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val weatherHarmonyBoost =
      harmonyBoost + (if (challenge.prismLensUnlocked) 0.20 else 0.0)
    val weatherMult = 1.0 + ((weather.condition.ppsMultiplier - 1.0) * weatherHarmonyBoost)
    val eventPpsBase = maxOf(event.ppsMultiplier, festivalTier.ppsMultiplier)
    val eventMult = 1.0 + ((eventPpsBase - 1.0) * harmonyBoost)
    val moonMult = 1.0 + ((moonlightPhase.ppsMultiplier - 1.0) * harmonyBoost)
    val goldenRainMult = if (goldenRainActive) GameCatalog.GOLDEN_RAIN_MULTIPLIER else 1.0
    val gemCharmMult = GameCatalog.gemCharmMultiplier(challenge.gemCharmLevel)
    val gemFrenzyMult =
      if (challenge.gemFrenzyRemainingMillis > 0L) GameCatalog.GEM_FRENZY_MULTIPLIER else 1.0
    val hardcoreMult =
      if (challenge.isHardcoreMode) GameCatalog.HARDCORE_EARNINGS_MULTIPLIER else 1.0
    return (base *
        weatherMult *
        eventMult *
        moonMult *
        goldenRainMult *
        gemCharmMult *
        gemFrenzyMult *
        rebirthMult *
        goldenMult *
        symphonyMult *
        sovereignMult *
        hardcoreMult)
      .roundToLong()
      .coerceAtLeast(1L)
  }

  private fun currentUtcEpochDay(nowMillis: Long = System.currentTimeMillis()): Long {
    return TimeUnit.MILLISECONDS.toDays(nowMillis)
  }

  private fun computeNextDailyDay(profile: UserProfile, today: Long): Int {
    if (profile.lastDailyClaimEpochDay == 0L) return 1
    return if (profile.lastDailyClaimEpochDay >= today) {
      (((profile.dailyStreak - 1L).coerceAtLeast(0L) % 14L) + 1L).toInt()
    } else if (profile.lastDailyClaimEpochDay == today - 1L) {
      ((profile.dailyStreak % 14L) + 1L).toInt()
    } else {
      1
    }
  }

  private fun computeNextStreakNumber(profile: UserProfile, today: Long): Long {
    if (profile.lastDailyClaimEpochDay == 0L) return 1L
    return if (profile.lastDailyClaimEpochDay >= today) {
      profile.dailyStreak.coerceAtLeast(1L)
    } else if (profile.lastDailyClaimEpochDay == today - 1L) {
      profile.dailyStreak + 1L
    } else {
      1L
    }
  }

  private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
      days > 0 -> "${days}d ${hours}h"
      hours > 0 -> "${hours}h ${minutes}m"
      else -> "${minutes}m ${seconds}s"
    }
  }

  companion object {
    private const val TAG = "PurrGameVM"
  }
}
