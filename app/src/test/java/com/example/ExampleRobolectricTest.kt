package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppSettingsState
import com.example.data.local.LocalGameStorage
import com.example.data.model.BattlePassState
import com.example.data.model.ChallengeProgressState
import com.example.data.model.NavigationLayoutFormat
import com.example.data.model.UserProfile
import com.example.ui.components.ImageEncodingUtil
import com.example.ui.theme.AppColorTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PurrTap", appName)
  }

  @Test
  fun `local sanctuary code and save backup code roundtrip preserves gems and progress`() {
    runBlocking {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val storage = LocalGameStorage(context)

      val code = storage.generateSanctuaryCode()
      assertTrue(code.startsWith("PURR-"))
      assertEquals(14, code.length)

      val profile =
        UserProfile(
          displayName = "LunaCaretaker",
          purrs = 42_000L,
          totalPurrsEarned = 150_000L,
          totalPets = 777L,
          selectedCatName = "Boba",
          selectedPatternId = "prism_galaxy",
          selectedAccessoryId = "diamond_crown",
          unlockedPatterns = listOf("tabby", "prism_galaxy"),
          unlockedAccessories = listOf("none", "diamond_crown"),
          dailyStreak = 12L,
        )
      val challenge =
        ChallengeProgressState(
          rebirthCount = 3L,
          gatoShards = 2L,
          gems = 27L,
          totalGemsEarned = 45L,
          gemCharmLevel = 4L,
          prismLensUnlocked = true,
          turkeys = 19L,
          totalTurkeysEarned = 34L,
          festivalHourEpoch = 498_500L,
          festivalIsMega = true,
          turkeyGatoTalkCount = 5,
          part2MapUnlocked = true,
          festivalQuestHourEpoch = 498_500L,
          claimedFestivalQuestIds = listOf("tq_pets_35", "tq_turkeys_3"),
          festHourPets = 88L,
          festHourTurkeysTapped = 6L,
          festHourUpgradesBought = 2L,
          festHourWeatherShifts = 3L,
        )
      val themes =
        listOf(
          AppColorTheme.TERRACOTTA.id,
          AppColorTheme.EMERALD_ROYALE.id,
          AppColorTheme.AUTUMN_CRANBERRY.id,
        )

      val exported = storage.exportSaveCode(profile, challenge, themes)
      assertTrue(exported.startsWith("PURRSAVE1:"))

      val restored = storage.parseSaveCode(exported)
      assertNotNull(restored)
      assertEquals("LunaCaretaker", restored!!.profile.displayName)
      assertEquals(42_000L, restored.profile.purrs)
      assertEquals("prism_galaxy", restored.profile.selectedPatternId)
      assertEquals("diamond_crown", restored.profile.selectedAccessoryId)
      assertEquals(27L, restored.challenge.gems)
      assertEquals(45L, restored.challenge.totalGemsEarned)
      assertEquals(4L, restored.challenge.gemCharmLevel)
      assertTrue(restored.challenge.prismLensUnlocked)
      assertEquals(19L, restored.challenge.turkeys)
      assertEquals(34L, restored.challenge.totalTurkeysEarned)
      assertEquals(5, restored.challenge.turkeyGatoTalkCount)
      assertTrue(restored.challenge.part2MapUnlocked)
      assertTrue(AppColorTheme.EMERALD_ROYALE.id in restored.unlockedThemes)
      assertTrue(AppColorTheme.AUTUMN_CRANBERRY.id in restored.unlockedThemes)

      storage.saveChallengeProgress(challenge)
      val loadedChallenge = storage.loadChallengeProgress()
      assertEquals(19L, loadedChallenge.turkeys)
      assertEquals(34L, loadedChallenge.totalTurkeysEarned)
      assertEquals(498_500L, loadedChallenge.festivalHourEpoch)
      assertTrue(loadedChallenge.festivalIsMega)
      assertEquals(5, loadedChallenge.turkeyGatoTalkCount)
      assertTrue(loadedChallenge.part2MapUnlocked)
      assertEquals(498_500L, loadedChallenge.festivalQuestHourEpoch)
      assertEquals(listOf("tq_pets_35", "tq_turkeys_3"), loadedChallenge.claimedFestivalQuestIds)
      assertEquals(88L, loadedChallenge.festHourPets)
      assertEquals(6L, loadedChallenge.festHourTurkeysTapped)
      assertEquals(2L, loadedChallenge.festHourUpgradesBought)
      assertEquals(3L, loadedChallenge.festHourWeatherShifts)

      assertNull(storage.parseSaveCode("INVALID_CODE"))
    }
  }

  @Test
  fun `settings navigation format passCompletions and base64 image decoder behave safely`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = LocalGameStorage(context)

    val savedSettings =
      AppSettingsState(navigationFormat = NavigationLayoutFormat.CLEAN_FLOATING)
    storage.saveSettings(savedSettings)
    val loadedSettings = storage.loadSettings()
    assertEquals(NavigationLayoutFormat.CLEAN_FLOATING, loadedSettings.navigationFormat)

    val savedPass =
      BattlePassState(
        passXp = 450L,
        vipUnlocked = true,
        passCompletions = 2L,
      )
    storage.saveBattlePassState(savedPass)
    val loadedPass = storage.loadBattlePassState()
    assertEquals(2L, loadedPass.passCompletions)
    assertEquals(450L, loadedPass.passXp)
    assertTrue(loadedPass.vipUnlocked)

    assertNull(ImageEncodingUtil.decodeBase64ImageBitmap(""))
    assertNull(ImageEncodingUtil.decodeBase64ImageBitmap("==="))

    val bmp = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
    val baos = java.io.ByteArrayOutputStream()
    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
    val validBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP)
    assertNotNull(ImageEncodingUtil.decodeBase64ImageBitmap(validBase64))
  }

  @Test
  fun `widget pet tap updates snapshot purrs passXp and respects hardcore mode`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = LocalGameStorage(context)

    val initialProfile =
      UserProfile(
        displayName = "MochiOwner",
        selectedCatName = "Boba",
        purrs = 100L,
        totalPurrsEarned = 100L,
        totalPets = 5L,
        upgradeLevels = mapOf("feather_wand" to 4L),
      )
    storage.saveProfile(initialProfile)
    storage.saveWidgetWeatherSnapshot("Sunny", 24)
    storage.saveChallengeProgress(ChallengeProgressState(isHardcoreMode = false, gems = 7L))

    val tapSnapshot = storage.recordWidgetPetTap()
    assertEquals("Boba", tapSnapshot.catName)
    assertEquals("Sunny", tapSnapshot.weatherLabel)
    assertEquals(24, tapSnapshot.weatherTempC)
    assertTrue(tapSnapshot.activeBalance > 100L)
    assertEquals(6L, storage.loadProfile().totalPets)
    assertEquals(7L, tapSnapshot.gems)
    assertTrue(storage.loadBattlePassState().passXp >= 2L)

    // Hardcore mode isolates currency and uses Hardcore Purrs balance
    storage.saveChallengeProgress(
      ChallengeProgressState(
        isHardcoreMode = true,
        hardcorePurrs = 250L,
        hardcoreUpgradeLevels = mapOf("feather_wand" to 2L),
        gems = 9L,
      )
    )
    val hcSnapshot = storage.recordWidgetPetTap()
    assertTrue(hcSnapshot.isHardcore)
    assertTrue(hcSnapshot.activeBalance > 250L)
    assertEquals(storage.loadChallengeProgress().hardcorePurrs, hcSnapshot.activeBalance)
    assertEquals(tapSnapshot.activeBalance, storage.loadProfile().purrs)
  }

  @Test
  fun `home screen widget receivers are registered in manifest`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pm = context.packageManager
    val sanctuaryInfo =
      pm.getReceiverInfo(
        android.content.ComponentName(context, com.example.widget.PurrSanctuaryWidgetReceiver::class.java),
        0,
      )
    val miniInfo =
      pm.getReceiverInfo(
        android.content.ComponentName(context, com.example.widget.PurrMiniPetWidgetReceiver::class.java),
        0,
      )
    assertNotNull(sanctuaryInfo)
    assertNotNull(miniInfo)
  }
}
