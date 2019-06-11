package com.ampnet.userservice.config.auth

import com.ampnet.userservice.persistence.model.User

data class UserPrincipal(
    val uuid: String,
    val email: String,
    val name: String,
    val authorities: Set<String>,
    val completeProfile: Boolean,
    val enabled: Boolean
) {
    constructor(user: User) : this(
        user.uuid.toString(),
        user.email,
        user.getFullName(),
        user.getAuthorities().asSequence().map { it.authority }.toSet(),
        true,
        user.enabled
    )
}
