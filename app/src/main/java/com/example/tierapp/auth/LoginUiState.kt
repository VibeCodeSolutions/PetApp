// app/src/main/java/com/example/tierapp/auth/LoginUiState.kt
package com.example.tierapp.auth

import com.example.tierapp.core.model.AuthUser

sealed interface LoginUiState {
    data object Unauthenticated : LoginUiState
    data object Loading : LoginUiState
    data class Authenticated(val user: AuthUser) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
