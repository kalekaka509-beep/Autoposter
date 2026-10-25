package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrafficStatus
import com.example.ui.components.LivePulseIndicator
import com.example.ui.components.PostDetailModal
import com.example.ui.screens.BloggerPostsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OptimizerScreen
import com.example.ui.screens.QueueAndLogsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CoralPeak
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val trafficInfo = uiState.usTrafficStatus

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ViralBlogger",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = when (trafficInfo.status) {
                                TrafficStatus.PEAK_ACTIVE -> CoralPeak.copy(alpha = 0.2f)
                                else -> ElectricBlue.copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                LivePulseIndicator(isPeak = trafficInfo.status == TrafficStatus.PEAK_ACTIVE)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "US " + trafficInfo.formattedEstTime.take(8),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (trafficInfo.status) {
                                        TrafficStatus.PEAK_ACTIVE -> CoralPeak
                                        else -> ElectricBlue
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadBloggerPosts() },
                        modifier = Modifier.testTag("top_refresh_button")
                    ) {
                        if (uiState.isLoadingPosts) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Blogger Posts"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.DASHBOARD,
                    onClick = { viewModel.setTab(ScreenTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "US Live"
                        )
                    },
                    label = { Text("US Live", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricBlue.copy(alpha = 0.2f))
                )

                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.BLOG_POSTS,
                    onClick = { viewModel.setTab(ScreenTab.BLOG_POSTS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.BLOG_POSTS) Icons.Filled.Article else Icons.Outlined.Article,
                            contentDescription = "Articles"
                        )
                    },
                    label = { Text("Articles", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricBlue.copy(alpha = 0.2f))
                )

                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.OPTIMIZER,
                    onClick = { viewModel.setTab(ScreenTab.OPTIMIZER) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.OPTIMIZER) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Viral CTR"
                        )
                    },
                    label = { Text("Viral CTR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricBlue.copy(alpha = 0.2f))
                )

                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.QUEUE_LOGS,
                    onClick = { viewModel.setTab(ScreenTab.QUEUE_LOGS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.QUEUE_LOGS) Icons.Filled.HourglassTop else Icons.Outlined.HourglassTop,
                            contentDescription = "Queue"
                        )
                    },
                    label = { Text("Queue", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricBlue.copy(alpha = 0.2f))
                )

                NavigationBarItem(
                    selected = uiState.currentTab == ScreenTab.SETTINGS,
                    onClick = { viewModel.setTab(ScreenTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == ScreenTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricBlue.copy(alpha = 0.2f))
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                ScreenTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                ScreenTab.BLOG_POSTS -> BloggerPostsScreen(viewModel = viewModel)
                ScreenTab.OPTIMIZER -> OptimizerScreen(viewModel = viewModel)
                ScreenTab.QUEUE_LOGS -> QueueAndLogsScreen(viewModel = viewModel)
                ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal when user clicks an individual post
    uiState.selectedPostForDetail?.let { post ->
        PostDetailModal(
            post = post,
            selectedPlatforms = uiState.selectedPlatformsForPost,
            onTogglePlatform = { viewModel.togglePlatformSelection(it) },
            onDismiss = { viewModel.selectPostForDetail(null) },
            onQueuePeak = {
                viewModel.queuePostForNextPeak(post)
                viewModel.selectPostForDetail(null)
            },
            onPushAll = {
                viewModel.dispatchPostToAllPlatforms(post)
                viewModel.selectPostForDetail(null)
            },
            onDirectShare = { platform ->
                viewModel.dispatchPostToPlatform(post, platform, launchIntent = true)
            }
        )
    }
}
