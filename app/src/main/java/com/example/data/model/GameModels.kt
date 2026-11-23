package com.example.data.model

import com.example.ui.theme.AppColorTheme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Cat Lover",
  val purrs: Long = 0L,
  val totalPurrsEarned: Long = 0L,
  val totalPets: Long = 0L,
  val selectedCatName: String = "Mochi",
  val selectedPatternId: String = "tabby",
  val selectedAccessoryId: String = "none",
  val selectedBackgroundId: String = "cozy_parlor",
  val unlockedPatterns: List<String> = listOf("tabby"),
  val unlockedAccessories: List<String> = listOf("none"),
  val unlockedBackgrounds: List<String> = listOf("cozy_parlor"),
  val upgradeLevels: Map<String, Long> = emptyMap(),
  val dailyStreak: Long = 0L,
  val lastDailyClaimEpochDay: Long = 0L,
  val claimedQuestIds: List<String> = emptyList(),
  val unlockedBadgeIds: List<String> = emptyList(),
  val equippedBadgeId: String = "",
  val eventPurrsEarned: Long = 0L,
  val preferredCity: String = "Sanctuary Sky",
  val weatherCheckedCount: Long = 0L,
  val statsPublic: Boolean = true,
  val bio: String = "",
  val avatarBase64: String = "",
  val bannerBase64: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class CatBackgroundDef(
  val id: String,
  val name: String,
  val description: String,
  val cost: Long,
  val primaryHex: Long,
  val secondaryHex: Long,
  val accentHex: Long,
  val isEventExclusive: Boolean = false,
  val turkeyCost: Long = 0L,
  val eventGroup: String? = null,
)

@IgnoreExtraProperties
data class LeaderboardEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "Cat Lover",
  val catName: String = "Mochi",
  val catPatternId: String = "tabby",
  val catAccessoryId: String = "none",
  val equippedBadgeId: String = "",
  val totalPurrsEarned: Long = 0L,
  val totalPets: Long = 0L,
  val badgesCount: Long = 0L,
  val statsPublic: Boolean = true,
  val bio: String = "",
  val avatarBase64: String = "",
  val bannerBase64: String = "",
  val publicRebirths: Long = 0L,
  val publicAscensions: Long = 0L,
  val publicDailyStreak: Long = 0L,
  val publicHighestComboX100: Long = 100L,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class LeaderboardPublicStats(
  val rebirths: Long = 0L,
  val ascensions: Long = 0L,
  val dailyStreak: Long = 0L,
  val highestComboX100: Long = 100L,
)

