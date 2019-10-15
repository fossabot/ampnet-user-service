package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.MailToken
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface MailTokenRepository : JpaRepository<MailToken, Int> {
    fun findByToken(token: UUID): Optional<MailToken>
    fun findByUserUuid(userUuid: UUID): Optional<MailToken>
}
