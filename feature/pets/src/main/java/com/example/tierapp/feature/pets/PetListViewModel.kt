package com.example.tierapp.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tierapp.core.model.PetPhotoRepository
import com.example.tierapp.core.model.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PetListViewModel @Inject constructor(
    petRepository: PetRepository,
    private val petPhotoRepository: PetPhotoRepository,
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<PetListUiState> = _retryTrigger
        .flatMapLatest {
            petRepository.getAll()
                .flatMapLatest { pets ->
                    if (pets.isEmpty()) return@flatMapLatest flowOf(PetListUiState.Empty)
                    val photoFlows = pets.map { pet ->
                        petPhotoRepository.getByPetId(pet.id).map { photos ->
                            val thumb = photos.firstOrNull { it.id == pet.profilePhotoId }
                            PetSummary(
                                id = pet.id,
                                name = pet.name,
                                species = pet.species,
                                breed = pet.breed,
                                thumbSmallPath = thumb?.thumbSmallPath,
                            )
                        }
                    }
                    combine(photoFlows) { PetListUiState.Success(it.toList()) }
                }
                .catch { e -> emit(PetListUiState.Error(e.message ?: "Fehler beim Laden")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PetListUiState.Loading,
        )

    fun retry() { _retryTrigger.value++ }
}
