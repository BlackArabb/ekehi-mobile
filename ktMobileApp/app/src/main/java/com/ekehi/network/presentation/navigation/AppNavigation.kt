package com.ekehi.network.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ekehi.network.presentation.ui.*
import com.ekehi.network.presentation.ui.components.BottomNavigationBar

@Composable
fun AppNavigation(isAuthenticated: Boolean? = null) {
    val navController = rememberNavController()
    
    // Determine the start destination based on authentication state
    val startDestination = when (isAuthenticated) {
        true -> {
            Log.d("AppNavigation", "User authenticated, starting at main screen")
            "main"
        }
        false -> {
            Log.d("AppNavigation", "User not authenticated, starting at landing screen")
            "landing"
        }
        null -> {
            Log.d("AppNavigation", "Authentication state unknown, starting at landing screen")
            "landing"
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("landing") {
            LandingScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    try {
                        navController.navigate("main") {
                            // Clear the entire back stack when navigating to main
                            popUpTo("landing") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }

        composable("register") {
            RegistrationScreen(
                onRegistrationSuccess = {
                    try {
                        navController.navigate("main") {
                            // Clear the entire back stack when navigating to main
                            popUpTo("landing") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onNavigateToLogin = {
                    try {
                        navController.navigate("login")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }

        composable("main") {
            MainScreen()
        }

        composable("settings") {
            SettingsScreen(
                onSignOut = {
                    try {
                        navController.navigate("landing") {
                            // Clear the back stack when signing out
                            popUpTo("main") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }

        // Additional screens can be added here
        composable("presale") {
            // PresaleScreen() - to be implemented
        }

        composable("wallet") {
            // WalletScreen() - to be implemented
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: "mining"

    Scaffold(
        bottomBar = {
            // Show bottom navigation bar for main screens only
            if (currentScreen in listOf("mining", "social", "leaderboard", "profile")) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "mining"
            ) {
                composable("mining") {
                    MiningScreen()
                }

                composable("social") {
                    SocialTasksScreen()
                }

                composable("leaderboard") {
                    LeaderboardScreen()
                }

                composable("profile") {
                    ProfileScreen(
                        onNavigateToSettings = {
                            try {
                                navController.navigate("settings")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
            }
        }
    }
}