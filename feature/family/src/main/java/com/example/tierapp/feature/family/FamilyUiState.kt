package com.example.tierapp.feature.family

import com.example.tierapp.core.model.Family
import com.example.tierapp.core.model.FamilyMember

sealed interface FamilyUiState {
    data object Loading : FamilyUiState
    data object NoFamily : FamilyUiState
    data class HasFamily(
        val family: Family,
        val members: List<FamilyMember> = emptyList(),
        val isCopied: Boolean = false,
    ) : FamilyUiState
    data class Error(val message: String) : FamilyUiState
}
