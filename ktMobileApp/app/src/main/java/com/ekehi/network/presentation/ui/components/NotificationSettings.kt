package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationSettings(
    miningNotificationsEnabled: Boolean,
    socialTaskNotificationsEnabled: Boolean,
    referralNotificationsEnabled: Boolean,
    streakNotificationsEnabled: Boolean,
    onMiningNotificationsChanged: (Boolean) -> Unit,
    onSocialTaskNotificationsChanged: (Boolean) -> Unit,
    onReferralNotificationsChanged: (Boolean) -> Unit,
    onStreakNotificationsChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Notification Settings",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingToggle(
            text = "Mining Updates",
            description = "Get notified when you earn EKEHI coins from mining",
            checked = miningNotificationsEnabled,
            onCheckedChange = onMiningNotificationsChanged
        )
        
        SettingToggle(
            text = "Social Task Completion",
            description = "Get notified when you complete social tasks",
            checked = socialTaskNotificationsEnabled,
            onCheckedChange = onSocialTaskNotificationsChanged
        )
        
        SettingToggle(
            text = "Referral Bonuses",
            description = "Get notified when you earn referral bonuses",
            checked = referralNotificationsEnabled,
            onCheckedChange = onReferralNotificationsChanged
        )
        
        SettingToggle(
            text = "Streak Bonuses",
            description = "Get notified when you maintain mining streaks",
            checked = streakNotificationsEnabled,
            onCheckedChange = onStreakNotificationsChanged
        )
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