package com.leonoretech.marianas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.leonoretech.marianas.ui.components.rememberImagePickerLauncher
import com.leonoretech.marianas.ui.navigation.MarianasRoute
import com.leonoretech.marianas.ui.navigation.SideMenuContent
import com.leonoretech.marianas.ui.screens.LoadingScreen
import com.leonoretech.marianas.ui.screens.LoginScreen
import com.leonoretech.marianas.ui.screens.chat.ChatScreen
import com.leonoretech.marianas.ui.screens.dashboard.AppearanceDashboard
import com.leonoretech.marianas.ui.screens.dashboard.DataDashboard
import com.leonoretech.marianas.ui.screens.dashboard.ProviderDashboard
import com.leonoretech.marianas.ui.theme.BgDark
import com.leonoretech.marianas.ui.theme.MarianasTheme
import com.leonoretech.marianas.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarianasTheme {
                MarianasApp(viewModel)
            }
        }
    }
}

@Composable
fun MarianasApp(viewModel: ChatViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    if (!isLoggedIn) {
        LoginScreen(viewModel)
        return
    }

    val isAppReady by viewModel.isAppReady.collectAsState()

    if (!isAppReady) {
        LoadingScreen()
        return
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val dataState by viewModel.dataState.collectAsState()

    val pickImagesLauncher = rememberImagePickerLauncher(maxItems = 4) { uris ->
        uris.forEach { uri -> viewModel.persistAndAttachImage(uri) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideMenuContent(
                sessions = dataState.sessions,
                onNavigate = { route ->
                    navController.navigate(route.route) {
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                },
                onSelectSession = { sessionId ->
                    viewModel.switchSession(sessionId)
                    navController.navigate(MarianasRoute.Chat.route) { launchSingleTop = true }
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) },
                onNewChat = {
                    viewModel.createNewSession()
                    navController.navigate(MarianasRoute.Chat.route) { launchSingleTop = true }
                    scope.launch { drawerState.close() }
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = MarianasRoute.Chat.route,
            modifier = Modifier.fillMaxSize().background(BgDark)
        ) {
            composable(MarianasRoute.Chat.route) {
                ChatScreen(
                    viewModel = viewModel,
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    onPickImage = pickImagesLauncher
                )
            }
            composable(MarianasRoute.Provider.route) {
                ProviderDashboard(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(MarianasRoute.Appearance.route) {
                AppearanceDashboard(onBack = { navController.popBackStack() })
            }
            composable(MarianasRoute.DataSync.route) {
                DataDashboard(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSessionSelected = { navController.navigate(MarianasRoute.Chat.route) { launchSingleTop = true } }
                )
            }
        }
    }
}
