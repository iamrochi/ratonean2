package com.example.ratonean2_app.user.domain.usecase

import com.example.ratonean2_app.user.domain.provider.UserProvider

class DeleteUserByIdUseCase(private val userProvider: UserProvider) {
    operator fun invoke(id: Int) = userProvider.deleteUser(id)
}