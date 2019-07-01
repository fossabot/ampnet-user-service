package com.ampnet.userservice.config.auth

import com.ampnet.userservice.persistence.model.User
import java.util.UUID

data class UserPrincipal(
    val uuid: UUID,
    val email: String,
    val name: String,
    val authorities: Set<String>,
    val enabled: Boolean
) {
    constructor(user: User) : this(
        user.uuid,
        user.email,
        user.getFullName(),
        user.getAuthorities().asSequence().map { it.authority }.toSet(),
        user.enabled
    )
}
