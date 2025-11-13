package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.presentation.ui.components.NotificationSettings
import com.ekehi.network.presentation.viewmodel.SettingsViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import android.util.Log


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onChangePassword: () -> Unit,
    onContactSupport: () -> Unit,
    onTermsOfService: () -> Unit,
    onLoginHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    Log.d("SettingsScreen", "🔄 SettingsScreen recomposed")
    
    val miningNotificationsEnabled by viewModel.miningNotificationsEnabled.collectAsState()
    val socialTaskNotificationsEnabled by viewModel.socialTaskNotificationsEnabled.collectAsState()
    val referralNotificationsEnabled by viewModel.referralNotificationsEnabled.collectAsState()
    val streakNotificationsEnabled by viewModel.streakNotificationsEnabled.collectAsState()
    val emailNotificationsEnabled by viewModel.emailNotificationsEnabled.collectAsState()
    val inAppNotificationsEnabled by viewModel.inAppNotificationsEnabled.collectAsState()
    val pushNotificationsEnabled by viewModel.pushNotificationsEnabled.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 24.dp)
            )

            // Notification Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x33FFFFFF) // 20% opacity white (increased from 10%)
                )
            ) {
                NotificationSettings(
                    miningNotificationsEnabled = miningNotificationsEnabled,
                    socialTaskNotificationsEnabled = socialTaskNotificationsEnabled,
                    referralNotificationsEnabled = referralNotificationsEnabled,
                    streakNotificationsEnabled = streakNotificationsEnabled,
                    onMiningNotificationsChanged = { enabled -> 
                        Log.d("SettingsScreen", "Mining notifications changed: $enabled")
                        viewModel.updateMiningNotifications(enabled)
                    },
                    onSocialTaskNotificationsChanged = { enabled -> 
                        Log.d("SettingsScreen", "Social task notifications changed: $enabled")
                        viewModel.updateSocialTaskNotifications(enabled)
                    },
                    onReferralNotificationsChanged = { enabled -> 
                        Log.d("SettingsScreen", "Referral notifications changed: $enabled")
                        viewModel.updateReferralNotifications(enabled)
                    },
                    onStreakNotificationsChanged = { enabled -> 
                        Log.d("SettingsScreen", "Streak notifications changed: $enabled")
                        viewModel.updateStreakNotifications(enabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Additional Notification Settings
            AdditionalNotificationSettings(
                pushNotificationsEnabled = pushNotificationsEnabled,
                emailNotificationsEnabled = emailNotificationsEnabled,
                inAppNotificationsEnabled = inAppNotificationsEnabled,
                onPushNotificationsChanged = { enabled ->
                    Log.d("SettingsScreen", "Push notifications changed: $enabled")
                    viewModel.updatePushNotifications(enabled)
                },
                onEmailNotificationsChanged = { enabled ->
                    Log.d("SettingsScreen", "Email notifications changed: $enabled")
                    viewModel.updateEmailNotifications(enabled)
                },
                onInAppNotificationsChanged = { enabled ->
                    Log.d("SettingsScreen", "In-app notifications changed: $enabled")
                    viewModel.updateInAppNotifications(enabled)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Settings
            PrivacySettingsSection(
                analyticsEnabled = analyticsEnabled,
                onAnalyticsToggle = { enabled ->
                    Log.d("SettingsScreen", "Analytics toggle changed: $enabled")
                    viewModel.updateAnalytics(enabled)
                },
                onPrivacyPolicyClick = {
                    Log.d("SettingsScreen", "Privacy policy clicked")
                    // TODO: Navigate to privacy policy screen
                },
                onDataManagementClick = {
                    Log.d("SettingsScreen", "Data management clicked")
                    // TODO: Navigate to data management screen
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Security Settings
            SecuritySettingsSection(
                onChangePassword = {
                    Log.d("SettingsScreen", "Change password clicked")
                    onChangePassword()
                },
                onLoginHistoryClick = {
                    Log.d("SettingsScreen", "Login history clicked")
                    onLoginHistory()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            AboutSection(
                onContactSupport = {
                    Log.d("SettingsScreen", "Contact support clicked")
                    onContactSupport()
                },
                onTermsOfServiceClick = {
                    Log.d("SettingsScreen", "Terms of service clicked")
                    onTermsOfService()
                },
                onVersionInfoClick = {
                    Log.d("SettingsScreen", "Version info clicked")
                    // TODO: Show version info dialog or screen
                }
            )

            // Remove the SignOutButton since we have logout on the profile page
        }
    }
}

@Composable
fun SettingItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent,
        onClick = {
            Log.d("SettingItem", "🔘 Item clicked: $text")
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFFffa000),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xB3FFFFFF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingToggle(
    text: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFffa000),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0x33FFFFFF) // Light white
            )
        )
    }
}

@Composable
fun SignOutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFef4444) // Red for sign out
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = "Sign Out",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdditionalNotificationSettings(
    pushNotificationsEnabled: Boolean,
    emailNotificationsEnabled: Boolean,
    inAppNotificationsEnabled: Boolean,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onEmailNotificationsChanged: (Boolean) -> Unit,
    onInAppNotificationsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF) // 20% opacity white (increased from 10%)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Additional Notifications",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingToggle(
                text = "Push Notifications",
                description = "Receive push notifications for mining updates and rewards",
                checked = pushNotificationsEnabled,
                onCheckedChange = onPushNotificationsChanged
            )

            SettingToggle(
                text = "Email Notifications",
                description = "Receive email notifications for important updates",
                checked = emailNotificationsEnabled,
                onCheckedChange = onEmailNotificationsChanged
            )

            SettingToggle(
                text = "In-App Notifications",
                description = "Show notifications within the app",
                checked = inAppNotificationsEnabled,
                onCheckedChange = onInAppNotificationsChanged
            )
        }
    }
}

