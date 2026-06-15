package com.deep.freezertofeast.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.freezertofeast.Resource
import com.deep.freezertofeast.ui.components.CommonTopBar
import com.deep.freezertofeast.ui.viewmodels.JournalViewModel
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.BackgroundColor
import com.deep.freezertofeast.MutedGreen
import com.deep.freezertofeast.DarkCharcoal
import com.deep.freezertofeast.OutlineColor
import com.deep.freezertofeast.SurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onSignOut: () -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val rawInput by viewModel.rawInput.collectAsState()
    val selectedSlot by viewModel.selectedSlot.collectAsState()
    val statusResource by viewModel.statusResource.collectAsState()
    val generatedRecipe by viewModel.generatedRecipe.collectAsState()

    val slots = listOf("breakfast", "lunch", "dinner", "snacks")

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Daily Nourishment",
                actions = {
                    Text(
                        text = "Sign Out 🚪",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryYellow,
                        modifier = Modifier
                            .clickable { onSignOut() }
                            .padding(8.dp)
                    )
                }
            )
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
                    .padding(24.dp)
            ) {
                // Meal Slot Selector
                Text(
                    text = "MEAL SLOT",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slots.forEach { slot ->
                        val isSelected = selectedSlot == slot
                        val chipBg = if (isSelected) PrimaryGreen else SurfaceContainer
                        val textColor = if (isSelected) SecondaryYellow else DarkCharcoal

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .clickable { viewModel.updateSelectedSlot(slot) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = slot.replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ingredients Input
                Text(
                    text = "WHAT INGREDIENTS DO YOU HAVE?",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { viewModel.updateRawInput(it) },
                    placeholder = { Text("e.g. Paneer, bell pepper, tomato, cream...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = OutlineColor,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                if (statusResource is Resource.Loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else {
                    Button(
                        onClick = { viewModel.requestRecipe() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(25.dp),
                        enabled = rawInput.isNotBlank()
                    ) {
                        Text(
                            text = "Generate Recipe with AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recipe Display / Loading / Error
                AnimatedVisibility(
                    visible = statusResource is Resource.Error,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (statusResource is Resource.Error) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Failed to Generate Recipe",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = (statusResource as Resource.Error).errorMessage,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = generatedRecipe != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    generatedRecipe?.let { recipe ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SecondaryYellow),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, OutlineColor.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Badge indicating success logging
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryGreen.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Logged to ${selectedSlot.uppercase()} ✓",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = recipe.recipeName,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoal
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🗓 ",
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${recipe.prepTime} minutes prep time",
                                        fontSize = 14.sp,
                                        color = DarkCharcoal.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Ingredients Used",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                recipe.ingredientsUsed.forEach { ingredient ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "▸ ",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = ingredient,
                                            fontSize = 14.sp,
                                            color = DarkCharcoal
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Instructions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                recipe.steps.forEachIndexed { index, step ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. ",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = step,
                                            fontSize = 14.sp,
                                            color = DarkCharcoal,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
