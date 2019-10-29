package com.ampnet.userservice.service.pojo

import com.ampnet.userservice.controller.pojo.request.SignupRequest
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
    constructor(request: SignupRequest, email: String, password: String?) : this(
        request.firstName,
        request.lastName,
        email,
        password,
        request.signupMethod
    )
}
