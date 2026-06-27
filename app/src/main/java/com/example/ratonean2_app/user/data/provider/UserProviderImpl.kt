package com.example.ratonean2_app.user.data.provider

import com.example.ratonean2_app.auth.data.dto.UpdateUserRequest
import com.example.ratonean2_app.core.network.ApiUrls
import com.example.ratonean2_app.core.network.NetworkResponse
import com.example.ratonean2_app.user.domain.model.User
import com.example.ratonean2_app.user.domain.provider.UserProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserProviderImpl(private val client: HttpClient) : UserProvider {
    override fun getUserById(id: Int): Flow<NetworkResponse<User>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.get(ApiUrls.USER_BY_ID.replace("{id}", id.toString()))
            if (response.status.isSuccess()) {
                val user = response.body<User>()
                emit(NetworkResponse.Success(user))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun getUserByEmail(email: String): Flow<NetworkResponse<User>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.get(ApiUrls.USER_BY_EMAIL.replace("{email}", email))
            if (response.status.isSuccess()) {
                val user = response.body<User>()
                emit(NetworkResponse.Success(user))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun deleteUser(id: Int): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.delete(ApiUrls.USER_BY_ID.replace("{id}", id.toString()))
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

    override fun updateUser(id: Int, userUpdate: UpdateUserRequest): Flow<NetworkResponse<Unit>> = flow {
        try {
            emit(NetworkResponse.Loading())
            val response = client.put(ApiUrls.USER_BY_ID.replace("{id}", id.toString())) {
                contentType(ContentType.Application.Json)
                setBody(userUpdate)
            }
            if (response.status.isSuccess()) {
                emit(NetworkResponse.Success(Unit))
            } else {
                emit(NetworkResponse.Failure(response.status.description))
            }
        } catch (e: Exception) {
            emit(NetworkResponse.Failure(e.message ?: "Unknown error"))
        }
    }

}