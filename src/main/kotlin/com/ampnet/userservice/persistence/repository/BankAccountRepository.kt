package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.BankAccount
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface BankAccountRepository : JpaRepository<BankAccount, Int> {
    fun findByUserUuid(userUuid: UUID): List<BankAccount>
    fun deleteByUserUuidAndId(userUuid: UUID, id: Int)
}
