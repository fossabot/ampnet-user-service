package com.ampnet.userservice.service.impl

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.exception.TokenException
import com.ampnet.userservice.persistence.model.RefreshToken
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.repository.RefreshTokenRepository
import com.ampnet.userservice.service.RefreshTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.UUID

@Service
class TokenServiceImpl(
    private val applicationProperties: ApplicationProperties,
    private val refreshTokenRepository: RefreshTokenRepository
) : RefreshTokenService {

    @Transactional
    override fun generateRefreshToken(user: User): RefreshToken {
        val token = UUID.randomUUID()
        val refreshToken = RefreshToken(0, user, token, ZonedDateTime.now())
        return refreshTokenRepository.save(refreshToken)
    }

    @Throws(TokenException::class)
    @Transactional
    override fun getUserForToken(token: UUID): User {
        val refreshToken = ServiceUtils.wrapOptional(refreshTokenRepository.findByToken(token))
            ?: throw TokenException("Non existing refresh token")
        val expiration = refreshToken.createdAt.plusSeconds(applicationProperties.jwt.refreshTokenValidity.toLong())
        if (ZonedDateTime.now().isAfter(expiration)) {
            refreshTokenRepository.delete(refreshToken)
            throw TokenException("Refresh token expired")
        }
        return refreshToken.user
    }

    @Transactional
    override fun deleteRefreshToken(userUuid: UUID) {
        ServiceUtils.wrapOptional(refreshTokenRepository.findByUserUuid(userUuid))?.let {
            refreshTokenRepository.delete(it)
        }
    }
}
