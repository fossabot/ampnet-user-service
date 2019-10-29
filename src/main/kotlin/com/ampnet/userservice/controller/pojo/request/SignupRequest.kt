package com.ampnet.userservice.controller.pojo.request

import com.ampnet.userservice.enums.AuthMethod

// TODO: remove first and last name, move to userInfo for registration with email
data class SignupRequest(
    // val webSessionUuid: String,
    val firstName: String,
    val lastName: String,
    val signupMethod: AuthMethod,
    val userInfo: Map<String, String>
) {
    override fun toString(): String {
        // return "SignupRequest(webSessionUuid: $webSessionUuid, method: $signupMethod)"
        return "SignupRequest(firstName: $firstName, lastName: $lastName method: $signupMethod)"
    }
}
