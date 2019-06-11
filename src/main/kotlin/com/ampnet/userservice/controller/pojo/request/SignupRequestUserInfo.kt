package com.ampnet.userservice.controller.pojo.request

data class SignupRequestUserInfo(
    val email: String,
    val password: String
) {
    override fun toString(): String = "email: $email"
}
