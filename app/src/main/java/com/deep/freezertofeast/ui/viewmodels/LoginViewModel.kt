package com.deep.freezertofeast.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
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

    fun signInWithGoogle(context: Context) {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                // 1. Initialize CredentialManager
                val credentialManager = CredentialManager.create(context)

                // 2. Configure Google ID Option using Web Client ID from google-services.json
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("663741025631-6c9iukuinlp30176a9kuofhc840s96d9.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                // 3. Build GetCredentialRequest
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 4. Retrieve Credential
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                // 5. Parse and authenticate ID Token with Firebase
                val credential = result.credential
                if (credential is CustomCredential && 
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Firebase.auth.signInWithCredential(firebaseCredential).await()
                    _loginSuccess.value = true
                } else {
                    throw IllegalStateException("Received unexpected credential type: ${credential.type}")
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: ""
                if (message.contains("restricted", ignoreCase = true) || 
                    message.contains("admin", ignoreCase = true) || 
                    message.contains("sign-up is disabled", ignoreCase = true)) {
                    _errorMessage.value = "Firebase Sign-Up is disabled.\n\nTo fix this:\n1. Open your Firebase Console.\n2. Navigate to Authentication -> Settings -> User actions.\n3. Toggle ON \"Enable create (sign-up)\".\n4. Under Sign-in method, toggle ON \"Google\" provider."
                } else if (message.contains("16", ignoreCase = true) || message.contains("cancelled", ignoreCase = true)) {
                    _errorMessage.value = "Sign-in cancelled or failed.\n\nDeveloper Note: Ensure your local debug SHA-1 signature (96:5F:B1:80:64:73:07:8C:01:28:29:9B:A9:BF:B7:02:5F:25:DD:81) is added to your Firebase Console under Project Settings -> General -> Your apps."
                } else {
                    _errorMessage.value = "Authentication error: ${e.localizedMessage}"
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetSuccess() {
        _loginSuccess.value = false
    }
}
