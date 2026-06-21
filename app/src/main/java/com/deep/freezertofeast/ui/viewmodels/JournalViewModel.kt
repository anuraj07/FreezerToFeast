package com.deep.freezertofeast.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.freezertofeast.MealData
import com.deep.freezertofeast.MealIntelligenceRepository
import com.deep.freezertofeast.Resource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JournalViewModel(
    private val repository: MealIntelligenceRepository = MealIntelligenceRepository()
) : ViewModel() {

    private val _rawInput = MutableStateFlow("")
    val rawInput: StateFlow<String> = _rawInput.asStateFlow()

    private val _selectedSlot = MutableStateFlow("lunch")
    val selectedSlot: StateFlow<String> = _selectedSlot.asStateFlow()

    private val _statusResource = MutableStateFlow<Resource<MealData>?>(null)
    val statusResource: StateFlow<Resource<MealData>?> = _statusResource.asStateFlow()

    private val _generatedRecipe = MutableStateFlow<MealData?>(null)
    val generatedRecipe: StateFlow<MealData?> = _generatedRecipe.asStateFlow()

    private val _loggedMeals = MutableStateFlow<Map<String, MealData>>(emptyMap())
    val loggedMeals: StateFlow<Map<String, MealData>> = _loggedMeals.asStateFlow()

    private val _userName = MutableStateFlow("Explorer")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // State flow for selected calendar date
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Dynamic custom meal slots list
    private val _customSlots = MutableStateFlow<List<String>>(emptyList())
    val customSlots: StateFlow<List<String>> = _customSlots.asStateFlow()

    // Water intake tracking (number of glasses)
    private val _waterGlasses = MutableStateFlow(0)
    val waterGlasses: StateFlow<Int> = _waterGlasses.asStateFlow()

    // Dynamic daily nutrition totals computed from logged meals
    private val _dailyProtein = MutableStateFlow(0)
    val dailyProtein: StateFlow<Int> = _dailyProtein.asStateFlow()

    private val _dailyFiber = MutableStateFlow(0)
    val dailyFiber: StateFlow<Int> = _dailyFiber.asStateFlow()

    private val _dailyCarbs = MutableStateFlow(0)
    val dailyCarbs: StateFlow<Int> = _dailyCarbs.asStateFlow()

    private val _dailyCalories = MutableStateFlow(0)
    val dailyCalories: StateFlow<Int> = _dailyCalories.asStateFlow()

    init {
        loadLoggedMeals()
    }

    fun updateRawInput(input: String) {
        _rawInput.value = input
    }

    fun updateSelectedSlot(slot: String) {
        _selectedSlot.value = slot
        _generatedRecipe.value = _loggedMeals.value[slot]
        _statusResource.value = null
    }

    fun selectPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        loadLoggedMeals()
    }

    fun selectNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        loadLoggedMeals()
    }

    fun loadLoggedMeals() {
        val user = Firebase.auth.currentUser ?: return
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            try {
                // Load profile name and custom slots list for personalized greeting
                val profileDoc = Firebase.firestore.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .await()
                
                var customList = emptyList<String>()
                if (profileDoc.exists()) {
                    _userName.value = profileDoc.getString("name") ?: user.displayName ?: "Explorer"
                    @Suppress("UNCHECKED_CAST")
                    customList = profileDoc.get("customSlots") as? List<String> ?: emptyList()
                    _customSlots.value = customList
                } else {
                    _userName.value = user.displayName ?: "Explorer"
                    _customSlots.value = emptyList()
                }

                // Reset before loading to ensure clean state transitions
                _loggedMeals.value = emptyMap()
                _generatedRecipe.value = null
                _waterGlasses.value = 0
                _dailyProtein.value = 0
                _dailyFiber.value = 0
                _dailyCarbs.value = 0
                _dailyCalories.value = 0

                // Load daily meals logged
                val doc = Firebase.firestore.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("daily_meals")
                    .document(dateString)
                    .get()
                    .await()

                if (doc.exists()) {
                    // Load water glasses
                    val glasses = (doc.get("water_glasses") as? Number)?.toInt() ?: 0
                    _waterGlasses.value = glasses

                    // Determine slot keys to load (standard + custom slots)
                    val standardSlots = listOf("breakfast", "lunch", "dinner", "snacks")
                    val customSlotIds = customList.map { it.trim().lowercase().replace(" ", "_") }
                    val allSlots = standardSlots + customSlotIds

                    val meals = mutableMapOf<String, MealData>()
                    var totalProtein = 0
                    var totalFiber = 0
                    var totalCarbs = 0
                    var totalCalories = 0

                    for (slot in allSlots) {
                        val mealMap = doc.get(slot) as? Map<*, *>
                        if (mealMap != null) {
                            val recipeName = mealMap["recipeName"] as? String ?: ""
                            val prepTime = (mealMap["prepTime"] as? Number)?.toInt() ?: 0
                            @Suppress("UNCHECKED_CAST")
                            val ingredients = mealMap["ingredientsUsed"] as? List<String> ?: emptyList()
                            @Suppress("UNCHECKED_CAST")
                            val steps = mealMap["steps"] as? List<String> ?: emptyList()
                            val protein = (mealMap["protein"] as? Number)?.toInt() ?: 0
                            val fiber = (mealMap["fiber"] as? Number)?.toInt() ?: 0
                            val carbs = (mealMap["carbs"] as? Number)?.toInt() ?: 0
                            val calories = (mealMap["calories"] as? Number)?.toInt() ?: 0
                            val imageUrl = mealMap["imageUrl"] as? String ?: ""

                            val mealData = MealData(recipeName, prepTime, ingredients, steps, protein, fiber, carbs, calories, imageUrl)
                            meals[slot] = mealData

                            // Sum dynamic nutrition metrics
                            totalProtein += protein
                            totalFiber += fiber
                            totalCarbs += carbs
                            totalCalories += calories
                        }
                    }
                    _loggedMeals.value = meals
                    _generatedRecipe.value = meals[_selectedSlot.value]

                    _dailyProtein.value = totalProtein
                    _dailyFiber.value = totalFiber
                    _dailyCarbs.value = totalCarbs
                    _dailyCalories.value = totalCalories
                }
            } catch (e: Exception) {
                // Ignore load errors in development
            }
        }
    }

    fun addCustomSlot(slotName: String) {
        val user = Firebase.auth.currentUser ?: return
        if (slotName.isBlank()) return
        
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .update("customSlots", com.google.firebase.firestore.FieldValue.arrayUnion(slotName))
                    .await()
                loadLoggedMeals()
            } catch (e: Exception) {
                // Fallback / merge if document update fails offline
                try {
                    Firebase.firestore.collection("freezer_to_feast")
                        .document("app")
                        .collection("users")
                        .document(user.uid)
                        .set(mapOf("customSlots" to listOf(slotName)), com.google.firebase.firestore.SetOptions.merge())
                        .await()
                    loadLoggedMeals()
                } catch (ex: Exception) {
                    val current = _customSlots.value.toMutableList()
                    if (!current.contains(slotName)) {
                        current.add(slotName)
                        _customSlots.value = current
                    }
                }
            }
        }
    }

    fun updateWaterIntake(glasses: Int) {
        val user = Firebase.auth.currentUser ?: return
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("daily_meals")
                    .document(dateString)
                    .update("water_glasses", glasses)
                    .await()
                _waterGlasses.value = glasses
            } catch (e: Exception) {
                // Set document if not created yet
                try {
                    Firebase.firestore.collection("freezer_to_feast")
                        .document("app")
                        .collection("users")
                        .document(user.uid)
                        .collection("daily_meals")
                        .document(dateString)
                        .set(mapOf("water_glasses" to glasses, "timestamp" to com.google.firebase.Timestamp.now()), com.google.firebase.firestore.SetOptions.merge())
                        .await()
                    _waterGlasses.value = glasses
                } catch (ex: Exception) {
                    _waterGlasses.value = glasses
                }
            }
        }
    }

    fun requestRecipe() {
        val input = _rawInput.value
        val slot = _selectedSlot.value
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (input.isBlank()) return

        viewModelScope.launch {
            repository.generateAndLogMeal(input, slot, dateString).collectLatest { resource ->
                _statusResource.value = resource
                if (resource is Resource.Success) {
                    _generatedRecipe.value = resource.data
                    _rawInput.value = ""
                    loadLoggedMeals()
                }
            }
        }
    }

    fun requestRecipeWithImages(bitmaps: List<Bitmap>, slot: String) {
        val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (bitmaps.isEmpty()) return
        viewModelScope.launch {
            repository.generateAndLogMealWithImages(bitmaps, slot, dateString).collectLatest { resource ->
                _statusResource.value = resource
                if (resource is Resource.Success) {
                    _generatedRecipe.value = resource.data
                    loadLoggedMeals()
                }
            }
        }
    }

    fun resetStatus() {
        _statusResource.value = null
    }
}
