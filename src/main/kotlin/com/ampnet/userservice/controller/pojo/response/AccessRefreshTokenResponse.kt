package com.ampnet.userservice.controller.pojo.response

import com.ampnet.userservice.service.pojo.AccessAndRefreshToken
import java.util.UUID

data class AccessRefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: UUID,
    val refreshTokenExpiresIn: Long
) {
    constructor(accessAndRefreshToken: AccessAndRefreshToken) : this(
        accessAndRefreshToken.accessToken,
        accessAndRefreshToken.expiresIn,
        accessAndRefreshToken.refreshToken,
        accessAndRefreshToken.refreshTokenExpiresIn
    )
}
