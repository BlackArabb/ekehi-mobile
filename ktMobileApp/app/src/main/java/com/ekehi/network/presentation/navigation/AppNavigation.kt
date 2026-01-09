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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.ekehi.network.presentation.ui.*
import com.ekehi.network.presentation.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.presentation.ui.components.BottomNavigationBar
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import com.ekehi.network.auth.SocialAuthManager

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
                },
                onNavigateToSecondaryInfo = {
                    try {
                        navController.navigate("secondary_info")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }
        
        // Secondary info screen for additional information after OAuth registration
        composable("secondary_info") {
            SecondaryInfoScreen(
                onSecondaryInfoSuccess = {
                    try {
                        navController.navigate("main") {
                            popUpTo("landing") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
                    try {
                        Log.d("AppNavigation", "Logging out - clearing session and navigating to landing")
                        navController.navigate("landing") {
                            popUpTo(0) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: "mining"
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    
    Scaffold(
        bottomBar = {
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
                    MiningScreen(navController = navController)
                }

                composable("social") {
                    val authManager = SocialAuthManager(androidx.compose.ui.platform.LocalContext.current)
                    SocialTasksScreen(authManager = authManager)
                }

                composable("leaderboard") {
                    LeaderboardScreen()
                }

                composable("profile") {
                    val coroutineScope = rememberCoroutineScope()
                    
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            EventBus.sendEvent(Event.RefreshUserProfile)
                        }
                    }
                    
                    ProfileScreen(
                        onNavigateToSettings = {
                            try {
                                Log.d("MainScreen", "Navigating to settings")
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
                            try {
                                Log.d("MainScreen", "Logout button clicked - starting logout process")
                                // Sign out from Appwrite and clear local storage
                                settingsViewModel.signOut()
                                Log.d("MainScreen", "SettingsViewModel.signOut() completed")
                                // Navigate to landing screen
                                onLogout()
                                Log.d("MainScreen", "Navigation callback triggered")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Error during logout", e)
                            }
                        }
                    )
                }
                
                composable("settings") {
                    SettingsScreen(
                        onChangePassword = {
                            try {
                                Log.d("MainScreen", "Navigating to change password")
                                navController.navigate("change_password")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onContactSupport = {
                            try {
                                Log.d("MainScreen", "Navigating to contact support")
                                navController.navigate("contact_support")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onTermsOfService = {
                            try {
                                Log.d("MainScreen", "Navigating to terms of service")
                                navController.navigate("terms_and_service")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onSignOut = {
                            try {
                                Log.d("MainScreen", "Sign out triggered from SettingsScreen")
                                settingsViewModel.signOut()
                                onLogout()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Sign out error", e)
                            }
                        },
                        onPrivacyPolicy = {
                            try {
                                Log.d("MainScreen", "Navigating to privacy policy")
                                navController.navigate("privacy_policy")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        },
                        onDataManagement = {
                            try {
                                Log.d("MainScreen", "Navigating to data management")
                                navController.navigate("data_management")
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }

                composable("change_password") {
                    ChangePasswordScreen(
                        onNavigateBack = {
                            Log.d("MainScreen", "Navigating back from change password")
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
                            Log.d("MainScreen", "Navigating back from contact support")
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
                
                composable("terms_and_service") {
                    TermsOfServiceScreen(
                        onNavigateBack = {
                            Log.d("MainScreen", "Navigating back from terms and service")
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
                
                composable("edit_profile") {
                    EditUsernameScreen(
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

                composable("friends") {
                    FriendsScreen(
                        onNavigateBack = {
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }

                composable("privacy_policy") {
                    PrivacyPolicyScreen(
                        onNavigateBack = {
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
                            }
                        }
                    )
                }
                
                composable("data_management") {
                    DataManagementScreen(
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