package com.example.tierapp.core.model

enum class MemberRole {
    /** Besitzer der Familie; kann Mitglieder einladen und entfernen. */
    OWNER,
    /** Vollstaendige Lese- und Schreibrechte. */
    ADMIN,
    /** Lese- und eingeschraenkte Schreibrechte. */
    MEMBER,
    /** Nur Lesezugriff. */
    VIEWER,
}
