package com.example.tierapp.feature.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.FamilyRepository
import com.example.tierapp.core.model.TierResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FamilyUiState>(FamilyUiState.Loading)
    val uiState: StateFlow<FamilyUiState> = _uiState

    init {
        combine(
            familyRepository.observeCurrentFamily(),
            familyRepository.observeCurrentFamily().flatMapLatest { family ->
                if (family != null) {
                    familyRepository.observeMembers(family.id)
                } else {
                    flowOf(emptyList())
                }
            },
        ) { family, members ->
            if (family != null) {
                FamilyUiState.HasFamily(family = family, members = members)
            } else {
                FamilyUiState.NoFamily
            }
        }.onEach { newState ->
            // Fehler-Zustand nicht überschreiben, wenn er vom User gesetzt wurde
            if (_uiState.value !is FamilyUiState.Error) {
                _uiState.value = newState
            } else if (newState is FamilyUiState.HasFamily) {
                // Erfolgreicher Zustand löst Fehler ab
                _uiState.value = newState
            }
        }.launchIn(viewModelScope)
    }

    fun createFamily(name: String, owner: AuthUser) {
        viewModelScope.launch {
            when (val result = familyRepository.createFamily(name.trim(), owner)) {
                is TierResult.Success -> {
                    // State wird durch Flow-Observer automatisch aktualisiert
                }
                is TierResult.Error -> {
                    _uiState.value = FamilyUiState.Error(
                        result.message ?: "Fehler beim Erstellen der Familie"
                    )
                }
            }
        }
    }

    fun joinByInviteCode(inviteCode: String, user: AuthUser) {
        viewModelScope.launch {
            when (val result = familyRepository.joinByInviteCode(inviteCode.trim(), user)) {
                is TierResult.Success -> {
                    // State wird durch Flow-Observer automatisch aktualisiert
                }
                is TierResult.Error -> {
                    _uiState.value = FamilyUiState.Error(
                        result.message ?: "Ungültiger Einladungscode"
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { current ->
            if (current is FamilyUiState.Error) FamilyUiState.NoFamily else current
        }
    }
}
