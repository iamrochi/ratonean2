package com.example.ratonean2_app.branch.domain.usecase

import com.example.ratonean2_app.core.network.DEFAULT_PAGE_SIZE

import com.example.ratonean2_app.branch.domain.repository.BranchRepository

class GetNearbyBranchesUseCase(private val repository: BranchRepository) {
    operator fun invoke(lat: Double, lon: Double, dist: Double, limit: Int = DEFAULT_PAGE_SIZE, offset: Int = 0) =
        repository.getNearbyBranches(lat, lon, dist, limit, offset)
}
