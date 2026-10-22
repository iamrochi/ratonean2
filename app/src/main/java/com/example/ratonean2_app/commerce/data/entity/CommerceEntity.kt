package com.example.ratonean2_app.commerce.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ratonean2_app.commerce.domain.model.Commerce
import kotlinx.serialization.Serializable

@Entity(tableName = "commerce", indices = [Index("id")])
@Serializable
data class CommerceEntity(
    @PrimaryKey val commerceKey: String,
    val id: String,
    val flagId: String,
    val cuit: String,
    val businessName: String,
    val flagName: String,
    val flagUrl: String,
    val sepaVersion: String,
    val lastUpdate: String,
    // P0.3/M4: TTL local. No viene del backend, default 0 = stale.
    val cacheUpdated: Long = 0L
)
fun CommerceEntity.toDomain(): Commerce = Commerce(
    id = id,
    flagId = flagId,
    cuit = cuit,
    businessName = businessName,
    flagName = flagName,
    flagUrl = flagUrl,
    sepaVersion = sepaVersion,
    lastUpdate = lastUpdate
)

fun Commerce.toEntity(): CommerceEntity = CommerceEntity(
    commerceKey = "$id|$flagId",
    id = id,
    flagId = flagId,
    cuit = cuit,
    businessName = businessName,
    flagName = flagName,
    flagUrl = flagUrl,
    sepaVersion = sepaVersion,
    lastUpdate = lastUpdate,
    cacheUpdated = System.currentTimeMillis()
)