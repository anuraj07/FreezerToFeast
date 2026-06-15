package com.deep.freezertofeast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.freezertofeast.ui.components.CommonTopBar
import com.deep.freezertofeast.ui.viewmodels.LoginViewModel
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.BackgroundColor
import com.deep.freezertofeast.MutedGreen
import com.deep.freezertofeast.DarkCharcoal

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Welcome Back",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Subtle Background Atmosphere Circles (Decorative)
            Box(modifier = Modifier.fillMaxSize()) {
                // We draw simple gradient shapes or keep clean
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "Freezer-to-Feast",
                    fontFamily = FontFamily.Serif,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome back",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = DarkCharcoal.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Error / Settings Resolution Guide Display
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                if (loading) {
                    CircularProgressIndicator(color = PrimaryGreen)
                } else {
                    // Google Sign-In Button: "Enter the Kitchen"
                    Button(
                        onClick = { viewModel.signInWithGoogle() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(28.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ENTER THE KITCHEN",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryYellow,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "→",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryYellow
                            )
                        }
                    }
                }
            }
        }
    }
}
