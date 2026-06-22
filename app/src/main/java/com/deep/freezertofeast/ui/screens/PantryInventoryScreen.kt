package com.deep.freezertofeast.ui.screens

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
import com.deep.freezertofeast.PantryItem
import com.deep.freezertofeast.ui.viewmodels.InventoryViewModel
import com.deep.freezertofeast.PrimaryGreen
import com.deep.freezertofeast.SecondaryYellow
import com.deep.freezertofeast.BackgroundColor
import com.deep.freezertofeast.MutedGreen
import com.deep.freezertofeast.DarkCharcoal
import com.deep.freezertofeast.OutlineColor
import com.deep.freezertofeast.SurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryInventoryScreen(
    viewModel: InventoryViewModel = viewModel()
) {
    val items by viewModel.pantryItems.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    
    // Add Item Dialog state
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddOutOfStockDialog by remember { mutableStateOf(false) }
    
    // Add item form states
    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("perishables") } // "perishables", "proteins", "foundations"
    var itemQuantity by remember { mutableStateOf("") }
    var itemExpiryInfo by remember { mutableStateOf("") }
    var itemTag by remember { mutableStateOf("") }
    
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }
    
    val perishables = filteredItems.filter { it.category == "perishables" && !it.outOfStock }
    val proteins = filteredItems.filter { it.category == "proteins" && !it.outOfStock }
    val foundations = filteredItems.filter { it.category == "foundations" && !it.outOfStock }
    val outOfStockItems = items.filter { it.outOfStock }
    
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
            // Header matching other screens
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Freezer-to-Feast",
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    letterSpacing = (-0.5).sp
                )
            }
            
            // Screen Title & Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Larder Inventory",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
                Text(
                    text = "A collection of ingredients currently tracked in your larder.",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = DarkCharcoal.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your larder...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MutedGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Category: Fresh Harvest (Perishables)
            if (perishables.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Fresh Harvest",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "PERISHABLES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            letterSpacing = 1.5.sp
                        )
                    }
                    
                    val chunkedPerishables = perishables.chunked(2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunkedPerishables.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.85f),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .background(SurfaceContainer)
                                            ) {
                                                if (item.imageUrl.isNotEmpty()) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(item.imageUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = item.name,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                if (item.tag.isNotEmpty()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(8.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(SecondaryYellow.copy(alpha = 0.9f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = item.tag,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = DarkCharcoal
                                                        )
                                                    }
                                                }
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = item.name,
                                                    fontFamily = FontFamily.Serif,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkCharcoal
                                                )
                                                if (item.expiryInfo.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Timer,
                                                            contentDescription = "Expiry",
                                                            tint = if (item.expiryInfo.contains("within", ignoreCase = true) || item.expiryInfo.contains("hours", ignoreCase = true)) Color.Red else MutedGreen,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = item.expiryInfo,
                                                            fontSize = 10.sp,
                                                            color = if (item.expiryInfo.contains("within", ignoreCase = true) || item.expiryInfo.contains("hours", ignoreCase = true)) Color.Red else MutedGreen
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = { viewModel.toggleOutOfStock(item) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(28.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                                    contentPadding = PaddingValues(0.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(text = "Out of Stock", fontSize = 10.sp, color = SecondaryYellow)
                                                }
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            
            // Category: Proteins & Dals
            if (proteins.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Proteins & Dals",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "NOURISHMENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            letterSpacing = 1.5.sp
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        proteins.forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(SurfaceContainer)
                                    ) {
                                        if (item.imageUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(item.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = item.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        if (item.tag.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(PrimaryGreen.copy(alpha = 0.8f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.tag,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SecondaryYellow
                                                )
                                            }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .fillMaxHeight()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = item.name,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkCharcoal
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Quantity: ${item.quantity} • Restocked: ${item.lastRestocked}",
                                                fontSize = 11.sp,
                                                color = DarkCharcoal.copy(alpha = 0.6f)
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.toggleOutOfStock(item) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(28.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                            contentPadding = PaddingValues(0.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(text = "Mark Out of Stock", fontSize = 10.sp, color = SecondaryYellow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Category: Foundations (Staples)
            if (foundations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "The Foundations",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "STAPLES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            letterSpacing = 1.5.sp
                        )
                    }
                    
                    val chunkedStaples = foundations.chunked(3)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunkedStaples.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.toggleOutOfStock(item) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, OutlineColor.copy(alpha = 0.3f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceContainer)
                                            ) {
                                                if (item.imageUrl.isNotEmpty()) {
                                                    AsyncImage(
                                                        model = ImageRequest.Builder(LocalContext.current)
                                                            .data(item.imageUrl)
                                                            .crossfade(true)
                                                            .build(),
                                                        contentDescription = item.name,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkCharcoal,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (item.quantity.isNotEmpty()) item.quantity else "Low Stock",
                                                fontSize = 10.sp,
                                                color = MutedGreen,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Section: Out of Stock / Shopping List
            Spacer(modifier = Modifier.height(40.dp))
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
                            text = "SHOPPING LIST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGreen,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Out of Stock Items",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                    }
                    TextButton(
                        onClick = { showAddOutOfStockDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Add Item", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (outOfStockItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(BorderStroke(1.dp, OutlineColor.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp))
                            .background(SurfaceContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All items restocked. Larder is optimal!",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = DarkCharcoal.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, OutlineColor.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            outOfStockItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryGreen.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingBag,
                                                contentDescription = "Item",
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = item.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkCharcoal
                                            )
                                            Text(
                                                text = "${item.category.replaceFirstChar { it.uppercase() }}${if (item.quantity.isNotEmpty()) " • " + item.quantity else ""}",
                                                fontSize = 10.sp,
                                                color = DarkCharcoal.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.toggleOutOfStock(item) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = "Restock", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteItem(item.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.Red.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = {
                itemName = ""
                itemQuantity = ""
                itemExpiryInfo = ""
                itemTag = ""
                selectedCategory = "perishables"
                showAddItemDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = PrimaryGreen,
            contentColor = SecondaryYellow,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Item",
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Dialog: Add Pantry Item
        if (showAddItemDialog || showAddOutOfStockDialog) {
            val isOutOfStockDirect = showAddOutOfStockDialog
            Dialog(
                onDismissRequest = {
                    showAddItemDialog = false
                    showAddOutOfStockDialog = false
                }
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isOutOfStockDirect) "Add Out of Stock Item" else "Add Pantry Item",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Divider(color = OutlineColor.copy(alpha = 0.2f))
                        
                        // Item Name
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            placeholder = { Text("e.g. Tuscan Kale, masoor dal, paneer") },
                            label = { Text("Item Name") },
                            modifier = Modifier.fillMaxWidth(),
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
                        
                        // Category Select
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedGreen)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("perishables" to "Fresh", "proteins" to "Protein", "foundations" to "Staple").forEach { (catId, catLabel) ->
                                    val isSel = selectedCategory == catId
                                    val chipBg = if (isSel) PrimaryGreen else SurfaceContainer
                                    val chipTextColor = if (isSel) SecondaryYellow else DarkCharcoal
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(chipBg)
                                            .clickable { selectedCategory = catId }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = catLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = chipTextColor)
                                    }
                                }
                            }
                        }
                        
                        // Quantity
                        OutlinedTextField(
                            value = itemQuantity,
                            onValueChange = { itemQuantity = it },
                            placeholder = { Text("e.g. 2.5kg, 3 Bulbs, 500g") },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth(),
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
                        
                        // Expiry Status / Stock level
                        OutlinedTextField(
                            value = itemExpiryInfo,
                            onValueChange = { itemExpiryInfo = it },
                            placeholder = { Text("e.g. Use within 2 days, Peak Freshness") },
                            label = { Text("Expiry Status / Details") },
                            modifier = Modifier.fillMaxWidth(),
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
                        
                        // Tag / Label (Organic, Local, etc.)
                        OutlinedTextField(
                            value = itemTag,
                            onValueChange = { itemTag = it },
                            placeholder = { Text("e.g. Organic, Local, Bulk") },
                            label = { Text("Tag (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
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
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showAddItemDialog = false
                                    showAddOutOfStockDialog = false
                                }
                            ) {
                                Text(text = "Cancel", color = DarkCharcoal.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (itemName.isNotBlank()) {
                                        viewModel.addItem(
                                            name = itemName,
                                            category = selectedCategory,
                                            quantity = itemQuantity,
                                            expiryInfo = itemExpiryInfo,
                                            tag = itemTag,
                                            outOfStock = isOutOfStockDirect
                                        )
                                    }
                                    showAddItemDialog = false
                                    showAddOutOfStockDialog = false
                                    itemName = ""
                                },
                                enabled = itemName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = if (isOutOfStockDirect) "Add to List" else "Add Item", color = SecondaryYellow)
                            }
                        }
                    }
                }
            }
        }
    }
}
