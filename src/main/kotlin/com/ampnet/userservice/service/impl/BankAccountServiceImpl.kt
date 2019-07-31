package com.ampnet.userservice.service.impl

import com.ampnet.userservice.controller.pojo.request.BankAccountRequest
import com.ampnet.userservice.exception.BankAccountException
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.persistence.model.BankAccount
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.repository.BankAccountRepository
import com.ampnet.userservice.persistence.repository.UserRepository
import com.ampnet.userservice.service.BankAccountService
import mu.KLogging
import org.iban4j.BicFormatException
import org.iban4j.BicUtil
import org.iban4j.Iban4jException
import org.iban4j.IbanUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.UUID

@Service
class BankAccountServiceImpl(
    private val bankAccountRepository: BankAccountRepository,
    private val userRepository: UserRepository
) : BankAccountService {

    companion object : KLogging()

    @Transactional(readOnly = true)
    override fun findBankAccounts(userUuid: UUID): List<BankAccount> {
        return bankAccountRepository.findByUserUuid(userUuid)
    }

    @Throws(BankAccountException::class)
    @Transactional
    override fun createBankAccount(userUuid: UUID, request: BankAccountRequest): BankAccount {
        val user = getUser(userUuid)
        validateBankCode(request.bankCode)
        validateIban(request.iban)
        val bankAccount = BankAccount(0, user, request.iban, request.bankCode, ZonedDateTime.now())
        return bankAccountRepository.save(bankAccount)
    }

    @Transactional
    override fun deleteBankAccount(userUuid: UUID, id: Int) {
        bankAccountRepository.deleteByUserUuidAndId(userUuid, id)
    }

    private fun validateBankCode(bankCode: String) {
        try {
            BicUtil.validate(bankCode)
        } catch (ex: BicFormatException) {
            logger.info { "Invalid bank code: $bankCode. ${ex.message}" }
            throw BankAccountException("Invalid bank code")
        }
    }

    private fun validateIban(iban: String) {
        try {
            IbanUtil.validate(iban)
        } catch (ex: Iban4jException) {
            logger.info { "Invalid IBAN: $iban. ${ex.message}" }
            throw BankAccountException("Invalid IBAN")
        }
    }

    private fun getUser(userUuid: UUID): User =
        userRepository.findById(userUuid).orElseThrow {
            ResourceNotFoundException(ErrorCode.USER_MISSING, "Missing user with uuid: $userUuid")
        }
}
