package com.example.ratonean2_app.branch.domain.repository

import com.example.ratonean2_app.branch.domain.model.Branch
import kotlinx.coroutines.flow.Flow

interface BranchRepository {

    fun getBranchById(id: String): Flow<Branch?>
    fun getNearbyBranches(
        lat: Double,
        lon: Double,
        distance: Double,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Branch>>
}
