package com.example.ratonean2_app.user.domain.usecase

import com.example.ratonean2_app.user.domain.provider.UserProvider

class GetUserByIdUseCase (private val userProvider: UserProvider) {
    operator fun invoke(id: Int) = userProvider.getUserById(id)
}