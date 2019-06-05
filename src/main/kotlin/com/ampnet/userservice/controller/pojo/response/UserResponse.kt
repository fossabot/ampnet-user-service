package com.ampnet.userservice.controller.pojo.response

import com.ampnet.userservice.persistence.model.User

data class UserResponse(
    val id: Int,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val role: String
) {

    constructor(user: User) : this(
            user.id,
            user.email,
            user.userInfo.firstName,
            user.userInfo.lastName,
            user.role.name
    )
}

data class UsersListResponse(val users: List<UserResponse>)
