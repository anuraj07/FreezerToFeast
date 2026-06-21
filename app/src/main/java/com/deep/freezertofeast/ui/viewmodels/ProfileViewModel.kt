package com.deep.freezertofeast.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfileViewModel : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedDiet = MutableStateFlow("Balanced")
    val selectedDiet: StateFlow<String> = _selectedDiet.asStateFlow()

    private val _staples = MutableStateFlow(
        listOf("Oil", "Atta", "Rice", "Turmeric", "Salt", "Red Chili Powder", "Ghee")
    )
    val staples: StateFlow<List<String>> = _staples.asStateFlow()

    private val _palatePreferences = MutableStateFlow<List<String>>(emptyList())
    val palatePreferences: StateFlow<List<String>> = _palatePreferences.asStateFlow()

    private val _activePalates = MutableStateFlow<List<String>>(emptyList())
    val activePalates: StateFlow<List<String>> = _activePalates.asStateFlow()

    private val _activeDays = MutableStateFlow(0)
    val activeDays: StateFlow<Int> = _activeDays.asStateFlow()

    private val _todayAiUsage = MutableStateFlow(0)
    val todayAiUsage: StateFlow<Int> = _todayAiUsage.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadProfile()
    }

    private fun saveProfileChanges() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val profileMap = hashMapOf<String, Any>(
                    "dietaryFocus" to _selectedDiet.value,
                    "staples" to _staples.value,
                    "palatePreferences" to _palatePreferences.value,
                    "activePalates" to _activePalates.value,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .set(profileMap, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                // Ignore save failures in dev
            }
        }
    }

    fun updateDiet(newDiet: String) {
        _selectedDiet.value = newDiet
        saveProfileChanges()
    }

    fun toggleStaple(staple: String) {
        val current = _staples.value.toMutableList()
        if (current.contains(staple)) {
            current.remove(staple)
        } else {
            current.add(staple)
        }
        _staples.value = current
        saveProfileChanges()
    }

    fun addPalatePreference(pref: String) {
        val trimmed = pref.trim()
        if (trimmed.isEmpty()) return
        val currentList = _palatePreferences.value.toMutableList()
        if (!currentList.contains(trimmed)) {
            currentList.add(trimmed)
            _palatePreferences.value = currentList
            
            // Auto check it by default
            val currentActive = _activePalates.value.toMutableList()
            if (!currentActive.contains(trimmed)) {
                currentActive.add(trimmed)
                _activePalates.value = currentActive
            }
            saveProfileChanges()
        }
    }

    fun removePalatePreference(pref: String) {
        val currentList = _palatePreferences.value.toMutableList()
        val currentActive = _activePalates.value.toMutableList()
        var changed = false
        if (currentList.remove(pref)) {
            _palatePreferences.value = currentList
            changed = true
        }
        if (currentActive.remove(pref)) {
            _activePalates.value = currentActive
            changed = true
        }
        if (changed) {
            saveProfileChanges()
        }
    }

    fun toggleActivePalate(pref: String) {
        val current = _activePalates.value.toMutableList()
        if (current.contains(pref)) {
            current.remove(pref)
        } else {
            current.add(pref)
        }
        _activePalates.value = current
        saveProfileChanges()
    }

    fun loadProfile() {
        val user = Firebase.auth.currentUser ?: return
        _name.value = user.displayName ?: "Nourished Explorer"
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                
                // 1. Load user settings
                val document = db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .await()
                    
                var customSlots = emptyList<String>()
                if (document.exists()) {
                    _name.value = document.getString("name") ?: user.displayName ?: "Nourished Explorer"
                    _selectedDiet.value = document.getString("dietaryFocus") ?: "Balanced"
                    
                    @Suppress("UNCHECKED_CAST")
                    val list = document.get("staples") as? List<String>
                    if (list != null) {
                        _staples.value = list
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    val prefs = document.get("palatePreferences") as? List<String>
                    if (prefs != null) {
                        _palatePreferences.value = prefs
                    }

                    @Suppress("UNCHECKED_CAST")
                    val active = document.get("activePalates") as? List<String>
                    if (active != null) {
                        _activePalates.value = active
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    customSlots = document.get("customSlots") as? List<String> ?: emptyList()
                }

                // 2. Query total logged days (active rituals)
                val dailyMealsQuery = db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("daily_meals")
                    .get()
                    .await()
                _activeDays.value = dailyMealsQuery.size()

                // 3. Query today's logged meals (AI Usage)
                val todayString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val todayDoc = db.collection("freezer_to_feast")
                    .document("app")
                    .collection("users")
                    .document(user.uid)
                    .collection("daily_meals")
                    .document(todayString)
                    .get()
                    .await()
                
                var usageCount = 0
                if (todayDoc.exists()) {
                    val standardSlots = listOf("breakfast", "lunch", "dinner", "snacks")
                    val customSlotIds = customSlots.map { it.trim().lowercase().replace(" ", "_") }
                    val allSlots = standardSlots + customSlotIds
                    for (slot in allSlots) {
                        val mealMap = todayDoc.get(slot) as? Map<*, *>
                        if (mealMap != null && mealMap["recipeName"] != null) {
                            usageCount++
                        }
                    }
                }
                _todayAiUsage.value = usageCount

            } catch (e: Exception) {
                // Ignore load failures in dev / offline mode
            }
        }
    }

    // Keep saveProfile as empty fallback to avoid breakages in call sites
    fun saveProfile() {
        _saveSuccess.value = true
    }

    fun resetSuccess() {
        _saveSuccess.value = false
    }
}
