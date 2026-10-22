package com.example.ratonean2_app.branch.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ratonean2_app.branch.domain.model.Branch
import kotlinx.serialization.Serializable

@Entity(
    tableName = "branches",
    indices = [Index("commerceId"), Index("branchId")]
)
@Serializable
data class BranchEntity(
    @PrimaryKey val branchKey: String,
    val branchId: String,
    val commerceId: String,
    val flagId: String,
    val name: String,
    val type: String,
    val street: String,
    val number: String,
    val latitude: Double,
    val longitude: Double,
    val observations: String?,
    val neighborhood: String,
    val postalCode: String,
    val location: String,
    val province: String,
    val mondaySchedule: String?,
    val tuesdaySchedule: String?,
    val wednesdaySchedule: String?,
    val thursdaySchedule: String?,
    val fridaySchedule: String?,
    val saturdaySchedule: String?,
    val sundaySchedule: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun BranchEntity.toDomain(): Branch =
    Branch(
        branchId = branchId,
        commerceId = commerceId,
        flagId = flagId,
        name = name,
        type = type,
        street = street,
        number = number,
        latitude = latitude,
        longitude = longitude,
        observations = observations,
        neighborhood = neighborhood,
        postalCode = postalCode,
        location = location,
        province = province,
        mondaySchedule = mondaySchedule,
        tuesdaySchedule = tuesdaySchedule,
        wednesdaySchedule = wednesdaySchedule,
        thursdaySchedule = thursdaySchedule,
        fridaySchedule = fridaySchedule,
        saturdaySchedule = saturdaySchedule,
        sundaySchedule = sundaySchedule
    )

fun Branch.toEntity(): BranchEntity =
    BranchEntity(
        branchKey = "$commerceId|$flagId|$branchId",
        branchId = branchId,
        commerceId = commerceId,
        flagId = flagId,
        name = name,
        type = type,
        street = street,
        number = number,
        latitude = latitude,
        longitude = longitude,
        observations = observations,
        neighborhood = neighborhood,
        postalCode = postalCode,
        location = location,
        province = province,
        mondaySchedule = mondaySchedule,
        tuesdaySchedule = tuesdaySchedule,
        wednesdaySchedule = wednesdaySchedule,
        thursdaySchedule = thursdaySchedule,
        fridaySchedule = fridaySchedule,
        saturdaySchedule = saturdaySchedule,
        sundaySchedule = sundaySchedule,
        lastUpdated = System.currentTimeMillis()
    )
