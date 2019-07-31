package com.ampnet.userservice.service

import com.ampnet.userservice.controller.pojo.request.BankAccountRequest
import com.ampnet.userservice.persistence.model.BankAccount
import java.util.UUID

interface BankAccountService {
    fun findBankAccounts(userUuid: UUID): List<BankAccount>
    fun createBankAccount(userUuid: UUID, request: BankAccountRequest): BankAccount
    fun deleteBankAccount(userUuid: UUID, id: Int)
}
