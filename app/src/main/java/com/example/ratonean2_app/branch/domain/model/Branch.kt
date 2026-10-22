package com.example.ratonean2_app.branch.domain.model
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.example.ratonean2_app.branch.data.entity.BranchEntity
import kotlinx.serialization.Serializable

@Serializable
data class Branch(
    val branchId: String,
    val commerceId: String,
    val flagId: String,
    val name: String,
    val type: String,
    val street: String,
    val number: String,
    val latitude: Double,
    val longitude: Double,
    val observations: String? = null,
    val neighborhood: String,
    val postalCode: String,
    val location: String,
    val province: String,
    val mondaySchedule: String? = null,
    val tuesdaySchedule: String? = null,
    val wednesdaySchedule: String? = null,
    val thursdaySchedule: String? = null,
    val fridaySchedule: String? = null,
    val saturdaySchedule: String? = null,
    val sundaySchedule: String? = null
)

/** Clave compuesta; id_sucursal no es único entre comercios. */
val Branch.branchKey: String
    get() = "$commerceId|$flagId|$branchId"

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
        sundaySchedule = sundaySchedule
    )
