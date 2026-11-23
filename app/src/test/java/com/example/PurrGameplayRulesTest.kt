package com.example

import com.example.audio.CatSoundEffect
import com.example.audio.CustomMusicTrack
import com.example.audio.MusicTrack
import com.example.audio.RepeatMode
import com.example.audio.SoundManager
import com.example.data.model.BattlePassState
import com.example.data.model.ChallengeProgressState
import com.example.data.model.DailyBonusPrizeType
import com.example.data.model.FeastZoneActivityType
import com.example.data.model.FestivalTier
import com.example.data.model.GameCatalog
import com.example.data.model.InventoryEventGroup
import com.example.data.model.MoonlightPhase
import com.example.data.model.NavigationLayoutFormat
import com.example.data.model.PassRewardKind
import com.example.data.model.PlayerStatsState
import com.example.data.model.UpgradeCategory
import com.example.data.model.UserProfile
import com.example.data.weather.WeatherConditionKind
import com.example.data.weather.WeatherRepository
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.ThemeMode
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PurrGameplayRulesTest {

  @Test
  fun randomGameWeather_rollsAcrossTwelveConditionsEveryMinute_withNoRealLocation() {
    val repo = WeatherRepository()
    assertEquals(60, WeatherRepository.WEATHER_INTERVAL_SECONDS)
    assertEquals(12, WeatherConditionKind.entries.size)
    assertTrue(WeatherConditionKind.SUNSET_GAZE in WeatherConditionKind.entries)
    assertTrue(WeatherConditionKind.AURORA_BOREALIS in WeatherConditionKind.entries)
    assertTrue(WeatherConditionKind.RAINBOW_SHOWER in WeatherConditionKind.entries)
    assertTrue(WeatherConditionKind.METEOR_SHOWER in WeatherConditionKind.entries)

    var prev = WeatherConditionKind.SUNNY
    val rng = Random(42)
    val seen = mutableSetOf<WeatherConditionKind>()
    repeat(60) { idx ->
      val rolled = repo.rollRandomWeather(previous = prev, shiftCount = idx + 1L, random = rng)
      assertNotEquals(prev, rolled.condition)
      assertEquals("Sanctuary Sky", rolled.cityName)
      assertTrue(rolled.temperatureC.toInt() in rolled.condition.tempRangeC)
      seen.add(rolled.condition)
      prev = rolled.condition
    }
    assertTrue(seen.size >= 10)
  }

  @Test
  fun moonlightEvent_activeMidnightUntilMorning_withBloodmoonVariant() {
    val sunriseMinute = 6 * 60 // 06:00 AM

    val normalNight =
      GameCatalog.evaluateMoonlightWindow(
        minuteOfDay = 90,
        morningEndMinute = sunriseMinute,
        isBloodmoonNight = false,
      )
    assertEquals(MoonlightPhase.MOONLIGHT, normalNight.phase)
    assertEquals(1.75, normalNight.phase.clickMultiplier, 0.001)
    assertEquals(1.75, normalNight.phase.ppsMultiplier, 0.001)

    val bloodmoonNight =
      GameCatalog.evaluateMoonlightWindow(
        minuteOfDay = 90,
        morningEndMinute = sunriseMinute,
        isBloodmoonNight = true,
      )
    assertEquals(MoonlightPhase.BLOODMOON, bloodmoonNight.phase)
    assertEquals(2.5, bloodmoonNight.phase.clickMultiplier, 0.001)
    assertEquals(2.5, bloodmoonNight.phase.ppsMultiplier, 0.001)

    val morningDay =
      GameCatalog.evaluateMoonlightWindow(
        minuteOfDay = 7 * 60,
        morningEndMinute = sunriseMinute,
        isBloodmoonNight = true,
      )
    assertEquals(MoonlightPhase.INACTIVE, morningDay.phase)
    assertEquals(1.0, morningDay.phase.clickMultiplier, 0.001)
  }

  @Test
  fun economyBalance_hasExpandedUpgradesAndChallengingAchievements() {
    val clickUpgrades = GameCatalog.upgrades.filter { it.category == UpgradeCategory.CLICK }
    val ppsUpgrades = GameCatalog.upgrades.filter { it.category == UpgradeCategory.PER_SECOND }

    assertEquals(11, clickUpgrades.size)
    assertEquals(12, ppsUpgrades.size)

    for (i in 1 until clickUpgrades.size) {
      assertTrue(clickUpgrades[i].baseCost > clickUpgrades[i - 1].baseCost)
    }
    for (i in 1 until ppsUpgrades.size) {
      assertTrue(ppsUpgrades[i].baseCost > ppsUpgrades[i - 1].baseCost)
    }

    assertEquals(25, GameCatalog.quests.size)
    assertEquals(26, GameCatalog.badges.size)
    assertTrue((GameCatalog.quests.find { it.id == "q_first_pets" }?.target ?: 0L) >= 100L)
    assertTrue((GameCatalog.quests.maxOf { it.target }) >= 1_000_000_000L)
  }

  @Test
  fun eventBoutiqueAndDailyRewards_includeSkinsAccessoriesUpgradesAndThemes() {
    assertEquals(14, GameCatalog.dailyRewards.size)
    val bonusDays = GameCatalog.dailyRewards.filter { it.bonusType != DailyBonusPrizeType.NONE }
    assertTrue(bonusDays.any { it.day == 3 && it.bonusType == DailyBonusPrizeType.ACCESSORY })
    assertTrue(bonusDays.any { it.day == 5 && it.bonusType == DailyBonusPrizeType.UPGRADE_LEVEL })
    assertTrue(bonusDays.any { it.day == 7 && it.bonusType == DailyBonusPrizeType.PATTERN })
    assertTrue(bonusDays.any { it.day == 10 && it.bonusType == DailyBonusPrizeType.COLOR_THEME })
    assertTrue(bonusDays.any { it.day == 14 && it.bonusType == DailyBonusPrizeType.PATTERN })

    assertTrue(GameCatalog.eventExclusivePatterns.size >= 4)
    assertTrue(GameCatalog.eventExclusiveAccessories.size >= 4)
    assertTrue(AppColorTheme.freeThemes.size >= 6)
    assertTrue(AppColorTheme.eventThemes.size >= 3)
  }

  @Test
  fun themeRequirements_strictlyEnforceMilestonesBeforeUnlock() {
    val freshProfile = UserProfile(purrs = 10_000_000L)
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.STARLIGHT_LANTERN, freshProfile).first)
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.CRIMSON_BLOODMOON, freshProfile).first)
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.AURORA_FROST, freshProfile).first)
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.ROSE_GOLD, freshProfile).first)

    val qualifiedProfile =
      UserProfile(
        purrs = 500_000L,
        eventPurrsEarned = 120_000L,
        totalPets = 2_000L,
        upgradeLevels = mapOf("soft_Brush" to 15L),
        weatherCheckedCount = 12L,
      )
    assertTrue(GameCatalog.evaluateThemeRequirement(AppColorTheme.STARLIGHT_LANTERN, qualifiedProfile).first)
    assertTrue(GameCatalog.evaluateThemeRequirement(AppColorTheme.CRIMSON_BLOODMOON, qualifiedProfile).first)
    assertTrue(GameCatalog.evaluateThemeRequirement(AppColorTheme.AURORA_FROST, qualifiedProfile).first)
    // Daily reward themes cannot be bought in event shop
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.ROSE_GOLD, qualifiedProfile).first)
  }

  @Test
  fun thanksgivingPartTwo_replacesStarlightWithHourlyRegularAndGravyRushFestivals() {
    assertEquals(1, GameCatalog.periodicEvents.size)
    assertEquals("thanksgiving_part_2", GameCatalog.periodicEvents.first().id)
    assertEquals("thanksgiving_part_2", GameCatalog.currentPeriodicEvent().first.id)
    assertTrue(GameCatalog.thanksgivingSneakPeek.dateWindow.contains("LIVE", ignoreCase = true))
    assertTrue(GameCatalog.thanksgivingSneakPeek.previewHighlights.isNotEmpty())

    // Hourly Festival tiers: Part 2 Regular (1.75x, 25% spawn) vs Gravy Rush (3.0x, 45% spawn)
    val regular = GameCatalog.festivalTierFor(isMega = false)
    val mega = GameCatalog.festivalTierFor(isMega = true)
    assertEquals(FestivalTier.REGULAR, regular)
    assertEquals(1.75, regular.clickMultiplier, 0.001)
    assertEquals(1.75, regular.ppsMultiplier, 0.001)
    assertEquals(25, regular.turkeySpawnChancePercent)
    assertEquals(FestivalTier.MEGA, mega)
    assertEquals(3.0, mega.clickMultiplier, 0.001)
    assertEquals(3.0, mega.ppsMultiplier, 0.001)
    assertEquals(45, mega.turkeySpawnChancePercent)

    // Seeded Mega Festival roll yields ~33% Gravy Rush (exact 33% threshold)
    val rng = Random(2026)
    var megaCount = 0
    repeat(1000) {
      if (GameCatalog.rollHourlyFestivalIsMega(rng)) megaCount++
    }
    assertTrue("Expected ~33% mega festivals, got $megaCount", megaCount in 280..380)

    // Turkey & Golden Gobbler spawn probability increases from Regular to Gravy Rush
    val spawnRngA = Random(77)
    val spawnRngB = Random(77)
    var regSpawns = 0
    var megaSpawns = 0
    var goldenGravySpawns = 0
    val goldenRng = Random(99)
    repeat(500) {
      if (GameCatalog.shouldSpawnTurkey(FestivalTier.REGULAR, spawnRngA)) regSpawns++
      if (GameCatalog.shouldSpawnTurkey(FestivalTier.MEGA, spawnRngB)) megaSpawns++
      if (GameCatalog.shouldSpawnGoldenGobbler(FestivalTier.MEGA, goldenRng)) goldenGravySpawns++
    }
    assertTrue(regSpawns in 95..165)
    assertTrue(megaSpawns > regSpawns)
    assertTrue(megaSpawns in 185..265)
    assertTrue(goldenGravySpawns in 135..215)
    assertEquals(3L, GameCatalog.GOLDEN_GOBBLER_REWARD)

    // Part 1 + Part 2 Turkey-only cosmetics, themes & backgrounds cost strictly Turkeys
    assertEquals(5, GameCatalog.turkeyEventPatterns.size)
    assertEquals(5, GameCatalog.turkeyEventAccessories.size)
    assertEquals(3, AppColorTheme.turkeyEventThemes.size)
    assertEquals(4, GameCatalog.backgrounds.count { it.turkeyCost > 0L })
    assertTrue(GameCatalog.turkeyEventPatterns.all { it.cost == 0L && it.turkeyCost > 0L })
    assertTrue(GameCatalog.turkeyEventAccessories.all { it.cost == 0L && it.turkeyCost > 0L })
    assertTrue(AppColorTheme.turkeyEventThemes.all { it.eventCost == 0L && it.turkeyCost > 0L })
    assertTrue(GameCatalog.isEventItemExemptPattern("pumpkin_pie_tabby"))
    assertTrue(GameCatalog.isEventItemExemptPattern("roasted_crown_calico"))
    assertTrue(GameCatalog.isEventItemExemptPattern("velvet_gravy_tabby"))
    assertTrue(GameCatalog.isEventItemExemptAccessory("pilgrim_hat"))
    assertTrue(GameCatalog.isEventItemExemptAccessory("cornucopia_horn"))
    assertTrue(GameCatalog.isEventItemExemptAccessory("gravy_ladle"))
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.AUTUMN_CRANBERRY, UserProfile()).first)
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.GOLDEN_HARVEST, UserProfile()).first)
    assertFalse(GameCatalog.evaluateThemeRequirement(AppColorTheme.CORNUCOPIA_ROYALE, UserProfile()).first)

    // Part 2 Sovereign badge quest
    val part2Quest = GameCatalog.quests.find { it.id == "q_part2_feast" }
    assertTrue(part2Quest != null && part2Quest.badgeId == "part2_feast_sovereign")
    assertEquals(
      15L,
      part2Quest!!.progressExtractor(UserProfile(), ChallengeProgressState(totalTurkeysEarned = 15L), PlayerStatsState()),
    )

    // Turkey Gato NPC talks unlock 5 Part 2 Map landmarks + Hourly Festival Quests track & award Turkeys
    assertTrue(GameCatalog.turkeyGatoDialogues.size > GameCatalog.TURKEY_GATO_MAP_TALKS)
    assertEquals(5, GameCatalog.part2MapLandmarks.size)
    val questCheck =
      GameCatalog.evaluateHourlyFestivalQuests(
        ChallengeProgressState(
          festHourPets = 46L,
          festHourTurkeysTapped = 2L,
          claimedFestivalQuestIds = listOf("tq_pets_35"),
        )
      )
    assertEquals(5, questCheck.size)
    val warmup = questCheck.first { it.template.id == "tq_pets_35" }
    val spotter = questCheck.first { it.template.id == "tq_turkeys_3" }
    assertTrue(warmup.isCompleted && warmup.isClaimed)
    assertTrue(spotter.isCompleted && !spotter.isClaimed)

    // 5-Minute Turkey Spawn Rush window per hourly reroll
    assertEquals(5L * 60L * 1000L, GameCatalog.TURKEY_SPAWN_WINDOW_MS)
    assertTrue(GameCatalog.isTurkeySpawnWindowActive(0L))
    assertEquals(GameCatalog.TURKEY_SPAWN_WINDOW_MS, GameCatalog.turkeySpawnWindowRemainingMillis(0L))
    assertTrue(GameCatalog.isTurkeySpawnWindowActive(299_999L))
    assertEquals(1L, GameCatalog.turkeySpawnWindowRemainingMillis(299_999L))
    assertFalse(GameCatalog.isTurkeySpawnWindowActive(300_000L))
    assertEquals(0L, GameCatalog.turkeySpawnWindowRemainingMillis(300_000L))
    assertFalse(GameCatalog.isTurkeySpawnWindowActive(3_599_999L))
    assertTrue(GameCatalog.isTurkeySpawnWindowActive(3_600_000L))
    assertTrue(GameCatalog.turkeyRushStatusText(60_000L).contains("spawning", ignoreCase = true))
    assertTrue(GameCatalog.turkeyRushStatusText(600_000L).contains("Next", ignoreCase = true))

    // Strict Event Separation in Inventory (Starlight Catnip archive never mixes with Thanksgiving)
    assertEquals(
      InventoryEventGroup.THANKSGIVING_PART_1,
      InventoryEventGroup.fromSourceLabel("Thanksgiving Part 1"),
    )
    assertEquals(
      InventoryEventGroup.THANKSGIVING_PART_1,
      InventoryEventGroup.fromSourceLabel("Thanksgiving Part 2"),
    )
    assertEquals(
      InventoryEventGroup.STARLIGHT_CATNIP,
      InventoryEventGroup.fromSourceLabel("Limited Event"),
    )
    assertEquals(
      InventoryEventGroup.NOVEMBER_PASS,
      InventoryEventGroup.fromSourceLabel("November Pass VIP"),
    )
    assertEquals(
      InventoryEventGroup.GEM_VAULT,
      InventoryEventGroup.fromSourceLabel("Gem Vault"),
    )
    assertEquals(
      InventoryEventGroup.DAILY_STREAKS,
      InventoryEventGroup.fromSourceLabel("Day 7 Prize"),
    )
    assertEquals(
      InventoryEventGroup.SECRET_VAULT,
      InventoryEventGroup.fromSourceLabel("Secret Code Vault"),
    )
    assertNotEquals(
      InventoryEventGroup.fromSourceLabel("Thanksgiving Part 1"),
      InventoryEventGroup.fromSourceLabel("Limited Event"),
    )
  }

  @Test
  fun hardcoreModeAndRebirthRules_enforceRequirementsDoublingAndEventExemptions() {
    val starter = UserProfile(totalPurrsEarned = 10_000L, totalPets = 100L)
    assertFalse(GameCatalog.meetsHardcoreRequirements(starter, rebirthCount = 0L))

    val veteran =
      UserProfile(
        totalPurrsEarned = 1_500_000L,
        totalPets = 6_000L,
        upgradeLevels = mapOf("feather_wand" to 50L),
        unlockedBadgeIds = listOf("b1", "b2", "b3", "b4", "b5"),
      )
    assertTrue(GameCatalog.meetsHardcoreRequirements(veteran, rebirthCount = 0L))

    val upgrade = GameCatalog.upgrades.first()
    val normalCost = GameCatalog.effectiveUpgradeCost(upgrade, 3L, isHardcore = false)
    val hardcoreCost = GameCatalog.effectiveUpgradeCost(upgrade, 3L, isHardcore = true)
    assertTrue(hardcoreCost >= (normalCost * 3L))

    assertEquals(100_000L, GameCatalog.rebirthCost(0L, isHardcore = false))
    assertEquals(200_000L, GameCatalog.rebirthCost(1L, isHardcore = false))
    assertEquals(400_000L, GameCatalog.rebirthCost(2L, isHardcore = false))
    assertEquals(200_000L, GameCatalog.rebirthCost(0L, isHardcore = true))

    assertEquals(1.0, GameCatalog.rebirthMultiplier(0L), 0.001)
    assertEquals(1.25, GameCatalog.rebirthMultiplier(1L), 0.001)
    assertEquals(1.5, GameCatalog.rebirthMultiplier(2L), 0.001)

    assertTrue(GameCatalog.isEventItemExemptPattern("lantern_koi"))
    assertTrue(GameCatalog.isEventItemExemptPattern("lunar_eclipse"))
    assertFalse(GameCatalog.isEventItemExemptPattern("siamese"))
    assertFalse(GameCatalog.isEventItemExemptPattern("cinnamon_swirl"))
    assertTrue(GameCatalog.isEventItemExemptAccessory("paper_lantern"))
    assertFalse(GameCatalog.isEventItemExemptAccessory("crown"))
  }

  @Test
  fun gatoAscensionAndAscensionTree_enforceFiveRebirthsPrerequisitesAndDiscounts() {
    assertEquals(5L, GameCatalog.ASCENSION_REQUIRED_REBIRTHS)
    assertEquals(1L, GameCatalog.ASCENSION_RESET_REBIRTH_COUNT)
    assertEquals(12, GameCatalog.ascensionUpgrades.size)
    val expectedNewNodes =
      listOf(
        "asc_gem_seeker",
        "asc_offline_mastery",
        "asc_pass_scholar",
        "asc_click_supernova",
        "asc_passive_symphony",
        "asc_cosmic_sovereign",
      )
    expectedNewNodes.forEach { id ->
      assertTrue("Missing Ring 2 node $id", GameCatalog.ascensionUpgrades.any { it.id == id })
    }

    // Multiplier resets to 1.0x (0 bonus steps) even after Rebirths reset to 1
    assertEquals(1.0, GameCatalog.rebirthMultiplier(rebirthBonusSteps = 0L), 0.001)
    // With Eternal Rebirth Echo, each Rebirth bonus step gives +0.45x instead of +0.25x
    assertEquals(
      1.9,
      GameCatalog.rebirthMultiplier(
        rebirthBonusSteps = 2L,
        unlockedAscensionIds = listOf("asc_rebirth_echo"),
      ),
      0.001,
    )

    val goldenNode = GameCatalog.ascensionUpgrades.first { it.id == "asc_golden_whiskers" }
    val bargainNode = GameCatalog.ascensionUpgrades.first { it.id == "asc_bargain_paws" }
    val capstoneNode = GameCatalog.ascensionUpgrades.first { it.id == "asc_rebirth_echo" }

    // Cannot unlock without shards or prerequisites
    val emptyChallenge = ChallengeProgressState(gatoShards = 0L)
    assertFalse(GameCatalog.canUnlockAscensionNode(goldenNode, emptyChallenge).first)
    assertFalse(
      GameCatalog.canUnlockAscensionNode(bargainNode, ChallengeProgressState(gatoShards = 5L)).first
    )

    // Unlocking Tier 1 enables Tier 2 when shards are available
    val afterTier1 =
      ChallengeProgressState(
        gatoShards = 1L,
        unlockedAscensionIds = listOf("asc_golden_whiskers"),
      )
    assertFalse(GameCatalog.canUnlockAscensionNode(goldenNode, afterTier1).first)
    assertTrue(GameCatalog.canUnlockAscensionNode(bargainNode, afterTier1).first)

    // Capstone unlocks once a Tier 3 prerequisite is owned and 3 Shards exist
    val readyForCapstone =
      ChallengeProgressState(
        gatoShards = 3L,
        unlockedAscensionIds = listOf("asc_golden_whiskers", "asc_bargain_paws", "asc_lucky_catnip"),
      )
    assertTrue(GameCatalog.canUnlockAscensionNode(capstoneNode, readyForCapstone).first)

    // Celestial Bargain applies -25% discount to both Standard and Hardcore prices
    val upgrade = GameCatalog.upgrades.first()
    val fullStandard = GameCatalog.effectiveUpgradeCost(upgrade, 4L, isHardcore = false)
    val discountedStandard =
      GameCatalog.effectiveUpgradeCost(
        upgrade,
        4L,
        isHardcore = false,
        unlockedAscensionIds = listOf("asc_bargain_paws"),
      )
    assertEquals(kotlin.math.round(fullStandard * 0.75).toLong(), discountedStandard)

    val fullItem = GameCatalog.effectiveItemCost(10_000L, isHardcore = true)
    val discountedItem =
      GameCatalog.effectiveItemCost(
        10_000L,
        isHardcore = true,
        unlockedAscensionIds = listOf("asc_bargain_paws"),
      )
    assertTrue(discountedItem < fullItem)
    assertEquals(kotlin.math.round(fullItem * 0.75).toLong(), discountedItem)
  }

  @Test
  fun hardcorePurrsCurrency_staysIsolatedFromStandardProfileAndUpgrades() {
    val standardProfile =
      UserProfile(purrs = 500_000L, upgradeLevels = mapOf("feather_wand" to 10L))
    val challengeState =
      ChallengeProgressState(
        isHardcoreMode = true,
        hardcoreUnlocked = true,
        hardcorePurrs = 250L,
        hardcoreUpgradeLevels = mapOf("feather_wand" to 2L),
      )

    val standardClick = GameCatalog.computeClickPower(standardProfile)
    val hardcoreClick = GameCatalog.computeClickPower(challengeState.hardcoreUpgradeLevels)
    assertEquals(11L, standardClick)
    assertEquals(3L, hardcoreClick)
    assertNotEquals(standardProfile.purrs, challengeState.hardcorePurrs)
  }

  @Test
  fun themesAndAudioCatalog_provideCustomAndBuiltInSafety() {
    assertEquals(3, ThemeMode.entries.size)
    assertTrue(MusicTrack.entries.size >= 4)

    val custom = CustomMusicTrack(id = "custom_1", title = "My Song", filePath = "/nonexistent/song.mp3")
    val soundManager = SoundManager()
    soundManager.configureAudio(
      sfxEnabled = true,
      sfxVolume = 0.8f,
      musicEnabled = false,
      musicVolume = 0.5f,
      trackId = custom.id,
      customTracks = listOf(custom),
    )
    soundManager.playSpecificSfx(CatSoundEffect.DEEP_RUMBLE_PURR)
    soundManager.playSpecificSfx(CatSoundEffect.COZY_PURR)
    soundManager.playCatTapSound(isComboCrit = false)
    soundManager.playCatTapSound(isComboCrit = true)
    soundManager.release()
  }

  @Test
  fun endlessDailyStreaks_scaleForeverWithCycleMultipliersAndDay30Shards() {
    val day1 = GameCatalog.computeEndlessDailyPayout(1L)
    val day15 = GameCatalog.computeEndlessDailyPayout(15L)
    val day30 = GameCatalog.computeEndlessDailyPayout(30L)
    val day100 = GameCatalog.computeEndlessDailyPayout(100L)

    assertEquals(1L, day1.cycleIndex)
    assertEquals(1.0, day1.streakMultiplier, 0.001)
    assertEquals(2L, day15.cycleIndex)
    assertEquals(1, day15.slotDay)
    assertEquals(1.5, day15.streakMultiplier, 0.001)
    assertTrue(day15.totalPurrs > day1.totalPurrs)

    assertEquals(3L, day30.cycleIndex)
    assertEquals(1L, day30.bonusShards)
    assertTrue(day100.totalPurrs > day30.totalPurrs)
    assertTrue(GameCatalog.computeEndlessDailyPayout(21L).bonusUpgradeLevel)
  }

  @Test
  fun weeklyLeaderboardRewardsAndOfflineEarnings_computeAccurately() {
    val rank1 = GameCatalog.weeklyTierForRank(1)
    val rank3 = GameCatalog.weeklyTierForRank(3)
    val rank8 = GameCatalog.weeklyTierForRank(8)
    val rank18 = GameCatalog.weeklyTierForRank(18)
    val unranked = GameCatalog.weeklyTierForRank(null)

    assertEquals(250_000L, rank1.purrsReward)
    assertEquals(2L, rank1.shardsReward)
    assertEquals(1L, rank3.shardsReward)
    assertTrue(rank8.bonusUpgradeLevels >= 1L)
    assertEquals(15_000L, rank18.purrsReward)
    assertEquals(5_000L, unranked.purrsReward)

    // Offline threshold & efficiency checks
    assertEquals(null, GameCatalog.computeOfflineEarnings(5_000L, 100L, isHardcore = false, hasAscended = false))

    val twoMinStandard =
      GameCatalog.computeOfflineEarnings(120_000L, 100L, isHardcore = false, hasAscended = false)!!
    assertEquals(120L, twoMinStandard.cappedSeconds)
    assertEquals(50, twoMinStandard.efficiencyPercent)
    assertEquals(6_000L, twoMinStandard.purrsEarned)

    // 40h away caps cleanly at 24h (86,400s), and ascended increases efficiency to 75%
    val fortyHoursAscended =
      GameCatalog.computeOfflineEarnings(
        40L * 3600L * 1000L,
        10L,
        isHardcore = true,
        hasAscended = true,
      )!!
    assertEquals(86_400L, fortyHoursAscended.cappedSeconds)
    assertEquals(75, fortyHoursAscended.efficiencyPercent)
    assertTrue(fortyHoursAscended.isHardcore)
    assertEquals(648_000L, fortyHoursAscended.purrsEarned)

    // With Dream Weaver (asc_offline_mastery): 90% efficiency & 48h cap (172,800s)
    val fiftyHoursMastery =
      GameCatalog.computeOfflineEarnings(
        50L * 3600L * 1000L,
        10L,
        isHardcore = false,
        hasAscended = true,
        unlockedAscensionIds = listOf("asc_offline_mastery"),
      )!!
    assertEquals(172_800L, fiftyHoursMastery.cappedSeconds)
    assertEquals(90, fiftyHoursMastery.efficiencyPercent)
    assertEquals(1_555_200L, fiftyHoursMastery.purrsEarned)

    assertEquals("2h 5m 10s", GameCatalog.formatElapsedDuration((2 * 3600 + 5 * 60 + 10) * 1000L))
  }

  @Test
  fun novemberPass_has30TiersVipRequirementsGoldenTouchAndRebirthExemptions() {
    assertEquals(30, GameCatalog.novemberPassTiers.size)
    val tier30 = GameCatalog.novemberPassTiers.last()
    assertEquals(30, tier30.tier)
    assertEquals(PassRewardKind.SKIN, tier30.freeReward.kind)
    assertEquals(GameCatalog.NOVEMBER_FREE_SKIN_ID, tier30.freeReward.itemId)
    assertEquals(PassRewardKind.SKIN, tier30.vipReward.kind)
    assertEquals(GameCatalog.GOLDEN_TOUCH_SKIN_ID, tier30.vipReward.itemId)

    // Nerfed Progressive XP Scaling: Free (100 + 5%/tier) & VIP (500 + 15%/tier)
    assertEquals(100L, GameCatalog.freeStepXpForTier(1))
    assertEquals(105L, GameCatalog.freeStepXpForTier(2))
    assertEquals(110L, GameCatalog.freeStepXpForTier(3))
    assertTrue(GameCatalog.freeStepXpForTier(30) > GameCatalog.freeStepXpForTier(15))

    assertEquals(500L, GameCatalog.vipStepXpForTier(1))
    assertEquals(575L, GameCatalog.vipStepXpForTier(2))
    assertEquals(661L, GameCatalog.vipStepXpForTier(3))
    assertTrue(GameCatalog.vipStepXpForTier(30) > GameCatalog.freeStepXpForTier(30))

    assertEquals(0, GameCatalog.freeTierFromXp(0L))
    assertEquals(1, GameCatalog.freeTierFromXp(100L))
    assertEquals(30, GameCatalog.freeTierFromXp(GameCatalog.freeTotalXpForTier(30)))

    assertEquals(0, GameCatalog.vipTierFromXp(499L))
    assertEquals(1, GameCatalog.vipTierFromXp(500L))
    assertEquals(2, GameCatalog.vipTierFromXp(500L + 575L))
    assertEquals(30, GameCatalog.vipTierFromXp(GameCatalog.vipTotalXpForTier(30)))

    // November Pass completion check respects whether VIP is unlocked
    assertFalse(GameCatalog.isNovemberPassCompleted(BattlePassState(passXp = 0L, vipUnlocked = false)))
    assertTrue(
      GameCatalog.isNovemberPassCompleted(
        BattlePassState(passXp = GameCatalog.freeTotalXpForTier(30), vipUnlocked = false)
      )
    )
    assertFalse(
      GameCatalog.isNovemberPassCompleted(
        BattlePassState(passXp = GameCatalog.freeTotalXpForTier(30), vipUnlocked = true)
      )
    )
    assertTrue(
      GameCatalog.isNovemberPassCompleted(
        BattlePassState(passXp = GameCatalog.vipTotalXpForTier(30), vipUnlocked = true)
      )
    )

    // Hourly Battle Pass Quests refresh deterministically per epoch hour & scale with tier
    val hourA = 500_000L
    val hourIds = GameCatalog.selectHourlyPassQuestIds(hourA)
    assertEquals(3, hourIds.size)
    assertEquals(3, hourIds.distinct().size)
    val earlyPass =
      BattlePassState(
        questHourEpoch = hourA,
        activeQuestIds = hourIds,
        passXp = 0L,
        hourPets = 200L,
        hourPurrsEarned = 50_000L,
        hourUpgradesBought = 10L,
        hourWeatherShifts = 10L,
        hourComboCrits = 25L,
      )
    val earlyEvaluated = GameCatalog.evaluateHourlyPassQuests(earlyPass)
    assertEquals(3, earlyEvaluated.size)
    assertTrue(earlyEvaluated.all { it.isCompleted })

    val lateVipPass = earlyPass.copy(vipUnlocked = true, passXp = 50_000L)
    val lateEvaluated = GameCatalog.evaluateHourlyPassQuests(lateVipPass)
    assertTrue(lateEvaluated.first().xpReward > earlyEvaluated.first().xpReward)

    // Inventory vs Customization separation: limited/pass items only in Inventory
    val standardPatterns = GameCatalog.patterns.filter { !it.isEventExclusive }
    val inventoryPatterns = GameCatalog.patterns.filter { it.isEventExclusive }
    assertFalse(standardPatterns.any { it.id == GameCatalog.NOVEMBER_FREE_SKIN_ID })
    assertFalse(standardPatterns.any { it.id == GameCatalog.GOLDEN_TOUCH_SKIN_ID })
    assertTrue(inventoryPatterns.any { it.id == GameCatalog.NOVEMBER_FREE_SKIN_ID })
    assertTrue(inventoryPatterns.any { it.id == GameCatalog.GOLDEN_TOUCH_SKIN_ID })

    val standardAccessories = GameCatalog.accessories.filter { !it.isEventExclusive }
    val inventoryAccessories = GameCatalog.accessories.filter { it.isEventExclusive }
    assertFalse(standardAccessories.any { it.id == "golden_collar" })
    assertTrue(inventoryAccessories.any { it.id == "golden_collar" })

    // VIP requires BOTH 1 Gato Shard and 100+ Upgrades
    val onlyShard =
      GameCatalog.meetsNovemberVipRequirements(
        profile = UserProfile(upgradeLevels = mapOf("feather_wand" to 99L)),
        challenge = ChallengeProgressState(gatoShards = 1L),
        stats = PlayerStatsState(totalUpgradesPurchased = 99L),
      )
    assertFalse(onlyShard.first)

    val onlyUpgrades =
      GameCatalog.meetsNovemberVipRequirements(
        profile = UserProfile(upgradeLevels = mapOf("feather_wand" to 120L)),
        challenge = ChallengeProgressState(gatoShards = 0L),
        stats = PlayerStatsState(totalUpgradesPurchased = 120L),
      )
    assertFalse(onlyUpgrades.first)

    val qualifiedVip =
      GameCatalog.meetsNovemberVipRequirements(
        profile = UserProfile(upgradeLevels = mapOf("feather_wand" to 100L)),
        challenge = ChallengeProgressState(gatoShards = 1L),
        stats = PlayerStatsState(totalUpgradesPurchased = 100L),
      )
    assertTrue(qualifiedVip.first)

    // Golden Touch 30m equip -> 3m Golden Rain 2.0x
    assertEquals(30L * 60L * 1000L, GameCatalog.GOLDEN_TOUCH_INTERVAL_MS)
    assertEquals(3L * 60L * 1000L, GameCatalog.GOLDEN_RAIN_DURATION_MS)
    assertEquals(2.0, GameCatalog.GOLDEN_RAIN_MULTIPLIER, 0.001)

    // Rebirth & Ascension preserve November Pass skins & VIP accessories
    assertTrue(GameCatalog.isEventItemExemptPattern(GameCatalog.NOVEMBER_FREE_SKIN_ID))
    assertTrue(GameCatalog.isEventItemExemptPattern(GameCatalog.GOLDEN_TOUCH_SKIN_ID))
    assertTrue(GameCatalog.isEventItemExemptAccessory("harvest_beret"))
    assertTrue(GameCatalog.isEventItemExemptAccessory("golden_collar"))
  }

  @Test
  fun rareGemsEconomy_rewardsScalingCharmFrenzyAndExemptCosmetics() {
    // Rebalanced Rare Gem drop rates, Rebirth, Ascension, Frenzy & Charm constants
    assertEquals(2, GameCatalog.GEM_DROP_RATE_NORMAL_PER_1000)
    assertEquals(4, GameCatalog.GEM_DROP_RATE_BOOSTED_PER_1000)
    assertEquals(2, GameCatalog.gemDropRatePer1000(boostedSky = false, unlockedAscensionIds = emptyList()))
    assertEquals(4, GameCatalog.gemDropRatePer1000(boostedSky = true, unlockedAscensionIds = emptyList()))
    assertEquals(3, GameCatalog.gemDropRatePer1000(boostedSky = false, unlockedAscensionIds = listOf("asc_gem_seeker")))
    assertEquals(6, GameCatalog.gemDropRatePer1000(boostedSky = true, unlockedAscensionIds = listOf("asc_gem_seeker")))
    assertEquals(1L, GameCatalog.REBIRTH_GEM_REWARD)
    assertEquals(4L, GameCatalog.ASCENSION_GEM_REWARD)
    assertEquals(1.85, GameCatalog.GEM_FRENZY_MULTIPLIER, 0.001)
    assertEquals(120_000L, GameCatalog.GEM_FRENZY_DURATION_MS)
    assertEquals(4L, GameCatalog.GEM_FRENZY_COST)

    assertEquals(5L, GameCatalog.gemCharmCost(0L))
    assertEquals(8L, GameCatalog.gemCharmCost(1L))
    assertEquals(11L, GameCatalog.gemCharmCost(2L))
    assertEquals(1.0, GameCatalog.gemCharmMultiplier(0L), 0.001)
    assertEquals(1.2, GameCatalog.gemCharmMultiplier(2L), 0.001)

    // Mid/Late Quests, Top-25 Weekly tiers, Endless Streaks (Day 7 & Day 14), and Pass Tiers grant Rare Gems
    assertEquals(0L, GameCatalog.quests.first { it.id == "q_first_pets" }.rewardGems)
    assertTrue(GameCatalog.quests.count { it.rewardGems >= 1L } >= 10)
    assertTrue(GameCatalog.weeklyLeaderboardTiers.filter { it.maxRankInclusive <= 25 }.all { it.gemsReward >= 1L })
    assertEquals(0L, GameCatalog.weeklyTierForRank(null).gemsReward)
    assertEquals(6L, GameCatalog.weeklyTierForRank(1).gemsReward)

    val day7Payout = GameCatalog.computeEndlessDailyPayout(7L)
    assertEquals(1L, day7Payout.bonusGems)
    val day14Payout = GameCatalog.computeEndlessDailyPayout(14L)
    assertEquals(2L, day14Payout.bonusGems)
    val day28Payout = GameCatalog.computeEndlessDailyPayout(28L)
    assertEquals(3L, day28Payout.bonusGems)

    val tier10 = GameCatalog.novemberPassTiers.first { it.tier == 10 }
    val tier15 = GameCatalog.novemberPassTiers.first { it.tier == 15 }
    assertEquals(PassRewardKind.GEM, tier10.freeReward.kind)
    assertEquals(PassRewardKind.GEM, tier15.vipReward.kind)

    // Exclusive Gem Vault cosmetics & theme exist and are preserved across Rebirths
    val prismSkin = GameCatalog.patterns.find { it.id == "prism_galaxy" }
    assertTrue(prismSkin != null && prismSkin.gemCost == 20L)
    val diamondCrown = GameCatalog.accessories.find { it.id == "diamond_crown" }
    assertTrue(diamondCrown != null && diamondCrown.gemCost == 12L)
    assertTrue(GameCatalog.isEventItemExemptPattern("prism_galaxy"))
    assertTrue(GameCatalog.isEventItemExemptAccessory("diamond_crown"))
    assertTrue(AppColorTheme.EMERALD_ROYALE in AppColorTheme.gemThemes)
    assertEquals(15L, AppColorTheme.EMERALD_ROYALE.gemCost)
  }

  @Test
  fun updateBlogPosts_includeV14AndFullArticleReaderParagraphs() {
    assertTrue(GameCatalog.blogPosts.isNotEmpty())
    val latest = GameCatalog.blogPosts.first()
    assertEquals("v23.0", latest.version)
    assertTrue(GameCatalog.blogPosts.any { it.version == "v20.0" })
    assertTrue(GameCatalog.blogPosts.any { it.version == "v15.0" })
    assertTrue(GameCatalog.blogPosts.any { it.version == "v14.0" })
    assertTrue(GameCatalog.blogPosts.any { it.version == "v13.0" })
    assertTrue(GameCatalog.blogPosts.any { it.version == "v12.0" })
    assertTrue(GameCatalog.blogPosts.any { it.version == "v11.0" })
    assertTrue(GameCatalog.blogPosts.any { it.version == "v9.0" })
    assertTrue(GameCatalog.blogPosts.all { it.fullArticleParagraphs.isNotEmpty() })
    assertTrue(GameCatalog.blogPosts.all { it.readTimeLabel.isNotBlank() && it.authorTag.isNotBlank() })

    assertEquals(NavigationLayoutFormat.BOTTOM_BAR, NavigationLayoutFormat.fromId("unknown"))
    assertEquals(NavigationLayoutFormat.SIDE_RAIL, NavigationLayoutFormat.fromId("side_rail"))
    assertEquals(NavigationLayoutFormat.CLEAN_FLOATING, NavigationLayoutFormat.fromId("clean_floating"))
  }

  @Test
  fun v11Features_adminEmailRepeatModesAndLimitedExistCatalog_validateAccurately() {
    assertTrue(GameCatalog.isAdminEmail("silvestrecastaneda9@gmail.com"))
    assertTrue(GameCatalog.isAdminEmail("  SilvestreCastaneda9@GMAIL.com "))
    assertFalse(GameCatalog.isAdminEmail("other@gmail.com"))
    assertFalse(GameCatalog.isAdminEmail(null))

    assertEquals(RepeatMode.ALL, RepeatMode.OFF.next())
    assertEquals(RepeatMode.ONE, RepeatMode.ALL.next())
    assertEquals(RepeatMode.OFF, RepeatMode.ONE.next())

    val limitedRefs = GameCatalog.limitedTimeCatalogRefs()
    assertTrue(limitedRefs.any { it.first == "pat_golden_touch_cat" && it.second == "PATTERN" })
    assertTrue(limitedRefs.any { it.first == "pat_roasted_crown_calico" && it.second == "PATTERN" })
    assertTrue(limitedRefs.any { it.first == "acc_diamond_crown" && it.second == "ACCESSORY" })
    assertTrue(limitedRefs.any { it.first == "acc_cornucopia_horn" && it.second == "ACCESSORY" })
    assertTrue(limitedRefs.any { it.first == "thm_hacker_protocol" && it.second == "THEME" })
    assertTrue(limitedRefs.any { it.first == "thm_cornucopia_royale" && it.second == "THEME" })
    assertTrue(limitedRefs.any { it.first == "bg_cornucopia_citadel" && it.second == "BACKGROUND" })
  }

  @Test
  fun newAchievements_evaluateGemsAscensionRebirthOfflineAndComboAccurately() {
    val newQuestIds =
      listOf("q_gems_10", "q_ascension_tree_3", "q_rebirth_5", "q_offline_1h", "q_combo_2x")
    val newBadgeIds =
      listOf("gem_collector", "ascension_scholar", "rebirth_veteran", "offline_dreamer", "frenzy_acrobat")

    newQuestIds.forEach { qId ->
      val q = GameCatalog.quests.find { it.id == qId }
      assertTrue("Missing quest $qId", q != null)
      assertTrue(q!!.badgeId in newBadgeIds)
    }

    val starterProfile = UserProfile()
    val emptyChallenge = ChallengeProgressState()
    val emptyStats = PlayerStatsState()

    newQuestIds.forEach { qId ->
      val q = GameCatalog.quests.first { it.id == qId }
      assertEquals(0L, q.progressExtractor(starterProfile, emptyChallenge, emptyStats))
    }

    val qualifiedChallenge =
      ChallengeProgressState(
        totalGemsEarned = 12L,
        unlockedAscensionIds = listOf("asc_golden_whiskers", "asc_bargain_paws", "asc_lucky_catnip"),
        rebirthCount = 5L,
      )
    val qualifiedStats =
      PlayerStatsState(
        totalOfflineSeconds = 4_000L,
        highestComboMultiplier = 2.25,
      )

    newQuestIds.forEach { qId ->
      val q = GameCatalog.quests.first { it.id == qId }
      val progress = q.progressExtractor(starterProfile, qualifiedChallenge, qualifiedStats)
      assertTrue("Expected $qId progress >= target", progress >= q.target)
    }
  }

  @Test
  fun secretCodeVault_dogKeeperKickoutDurationThemeAndAchievement_workAccurately() {
    assertEquals(7, GameCatalog.VAULT_KEEPER_KICKOUT_TAPS)
    assertEquals(3L * 60L * 1000L, GameCatalog.VAULT_KICKOUT_DURATION_MS)
    assertTrue(GameCatalog.vaultKeeperWarnings.isNotEmpty())
    assertTrue(
      GameCatalog.vaultKeeperWarnings.all {
        it.contains("World Of Cats", ignoreCase = true) ||
          it.contains("Hackers", ignoreCase = true)
      }
    )

    // Hacker Protocol theme is non-free and gated strictly behind the Code Vault
    assertFalse(AppColorTheme.HACKER_PROTOCOL.isFree)
    assertFalse(AppColorTheme.HACKER_PROTOCOL in AppColorTheme.eventThemes)
    assertFalse(AppColorTheme.HACKER_PROTOCOL in AppColorTheme.gemThemes)
    assertFalse(
      GameCatalog.evaluateThemeRequirement(AppColorTheme.HACKER_PROTOCOL, UserProfile()).first
    )

    // Vault Intruder badge & achievement evaluate hackerVaultUnlocked accurately
    val badge = GameCatalog.badges.find { it.id == "vault_hacker" }
    val quest = GameCatalog.quests.find { it.id == "q_vault_keeper" }
    assertTrue(badge != null && quest != null)
    assertEquals("vault_hacker", quest!!.badgeId)
    assertEquals(
      0L,
      quest.progressExtractor(UserProfile(), ChallengeProgressState(hackerVaultUnlocked = false), PlayerStatsState()),
    )
    assertEquals(
      1L,
      quest.progressExtractor(UserProfile(), ChallengeProgressState(hackerVaultUnlocked = true), PlayerStatsState()),
    )
  }

  @Test
  fun catComputer_passwordRiddleAndChallengeFlags_workAccurately() {
    assertTrue(GameCatalog.CAT_COMPUTER_RIDDLE.isNotBlank())
    assertTrue(GameCatalog.CAT_COMPUTER_HINT.isNotBlank())
    assertTrue(GameCatalog.isValidCatComputerPassword("cat"))
    assertTrue(GameCatalog.isValidCatComputerPassword("  CAT "))
    assertTrue(GameCatalog.isValidCatComputerPassword("Cats"))
    assertTrue(GameCatalog.isValidCatComputerPassword("Gato"))
    assertFalse(GameCatalog.isValidCatComputerPassword("dog"))
    assertFalse(GameCatalog.isValidCatComputerPassword(""))

    val defaultState = ChallengeProgressState()
    assertFalse(defaultState.catComputerUnlocked)
    assertFalse(defaultState.dogComputerSearched)
    assertFalse(defaultState.citadelBossGlitchSeen)
  }

  @Test
  fun part2MapZonesAndBossCutscene_validateAccurately() {
    val landmarks = GameCatalog.part2MapLandmarks
    assertEquals(5, landmarks.size)
    val byId = landmarks.associateBy { it.id }
    assertEquals(FeastZoneActivityType.MINIGAME_CRANBERRY, byId["cranberry_bog"]?.activityType)
    assertEquals(FeastZoneActivityType.MINIGAME_BAKERY, byId["pumpkin_bakery"]?.activityType)
    assertEquals(FeastZoneActivityType.FISHING, byId["gravy_falls"]?.activityType)
    assertEquals(FeastZoneActivityType.COOKING, byId["turkey_gato_hall"]?.activityType)
    assertEquals(FeastZoneActivityType.BOSS_BATTLE, byId["cornucopia_citadel"]?.activityType)
    assertTrue(landmarks.all { it.actionLabel.isNotBlank() && it.rewardSummary.isNotBlank() })

    val crown = GameCatalog.accessories.find { it.id == "melted_solstice_crown" }
    assertTrue(crown != null)
    assertEquals(0L, crown!!.cost)
    assertTrue(crown.isEventExclusive)
    assertEquals("December 1st Teaser", crown.unlockSourceLabel)
    assertTrue(GameCatalog.isEventItemExemptAccessory("melted_solstice_crown"))
    assertEquals(
      InventoryEventGroup.SOLSTICE_OMEN,
      InventoryEventGroup.fromSourceLabel(crown.unlockSourceLabel),
    )

    val limitedRefs = GameCatalog.limitedTimeCatalogRefs()
    assertTrue(limitedRefs.any { it.first == "acc_melted_solstice_crown" && it.second == "ACCESSORY" })
  }
}
