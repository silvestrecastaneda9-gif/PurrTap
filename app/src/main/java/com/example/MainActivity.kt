package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.audio.SoundManager
import com.example.data.local.LocalGameStorage
import com.example.data.repository.PurrGameRepository
import com.example.data.weather.WeatherRepository
import com.example.ui.auth.GoogleSignInHelper
import com.example.ui.components.ChallengeAndRebirthDialog
import com.example.ui.components.DailyRewardsSheet
import com.example.ui.components.EventShopDialog
import com.example.ui.components.InventoryDialog
import com.example.ui.components.NovemberPassDialog
import com.example.ui.components.OfflineEarningsDialog
import com.example.ui.components.SecretCodeVaultDialog
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SignInOptionsDialog
import com.example.ui.components.WeatherSyncSheet
import com.example.ui.components.WeeklyRewardCelebrationDialog
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.QuestsAndBadgesScreen
import com.example.ui.screens.SanctuaryScreen
import com.example.ui.screens.ShopAndCustomizeScreen
import com.example.ui.theme.PurrTapTheme
import com.example.ui.viewmodel.PurrGameViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

private data class PurrNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val purrNavItems =
  listOf(
    PurrNavItem("sanctuary", "Sanctuary", Icons.Rounded.Pets, Icons.Outlined.Pets),
    PurrNavItem("shop", "Shop", Icons.Rounded.Storefront, Icons.Outlined.Storefront),
    PurrNavItem("quests", "Quests", Icons.Rounded.MilitaryTech, Icons.Outlined.MilitaryTech),
    PurrNavItem("leaderboard", "Ranks", Icons.Rounded.EmojiEvents, Icons.Outlined.EmojiEvents),
    PurrNavItem(
      "guide",
      "Guide",
      Icons.AutoMirrored.Rounded.MenuBook,
      Icons.AutoMirrored.Outlined.MenuBook,
    ),
  )

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AppNavigation()
    }
  }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val uid = currentUser?.uid
  key(uid ?: "guest") {
    PurrTapMainApp(
      currentUserId = uid,
      initialDisplayName = currentUser?.displayName ?: "Feline Friend",
    )
  }
}

