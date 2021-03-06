package com.ampnet.userservice.controller.pojo.response

import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.persistence.model.User

data class UserResponse(
    val uuid: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val enabled: Boolean,
    val verified: Boolean
) {

    constructor(user: User) : this(
        user.uuid.toString(),
        user.email,
        user.firstName,
        user.lastName,
        user.role.name,
        user.enabled,
        user.userInfo != null || user.role.id == UserRoleType.ADMIN.id
    )
}

data class UsersListResponse(val users: List<UserResponse>)
