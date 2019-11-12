package com.ampnet.userservice.controller.pojo.request

data class SignupRequestUserInfo(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
) {
    override fun toString(): String = "SignupRequestUserInfo(email: $email, firstName: $firstName, lastName: $lastName)"
}
