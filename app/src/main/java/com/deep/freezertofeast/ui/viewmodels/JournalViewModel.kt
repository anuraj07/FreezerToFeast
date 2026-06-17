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

    fun loadLoggedMeals() {
        val user = Firebase.auth.currentUser ?: return
        val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            try {
                // Load profile name for personalized greeting
                val profileDoc = Firebase.firestore.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .await()
                if (profileDoc.exists()) {
                    _userName.value = profileDoc.getString("name") ?: "Explorer"
                }

                // Load daily meals logged
                val doc = Firebase.firestore.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("daily_meals")
                    .document(currentDate)
                    .get()
                    .await()

                if (doc.exists()) {
                    val meals = mutableMapOf<String, MealData>()
                    val slots = listOf("breakfast", "lunch", "dinner", "snacks")
                    for (slot in slots) {
                        val mealMap = doc.get(slot) as? Map<*, *>
                        if (mealMap != null) {
                            val recipeName = mealMap["recipeName"] as? String ?: ""
                            val prepTime = (mealMap["prepTime"] as? Number)?.toInt() ?: 0
                            @Suppress("UNCHECKED_CAST")
                            val ingredients = mealMap["ingredientsUsed"] as? List<String> ?: emptyList()
                            @Suppress("UNCHECKED_CAST")
                            val steps = mealMap["steps"] as? List<String> ?: emptyList()
                            meals[slot] = MealData(recipeName, prepTime, ingredients, steps)
                        }
                    }
                    _loggedMeals.value = meals
                    _generatedRecipe.value = meals[_selectedSlot.value]
                }
            } catch (e: Exception) {
                // Ignore load errors in development
            }
        }
    }

    fun requestRecipe() {
        val input = _rawInput.value
        val slot = _selectedSlot.value
        if (input.isBlank()) return

        viewModelScope.launch {
            repository.generateAndLogMeal(input, slot).collectLatest { resource ->
                _statusResource.value = resource
                if (resource is Resource.Success) {
                    _generatedRecipe.value = resource.data
                    _rawInput.value = ""
                    loadLoggedMeals()
                }
            }
        }
    }

    fun requestRecipeWithImage(bitmap: Bitmap, slot: String) {
        viewModelScope.launch {
            repository.generateAndLogMealWithImage(bitmap, slot).collectLatest { resource ->
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
