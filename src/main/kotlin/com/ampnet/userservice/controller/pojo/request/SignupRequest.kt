package com.ampnet.userservice.controller.pojo.request

import com.ampnet.userservice.enums.AuthMethod

data class SignupRequest(
    val identyumUuid: String,
    val signupMethod: AuthMethod,
    val userInfo: Map<String, String>
)
