package com.deep.freezertofeast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deep.freezertofeast.ui.theme.FreezerToFeastTheme
import com.deep.freezertofeast.ui.screens.OnboardingScreen
import com.deep.freezertofeast.ui.screens.LoginScreen
import com.deep.freezertofeast.ui.screens.ProfileScreen
import com.deep.freezertofeast.ui.screens.JournalScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreezerToFeastTheme {
                val navController = rememberNavController()

                // Determine start destination based on authentication status
                val currentUser = Firebase.auth.currentUser
                val startDestination = if (currentUser != null) "journal" else "onboarding"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("profile") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onProfileSaved = {
                                    navController.navigate("journal") {
                                        popUpTo("profile") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("journal") {
                            JournalScreen(
                                onSignOut = {
                                    Firebase.auth.signOut()
                                    navController.navigate("onboarding") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}