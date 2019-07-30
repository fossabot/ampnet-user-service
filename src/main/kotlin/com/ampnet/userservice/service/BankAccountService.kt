package com.ampnet.userservice.service

import com.ampnet.userservice.persistence.model.BankAccount
import java.util.UUID

interface BankAccountService {
    fun findBankAccounts(userUuid: UUID): List<BankAccount>
    fun createBankAccount(userUuid: UUID, account: String): BankAccount
    fun deleteBankAccount(userUuid: UUID, id: Int)
}
