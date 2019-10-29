package com.ampnet.userservice.service.pojo

import com.ampnet.userservice.controller.pojo.request.SignupRequestUserInfo
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.validation.EmailConstraint
import com.ampnet.userservice.validation.PasswordConstraint
import javax.validation.constraints.NotNull

data class CreateUserServiceRequest(

    // @NotNull
    // val webSessionUuid: String,

    @NotNull
    val firstName: String,

    @NotNull
    val lastName: String,

    @EmailConstraint
    @NotNull
    val email: String,

    @PasswordConstraint
    val password: String?,

    @NotNull
    val authMethod: AuthMethod

) {
    constructor(request: SignupRequestUserInfo) : this(
        request.firstName,
        request.lastName,
        request.email,
        request.password,
        AuthMethod.EMAIL
    )

    constructor(socialUser: SocialUser, authMethod: AuthMethod) : this(
        socialUser.firstName,
        socialUser.lastName,
        socialUser.email,
        null,
        authMethod
    )
}
