package com.deep.freezertofeast.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.freezertofeast.R
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.BackgroundColor
import com.deep.freezertofeast.MutedGreen
import com.deep.freezertofeast.DarkCharcoal

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit
) {
    val OnboardingBackgroundColor = Color(0xFFF4F3EF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Hero Image Section with Fade Effect Overlay extending to the very top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color.LightGray)
            ) {
                // High-resolution local image asset
                Image(
                    painter = painterResource(id = R.drawable.fresh_harvest_onboarding),
                    contentDescription = "Artisanal fresh vegetables on kitchen table",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark tint overlay for luxury photography feel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f))
                )
                // Vertical gradient fade overlay: transparent at the top to off-white background color at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    OnboardingBackgroundColor.copy(alpha = 0.5f),
                                    OnboardingBackgroundColor
                                )
                            )
                        )
                )
            }

            // Left-Aligned Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start // Left-aligned
            ) {
                Text(
                    text = "THE ART OF SUSTENANCE",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedGreen,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Nourish with Intention",
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkCharcoal,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Step into a world where every ingredient is a choice, and every meal is a ritual of restoration and mindful presence.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = DarkCharcoal.copy(alpha = 0.8f),
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Begin Journey Button
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Begin your journey",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryYellow
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

                Spacer(modifier = Modifier.height(14.dp))

                // Account Link (Left-aligned)
                Text(
                    text = "I already have an account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
