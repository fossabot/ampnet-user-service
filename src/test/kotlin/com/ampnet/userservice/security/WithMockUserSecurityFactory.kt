package com.ampnet.userservice.security

import com.ampnet.userservice.config.auth.UserPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockUserSecurityFactory : WithSecurityContextFactory<WithMockCrowdfoundUser> {

    private val password = "password"

    override fun createSecurityContext(annotation: WithMockCrowdfoundUser): SecurityContext {
        val authorities = mapPrivilegesOrRoleToAuthorities(annotation)
        val userPrincipal = UserPrincipal(
                annotation.email, authorities.asSequence().map { it.authority }.toSet(),
                annotation.completeProfile, annotation.enabled)
        val token = UsernamePasswordAuthenticationToken(
                userPrincipal,
                password,
                authorities
        )

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = token
        return context
    }

    private fun mapPrivilegesOrRoleToAuthorities(annotation: WithMockCrowdfoundUser): List<SimpleGrantedAuthority> {
        return if (annotation.privileges.isNotEmpty()) {
            annotation.privileges.map { SimpleGrantedAuthority(it.name) }
        } else {
            annotation.role.getPrivileges().map { SimpleGrantedAuthority(it.name) }
        }
    }
}