@Composable
fun PrivacySettingsSection(
    analyticsEnabled: Boolean,
    onAnalyticsToggle: (Boolean) -> Unit,
    onPrivacyPolicyClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Privacy",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingToggle(
                text = "Analytics",
                description = "Help us improve the app by sending anonymous usage data",
                checked = analyticsEnabled,
                onCheckedChange = onAnalyticsToggle
            )

            SettingItem(
                text = "Privacy Policy",
                icon = Icons.Default.Policy,
                onClick = {
                    Log.d("PrivacySettings", "🔒 Privacy Policy clicked")
                    onPrivacyPolicyClick()
                }
            )

            SettingItem(
                text = "Data Management",
                icon = Icons.Default.DataUsage,
                onClick = {
                    Log.d("PrivacySettings", "💾 Data Management clicked")
                    onDataManagementClick()
                }
            )
        }
    }
}

@Composable
fun SecuritySettingsSection(
    onChangePassword: () -> Unit,
    onLoginHistoryClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Security",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                text = "Change Password",
                icon = Icons.Default.Lock,
                onClick = {
                    Log.d("SecuritySettings", "🔐 Change Password clicked - triggering callback")
                    onChangePassword()
                }
            )

            SettingItem(
                text = "Login History",
                icon = Icons.Default.History,
                onClick = {
                    Log.d("SecuritySettings", "📜 Login History clicked - triggering callback")
                    onLoginHistoryClick()
                }
            )
        }
    }
}

@Composable
fun AboutSection(
    onContactSupport: () -> Unit,
    onTermsOfServiceClick: () -> Unit = {},
    onVersionInfoClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "About",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                text = "Terms of Service",
                icon = Icons.Default.Description,
                onClick = {
                    Log.d("AboutSection", "📄 Terms of Service clicked - triggering callback")
                    onTermsOfServiceClick()
                }
            )

            SettingItem(
                text = "Version 1.0.0",
                icon = Icons.Default.Info,
                onClick = {
                    Log.d("AboutSection", "ℹ️ Version Info clicked")
                    onVersionInfoClick()
                }
            )

            SettingItem(
                text = "Contact Support",
                icon = Icons.Default.SupportAgent,
                onClick = {
                    Log.d("AboutSection", "📧 Contact Support clicked - triggering callback")
                    onContactSupport()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    EkehiMobileTheme {
        SettingsScreen(
            onChangePassword = {},
            onContactSupport = {},
            onTermsOfService = {},
            onLoginHistory = {},
            onSignOut = {}
        )
    }
}