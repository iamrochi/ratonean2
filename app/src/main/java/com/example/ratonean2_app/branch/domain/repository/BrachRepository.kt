package com.example.ratonean2_app.branch.domain.repository

import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE

import com.example.ratonean2_app.branch.domain.model.Branch
import kotlinx.coroutines.flow.Flow

interface BranchRepository {

    fun getBranchById(id: String): Flow<Branch?>
    fun getNearbyBranches(
        lat: Double,
        lon: Double,
        distance: Double,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<Branch>>
}
