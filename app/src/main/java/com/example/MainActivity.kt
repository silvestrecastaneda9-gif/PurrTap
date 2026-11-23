package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.data.model.GameCatalog
import com.example.data.model.LeaderboardEntry
import com.example.data.model.NavigationLayoutFormat
import com.example.data.repository.PurrGameRepository
import com.example.data.weather.WeatherRepository
import com.example.ui.auth.GoogleSignInHelper
import com.example.ui.components.AdminPanelDialog
import com.example.ui.components.BossGlitchCutsceneOverlay
import com.example.ui.components.ChallengeAndRebirthDialog
import com.example.ui.components.FeastZoneAreaScreen
import com.example.ui.components.DailyRewardsSheet
import com.example.ui.components.DogCrashOmenOverlay
import com.example.ui.components.EditPublicProfileDialog
import com.example.ui.components.EventShopDialog
import com.example.ui.components.HoneycombAscensionTreeScreen
import com.example.ui.components.InventoryDialog
import com.example.ui.components.LocalPurrFeedback
import com.example.ui.components.MoreMenuDialog
import com.example.ui.components.MusicPlayerDialog
import com.example.ui.components.NovemberPassDialog
import com.example.ui.components.TrillionCutsceneOverlay
import com.example.ui.components.UpdateBlogPopupDialog
import com.example.ui.components.OfflineEarningsDialog
import com.example.ui.components.PlayerProfilePopupDialog
import com.example.ui.components.PurrFeedbackController
import com.example.ui.components.SecretCodeVaultDialog
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SignInOptionsDialog
import com.example.ui.components.SpawnedTurkeyBadge
import com.example.ui.components.ThanksgivingPart2MapDialog
import com.example.ui.components.WeatherSyncSheet
import com.example.ui.components.WeeklyRewardCelebrationDialog
import com.example.ui.components.WidgetsGalleryDialog
import com.example.ui.components.WindowGatoDesktopOverlay
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.QuestsAndBadgesScreen
import com.example.ui.screens.SanctuaryScreen
import com.example.ui.screens.ShopAndCustomizeScreen
import com.example.ui.theme.PurrTapTheme
import com.example.ui.viewmodel.PurrGameViewModel
import com.example.widget.PurrWidgetUpdater
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
      currentUserEmail = currentUser?.email,
      initialDisplayName = currentUser?.displayName ?: "Feline Friend",
    )
  }
}

