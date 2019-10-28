package com.ampnet.userservice.service

import com.ampnet.userservice.controller.pojo.request.CreateAdminUserRequest
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.pojo.UserCount
import java.util.UUID

interface AdminService {
    fun findAll(): List<User>
    fun findByEmail(email: String): List<User>
    fun findByRole(role: UserRoleType): List<User>
    fun createUser(request: CreateAdminUserRequest): User
    fun changeUserRole(userUuid: UUID, role: UserRoleType): User
    fun countUsers(): UserCount
}