@Composable
fun PurrTapMainApp(
  currentUserId: String?,
  initialDisplayName: String,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val viewModel: PurrGameViewModel =
    viewModel(
      key = currentUserId ?: "guest",
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val repository =
              if (currentUserId != null) {
                val databaseId = app.getString(R.string.firestore_database_id)
                PurrGameRepository(FirebaseFirestore.getInstance(databaseId))
              } else {
                null
              }
            PurrGameViewModel(
              repository = repository,
              localStorage = LocalGameStorage(app),
              weatherRepository = WeatherRepository(),
              soundManager = SoundManager(app),
              currentUserId = currentUserId,
              initialDisplayName = initialDisplayName,
            )
          }
        },
    )

  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> viewModel.setAppForeground(true)
        Lifecycle.Event.ON_STOP -> viewModel.setAppForeground(false)
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: "sanctuary"

  var showDailyRewards by remember { mutableStateOf(false) }
  var showWeatherSheet by remember { mutableStateOf(false) }
  var showEventShop by remember { mutableStateOf(false) }
  var showNovemberPass by remember { mutableStateOf(false) }
  var showInventory by remember { mutableStateOf(false) }
  var showChallengeDialog by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showSignInOptions by remember { mutableStateOf(false) }
  var showSecretVault by remember { mutableStateOf(false) }
  var shopInitialSubTab by remember { mutableIntStateOf(0) }
  var isSigningIn by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }

  val triggerGoogleSignIn: () -> Unit = {
    if (!isSigningIn) {
      isSigningIn = true
      GoogleSignInHelper.onGoogleSignInClicked(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = { isSigningIn = false },
        onAuthCancelled = { isSigningIn = false },
        onAuthError = { message ->
          isSigningIn = false
          coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
          }
        },
        scope = coroutineScope,
      )
    }
  }

  LaunchedEffect(viewModel) {
    viewModel.toastEvents.collect { msg ->
      snackbarHostState.currentSnackbarData?.dismiss()
      snackbarHostState.showSnackbar(msg)
    }
  }

  val navigateToTab: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  PurrTapTheme(
    themeMode = uiState.settings.themeMode,
    colorTheme = uiState.settings.colorTheme,
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useNavRail = maxWidth >= 600.dp

      Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
          if (!useNavRail) {
            NavigationBar {
              purrNavItems.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                  selected = selected,
                  onClick = {
                    if (item.route == "shop") shopInitialSubTab = 0
                    navigateToTab(item.route)
                  },
                  icon = {
                    Icon(
                      imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                    )
                  },
                  label = { Text(text = item.label, maxLines = 1) },
                  modifier = Modifier.testTag("nav_${item.route}"),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (useNavRail) {
            NavigationRail {
              purrNavItems.forEach { item ->
                val selected = currentRoute == item.route
                NavigationRailItem(
                  selected = selected,
                  onClick = {
                    if (item.route == "shop") shopInitialSubTab = 0
                    navigateToTab(item.route)
                  },
                  icon = {
                    Icon(
                      imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                    )
                  },
                  label = { Text(text = item.label, maxLines = 1) },
                  modifier = Modifier.testTag("nav_rail_${item.route}"),
                )
              }
            }
          }

          if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
          } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
              Text(
                text = uiState.errorMessage ?: "Something went wrong.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
              )
            }
          } else {
            NavHost(
              navController = navController,
              startDestination = "sanctuary",
              modifier = Modifier.weight(1f).fillMaxSize(),
            ) {
              composable("sanctuary") {
                SanctuaryScreen(
                  uiState = uiState,
                  onPetCat = viewModel::onCatPetted,
                  onOpenDailyRewards = { showDailyRewards = true },
                  onOpenWeatherSync = { showWeatherSheet = true },
                  onOpenEventShop = { showEventShop = true },
                  onOpenNovemberPass = { showNovemberPass = true },
                  onOpenInventory = { showInventory = true },
                  onOpenChallengeDialog = { showChallengeDialog = true },
                  onOpenSettings = { showSettingsSheet = true },
                  onNavigateToCustomize = {
                    shopInitialSubTab = 2
                    navigateToTab("shop")
                  },
                  onSignInClick = { showSignInOptions = true },
                  onSignOut = {
                    GoogleSignInHelper.signOut(credentialManager, coroutineScope)
                  },
                )
              }
              composable("shop") {
                ShopAndCustomizeScreen(
                  uiState = uiState,
                  initialSubTab = shopInitialSubTab,
                  onBuyUpgrade = viewModel::buyUpgrade,
                  onSelectOrUnlockPattern = viewModel::selectOrUnlockPattern,
                  onSelectOrUnlockAccessory = viewModel::selectOrUnlockAccessory,
                  onRenameCat = viewModel::renameCat,
                  onOpenInventory = { showInventory = true },
                  onBuyGemCharm = viewModel::buyGemCharm,
                  onActivateGemFrenzy = viewModel::activateGemFrenzy,
                  onSummonCosmicWeather = viewModel::summonCosmicWeatherWithGems,
                  onUnlockPrismLens = viewModel::unlockPrismLens,
                  onUnlockGemPattern = viewModel::unlockGemPattern,
                  onUnlockGemAccessory = viewModel::unlockGemAccessory,
                  onUnlockGemColorTheme = viewModel::unlockGemColorTheme,
                  onForgeGatoShard = viewModel::forgeGatoShardFromGems,
                )
              }
              composable("quests") {
                QuestsAndBadgesScreen(
                  uiState = uiState,
                  onClaimQuest = viewModel::claimQuest,
                  onEquipBadge = viewModel::equipBadge,
                )
              }
              composable("leaderboard") {
                LeaderboardScreen(
                  currentUserId = currentUserId,
                  uiState = uiState,
                  leaderboardState = leaderboardState,
                  onClaimWeeklyReward = { viewModel.claimWeeklyLeaderboardReward(isAutoRollover = false) },
                  onSignInClick = triggerGoogleSignIn,
                  onOpenSignInOptions = { showSignInOptions = true },
                  isSigningIn = isSigningIn,
                )
              }
              composable("guide") {
                GuideScreen()
              }
            }
          }
        }
      }

      if (showDailyRewards) {
        DailyRewardsSheet(
          dailyStreak = uiState.profile.dailyStreak,
          nextDailyDayIndex = uiState.nextDailyDayIndex,
          nextDailyPayout = uiState.nextDailyPayout,
          canClaimToday = uiState.canClaimDailyReward,
          currencyShortLabel = uiState.activeCurrencyShortLabel,
          onClaim = viewModel::claimDailyReward,
          onDismiss = { showDailyRewards = false },
        )
      }

      if (showWeatherSheet) {
        WeatherSyncSheet(
          weather = uiState.weather,
          onDismiss = { showWeatherSheet = false },
        )
      }

      if (showEventShop) {
        EventShopDialog(
          uiState = uiState,
          onSelectOrUnlockPattern = viewModel::selectOrUnlockPattern,
          onSelectOrUnlockAccessory = viewModel::selectOrUnlockAccessory,
          onSelectOrUnlockColorTheme = { theme ->
            viewModel.selectOrUnlockColorTheme(theme, allowPurchase = true)
          },
          onDismiss = { showEventShop = false },
        )
      }

      if (showNovemberPass) {
        NovemberPassDialog(
          uiState = uiState,
          onUnlockVip = viewModel::unlockNovemberPassVip,
          onClaimTierReward = viewModel::claimPassTierReward,
          onClaimAll = viewModel::claimAllPassRewards,
          onClaimPassQuest = viewModel::claimHourlyPassQuest,
          onDismiss = { showNovemberPass = false },
        )
      }

      if (showInventory) {
        InventoryDialog(
          uiState = uiState,
          onSelectPattern = viewModel::selectOrUnlockPattern,
          onSelectAccessory = viewModel::selectOrUnlockAccessory,
          onApplyColorTheme = { theme ->
            viewModel.selectOrUnlockColorTheme(theme, allowPurchase = false)
          },
          onDismiss = { showInventory = false },
        )
      }

      if (showChallengeDialog) {
        ChallengeAndRebirthDialog(
          uiState = uiState,
          onPerformRebirth = viewModel::performRebirth,
          onPerformGatoAscension = viewModel::performGatoAscension,
          onUnlockAscensionNode = viewModel::unlockAscensionUpgrade,
          onToggleHardcoreMode = viewModel::toggleHardcoreMode,
          onDismiss = { showChallengeDialog = false },
        )
      }

      if (showSettingsSheet) {
        SettingsSheet(
          uiState = uiState,
          settings = uiState.settings,
          onSelectThemeMode = viewModel::setThemeMode,
          onSelectColorTheme = { theme ->
            viewModel.selectOrUnlockColorTheme(theme, allowPurchase = false)
          },
          onToggleSfx = viewModel::setSfxEnabled,
          onSfxVolumeChange = viewModel::setSfxVolume,
          onTestSfx = viewModel::testCatSfx,
          onToggleMusic = viewModel::toggleMusicPlaying,
          onSelectTrackById = viewModel::selectMusicTrackById,
          onSkipTrack = viewModel::skipMusicTrack,
          onMusicVolumeChange = viewModel::setMusicVolume,
          onUploadAudioUri = { uri -> viewModel.importCustomMusicFile(context, uri) },
          onRemoveCustomTrack = viewModel::removeCustomMusicTrack,
          onOpenSignInOptions = {
            showSettingsSheet = false
            showSignInOptions = true
          },
          onOpenSecretVault = {
            showSettingsSheet = false
            showSecretVault = true
          },
          onDismiss = { showSettingsSheet = false },
        )
      }

      if (showSecretVault) {
        SecretCodeVaultDialog(
          uiState = uiState,
          onTalkToKeeper = viewModel::talkToVaultKeeper,
          onDismiss = { showSecretVault = false },
        )
      }

      if (showSignInOptions) {
        SignInOptionsDialog(
          isSignedIn = uiState.isSignedIn,
          currentDisplayName = uiState.profile.displayName,
          accountMode = uiState.settings.accountMode,
          localAccountCode = uiState.settings.localAccountCode,
          hasLocalPin = uiState.settings.localAccountPin.isNotBlank(),
          onDismiss = { showSignInOptions = false },
          onGoogleSignIn = triggerGoogleSignIn,
          onGoogleSignOut = {
            GoogleSignInHelper.signOut(credentialManager, coroutineScope)
          },
          onSaveLocalAccount = viewModel::saveLocalCaretakerAccount,
          onSwitchToGuest = viewModel::switchToGuestCaretaker,
          onExportSaveCode = viewModel::exportSanctuarySaveCode,
          onImportSaveCode = viewModel::importSanctuarySaveCode,
        )
      }

      uiState.pendingOfflineReport?.let { offlineReport ->
        OfflineEarningsDialog(
          report = offlineReport,
          catName = uiState.profile.selectedCatName,
          onDismiss = viewModel::dismissOfflineEarningsReport,
        )
      }

      if (uiState.pendingOfflineReport == null) {
        uiState.pendingWeeklyReward?.let { weeklyReceipt ->
          WeeklyRewardCelebrationDialog(
            receipt = weeklyReceipt,
            isHardcore = uiState.challengeState.isHardcoreMode,
            onDismiss = viewModel::dismissWeeklyRewardReceipt,
          )
        }
      }
    }
  }
}
