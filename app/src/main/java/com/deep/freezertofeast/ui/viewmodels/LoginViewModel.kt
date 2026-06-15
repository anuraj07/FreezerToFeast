package com.deep.freezertofeast.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun signInWithGoogle() {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // Simulate/Mock Google Sign-In using Firebase Anonymous Auth
                // to authenticate the user and obtain a valid UID for Firestore
                Firebase.auth.signInAnonymously().await()
                _loginSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Google Sign-In failed."
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetSuccess() {
        _loginSuccess.value = false
    }
}