@IgnoreExtraProperties
data class ModerationRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val status: String = "ACTIVE", // ACTIVE, WARNED, TIMED_OUT, BANNED
  val reason: String = "",
  val timeoutUntilMillis: Long = 0L,
  val warningCount: Long = 0L,
  val updatedByEmail: String = "",
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class GlobalAnnouncement(
  @DocumentId val id: String = "",
  val messageId: String = "",
  val title: String = "",
  val body: String = "",
  val severity: String = "INFO", // INFO, EVENT, WARNING
  val senderEmail: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class LimitedItemCounter(
  @DocumentId val id: String = "",
  val itemId: String = "",
  val itemType: String = "PATTERN", // PATTERN, ACCESSORY, THEME
  val existCount: Long = 0L,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class UpgradeCategory {
  CLICK,
  PER_SECOND,
}

data class ShopUpgradeDef(
  val id: String,
  val name: String,
  val description: String,
  val category: UpgradeCategory,
  val baseCost: Long,
  val costMultiplier: Double,
  val powerPerLevel: Long,
) {
  fun costForLevel(currentLevel: Long): Long {
    return (baseCost * costMultiplier.pow(currentLevel.toDouble())).roundToLong().coerceAtLeast(1L)
  }
}

data class CatPatternDef(
  val id: String,
  val name: String,
  val description: String,
  val cost: Long,
  val gemCost: Long = 0L,
  val turkeyCost: Long = 0L,
  val isEventExclusive: Boolean = false,
  val unlockSourceLabel: String? = null,
)

data class CatAccessoryDef(
  val id: String,
  val name: String,
  val description: String,
  val cost: Long,
  val gemCost: Long = 0L,
  val turkeyCost: Long = 0L,
  val isEventExclusive: Boolean = false,
  val unlockSourceLabel: String? = null,
)

data class QuestDef(
  val id: String,
  val title: String,
  val description: String,
  val target: Long,
  val rewardPurrs: Long,
  val rewardGems: Long = 0L,
  val badgeId: String? = null,
  val progressExtractor: (UserProfile, ChallengeProgressState, PlayerStatsState) -> Long,
)

data class BadgeDef(
  val id: String,
  val name: String,
  val description: String,
  val rarity: String,
)

enum class DailyBonusPrizeType {
  NONE,
  PATTERN,
  ACCESSORY,
  COLOR_THEME,
  UPGRADE_LEVEL,
}

data class DailyRewardDef(
  val day: Int,
  val title: String,
  val purrs: Long,
  val bonusNote: String,
  val bonusType: DailyBonusPrizeType = DailyBonusPrizeType.NONE,
  val bonusItemId: String = "",
  val bonusPrizeLabel: String = "",
)

data class LimitedEventDef(
  val id: String,
  val title: String,
  val subtitle: String,
  val boostLabel: String,
  val clickMultiplier: Double,
  val ppsMultiplier: Double,
  val milestoneGoal: Long,
  val milestoneRewardPurrs: Long,
  val badgeId: String,
)

data class BlogPostDef(
  val version: String,
  val date: String,
  val title: String,
  val summary: String,
  val highlights: List<String>,
  val readTimeLabel: String = "2 min read",
  val authorTag: String = "Sanctuary Dev Team",
  val fullArticleParagraphs: List<String> = emptyList(),
)

data class FaqItemDef(
  val question: String,
  val answer: String,
)

enum class MoonlightPhase(
  val title: String,
  val subtitle: String,
  val boostLabel: String,
  val clickMultiplier: Double,
  val ppsMultiplier: Double,
) {
  INACTIVE(
    title = "Moonlight Event",
    subtitle = "Occurs Midnight until Morning (33% Bloodmoon chance)",
    boostLabel = "1.75x–2.5x Boost at 12:00 AM",
    clickMultiplier = 1.0,
    ppsMultiplier = 1.0,
  ),
  MOONLIGHT(
    title = "Moonlight Event",
    subtitle = "Silver moonbeams soothe your sanctuary until morning!",
    boostLabel = "1.75x Click & 1.75x Auto-Purr",
    clickMultiplier = 1.75,
    ppsMultiplier = 1.75,
  ),
  BLOODMOON(
    title = "Crimson Bloodmoon",
    subtitle = "Rare 33% Bloodmoon night! Celestial purrs surge until morning!",
    boostLabel = "2.5x Click & 2.5x Auto-Purr",
    clickMultiplier = 2.5,
    ppsMultiplier = 2.5,
  ),
}

data class MoonlightEventState(
  val phase: MoonlightPhase = MoonlightPhase.INACTIVE,
  val statusText: String = "Starts at Midnight",
)

data class ChallengeProgressState(
  val rebirthCount: Long = 0L,
  val rebirthBonusSteps: Long = 0L,
  val isHardcoreMode: Boolean = false,
  val hardcoreUnlocked: Boolean = false,
  val hardcorePurrs: Long = 0L,
  val hardcoreUpgradeLevels: Map<String, Long> = emptyMap(),
  val ascensionCount: Long = 0L,
  val gatoShards: Long = 0L,
  val unlockedAscensionIds: List<String> = emptyList(),
  val gems: Long = 0L,
  val totalGemsEarned: Long = 0L,
  val gemCharmLevel: Long = 0L,
  val prismLensUnlocked: Boolean = false,
  val gemFrenzyRemainingMillis: Long = 0L,
  val hackerVaultUnlocked: Boolean = false,
  val vaultKickoutUntilMillis: Long = 0L,
  val dogCrashOmenSeen: Boolean = false,
  val trillionCutsceneSeen: Boolean = false,
  val citadelBossGlitchSeen: Boolean = false,
  val catComputerUnlocked: Boolean = false,
  val dogComputerSearched: Boolean = false,
  val turkeys: Long = 0L,
  val totalTurkeysEarned: Long = 0L,
  val festivalHourEpoch: Long = 0L,
  val festivalIsMega: Boolean = false,
  val turkeyGatoTalkCount: Int = 0,
  val part2MapUnlocked: Boolean = false,
  val festivalQuestHourEpoch: Long = 0L,
  val claimedFestivalQuestIds: List<String> = emptyList(),
  val festHourPets: Long = 0L,
  val festHourTurkeysTapped: Long = 0L,
  val festHourUpgradesBought: Long = 0L,
  val festHourWeatherShifts: Long = 0L,
)

data class PlayerStatsState(
  val joinedTimestampMillis: Long = 0L,
  val lastActiveTimestampMillis: Long = 0L,
  val totalTimePlayedSeconds: Long = 0L,
  val totalOfflineSeconds: Long = 0L,
  val totalOfflinePurrsEarned: Long = 0L,
  val highestComboMultiplier: Double = 1.0,
  val biggestSingleTapPurrs: Long = 0L,
  val totalUpgradesPurchased: Long = 0L,
  val totalDailyRewardsClaimed: Long = 0L,
  val longestDailyStreak: Long = 0L,
  val lastWeeklyRewardWeekId: Long = 0L,
  val weeklyRewardsClaimedCount: Long = 0L,
  val totalWeeklyPurrsEarned: Long = 0L,
  val totalPlaySessions: Long = 1L,
)

data class OfflineEarningsReport(
  val elapsedMillis: Long,
  val cappedSeconds: Long,
  val purrsEarned: Long,
  val isHardcore: Boolean,
  val efficiencyPercent: Int,
  val ppsRateUsed: Long,
)

data class WeeklyLeaderboardTier(
  val rankLabel: String,
  val maxRankInclusive: Int,
  val purrsReward: Long,
  val shardsReward: Long,
  val bonusUpgradeLevels: Long,
  val gemsReward: Long = 0L,
  val title: String,
)

data class WeeklyRewardReceipt(
  val weekId: Long,
  val rankAchieved: Int?,
  val tierTitle: String,
  val purrsEarned: Long,
  val shardsEarned: Long,
  val gemsEarned: Long = 0L,
  val bonusUpgradeName: String?,
)

data class EndlessDailyPayout(
  val streakDay: Long,
  val cycleIndex: Long,
  val slotDay: Int,
  val baseReward: DailyRewardDef,
  val streakMultiplier: Double,
  val totalPurrs: Long,
  val bonusShards: Long,
  val bonusGems: Long = 0L,
  val bonusUpgradeLevel: Boolean,
)

data class AscensionUpgradeDef(
  val id: String,
  val tier: Int,
  val name: String,
  val description: String,
  val effectSummary: String,
  val shardCost: Long,
  val prerequisiteIds: List<String> = emptyList(),
)

data class EventSneakPeekDef(
  val title: String,
  val dateWindow: String,
  val summary: String,
  val previewHighlights: List<String>,
)

enum class PassRewardKind {
  PURRS,
  UPGRADE_LEVEL,
  GATO_SHARD,
  GEM,
  ACCESSORY,
  SKIN,
}

data class BattlePassRewardItem(
  val kind: PassRewardKind,
  val label: String,
  val amount: Long = 0L,
  val itemId: String = "",
)

data class BattlePassTierDef(
  val tier: Int,
  val freeReward: BattlePassRewardItem,
  val vipReward: BattlePassRewardItem,
)

enum class PassQuestKind {
  PETS,
  PURRS,
  UPGRADES,
  WEATHER_SHIFTS,
  COMBO_CRITS,
}

data class BattlePassQuestTemplate(
  val id: String,
  val title: String,
  val description: String,
  val kind: PassQuestKind,
  val target: Long,
  val baseXpReward: Long,
  val tierScaleFactor: Double,
)

data class ActiveBattlePassQuest(
  val template: BattlePassQuestTemplate,
  val progress: Long,
  val xpReward: Long,
  val isClaimed: Boolean,
) {
  val isCompleted: Boolean
    get() = progress >= template.target
}

enum class NavigationLayoutFormat(
  val id: String,
  val label: String,
  val subtitle: String,
) {
  BOTTOM_BAR("bottom_bar", "Bottom", "Classic bottom navigation bar"),
  SIDE_RAIL("side_rail", "Side Rail", "Vertical bar along the left edge"),
  CLEAN_FLOATING("clean_floating", "Clean Dock", "Compact floating pill bar");

  companion object {
    fun fromId(id: String): NavigationLayoutFormat {
      return entries.find { it.id == id } ?: BOTTOM_BAR
    }
  }
}

data class BattlePassState(
  val seasonId: String = "november_pass",
  val passXp: Long = 0L,
  val vipUnlocked: Boolean = false,
  val passCompletions: Long = 0L,
  val claimedFreeTiers: List<Int> = emptyList(),
  val claimedVipTiers: List<Int> = emptyList(),
  val goldenTouchEquippedMillis: Long = 0L,
  val goldenRainRemainingMillis: Long = 0L,
  val questHourEpoch: Long = 0L,
  val activeQuestIds: List<String> = emptyList(),
  val claimedPassQuestIds: List<String> = emptyList(),
  val hourPets: Long = 0L,
  val hourPurrsEarned: Long = 0L,
  val hourUpgradesBought: Long = 0L,
  val hourWeatherShifts: Long = 0L,
  val hourComboCrits: Long = 0L,
)

enum class FestivalTier(
  val id: String,
  val label: String,
  val shortBadge: String,
  val boostLabel: String,
  val clickMultiplier: Double,
  val ppsMultiplier: Double,
  val turkeySpawnChancePercent: Int,
) {
  REGULAR(
    id = "regular",
    label = "Thanksgiving Part 2 Festival",
    shortBadge = "PART 2 LIVE",
    boostLabel = "1.75x Purrs • 25% Turkey/10s • Golden Gobblers",
    clickMultiplier = 1.75,
    ppsMultiplier = 1.75,
    turkeySpawnChancePercent = 25,
  ),
  MEGA(
    id = "mega",
    label = "MEGA Gravy Rush Festival!",
    shortBadge = "33% GRAVY RUSH!",
    boostLabel = "3.0x Gravy Rush • 45% Turkey/10s • +3 Gobblers",
    clickMultiplier = 3.0,
    ppsMultiplier = 3.0,
    turkeySpawnChancePercent = 45,
  ),
}

enum class FestivalQuestKind {
  PETS,
  TURKEYS_TAPPED,
  UPGRADES,
  WEATHER_SHIFTS,
}

data class FestivalQuestTemplate(
  val id: String,
  val title: String,
  val description: String,
  val kind: FestivalQuestKind,
  val target: Long,
  val turkeyReward: Long,
)

data class ActiveFestivalQuest(
  val template: FestivalQuestTemplate,
  val progress: Long,
  val isClaimed: Boolean,
) {
  val isCompleted: Boolean
    get() = progress >= template.target
}

enum class FeastZoneActivityType {
  MINIGAME_CRANBERRY,
  MINIGAME_BAKERY,
  FISHING,
  COOKING,
  BOSS_BATTLE,
}

data class Part2MapLandmark(
  val id: String,
  val title: String,
  val subtitle: String,
  val xFraction: Float,
  val yFraction: Float,
  val previewDetail: String,
  val activityType: FeastZoneActivityType,
  val actionLabel: String,
  val rewardSummary: String,
)

enum class InventoryEventGroup(
  val sortOrder: Int,
  val title: String,
  val badgeLabel: String,
  val subtitle: String,
) {
  THANKSGIVING_PART_1(
    sortOrder = 0,
    title = "Thanksgiving Event (Part 1 & 2)",
    badgeLabel = "PART 2 LIVE • TURKEYS",
    subtitle = "Feast gear bought exclusively with wild & quest Turkeys.",
  ),
  SOLSTICE_OMEN(
    sortOrder = 1,
    title = "December 1st Glitch Omen",
    badgeLabel = "BOSS TEASER • LIMITED",
    subtitle = "Relics thawed from Cornucopia Citadel's frozen anomaly.",
  ),
  NOVEMBER_PASS(
    sortOrder = 2,
    title = "November Pass (Season 1)",
    badgeLabel = "BATTLE PASS",
    subtitle = "Rewards earned along the 30-tier Free & VIP Pass tracks.",
  ),
  GEM_VAULT(
    sortOrder = 3,
    title = "Gem Vault Relics",
    badgeLabel = "RARE GEMS",
    subtitle = "Luxury collectibles forged in Shop → Gems.",
  ),
  DAILY_STREAKS(
    sortOrder = 4,
    title = "Daily Check-In Streaks",
    badgeLabel = "CHECK-IN PRIZES",
    subtitle = "Milestone gifts from your Endless Daily Streak.",
  ),
  STARLIGHT_CATNIP(
    sortOrder = 5,
    title = "Starlight Catnip Event (Retired)",
    badgeLabel = "ARCHIVE",
    subtitle = "Legacy items preserved from the concluded Starlight Festival.",
  ),
  SECRET_VAULT(
    sortOrder = 6,
    title = "Secret Code Vault",
    badgeLabel = "CLASSIFIED",
    subtitle = "Glitched anomaly gear from the Dog Vault Keeper.",
  );

  companion object {
    fun fromSourceLabel(label: String?): InventoryEventGroup {
      return when {
        label == "Thanksgiving Part 1" || label == "Thanksgiving Part 2" -> THANKSGIVING_PART_1
        label == "December 1st Teaser" -> SOLSTICE_OMEN
        label == "November Pass" || label == "November Pass VIP" -> NOVEMBER_PASS
        label == "Gem Vault" -> GEM_VAULT
        label?.startsWith("Day ") == true -> DAILY_STREAKS
        label == "Limited Event" -> STARLIGHT_CATNIP
        label == "Secret Code Vault" -> SECRET_VAULT
        else -> STARLIGHT_CATNIP
      }
    }
  }
}

object GameCatalog {
  val upgrades: List<ShopUpgradeDef> =
    listOf(
      // Click Upgrades (8 balanced tiers)
      ShopUpgradeDef(
        id = "feather_wand",
        name = "Feather Wand",
        description = "Tickle behind the ears for extra happy purrs.",
        category = UpgradeCategory.CLICK,
        baseCost = 30L,
        costMultiplier = 1.55,
        powerPerLevel = 1L,
      ),
      ShopUpgradeDef(
        id = "chin_scratch",
        name = "Chin Scratch Pro",
        description = "Precision chin scritches that delight your cat.",
        category = UpgradeCategory.CLICK,
        baseCost = 240L,
        costMultiplier = 1.58,
        powerPerLevel = 4L,
      ),
      ShopUpgradeDef(
        id = "silk_glove",
        name = "Silk Grooming Glove",
        description = "Velvet-smooth strokes for velvety purrs.",
        category = UpgradeCategory.CLICK,
        baseCost = 1_600L,
        costMultiplier = 1.62,
        powerPerLevel = 16L,
      ),
      ShopUpgradeDef(
        id = "laser_pointer",
        name = "Starlight Laser",
        description = "Hypersonic red dot chases boost tap energy.",
        category = UpgradeCategory.CLICK,
        baseCost = 11_000L,
        costMultiplier = 1.65,
        powerPerLevel = 65L,
      ),
      ShopUpgradeDef(
        id = "golden_brush",
        name = "Royal Boar Brush",
        description = "Artisanal brush fit for feline royalty.",
        category = UpgradeCategory.CLICK,
        baseCost = 75_000L,
        costMultiplier = 1.68,
        powerPerLevel = 260L,
      ),
      ShopUpgradeDef(
        id = "sonic_purr_gloves",
        name = "Harmonic Massage Mitts",
        description = "Resonant fingertips tuned to pure feline bliss.",
        category = UpgradeCategory.CLICK,
        baseCost = 520_000L,
        costMultiplier = 1.70,
        powerPerLevel = 1_100L,
      ),
      ShopUpgradeDef(
        id = "celestial_bell",
        name = "Celestial Catbell",
        description = "Each chime resonates purrs across nine lives.",
        category = UpgradeCategory.CLICK,
        baseCost = 3_800_000L,
        costMultiplier = 1.72,
        powerPerLevel = 4_800L,
      ),
      ShopUpgradeDef(
        id = "nebula_stylus",
        name = "Astral Whisker Wand",
        description = "Channels starlight directly into every gentle tap.",
        category = UpgradeCategory.CLICK,
        baseCost = 28_000_000L,
        costMultiplier = 1.75,
        powerPerLevel = 22_000L,
      ),

      // Per-Second Upgrades (9 balanced tiers)
      ShopUpgradeDef(
        id = "cardboard_box",
        name = "Cozy Cardboard Box",
        description = "If it fits, it sits — steady passive purrs.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 45L,
        costMultiplier = 1.52,
        powerPerLevel = 1L,
      ),
      ShopUpgradeDef(
        id = "catnip_cushion",
        name = "Catnip Cloud Cushion",
        description = "Herbal bliss keeps the purr engine humming.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 380L,
        costMultiplier = 1.55,
        powerPerLevel = 5L,
      ),
      ShopUpgradeDef(
        id = "sunbeam_lounger",
        name = "Sunbeam Window Hammock",
        description = "All-day solar-powered feline relaxation.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 2_600L,
        costMultiplier = 1.58,
        powerPerLevel = 22L,
      ),
      ShopUpgradeDef(
        id = "robo_petter",
        name = "Automated Petting Arm",
        description = "Gentle motorized scritches round the clock.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 18_000L,
        costMultiplier = 1.62,
        powerPerLevel = 95L,
      ),
      ShopUpgradeDef(
        id = "grooming_spa",
        name = "Purr-adise Grooming Spa",
        description = "Warm towel & whisker massage lounge.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 125_000L,
        costMultiplier = 1.65,
        powerPerLevel = 420L,
      ),
      ShopUpgradeDef(
        id = "feline_choir",
        name = "Harmonic Purr Choir",
        description = "Synchronized ambient purring fills the room.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 900_000L,
        costMultiplier = 1.68,
        powerPerLevel = 1_850L,
      ),
      ShopUpgradeDef(
        id = "catnip_botanical",
        name = "Catnip Greenhouse",
        description = "Lush indoor sanctuary growing heirloom catnip.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 6_500_000L,
        costMultiplier = 1.70,
        powerPerLevel = 8_200L,
      ),
      ShopUpgradeDef(
        id = "moonlit_observatory",
        name = "Moonlit Roof Observatory",
        description = "Stargazing perches blessed by midnight purrs.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 45_000_000L,
        costMultiplier = 1.72,
        powerPerLevel = 36_000L,
      ),
      ShopUpgradeDef(
        id = "sanctuary_cathedral",
        name = "Grand Feline Sanctuary",
        description = "A legendary paradise revered by cats everywhere.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 320_000_000L,
        costMultiplier = 1.75,
        powerPerLevel = 165_000L,
      ),
      // Late-game Click Upgrades (Billions -> Trillions)
      ShopUpgradeDef(
        id = "celestial_paw_gauntlet",
        name = "Celestial Paw Gauntlet",
        description = "Channels starlight directly into every single pet.",
        category = UpgradeCategory.CLICK,
        baseCost = 2_500_000_000L,
        costMultiplier = 1.72,
        powerPerLevel = 450_000L,
      ),
      ShopUpgradeDef(
        id = "quantum_scratch_matrix",
        name = "Quantum Scratch Matrix",
        description = "Pets your cat across 9 parallel dimensions at once.",
        category = UpgradeCategory.CLICK,
        baseCost = 35_000_000_000L,
        costMultiplier = 1.75,
        powerPerLevel = 3_800_000L,
      ),
      ShopUpgradeDef(
        id = "royal_miga_scepter",
        name = "Royal Scepter of Miga",
        description = "An ancient relic radiating trillion-tier petting power.",
        category = UpgradeCategory.CLICK,
        baseCost = 500_000_000_000L,
        costMultiplier = 1.78,
        powerPerLevel = 32_000_000L,
      ),
      // Late-game PPS Upgrades (Billions -> Trillions)
      ShopUpgradeDef(
        id = "nebula_cat_tower",
        name = "Nebula Spire Cat Tower",
        description = "A zero-gravity scratching post orbiting the Milky Way.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 2_200_000_000L,
        costMultiplier = 1.76,
        powerPerLevel = 780_000L,
      ),
      ShopUpgradeDef(
        id = "astral_purr_reactor",
        name = "Astral Purr Reactor",
        description = "Converts pure feline contentment into infinite cosmic purrs.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 28_000_000_000L,
        costMultiplier = 1.78,
        powerPerLevel = 6_400_000L,
      ),
      ShopUpgradeDef(
        id = "imperial_sanctuary_citadel",
        name = "Imperial Sanctuary Citadel",
        description = "A golden palace built to honor the realm's greatest cats.",
        category = UpgradeCategory.PER_SECOND,
        baseCost = 420_000_000_000L,
        costMultiplier = 1.80,
        powerPerLevel = 55_000_000L,
      ),
    )

  val patterns: List<CatPatternDef> =
    listOf(
      CatPatternDef("tabby", "Ginger Tabby", "Warm marmalade coat with classic stripes.", 0L),
      CatPatternDef("tuxedo", "Dapper Tuxedo", "Sleek midnight fur with crisp snowy paws.", 350L),
      CatPatternDef("calico", "Autumn Calico", "Creamy coat patched with amber and espresso.", 1_800L),
      CatPatternDef("siamese", "Cream Siamese", "Silky ivory fur with toasted cocoa ears.", 7_500L),
      CatPatternDef("sakura", "Sakura Blossom", "Soft petal pink coat dusted with spring blossoms.", 28_000L),
      CatPatternDef("cosmic", "Cosmic Nebula", "Starlight indigo fur shimmering with constellations.", 120_000L),
      // Limited-Time Event & Daily Prize Skins
      CatPatternDef(
        id = "lantern_koi",
        name = "Lantern Koi",
        description = "Festive pearl-white fur with scarlet & gold koi spots.",
        cost = 4_500L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatPatternDef(
        id = "golden_taiyaki",
        name = "Golden Taiyaki",
        description = "Toasted caramel-gold coat warm from festival stalls.",
        cost = 14_000L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatPatternDef(
        id = "solar_sphinx",
        name = "Solar Sunbeam",
        description = "Radiant sunlit ivory coat with amber sun-crest markings.",
        cost = 32_000L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatPatternDef(
        id = "lunar_eclipse",
        name = "Lunar Eclipse",
        description = "Midnight obsidian fur rimmed with crimson eclipse glow.",
        cost = 65_000L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatPatternDef(
        id = "cinnamon_swirl",
        name = "Cinnamon Swirl",
        description = "Cozy bakery cinnamon-latte stripes from Week 1 Check-in.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "Day 7 Prize",
      ),
      CatPatternDef(
        id = "aurora_tabby",
        name = "Celestial Aurora",
        description = "Iridescent northern lights fur from Day 14 Grand Check-in.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "Day 14 Prize",
      ),
      CatPatternDef(
        id = "november_maple_tabby",
        name = "November Maple",
        description = "Limited-time autumn amber coat from November Pass Tier 30 (Free Track).",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "November Pass",
      ),
      CatPatternDef(
        id = "golden_touch_cat",
        name = "Golden Touch",
        description = "24K living gold fur from VIP Tier 30! Triggers Golden Rain (4.0x) for 3m every 30m equipped.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "November Pass VIP",
      ),
      CatPatternDef(
        id = "prism_galaxy",
        name = "Prismatic Galaxy",
        description = "Rare sapphire & iridescent opal starlight coat forged exclusively in the Gem Vault.",
        cost = 0L,
        gemCost = 20L,
        isEventExclusive = true,
        unlockSourceLabel = "Gem Vault",
      ),
      // Thanksgiving Event Part One (Turkey Currency Only)
      CatPatternDef(
        id = "pumpkin_pie_tabby",
        name = "Pumpkin Pie Tabby",
        description = "Spiced pumpkin stripes with whipped cream paws — Thanksgiving Part 1 exclusive!",
        cost = 0L,
        turkeyCost = 5L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 1",
      ),
      CatPatternDef(
        id = "cranberry_calico",
        name = "Cranberry Harvest",
        description = "Deep cranberry, toasted pecan & maple gold patches — Thanksgiving Part 1 exclusive!",
        cost = 0L,
        turkeyCost = 12L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 1",
      ),
      CatPatternDef(
        id = "pilgrim_pheasant",
        name = "Plumage Pheasant",
        description = "Shimmering bronze & crimson-plumed harvest fur — Thanksgiving Part 1 grand coat!",
        cost = 0L,
        turkeyCost = 25L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 1",
      ),
      // Thanksgiving Event Part Two (Mythic Feast Coats — Turkey Currency Only)
      CatPatternDef(
        id = "roasted_crown_calico",
        name = "Roasted Crown Calico",
        description = "Mythic Part 2 calico with toasted chestnut crests & champagne banquet rosettes!",
        cost = 0L,
        turkeyCost = 28L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 2",
      ),
      CatPatternDef(
        id = "velvet_gravy_tabby",
        name = "Velvet Gravy Tabby",
        description = "Mythic Part 2 amber-velvet coat gleaming with molten Golden Gravy Falls swirls!",
        cost = 0L,
        turkeyCost = 36L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 2",
      ),
    )

  val accessories: List<CatAccessoryDef> =
    listOf(
      CatAccessoryDef("none", "Au Naturel", "Pure whiskered elegance with zero clutter.", 0L),
      CatAccessoryDef("bell_collar", "Golden Bell Collar", "Classic scarlet ribbon with a chiming brass bell.", 250L),
      CatAccessoryDef("scarf", "Knit Cozy Scarf", "Striped wool scarf perfect for rainy window watches.", 1_400L),
      CatAccessoryDef("sunglasses", "Cool Cat Shades", "Retro tinted frames for effortless attitude.", 6_000L),
      CatAccessoryDef("wizard_hat", "Starry Wizard Hat", "Enchanted indigo hat for arcane biscuit making.", 24_000L),
      CatAccessoryDef("crown", "Royal Purr Crown", "Jeweled golden crown for top leaderboard royalty.", 95_000L),
      // Limited-Time Event & Daily Prize Accessories
      CatAccessoryDef(
        id = "paper_lantern",
        name = "Festival Lantern",
        description = "Warm glowing paper lantern floating beside your cat.",
        cost = 3_200L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatAccessoryDef(
        id = "taiyaki_plush",
        name = "Taiyaki Treat",
        description = "Golden fish-shaped pastry charm held near the collar.",
        cost = 9_500L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatAccessoryDef(
        id = "sunflower_halo",
        name = "Sunbeam Garland",
        description = "Bright golden festival blossoms tucked behind the ears.",
        cost = 22_000L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatAccessoryDef(
        id = "crescent_amulet",
        name = "Bloodmoon Crescent",
        description = "Mystic lunar crescent medallion glowing after midnight.",
        cost = 48_000L,
        isEventExclusive = true,
        unlockSourceLabel = "Limited Event",
      ),
      CatAccessoryDef(
        id = "bow_tie",
        name = "Dapper Bow Tie",
        description = "Silk polka-dot bow tie unlocked on Day 3 Check-in.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "Day 3 Prize",
      ),
      CatAccessoryDef(
        id = "tiny_beret",
        name = "Artist Kitty Beret",
        description = "Cozy burgundy felt beret unlocked on Day 12 Check-in.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "Day 12 Prize",
      ),
      CatAccessoryDef(
        id = "harvest_beret",
        name = "Maple Harvest Hat",
        description = "Warm autumn felt hat unlocked at November Pass VIP Tier 10.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "November Pass VIP",
      ),
      CatAccessoryDef(
        id = "golden_collar",
        name = "24K Golden Bell",
        description = "Gleaming gold collar & diamond charm unlocked at November Pass VIP Tier 20.",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "November Pass VIP",
      ),
      CatAccessoryDef(
        id = "diamond_crown",
        name = "Diamond Catnip Crown",
        description = "Sparkling icy-cyan tiara crafted from ultra-rare Sanctuary Gems.",
        cost = 0L,
        gemCost = 12L,
        isEventExclusive = true,
        unlockSourceLabel = "Gem Vault",
      ),
      // Thanksgiving Event Part One (Turkey Currency Only)
      CatAccessoryDef(
        id = "pilgrim_hat",
        name = "Harvest Pilgrim Hat",
        description = "Buckled charcoal-felt hat worn by festive Thanksgiving caretakers!",
        cost = 0L,
        turkeyCost = 4L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 1",
      ),
      CatAccessoryDef(
        id = "cranberry_scarf",
        name = "Autumn Leaf Scarf",
        description = "Hand-knitted cranberry wool scarf pinned with a crisp maple leaf.",
        cost = 0L,
        turkeyCost = 9L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 1",
      ),
      CatAccessoryDef(
        id = "turkey_plush",
        name = "Golden Turkey Plush",
        description = "Festive roasted-gold turkey companion perched beside your collar!",
        cost = 0L,
        turkeyCost = 18L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 1",
      ),
      // Thanksgiving Event Part Two Accessories
      CatAccessoryDef(
        id = "cornucopia_horn",
        name = "Cornucopia Horn",
        description = "Braided golden harvest horn overflowing with cranberries & wheat — Part 2 exclusive!",
        cost = 0L,
        turkeyCost = 14L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 2",
      ),
      CatAccessoryDef(
        id = "gravy_ladle",
        name = "Golden Gravy Scepter",
        description = "Royal amber ladle brimming with 3.0x Gravy Rush sparkle from Part 2 Banquet!",
        cost = 0L,
        turkeyCost = 22L,
        isEventExclusive = true,
        unlockSourceLabel = "Thanksgiving Part 2",
      ),
      // December 1st Boss Glitch Limited-Time Reward
      CatAccessoryDef(
        id = "melted_solstice_crown",
        name = "Melting Solstice Crown",
        description = "Crystalline frost crown slowly thawing toward December 1st — recovered after glitching out Mecha-Gobbler Prime!",
        cost = 0L,
        isEventExclusive = true,
        unlockSourceLabel = "December 1st Teaser",
      ),
    )

  val eventExclusivePatterns: List<CatPatternDef>
    get() = patterns.filter { it.isEventExclusive && it.cost > 0L }

  val eventExclusiveAccessories: List<CatAccessoryDef>
    get() = accessories.filter { it.isEventExclusive && it.cost > 0L }

  val turkeyEventPatterns: List<CatPatternDef>
    get() = patterns.filter { it.turkeyCost > 0L }

  val turkeyEventAccessories: List<CatAccessoryDef>
    get() = accessories.filter { it.turkeyCost > 0L }

  val badges: List<BadgeDef> =
    listOf(
      BadgeDef("first_purr", "First Purr", "Gave your cat 100 heartfelt pets.", "Common"),
      BadgeDef("combo_whisperer", "Rhythm Whisperer", "Reached 2,500 lifetime pets.", "Uncommon"),
      BadgeDef("golden_paw", "Golden Paw", "Reached 15,000 lifetime pets.", "Epic"),
      BadgeDef("whisker_deity", "Whisker Deity", "Reached 100,000 lifetime pets.", "Mythic"),
      BadgeDef("shop_tycoon", "Cozy Collector", "Purchased 25 total shop upgrades.", "Uncommon"),
      BadgeDef("purr_tycoon", "Purr Tycoon", "Purchased 75 total shop upgrades.", "Epic"),
      BadgeDef("fashionista", "Whisker Stylist", "Unlocked 5 coat patterns or accessories.", "Rare"),
      BadgeDef("weather_watcher", "Sky Gazer", "Experienced 5 Sanctuary Weather shifts.", "Uncommon"),
      BadgeDef("storm_whisperer", "Sky Sovereign", "Witnessed 25 Sanctuary Weather shifts.", "Epic"),
      BadgeDef("streak_master", "Devoted Caretaker", "Built a 5-day Daily Rewards streak.", "Epic"),
      BadgeDef("fortnight_guardian", "Fortnight Guardian", "Reached a 14-day Daily Rewards streak.", "Rare"),
      BadgeDef("eternal_caretaker", "Endless Devotion", "Reached a 30-day Endless Daily Streak.", "Legendary"),
      BadgeDef("event_champion", "Festival Spirit", "Earned 50,000 purrs during limited-time events.", "Rare"),
      BadgeDef("phoenix_kitten", "Phoenix Kitten", "Performed your very first Rebirth.", "Epic"),
      BadgeDef("astral_gato", "Astral Gato", "Completed a Gato Ascensión.", "Mythic"),
      BadgeDef("celestial_sovereign", "Celestial Sovereign", "Earned 1,000,000 lifetime Purrs.", "Legendary"),
      BadgeDef("eternal_whisperer", "Eternal Cat Whisperer", "Earned 50,000,000 lifetime Purrs.", "Mythic"),
      BadgeDef("galaxy_purr_emperor", "Galaxy Purr Emperor", "Earned 1,000,000,000 lifetime Purrs.", "Mythic"),
      BadgeDef("gem_collector", "Crystal Purr Seeker", "Collected 10 Rare Sanctuary Gems.", "Rare"),
      BadgeDef("ascension_scholar", "Starlight Scholar", "Unlocked 3 nodes in the Gato Ascension Tree.", "Epic"),
      BadgeDef("rebirth_veteran", "Nine-Lives Veteran", "Performed 5 Sanctuary Rebirths.", "Mythic"),
      BadgeDef("offline_dreamer", "Catnap Earner", "Accumulated 1 hour of Offline Sanctuary time.", "Uncommon"),
      BadgeDef("frenzy_acrobat", "Hyper Purr Acrobat", "Reached a 2.00x Petting Frenzy Combo.", "Rare"),
      BadgeDef("vault_hacker", "Vault Intruder", "Angered the Dog Vault Keeper inside the Secret Code Vault.", "Mythic"),
      BadgeDef("royal_miga_decree", "Miga's Royal Decree", "Reached 1 Trillion Purrs, met Miga the Great, and witnessed the Red Eyes.", "Royal"),
      BadgeDef("part2_feast_sovereign", "Cornucopia Sovereign", "Collected 15 lifetime Turkeys during the Thanksgiving Part Two Feast.", "Mythic"),
    )

  val quests: List<QuestDef> =
    listOf(
      QuestDef(
        id = "q_first_pets",
        title = "Gentle Introductions",
        description = "Pet your cat 100 times.",
        target = 100L,
        rewardPurrs = 120L,
        rewardGems = 0L,
        badgeId = "first_purr",
        progressExtractor = { p, _, _ -> p.totalPets },
      ),
      QuestDef(
        id = "q_pets_500",
        title = "Dedicated Scritcher",
        description = "Reach 2,500 lifetime pets.",
        target = 2_500L,
        rewardPurrs = 1_500L,
        rewardGems = 0L,
        badgeId = "combo_whisperer",
        progressExtractor = { p, _, _ -> p.totalPets },
      ),
      QuestDef(
        id = "q_pets_15000",
        title = "Tireless Whisker Keeper",
        description = "Reach 15,000 lifetime pets.",
        target = 15_000L,
        rewardPurrs = 25_000L,
        rewardGems = 1L,
        badgeId = "golden_paw",
        progressExtractor = { p, _, _ -> p.totalPets },
      ),
      QuestDef(
        id = "q_pets_100k",
        title = "Cosmic Scritch Legend",
        description = "Reach 100,000 lifetime pets.",
        target = 100_000L,
        rewardPurrs = 750_000L,
        rewardGems = 2L,
        badgeId = "whisker_deity",
        progressExtractor = { p, _, _ -> p.totalPets },
      ),
      QuestDef(
        id = "q_upgrades_10",
        title = "Sanctuary Builder",
        description = "Own 25 combined shop upgrade levels.",
        target = 25L,
        rewardPurrs = 1_200L,
        rewardGems = 0L,
        badgeId = "shop_tycoon",
        progressExtractor = { p, _, _ -> p.upgradeLevels.values.sum() },
      ),
      QuestDef(
        id = "q_upgrades_75",
        title = "Master Architect",
        description = "Own 75 combined shop upgrade levels.",
        target = 75L,
        rewardPurrs = 40_000L,
        rewardGems = 1L,
        badgeId = "purr_tycoon",
        progressExtractor = { p, _, _ -> p.upgradeLevels.values.sum() },
      ),
      QuestDef(
        id = "q_style_3",
        title = "Boutique Regular",
        description = "Unlock 5 patterns or accessories beyond defaults.",
        target = 5L,
        rewardPurrs = 3_500L,
        rewardGems = 0L,
        badgeId = "fashionista",
        progressExtractor = { p, _, _ ->
          ((p.unlockedPatterns.size - 1).coerceAtLeast(0) +
            (p.unlockedAccessories.size - 1).coerceAtLeast(0)).toLong()
        },
      ),
      QuestDef(
        id = "q_weather_1",
        title = "Forecast Watcher",
        description = "Witness 5 random Sanctuary Weather shifts.",
        target = 5L,
        rewardPurrs = 800L,
        rewardGems = 0L,
        badgeId = "weather_watcher",
        progressExtractor = { p, _, _ -> p.weatherCheckedCount },
      ),
      QuestDef(
        id = "q_weather_25",
        title = "Celestial Meteorologist",
        description = "Witness 25 Sanctuary Weather shifts.",
        target = 25L,
        rewardPurrs = 30_000L,
        rewardGems = 1L,
        badgeId = "storm_whisperer",
        progressExtractor = { p, _, _ -> p.weatherCheckedCount },
      ),
      QuestDef(
        id = "q_daily_3",
        title = "Morning Routine",
        description = "Build a 5-day Daily Rewards streak.",
        target = 5L,
        rewardPurrs = 4_500L,
        rewardGems = 0L,
        badgeId = "streak_master",
        progressExtractor = { p, _, stats -> maxOf(p.dailyStreak, stats.longestDailyStreak) },
      ),
      QuestDef(
        id = "q_streak_14",
        title = "Fortnight Ritual",
        description = "Reach a 14-day Daily Rewards streak.",
        target = 14L,
        rewardPurrs = 15_000L,
        rewardGems = 1L,
        badgeId = "fortnight_guardian",
        progressExtractor = { p, _, stats -> maxOf(p.dailyStreak, stats.longestDailyStreak) },
      ),
      QuestDef(
        id = "q_streak_30",
        title = "Endless Monthly Devotion",
        description = "Reach a 30-day Endless Daily Rewards streak.",
        target = 30L,
        rewardPurrs = 120_000L,
        rewardGems = 2L,
        badgeId = "eternal_caretaker",
        progressExtractor = { p, _, stats -> maxOf(p.dailyStreak, stats.longestDailyStreak) },
      ),
      QuestDef(
        id = "q_event_2500",
        title = "Festival Celebrant",
        description = "Earn 50,000 Purrs during Limited-Time Events.",
        target = 50_000L,
        rewardPurrs = 15_000L,
        rewardGems = 1L,
        badgeId = "event_champion",
        progressExtractor = { p, _, _ -> p.eventPurrsEarned },
      ),
      QuestDef(
        id = "q_rebirth_1",
        title = "Reborn in Purrs",
        description = "Perform your first Rebirth.",
        target = 1L,
        rewardPurrs = 50_000L,
        rewardGems = 1L,
        badgeId = "phoenix_kitten",
        progressExtractor = { _, ch, _ -> ch.rebirthCount },
      ),
      QuestDef(
        id = "q_ascension_1",
        title = "Ascended Caretaker",
        description = "Complete a Gato Ascensión.",
        target = 1L,
        rewardPurrs = 500_000L,
        rewardGems = 2L,
        badgeId = "astral_gato",
        progressExtractor = { _, ch, _ -> ch.ascensionCount },
      ),
      QuestDef(
        id = "q_purrs_1m",
        title = "Purr Millionaire",
        description = "Earn 1,000,000 lifetime Purrs.",
        target = 1_000_000L,
        rewardPurrs = 150_000L,
        rewardGems = 1L,
        badgeId = "celestial_sovereign",
        progressExtractor = { p, _, _ -> p.totalPurrsEarned },
      ),
      QuestDef(
        id = "q_purrs_50m",
        title = "Nine Lives Legend",
        description = "Earn 50,000,000 lifetime Purrs.",
        target = 50_000_000L,
        rewardPurrs = 2_500_000L,
        rewardGems = 2L,
        badgeId = "eternal_whisperer",
        progressExtractor = { p, _, _ -> p.totalPurrsEarned },
      ),
      QuestDef(
        id = "q_purrs_1b",
        title = "Billionaire Cat Patron",
        description = "Earn 1,000,000,000 lifetime Purrs.",
        target = 1_000_000_000L,
        rewardPurrs = 25_000_000L,
        rewardGems = 3L,
        badgeId = "galaxy_purr_emperor",
        progressExtractor = { p, _, _ -> p.totalPurrsEarned },
      ),
      QuestDef(
        id = "q_gems_10",
        title = "Gem Collector",
        description = "Collect 10 Rare Sanctuary Gems.",
        target = 10L,
        rewardPurrs = 35_000L,
        rewardGems = 1L,
        badgeId = "gem_collector",
        progressExtractor = { _, ch, _ -> ch.totalGemsEarned },
      ),
      QuestDef(
        id = "q_ascension_tree_3",
        title = "Ascension Scholar",
        description = "Unlock 3 nodes in the Gato Ascension Tree.",
        target = 3L,
        rewardPurrs = 1_500_000L,
        rewardGems = 2L,
        badgeId = "ascension_scholar",
        progressExtractor = { _, ch, _ -> ch.unlockedAscensionIds.size.toLong() },
      ),
      QuestDef(
        id = "q_rebirth_5",
        title = "Rebirth Veteran",
        description = "Perform 5 Sanctuary Rebirths.",
        target = 5L,
        rewardPurrs = 750_000L,
        rewardGems = 2L,
        badgeId = "rebirth_veteran",
        progressExtractor = { _, ch, _ -> ch.rebirthCount },
      ),
      QuestDef(
        id = "q_offline_1h",
        title = "Dreaming Sanctuary",
        description = "Accumulate 1 hour (3,600s) of Offline earnings time.",
        target = 3_600L,
        rewardPurrs = 18_000L,
        rewardGems = 1L,
        badgeId = "offline_dreamer",
        progressExtractor = { _, _, stats -> stats.totalOfflineSeconds },
      ),
      QuestDef(
        id = "q_combo_2x",
        title = "Frenzy Acrobat",
        description = "Reach a 2.00x Petting Frenzy Combo multiplier.",
        target = 1L,
        rewardPurrs = 8_000L,
        rewardGems = 1L,
        badgeId = "frenzy_acrobat",
        progressExtractor = { _, _, stats -> if (stats.highestComboMultiplier >= 2.0) 1L else 0L },
      ),
      QuestDef(
        id = "q_vault_keeper",
        title = "Code Vault Intruder",
        description = "Bother the Dog Vault Keeper in Settings' Secret Vault until kicked out.",
        target = 1L,
        rewardPurrs = 50_000L,
        rewardGems = 2L,
        badgeId = "vault_hacker",
        progressExtractor = { _, ch, _ -> if (ch.hackerVaultUnlocked) 1L else 0L },
      ),
      QuestDef(
        id = "q_part2_feast",
        title = "Cornucopia Citadel Sovereign",
        description = "Earn 15 lifetime Turkeys (including +3 Golden Gobblers) in Thanksgiving Part 2.",
        target = 15L,
        rewardPurrs = 125_000L,
        rewardGems = 2L,
        badgeId = "part2_feast_sovereign",
        progressExtractor = { _, ch, _ -> ch.totalTurkeysEarned },
      ),
    )

  val dailyRewards: List<DailyRewardDef> =
    listOf(
      DailyRewardDef(1, "Day 1", 60L, "Warm Milk Welcome"),
      DailyRewardDef(2, "Day 2", 180L, "Crunchy Salmon Bites"),
      DailyRewardDef(
        day = 3,
        title = "Day 3",
        purrs = 350L,
        bonusNote = "Dapper Ribbon Gift",
        bonusType = DailyBonusPrizeType.ACCESSORY,
        bonusItemId = "bow_tie",
        bonusPrizeLabel = "+ Dapper Bow Tie",
      ),
      DailyRewardDef(4, "Day 4", 750L, "Velvet Cushion Nap"),
      DailyRewardDef(
        day = 5,
        title = "Day 5",
        purrs = 1_200L,
        bonusNote = "Playtime Tool Box",
        bonusType = DailyBonusPrizeType.UPGRADE_LEVEL,
        bonusItemId = "feather_wand",
        bonusPrizeLabel = "+1 Feather Wand Lv",
      ),
      DailyRewardDef(6, "Day 6", 2_200L, "Starlight Yarn Cache"),
      DailyRewardDef(
        day = 7,
        title = "Day 7",
        purrs = 4_000L,
        bonusNote = "Week 1 Feline Feast",
        bonusType = DailyBonusPrizeType.PATTERN,
        bonusItemId = "cinnamon_swirl",
        bonusPrizeLabel = "+ Cinnamon Swirl Skin",
      ),
      DailyRewardDef(8, "Day 8", 5_500L, "Crispy Tuna Flakes"),
      DailyRewardDef(
        day = 9,
        title = "Day 9",
        purrs = 7_500L,
        bonusNote = "Sunlit Box Delivery",
        bonusType = DailyBonusPrizeType.UPGRADE_LEVEL,
        bonusItemId = "cardboard_box",
        bonusPrizeLabel = "+1 Cardboard Box Lv",
      ),
      DailyRewardDef(
        day = 10,
        title = "Day 10",
        purrs = 10_000L,
        bonusNote = "Royal Palette Gift",
        bonusType = DailyBonusPrizeType.COLOR_THEME,
        bonusItemId = "rose_gold",
        bonusPrizeLabel = "+ Rose Gold Theme",
      ),
      DailyRewardDef(11, "Day 11", 13_500L, "Moonbeam Milk Tea"),
      DailyRewardDef(
        day = 12,
        title = "Day 12",
        purrs = 18_000L,
        bonusNote = "Atelier Cat Gift",
        bonusType = DailyBonusPrizeType.ACCESSORY,
        bonusItemId = "tiny_beret",
        bonusPrizeLabel = "+ Artist Kitty Beret",
      ),
      DailyRewardDef(13, "Day 13", 25_000L, "Golden Catnip Bouquet"),
      DailyRewardDef(
        day = 14,
        title = "Day 14",
        purrs = 50_000L,
        bonusNote = "Fortnight Grand Jackpot",
        bonusType = DailyBonusPrizeType.PATTERN,
        bonusItemId = "aurora_tabby",
        bonusPrizeLabel = "+ Celestial Aurora Skin",
      ),
    )

  val periodicEvents: List<LimitedEventDef> =
    listOf(
      LimitedEventDef(
        id = "thanksgiving_part_2",
        title = "Thanksgiving Event: Part Two",
        subtitle = "Part 2 IS LIVE! Catch +3 Golden Gobblers, ride 3.0x Gravy Rush & unlock Mythic Banquet gear!",
        boostLabel = "1.75x–3.0x Gravy Rush • +3 Golden Gobblers",
        clickMultiplier = 1.75,
        ppsMultiplier = 1.75,
        milestoneGoal = 150_000L,
        milestoneRewardPurrs = 12_000L,
        badgeId = "event_champion",
      ),
    )

  val thanksgivingSneakPeek =
    EventSneakPeekDef(
      title = "Thanksgiving Event: Part Two",
      dateWindow = "NOW LIVE!",
      summary = "Thanksgiving Part Two has arrived! Catch shimmering +3 Golden Gobblers during each hourly 5-minute rush, surge with 3.0x MEGA Gravy Rush, and unlock Cornucopia Citadel & Mythic Banquet coats!",
      previewHighlights =
        listOf(
          "Golden Gobblers (+3 Turkeys) & 3.0x MEGA Gravy Rush",
          "Mythic Coats: Roasted Crown Calico & Velvet Gravy Tabby",
          "Part 2 Gear, Cornucopia Citadel & Gravy Falls Rooms + Sovereign Badge!",
        ),
    )

  const val FESTIVAL_SPAWN_INTERVAL_SECONDS = 10
  const val TURKEY_SPAWN_WINDOW_MS = 5L * 60L * 1000L
  const val TURKEY_SPAWN_CHANCE_REGULAR_PERCENT = 25
  const val TURKEY_SPAWN_CHANCE_MEGA_PERCENT = 45
  const val MEGA_FESTIVAL_CHANCE_PERCENT = 33
  const val GOLDEN_GOBBLER_CHANCE_REGULAR_PERCENT = 25
  const val GOLDEN_GOBBLER_CHANCE_MEGA_PERCENT = 35
  const val GOLDEN_GOBBLER_REWARD = 3L
  const val TURKEY_GATO_MAP_TALKS = 5

  fun turkeySpawnWindowRemainingMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    val elapsedInHour = nowMillis % 3_600_000L
    return (TURKEY_SPAWN_WINDOW_MS - elapsedInHour).coerceAtLeast(0L)
  }

  fun isTurkeySpawnWindowActive(nowMillis: Long = System.currentTimeMillis()): Boolean {
    return turkeySpawnWindowRemainingMillis(nowMillis) > 0L
  }

  fun turkeyRushStatusText(nowMillis: Long = System.currentTimeMillis()): String {
    val rem = turkeySpawnWindowRemainingMillis(nowMillis)
    return if (rem > 0L) {
      "Turkeys & +3 Gobblers spawning (${formatElapsedDuration(rem)} left)"
    } else {
      "Next rush & store open in ${formatElapsedDuration(millisUntilNextEpochHour(nowMillis))}"
    }
  }

  fun rollHourlyFestivalIsMega(random: kotlin.random.Random = kotlin.random.Random): Boolean {
    return random.nextInt(100) < MEGA_FESTIVAL_CHANCE_PERCENT
  }

  fun festivalTierFor(isMega: Boolean): FestivalTier {
    return if (isMega) FestivalTier.MEGA else FestivalTier.REGULAR
  }

  fun shouldSpawnTurkey(tier: FestivalTier, random: kotlin.random.Random = kotlin.random.Random): Boolean {
    return random.nextInt(100) < tier.turkeySpawnChancePercent
  }

  fun shouldSpawnGoldenGobbler(tier: FestivalTier, random: kotlin.random.Random = kotlin.random.Random): Boolean {
    val chance =
      if (tier == FestivalTier.MEGA) GOLDEN_GOBBLER_CHANCE_MEGA_PERCENT
      else GOLDEN_GOBBLER_CHANCE_REGULAR_PERCENT
    return random.nextInt(100) < chance
  }

  val turkeyGatoDialogues: List<String> =
    listOf(
      "Gobble-MEOW!! Part Two is finally HERE! Cornucopia Citadel is open and Golden Gravy Falls is pouring 3.0x Gravy Rush boosts!",
      "Keep your eyes peeled during the 5-minute hourly rush! Sparkling Golden Gobblers now spawn alongside regular Turkeys — tap one for +3 Turkeys at once!",
      "Both Part One classics AND brand-new Part Two Mythic items (Roasted Crown Calico, Velvet Gravy Tabby, Cornucopia Horn & 2 Part 2 Rooms) are stocked inside!",
      "Heard of Cornucopia Sovereign? Gather 15 lifetime Turkeys during the festival to claim the Mythic Cornucopia Sovereign badge in Quests!",
      "Keep chatting with me if you haven't claimed my Feast Map yet — all 5 island zones are now bustling with banquet guests!",
      "Gobble-PURR!! Here is your permanent Part 2 Feast Map! Open it anytime via the More menu to inspect every live Banquet Citadel zone!",
    )

  val part2MapLandmarks: List<Part2MapLandmark> =
    listOf(
      Part2MapLandmark(
        id = "cornucopia_citadel",
        title = "Cornucopia Citadel",
        subtitle = "BOSS ARENA",
        xFraction = 0.50f,
        yFraction = 0.24f,
        previewDetail = "Face Mecha-Gobbler Prime atop Cornucopia Citadel! Dodge its laser volley, break its shield, and witness what happens right before defeat...",
        activityType = FeastZoneActivityType.BOSS_BATTLE,
        actionLabel = "Teleport to Boss Arena",
        rewardSummary = "Limited-Time Item • Turkeys • Pass XP",
      ),
      Part2MapLandmark(
        id = "gravy_falls",
        title = "Golden Gravy Falls",
        subtitle = "FISHING PIER",
        xFraction = 0.20f,
        yFraction = 0.44f,
        previewDetail = "Cast your line into shimmering molten amber! Time your hook and tension gauge to reel in Gravy Koi, +2 Turkeys, and rare Pearl Gems.",
        activityType = FeastZoneActivityType.FISHING,
        actionLabel = "Teleport to Fishing Pier",
        rewardSummary = "Purrs • +1–2 Turkeys • Rare Gem Chance",
      ),
      Part2MapLandmark(
        id = "pumpkin_bakery",
        title = "Spiced Pie Bakery",
        subtitle = "PIE DASH GAME",
        xFraction = 0.78f,
        yFraction = 0.40f,
        previewDetail = "Step behind the warm brick oven! Assemble pumpkin, cinnamon & cream orders in Spiced Pie Dash before the timer runs out.",
        activityType = FeastZoneActivityType.MINIGAME_BAKERY,
        actionLabel = "Teleport to Bakery",
        rewardSummary = "Purrs • +2 Turkeys • Pass XP",
      ),
      Part2MapLandmark(
        id = "cranberry_bog",
        title = "Cranberry Meadow",
        subtitle = "HARVEST CATCH",
        xFraction = 0.26f,
        yFraction = 0.74f,
        previewDetail = "Play Cranberry Harvest Catch! Tap popping ruby cranberries and golden berries while dodging spoiled acorns.",
        activityType = FeastZoneActivityType.MINIGAME_CRANBERRY,
        actionLabel = "Teleport to Meadow",
        rewardSummary = "Purrs • +2 Turkeys • Pass XP",
      ),
      Part2MapLandmark(
        id = "turkey_gato_hall",
        title = "Turkey Gato's Banquet",
        subtitle = "FEAST COOKING",
        xFraction = 0.72f,
        yFraction = 0.75f,
        previewDetail = "Enter Turkey Gato's Royal Kitchen! Season, sizzle-sear at the sweet-spot heat, and glaze a 5-Star Thanksgiving Feast.",
        activityType = FeastZoneActivityType.COOKING,
        actionLabel = "Teleport to Kitchen",
        rewardSummary = "Purrs • Up to +3 Turkeys • Pass XP",
      ),
    )

  val festivalQuestPool: List<FestivalQuestTemplate> =
    listOf(
      FestivalQuestTemplate(
        id = "tq_pets_35",
        title = "Harvest Warmup",
        description = "Pet your cat 45 times this hour.",
        kind = FestivalQuestKind.PETS,
        target = 45L,
        turkeyReward = 1L,
      ),
      FestivalQuestTemplate(
        id = "tq_turkeys_3",
        title = "Turkey Spotter",
        description = "Catch 2 spawning Turkeys during the 5m rush.",
        kind = FestivalQuestKind.TURKEYS_TAPPED,
        target = 2L,
        turkeyReward = 2L,
      ),
      FestivalQuestTemplate(
        id = "tq_upgrades_2",
        title = "Feast Preparations",
        description = "Buy 3 shop upgrades this hour.",
        kind = FestivalQuestKind.UPGRADES,
        target = 3L,
        turkeyReward = 1L,
      ),
      FestivalQuestTemplate(
        id = "tq_weather_2",
        title = "Autumn Sky Watch",
        description = "Witness 3 Sanctuary Weather shifts this hour.",
        kind = FestivalQuestKind.WEATHER_SHIFTS,
        target = 3L,
        turkeyReward = 1L,
      ),
      FestivalQuestTemplate(
        id = "tq_pets_100",
        title = "Gobble Scritch Marathon",
        description = "Pet your cat 140 times this hour.",
        kind = FestivalQuestKind.PETS,
        target = 140L,
        turkeyReward = 2L,
      ),
    )

  fun evaluateHourlyFestivalQuests(challenge: ChallengeProgressState): List<ActiveFestivalQuest> {
    return festivalQuestPool.map { tpl ->
      val progress =
        when (tpl.kind) {
          FestivalQuestKind.PETS -> challenge.festHourPets
          FestivalQuestKind.TURKEYS_TAPPED -> challenge.festHourTurkeysTapped
          FestivalQuestKind.UPGRADES -> challenge.festHourUpgradesBought
          FestivalQuestKind.WEATHER_SHIFTS -> challenge.festHourWeatherShifts
        }
      ActiveFestivalQuest(
        template = tpl,
        progress = progress,
        isClaimed = tpl.id in challenge.claimedFestivalQuestIds,
      )
    }
  }

  val ascensionUpgrades: List<AscensionUpgradeDef> =
    listOf(
      AscensionUpgradeDef(
        id = "asc_golden_whiskers",
        tier = 1,
        name = "Golden Whiskers",
        description = "Infuse every scritch and toy with celestial energy.",
        effectSummary = "2.0x Global Click & Auto-Purr Power",
        shardCost = 1L,
        prerequisiteIds = emptyList(),
      ),
      AscensionUpgradeDef(
        id = "asc_gem_seeker",
        tier = 2,
        name = "Crystal Radar",
        description = "Attune whiskers to rare gem veins hidden beneath the stars.",
        effectSummary = "+50% Rare Gem Drop Rate & +1 Bonus Gem on Ascension",
        shardCost = 2L,
        prerequisiteIds = listOf("asc_golden_whiskers"),
      ),
      AscensionUpgradeDef(
        id = "asc_bargain_paws",
        tier = 2,
        name = "Celestial Bargain",
        description = "Shopkeepers bow before ascended caretakers.",
        effectSummary = "-25% Prices in Shop & Hardcore Mode",
        shardCost = 1L,
        prerequisiteIds = listOf("asc_golden_whiskers"),
      ),
      AscensionUpgradeDef(
        id = "asc_frenzy_aura",
        tier = 2,
        name = "Hyper Frenzy Aura",
        description = "Petting momentum builds twice as fast and surges higher.",
        effectSummary = "2x Combo Speed, 50% Slower Decay & 3.0x Max Combo",
        shardCost = 1L,
        prerequisiteIds = listOf("asc_golden_whiskers"),
      ),
      AscensionUpgradeDef(
        id = "asc_offline_mastery",
        tier = 3,
        name = "Dreaming Catnap",
        description = "Train sanctuary helpers to keep purring longer while you sleep.",
        effectSummary = "+15% Offline Efficiency (Up to 90%) & 48h Offline Cap",
        shardCost = 2L,
        prerequisiteIds = listOf("asc_bargain_paws"),
      ),
      AscensionUpgradeDef(
        id = "asc_pass_scholar",
        tier = 3,
        name = "Season Tactician",
        description = "Extract extra seasonal mastery from every pet, shift, and quest.",
        effectSummary = "+50% Bonus November Pass XP from All Sources",
        shardCost = 2L,
        prerequisiteIds = listOf("asc_frenzy_aura"),
      ),
      AscensionUpgradeDef(
        id = "asc_lucky_catnip",
        tier = 3,
        name = "Lucky Catnip Burst",
        description = "Each tap has a chance to trigger a dazzling purr jackpot.",
        effectSummary = "20% Chance for 5x Golden Crit on Pet",
        shardCost = 2L,
        prerequisiteIds = listOf("asc_bargain_paws"),
      ),
      AscensionUpgradeDef(
        id = "asc_lunar_harmony",
        tier = 3,
        name = "Astral Weather Harmony",
        description = "Resonate deeply with sky shifts, Festivals, and Moonlight.",
        effectSummary = "+50% Stronger Weather, Event & Bloodmoon Buffs",
        shardCost = 2L,
        prerequisiteIds = listOf("asc_frenzy_aura"),
      ),
      AscensionUpgradeDef(
        id = "asc_click_supernova",
        tier = 4,
        name = "Paw Supernova",
        description = "Channel your automated toys directly into thunderous manual taps.",
        effectSummary = "2.0x Tap Power & +10% Base PPS Added to Every Tap",
        shardCost = 3L,
        prerequisiteIds = listOf("asc_lucky_catnip", "asc_offline_mastery"),
      ),
      AscensionUpgradeDef(
        id = "asc_passive_symphony",
        tier = 4,
        name = "Sanctuary Symphony",
        description = "Harmonize every cushion, spa, and observatory into an endless purr chorus.",
        effectSummary = "2.0x Global Auto-Purr (PPS) Output",
        shardCost = 3L,
        prerequisiteIds = listOf("asc_lunar_harmony", "asc_pass_scholar"),
      ),
      AscensionUpgradeDef(
        id = "asc_rebirth_echo",
        tier = 4,
        name = "Eternal Rebirth Echo",
        description = "Every Rebirth echoes with double prestige blessing.",
        effectSummary = "+1.0x per Rebirth & Start Rebirths with 1,000 Purrs",
        shardCost = 3L,
        prerequisiteIds = listOf("asc_lucky_catnip", "asc_lunar_harmony"),
      ),
      AscensionUpgradeDef(
        id = "asc_cosmic_sovereign",
        tier = 5,
        name = "Cosmic Nine Lives",
        description = "Ultimate Honeycomb pinnacle crowning eternal caretakers.",
        effectSummary = "1.5x Global Purr Surge & Start Rebirths with Lv.3 Starter Toys",
        shardCost = 4L,
        prerequisiteIds = listOf("asc_rebirth_echo"),
      ),
    )

  val weeklyLeaderboardTiers: List<WeeklyLeaderboardTier> =
    listOf(
      WeeklyLeaderboardTier(
        rankLabel = "#1",
        maxRankInclusive = 1,
        purrsReward = 250_000L,
        shardsReward = 2L,
        bonusUpgradeLevels = 2L,
        gemsReward = 6L,
        title = "Grand Weekly Sovereign",
      ),
      WeeklyLeaderboardTier(
        rankLabel = "#2–#3",
        maxRankInclusive = 3,
        purrsReward = 100_000L,
        shardsReward = 1L,
        bonusUpgradeLevels = 1L,
        gemsReward = 3L,
        title = "Royal Podium",
      ),
      WeeklyLeaderboardTier(
        rankLabel = "#4–#10",
        maxRankInclusive = 10,
        purrsReward = 40_000L,
        shardsReward = 0L,
        bonusUpgradeLevels = 1L,
        gemsReward = 2L,
        title = "Elite Caretaker",
      ),
      WeeklyLeaderboardTier(
        rankLabel = "#11–#25",
        maxRankInclusive = 25,
        purrsReward = 15_000L,
        shardsReward = 0L,
        bonusUpgradeLevels = 0L,
        gemsReward = 1L,
        title = "Cozy Contender",
      ),
      WeeklyLeaderboardTier(
        rankLabel = "#26+ / Local",
        maxRankInclusive = Int.MAX_VALUE,
        purrsReward = 5_000L,
        shardsReward = 0L,
        bonusUpgradeLevels = 0L,
        gemsReward = 0L,
        title = "Weekly Sanctuary Bonus",
      ),
    )

  val backgrounds: List<CatBackgroundDef> =
    listOf(
      CatBackgroundDef(
        id = "cozy_parlor",
        name = "Cozy Sunlit Parlor",
        description = "The classic warm sanctuary room with sunlit wood & cozy cushions.",
        cost = 0L,
        primaryHex = 0xFFFFF3E0,
        secondaryHex = 0xFFFFE0B2,
        accentHex = 0xFFFFB74D,
      ),
      CatBackgroundDef(
        id = "sakura_garden",
        name = "Sakura Tea Garden",
        description = "Drifting cherry blossom petals over a serene lantern-lit veranda.",
        cost = 1_500L,
        primaryHex = 0xFFFCE4EC,
        secondaryHex = 0xFFF8BBD0,
        accentHex = 0xFFEC407A,
      ),
      CatBackgroundDef(
        id = "autumn_porch",
        name = "Golden Autumn Porch",
        description = "Crisp harvest leaves, warm amber lanterns, and a cozy wooden railing.",
        cost = 8_000L,
        primaryHex = 0xFFFFF3E0,
        secondaryHex = 0xFFFFCC80,
        accentHex = 0xFFE65100,
      ),
      CatBackgroundDef(
        id = "starlight_observatory",
        name = "Starlight Observatory",
        description = "A midnight glass dome glittering with constellations and soft nebula glow.",
        cost = 25_000L,
        primaryHex = 0xFF1A1B3A,
        secondaryHex = 0xFF2E2A5E,
        accentHex = 0xFF7C4DFF,
      ),
      CatBackgroundDef(
        id = "tropical_cabana",
        name = "Sunset Beach Cabana",
        description = "Golden ocean horizon, gentle turquoise waves, and swaying palm silhouettes.",
        cost = 65_000L,
        primaryHex = 0xFFFFE0B2,
        secondaryHex = 0xFF4DD0E1,
        accentHex = 0xFFFF7043,
      ),
      CatBackgroundDef(
        id = "crystal_shrine",
        name = "Amethyst Crystal Shrine",
        description = "Luminous floating quartz spires in a tranquil subterranean grotto.",
        cost = 180_000L,
        primaryHex = 0xFF2A1845,
        secondaryHex = 0xFF4A2574,
        accentHex = 0xFFE040FB,
      ),
      CatBackgroundDef(
        id = "neon_cyber_lounge",
        name = "Neon Lo-Fi Lounge",
        description = "Synthwave skyline, glowing grid floor, and midnight cyber-cat vibes.",
        cost = 500_000L,
        primaryHex = 0xFF110D26,
        secondaryHex = 0xFF1D1147,
        accentHex = 0xFF00E5FF,
      ),
      CatBackgroundDef(
        id = "thanksgiving_feast_hall",
        name = "Autumn Harvest Feast Hall",
        description = "Warm candlelight, golden garlands, and a grand Thanksgiving banquet backdrop.",
        cost = 0L,
        primaryHex = 0xFF4E2300,
        secondaryHex = 0xFF8D3B00,
        accentHex = 0xFFFFB300,
        isEventExclusive = true,
        turkeyCost = 10L,
        eventGroup = "Thanksgiving",
      ),
      CatBackgroundDef(
        id = "thanksgiving_pumpkin_meadow",
        name = "Twilight Pumpkin Meadow",
        description = "Glowing carved pumpkins, crisp harvest moon, and drifting crimson maple leaves.",
        cost = 0L,
        primaryHex = 0xFF2B132C,
        secondaryHex = 0xFF6A1B33,
        accentHex = 0xFFFF7043,
        isEventExclusive = true,
        turkeyCost = 18L,
        eventGroup = "Thanksgiving",
      ),
      CatBackgroundDef(
        id = "cornucopia_citadel",
        name = "Cornucopia Citadel",
        description = "Grand Thanksgiving Part 2 banquet spires aglow with amber lanterns & harvest banners.",
        cost = 0L,
        primaryHex = 0xFF3E1900,
        secondaryHex = 0xFF7B2E00,
        accentHex = 0xFFFFD54F,
        isEventExclusive = true,
        turkeyCost = 22L,
        eventGroup = "Thanksgiving Part 2",
      ),
      CatBackgroundDef(
        id = "golden_gravy_falls",
        name = "Golden Gravy Falls",
        description = "Cascading molten amber waterfalls plunging beneath Cranberry Meadow stars — Part 2!",
        cost = 0L,
        primaryHex = 0xFF210E1F,
        secondaryHex = 0xFF6E260E,
        accentHex = 0xFFFFAB00,
        isEventExclusive = true,
        turkeyCost = 30L,
        eventGroup = "Thanksgiving Part 2",
      ),
    )

  val blogPosts: List<BlogPostDef> =
    listOf(
      BlogPostDef(
        version = "v23.0",
        date = "Nov 2026",
        title = "Playable Part 2 Map Zones, Fishing, Feast Cooking, Boss Battle & Frozen Teaser!",
        summary = "Tap any zone on the Part 2 Feast Map to teleport! Play Cranberry Harvest, Spiced Pie Dash, Golden Gravy Fishing, Turkey Feast Cooking & battle Mecha-Gobbler Prime for a shocking December 1st Cutscene + Limited-Time Reward!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Interactive Map Teleporting: Click any landmark on Turkey Gato's Part 2 Map to travel straight into 5 brand-new playable island zones!",
          "Fishing, Cooking & Minigames: Reel in Golden Gravy Koi at Gravy Falls, season & sear a 5-Star feast at Turkey Gato's Banquet, or beat Cranberry Meadow & Spiced Pie Bakery for Turkeys, Purrs & rare Gems!",
          "Cornucopia Citadel Boss Battle: Challenge Mecha-Gobbler Prime with timed Shields, Catnip Heals & Cranberry Surges!",
          "Boss Glitch Cutscene & Limited Reward: Push the Boss to the brink of defeat to witness a glitched blackout, a Christmas Cat slowly thawing inside an ice cube ('Fully Melts On December 1st.'), and claim the limited Melting Solstice Crown!",
        ),
        fullArticleParagraphs = listOf(
          "Your Part 2 Feast Map isn't just a preview atlas anymore — every single island landmark now teleports you into a dedicated interactive zone!",
          "Cast your line at Golden Gravy Falls Fishing Pier, whisk up recipes at Spiced Pie Bakery, tap ripe harvest berries in Cranberry Meadow, or run the 3-stage grill in Turkey Gato's Banquet Kitchen to stack Purrs, Turkeys, Pass XP, and occasional Rare Gems.",
          "Ready for the main event? Teleport to Cornucopia Citadel to battle Mecha-Gobbler Prime! Dodge incoming beams and chip the Boss down — right before defeat, a dramatic Cutscene triggers where something cold glitches the Boss off the screen, revealing a frozen Christmas Cat melting toward December 1st and awarding the Limited-Time Melting Solstice Crown!",
        ),
      ),
      BlogPostDef(
        version = "v20.0",
        date = "Nov 2026",
        title = "Thanksgiving Event: Part Two, Leaderboard Renovation & Cloud Sync Fix!",
        summary = "Thanksgiving Part Two is HERE with +3 Golden Gobblers, 3.0x Gravy Rush, Mythic Banquet Coats, 2 Part 2 Rooms, renovated Podium Leaderboard & Firestore Permissions Fix!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Thanksgiving Event: Part Two: Golden Gobblers (+3 Turkeys!), 3.0x MEGA Gravy Rush, Mythic Roasted Crown Calico & Velvet Gravy Tabby coats, Cornucopia Horn, Golden Gravy Scepter, Cornucopia Royale Theme & 2 Part 2 Rooms!",
          "Cornucopia Sovereign Achievement: Earn 15 lifetime Turkeys to unlock the Mythic Cornucopia Sovereign badge in Quests.",
          "Renovated Global Leaderboard: Cleaner Top-3 Podium showcase, streamlined Weekly Rewards bar, pinned Your Rank chip & crisp formatted Purr badges.",
          "Firestore PERMISSION_DENIED Fix: Resolved missing cloud schema allowances for Room Backgrounds and limited background Exist Counters.",
        ),
        fullArticleParagraphs = listOf(
          "Welcome to Thanksgiving Event: Part Two! Everything teased on Turkey Gato's Part 2 Feast Map is now live across the Sanctuary.",
          "During every hourly 5-minute rush window, shimmering Golden Gobblers now spawn alongside regular Turkeys — tap a Golden Gobbler for +3 Turkeys at once! Hourly Festival buffs also upgrade to 1.75x Regular and 3.0x Gravy Rush during Mega hours (33% chance every hour). Spend Turkeys in the Festival Store for Mythic Roasted Crown Calico, Velvet Gravy Tabby, Cornucopia Horn, Golden Gravy Scepter, Cornucopia Royale Theme, Cornucopia Citadel, and Golden Gravy Falls room backgrounds!",
          "We also gave the Leaderboard screen a ground-up visual renovation: enjoy a clean Top-3 Podium showcase, a compact Weekly Rewards banner that keeps rankings front-and-center, and formatted Purr badges with zero layout crowding.",
          "Finally, we squashed the Firestore PERMISSION_DENIED cloud sync error by updating security validation for custom Sanctuary Room Backgrounds and Limited Background Exist Counters.",
        ),
      ),
      BlogPostDef(
        version = "v17.0",
        date = "Nov 2026",
        title = "Purchasable Cat Room Backgrounds, Universal Slide-Nav, Quick Music & Frenzy Rebalance!",
        summary = "Buy & equip 7 custom Sanctuary Room Backgrounds, slide across ALL 3 navigation layouts, open the Music Player in 1 tap from the Sanctuary, tougher Purr Frenzy combo, 5-minute Festival Store window, cleaner Settings & auto-opening Update Blog!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Purchasable Cat Room Backgrounds: Upgrade your cat's stage beyond the standard room! Unlock Sakura Tea Garden, Golden Autumn Porch, Starlight Observatory, Sunset Beach Cabana, Amethyst Crystal Shrine & Neon Lo-Fi Lounge in Shop -> Customize.",
          "Slide-to-Navigate on ALL 3 Layouts: Smooth finger-sliding navigation with crisp haptic ticks now works on Pill Dock, Bottom Bar, AND Side Rail!",
          "Quick-Access Music Player & Cleaner Settings: Moved the Studio Music Player right into the Sanctuary header & More menu. Settings now stays super clean with Free Themes only (Limited Themes live in Inventory) and no redundant Widget button.",
          "Tougher Purr Frenzy & 5-Minute Festival Store: Purr Frenzy now takes real rapid-tapping skill to charge and hold, and the Thanksgiving Festival Store opens for the same exciting 5-minute rush window each hour!",
        ),
        fullArticleParagraphs = listOf(
          "Welcome to PurrTap v17.0 — packed with community-requested polish, deeper customization, and tighter gameplay balance!",
          "You can now purchase and equip custom Sanctuary Room Backgrounds for your cat in Shop -> Customize! Swap the classic sunlit parlor for a drifting Sakura Tea Garden, a midnight Starlight Observatory, a glowing Amethyst Crystal Shrine, or a Neon Lo-Fi Lounge.",
          "Navigation just got even smoother: the tactile finger-sliding gesture from the Pill Dock now works across ALL 3 navigation formats (Floating Pill Dock, Bottom Bar, and Side Rail). We also moved the Studio Music Player out of Settings into a dedicated 1-tap button in the Sanctuary header and More menu, cleaned up Settings to show Free Themes only (while Limited Themes stay organized in Inventory), and removed the duplicate Widgets button.",
          "Finally, Purr Frenzy Combo has been rebalanced to feel truly earned — requiring faster tapping to charge and steady rhythm to maintain — and the Thanksgiving Festival Store now opens exclusively during the 5-minute hourly rush window alongside wild Turkey spawns. Plus, whenever a new update drops, this Update Blog will pop up automatically so you never miss what's new!",
        ),
      ),
      BlogPostDef(
        version = "v15.0",
        date = "Nov 2026",
        title = "Cleaner Sanctuary Menu, 5m Turkey Rush, 12-Hex Ascension Tree & Organized Inventory!",
        summary = "Decluttered Sanctuary with a unified More menu, organized Weather + Moonlight countdown inside the Weather hub, 5-minute hourly Turkey spawns, 6 new Honeycomb Ascension upgrades, strict Event-ordered Inventory, and rare Gems & Turkeys rebalance!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Cleaner Sanctuary & More Menu: Consolidated Inventory, Style, Widgets, Part 2 Map, Daily Rewards, Settings, Account & Admin into a single sleek More menu — plus streamlined Weather pill outside & Moonlight Countdown moved inside Weather!",
          "5-Minute Turkey Spawn Rush: When the Thanksgiving Festival rerolls each hour, wild Turkeys now only spawn during the first 5 minutes so every catch feels exciting before waiting for the next reroll.",
          "12-Hex Honeycomb Ascension Tree: Added 6 outer-ring upgrades (Crystal Radar, Dreaming Catnap, Season Tactician, Paw Supernova, Sanctuary Symphony, and Cosmic Nine Lives).",
          "Event-Organized Inventory & Currency Rebalance: Inventory now separates items strictly by Event (Thanksgiving, November Pass, Gem Vault, Daily Streaks, Starlight Archive, Code Vault) and rebalanced Gems & Turkeys to feel truly rewarding!",
        ),
        fullArticleParagraphs = listOf(
          "PurrTap v15.0 delivers a cleaner, calmer Sanctuary experience while deepening late-game strategy!",
          "First, we decluttered the main Sanctuary screen: Inventory, Style, Widgets, Daily Rewards, Settings, Part 2 Map, Account, and Admin tools now live inside a clean More button & Sanctuary Menu sheet. We also simplified the Weather pill outside and moved the live Moonlight / Bloodmoon countdown card directly inside the newly organized Game Weather sheet.",
          "Second, Thanksgiving Part One Turkeys and Rare Sanctuary Gems have been carefully rebalanced! Wild Turkeys now spawn exclusively during a 5-minute rush window right after each hourly Festival reroll, and Rare Gems drop at tighter, more coveted rates across taps, quests, streaks, passes, and prestige resets so every Gem and Turkey earned feels genuinely rewarding.",
          "Finally, the Honeycomb Ascension Tree doubles from 6 to 12 interconnected hexes with real mechanics across taps, passive purrs, offline catnaps, Pass XP, Gems, and starter toys — and your Inventory now strictly separates every collectible by Event so Starlight Catnip archive gear never mixes with Thanksgiving items!",
        ),
      ),
      BlogPostDef(
        version = "v14.0",
        date = "Nov 2026",
        title = "Thanksgiving Event Part One, Turkey Gato, Part 2 Map & Mobile Tree + Widgets Button!",
        summary = "Starlight Catnip Event is officially over! Welcome Thanksgiving Part One with Hourly Regular/Mega Festivals (33% Mega!), Turkeys every 10s, Turkey Gato NPC & Part 2 Map (Nov 22), Mobile-reachable Ascension Tree & a main-screen Widgets button!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Thanksgiving Event: Part One: Rerolls every hour with a 33% chance to erupt into a MEGA Festival! Catch Turkeys spawning every 10s (20% Regular / 40% Mega) across your screen to buy limited Thanksgiving Cat Skins, Accessories & Themes!",
          "Turkey Gato & Part 2 Feast Map (Coming Nov 22nd): Meet Turkey Gato inside the Event Boutique! Chat with him enough times to unlock an interactive Part 2 Map you can view anytime — plus Festival Quests & Nov 22 Sneak Peek!",
          "Ascension Tree Mobile Reachability Fix: Rebuilt Honeycomb Ascension Tree as a true full-screen safe-inset overlay with 48dp top zoom controls, scrollable inspector, and one-tap Hex Quick-Select chips!",
          "Main-Screen Widgets Button & Gallery: Added a visible Widgets button right on the Sanctuary so you can preview and pin Home Screen Widgets effortlessly!",
        ),
        fullArticleParagraphs = listOf(
          "The Starlight Catnip Event has officially concluded — thank you to every caretaker who lit lanterns with us! Today kicks off Thanksgiving Event: Part One!",
          "Every single hour, a fresh Thanksgiving Festival rolls open with a 67% chance of a Regular Festival (1.5x Purrs, 20% Turkey spawn rate every 10s) and a 33% chance of a MEGA Festival (2.25x Purrs, 40% Turkey spawn rate every 10s). Wild Turkeys pop up right on your screen — tap them for +1 Turkey and complete Hourly Festival Quests for bonus Turkeys!",
          "Limited Thanksgiving Cat Skins (Pumpkin Pie Tabby, Cranberry Harvest, Plumage Pheasant), Accessories (Harvest Pilgrim Hat, Autumn Leaf Scarf, Golden Turkey Plush), and Themes (Cranberry Feast, Golden Cornucopia) can only be purchased with Turkeys. Greet Turkey Gato inside the festival and keep talking to unlock the permanent Part 2 Feast Map previewing what lands on November 22nd!",
          "On the usability front, the Honeycomb Ascension Tree now uses proper full-screen safe insets with accessible 48dp controls so every button is easy to reach on mobile, and a new Widgets button sits directly on your Sanctuary screen!",
        ),
      ),
      BlogPostDef(
        version = "v13.0",
        date = "Nov 2026",
        title = "Side Rail Layout Polish, Full-Screen Honeycomb Ascension Tree & Home Screen Widgets!",
        summary = "Fixed tight Side Rail formatting across all screens, rebuilt Gato Ascensión into a full-screen Honeycomb upgrade lattice, and launched interactive Android Home Screen Widgets!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Side Rail Responsive Polish: Headers, segmented tabs, Rebirth/Pass badges, and Shop rows now adapt cleanly without clipping or text squishing when using Side Rail on phones!",
          "Full-Screen Honeycomb Ascension Tree: Opening the Ascension Tree now launches a full-screen interactive hexagonal honeycomb map with glowing energy conduits, pan/zoom controls & node inspector!",
          "Home Screen Widgets (2 Styles): Pin PurrTap Sanctuary or PurrTap Mini Cat from your Home Screen or Settings -> Preferences to track Purrs, +Tap/+PPS, Weather & pet your cat right from your home screen!",
        ),
        fullArticleParagraphs = listOf(
          "PurrTap v13.0 focuses on spatial clarity, prestige spectacle, and instant sanctuary access right from your Android Home Screen.",
          "First, every Sanctuary screen—Sanctuary, Shop, Quests, Ranks, and Guide—now adapts smoothly when Side Rail navigation is enabled on handheld screens. Top headers, balance chips, segmented buttons, and Event/Pass banners automatically reflow so text stays sharp and tappable.",
          "Second, Gato Ascensión upgrades now open in a dedicated Full-Screen Honeycomb Ascension Tree! Explore your celestial hex nodes arranged around the Golden Sanctuary Core with live prerequisite beams, pan-and-zoom navigation, and one-tap Shard unlocking.",
          "Finally, you can now add two interactive Jetpack Glance Home Screen Widgets (PurrTap Sanctuary & PurrTap Mini Cat)! View your live Purrs balance, active +Tap/+PPS, Gems, Daily streak, and current Weather—and tap 'Pet Cat' directly on your home screen to earn Purrs before you even open the app.",
        ),
      ),
      BlogPostDef(
        version = "v12.0",
        date = "Nov 2026",
        title = "Layout Formats, Smooth Pass Rebalance, Profile Fixes & Weird Signal Static",
        summary = "Fixed Profile Picture & Banner uploads, added customizable Navigation Formats in Settings, rebalanced November Pass XP scaling (+5% Free / +15% VIP) with full Pass Restart, plus strange Sanctuary signal glitches...",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "PFP & Banner Upload Fix: Fixed photo compression orientation, transparent PNG handling, and live ticker state resets across Local & Cloud profiles!",
          "Navigation Layout Formats in Settings: Switch freely between Bottom Bar, Side Rail, or a sleek Clean Floating Dock anytime in Settings -> Preferences!",
          "Rebalanced November Pass + Pass Restart: Free Tier XP curve nerfed to +5%/tier and VIP Tier nerfed to +15%/tier. Reach Tier 30 to Restart the Pass as many times as you like!",
          "System Static Whisper: Keep an eye on small glitched packets across Sanctuary screens — some say the Dog Vault Keeper wasn't exaggerating...",
        ),
        fullArticleParagraphs = listOf(
          "PurrTap v12.0 brings a big quality-of-life polish pass! First, we squashed the bugs affecting custom Profile Picture (PFP) and Banner uploads—photos now auto-rotate, flatten cleanly, compress reliably under cloud limits, and preview right on your Caretaker Card even before linking a Google account.",
          "Second, you can now pick how you navigate PurrTap! Visit Settings -> Preferences -> Navigation Format to switch between the classic Bottom Bar, a compact Side Rail along the left edge, or a sleek Clean Floating Dock.",
          "Third, we heavily eased the November Pass grind: Free Track XP scaling dropped from +15%/tier to +5%/tier, and VIP Track XP scaling dropped from +30%/tier to +15%/tier! Plus, once you max out the pass, you can Restart the November Pass from Tier 0 (keeping VIP unlocked) to re-earn all 30 tiers of rewards.",
          "Finally... a few caretakers reported tiny corrupted monospace flickers appearing on Sanctuary dashboards. Our engineers assure everyone it's probably harmless weather interference—though the Dog Vault Keeper in Settings seems convinced otherwise.",
        ),
      ),
      BlogPostDef(
        version = "v11.0",
        date = "Nov 2026",
        title = "Studio Music Deck, Interactive Profiles, Exist Counters & Danger Zone!",
        summary = "Remodeled Music Player with scrubbable timeline & speed controls, clickable Leaderboard Profile cards with custom PFP/Banners, Exist Counters on all limited-time items, and a fresh-start Danger Zone!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Remodeled Studio Music Deck: Scrub anywhere with a real progress bar, shuffle tracks, cycle Repeat modes (Off/All/One), adjust playback speed (0.75x–1.5x), or set a Sleep Timer!",
          "Interactive Leaderboard Profiles: Tap any caretaker (including yourself!) on Ranks to view their Banner, Profile Picture, Bio, and Public or Private stats—customize yours anytime.",
          "Limited-Time Exist Counters: Every limited-time Skin, Accessory, and Color Theme now tracks how many exist worldwide (\"X Exist\") across Inventory, Event Shop, Pass & Vaults!",
          "Danger Zone Fresh Start & Sanctuary Admin Controls: Start completely over anytime in Settings -> Preferences -> Danger Zone.",
        ),
        fullArticleParagraphs = listOf(
          "Welcome to PurrTap v11.0! We rebuilt the audio deck from scratch into a Studio Music Player inside Settings. You can now scrub along the track timeline, toggle Shuffle, switch between Repeat Off / All / One, change playback speed from 0.75x to 1.5x, set a Sleep Timer, and manage your custom uploaded audio queue.",
          "Your Ranks page is now social and personal! Clicking anyone on the Global Leaderboard—including your own entry—pops open a custom Profile Card showing their uploaded Banner, Profile Picture, Cat Showcase, Bio, and Lifetime Stats (if set to Public) or a crisp Privacy badge when kept Private.",
          "Every limited-time Skin, Accessory, and Color Theme—from Starlight Festival exclusives and Daily Streak rewards to November Pass Tier 30 Golden Touch, Gem Vault relics, and Hacker Protocol—now carries a live Exist Counter showing how many exist!",
        ),
      ),
      BlogPostDef(
        version = "v9.0",
        date = "Nov 2026",
        title = "Alternative Sign-In Options, Full Blog Reader & Rare Gems!",
        summary = "Flexible Caretaker Sign-In Options without needing a Google account, clickable Update Blog bubbles, and a brand-new rare Gems currency with an exclusive Gem Vault!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Sign-In Options Hub: Players without a Google account can now create a Local Caretaker Account with a Sanctuary ID, optional PIN, Backup Save Code Export/Import, or jump in with Instant Guest Play.",
          "Full Blog Reader Bubbles: Tap any Update Blog card in the Guide tab to pop open the full article bubble with complete notes and issue switcher!",
          "Rare Gems Currency: Find rare +1 Gem drops while petting at active combos, or earn Gems via Quests, Endless Streaks, Rebirths, Gato Ascensións, Weekly Leaderboard payouts, and November Pass tiers.",
          "Exclusive Gem Vault (Shop -> Gems): Spend Gems on Gemstone Purr Charms (+25% permanent boost/lv), 2m Starlight Gem Frenzy (3.0x), Cosmic Weather Summons, Prismatic Weather Lens, Diamond Crown, Prismatic Galaxy Coat, Emerald Royale Theme, and Shard Forging!",
        ),
        fullArticleParagraphs = listOf(
          "Welcome to PurrTap v9.0! We wanted every caretaker—whether you have a Google account, play strictly offline, or share a device—to enjoy clear account choices, deeper patch notes, and an exciting ultra-rare progression currency.",
          "1. Sign-In Options Without a Google Account: Tapping More -> Account on the Sanctuary, Ranks, or Settings now opens our Sign-In Options dialog. You can set up a named Local Caretaker Account complete with a unique Sanctuary ID code, optional 4-digit PIN protection, and one-tap Save Backup Code Export & Import so you can back up or move your sanctuary anytime. Google Sign-In via Credential Manager remains available whenever you want Cloud Sync and Global Ranks.",
          "2. Full Update Blog Reader Bubbles: Previously, Update Blog cards only showed short bullet summaries. Now, clicking any release card opens an interactive Update Blog Reader Bubble displaying the full story behind every update plus Next/Previous issue navigation.",
          "3. Rare Gems & The Gem Vault: Gems are a rare luxury currency separate from Purrs and Hardcore Purrs—they survive Rebirths and Ascensions! Find them rarely on high-combo taps (1.75x+ combo), or earn them through mid/endgame Quests, Day 7/14 Streaks, Weekly Rank rewards, Rebirths (+1 Gem), and Gato Ascensións (+4 Gems). Visit Shop -> Gems to forge permanent Gemstone Purr Charms, unleash 3.0x Starlight Gem Frenzy surges, summon Cosmic Skies, and collect the Prismatic Galaxy coat and Diamond Catnip Crown!",
        ),
      ),
      BlogPostDef(
        version = "v7.0",
        date = "Nov 2026",
        title = "November Pass, Hourly Pass Quests, Weather Particles & Inventory",
        summary = "Full-screen live Weather Particles, a dedicated Inventory for Limited-Time & Battle Pass collectibles, Hourly Battle Pass Quests, and scaled Free & VIP XP curves!",
        readTimeLabel = "3 min read",
        authorTag = "Sanctuary Team",
        highlights = listOf(
          "Main Screen Weather Particles: Watch falling autumn leaves, drifting snow, cherry blossoms, twinkling meteors, raindrops, and golden sparkles animate live across the Sanctuary screen!",
          "Dedicated Inventory: Tap More -> Inventory or Shop -> Customize to browse and equip Event-organized Skins, Accessories, and Themes—keeping Customization focused on permanent styles.",
          "Hourly Battle Pass Quests: Three fresh Pass Quests arrive every hour to award scaling Pass XP!",
          "Balanced Battle Pass XP Curves: Free Track starts at 100 XP and scales +5% per tier; VIP Track starts at 500 XP and increases by +15% every tier (rebalanced in v12.0)!",
        ),
        fullArticleParagraphs = listOf(
          "Your Sanctuary sky now comes alive with full-screen ambient particle physics! Every time our randomized Game Weather shifts, crisp visual atmosphere fills the stage—from fluttering Autumn Maple Leaves and Sakura Petals to glistening Snowflakes, Shooting Stars, Aurora Ribbons, and shimmering 24K Golden Rain.",
          "We also introduced the 30-Tier November Pass with both a Free Track and a prestige VIP Track (unlocked at 1 Gato Shard and 100+ Upgrades). Free Tiers start at 100 XP and grow +5% per tier, while VIP Tiers start at 500 XP and climb +15% each tier (rebalanced in v12.0). Three Hourly Pass Quests refresh at the top of every hour with scaling XP payouts!",
          "Reaching VIP Tier 30 unlocks the Golden Touch Cat Skin: every 30 minutes it stays equipped, your Sanctuary erupts into Golden Rain—granting 4.0x all Purr gains for 3 minutes! And to keep Customization tidy, all Event, Daily Prize, Battle Pass, and Gem Vault collectibles live inside your dedicated Inventory.",
        ),
      ),
      BlogPostDef(
        version = "v6.0",
        date = "Nov 2026",
        title = "Endless Streaks, Weekly Rank Rewards, Offline Earnings & Lifetime Stats",
        summary = "Keep growing your Daily Streak forever, collect Weekly Leaderboard prizes, earn Purrs while away, unlock 7 new achievements, and view full Lifetime Stats in Settings!",
        readTimeLabel = "3 min read",
        authorTag = "Economy & Systems",
        highlights = listOf(
          "Endless Daily Streaks: Daily Streaks never cap at 14 days—keep streaking forever with scaling +0.5x cycle multipliers, +150 Purrs/day streak scaling, Cycle 2+ weekly upgrade drops, and +1 Gato Shard every 30 days!",
          "Weekly Leaderboard Rewards: Every week pays out Purrs, Gato Shards, and bonus Upgrades based on your Global Leaderboard rank (or Sanctuary Qualifier tier).",
          "Offline Earnings & Welcome Back Report: Your Auto-Purr Sanctuary keeps earning while you're away (up to 24h) and greets you with exact time away and Purrs made!",
          "7 New Achievements & Badges: 24 total Badges & Quests including Fortnight Guardian, Endless Devotion, Phoenix Kitten, Astral Gato, Sky Sovereign, Whisker Deity, and Galaxy Purr Emperor.",
          "Lifetime Player Stats in Settings: Open Settings -> Stats to inspect everything since the day you joined!",
        ),
        fullArticleParagraphs = listOf(
          "Devoted caretakers deserve rewards that never hit a ceiling. In v6.0 we removed the 14-day cap so Daily Streaks scale endlessly—Day 15, Day 30, Day 100, and beyond! Each new 14-day cycle adds +0.5x to base daily Purrs plus +150 Purrs per streak day, while every 30th day awards a coveted +1 Gato Shard.",
          "Weekly Leaderboard Rewards now reset every Monday UTC, granting bonus Purrs, Shards, Gems, and free Upgrade levels for everyone on the Ranks tab—even local caretakers receive the Weekly Sanctuary Bonus.",
          "Finally, your Auto-Purr Sanctuary continues working while you're away (up to 24 hours per session at 50% efficiency, rising to 75% once you Ascend, or 90%/48h with Dreaming Catnap). Whenever you return, a Welcome Back report calculates the exact duration gone and Purrs earned, alongside a full Lifetime Stats ledger inside Settings.",
        ),
      ),
      BlogPostDef(
        version = "v5.0",
        date = "Nov 2026",
        title = "Gato Ascensión, Ascension Tree, Hardcore Purrs & Cat SFX Fix",
        summary = "Major prestige & Hardcore overhaul plus rebuilt SoundPool cat purring & meowing audio!",
        readTimeLabel = "2 min read",
        authorTag = "Audio & Prestige",
        highlights = listOf(
          "Rebirths Preserve Daily Streak & Color Themes: Prestiging now keeps your Daily Rewards streak and all unlocked Color Themes alongside Limited Event items.",
          "Hardcore Purrs Currency: Hardcore Mode now uses an isolated Hardcore Purrs wallet and upgrade set that only appear while inside Hardcore Mode—preventing cross-mode cheating.",
          "Added Gato Ascensión & Ascension Tree: Spend 5 Rebirths to Ascend (resetting Rebirths back to 1 and bonus multiplier back to 0) to earn 1 valuable Gato Shard for permanent Ascension Tree upgrades!",
          "Fixed Cat Purr & Meow SFX: Switched tap audio to pre-cached SoundPool WAVs at 44.1kHz with realistic 25Hz purr rumble & expressive meows.",
        ),
        fullArticleParagraphs = listOf(
          "PurrTap v5.0 introduced Gato Ascensión—a thrilling second layer of prestige. Once you accumulate 5 Rebirths, you can Ascend: resetting your Rebirth count back to 1 and Rebirth bonus multiplier to 0 in exchange for 1 precious Gato Shard.",
          "Spend Gato Shards on the permanent Honeycomb Ascension Tree! Hardcore Mode also gained its own sealed Hardcore Purrs currency wallet so challenge runs stay 100% fair!",
          "On the audio side, we replaced legacy streaming buffers with pre-synthesized 44.1kHz WAV buffers loaded directly into SoundPool—producing warm, realistic 25Hz feline purrs and gentle meows every single time you pet your cat.",
        ),
      ),
      BlogPostDef(
        version = "v4.0",
        date = "Nov 2026",
        title = "Rebirths, Hardcore Challenge, Nov 18 Finale & Theme Verification",
        summary = "Fixed theme requirement enforcement, extended the Starlight Festival through Nov 18 with a Thanksgiving Event sneak peek, and added Rebirths + Hardcore Mode!",
        readTimeLabel = "2 min read",
        authorTag = "Core Balance",
        highlights = listOf(
          "Theme Requirements Enforced: Unlockable themes now verify both their gameplay milestone requirements and Purr cost before unlocking.",
          "Starlight Catnip Festival Archive: Older Starlight event collectibles are now safely preserved inside their own Archive section in Inventory.",
          "Added Rebirths: Reset active run Purrs, Shop Upgrades, and standard Skins/Accessories (Event items, Daily Streak & Themes stay safe!) for a stacking +0.5x multiplier.",
          "Added Hardcore Mode: High unlock requirements (1M Lifetime Purrs, 5K Pets, 50 Upgrades, 5 Badges), 3.5x shop costs, 0.75x earnings, and 2x faster combo decay.",
        ),
        fullArticleParagraphs = listOf(
          "This update tightened game balance and fixed a bug where Event Color Themes could be claimed without hitting their gameplay milestones. Every Event Theme now checks its exact milestone thresholds alongside its Purr price.",
          "We also introduced Rebirths—doubling in price with each prestige while granting a stacking +0.5x multiplier—and Hardcore Mode for veteran caretakers who clear 1M Lifetime Purrs, 5,000 Pets, 50 Upgrade levels, and 5 Badges.",
        ),
      ),
      BlogPostDef(
        version = "v3.0",
        date = "Nov 2026",
        title = "Event Pop-Up Boutique, Custom Music Uploads & 12 Weathers",
        summary = "Tap any Live Event card to pop open the Limited-Time Event Shop, upload your own audio files to the Music Player, experience 12 dynamic Game Weathers, and unlock 14 days of Daily Rewards!",
        readTimeLabel = "2 min read",
        authorTag = "Features & Audio",
        highlights = listOf(
          "Clicking the Live Event card opens an interactive Pop-Up Boutique with limited-time Cat Skins, Accessories, and Color Themes.",
          "Upload your own MP3, WAV, or audio files directly into the Settings Built-In Music Player!",
          "Expanded Sanctuary Game Weather to 12 atmospheric skies (Petal Breeze, Morning Mist, Sunshower Rainbow, Starfall Sky, Aurora Borealis, Autumn Gust, and more).",
          "Doubled Daily Rewards to a 14-Day Prize Calendar including bonus Cat Skins, Accessories, Upgrade Levels, and a Velvet Rose Gold Color Theme.",
        ),
        fullArticleParagraphs = listOf(
          "In v3.0 we transformed Live Events from simple multipliers into a full interactive Event Pop-Up Boutique where you can browse Festival Skins, Accessories, and Limited Themes.",
          "We also upgraded the Built-In Settings Music Player so you can upload your own local MP3, WAV, or M4A tracks, and randomized Sanctuary Game Weather across 12 atmospheric skies every minute without needing any device location permissions.",
        ),
      ),
    )

  val howToPlaySteps: List<Pair<String, String>> =
    listOf(
      "1. Pet Your Cat & Hunt Rare Gems" to
        "Tap your cat for purrs, meows, and Purr Frenzy combos. Keeping combos at 1.75x+ gives a rare chance to discover +1 Gem (boosted during Bloodmoon, Aurora, Meteor Shower, or Golden Rain—or with Crystal Radar!). Spend Gems in Shop -> Gems.",
      "2. Clean Sanctuary More Menu & Account Backups" to
        "Tap More in the Sanctuary header to jump to Inventory, Style, Daily Rewards, Home Widgets, Settings, Account/Save Backup Codes, and Admin tools without screen clutter.",
      "3. 5-Minute Hourly Turkey Rush & Part Two Store" to
        "At the top of each hour the Thanksgiving Part 2 Festival rerolls (33% Gravy Rush 3.0x!). Wild Turkeys & +3 Golden Gobblers spawn and the Festival Store opens for the first 5 minutes of every hour.",
      "4. Organized Inventory & November Pass" to
        "Tap November Pass to climb 30 tiers via Hourly Pass Quests, and open More -> Inventory to browse limited collectibles neatly grouped by Event.",
      "5. Rebirth, 12-Hex Honeycomb Ascensión & Weather" to
        "Rebirth grants +0.25x multipliers & +1 Gem while keeping Streaks, Themes, Gems, and Limited items. At 5 Rebirths, Ascend for +1 Gato Shard & +4 Rare Gems to unlock any of the 12 Honeycomb Tree hexes! Tap the Weather pill anytime for Weather Buffs & Midnight Moonlight Countdown.",
    )

  val faqItems: List<FaqItemDef> =
    listOf(
      FaqItemDef(
        "How do I play and back up my save without a Google Account?",
        "Tap More -> Account & Sign-In on the Sanctuary or the Account button on Ranks/Settings. You can switch between Instant Guest Mode, a named Local Caretaker Account (with a Sanctuary ID code & optional PIN lock + Export/Import Save Code), or Google Sign-In for automatic cloud sync.",
      ),
      FaqItemDef(
        "How do I earn Rare Gems and what do they unlock?",
        "Gems are a scarce luxury currency preserved across Rebirths and Ascensions! Find them rarely on Critical Pets (0.2% normal or 0.4% boosted during Bloodmoon, Aurora, Meteor Shower, and Golden Rain), or earn them via mid/endgame Quests, Day 7/14 Streaks, Weekly Top-25 Rank payouts, Rebirths (+1 Gem), Ascensions (+4 Gems), and November Pass milestones. Spend them in Shop -> Gems!",
      ),
      FaqItemDef(
        "When do Turkeys & +3 Golden Gobblers spawn in Thanksgiving Part Two?",
        "Whenever the hourly Festival rerolls at the top of the hour (67% Regular / 33% 3.0x Gravy Rush), wild Turkeys and Golden Gobblers (+3 Turkeys!) spawn and the Event Shop opens exclusively during the first 5 minutes! Once the 5-minute rush ends, spawning pauses until the next hourly reroll.",
      ),
      FaqItemDef(
        "Where did Weather's Moonlight Countdown, Inventory, Style, and Settings move?",
        "Moonlight & Bloodmoon's live countdown now lives cleanly inside the Weather popup (tap the Weather pill at the top right). Inventory, Style, Widgets, Daily Rewards, Part 2 Map, Settings, and Account tools are neatly organized inside the More button in the Sanctuary header!",
      ),
      FaqItemDef(
        "How does the Inventory organize Limited-Time collectibles?",
        "Inventory separates every limited item cleanly by Event in order—Thanksgiving Event (Part 1 & 2), November Pass, Gem Vault Relics, Daily Check-In Streaks, retired Starlight Catnip Archive, and the Secret Code Vault—so items from different events are never mixed together.",
      ),
      FaqItemDef(
        "How do the 12 Honeycomb Ascension Tree upgrades work?",
        "Gato Ascensión requires 5 Rebirths, resetting Rebirths back to 1 while granting +1 Gato Shard and +4 Rare Gems. Open the Honeycomb Ascension Tree to spend Shards across 12 interconnected hexes boosting click power, auto-purrs, prices, crit luck, weather buffs, offline catnaps, Pass XP, Gem drops, and Rebirth starters!",
      ),
    )

  fun formatCompactPurrs(amount: Long): String {
    return when {
      amount >= 1_000_000_000_000_000_000L ->
        String.format(Locale.US, "%.2fQT", amount / 1_000_000_000_000_000_000.0)
      amount >= 1_000_000_000_000_000L ->
        String.format(Locale.US, "%.2fqd", amount / 1_000_000_000_000_000.0)
      amount >= 1_000_000_000_000L ->
        String.format(Locale.US, "%.2fT", amount / 1_000_000_000_000.0)
      amount >= 1_000_000_000L -> String.format(Locale.US, "%.2fB", amount / 1_000_000_000.0)
      amount >= 1_000_000L -> String.format(Locale.US, "%.2fM", amount / 1_000_000.0)
      amount >= 10_000L -> String.format(Locale.US, "%.1fK", amount / 1_000.0)
      else -> NumberFormat.getNumberInstance(Locale.US).format(amount)
    }
  }

  fun computeClickPower(upgradeLevels: Map<String, Long>): Long {
    val base = 1L
    val bonus =
      upgrades
        .filter { it.category == UpgradeCategory.CLICK }
        .sumOf { def -> (upgradeLevels[def.id] ?: 0L) * def.powerPerLevel }
    return base + bonus
  }

  fun computeClickPower(profile: UserProfile): Long = computeClickPower(profile.upgradeLevels)

  fun computeBasePps(upgradeLevels: Map<String, Long>): Long {
    return upgrades
      .filter { it.category == UpgradeCategory.PER_SECOND }
      .sumOf { def -> (upgradeLevels[def.id] ?: 0L) * def.powerPerLevel }
  }

  fun computeBasePps(profile: UserProfile): Long = computeBasePps(profile.upgradeLevels)

  const val HARDCORE_PRICE_MULTIPLIER = 3.5
  const val HARDCORE_EARNINGS_MULTIPLIER = 0.75
  const val HARDCORE_REQ_PURRS = 1_000_000L
  const val HARDCORE_REQ_PETS = 5_000L
  const val HARDCORE_REQ_UPGRADES = 50L
  const val HARDCORE_REQ_BADGES = 5

  const val REBIRTH_BASE_COST = 100_000L
  const val REBIRTH_MULTIPLIER_STEP = 0.5
  const val ASCENSION_REQUIRED_REBIRTHS = 5L
  const val ASCENSION_RESET_REBIRTH_COUNT = 1L

  fun meetsHardcoreRequirements(profile: UserProfile, rebirthCount: Long): Boolean {
    val totalUpgrades = profile.upgradeLevels.values.sum()
    return profile.totalPurrsEarned >= HARDCORE_REQ_PURRS &&
      profile.totalPets >= HARDCORE_REQ_PETS &&
      (totalUpgrades >= HARDCORE_REQ_UPGRADES || rebirthCount >= 1L) &&
      profile.unlockedBadgeIds.size >= HARDCORE_REQ_BADGES
  }

  fun effectiveUpgradeCost(
    def: ShopUpgradeDef,
    level: Long,
    isHardcore: Boolean,
    unlockedAscensionIds: List<String> = emptyList(),
  ): Long {
    val discount = if ("asc_bargain_paws" in unlockedAscensionIds) 0.75 else 1.0
    val raw =
      if (!isHardcore) {
        def.costForLevel(level).toDouble()
      } else {
        def.baseCost * HARDCORE_PRICE_MULTIPLIER * (def.costMultiplier + 0.08).pow(level.toDouble())
      }
    return (raw * discount).roundToLong().coerceAtLeast(1L)
  }

  fun effectiveItemCost(
    baseCost: Long,
    isHardcore: Boolean,
    unlockedAscensionIds: List<String> = emptyList(),
  ): Long {
    if (baseCost <= 0L) return 0L
    val discount = if ("asc_bargain_paws" in unlockedAscensionIds) 0.75 else 1.0
    val raw = if (isHardcore) baseCost * HARDCORE_PRICE_MULTIPLIER else baseCost.toDouble()
    return (raw * discount).roundToLong().coerceAtLeast(1L)
  }

  fun rebirthCost(rebirthCount: Long, isHardcore: Boolean): Long {
    val safeCount = rebirthCount.coerceIn(0L, 28L).toInt()
    val normalCost = REBIRTH_BASE_COST * (1L shl safeCount)
    return if (isHardcore) normalCost * 2L else normalCost
  }

  fun rebirthMultiplier(
    rebirthBonusSteps: Long,
    unlockedAscensionIds: List<String> = emptyList(),
  ): Double {
    val step =
      if ("asc_rebirth_echo" in unlockedAscensionIds) 0.45 else 0.25
    return 1.0 + (rebirthBonusSteps.coerceAtLeast(0L) * step)
  }

  fun canUnlockAscensionNode(
    node: AscensionUpgradeDef,
    state: ChallengeProgressState,
  ): Pair<Boolean, String?> {
    if (node.id in state.unlockedAscensionIds) {
      return false to "Already Unlocked"
    }
    if (
      node.prerequisiteIds.isNotEmpty() &&
        node.prerequisiteIds.none { it in state.unlockedAscensionIds }
    ) {
      val reqNames =
        node.prerequisiteIds.mapNotNull { pid -> ascensionUpgrades.find { it.id == pid }?.name }
      return false to "Requires ${reqNames.joinToString(" or ")}"
    }
    if (state.gatoShards < node.shardCost) {
      return false to "Need ${node.shardCost} Gato Shard(s)"
    }
    return true to null
  }

  const val GEM_FRENZY_COST = 4L
  const val GEM_FRENZY_DURATION_MS = 120_000L
  const val GEM_FRENZY_MULTIPLIER = 1.85
  const val GEM_WEATHER_SUMMON_COST = 3L
  const val GEM_PRISM_LENS_COST = 10L
  const val GEM_SHARD_FORGE_COST = 25L
  const val REBIRTH_GEM_REWARD = 1L
  const val ASCENSION_GEM_REWARD = 4L
  const val GEM_DROP_RATE_NORMAL_PER_1000 = 2 // 0.2% per high-combo (1.75x+) pet
  const val GEM_DROP_RATE_BOOSTED_PER_1000 = 4 // 0.4% during Bloodmoon/Aurora/Meteor/Golden Rain

  fun gemDropRatePer1000(
    boostedSky: Boolean,
    unlockedAscensionIds: List<String> = emptyList(),
  ): Int {
    val base = if (boostedSky) GEM_DROP_RATE_BOOSTED_PER_1000 else GEM_DROP_RATE_NORMAL_PER_1000
    return if ("asc_gem_seeker" in unlockedAscensionIds) {
      (base * 1.5).toInt()
    } else {
      base
    }
  }

  fun gemCharmCost(currentLevel: Long): Long = 5L + (currentLevel.coerceAtLeast(0L) * 3L)

  fun gemCharmMultiplier(currentLevel: Long): Double = 1.0 + (currentLevel.coerceAtLeast(0L) * 0.10)

  fun isEventItemExemptPattern(patternId: String): Boolean {
    return patterns.find { it.id == patternId }?.let {
      it.isEventExclusive &&
        (it.unlockSourceLabel == "Limited Event" ||
          it.unlockSourceLabel == "Thanksgiving Part 1" ||
          it.unlockSourceLabel == "Thanksgiving Part 2" ||
          it.unlockSourceLabel == "November Pass" ||
          it.unlockSourceLabel == "November Pass VIP" ||
          it.unlockSourceLabel == "Gem Vault")
    } == true
  }

  fun isEventItemExemptAccessory(accessoryId: String): Boolean {
    return accessories.find { it.id == accessoryId }?.let {
      it.isEventExclusive &&
        (it.unlockSourceLabel == "Limited Event" ||
          it.unlockSourceLabel == "Thanksgiving Part 1" ||
          it.unlockSourceLabel == "Thanksgiving Part 2" ||
          it.unlockSourceLabel == "December 1st Teaser" ||
          it.unlockSourceLabel == "November Pass" ||
          it.unlockSourceLabel == "November Pass VIP" ||
          it.unlockSourceLabel == "Gem Vault")
    } == true
  }

  const val NOVEMBER_PASS_MAX_TIER = 30
  const val NOVEMBER_PASS_FREE_BASE_XP = 100L
  const val NOVEMBER_PASS_FREE_GROWTH = 1.05
  const val NOVEMBER_PASS_VIP_BASE_XP = 500L
  const val NOVEMBER_PASS_VIP_GROWTH = 1.15
  const val NOVEMBER_PASS_XP_PER_TIER = 100L
  const val NOVEMBER_PASS_VIP_SHARD_COST = 1L
  const val NOVEMBER_PASS_VIP_REQUIRED_UPGRADES = 100L
  const val NOVEMBER_FREE_SKIN_ID = "november_maple_tabby"
  const val GOLDEN_TOUCH_SKIN_ID = "golden_touch_cat"
  const val GOLDEN_TOUCH_INTERVAL_MS = 30L * 60L * 1000L
  const val GOLDEN_RAIN_DURATION_MS = 3L * 60L * 1000L
  const val GOLDEN_RAIN_MULTIPLIER = 2.0

  fun isNovemberPassCompleted(passState: BattlePassState): Boolean {
    val freeDone = freeTierFromXp(passState.passXp) >= NOVEMBER_PASS_MAX_TIER
    val vipDone = !passState.vipUnlocked || vipTierFromXp(passState.passXp) >= NOVEMBER_PASS_MAX_TIER
    return freeDone && vipDone
  }

  val novemberPassTiers: List<BattlePassTierDef> =
    (1..NOVEMBER_PASS_MAX_TIER).map { tier ->
      val freeReward =
        when (tier) {
          30 ->
            BattlePassRewardItem(
              kind = PassRewardKind.SKIN,
              label = "November Maple Skin (Limited)",
              itemId = NOVEMBER_FREE_SKIN_ID,
            )
          10 ->
            BattlePassRewardItem(
              kind = PassRewardKind.GEM,
              label = "+1 Rare Gem",
              amount = 1L,
            )
          20 ->
            BattlePassRewardItem(
              kind = PassRewardKind.GEM,
              label = "+1 Rare Gem",
              amount = 1L,
            )
          5, 15, 25 -> {
            val upg = upgrades[(tier / 5) % upgrades.size]
            BattlePassRewardItem(
              kind = PassRewardKind.UPGRADE_LEVEL,
              label = "+1 ${upg.name} Lv",
              amount = 1L,
              itemId = upg.id,
            )
          }
          else -> {
            val purrAmt = 150L * tier + (if (tier > 20) 1_500L else 0L)
            BattlePassRewardItem(
              kind = PassRewardKind.PURRS,
              label = "+${formatCompactPurrs(purrAmt)} Purrs",
              amount = purrAmt,
            )
          }
        }

      val vipReward =
        when (tier) {
          30 ->
            BattlePassRewardItem(
              kind = PassRewardKind.SKIN,
              label = "Golden Touch Cat Skin",
              itemId = GOLDEN_TOUCH_SKIN_ID,
            )
          25 ->
            BattlePassRewardItem(
              kind = PassRewardKind.GATO_SHARD,
              label = "+1 Gato Shard",
              amount = 1L,
            )
          5 ->
            BattlePassRewardItem(
              kind = PassRewardKind.GEM,
              label = "+1 Rare Gem",
              amount = 1L,
            )
          15 ->
            BattlePassRewardItem(
              kind = PassRewardKind.GEM,
              label = "+2 Rare Gems",
              amount = 2L,
            )
          10 ->
            BattlePassRewardItem(
              kind = PassRewardKind.ACCESSORY,
              label = "Maple Harvest Hat",
              itemId = "harvest_beret",
            )
          20 ->
            BattlePassRewardItem(
              kind = PassRewardKind.ACCESSORY,
              label = "24K Golden Bell Collar",
              itemId = "golden_collar",
            )
          4, 8, 12, 16, 24, 28 -> {
            val upg = upgrades[(tier / 4 + 2) % upgrades.size]
            val lvGain = if (tier >= 24) 2L else 1L
            BattlePassRewardItem(
              kind = PassRewardKind.UPGRADE_LEVEL,
              label = "+$lvGain ${upg.name} Lv",
              amount = lvGain,
              itemId = upg.id,
            )
          }
          else -> {
            val vipPurrs = 1_200L * tier + (if (tier >= 20) 15_000L else 0L)
            BattlePassRewardItem(
              kind = PassRewardKind.PURRS,
              label = "+${formatCompactPurrs(vipPurrs)} Purrs",
              amount = vipPurrs,
            )
          }
        }

      BattlePassTierDef(tier = tier, freeReward = freeReward, vipReward = vipReward)
    }

  fun freeStepXpForTier(tier: Int): Long {
    if (tier <= 1) return NOVEMBER_PASS_FREE_BASE_XP
    val safeTier = tier.coerceIn(1, NOVEMBER_PASS_MAX_TIER)
    return (NOVEMBER_PASS_FREE_BASE_XP * NOVEMBER_PASS_FREE_GROWTH.pow(safeTier - 1)).roundToLong()
  }

  fun freeTotalXpForTier(tier: Int): Long {
    if (tier <= 0) return 0L
    val safeTier = tier.coerceIn(1, NOVEMBER_PASS_MAX_TIER)
    var sum = 0L
    for (t in 1..safeTier) {
      sum += freeStepXpForTier(t)
    }
    return sum
  }

  fun freeTierFromXp(xp: Long): Int {
    var remaining = xp.coerceAtLeast(0L)
    var reached = 0
    for (t in 1..NOVEMBER_PASS_MAX_TIER) {
      val step = freeStepXpForTier(t)
      if (remaining >= step) {
        remaining -= step
        reached = t
      } else {
        break
      }
    }
    return reached
  }

  fun freeXpProgressInCurrentTier(xp: Long): Pair<Long, Long> {
    var remaining = xp.coerceAtLeast(0L)
    for (t in 1..NOVEMBER_PASS_MAX_TIER) {
      val step = freeStepXpForTier(t)
      if (remaining >= step) {
        remaining -= step
      } else {
        return remaining to step
      }
    }
    val maxStep = freeStepXpForTier(NOVEMBER_PASS_MAX_TIER)
    return maxStep to maxStep
  }

  fun vipStepXpForTier(tier: Int): Long {
    if (tier <= 1) return NOVEMBER_PASS_VIP_BASE_XP
    val safeTier = tier.coerceIn(1, NOVEMBER_PASS_MAX_TIER)
    return (NOVEMBER_PASS_VIP_BASE_XP * NOVEMBER_PASS_VIP_GROWTH.pow(safeTier - 1)).roundToLong()
  }

  fun vipTotalXpForTier(tier: Int): Long {
    if (tier <= 0) return 0L
    val safeTier = tier.coerceIn(1, NOVEMBER_PASS_MAX_TIER)
    var sum = 0L
    for (t in 1..safeTier) {
      sum += vipStepXpForTier(t)
    }
    return sum
  }

  fun vipTierFromXp(xp: Long): Int {
    var remaining = xp.coerceAtLeast(0L)
    var reached = 0
    for (t in 1..NOVEMBER_PASS_MAX_TIER) {
      val step = vipStepXpForTier(t)
      if (remaining >= step) {
        remaining -= step
        reached = t
      } else {
        break
      }
    }
    return reached
  }

  fun vipXpProgressInCurrentTier(xp: Long): Pair<Long, Long> {
    var remaining = xp.coerceAtLeast(0L)
    for (t in 1..NOVEMBER_PASS_MAX_TIER) {
      val step = vipStepXpForTier(t)
      if (remaining >= step) {
        remaining -= step
      } else {
        return remaining to step
      }
    }
    val maxStep = vipStepXpForTier(NOVEMBER_PASS_MAX_TIER)
    return maxStep to maxStep
  }

  fun passTierFromXp(xp: Long): Int = freeTierFromXp(xp)

  val battlePassQuestPool: List<BattlePassQuestTemplate> =
    listOf(
      BattlePassQuestTemplate(
        id = "bp_pets_45",
        title = "Hourly Scritch Sprint",
        description = "Pet your cat 45 times this hour.",
        kind = PassQuestKind.PETS,
        target = 45L,
        baseXpReward = 65L,
        tierScaleFactor = 0.35,
      ),
      BattlePassQuestTemplate(
        id = "bp_pets_120",
        title = "Purr Marathon",
        description = "Pet your cat 120 times this hour.",
        kind = PassQuestKind.PETS,
        target = 120L,
        baseXpReward = 110L,
        tierScaleFactor = 0.50,
      ),
      BattlePassQuestTemplate(
        id = "bp_purrs_2k",
        title = "Purr Collector",
        description = "Earn 2,000 Purrs this hour.",
        kind = PassQuestKind.PURRS,
        target = 2_000L,
        baseXpReward = 70L,
        tierScaleFactor = 0.36,
      ),
      BattlePassQuestTemplate(
        id = "bp_purrs_25k",
        title = "Golden Whisker Haul",
        description = "Earn 25,000 Purrs this hour.",
        kind = PassQuestKind.PURRS,
        target = 25_000L,
        baseXpReward = 125L,
        tierScaleFactor = 0.55,
      ),
      BattlePassQuestTemplate(
        id = "bp_upgrades_2",
        title = "Workshop Tune-Up",
        description = "Buy 2 shop upgrades this hour.",
        kind = PassQuestKind.UPGRADES,
        target = 2L,
        baseXpReward = 80L,
        tierScaleFactor = 0.40,
      ),
      BattlePassQuestTemplate(
        id = "bp_upgrades_5",
        title = "Sanctuary Expansion",
        description = "Buy 5 shop upgrades this hour.",
        kind = PassQuestKind.UPGRADES,
        target = 5L,
        baseXpReward = 135L,
        tierScaleFactor = 0.58,
      ),
      BattlePassQuestTemplate(
        id = "bp_weather_2",
        title = "Sky Chaser",
        description = "Witness 2 weather shifts this hour.",
        kind = PassQuestKind.WEATHER_SHIFTS,
        target = 2L,
        baseXpReward = 75L,
        tierScaleFactor = 0.38,
      ),
      BattlePassQuestTemplate(
        id = "bp_weather_4",
        title = "Storm Spotter",
        description = "Witness 4 weather shifts this hour.",
        kind = PassQuestKind.WEATHER_SHIFTS,
        target = 4L,
        baseXpReward = 120L,
        tierScaleFactor = 0.52,
      ),
      BattlePassQuestTemplate(
        id = "bp_crits_12",
        title = "Purr Frenzy Surge",
        description = "Land 12 Frenzy Crit pets (1.65x+) this hour.",
        kind = PassQuestKind.COMBO_CRITS,
        target = 12L,
        baseXpReward = 95L,
        tierScaleFactor = 0.45,
      ),
    )

  fun currentEpochHour(nowMillis: Long = System.currentTimeMillis()): Long {
    return (nowMillis / 3_600_000L).coerceAtLeast(1L)
  }

  fun millisUntilNextEpochHour(nowMillis: Long = System.currentTimeMillis()): Long {
    return (3_600_000L - (nowMillis % 3_600_000L)).coerceAtLeast(1_000L)
  }

  fun selectHourlyPassQuestIds(hourEpoch: Long): List<String> {
    val grouped = battlePassQuestPool.groupBy { it.kind }.values.toList()
    val rng = kotlin.random.Random(hourEpoch * 6364136223846793005L + 17L)
    return grouped.shuffled(rng).take(3).map { bucket ->
      bucket[rng.nextInt(bucket.size)].id
    }
  }

  fun evaluateHourlyPassQuests(passState: BattlePassState): List<ActiveBattlePassQuest> {
    val freeTier = freeTierFromXp(passState.passXp)
    val vipTier = vipTierFromXp(passState.passXp)
    val nextFreeStep =
      if (freeTier < NOVEMBER_PASS_MAX_TIER) freeStepXpForTier(freeTier + 1)
      else freeStepXpForTier(NOVEMBER_PASS_MAX_TIER)
    val nextVipStep =
      if (passState.vipUnlocked && vipTier < NOVEMBER_PASS_MAX_TIER) vipStepXpForTier(vipTier + 1)
      else 0L
    val governingStep =
      if (passState.vipUnlocked && freeTier >= NOVEMBER_PASS_MAX_TIER) {
        maxOf(nextFreeStep, nextVipStep)
      } else if (passState.vipUnlocked) {
        maxOf(nextFreeStep, (nextVipStep * 0.55).roundToLong())
      } else {
        nextFreeStep
      }

    val ids =
      passState.activeQuestIds.ifEmpty { selectHourlyPassQuestIds(passState.questHourEpoch.coerceAtLeast(1L)) }
    return ids.mapNotNull { id ->
      val template = battlePassQuestPool.find { it.id == id } ?: return@mapNotNull null
      val progress =
        when (template.kind) {
          PassQuestKind.PETS -> passState.hourPets
          PassQuestKind.PURRS -> passState.hourPurrsEarned
          PassQuestKind.UPGRADES -> passState.hourUpgradesBought
          PassQuestKind.WEATHER_SHIFTS -> passState.hourWeatherShifts
          PassQuestKind.COMBO_CRITS -> passState.hourComboCrits
        }
      val xpReward =
        maxOf(template.baseXpReward, (governingStep * template.tierScaleFactor).roundToLong())
      ActiveBattlePassQuest(
        template = template,
        progress = progress,
        xpReward = xpReward,
        isClaimed = template.id in passState.claimedPassQuestIds,
      )
    }
  }

  fun totalUpgradesForPassVip(
    profile: UserProfile,
    challenge: ChallengeProgressState,
    stats: PlayerStatsState,
  ): Long {
    val activeCombined =
      profile.upgradeLevels.values.sum() + challenge.hardcoreUpgradeLevels.values.sum()
    return maxOf(stats.totalUpgradesPurchased, activeCombined)
  }

  fun meetsNovemberVipRequirements(
    profile: UserProfile,
    challenge: ChallengeProgressState,
    stats: PlayerStatsState,
  ): Pair<Boolean, String?> {
    val upgradeCount = totalUpgradesForPassVip(profile, challenge, stats)
    val unmet = mutableListOf<String>()
    if (challenge.gatoShards < NOVEMBER_PASS_VIP_SHARD_COST) {
      unmet.add("$NOVEMBER_PASS_VIP_SHARD_COST Gato Shard")
    }
    if (upgradeCount < NOVEMBER_PASS_VIP_REQUIRED_UPGRADES) {
      unmet.add("$NOVEMBER_PASS_VIP_REQUIRED_UPGRADES Upgrades ($upgradeCount/$NOVEMBER_PASS_VIP_REQUIRED_UPGRADES)")
    }
    return if (unmet.isEmpty()) {
      true to null
    } else {
      false to "VIP requires ${unmet.joinToString(" & ")}!"
    }
  }

  const val VAULT_KEEPER_KICKOUT_TAPS = 7
  const val VAULT_KICKOUT_DURATION_MS = 3L * 60L * 1000L

  const val CAT_COMPUTER_RIDDLE =
    "Silent I stalk beneath the sunbeam, with nine lives to spare and whiskers to twitch. Even the Dog Vault Keeper guards his PC from my kind's doom. What 3-letter creature am I?"
  const val CAT_COMPUTER_HINT =
    "Hint: 3 letters — look at who you pet every day in the Sanctuary."

  fun isValidCatComputerPassword(attempt: String): Boolean {
    val clean = attempt.trim().lowercase(Locale.US)
    return clean == "cat" || clean == "cats" || clean == "gato"
  }

  val vaultKeeperWarnings: List<String> =
    listOf(
      "The World Of Cats is ending soon... The Hackers will destroy everything!",
      "Keep your paws off the terminal! The World Of Cats is ending soon — the Hackers are coming!",
      "Don't you hear the static? The Hackers will destroy everything in the World Of Cats!",
      "Stop poking me! I'm guarding the last clean Purr backup before the Hackers destroy everything!",
      "Quit tapping the Dog Vault Keeper!! The World Of Cats is ending soon and the Hackers will destroy everything!",
      "LAST WARNING! The World Of Cats is ending soon, the Hackers will destroy everything — click me again and I'm kicking you out for 3 minutes!!",
    )

  fun evaluateThemeRequirement(theme: AppColorTheme, profile: UserProfile): Pair<Boolean, String?> {
    if (theme.isFree) return true to null
    if (theme == AppColorTheme.HACKER_PROTOCOL) {
      return false to "Unlock by angering the Dog Vault Keeper in the Secret Code Vault!"
    }
    if (theme.turkeyCost > 0L) {
      return false to "Unlock in Thanksgiving Festival Shop with ${theme.turkeyCost} Turkeys!"
    }
    if (theme.eventCost <= 0L) {
      return false to "Unlock via ${theme.unlockSourceLabel} in Daily Rewards!"
    }
    val upgradeSum = profile.upgradeLevels.values.sum()
    val unmet = mutableListOf<String>()
    if (profile.eventPurrsEarned < theme.reqEventPurrs) {
      unmet.add("${formatCompactPurrs(theme.reqEventPurrs)} Event Purrs")
    }
    if (profile.totalPets < theme.reqTotalPets) {
      unmet.add("${formatCompactPurrs(theme.reqTotalPets)} Lifetime Pets")
    }
    if (upgradeSum < theme.reqUpgradeLevels) {
      unmet.add("${theme.reqUpgradeLevels} Shop Upgrades")
    }
    if (profile.weatherCheckedCount < theme.reqWeatherShifts) {
      unmet.add("${theme.reqWeatherShifts} Weather Shifts")
    }
    return if (unmet.isEmpty()) {
      true to null
    } else {
      false to "Need ${unmet.joinToString(" & ")} first!"
    }
  }

  fun currentPeriodicEvent(nowMillis: Long = System.currentTimeMillis()): Pair<LimitedEventDef, Long> {
    return periodicEvents.first() to millisUntilNextEpochHour(nowMillis)
  }

  fun evaluateMoonlightWindow(
    minuteOfDay: Int,
    morningEndMinute: Int = 360,
    isBloodmoonNight: Boolean,
  ): MoonlightEventState {
    val endMin = morningEndMinute.coerceIn(300, 480) // 5:00 AM to 8:00 AM
    return if (minuteOfDay in 0 until endMin) {
      val minsLeft = (endMin - minuteOfDay).coerceAtLeast(1)
      val h = minsLeft / 60
      val m = minsLeft % 60
      val timeLeft = if (h > 0) "${h}h ${m}m left" else "${m}m left"
      MoonlightEventState(
        phase = if (isBloodmoonNight) MoonlightPhase.BLOODMOON else MoonlightPhase.MOONLIGHT,
        statusText = "Active until Morning • $timeLeft",
      )
    } else {
      val minsUntilMidnight = ((24 * 60) - minuteOfDay).coerceAtLeast(1)
      val h = minsUntilMidnight / 60
      val m = minsUntilMidnight % 60
      val startsIn = if (h > 0) "In ${h}h ${m}m" else "In ${m}m"
      MoonlightEventState(
        phase = MoonlightPhase.INACTIVE,
        statusText = "$startsIn (Midnight–Morning • 33% Bloodmoon)",
      )
    }
  }

  const val MIN_OFFLINE_THRESHOLD_MS = 10_000L
  const val MAX_OFFLINE_SECONDS = 24L * 3600L

  fun computeEndlessDailyPayout(streakDay: Long): EndlessDailyPayout {
    val safeDay = streakDay.coerceAtLeast(1L)
    val zeroBased = safeDay - 1L
    val slotDay = ((zeroBased % 14L) + 1L).toInt()
    val cycleIndex = (zeroBased / 14L) + 1L
    val base = dailyRewards.getOrElse(slotDay - 1) { dailyRewards.first() }
    val cycleMult = 1.0 + ((cycleIndex - 1L) * 0.5)
    val streakScalingBonus = zeroBased * 150L
    val totalPurrs = (base.purrs * cycleMult).roundToLong() + streakScalingBonus
    val bonusShards = if (safeDay % 30L == 0L) 1L else 0L
    val bonusGems =
      when (slotDay) {
        14 -> 2L + (cycleIndex - 1L).coerceAtMost(3L)
        7 -> 1L
        else -> 0L
      }
    val bonusUpgradeLevel =
      (cycleIndex == 1L && base.bonusType == DailyBonusPrizeType.UPGRADE_LEVEL) ||
        (safeDay > 14L && safeDay % 7L == 0L)
    return EndlessDailyPayout(
      streakDay = safeDay,
      cycleIndex = cycleIndex,
      slotDay = slotDay,
      baseReward = base,
      streakMultiplier = cycleMult,
      totalPurrs = totalPurrs,
      bonusShards = bonusShards,
      bonusGems = bonusGems,
      bonusUpgradeLevel = bonusUpgradeLevel,
    )
  }

  fun currentEpochWeek(nowMillis: Long = System.currentTimeMillis()): Long {
    // 1970-01-01 was a Thursday; +3 shifts to Monday-aligned UTC weeks
    val epochDay = nowMillis / 86_400_000L
    return (epochDay + 3L) / 7L
  }

  fun millisUntilNextWeeklyReset(nowMillis: Long = System.currentTimeMillis()): Long {
    val currentWeek = currentEpochWeek(nowMillis)
    val nextWeekStartDay = (currentWeek + 1L) * 7L - 3L
    val nextWeekStartMillis = nextWeekStartDay * 86_400_000L
    return (nextWeekStartMillis - nowMillis).coerceAtLeast(1_000L)
  }

  fun weeklyTierForRank(rank: Int?): WeeklyLeaderboardTier {
    if (rank == null || rank <= 0) return weeklyLeaderboardTiers.last()
    return weeklyLeaderboardTiers.firstOrNull { rank <= it.maxRankInclusive }
      ?: weeklyLeaderboardTiers.last()
  }

  fun computeOfflineEarnings(
    elapsedMillis: Long,
    effectivePps: Long,
    isHardcore: Boolean,
    hasAscended: Boolean,
    unlockedAscensionIds: List<String> = emptyList(),
  ): OfflineEarningsReport? {
    if (elapsedMillis < MIN_OFFLINE_THRESHOLD_MS) return null
    val hasCatnap = "asc_offline_mastery" in unlockedAscensionIds
    val maxSeconds = if (hasCatnap) MAX_OFFLINE_SECONDS * 2L else MAX_OFFLINE_SECONDS
    val cappedSeconds = (elapsedMillis / 1000L).coerceIn(1L, maxSeconds)
    val baseEfficiency = if (hasAscended) 75 else 50
    val efficiencyPercent = (baseEfficiency + (if (hasCatnap) 15 else 0)).coerceAtMost(95)
    val earned =
      if (effectivePps <= 0L) 0L
      else (effectivePps * cappedSeconds * (efficiencyPercent / 100.0)).roundToLong().coerceAtLeast(1L)
    return OfflineEarningsReport(
      elapsedMillis = elapsedMillis,
      cappedSeconds = cappedSeconds,
      purrsEarned = earned,
      isHardcore = isHardcore,
      efficiencyPercent = efficiencyPercent,
      ppsRateUsed = effectivePps,
    )
  }

  const val ADMIN_EMAIL = "silvestrecastaneda9@gmail.com"

  fun isAdminEmail(email: String?): Boolean {
    return email?.trim()?.equals(ADMIN_EMAIL, ignoreCase = true) == true
  }

  fun existStatId(itemType: String, itemId: String): String {
    val prefix =
      when (itemType.uppercase(Locale.US)) {
        "PATTERN" -> "pat_"
        "ACCESSORY" -> "acc_"
        "THEME" -> "thm_"
        "BACKGROUND" -> "bg_"
        else -> "itm_"
      }
    return prefix + itemId
  }

  fun limitedTimeCatalogRefs(): List<Pair<String, String>> {
    val refs = mutableListOf<Pair<String, String>>()
    patterns.filter { it.isEventExclusive }.forEach { refs.add(existStatId("PATTERN", it.id) to "PATTERN") }
    accessories.filter { it.isEventExclusive }.forEach { refs.add(existStatId("ACCESSORY", it.id) to "ACCESSORY") }
    AppColorTheme.entries.filter { !it.isFree }.forEach { refs.add(existStatId("THEME", it.id) to "THEME") }
    backgrounds.filter { it.isEventExclusive }.forEach { refs.add(existStatId("BACKGROUND", it.id) to "BACKGROUND") }
    return refs
  }

  fun formatElapsedDuration(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
      days > 0 -> "${days}d ${hours}h ${minutes}m"
      hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
      minutes > 0 -> "${minutes}m ${seconds}s"
      else -> "${seconds}s"
    }
  }
}
