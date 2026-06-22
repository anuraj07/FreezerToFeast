package com.deep.freezertofeast.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.ui.viewmodels.JournalViewModel
import com.deep.freezertofeast.ui.viewmodels.ProfileViewModel
import com.deep.freezertofeast.ui.viewmodels.InventoryViewModel

@Composable
fun MainTabContainer(
    onSignOut: () -> Unit,
    journalViewModel: JournalViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf("journal") }

    BackHandler(enabled = currentTab != "journal") {
        currentTab = "journal"
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF4F3F1),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "journal",
                    onClick = { currentTab = "journal" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Journal"
                        )
                    },
                    label = { Text("Journal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = Color(0xFFC6C7BD),
                        unselectedTextColor = Color(0xFFC6C7BD),
                        indicatorColor = SecondaryYellow
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "inventory",
                    onClick = { currentTab = "inventory" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Inventory"
                        )
                    },
                    label = { Text("Inventory") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = Color(0xFFC6C7BD),
                        unselectedTextColor = Color(0xFFC6C7BD),
                        indicatorColor = SecondaryYellow
                    )
                )

                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { currentTab = "profile" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        unselectedIconColor = Color(0xFFC6C7BD),
                        unselectedTextColor = Color(0xFFC6C7BD),
                        indicatorColor = SecondaryYellow
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "journal" -> {
                    JournalScreen(
                        onSignOut = onSignOut,
                        viewModel = journalViewModel
                    )
                }
                "inventory" -> {
                    PantryInventoryScreen(
                        viewModel = inventoryViewModel
                    )
                }
                "profile" -> {
                    ProfileScreen(
                        onSignOut = onSignOut,
                        viewModel = profileViewModel
                    )
                }
            }
        }
    }
}