@Composable
fun PurrTapMainApp(
  currentUserId: String?,
  currentUserEmail: String? = null,
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
              currentUserEmail = currentUserEmail,
              initialDisplayName = initialDisplayName,
              onSyncWidgets = { PurrWidgetUpdater.requestUpdate(app) },
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
  val playbackStatus by viewModel.musicPlaybackStatus.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: "sanctuary"

  var showDailyRewards by remember { mutableStateOf(false) }
  var showMoreMenu by remember { mutableStateOf(false) }
  var showMusicPlayer by remember { mutableStateOf(false) }
  var showWeatherSheet by remember { mutableStateOf(false) }
  var showEventShop by remember { mutableStateOf(false) }
  var showNovemberPass by remember { mutableStateOf(false) }
  var showInventory by remember { mutableStateOf(false) }
  var showChallengeDialog by remember { mutableStateOf(false) }
  var showHoneycombTree by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showWidgetsGallery by remember { mutableStateOf(false) }
  var showPart2Map by remember { mutableStateOf(false) }
  var showSignInOptions by remember { mutableStateOf(false) }
  var showSecretVault by remember { mutableStateOf(false) }
  var selectedLeaderboardProfile by remember { mutableStateOf<Pair<LeaderboardEntry, Int?>?>(null) }
  var showEditPublicProfile by remember { mutableStateOf(false) }
  var showAdminPanel by remember { mutableStateOf(false) }
  var adminInitialTargetUid by remember { mutableStateOf("") }
  var shopInitialSubTab by remember { mutableIntStateOf(0) }
  var isSigningIn by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val uiScope = rememberCoroutineScope()

  LaunchedEffect(uiState.isTurkeyRushActive) {
    if (!uiState.isTurkeyRushActive && showEventShop) {
      showEventShop = false
      snackbarHostState.showSnackbar("Thanksgiving Festival Store closed! Reopens at the next hourly reroll.")
    }
  }

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

  val haptic = LocalHapticFeedback.current
  val feedbackController =
    remember(haptic, viewModel) {
      PurrFeedbackController(
        tap = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          viewModel.playUiTap()
        },
        confirm = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.playUiConfirm()
        },
        slideTick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          viewModel.playPillSlideTick()
        },
      )
    }

  PurrTapTheme(
    themeMode = uiState.settings.themeMode,
    colorTheme = uiState.settings.colorTheme,
  ) {
    CompositionLocalProvider(LocalPurrFeedback provides feedbackController) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val navFormat = uiState.settings.navigationFormat
      val useNavRail =
        navFormat == NavigationLayoutFormat.SIDE_RAIL ||
          (maxWidth >= 600.dp && navFormat == NavigationLayoutFormat.BOTTOM_BAR)
      val isCompactPhoneRail = maxWidth < 600.dp
      val useCleanFloating = navFormat == NavigationLayoutFormat.CLEAN_FLOATING && !useNavRail

      Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
          SnackbarHost(hostState = snackbarHostState) { data ->
            val msg = data.visuals.message
            val lower = msg.lowercase()
            val isRejection =
              lower.contains("need ") ||
                lower.contains("not enough") ||
                lower.contains("locked") ||
                lower.contains("closed") ||
                lower.contains("cannot") ||
                lower.contains("can't") ||
                lower.contains("failed") ||
                lower.contains("error") ||
                lower.contains("invalid") ||
                lower.contains("banned") ||
                lower.contains("timed out") ||
                lower.contains("kicked") ||
                lower.contains("cooldown") ||
                lower.contains("requires") ||
                lower.contains("must ") ||
                lower.contains("rejected")
            val bubbleBg = if (isRejection) Color(0xFFC62828) else Color(0xFF2E7D32)
            Surface(
              color = bubbleBg,
              contentColor = Color.White,
              shape = RoundedCornerShape(24.dp),
              shadowElevation = 6.dp,
              modifier =
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                  .testTag(if (isRejection) "notification_bubble_red" else "notification_bubble_green"),
            ) {
              Text(
                text = msg,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
              )
            }
          }
        },
        bottomBar = {
          if (!useNavRail && !useCleanFloating) {
            var barWidthPx by remember { mutableIntStateOf(0) }
            var barDragIdx by remember {
              mutableIntStateOf(purrNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0))
            }
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .onSizeChanged { barWidthPx = it.width }
                  .pointerInput(purrNavItems.size) {
                    detectHorizontalDragGestures(
                      onDragStart = { offset ->
                        val slotW = (barWidthPx.toFloat() / purrNavItems.size.toFloat()).coerceAtLeast(1f)
                        val idx = (offset.x / slotW).toInt().coerceIn(0, purrNavItems.lastIndex)
                        if (idx != barDragIdx) {
                          barDragIdx = idx
                          feedbackController.slideTick()
                          val targetRoute = purrNavItems[idx].route
                          if (targetRoute == "shop") shopInitialSubTab = 0
                          navigateToTab(targetRoute)
                        }
                      },
                      onHorizontalDrag = { change, _ ->
                        change.consume()
                        val slotW = (barWidthPx.toFloat() / purrNavItems.size.toFloat()).coerceAtLeast(1f)
                        val idx = (change.position.x / slotW).toInt().coerceIn(0, purrNavItems.lastIndex)
                        if (idx != barDragIdx) {
                          barDragIdx = idx
                          feedbackController.slideTick()
                          val targetRoute = purrNavItems[idx].route
                          if (targetRoute == "shop") shopInitialSubTab = 0
                          navigateToTab(targetRoute)
                        }
                      },
                    )
                  }
            ) {
              NavigationBar {
                purrNavItems.forEachIndexed { idx, item ->
                  val selected = currentRoute == item.route
                  NavigationBarItem(
                    selected = selected,
                    onClick = {
                      barDragIdx = idx
                      feedbackController.tap()
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
          } else if (useCleanFloating) {
            val activeIndex =
              purrNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
            var dockWidthPx by remember { mutableIntStateOf(0) }
            var dragTargetIndex by remember { mutableIntStateOf(activeIndex) }
            var isDraggingDock by remember { mutableStateOf(false) }
            val displayedIndex = if (isDraggingDock) dragTargetIndex else activeIndex
            val density = LocalDensity.current

            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .navigationBarsPadding()
                  .padding(horizontal = 16.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                border =
                  BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                  ),
                modifier =
                  Modifier.widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .testTag("clean_floating_nav_dock"),
              ) {
                Box(
                  modifier =
                    Modifier.fillMaxWidth()
                      .padding(horizontal = 8.dp, vertical = 6.dp)
                      .onSizeChanged { dockWidthPx = it.width }
                      .pointerInput(purrNavItems.size) {
                        detectHorizontalDragGestures(
                          onDragStart = { offset ->
                            isDraggingDock = true
                            val slotW =
                              (dockWidthPx.toFloat() / purrNavItems.size.toFloat()).coerceAtLeast(1f)
                            val idx =
                              (offset.x / slotW).toInt().coerceIn(0, purrNavItems.lastIndex)
                            if (idx != dragTargetIndex) {
                              dragTargetIndex = idx
                              feedbackController.slideTick()
                              val targetRoute = purrNavItems[idx].route
                              if (targetRoute == "shop") shopInitialSubTab = 0
                              navigateToTab(targetRoute)
                            }
                          },
                          onHorizontalDrag = { change, _ ->
                            change.consume()
                            val slotW =
                              (dockWidthPx.toFloat() / purrNavItems.size.toFloat()).coerceAtLeast(1f)
                            val idx =
                              (change.position.x / slotW)
                                .toInt()
                                .coerceIn(0, purrNavItems.lastIndex)
                            if (idx != dragTargetIndex) {
                              dragTargetIndex = idx
                              feedbackController.slideTick()
                              val targetRoute = purrNavItems[idx].route
                              if (targetRoute == "shop") shopInitialSubTab = 0
                              navigateToTab(targetRoute)
                            }
                          },
                          onDragEnd = { isDraggingDock = false },
                          onDragCancel = { isDraggingDock = false },
                        )
                      },
                ) {
                  // Smooth sliding active pill indicator behind the icons
                  if (dockWidthPx > 0) {
                    val slotWidthDp =
                      with(density) { (dockWidthPx.toFloat() / purrNavItems.size.toFloat()).toDp() }
                    val indicatorOffset by
                      animateDpAsState(
                        targetValue = slotWidthDp * displayedIndex,
                        animationSpec =
                          spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy,
                          ),
                        label = "pillDockSlider",
                      )
                    Box(
                      modifier =
                        Modifier.offset(x = indicatorOffset)
                          .width(slotWidthDp)
                          .height(50.dp)
                          .clip(RoundedCornerShape(20.dp))
                          .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    purrNavItems.forEachIndexed { idx, item ->
                      val selected = displayedIndex == idx
                      Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier =
                          Modifier.weight(1f)
                            .heightIn(min = 50.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                              feedbackController.tap()
                              dragTargetIndex = idx
                              if (item.route == "shop") shopInitialSubTab = 0
                              navigateToTab(item.route)
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("nav_floating_${item.route}"),
                      ) {
                        Icon(
                          imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                          contentDescription = item.label,
                          tint =
                            if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(20.dp),
                        )
                        Text(
                          text = item.label,
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                          color =
                            if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
          if (useNavRail) {
            var railHeightPx by remember { mutableIntStateOf(0) }
            var railDragIdx by remember {
              mutableIntStateOf(purrNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0))
            }
            NavigationRail(
              windowInsets = WindowInsets(0, 0, 0, 0),
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              modifier = Modifier.widthIn(min = if (isCompactPhoneRail) 64.dp else 80.dp),
            ) {
              Column(
                modifier =
                  Modifier.fillMaxHeight()
                    .onSizeChanged { railHeightPx = it.height }
                    .pointerInput(purrNavItems.size) {
                      detectVerticalDragGestures(
                        onDragStart = { offset ->
                          val slotH = (railHeightPx.toFloat() / purrNavItems.size.toFloat()).coerceAtLeast(1f)
                          val idx = (offset.y / slotH).toInt().coerceIn(0, purrNavItems.lastIndex)
                          if (idx != railDragIdx) {
                            railDragIdx = idx
                            feedbackController.slideTick()
                            val targetRoute = purrNavItems[idx].route
                            if (targetRoute == "shop") shopInitialSubTab = 0
                            navigateToTab(targetRoute)
                          }
                        },
                        onVerticalDrag = { change, _ ->
                          change.consume()
                          val slotH = (railHeightPx.toFloat() / purrNavItems.size.toFloat()).coerceAtLeast(1f)
                          val idx = (change.position.y / slotH).toInt().coerceIn(0, purrNavItems.lastIndex)
                          if (idx != railDragIdx) {
                            railDragIdx = idx
                            feedbackController.slideTick()
                            val targetRoute = purrNavItems[idx].route
                            if (targetRoute == "shop") shopInitialSubTab = 0
                            navigateToTab(targetRoute)
                          }
                        },
                      )
                    }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
              ) {
                purrNavItems.forEachIndexed { idx, item ->
                  val selected = currentRoute == item.route
                  NavigationRailItem(
                    selected = selected,
                    onClick = {
                      railDragIdx = idx
                      feedbackController.tap()
                      if (item.route == "shop") shopInitialSubTab = 0
                      navigateToTab(item.route)
                    },
                    icon = {
                      Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                      )
                    },
                    label = {
                      Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                      )
                    },
                    modifier = Modifier.testTag("nav_rail_${item.route}"),
                  )
                }
              }
            }
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
          }

          if (uiState.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
          } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.weight(1f).fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
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
                  onOpenWeatherSync = { showWeatherSheet = true },
                  onOpenEventShop = {
                    if (uiState.isTurkeyRushActive) {
                      showEventShop = true
                    } else {
                      uiScope.launch {
                        snackbarHostState.showSnackbar(
                          "Festival Store is closed! Opens for 5 min at the next reroll (${uiState.festivalRerollCountdownText})."
                        )
                      }
                    }
                  },
                  onOpenNovemberPass = { showNovemberPass = true },
                  onOpenChallengeDialog = { showChallengeDialog = true },
                  onOpenMoreMenu = { showMoreMenu = true },
                  onOpenMusicPlayer = { showMusicPlayer = true },
                  onDismissAnnouncement = viewModel::dismissGlobalAnnouncement,
                )
              }
              composable("shop") {
                ShopAndCustomizeScreen(
                  uiState = uiState,
                  initialSubTab = shopInitialSubTab,
                  onBuyUpgrade = viewModel::buyUpgrade,
                  onSelectOrUnlockPattern = viewModel::selectOrUnlockPattern,
                  onSelectOrUnlockAccessory = viewModel::selectOrUnlockAccessory,
                  onSelectOrUnlockBackground = viewModel::selectOrUnlockBackground,
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
                  onSelectPlayerProfile = { entry, rank ->
                    selectedLeaderboardProfile = entry to rank
                  },
                  onOpenOwnProfileEditor = { showEditPublicProfile = true },
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

      if (showMoreMenu) {
        MoreMenuDialog(
          uiState = uiState,
          onOpenInventory = { showInventory = true },
          onOpenStyle = {
            shopInitialSubTab = 2
            navigateToTab("shop")
          },
          onOpenDailyRewards = { showDailyRewards = true },
          onOpenWidgets = { showWidgetsGallery = true },
          onOpenPart2Map = { showPart2Map = true },
          onOpenSettings = { showSettingsSheet = true },
          onOpenAccount = { showSignInOptions = true },
          onOpenAdminPanel = {
            adminInitialTargetUid = ""
            showAdminPanel = true
          },
          onOpenMusicPlayer = { showMusicPlayer = true },
          onDismiss = { showMoreMenu = false },
        )
      }

      if (showMusicPlayer) {
        MusicPlayerDialog(
          settings = uiState.settings,
          playbackStatus = playbackStatus,
          onToggleMusic = viewModel::toggleMusicPlaying,
          onSelectTrackById = viewModel::selectMusicTrackById,
          onSkipTrack = viewModel::skipMusicTrack,
          onSeekTo = viewModel::seekMusicTo,
          onCycleRepeatMode = viewModel::cycleMusicRepeatMode,
          onToggleShuffle = viewModel::toggleMusicShuffle,
          onSetPlaybackSpeed = viewModel::setMusicPlaybackSpeed,
          onSetSleepTimer = viewModel::setMusicSleepTimer,
          onMusicVolumeChange = viewModel::setMusicVolume,
          onUploadAudioUri = { uri -> viewModel.importCustomMusicFile(context, uri) },
          onRemoveCustomTrack = viewModel::removeCustomMusicTrack,
          onDismiss = { showMusicPlayer = false },
        )
      }

      uiState.pendingUpdateBlogPost?.let { updatePost ->
        UpdateBlogPopupDialog(
          post = updatePost,
          onDismiss = viewModel::dismissUpdateBlogPost,
        )
      }

      if (showWeatherSheet) {
        WeatherSyncSheet(
          weather = uiState.weather,
          moonlightState = uiState.moonlightState,
          onDismiss = { showWeatherSheet = false },
        )
      }

      if (showEventShop) {
        EventShopDialog(
          uiState = uiState,
          onTalkToTurkeyGato = viewModel::talkToTurkeyGato,
          onOpenPart2Map = {
            showEventShop = false
            showPart2Map = true
          },
          onUnlockTurkeyPattern = viewModel::unlockTurkeyPattern,
          onUnlockTurkeyAccessory = viewModel::unlockTurkeyAccessory,
          onUnlockTurkeyTheme = viewModel::unlockTurkeyTheme,
          onUnlockTurkeyBackground = viewModel::unlockTurkeyBackground,
          onClaimFestivalQuest = viewModel::claimFestivalQuest,
          onDismiss = { showEventShop = false },
        )
      }

      if (uiState.showTrillionCutscene) {
        TrillionCutsceneOverlay(
          catName = uiState.profile.selectedCatName,
          playerName = uiState.profile.displayName,
          onComplete = viewModel::completeTrillionCutscene,
        )
      }

      if (showNovemberPass) {
        NovemberPassDialog(
          uiState = uiState,
          onUnlockVip = viewModel::unlockNovemberPassVip,
          onClaimTierReward = viewModel::claimPassTierReward,
          onClaimAll = viewModel::claimAllPassRewards,
          onClaimPassQuest = viewModel::claimHourlyPassQuest,
          onRestartPass = viewModel::restartNovemberPass,
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
          onOpenPart2Map = {
            showInventory = false
            showPart2Map = true
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
          onOpenHoneycombTree = {
            showChallengeDialog = false
            showHoneycombTree = true
          },
          onDismiss = { showChallengeDialog = false },
        )
      }

      if (showSettingsSheet) {
        SettingsSheet(
          uiState = uiState,
          settings = uiState.settings,
          playbackStatus = playbackStatus,
          onSelectThemeMode = viewModel::setThemeMode,
          onSelectNavigationFormat = viewModel::setNavigationFormat,
          onSelectColorTheme = { theme ->
            viewModel.selectOrUnlockColorTheme(theme, allowPurchase = false)
          },
          onToggleSfx = viewModel::setSfxEnabled,
          onSfxVolumeChange = viewModel::setSfxVolume,
          onTestSfx = viewModel::testCatSfx,
          onToggleMusic = viewModel::toggleMusicPlaying,
          onSelectTrackById = viewModel::selectMusicTrackById,
          onSkipTrack = viewModel::skipMusicTrack,
          onSeekTo = viewModel::seekMusicTo,
          onCycleRepeatMode = viewModel::cycleMusicRepeatMode,
          onToggleShuffle = viewModel::toggleMusicShuffle,
          onSetPlaybackSpeed = viewModel::setMusicPlaybackSpeed,
          onSetSleepTimer = viewModel::setMusicSleepTimer,
          onMusicVolumeChange = viewModel::setMusicVolume,
          onUploadAudioUri = { uri -> viewModel.importCustomMusicFile(context, uri) },
          onRemoveCustomTrack = viewModel::removeCustomMusicTrack,
          onOpenSignInOptions = {
            showSettingsSheet = false
            showSignInOptions = true
          },
          onOpenProfileEditor = {
            showSettingsSheet = false
            showEditPublicProfile = true
          },
          onOpenAdminPanel = {
            showSettingsSheet = false
            adminInitialTargetUid = ""
            showAdminPanel = true
          },
          onDeleteAllData = {
            showSettingsSheet = false
            viewModel.deleteAllPlayerData(context)
          },
          onOpenSecretVault = {
            showSettingsSheet = false
            showSecretVault = true
          },
          onOpenWidgetsGallery = {
            showSettingsSheet = false
            showWidgetsGallery = true
          },
          onNotifyToast = { msg ->
            coroutineScope.launch {
              snackbarHostState.currentSnackbarData?.dismiss()
              snackbarHostState.showSnackbar(msg)
            }
          },
          onDismiss = { showSettingsSheet = false },
        )
      }

      if (showWidgetsGallery) {
        WidgetsGalleryDialog(
          uiState = uiState,
          onNotifyToast = { msg ->
            coroutineScope.launch {
              snackbarHostState.currentSnackbarData?.dismiss()
              snackbarHostState.showSnackbar(msg)
            }
          },
          onDismiss = { showWidgetsGallery = false },
        )
      }

      if (showPart2Map) {
        ThanksgivingPart2MapDialog(
          onTeleportToZone = { landmarkId ->
            showPart2Map = false
            viewModel.teleportToFeastZone(landmarkId)
          },
          onDismiss = { showPart2Map = false },
        )
      }

      if (showSecretVault && !uiState.showWindowGatoDesktop) {
        SecretCodeVaultDialog(
          uiState = uiState,
          onTalkToKeeper = viewModel::talkToVaultKeeper,
          onUnlockCatComputer = viewModel::unlockCatComputer,
          onOpenWindowGatoDesktop = viewModel::openWindowGatoDesktop,
          onDismissExitConfrontation = viewModel::dismissWindowGatoConfrontation,
          onDismiss = {
            viewModel.dismissWindowGatoConfrontation()
            showSecretVault = false
          },
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

      selectedLeaderboardProfile?.let { (entry, rank) ->
        val isSelf =
          (currentUserId != null && entry.userId == currentUserId) ||
            (currentUserId == null && entry.userId == uiState.profile.userId)
        PlayerProfilePopupDialog(
          entry = entry,
          rank = rank,
          isSelf = isSelf,
          isAdmin = uiState.isAdmin,
          onEditOwnProfile = {
            selectedLeaderboardProfile = null
            showEditPublicProfile = true
          },
          onModeratePlayer = { targetEntry ->
            selectedLeaderboardProfile = null
            adminInitialTargetUid = targetEntry.userId
            showAdminPanel = true
          },
          onDismiss = { selectedLeaderboardProfile = null },
        )
      }

      if (showEditPublicProfile) {
        EditPublicProfileDialog(
          currentProfile = uiState.profile,
          onSave = { statsPublic, bio, avatarB64, bannerB64 ->
            viewModel.updatePublicProfileCustomization(statsPublic, bio, avatarB64, bannerB64)
            showEditPublicProfile = false
          },
          onDismiss = { showEditPublicProfile = false },
        )
      }

      if (showAdminPanel && uiState.isAdmin) {
        val leaderboardEntries =
          (leaderboardState as? com.example.ui.viewmodel.UiState.Success)?.data ?: emptyList()
        AdminPanelDialog(
          uiState = uiState,
          leaderboardEntries = leaderboardEntries,
          initialTargetUid = adminInitialTargetUid,
          onModeratePlayer = viewModel::adminModeratePlayer,
          onClearModeration = viewModel::adminClearModeration,
          onGrantSelfCurrency = viewModel::adminGrantSelfCurrency,
          onSetWeather = viewModel::adminSetWeather,
          onSendGlobalMessage = viewModel::adminSendGlobalMessage,
          onDeleteGlobalMessage = viewModel::adminDeleteGlobalMessage,
          onDismiss = { showAdminPanel = false },
        )
      }

      if (uiState.isBanned && !uiState.isAdmin) {
        Surface(
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxSize().testTag("ban_lock_overlay"),
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.Block,
              contentDescription = "Account Banned",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text(
              text = "Account Suspended",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
              text =
                uiState.activeModeration?.reason?.ifBlank {
                  "Your account was banned by Sanctuary Administration."
                } ?: "Your account was banned by Sanctuary Administration.",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            if (uiState.isSignedIn) {
              Button(
                onClick = { GoogleSignInHelper.signOut(credentialManager, coroutineScope) },
                modifier = Modifier.testTag("ban_sign_out_button"),
              ) {
                Text("Sign Out")
              }
            }
          }
        }
      }
      // Floating On-Screen Turkeys (Thanksgiving Part Two: 25% Regular / 45% Gravy Rush every 10s + Golden Gobblers)
      if (!uiState.isLoading && !showHoneycombTree && !uiState.isBanned && uiState.activeFeastZoneId == null && !uiState.showBossGlitchCutscene) {
        val density = LocalDensity.current
        val rootW = constraints.maxWidth
        val rootH = constraints.maxHeight
        val badgePx = with(density) { 64.dp.toPx() }.roundToInt()
        uiState.spawnedTurkeys.forEach { turkey ->
          val tx = ((turkey.xFraction * rootW).roundToInt() - badgePx / 2).coerceIn(8, (rootW - badgePx - 8).coerceAtLeast(8))
          val ty = ((turkey.yFraction * rootH).roundToInt() - badgePx / 2).coerceIn(64, (rootH - badgePx - 96).coerceAtLeast(64))
          SpawnedTurkeyBadge(
            isGoldenGobbler = turkey.isGoldenGobbler,
            onTap = { viewModel.collectSpawnedTurkey(turkey.id) },
            modifier =
              Modifier.offset { IntOffset(tx, ty) }
                .testTag("spawned_turkey_${turkey.id}"),
          )
        }
      }

      // Full-screen in-activity Honeycomb Ascension Tree (uses WindowInsets.safeDrawing inside)
      if (showHoneycombTree) {
        HoneycombAscensionTreeScreen(
          uiState = uiState,
          onUnlockAscensionNode = viewModel::unlockAscensionUpgrade,
          onPerformGatoAscension = viewModel::performGatoAscension,
          onDismiss = { showHoneycombTree = false },
        )
      }

      // Playable Teleported Feast Map Areas (Minigames, Fishing, Cooking & Citadel Boss)
      val activeZone =
        uiState.activeFeastZoneId?.let { zoneId ->
          GameCatalog.part2MapLandmarks.find { it.id == zoneId }
        }
      if (activeZone != null && !uiState.showBossGlitchCutscene) {
        FeastZoneAreaScreen(
          landmark = activeZone,
          uiState = uiState,
          onGrantZoneReward = viewModel::grantFeastZoneReward,
          onTriggerBossCutscene = viewModel::triggerBossGlitchCutscene,
          onExitZone = viewModel::exitFeastZone,
        )
      }

      // Climactic Citadel Boss Glitch Cutscene -> Blackout -> Melting Dec 1st Christmas Cat + Limited Item
      if (uiState.showBossGlitchCutscene) {
        BossGlitchCutsceneOverlay(
          catName = uiState.profile.selectedCatName,
          playerName = uiState.profile.displayName,
          patternId = uiState.profile.selectedPatternId,
          firstTimeReward = "melted_solstice_crown" !in uiState.profile.unlockedAccessories,
          onComplete = viewModel::completeBossGlitchCutscene,
        )
      }

      // One-time Dog Vault Keeper Fake Crash Omen on first cat pet
      if (uiState.showDogCrashOmen) {
        DogCrashOmenOverlay(
          onDismiss = viewModel::dismissDogCrashOmen,
        )
      }

      // Window Gato OS (Landscape PC inside the Dog's Vault)
      if (uiState.showWindowGatoDesktop) {
        WindowGatoDesktopOverlay(
          playerName = uiState.profile.displayName,
          catName = uiState.profile.selectedCatName,
          totalPurrsEarned = uiState.profile.totalPurrsEarned,
          onPlayCorruptedAudio = viewModel::playCorruptedAudioSample,
          onPlayErrorSound = viewModel::playWindowGatoErrorSound,
          onPlayCatScoldSound = viewModel::playCatScoldSound,
          onExitDesktop = viewModel::closeWindowGatoDesktop,
        )
      }
    }
    }
  }
}
