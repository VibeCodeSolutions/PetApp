// app/src/main/java/com/example/tierapp/auth/LoginViewModel.kt
package com.example.tierapp.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.model.TierResult
import com.example.tierapp.core.network.auth.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Unauthenticated)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Reaktive Auth-Zustandsbeobachtung — überlebt Konfigurationsänderungen
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                // Loading-Zustand nicht überschreiben: aktiver Sign-In läuft noch
                if (_uiState.value !is LoginUiState.Loading) {
                    _uiState.value = if (user != null) {
                        LoginUiState.Authenticated(user)
                    } else {
                        LoginUiState.Unauthenticated
                    }
                }
            }
        }
    }

    /**
     * Startet Google Sign-In via Credential Manager (moderner Ersatz für GoogleSignInClient).
     * Context wird als Parameter übergeben und nicht gespeichert — kein Memory Leak.
     */
    fun initiateGoogleSignIn(context: Context, webClientId: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            runCatching {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()
                val request = GetCredentialRequest(listOf(googleIdOption))
                val result = credentialManager.getCredential(context, request)
                GoogleIdTokenCredential.createFrom(result.credential.data).idToken
            }.fold(
                onSuccess = { idToken -> signInWithGoogle(idToken) },
                onFailure = { e ->
                    _uiState.value = LoginUiState.Error(
                        e.message ?: "Google Sign-In fehlgeschlagen"
                    )
                },
            )
        }
    }

    /** Wird von Tests und intern nach Credential-Manager-Aufruf verwendet. */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is TierResult.Success -> _uiState.value = LoginUiState.Authenticated(result.data)
                is TierResult.Error -> _uiState.value = LoginUiState.Error(
                    result.message ?: "Google Sign-In fehlgeschlagen"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.signOut()) {
                is TierResult.Success -> { /* authState-Flow aktualisiert uiState reaktiv */ }
                is TierResult.Error -> _uiState.value = LoginUiState.Error(
                    result.message ?: "Abmelden fehlgeschlagen"
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Unauthenticated
        }
    }

    fun handleError(message: String) {
        _uiState.value = LoginUiState.Error(message)
    }
}
