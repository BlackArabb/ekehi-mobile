package com.ekehi.network.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.ui.*
import com.ekehi.network.presentation.ui.LandingScreen
import com.ekehi.network.presentation.ui.LoginScreen
import com.ekehi.network.presentation.ui.RegistrationScreen
import com.ekehi.network.presentation.ui.SettingsScreen
import com.ekehi.network.presentation.ui.ProfileScreen
import com.ekehi.network.presentation.ui.EditUsernameScreen
import com.ekehi.network.presentation.ui.ReferralCodeScreen
import com.ekehi.network.presentation.ui.ChangePasswordScreen
import com.ekehi.network.presentation.ui.ContactSupportScreen
import com.ekehi.network.presentation.ui.MiningScreen
import com.ekehi.network.presentation.ui.SocialTasksScreen
import com.ekehi.network.presentation.ui.LeaderboardScreen
import com.ekehi.network.presentation.ui.TermsOfServiceScreen
import com.ekehi.network.presentation.ui.PrivacyPolicyScreen
import com.ekehi.network.presentation.ui.DataManagementScreen
import com.ekehi.network.presentation.ui.components.BottomNavigationBar
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import com.ekehi.network.presentation.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
            MainScreen(
                onLogout = {
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
                onTermsOfService = {
                    try {
                        navController.navigate("terms_and_service")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onSignOut = {
                    try {
                        Log.d("AppNavigation", "Sign out from settings")
                        // Navigate to landing and clear the back stack
                        navController.navigate("landing") {
                            popUpTo("landing") { inclusive = false }
                        }
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onPrivacyPolicy = {
                    try {
                        navController.navigate("privacy_policy")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                },
                onDataManagement = {
                    try {
                        navController.navigate("data_management")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
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
                        Log.e("AppNavigation", "Navigation error", e)
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
                        Log.e("AppNavigation", "Navigation error", e)
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
                        Log.e("AppNavigation", "Navigation error", e)
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
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }

        // Add Privacy Policy Screen
        composable("privacy_policy") {
            PrivacyPolicyScreen(
                onNavigateBack = {
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Navigation error", e)
                    }
                }
            )
        }
        
        // Add Data Management Screen
        composable("data_management") {
            DataManagementScreen(
                onNavigateBack = {
                    try {
                        navController.popBackStack()
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
fun MainScreen(onLogout: () -> Unit = {}) {
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
                        onLogout = onLogout
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
                                Log.d("MainScreen", "Sign out from settings")
                                // Navigate to landing and clear the back stack
                                navController.navigate("landing") {
                                    popUpTo("landing") { inclusive = false }
                                }
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Navigation error", e)
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

                // Add Change Password Screen
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
                
                // Add Contact Support Screen
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
                
                // Add Terms and Service Screen
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
                
                // Add Privacy Policy Screen
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
                
                // Add Data Management Screen
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
