package com.jefferson.tracker.persistance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) val sessionId: Long,
    val startTime: Long,
    val endTime: Long = -1,
    val title: String = "Unnamed"
)
