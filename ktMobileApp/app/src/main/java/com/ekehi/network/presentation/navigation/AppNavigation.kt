package com.ekehi.network.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ekehi.network.presentation.ui.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegistrationScreen(
                onRegistrationSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToSocial = {
                    navController.navigate("social")
                },
                onNavigateToLeaderboard = {
                    navController.navigate("leaderboard")
                },
                onNavigateToMining = {
                    navController.navigate("mining")
                },
                onNavigateToPresale = {
                    navController.navigate("presale")
                },
                onNavigateToWallet = {
                    navController.navigate("wallet")
                }
            )
        }

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
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = false }
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