package com.ampnet.userservice.controller.pojo.response

import com.ampnet.userservice.persistence.model.BankAccount
import java.time.ZonedDateTime

data class BankAccountResponse(
    val id: Int,
    val account: String,
    val format: String,
    val createdAt: ZonedDateTime
) {
    constructor(bankAccount: BankAccount) : this(
        bankAccount.id,
        bankAccount.account,
        bankAccount.format,
        bankAccount.createdAt
    )
}

data class BankAccountListResponse(val bankAccounts: List<BankAccountResponse>)
