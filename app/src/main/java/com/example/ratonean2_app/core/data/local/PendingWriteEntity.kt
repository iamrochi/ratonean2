package com.example.ratonean2_app.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_writes")
data class PendingWriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val method: String,
    val path: String,
    val bodyJson: String?,
    val idempotencyKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
