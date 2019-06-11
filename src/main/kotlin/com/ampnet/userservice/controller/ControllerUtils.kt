package com.ampnet.userservice.controller

import com.ampnet.userservice.config.auth.UserPrincipal
import com.ampnet.userservice.exception.TokenException
import org.springframework.security.core.context.SecurityContextHolder

internal object ControllerUtils {

    fun getUserPrincipalFromSecurityContext(): UserPrincipal =
        SecurityContextHolder.getContext().authentication.principal as? UserPrincipal
            ?: throw TokenException("SecurityContext authentication principal must be UserPrincipal")
}
