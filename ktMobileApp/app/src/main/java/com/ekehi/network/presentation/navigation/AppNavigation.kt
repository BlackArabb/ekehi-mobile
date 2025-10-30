package com.ekehi.network.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ekehi.network.presentation.ui.*
import com.ekehi.network.presentation.ui.components.BottomNavigationBar
import com.ekehi.network.service.MiningManager

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
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
                            popUpTo("login") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        // Handle navigation error
                    }
                }
            )
        }

        composable("register") {
            RegistrationScreen(
                onRegistrationSuccess = {
                    try {
                        navController.navigate("main") {
                            popUpTo("register") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        // Handle navigation error
                    }
                },
                onNavigateToLogin = {
                    try {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        // Handle navigation error
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
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = false }
                        }
                    } catch (e: Exception) {
                        // Handle navigation error
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
                                // Handle navigation error
                            }
                        }
                    )
                }
            }
        }
    }
}