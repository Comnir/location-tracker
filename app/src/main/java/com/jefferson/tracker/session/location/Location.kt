package com.jefferson.tracker.session.location

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val sessionId: Long,
    val longitude: Double,
    val latitude: Double,
    val timestamp: Long
)