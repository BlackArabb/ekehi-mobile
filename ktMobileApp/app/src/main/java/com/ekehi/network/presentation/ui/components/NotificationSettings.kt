package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        SwitchSettingItem(
            title = "Mining Updates",
            description = "Get notified when you earn EKEHI coins from mining",
            checked = miningNotificationsEnabled,
            onCheckedChange = onMiningNotificationsChanged
        )
        
        SwitchSettingItem(
            title = "Social Task Completion",
            description = "Get notified when you complete social tasks",
            checked = socialTaskNotificationsEnabled,
            onCheckedChange = onSocialTaskNotificationsChanged
        )
        
        SwitchSettingItem(
            title = "Referral Bonuses",
            description = "Get notified when you earn referral bonuses",
            checked = referralNotificationsEnabled,
            onCheckedChange = onReferralNotificationsChanged
        )
        
        SwitchSettingItem(
            title = "Streak Bonuses",
            description = "Get notified when you maintain mining streaks",
            checked = streakNotificationsEnabled,
            onCheckedChange = onStreakNotificationsChanged
        )
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}