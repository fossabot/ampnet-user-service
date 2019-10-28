package com.ampnet.userservice.service.pojo

import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.validation.EmailConstraint
import com.ampnet.userservice.validation.PasswordConstraint
import javax.validation.constraints.NotNull

data class CreateUserWithUserInfo(

    @NotNull
    val webSessionUuid: String,

    @EmailConstraint
    @NotNull
    val email: String,

    @PasswordConstraint
    val password: String?,

    val authMethod: AuthMethod
)
