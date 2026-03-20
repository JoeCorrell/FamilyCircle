package com.haven.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.haven.app.data.model.Drive
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.BottomNavBar
import com.haven.app.ui.components.NavTab
import com.haven.app.ui.screens.*

object Routes {
    const val HOME = "home"
    const val MAP = "map"
    const val SAFETY = "safety"
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val MEMBER_DETAIL = "member_detail"
    const val DRIVE_DETAIL = "drive_detail"
    const val NOTIFICATIONS = "notifications"
    const val THEMES = "themes"
    const val ADD_PLACE = "add_place"
    const val SETTINGS_SUB = "settings_sub"
    const val PROFILE = "profile"
    const val HAVEN_MANAGEMENT = "haven_management"
    const val ABOUT = "about"
    const val SAVED_PLACES = "saved_places"
}

@Composable
fun HavenNavHost(
    navController: NavHostController,
    selectedMember: FamilyMember?,
    selectedDrive: Drive?,
    settingsSection: SettingsSection?,
    onMemberSelected: (FamilyMember) -> Unit,
    onDriveSelected: (Drive) -> Unit,
    onSettingsSectionSelected: (SettingsSection) -> Unit,
    onSosClick: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(250)) + slideInHorizontally(tween(300)) { it / 6 }
        },
        exitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { -it / 6 }
        },
        popEnterTransition = {
            fadeIn(tween(250)) + slideInHorizontally(tween(300)) { -it / 6 }
        },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 6 }
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onMemberClick = {
                    onMemberSelected(it)
                    navController.navigate(Routes.MEMBER_DETAIL)
                },
                onSosClick = onSosClick,
                onSafetyClick = { navController.navigate(Routes.SAFETY) },
                onMapClick = { navController.navigate(Routes.MAP) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onThemesClick = { navController.navigate(Routes.THEMES) },
                onAddPlaceClick = { navController.navigate(Routes.ADD_PLACE) },
                onPlacesClick = { navController.navigate(Routes.SAVED_PLACES) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.MAP) {
            MapScreen(
                onMemberClick = {
                    onMemberSelected(it)
                    navController.navigate(Routes.MEMBER_DETAIL)
                }
            )
        }

        composable(Routes.SAFETY) {
            SafetyScreen(
                onDriveClick = {
                    onDriveSelected(it)
                    navController.navigate(Routes.DRIVE_DETAIL)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHAT) {
            ChatScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onCirclesClick = { navController.navigate(Routes.HAVEN_MANAGEMENT) },
                onPlacesClick = { navController.navigate(Routes.SAVED_PLACES) },
                onThemesClick = { navController.navigate(Routes.THEMES) },
                onSectionClick = {
                    onSettingsSectionSelected(it)
                    navController.navigate(Routes.SETTINGS_SUB)
                },
                onAboutClick = { navController.navigate(Routes.ABOUT) },
                onJoinCircleClick = { navController.navigate(Routes.HAVEN_MANAGEMENT) },
                onSignOut = onSignOut
            )
        }

        composable(Routes.MEMBER_DETAIL) {
            selectedMember?.let { member ->
                MemberDetailScreen(
                    member = member,
                    onBack = { navController.popBackStack() },
                    onChatClick = {
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.HOME) { inclusive = false }
                        }
                    }
                )
            } ?: LaunchedEffect(Unit) { navController.popBackStack() }
        }

        composable(Routes.DRIVE_DETAIL) {
            selectedDrive?.let { drive ->
                DriveDetailScreen(
                    drive = drive,
                    onBack = { navController.popBackStack() }
                )
            } ?: LaunchedEffect(Unit) { navController.popBackStack() }
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.THEMES) {
            ThemesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_PLACE) {
            AddPlaceScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_SUB) {
            settingsSection?.let { section ->
                SettingsSubScreen(
                    title = section.title,
                    items = section.items,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HAVEN_MANAGEMENT) {
            HavenManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SAVED_PLACES) {
            SavedPlacesScreen(
                onBack = { navController.popBackStack() },
                onAddPlace = { navController.navigate(Routes.ADD_PLACE) }
            )
        }
    }
}

fun navTabToRoute(tab: NavTab): String = when (tab) {
    NavTab.HOME -> Routes.HOME
    NavTab.MAP -> Routes.MAP
    NavTab.CHAT -> Routes.CHAT
    NavTab.SETTINGS -> Routes.SETTINGS
}

fun routeToNavTab(route: String?): NavTab = when (route) {
    Routes.HOME -> NavTab.HOME
    Routes.MAP -> NavTab.MAP
    Routes.CHAT -> NavTab.CHAT
    Routes.SETTINGS -> NavTab.SETTINGS
    else -> NavTab.HOME
}
