package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_posts")
data class QueuePostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalUrl: String,
    val summary: String,
    val imageUrl: String? = null,
    val platformsCsv: String, // e.g. "PINTEREST,FACEBOOK,TWITTER,QUORA"
    val scheduledTimeEpochMillis: Long,
    val targetUsPeakLabel: String,
    val appendUtm: Boolean = true,
    val status: String = "QUEUED", // QUEUED, POSTED, CANCELLED, FAILED
    val createdEpochMillis: Long = System.currentTimeMillis()
)
