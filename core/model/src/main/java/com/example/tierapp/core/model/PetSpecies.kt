package com.example.tierapp.core.model

enum class PetSpecies {
    DOG,
    CAT,
    BIRD,
    RABBIT,
    GUINEA_PIG,
    HAMSTER,
    FISH,
    REPTILE,
    OTHER;

    fun toDisplayName(): String = when (this) {
        DOG        -> "Hund"
        CAT        -> "Katze"
        BIRD       -> "Vogel"
        RABBIT     -> "Kaninchen"
        GUINEA_PIG -> "Meerschweinchen"
        HAMSTER    -> "Hamster"
        FISH       -> "Fisch"
        REPTILE    -> "Reptil"
        OTHER      -> "Sonstiges"
    }
}
