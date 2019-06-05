package com.ampnet.userservice.controller

import com.ampnet.userservice.config.auth.UserPrincipal
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.exception.TokenException
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.UserService
import org.springframework.security.core.context.SecurityContextHolder

internal object ControllerUtils {

    fun getUserFromSecurityContext(userService: UserService): User {
        val userPrincipal = getUserPrincipalFromSecurityContext()
        return userService.find(userPrincipal.email)
                ?: throw ResourceNotFoundException(ErrorCode.USER_MISSING,
                        "Missing user with email: ${userPrincipal.email}")
    }

    fun getUserPrincipalFromSecurityContext(): UserPrincipal =
            SecurityContextHolder.getContext().authentication.principal as? UserPrincipal
                    ?: throw TokenException("SecurityContext authentication principal must be UserPrincipal")
}
