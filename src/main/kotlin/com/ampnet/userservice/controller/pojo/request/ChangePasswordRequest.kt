package com.ampnet.userservice.controller.pojo.request

import java.util.UUID
import javax.validation.constraints.NotEmpty

data class ChangePasswordRequest(@NotEmpty val oldPassword: String, @NotEmpty val newPassword: String) {
    override fun toString(): String {
        return "cannot show password"
    }
}
data class ChangePasswordTokenRequest(@NotEmpty val newPassword: String, val token: UUID) {
    override fun toString(): String {
        return token.toString()
    }
}
