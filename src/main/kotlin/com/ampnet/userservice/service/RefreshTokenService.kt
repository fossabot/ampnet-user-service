package com.ampnet.userservice.service

import com.ampnet.userservice.persistence.model.RefreshToken
import com.ampnet.userservice.persistence.model.User
import java.util.UUID

interface RefreshTokenService {
    fun generateRefreshToken(user: User): RefreshToken
    fun getUserForToken(token: UUID): User
    fun deleteRefreshToken(userUuid: UUID)
}
