package com.ampnet.userservice.config.auth

import com.ampnet.userservice.persistence.model.User

data class UserPrincipal(
    val email: String,
    val authorities: Set<String>,
    val completeProfile: Boolean,
    val enabled: Boolean
) {
    constructor(user: User) : this(
        user.email,
        user.getAuthorities().asSequence().map { it.authority }.toSet(),
        true,
        user.enabled
    )
}
