package com.ampnet.userservice.service.pojo

import java.util.UUID

data class AccessAndRefreshToken(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: UUID,
    val refreshTokenExpiresIn: Long
)
