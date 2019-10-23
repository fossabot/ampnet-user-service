package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.RefreshToken
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Int> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun findByUserUuid(userUuid: UUID): Optional<RefreshToken>
}
