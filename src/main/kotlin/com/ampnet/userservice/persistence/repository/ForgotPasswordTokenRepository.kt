package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.ForgotPasswordToken
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ForgotPasswordTokenRepository : JpaRepository<ForgotPasswordToken, Int> {
    fun findByToken(token: UUID): Optional<ForgotPasswordToken>
}
