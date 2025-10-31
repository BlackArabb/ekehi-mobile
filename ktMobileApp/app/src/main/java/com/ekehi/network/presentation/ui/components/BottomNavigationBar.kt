package com.ekehi.network.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
            BottomNavItem.Mine,
            BottomNavItem.SocialTasks,
            BottomNavItem.Leaderboard,
            BottomNavItem.Profile
    )

    NavigationBar(
            containerColor = Color(0xFF1a1a2e), // Dark blue background matching the app theme
            tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                    icon = {
                        Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                        )
                    },
                    label = {
                        Text(text = item.title)
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFffa000), // Ekehi orange
                            selectedTextColor = Color(0xFFffa000), // Ekehi orange
                            indicatorColor = Color(0xFF16213e), // Slightly lighter blue for indicator
                            unselectedIconColor = Color(0xB3FFFFFF), // Light gray with opacity
                            unselectedTextColor = Color(0xB3FFFFFF) // Light gray with opacity
                    )
            )
        }
    }
}

sealed class BottomNavItem(
        val route: String,
        val title: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Mine : BottomNavItem("mining", "Mine", Icons.Default.Home)
    object SocialTasks : BottomNavItem("social", "Social", Icons.Default.Share)
    object Leaderboard : BottomNavItem("leaderboard", "Leaderboard", Icons.Default.EmojiEvents)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}