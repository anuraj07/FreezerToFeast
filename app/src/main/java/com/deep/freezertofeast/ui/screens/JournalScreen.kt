package com.deep.freezertofeast.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.deep.freezertofeast.Resource
import com.deep.freezertofeast.ui.viewmodels.JournalViewModel
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.BackgroundColor
import com.deep.freezertofeast.MutedGreen
import com.deep.freezertofeast.DarkCharcoal
import com.deep.freezertofeast.OutlineColor
import com.deep.freezertofeast.SurfaceContainer
import com.deep.freezertofeast.MealData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onSignOut: () -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val context = LocalContext.current
    val rawInput by viewModel.rawInput.collectAsState()
    val selectedSlot by viewModel.selectedSlot.collectAsState()
    val statusResource by viewModel.statusResource.collectAsState()
    val generatedRecipe by viewModel.generatedRecipe.collectAsState()
    val loggedMeals by viewModel.loggedMeals.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val slots = listOf(
        "breakfast" to "Nashta",
        "lunch" to "Dopahar ka Khaana",
        "dinner" to "Raat ka Khaana"
    )

    // Selection / camera dialog states
    var showDialogForSlot by remember { mutableStateOf<String?>(null) }
    var showRecipeDetail by remember { mutableStateOf<MealData?>(null) }

    // Launchers for picking images
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        val slot = showDialogForSlot
        if (bitmap != null && slot != null) {
            viewModel.requestRecipeWithImage(bitmap, slot)
        }
        showDialogForSlot = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val slot = showDialogForSlot
        if (uri != null && slot != null) {
            val bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                viewModel.requestRecipeWithImage(bitmap, slot)
            }
        }
        showDialogForSlot = null
    }

    // Manual Ingredient Input Text dialog
    var showTextInputDialogForSlot by remember { mutableStateOf<String?>(null) }
    var textInputIngredient by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header bar matching journal.html
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                h1Branding()
                Text(
                    text = "Sign Out 🚪",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { onSignOut() }
                        .padding(8.dp)
                )
            }

            // Personalized Greeting
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "A morning of mindfulness, Anuraj",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
                Text(
                    text = "Today is an invitation to nourish your essence.",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = DarkCharcoal.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Meal slots grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                slots.forEach { (slotId, slotName) ->
                    val meal = loggedMeals[slotId]

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = slotName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedGreen,
                                letterSpacing = 1.5.sp
                            )
                            if (meal != null) {
                                Text(
                                    text = "Logged",
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = PrimaryGreen
                                )
                            }
                        }

                        if (meal != null) {
                            // Meal Logged card state
                            Card(
                                onClick = { showRecipeDetail = meal },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.25f),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Custom botanic abstract gradient representing the dish
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        PrimaryGreen.copy(alpha = 0.1f),
                                                        PrimaryGreen.copy(alpha = 0.7f)
                                                    )
                                                )
                                            )
                                    )
                                    // Details overlay at the bottom
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(18.dp)
                                    ) {
                                        Text(
                                            text = meal.recipeName,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Whatshot,
                                                    contentDescription = "Calories",
                                                    tint = SecondaryYellow,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "320 kcal",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = "Time",
                                                    tint = SecondaryYellow,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${meal.prepTime} min prep",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Meal Empty state: Tap to scan ingredients
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.25f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        BorderStroke(1.dp, OutlineColor),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(SurfaceContainer.copy(alpha = 0.3f))
                                    .clickable { showDialogForSlot = slotId }
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, OutlineColor.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Scan icon",
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "A fresh harvest awaits",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkCharcoal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tap to take a picture of ingredients",
                                        fontSize = 12.sp,
                                        color = DarkCharcoal.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Daily Ritual Progress Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Divider(color = OutlineColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAILY RITUAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Nourishment Quality",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                    }
                    Text(
                        text = "84%",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = 0.84f,
                    color = PrimaryGreen,
                    trackColor = SurfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AWARENESS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGreen,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "OPTIMIZED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGreen,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Micro stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Hydration", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "1.8 / 2.5L", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Proteins", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "62g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Fiber", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "24g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Vitality", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "High", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // Padding for bottom navbar & FAB
        }

        // Floating Action Button to scan ingredient from camera
        LargeFloatingActionButton(
            onClick = { showDialogForSlot = "lunch" }, // Defaults image scan to lunch
            containerColor = PrimaryGreen,
            contentColor = SecondaryYellow,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Analyze Ingredient",
                modifier = Modifier.size(30.dp)
            )
        }

        // 1. Loading Overlay Dialog
        if (statusResource is Resource.Loading) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Analyzing Ingredient Photo...",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Identifying the harvest and crafting custom suggestions",
                            fontSize = 12.sp,
                            color = DarkCharcoal.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 2. Picture Source Selection Dialog
        if (showDialogForSlot != null) {
            Dialog(onDismissRequest = { showDialogForSlot = null }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Log Ingredient",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Divider(color = OutlineColor.copy(alpha = 0.2f))

                        Button(
                            onClick = { cameraLauncher.launch() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Take Photo", color = SecondaryYellow)
                            }
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Photo, contentDescription = "Gallery")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Choose from Gallery", color = SecondaryYellow)
                            }
                        }

                        TextButton(
                            onClick = {
                                textInputIngredient = ""
                                showTextInputDialogForSlot = showDialogForSlot
                                showDialogForSlot = null
                            }
                        ) {
                            Text(
                                text = "Or Enter Ingredients Manually",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { showDialogForSlot = null }
                        ) {
                            Text(text = "Cancel", color = DarkCharcoal.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        // 3. Text Input Dialog (Fallback)
        if (showTextInputDialogForSlot != null) {
            Dialog(onDismissRequest = { showTextInputDialogForSlot = null }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Enter Ingredients",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Divider(color = OutlineColor.copy(alpha = 0.2f))

                        OutlinedTextField(
                            value = textInputIngredient,
                            onValueChange = { textInputIngredient = it },
                            placeholder = { Text("e.g. Tomato, Paneer, Spinach...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = OutlineColor,
                                focusedContainerColor = BackgroundColor,
                                unfocusedContainerColor = BackgroundColor
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTextInputDialogForSlot = null }) {
                                Text(text = "Cancel", color = DarkCharcoal.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val slot = showTextInputDialogForSlot
                                    if (textInputIngredient.isNotBlank() && slot != null) {
                                        viewModel.updateRawInput(textInputIngredient)
                                        viewModel.updateSelectedSlot(slot)
                                        viewModel.requestRecipe()
                                    }
                                    showTextInputDialogForSlot = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text(text = "Suggest", color = SecondaryYellow)
                            }
                        }
                    }
                }
            }
        }

        // 4. Recipe Details Dialog
        if (showRecipeDetail != null) {
            val recipe = showRecipeDetail!!
            Dialog(onDismissRequest = { showRecipeDetail = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 550.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryYellow),
                    border = BorderStroke(1.dp, OutlineColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        // Header badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGreen.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DAILY NOURISHMENT ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = recipe.recipeName,
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${recipe.prepTime} minutes preparation",
                                fontSize = 13.sp,
                                color = DarkCharcoal.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ingredients list
                        Text(
                            text = "Ingredients Used & Side suggestions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        recipe.ingredientsUsed.forEach { ingredient ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ingredient,
                                    fontSize = 13.sp,
                                    color = DarkCharcoal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Steps list
                        Text(
                            text = "Instructions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        recipe.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = step,
                                    fontSize = 13.sp,
                                    color = DarkCharcoal,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showRecipeDetail = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Close Recipe", color = SecondaryYellow)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun h1Branding() {
    Text(
        text = "Freezer-to-Feast",
        fontFamily = FontFamily.Serif,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = DarkCharcoal,
        letterSpacing = (-0.5).sp
    )
}

// Converts Gallery image Uris to Bitmaps
fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}
