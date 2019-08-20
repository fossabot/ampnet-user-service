package com.ampnet.userservice.controller.pojo.request

import javax.validation.constraints.NotEmpty

data class ChangePasswordRequest(@NotEmpty val oldPassword: String, @NotEmpty val newPassword: String)
