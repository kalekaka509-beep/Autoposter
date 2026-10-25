package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dispatch_logs")
data class DispatchLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postTitle: String,
    val targetPlatform: String,
    val destinationUrl: String,
    val usTimeFormatted: String,
    val executionMode: String, // "AUTOMATED_PEAK", "MANUAL_INSTANT", "WEBHOOK"
    val status: String, // "SUCCESS", "OPENED_INTENT", "FAILED"
    val message: String,
    val estimatedReach: Int, // Simulated audience reach based on US peak multiplier
    val timestampEpochMillis: Long = System.currentTimeMillis()
)
