package com.haven.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.haven.app.data.model.Drive
import com.haven.app.data.model.FamilyMember
import com.haven.app.data.preferences.UserPreferences
import com.haven.app.service.LocationService
import com.haven.app.service.SosService
import com.haven.app.ui.components.BottomNavBar
import com.haven.app.ui.dialogs.SosDialog
import com.haven.app.ui.navigation.*
import com.haven.app.ui.screens.AuthScreen
import com.haven.app.ui.screens.CreateJoinHavenScreen
import com.haven.app.ui.screens.OnboardingScreen
import com.haven.app.ui.screens.SettingsSection
import com.haven.app.ui.screens.SplashScreen
import com.haven.app.ui.theme.HavenAppTheme
import com.haven.app.ui.theme.HavenThemes
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.viewmodel.AuthState
import com.haven.app.ui.viewmodel.AuthViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var sosService: SosService

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocationService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        setContent {
            val themeKey by userPreferences.theme.collectAsStateWithLifecycle(initialValue = "sand")
            val havenColors = HavenThemes.fromKey(themeKey)

            HavenAppTheme(havenColors = havenColors) {
                HavenRoot(
                    onSosActivated = {
                        lifecycleScope.launch { sosService.activateSos() }
                    }
                )
            }
        }
    }

    private fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionRequest.launch(perms.toTypedArray())
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        startForegroundService(intent)
    }
}

@Composable
fun HavenRoot(onSosActivated: () -> Unit) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val error by authViewModel.error.collectAsStateWithLifecycle()
    val t = LocalHavenColors.current

    var showSplash by remember { mutableStateOf(true) }
    var showOnboarding by remember { mutableStateOf(false) }
    var hasSeenOnboarding by remember { mutableStateOf(false) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    when (authState) {
        AuthState.LOADING -> {
            Box(Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = t.accent)
            }
        }
        AuthState.SIGNED_OUT -> {
            AuthScreen(
                isLoading = isLoading,
                error = error,
                onSignIn = { email, pw -> authViewModel.signIn(email, pw) },
                onSignUp = { email, pw -> authViewModel.signUp(email, pw) },
                onClearError = { authViewModel.clearError() }
            )
        }
        AuthState.NO_HAVEN -> {
            CreateJoinHavenScreen(
                isLoading = isLoading,
                error = error,
                onCreateHaven = { name, user ->
                    authViewModel.createHaven(name, user)
                    showOnboarding = true
                },
                onJoinHaven = { code, user ->
                    authViewModel.joinHaven(code, user)
                    showOnboarding = true
                },
                onSignOut = { authViewModel.signOut() },
                onClearError = { authViewModel.clearError() }
            )
        }
        AuthState.READY -> {
            if (showOnboarding && !hasSeenOnboarding) {
                OnboardingScreen(onComplete = {
                    hasSeenOnboarding = true
                    showOnboarding = false
                })
            } else {
                HavenMainScreen(onSosActivated = onSosActivated, onSignOut = { authViewModel.signOut() })
            }
        }
    }
}

@Composable
fun HavenMainScreen(onSosActivated: () -> Unit, onSignOut: () -> Unit = {}) {
    val t = LocalHavenColors.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }
    var selectedDrive by remember { mutableStateOf<Drive?>(null) }
    var settingsSection by remember { mutableStateOf<SettingsSection?>(null) }
    var showSos by remember { mutableStateOf(false) }

    val mainTabs = setOf(Routes.HOME, Routes.MAP, Routes.CHAT, Routes.SETTINGS)
    val showBottomNav = currentRoute in mainTabs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .systemBarsPadding()
    ) {
        HavenNavHost(
            navController = navController,
            selectedMember = selectedMember,
            selectedDrive = selectedDrive,
            settingsSection = settingsSection,
            onMemberSelected = { selectedMember = it },
            onDriveSelected = { selectedDrive = it },
            onSettingsSectionSelected = { settingsSection = it },
            onSosClick = { showSos = true },
            onSignOut = onSignOut,
            modifier = Modifier.weight(1f)
        )

        if (showBottomNav) {
            BottomNavBar(
                selectedTab = routeToNavTab(currentRoute),
                onTabSelected = { tab ->
                    val route = navTabToRoute(tab)
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }

    if (showSos) {
        SosDialog(
            onDismiss = { showSos = false },
            onActivate = onSosActivated
        )
    }
}
