package com.example.ratonean2_app.user.domain.usecase

import com.example.ratonean2_app.auth.data.dto.UpdateUserRequest
import com.example.ratonean2_app.user.domain.provider.UserProvider

class UpdateUserUseCase (private val userProvider: UserProvider) {
    operator fun invoke(id: Int, userUpdate: UpdateUserRequest) = userProvider.updateUser(id, userUpdate)
}