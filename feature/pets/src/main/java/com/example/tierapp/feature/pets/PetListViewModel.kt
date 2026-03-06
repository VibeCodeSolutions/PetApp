package com.example.tierapp.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.model.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PetListViewModel @Inject constructor(
    petRepository: PetRepository,
) : ViewModel() {

    val uiState: StateFlow<PetListUiState> = petRepository.getAll()
        .map { pets ->
            if (pets.isEmpty()) PetListUiState.Empty
            else PetListUiState.Success(pets)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PetListUiState.Loading,
        )
}
