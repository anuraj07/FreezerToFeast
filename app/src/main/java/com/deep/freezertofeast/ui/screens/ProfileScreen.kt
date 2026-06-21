package com.deep.freezertofeast.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val name by viewModel.name.collectAsState()
    val selectedDiet by viewModel.selectedDiet.collectAsState()
    val staples by viewModel.staples.collectAsState()
    val palatePreferences by viewModel.palatePreferences.collectAsState()
    val activePalates by viewModel.activePalates.collectAsState()
    val activeDays by viewModel.activeDays.collectAsState()
    val todayAiUsage by viewModel.todayAiUsage.collectAsState()

    // Accordion expand states for editing preferences
    var editDietExpanded by remember { mutableStateOf(false) }
    var editStaplesExpanded by remember { mutableStateOf(false) }

    // Text field state for custom palate preference
    var customPreferenceInput by remember { mutableStateOf("") }

    val dietRow1 = listOf("Balanced", "Vegetarian", "Vegan")
    val dietRow2 = listOf("Keto", "High Protein")

    val allPossibleStaples = listOf(
        "Oil", "Atta", "Rice", "Turmeric", "Salt", "Red Chili Powder", "Ghee"
    )

    // Sourcing Purity calculated dynamically based on staples checked
    val purityPercent = remember(staples) {
        if (allPossibleStaples.isNotEmpty()) {
            (staples.size.toFloat() / allPossibleStaples.size.toFloat() * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    // Pantry Health status calculated dynamically
    val pantryHealthText = remember(staples) {
        if (staples.size >= 5) "Optimal" else if (staples.size >= 2) "Good" else "Basic"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header matching Login Screen
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Freezer-to-Feast",
                    fontFamily = FontFamily.Serif,
                    fontSize = 38.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "Your Nourishment Profile",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = DarkCharcoal.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar Section with verified badge
            Box(
                modifier = Modifier.padding(bottom = 24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryGreen, MutedGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Native initials avatar representation
                    Text(
                        text = if (name.isNotBlank()) name.take(2).uppercase() else "AT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryYellow,
                        fontFamily = FontFamily.Serif
                    )
                }

                // Verified Badge Overlay
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Member",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // User Info (Name is dynamically loaded from Google sign in, read-only)
            Text(
                text = name.ifEmpty { "Nourished Explorer" },
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoal
            )
            Text(
                text = "ELITE MEMBER SINCE 2026",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MutedGreen,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic Nourishment Stats Bento Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Sourcing Purity
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryGreen.copy(alpha = 0.05f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Purity",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sourcing Purity",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$purityPercent%",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = purityPercent.toFloat() / 100f,
                        color = PrimaryGreen,
                        trackColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }

                // Column for Card 2 and Card 3
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 2: Active Rituals
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainer.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Rituals",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Active Rituals",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen
                        )
                        Text(
                            text = "$activeDays Days",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "Consistency is the ultimate luxury.",
                            fontSize = 9.sp,
                            fontStyle = FontStyle.Italic,
                            color = MutedGreen,
                            lineHeight = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Card 3: Pantry Health
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PrimaryGreen)
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = "Pantry Health",
                            tint = SecondaryYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pantry Health",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryYellow.copy(alpha = 0.7f)
                        )
                        Text(
                            text = pantryHealthText,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MutedGreen)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SecondaryYellow)
                                )
                            }
                            Text(
                                text = "+${staples.size} tracked",
                                fontSize = 9.sp,
                                color = SecondaryYellow,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Daily AI Usage Limit Indicator Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, OutlineColor.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "AI Usage",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily AI Recipe Limit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal
                            )
                        }
                        Text(
                            text = "$todayAiUsage / 4 recipes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (todayAiUsage >= 4) Color.Red else PrimaryGreen
                        )
                    }

                    LinearProgressIndicator(
                        progress = (todayAiUsage.toFloat() / 4f).coerceIn(0f, 1f),
                        color = if (todayAiUsage >= 4) Color.Red else PrimaryGreen,
                        trackColor = OutlineColor.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text = if (todayAiUsage >= 4)
                            "Daily usage limit reached. Resets tomorrow."
                        else
                            "You have ${4 - todayAiUsage} recipe requests remaining today.",
                        fontSize = 11.sp,
                        color = DarkCharcoal.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings List with dynamic preference editor (Saves automatically on change)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Settings Item 1: Dietary Intentions (Accordion for Dietary Focus)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editDietExpanded = !editDietExpanded }
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = "Dietary focus icon",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Dietary Intentions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkCharcoal
                                )
                                Text(
                                    text = "Current: $selectedDiet",
                                    fontSize = 11.sp,
                                    color = MutedGreen
                                )
                            }
                        }
                        Icon(
                            imageVector = if (editDietExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                            contentDescription = "Toggle",
                            tint = OutlineColor
                        )
                    }

                    AnimatedVisibility(
                        visible = editDietExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
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
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = diet,
                                            fontSize = 12.sp,
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
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = diet,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = chipTextColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Divider(color = OutlineColor.copy(alpha = 0.3f))

                // Settings Item 2: Palate Preferences (Staples + Custom palate preferences, checkable)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editStaplesExpanded = !editStaplesExpanded }
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = "Palate preferences icon",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Palate Preferences",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkCharcoal
                                )
                                val totalPreferences = staples.size + palatePreferences.size
                                Text(
                                    text = "$totalPreferences preferences defined",
                                    fontSize = 11.sp,
                                    color = MutedGreen
                                )
                            }
                        }
                        Icon(
                            imageVector = if (editStaplesExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                            contentDescription = "Toggle",
                            tint = OutlineColor
                        )
                    }

                    AnimatedVisibility(
                        visible = editStaplesExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section A: Foundational Staples
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "FOUNDATIONAL STAPLES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedGreen,
                                    letterSpacing = 1.sp
                                )
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
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$checkmark$staple",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = DarkCharcoal
                                                )
                                            }
                                        }
                                        if (rowStaples.size < 3) {
                                            repeat(3 - rowStaples.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            // Section B: Custom Palate Preferences (Check/Uncheck option + Remove option)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "CUSTOM PALATE PREFERENCES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedGreen,
                                    letterSpacing = 1.sp
                                )

                                if (palatePreferences.isEmpty()) {
                                    Text(
                                        text = "No custom palate preferences defined yet.",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = DarkCharcoal.copy(alpha = 0.5f)
                                    )
                                } else {
                                    val chunkedPrefs = palatePreferences.chunked(3)
                                    chunkedPrefs.forEach { rowPrefs ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowPrefs.forEach { pref ->
                                                val isSelected = activePalates.contains(pref)
                                                val chipBg = if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else SurfaceContainer
                                                val checkmark = if (isSelected) "✓ " else ""

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(chipBg)
                                                        .clickable { viewModel.toggleActivePalate(pref) }
                                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = "$checkmark$pref",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = DarkCharcoal,
                                                            modifier = Modifier.weight(1f, fill = false),
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove preference",
                                                            tint = DarkCharcoal.copy(alpha = 0.5f),
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .clickable { viewModel.removePalatePreference(pref) }
                                                        )
                                                    }
                                                }
                                            }
                                            if (rowPrefs.size < 3) {
                                                repeat(3 - rowPrefs.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Input row to add new palate preference
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customPreferenceInput,
                                        onValueChange = { customPreferenceInput = it },
                                        placeholder = { Text("e.g. Mild Spicy, Gluten Free") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryGreen,
                                            unfocusedBorderColor = OutlineColor,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedTextColor = DarkCharcoal,
                                            unfocusedTextColor = DarkCharcoal,
                                            focusedPlaceholderColor = DarkCharcoal.copy(alpha = 0.5f),
                                            unfocusedPlaceholderColor = DarkCharcoal.copy(alpha = 0.5f)
                                        )
                                    )
                                    IconButton(
                                        onClick = {
                                            if (customPreferenceInput.isNotBlank()) {
                                                viewModel.addPalatePreference(customPreferenceInput)
                                                customPreferenceInput = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(PrimaryGreen, RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add custom preference",
                                            tint = SecondaryYellow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = OutlineColor.copy(alpha = 0.3f))

                // Settings Item 3: Subscription Tier (Elite display badge showing coming soon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Premium icon",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Subscription Tier",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkCharcoal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(OutlineColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "COMING SOON",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkCharcoal
                                    )
                                }
                            }
                            Text(
                                text = "Coming soon",
                                fontSize = 11.sp,
                                color = MutedGreen
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = OutlineColor
                    )
                }

                Divider(color = OutlineColor.copy(alpha = 0.3f))

                // Settings Item 4: Security (Showing coming soon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security icon",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Security",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkCharcoal
                            )
                            Text(
                                text = "Coming soon",
                                fontSize = 11.sp,
                                color = MutedGreen
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = OutlineColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Out Button matching the bottom of settings list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSignOut() }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIGN OUT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = DarkCharcoal.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
