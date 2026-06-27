package com.example.ratonean2_app.user.domain.usecase

import com.example.ratonean2_app.user.domain.provider.UserProvider

class GetUserByEmailUseCase(private val userProvider: UserProvider) {
    operator fun invoke(email: String) = userProvider.getUserByEmail(email)
}