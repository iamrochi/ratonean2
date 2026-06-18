package com.example.ratonean2_app.branch.domain.provider

import com.example.ratonean2_app.branch.domain.model.Branch
import com.example.ratonean2_app.core.network.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface BranchProvider {
    fun getAllBranches(limit: Int = 50, offset: Int = 0): Flow<NetworkResponse<List<Branch>>>
    fun getBranchById(id: String): Flow<NetworkResponse<Branch>>
    fun getNearbyBranches(
        latitude: Double,
        longitude: Double,
        distance: Double,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<NetworkResponse<List<Branch>>>
    fun createBranch(branch: Branch): Flow<NetworkResponse<Unit>>
    fun updateBranch(branch: Branch): Flow<NetworkResponse<Unit>>
    fun deleteBranch(id: String): Flow<NetworkResponse<Unit>>
}
