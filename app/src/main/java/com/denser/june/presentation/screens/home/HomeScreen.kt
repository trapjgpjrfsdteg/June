package com.denser.june.presentation.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.denser.june.presentation.navigation.AppNavigator
import com.denser.june.presentation.navigation.Route
import com.denser.june.core.domain.preferences.JournalPreferences
import com.denser.june.presentation.components.JuneAppBarType
import com.denser.june.presentation.components.JuneTopAppBar
import com.denser.june.presentation.screens.home.components.HomeBottomBar
import com.denser.june.presentation.screens.home.journals.JournalsPage
import com.denser.june.presentation.screens.home.timeline.TimelinePage
import com.denser.june.presentation.screens.home.tags.TagsPage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

import com.denser.june.core.R
import com.denser.june.presentation.screens.home.tags.TagsVM
import com.denser.june.presentation.components.SyncIndicator
import com.denser.june.MainVM
import org.koin.compose.viewmodel.koinViewModel

import com.denser.june.presentation.screens.home.components.AISummaryPill

enum class HomeTab(val label: String, val iconRes: Int, val filledIconRes: Int) {
    Journals("Home", R.drawable.home_24px, R.drawable.home_24px_fill),
    Tags("Spaces", R.drawable.view_cozy_24px, R.drawable.view_cozy_24px_fill),
    Timeline("Timeline", R.drawable.event_note_24px, R.drawable.event_note_24px_fill),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen() {
    val navigator = koinInject<AppNavigator>()
    val mainVM: MainVM = koinViewModel(
        viewModelStoreOwner = LocalContext.current as androidx.activity.ComponentActivity
    )
    val appState by mainVM.state.collectAsStateWithLifecycle()
    val journalPrefs = koinInject<JournalPreferences>()
    val isAutoTimeEnabled by journalPrefs.isAutoTimeEnabled().collectAsStateWithLifecycle(initialValue = false)
    
    val pagerState = rememberPagerState(pageCount = { HomeTab.entries.size })
    val scope = rememberCoroutineScope()

    val tagsVM: TagsVM = koinViewModel()
    val activeTag by tagsVM.selectedPrimaryTag.collectAsStateWithLifecycle()

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column {
                    JuneTopAppBar(
                        type = JuneAppBarType.CenterAligned,
                        title = {
                            Text(
                                text = "June",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            FilledIconButton(
                                onClick = { navigator.navigateTo(Route.Search) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search_24px),
                                    contentDescription = "Search"
                                )
                            }
                        },
                        actions = {
                            if (appState.isSyncEnabled && appState.isInternetAllowed) {
                                SyncIndicator(
                                    status = appState.syncStatus,
                                    onClick = { navigator.navigateTo(Route.SyncSettings) }
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            FilledIconButton(
                                onClick = { navigator.navigateTo(Route.Settings) },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.settings_24px),
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    )
                    DayProgressIndicator()
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                userScrollEnabled = false,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) { page ->
                when (HomeTab.entries[page]) {
                    HomeTab.Journals -> JournalsPage(isSelected = pagerState.currentPage == 0)
                    HomeTab.Tags -> TagsPage()
                    HomeTab.Timeline -> TimelinePage()
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AISummaryPill()
        }

        HomeBottomBar(
            pagerState = pagerState,
            onFabClick = {
                val currentTab = HomeTab.entries[pagerState.currentPage]
                handleFabClick(
                    currentTab = currentTab,
                    activeTag = activeTag,
                    isAutoTimeEnabled = isAutoTimeEnabled,
                    navigator = navigator
                )
            }
        )
    }
}

private fun handleFabClick(
    currentTab: HomeTab,
    activeTag: String?,
    isAutoTimeEnabled: Boolean,
    navigator: AppNavigator
) {
    val initialDate = if (isAutoTimeEnabled) System.currentTimeMillis() else null
    val route = if (currentTab == HomeTab.Tags && activeTag != null) {
        Route.Editor(initialDate = initialDate, initialTags = listOf(activeTag))
    } else {
        Route.Editor(initialDate = initialDate)
    }
    navigator.navigateTo(route)
}

@Composable
fun DayProgressIndicator() {
    val journalPrefs = koinInject<JournalPreferences>()
    val isTimeHidden by journalPrefs.isDayProgressTimeHidden().collectAsStateWithLifecycle(initialValue = false)

    var currentSeconds by remember { mutableStateOf(0) }
    var showFullScreenMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = java.util.Calendar.getInstance()
            currentSeconds = now.get(java.util.Calendar.HOUR_OF_DAY) * 3600 +
                             now.get(java.util.Calendar.MINUTE) * 60 +
                             now.get(java.util.Calendar.SECOND)
            kotlinx.coroutines.delay(1000)
        }
    }

    val displayProgress = currentSeconds.toFloat() / 86400f
    val displaySeconds = currentSeconds
    val hours = displaySeconds / 3600
    val minutes = (displaySeconds % 3600) / 60
    val seconds = displaySeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    val formattedPercent = String.format(Locale.getDefault(), "%.2f%%", displayProgress * 100)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable { showFullScreenMenu = true }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Empty track line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(1.dp)
                )
        )

        // Highlight completed track fill
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = displayProgress)
                .height(2.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(1.dp)
                )
        )

        // Pill shape indicator centered at the progress fraction using BiasAlignment
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = BiasAlignment(
                horizontalBias = (displayProgress * 2f) - 1f,
                verticalBias = 0f
            )
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                tonalElevation = 1.dp,
                modifier = Modifier.clickable { showFullScreenMenu = true }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (!isTimeHidden) {
                        Text(
                            text = formattedTime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .size(1.5.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = formattedPercent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showFullScreenMenu) {
        DayProgressDetailDialog(
            onDismiss = { showFullScreenMenu = false }
        )
    }
}

@Composable
private fun DayProgressDetailDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.close_24px),
                        contentDescription = "Close"
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Day Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Development in progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}