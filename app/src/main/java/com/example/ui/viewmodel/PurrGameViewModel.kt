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
import com.example.audio.MusicTrack
import com.example.audio.SoundManager
import com.example.data.local.AppSettingsState
import com.example.data.local.LocalGameStorage
import com.example.data.model.ActiveBattlePassQuest
import com.example.data.model.BattlePassRewardItem
import com.example.data.model.BattlePassState
import com.example.data.model.ChallengeProgressState
import com.example.data.model.DailyBonusPrizeType
import com.example.data.model.EndlessDailyPayout
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LimitedEventDef
import com.example.data.model.MoonlightEventState
import com.example.data.model.MoonlightPhase
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
}

class PurrGameViewModel(
  private val repository: PurrGameRepository?,
  private val localStorage: LocalGameStorage,
  private val weatherRepository: WeatherRepository,
  private val soundManager: SoundManager,
  private val currentUserId: String?,
  private val initialDisplayName: String,
) : ViewModel() {

  private val _uiState =
    MutableStateFlow(
      PurrGameUiState(
        isSignedIn = currentUserId != null,
        weather = weatherRepository.rollRandomWeather(previous = null, shiftCount = 1L),
      )
    )
  val uiState: StateFlow<PurrGameUiState> = _uiState.asStateFlow()

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

  init {
    bootstrapPlayer()
    startGameTicker()
    observeGlobalRank()
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

  private fun bootstrapPlayer() {
    viewModelScope.launch {
      try {
        val now = System.currentTimeMillis()
        val loadedSettings = localStorage.loadSettings()
        applySettingsToAudio(loadedSettings)
        val loadedChallenge = localStorage.loadChallengeProgress()
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
        _uiState.update {
          it.copy(
            settings = loadedSettings,
            challengeState = loadedChallenge,
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
          )
        }

        val roll = localStorage.loadNightRoll()
        rolledNightEpochDay = roll.epochDay
        isBloodmoonTonight = roll.isBloodmoon
        ensureNightRollForCurrentTime(now)

        val localProfile = localStorage.loadProfile()

        if (repository != null && currentUserId != null) {
          val remote = repository.ensureUserProfile(currentUserId, initialDisplayName)
          val merged = mergeLocalAndRemote(localProfile, remote, currentUserId, initialDisplayName)
          localStorage.saveProfile(merged)
          if (merged != remote) {
            repository.saveProfileAndLeaderboard(merged)
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
              if (remoteUpdate != null && !isDirty) {
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
        val now = System.currentTimeMillis()
        val (event, remainingMillis) = GameCatalog.currentPeriodicEvent(now)
        val countdown = formatDuration(remainingMillis)

        ensureNightRollForCurrentTime(now)

        secondsUntilNextWeather--
        val shouldRollWeather = secondsUntilNextWeather <= 0
        if (shouldRollWeather) {
          secondsUntilNextWeather = WeatherRepository.WEATHER_INTERVAL_SECONDS
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

          val currentChallenge =
            let {
              var next = state.challengeState
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
            nextPass =
              nextPass.copy(
                passXp = nextPass.passXp + 15L,
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

          val baseDecay = if (updatedChallenge.isHardcoreMode) 0.32f else 0.16f
          val decayRate =
            if ("asc_frenzy_aura" in updatedChallenge.unlockedAscensionIds) baseDecay * 0.5f
            else baseDecay
          val decayedCombo = (state.comboCharge - decayRate).coerceAtLeast(0f)
          val maxComboScale =
            if ("asc_frenzy_aura" in updatedChallenge.unlockedAscensionIds) 2.0 else 1.0
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

          val nextStreakPreview = computeNextStreakNumber(updatedProfile, epochDay)
          val nextFreeTier = GameCatalog.freeTierFromXp(nextPass.passXp)
          val nextVipTier = GameCatalog.vipTierFromXp(nextPass.passXp)
          val nextQuests = GameCatalog.evaluateHourlyPassQuests(nextPass)

          state.copy(
            profile = updatedProfile,
            weather = nextWeather,
            challengeState = updatedChallenge,
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

    val challenge = state.challengeState
    val baseChargeGain = if (challenge.isHardcoreMode) 0.05f else 0.10f
    val chargeGain =
      if ("asc_frenzy_aura" in challenge.unlockedAscensionIds) baseChargeGain * 2f
      else baseChargeGain
    val newCombo = (state.comboCharge + chargeGain).coerceAtMost(1f)
    val maxComboScale =
      if ("asc_frenzy_aura" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val comboMult = 1.0 + (newCombo * maxComboScale)

    val luckyJackpot =
      "asc_lucky_catnip" in challenge.unlockedAscensionIds && Random.nextInt(100) < 20
    val isCrit = luckyJackpot || comboMult >= 1.65
    val rawClickGain =
      computeEffectiveClick(
        state.profile,
        state.weather,
        state.activeEvent,
        state.moonlightState.phase,
        comboMult,
        challenge,
        state.goldenRainActive,
      )
    val clickGain = if (luckyJackpot) rawClickGain * 5L else rawClickGain

    soundManager.playCatTapSound(isComboCrit = isCrit)

    val boostedSky =
      state.goldenRainActive ||
        state.moonlightState.phase == MoonlightPhase.BLOODMOON ||
        state.weather.condition == WeatherConditionKind.AURORA_BOREALIS ||
        state.weather.condition == WeatherConditionKind.METEOR_SHOWER
    val gemThreshold =
      if (boostedSky) GameCatalog.GEM_DROP_RATE_BOOSTED_PER_1000
      else GameCatalog.GEM_DROP_RATE_NORMAL_PER_1000
    val foundGem = comboMult >= 1.25 && Random.nextInt(1000) < gemThreshold

    val particleId = ++particleCounter
    val particlePrefix = if (luckyJackpot) "★ +" else "+"
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

    isDirty = true
    statsDirty = true
    passDirty = true
    if (challenge.isHardcoreMode || foundGem) challengeDirty = true
    _uiState.update { s ->
      val nextChallenge =
        s.challengeState.let { c ->
          var updated =
            if (c.isHardcoreMode) c.copy(hardcorePurrs = c.hardcorePurrs + clickGain) else c
          if (foundGem) {
            updated = updated.copy(gems = updated.gems + 1L, totalGemsEarned = updated.totalGemsEarned + 1L)
          }
          updated
        }
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
      val (syncedPass, _) = ensureHourlyPassQuests(s.battlePassState, System.currentTimeMillis())
      val nextPass =
        syncedPass.copy(
          passXp = syncedPass.passXp + 2L,
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
          ),
        particles = appended.takeLast(12),
      )
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

  fun buyUpgrade(upgradeId: String) {
    val def = GameCatalog.upgrades.find { it.id == upgradeId } ?: return
    val state = _uiState.value
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

    if (isHardcore) {
      val updatedLevels =
        challenge.hardcoreUpgradeLevels.toMutableMap().apply { put(upgradeId, currentLevel + 1L) }
      val nextChallenge =
        challenge.copy(
          hardcorePurrs = balance - cost,
          hardcoreUpgradeLevels = updatedLevels,
        )
      saveChallengeAndRefresh(nextChallenge)
    } else {
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
      _toastEvents.tryEmit("Unlocked & equipped ${def.name}!")
    } else {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} for ${def.name}.")
    }
  }

  fun selectOrUnlockAccessory(accessoryId: String) {
    val def = GameCatalog.accessories.find { it.id == accessoryId } ?: return
    val state = _uiState.value
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
      _toastEvents.tryEmit("Unlocked & equipped ${def.name}!")
    } else {
      val missing = GameCatalog.formatCompactPurrs(cost - balance)
      _toastEvents.tryEmit("Need $missing more ${state.activeCurrencyShortLabel} for ${def.name}.")
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
    updateSettings { s ->
      s.copy(
        unlockedColorThemes = (s.unlockedColorThemes + theme.id).distinct(),
        colorTheme = theme,
      )
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

    // Rebirths DO NOT reset dailyStreak or unlocked Color Themes!
    val resetProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else starterPurrs,
        upgradeLevels = if (isHardcore) current.upgradeLevels else emptyMap(),
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
          if (isHardcore) emptyMap() else challenge.hardcoreUpgradeLevels,
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
      "Rebirth #$nextRebirthCount Complete! ${newMult}x multiplier & +${GameCatalog.REBIRTH_GEM_REWARD} Gems (Streak, Themes & Gems kept!)"
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
    val isHardcore = challenge.isHardcoreMode

    val resetProfile =
      current.copy(
        purrs = if (isHardcore) current.purrs else starterPurrs,
        upgradeLevels = if (isHardcore) current.upgradeLevels else emptyMap(),
        unlockedPatterns = preservedPatterns,
        unlockedAccessories = preservedAccessories,
        selectedPatternId = nextPattern,
        selectedAccessoryId = nextAccessory,
      )

    val nextShards = challenge.gatoShards + 1L
    val nextAscensionCount = challenge.ascensionCount + 1L
    val nextChallenge =
      challenge.copy(
        rebirthCount = GameCatalog.ASCENSION_RESET_REBIRTH_COUNT,
        rebirthBonusSteps = 0L,
        ascensionCount = nextAscensionCount,
        gatoShards = nextShards,
        gems = challenge.gems + GameCatalog.ASCENSION_GEM_REWARD,
        totalGemsEarned = challenge.totalGemsEarned + GameCatalog.ASCENSION_GEM_REWARD,
        hardcorePurrs = if (isHardcore) starterPurrs else challenge.hardcorePurrs,
        hardcoreUpgradeLevels =
          if (isHardcore) emptyMap() else challenge.hardcoreUpgradeLevels,
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
      "Gato Ascensión #$nextAscensionCount! Earned +1 Gato Shard & +${GameCatalog.ASCENSION_GEM_REWARD} Gems!"
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
    viewModelScope.launch { localStorage.saveChallengeProgress(nextChallenge) }
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
        }
      }
      DailyBonusPrizeType.ACCESSORY -> {
        if (reward.bonusItemId.isNotBlank() && reward.bonusItemId !in unlockedAccessories) {
          unlockedAccessories = unlockedAccessories + reward.bonusItemId
          bonusLabelSuffix = " & ${reward.bonusPrizeLabel}"
        }
      }
      DailyBonusPrizeType.COLOR_THEME -> {
        if (reward.bonusItemId.isNotBlank() && reward.bonusItemId !in state.settings.unlockedColorThemes) {
          updateSettings { s ->
            s.copy(unlockedColorThemes = (s.unlockedColorThemes + reward.bonusItemId).distinct())
          }
          bonusLabelSuffix = " & ${reward.bonusPrizeLabel}"
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
      )
    val report =
      GameCatalog.computeOfflineEarnings(
        elapsedMillis = elapsedMs,
        effectivePps = pps,
        isHardcore = challenge.isHardcoreMode,
        hasAscended = challenge.ascensionCount > 0L,
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

    val nextPass =
      syncedPass.copy(
        passXp = syncedPass.passXp + quest.xpReward,
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
    _toastEvents.tryEmit("Pass Quest Complete! +${quest.xpReward} Pass XP")
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
          }
        }
        PassRewardKind.SKIN -> {
          if (reward.itemId.isNotBlank() && reward.itemId !in patterns) {
            patterns.add(reward.itemId)
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
    val (syncedPass, _) = ensureHourlyPassQuests(_uiState.value.battlePassState, now)
    val nextPass =
      syncedPass.copy(
        passXp = syncedPass.passXp + xpGain,
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
    val nextChallenge = challenge.copy(gems = challenge.gems - GameCatalog.GEM_WEATHER_SUMMON_COST)
    val now = System.currentTimeMillis()
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

  // Settings & Built-In Music Player Controls
  fun setThemeMode(mode: ThemeMode) {
    updateSettings { it.copy(themeMode = mode) }
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
    val nextIndex = (currentIndex + step + allIds.size) % allIds.size
    updateSettings { it.copy(selectedTrackId = allIds[nextIndex], isMusicPlaying = true) }
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
        viewModelScope.launch {
          runCatching { localStorage.savePlayerStats(nextStats) }
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
        processOfflineReturn(pauseAt, now)
        checkWeeklyAutoRollover(now)
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
    )
  }

  private fun applyAndSaveImmediate(updatedProfile: UserProfile) {
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
      val nextStreakPreview = computeNextStreakNumber(profile, epochDay)
      val goldenActive = state.battlePassState.goldenRainRemainingMillis > 0L
      state.copy(
        isLoading = !isLoaded,
        errorMessage = null,
        profile = profile,
        weather = weather,
        activeEvent = event,
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
          ),
        effectivePps =
          computeEffectivePps(
            profile,
            weather,
            event,
            moonlight.phase,
            challenge,
            goldenActive,
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

  private suspend fun persistCurrentProfile() {
    if (!hasInitializedProfile) return
    val snapshot = _uiState.value.profile
    isDirty = false
    localStorage.saveProfile(snapshot)
    val repo = repository
    if (repo != null && currentUserId != null) {
      runCatching { repo.saveProfileAndLeaderboard(snapshot) }
        .onFailure { e ->
          Log.w(TAG, "Cloud save deferred due to error: ${e.message}")
          isDirty = true
        }
    }
  }

  override fun onCleared() {
    super.onCleared()
    soundManager.release()
    val now = System.currentTimeMillis()
    val snapshot = _uiState.value.profile
    val challengeSnapshot = _uiState.value.challengeState
    val statsSnapshot = _uiState.value.playerStats.copy(lastActiveTimestampMillis = now)
    val passSnapshot = _uiState.value.battlePassState
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
            runCatching { repo.saveProfileAndLeaderboard(snapshot) }
          }
        }
        if (saveChallengeNow) {
          runCatching { localStorage.saveChallengeProgress(challengeSnapshot) }
        }
      }
    }
  }

  private fun mergeLocalAndRemote(
    local: UserProfile,
    remote: UserProfile,
    uid: String,
    displayName: String,
  ): UserProfile {
    if (local.totalPurrsEarned <= remote.totalPurrsEarned && local.totalPets <= remote.totalPets) {
      return remote
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
      unlockedPatterns = (remote.unlockedPatterns + local.unlockedPatterns).distinct(),
      unlockedAccessories = (remote.unlockedAccessories + local.unlockedAccessories).distinct(),
      upgradeLevels = mergedUpgrades,
      dailyStreak = maxOf(remote.dailyStreak, local.dailyStreak),
      lastDailyClaimEpochDay = maxOf(remote.lastDailyClaimEpochDay, local.lastDailyClaimEpochDay),
      claimedQuestIds = (remote.claimedQuestIds + local.claimedQuestIds).distinct(),
      unlockedBadgeIds = (remote.unlockedBadgeIds + local.unlockedBadgeIds).distinct(),
      equippedBadgeId = remote.equippedBadgeId.ifBlank { local.equippedBadgeId },
      eventPurrsEarned = maxOf(remote.eventPurrsEarned, local.eventPurrsEarned),
      preferredCity = "Sanctuary Sky",
      weatherCheckedCount = maxOf(remote.weatherCheckedCount, local.weatherCheckedCount),
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
  ): Long {
    val activeUpgrades =
      if (challenge.isHardcoreMode) challenge.hardcoreUpgradeLevels else profile.upgradeLevels
    val base = GameCatalog.computeClickPower(activeUpgrades)
    val rebirthMult =
      GameCatalog.rebirthMultiplier(challenge.rebirthBonusSteps, challenge.unlockedAscensionIds)
    val goldenMult =
      if ("asc_golden_whiskers" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val harmonyBoost =
      if ("asc_lunar_harmony" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val weatherHarmonyBoost =
      harmonyBoost + (if (challenge.prismLensUnlocked) 0.20 else 0.0)
    val weatherMult = 1.0 + ((weather.condition.clickMultiplier - 1.0) * weatherHarmonyBoost)
    val eventMult = 1.0 + ((event.clickMultiplier - 1.0) * harmonyBoost)
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
  ): Long {
    val activeUpgrades =
      if (challenge.isHardcoreMode) challenge.hardcoreUpgradeLevels else profile.upgradeLevels
    val base = GameCatalog.computeBasePps(activeUpgrades)
    if (base <= 0L) return 0L
    val rebirthMult =
      GameCatalog.rebirthMultiplier(challenge.rebirthBonusSteps, challenge.unlockedAscensionIds)
    val goldenMult =
      if ("asc_golden_whiskers" in challenge.unlockedAscensionIds) 2.0 else 1.0
    val harmonyBoost =
      if ("asc_lunar_harmony" in challenge.unlockedAscensionIds) 1.5 else 1.0
    val weatherHarmonyBoost =
      harmonyBoost + (if (challenge.prismLensUnlocked) 0.20 else 0.0)
    val weatherMult = 1.0 + ((weather.condition.ppsMultiplier - 1.0) * weatherHarmonyBoost)
    val eventMult = 1.0 + ((event.ppsMultiplier - 1.0) * harmonyBoost)
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
