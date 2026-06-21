package com.deep.freezertofeast.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
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
import coil.request.ImageRequest
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val selectedDate by viewModel.selectedDate.collectAsState()
    val customSlotsList by viewModel.customSlots.collectAsState()
    val waterGlasses by viewModel.waterGlasses.collectAsState()

    val dailyProtein by viewModel.dailyProtein.collectAsState()
    val dailyFiber by viewModel.dailyFiber.collectAsState()
    val dailyCarbs by viewModel.dailyCarbs.collectAsState()
    val dailyCalories by viewModel.dailyCalories.collectAsState()

    val baseSlots = listOf(
        "breakfast" to "Nashta",
        "lunch" to "Dopahar ka Khaana",
        "dinner" to "Raat ka Khaana"
    )

    val allSlots = remember(customSlotsList) {
        baseSlots + customSlotsList.map { name ->
            val id = name.trim().lowercase().replace(" ", "_")
            id to name
        }
    }

    // Active ingredient logging slot and picked image list state
    var activeIngredientSlot by remember { mutableStateOf<String?>(null) }
    val pickedImages = remember { mutableStateListOf<Bitmap>() }
    var manualIngredientsText by remember { mutableStateOf("") }

    // Dialog for adding custom slot
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var newSlotName by remember { mutableStateOf("") }

    // Dialog for recipe detail view
    var showRecipeDetail by remember { mutableStateOf<MealData?>(null) }

    // Launchers for picking images
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            pickedImages.add(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                pickedImages.add(bitmap)
            }
        }
    }

    // Format selected date nicely
    val formattedDate = when (selectedDate) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy"))
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
        ) {
            // Header bar matching journal.html (Removed Sign Out option)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                h1Branding()
            }

            // Personalized Greeting (Greets the user dynamically)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "A morning of mindfulness, $userName",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.selectPreviousDay() }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Day",
                        tint = PrimaryGreen
                    )
                }
                
                Text(
                    text = formattedDate,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { viewModel.selectNextDay() }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Day",
                        tint = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Meal slots list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                allSlots.forEach { (slotId, slotName) ->
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
                                    if (meal.imageUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(meal.imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = meal.recipeName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
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
                                    }

                                    // Gradient overlay to ensure text readability
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.8f)
                                                    )
                                                )
                                            )
                                    )

                                    // Macro overlay on top-right corner
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "P: ${meal.protein}g",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "F: ${meal.fiber}g",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "C: ${meal.carbs}g",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

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
                                                    text = "${meal.calories} kcal",
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
                                    .clickable {
                                        activeIngredientSlot = slotId
                                        pickedImages.clear()
                                        manualIngredientsText = ""
                                    }
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
                                        text = "Tap to add ingredients & images",
                                        fontSize = 12.sp,
                                        color = DarkCharcoal.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Custom Meal Slot button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(1.dp, OutlineColor),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(SurfaceContainer.copy(alpha = 0.3f))
                        .clickable { showAddSlotDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add slot",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Custom Meal Slot",
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Daily Ritual Progress Section
            val progressPercent = remember(dailyProtein, dailyFiber, dailyCalories) {
                val proteinGoal = 60f
                val fiberGoal = 25f
                val caloriesGoal = 2000f
                
                val pProgress = if (dailyProtein > 0) (dailyProtein.toFloat() / proteinGoal) else 0f
                val fProgress = if (dailyFiber > 0) (dailyFiber.toFloat() / fiberGoal) else 0f
                val cProgress = if (dailyCalories > 0) (dailyCalories.toFloat() / caloriesGoal) else 0f
                
                val avg = (pProgress + fProgress + cProgress) / 3f
                (avg.coerceIn(0f, 1f) * 100).toInt()
            }

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
                        text = "$progressPercent%",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = progressPercent.toFloat() / 100f,
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
                        Text(text = "${waterGlasses * 250} ml", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Proteins", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "${dailyProtein}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Fiber", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "${dailyFiber}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Carbs", fontSize = 10.sp, color = MutedGreen)
                        Text(text = "${dailyCarbs}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                }

                // Interactive Water Intake Tracker Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, OutlineColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Opacity,
                                    contentDescription = "Water",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Water Intake Tracker",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoal
                                )
                                Text(
                                    text = "$waterGlasses glasses (${waterGlasses * 250} ml)",
                                    fontSize = 12.sp,
                                    color = DarkCharcoal.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.updateWaterIntake(maxOf(0, waterGlasses - 1)) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, OutlineColor.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease water",
                                    tint = DarkCharcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "$waterGlasses",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkCharcoal,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { viewModel.updateWaterIntake(waterGlasses + 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, OutlineColor.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase water",
                                    tint = DarkCharcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
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
                            text = "Analyzing Ingredients...",
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

        // 2. Unified Ingredient Logging Dialog (Supports Multiple Images + Manual Input)
        if (activeIngredientSlot != null) {
            val slotId = activeIngredientSlot!!
            val displaySlotName = allSlots.firstOrNull { it.first == slotId }?.second ?: slotId

            Dialog(onDismissRequest = {
                activeIngredientSlot = null
                pickedImages.clear()
                manualIngredientsText = ""
            }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Log Ingredients for $displaySlotName",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            textAlign = TextAlign.Center
                        )
                        Divider(color = OutlineColor.copy(alpha = 0.2f))

                        // Images display section
                        Text(
                            text = "Ingredients Images (${pickedImages.size} added)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        if (pickedImages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .border(BorderStroke(1.dp, OutlineColor), shape = RoundedCornerShape(12.dp))
                                    .background(BackgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No images added. Take photos or choose from gallery.",
                                    fontSize = 11.sp,
                                    color = DarkCharcoal.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pickedImages.forEachIndexed { index, bitmap ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, OutlineColor, RoundedCornerShape(8.dp))
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Picked Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Delete button
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .clickable { pickedImages.removeAt(index) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Image",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Buttons row for Camera / Gallery
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Camera",
                                        modifier = Modifier.size(16.dp),
                                        tint = SecondaryYellow
                                    )
                                    Text(text = "Camera", color = SecondaryYellow, fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Photo,
                                        contentDescription = "Gallery",
                                        modifier = Modifier.size(16.dp),
                                        tint = SecondaryYellow
                                    )
                                    Text(text = "Gallery", color = SecondaryYellow, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Manual Input
                        Text(
                            text = "Or Enter Ingredients Manually",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        OutlinedTextField(
                            value = manualIngredientsText,
                            onValueChange = { manualIngredientsText = it },
                            placeholder = { Text("e.g. Tomatoes, Paneer, Capsicum") },
                            modifier = Modifier.fillMaxWidth(),
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dialog CTA buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                activeIngredientSlot = null
                                pickedImages.clear()
                                manualIngredientsText = ""
                            }) {
                                Text(text = "Cancel", color = DarkCharcoal.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val slot = activeIngredientSlot
                                    if (slot != null) {
                                        if (pickedImages.isNotEmpty()) {
                                            viewModel.requestRecipeWithImages(pickedImages.toList(), slot)
                                        } else if (manualIngredientsText.isNotBlank()) {
                                            viewModel.updateRawInput(manualIngredientsText)
                                            viewModel.updateSelectedSlot(slot)
                                            viewModel.requestRecipe()
                                        }
                                    }
                                    activeIngredientSlot = null
                                    pickedImages.clear()
                                    manualIngredientsText = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                enabled = pickedImages.isNotEmpty() || manualIngredientsText.isNotBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Suggest Recipe", color = SecondaryYellow)
                            }
                        }
                    }
                }
            }
        }

        // 3. Add Custom Slot Dialog
        if (showAddSlotDialog) {
            Dialog(onDismissRequest = { showAddSlotDialog = false }) {
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
                            text = "Add Custom Meal Slot",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Divider(color = OutlineColor.copy(alpha = 0.2f))

                        OutlinedTextField(
                            value = newSlotName,
                            onValueChange = { newSlotName = it },
                            placeholder = { Text("e.g. Evening Chai, Midnight Snack") },
                            modifier = Modifier.fillMaxWidth(),
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddSlotDialog = false }) {
                                Text(text = "Cancel", color = DarkCharcoal.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newSlotName.isNotBlank()) {
                                        viewModel.addCustomSlot(newSlotName)
                                    }
                                    showAddSlotDialog = false
                                    newSlotName = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Add Slot", color = SecondaryYellow)
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
