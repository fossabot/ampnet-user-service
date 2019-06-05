package com.ampnet.userservice.security

import com.ampnet.userservice.enums.PrivilegeType
import com.ampnet.userservice.enums.UserRoleType
import org.springframework.security.test.context.support.WithSecurityContext

@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@WithSecurityContext(factory = WithMockUserSecurityFactory::class)
annotation class WithMockCrowdfoundUser(
    val email: String = "user@email.com",
    val role: UserRoleType = UserRoleType.USER,
    val privileges: Array<PrivilegeType> = [],
    val completeProfile: Boolean = true,
    val enabled: Boolean = true
)
