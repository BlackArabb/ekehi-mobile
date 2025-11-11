package com.ekehi.network.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.ekehi.network.presentation.ui.*
import com.ekehi.network.presentation.ui.LandingScreen
import com.ekehi.network.presentation.ui.LoginScreen
import com.ekehi.network.presentation.ui.RegistrationScreen
import com.ekehi.network.presentation.ui.SettingsScreen
import com.ekehi.network.presentation.ui.ProfileScreen
import com.ekehi.network.presentation.ui.EditProfileScreen
import com.ekehi.network.presentation.ui.ReferralCodeScreen
import com.ekehi.network.presentation.ui.ChangePasswordScreen
import com.ekehi.network.presentation.ui.ContactSupportScreen
import com.ekehi.network.presentation.ui.MiningScreen
import com.ekehi.network.presentation.ui.SocialTasksScreen
import com.ekehi.network.presentation.ui.LeaderboardScreen
import com.ekehi.network.presentation.ui.components.BottomNavigationBar
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event

@Composable
fun AppNavigation(isAuthenticated: Boolean = false) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "main" else "landing"
    ) {
        composable("landing") {
            LandingScreen(
                onNavigateToLogin = {
                    try {
                        navController.navigate("login")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onNavigateToRegister = {
                    try {
                        navController.navigate("register")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
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
                },
                onNavigateToRegistration = {
                    try {
                        navController.navigate("register")
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
                onChangePassword = {
                    try {
                        navController.navigate("change_password")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onContactSupport = {
                    try {
                        navController.navigate("contact_support")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onSignOut = {
                    try {
                        // Navigate to landing and clear the back stack
                        navController.navigate("landing") {
                            popUpTo("landing") { inclusive = false }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }

        composable("profile") {
            val coroutineScope = rememberCoroutineScope()
            
            // Refresh profile when entering this screen
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    EventBus.sendEvent(Event.RefreshUserProfile)
                }
            }
            
            ProfileScreen(
                onNavigateToSettings = {
                    try {
                        navController.navigate("settings")
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                },
                onNavigateToEditProfile = {
                    try {
                        navController.navigate("edit_profile")
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                },
                onNavigateToReferralCode = {
                    try {
                        navController.navigate("referral_code")
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                },
                onLogout = {
                    // Show exit ad before logging out
                    // TODO: Implement actual logout after ad
                    try {
                        // Navigate to landing and clear the back stack
                        navController.navigate("landing") {
                            popUpTo("landing") { inclusive = false }
                        }
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                }
            )
        }
        
        composable("edit_profile") {
            EditProfileScreen(
                onNavigateBack = {
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                }
            )
        }
        
        composable("referral_code") {
            ReferralCodeScreen(
                onNavigateBack = {
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                }
            )
        }
        
        composable("change_password") {
            ChangePasswordScreen(
                onNavigateBack = {
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
                    }
                }
            )
        }
        
        composable("contact_support") {
            ContactSupportScreen(
                onNavigateBack = {
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("MainScreen", "Navigation error", e)
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
                    val coroutineScope = rememberCoroutineScope()
                    
                    // Refresh profile when entering this screen
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            EventBus.sendEvent(Event.RefreshUserProfile)
                        }
                    }
                    
                    ProfileScreen(
                        onNavigateToSettings = {
                            try {
                                navController.navigate("settings")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onNavigateToEditProfile = {
                            try {
                                navController.navigate("edit_profile")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onNavigateToReferralCode = {
                            try {
                                navController.navigate("referral_code")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onLogout = {
                            // Show exit ad before logging out
                            // TODO: Implement actual logout after ad
                            try {
                                // Navigate to landing and clear the back stack
                                navController.navigate("landing") {
                                    popUpTo("landing") { inclusive = false }
                                }
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
                
                composable("settings") {
                    SettingsScreen(
                        onChangePassword = {
                            try {
                                navController.navigate("change_password")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onContactSupport = {
                            try {
                                navController.navigate("contact_support")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onSignOut = {
                            try {
                                // Navigate to landing and clear the back stack
                                navController.navigate("landing") {
                                    popUpTo("landing") { inclusive = false }
                                }
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
                
                composable("edit_profile") {
                    EditProfileScreen(
                        onNavigateBack = {
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
                
                composable("referral_code") {
                    ReferralCodeScreen(
                        onNavigateBack = {
                            try {
                                navController.popBackStack()
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
