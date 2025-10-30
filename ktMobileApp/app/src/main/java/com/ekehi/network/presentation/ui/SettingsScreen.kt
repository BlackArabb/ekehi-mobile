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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onSignOut: () -> Unit
) {
    val miningNotificationsEnabled by viewModel.miningNotificationsEnabled.collectAsState()
    val socialTaskNotificationsEnabled by viewModel.socialTaskNotificationsEnabled.collectAsState()
    val referralNotificationsEnabled by viewModel.referralNotificationsEnabled.collectAsState()
    val streakNotificationsEnabled by viewModel.streakNotificationsEnabled.collectAsState()
    
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
            NotificationSettings(
                miningNotificationsEnabled = miningNotificationsEnabled,
                socialTaskNotificationsEnabled = socialTaskNotificationsEnabled,
                referralNotificationsEnabled = referralNotificationsEnabled,
                streakNotificationsEnabled = streakNotificationsEnabled,
                onMiningNotificationsChanged = { enabled -> 
                    viewModel.updateMiningNotifications(enabled)
                },
                onSocialTaskNotificationsChanged = { enabled -> 
                    viewModel.updateSocialTaskNotifications(enabled)
                },
                onReferralNotificationsChanged = { enabled -> 
                    viewModel.updateReferralNotifications(enabled)
                },
                onStreakNotificationsChanged = { enabled -> 
                    viewModel.updateStreakNotifications(enabled)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Settings
            PrivacySettingsSection(
                onPrivacyToggle = { enabled ->
                    viewModel.updatePrivacySettings(enabled)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Security Settings
            SecuritySettingsSection()

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            AboutSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            SignOutButton(
                onClick = {
                    viewModel.signOut()
                    onSignOut()
                }
            )
        }
    }
}

@Composable
fun NotificationSettingsSection(onNotificationToggle: (Boolean) -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Notifications",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingToggle(
                text = "Push Notifications",
                description = "Receive push notifications for mining updates and rewards",
                checked = notificationsEnabled,
                onCheckedChange = { enabled ->
                    notificationsEnabled = enabled
                    onNotificationToggle(enabled)
                }
            )

            SettingToggle(
                text = "Email Notifications",
                description = "Receive email notifications for important updates",
                checked = true,
                onCheckedChange = { /* Handle email notifications toggle */ }
            )

            SettingToggle(
                text = "In-App Notifications",
                description = "Show notifications within the app",
                checked = true,
                onCheckedChange = { /* Handle in-app notifications toggle */ }
            )
        }
    }
}

@Composable
fun PrivacySettingsSection(onPrivacyToggle: (Boolean) -> Unit) {
    var analyticsEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
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
                onCheckedChange = { enabled ->
                    analyticsEnabled = enabled
                    onPrivacyToggle(enabled)
                }
            )

            SettingItem(
                text = "Privacy Policy",
                icon = Icons.Default.Policy,
                onClick = { /* Handle privacy policy */ }
            )

            SettingItem(
                text = "Data Management",
                icon = Icons.Default.DataUsage,
                onClick = { /* Handle data management */ }
            )
        }
    }
}

@Composable
fun SecuritySettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
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
                onClick = { /* Handle change password */ }
            )

            SettingItem(
                text = "Two-Factor Authentication",
                icon = Icons.Default.VerifiedUser,
                onClick = { /* Handle 2FA */ }
            )

            SettingItem(
                text = "Login History",
                icon = Icons.Default.History,
                onClick = { /* Handle login history */ }
            )
        }
    }
}

@Composable
fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
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
                onClick = { /* Handle terms of service */ }
            )

            SettingItem(
                text = "Version 1.0.0",
                icon = Icons.Default.Info,
                onClick = { /* Handle version info */ }
            )

            SettingItem(
                text = "Contact Support",
                icon = Icons.Default.SupportAgent,
                onClick = { /* Handle contact support */ }
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
                fontSize = 16.sp
            )
            Text(
                text = description,
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 12.sp,
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
fun SettingItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
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
            tint = Color(0xB3FFFFFF), // 70% opacity white
            modifier = Modifier.size(24.dp)
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    EkehiMobileTheme {
        SettingsScreen(
            onSignOut = {}
        )
    }
}