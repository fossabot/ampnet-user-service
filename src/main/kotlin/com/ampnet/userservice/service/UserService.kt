package com.ampnet.userservice.service

import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.pojo.CreateUserServiceRequest
import java.util.UUID

interface UserService {
    fun createUser(request: CreateUserServiceRequest): User
    fun find(email: String): User?
    fun find(userUuid: UUID): User?
    fun confirmEmail(token: UUID): User?
    fun resendConfirmationMail(user: User)
    fun changePassword(user: User, oldPassword: String, newPassword: String): User
    fun changePasswordWithToken(token: UUID, newPassword: String): User
    fun generateForgotPasswordToken(email: String): Boolean
}
