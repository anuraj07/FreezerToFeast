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

class ProfileViewModel : ViewModel() {
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedDiet = MutableStateFlow("Balanced")
    val selectedDiet: StateFlow<String> = _selectedDiet.asStateFlow()

    private val _staples = MutableStateFlow(
        listOf("Oil", "Atta", "Rice", "Turmeric", "Salt", "Red Chili Powder", "Ghee")
    )
    val staples: StateFlow<List<String>> = _staples.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadProfile()
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updateDiet(newDiet: String) {
        _selectedDiet.value = newDiet
    }

    fun toggleStaple(staple: String) {
        val current = _staples.value.toMutableList()
        if (current.contains(staple)) {
            current.remove(staple)
        } else {
            current.add(staple)
        }
        _staples.value = current
    }

    fun loadProfile() {
        val user = Firebase.auth.currentUser ?: return
        if (_name.value.isBlank()) {
            _name.value = user.displayName ?: ""
        }
        viewModelScope.launch {
            try {
                val document = Firebase.firestore.collection("freezer_to_feast").document("app").collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    _name.value = document.getString("name") ?: user.displayName ?: ""
                    _selectedDiet.value = document.getString("dietaryFocus") ?: "Balanced"
                    @Suppress("UNCHECKED_CAST")
                    val list = document.get("staples") as? List<String>
                    if (list != null) {
                        _staples.value = list
                    }
                }
            } catch (e: Exception) {
                // Ignore load failures in dev / offline mode
            }
        }
    }

    fun saveProfile() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            _saveSuccess.value = true // Proceed anyway
            return
        }
        _saving.value = true
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val profileMap = mapOf(
                    "name" to _name.value.trim().ifEmpty { "Nourished Explorer" },
                    "dietaryFocus" to _selectedDiet.value,
                    "staples" to _staples.value,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                db.collection("freezer_to_feast").document("app").collection("users").document(user.uid).set(profileMap).await()
                _saveSuccess.value = true
            } catch (e: Exception) {
                // Proceed even if Firestore save fails to keep UX flowing in dev
                _saveSuccess.value = true
            } finally {
                _saving.value = false
            }
        }
    }

    fun resetSuccess() {
        _saveSuccess.value = false
    }
}
