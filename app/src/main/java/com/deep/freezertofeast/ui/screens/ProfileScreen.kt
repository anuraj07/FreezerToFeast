package com.deep.freezertofeast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.freezertofeast.ui.components.CommonTopBar
import com.deep.freezertofeast.ui.viewmodels.ProfileViewModel
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.BackgroundColor
import com.deep.freezertofeast.MutedGreen
import com.deep.freezertofeast.DarkCharcoal
import com.deep.freezertofeast.OutlineColor
import com.deep.freezertofeast.SurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onProfileSaved: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val name by viewModel.name.collectAsState()
    val selectedDiet by viewModel.selectedDiet.collectAsState()
    val staples by viewModel.staples.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val dietRow1 = listOf("Balanced", "Vegetarian", "Vegan")
    val dietRow2 = listOf("Keto", "High Protein")

    val allPossibleStaples = listOf(
        "Oil", "Atta", "Rice", "Turmeric", "Salt", "Red Chili Powder", "Ghee"
    )

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onProfileSaved()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(title = "Your Nourishment Profile")
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Personalize your kitchen parameters to align cooking suggestions.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    color = MutedGreen,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = OutlineColor,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dietary focus header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "DIETARY FOCUS",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGreen,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dietary focus selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dietRow1.forEach { diet ->
                            val isSelected = selectedDiet == diet
                            val chipBg = if (isSelected) PrimaryGreen else SurfaceContainer
                            val chipTextColor = if (isSelected) SecondaryYellow else DarkCharcoal

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .clickable { viewModel.updateDiet(diet) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diet,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipTextColor
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dietRow2.forEach { diet ->
                            val isSelected = selectedDiet == diet
                            val chipBg = if (isSelected) PrimaryGreen else SurfaceContainer
                            val chipTextColor = if (isSelected) SecondaryYellow else DarkCharcoal

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .clickable { viewModel.updateDiet(diet) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diet,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipTextColor
                                )
                            }
                        }
                        // Spacer chip to keep rows equal size/look good
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Staples header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "PANTRY STAPLES (ALWAYS AVAILABLE)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGreen,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Staples grid (Simple nested Row rows for layout compatibility)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chunkedStaples = allPossibleStaples.chunked(3)
                    chunkedStaples.forEach { rowStaples ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowStaples.forEach { staple ->
                                val isSelected = staples.contains(staple)
                                val chipBg = if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else SurfaceContainer
                                val checkmark = if (isSelected) "✓ " else ""

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg)
                                        .clickable { viewModel.toggleStaple(staple) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$checkmark$staple",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkCharcoal
                                    )
                                }
                            }
                            // Add placeholder spacers if row has fewer than 3 elements
                            if (rowStaples.size < 3) {
                                repeat(3 - rowStaples.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Save button
                if (saving) {
                    CircularProgressIndicator(color = PrimaryGreen)
                } else {
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Save Preferences & Continue",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryYellow
                        )
                    }
                }
            }
        }
    }
}
