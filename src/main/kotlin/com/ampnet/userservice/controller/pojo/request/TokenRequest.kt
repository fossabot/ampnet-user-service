package com.ampnet.userservice.controller.pojo.request

import com.ampnet.userservice.enums.AuthMethod

data class TokenRequest(
    val loginMethod: AuthMethod,
    val credentials: Map<String, String>
)
