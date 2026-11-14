package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LocalGameStorage
import com.example.data.model.ChallengeProgressState
import com.example.data.model.UserProfile
import com.example.ui.theme.AppColorTheme
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
      )
    val themes = listOf(AppColorTheme.TERRACOTTA.id, AppColorTheme.EMERALD_ROYALE.id)

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
    assertTrue(AppColorTheme.EMERALD_ROYALE.id in restored.unlockedThemes)

    assertNull(storage.parseSaveCode("INVALID_CODE"))
  }
}
