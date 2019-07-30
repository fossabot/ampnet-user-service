package com.ampnet.userservice.service.impl

import com.ampnet.userservice.exception.BankAccountException
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.persistence.model.BankAccount
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.repository.BankAccountRepository
import com.ampnet.userservice.persistence.repository.UserRepository
import com.ampnet.userservice.service.BankAccountService
import mu.KotlinLogging
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

    private companion object {
        private val logger = KotlinLogging.logger {}
        private const val IBAN_FORMAT = "IBAN"
        private const val BIC_FORMAT = "BIC"
    }

    @Transactional(readOnly = true)
    override fun findBankAccounts(userUuid: UUID): List<BankAccount> {
        return bankAccountRepository.findByUserUuid(userUuid)
    }

    @Throws(BankAccountException::class)
    @Transactional
    override fun createBankAccount(userUuid: UUID, account: String): BankAccount {
        val user = getUser(userUuid)
        if (isIbanFormat(account)) {
            return createBankAccount(user, account, IBAN_FORMAT)
        }
        if (isBicFormat(account)) {
            return createBankAccount(user, account, BIC_FORMAT)
        }
        throw BankAccountException("Bank account is not in IBAN nor SWIFT-BIC format")
    }

    @Transactional
    override fun deleteBankAccount(userUuid: UUID, id: Int) {
        bankAccountRepository.deleteByUserUuidAndId(userUuid, id)
    }

    private fun createBankAccount(user: User, account: String, format: String): BankAccount {
        val bankAccount = BankAccount(0, user, account, format, ZonedDateTime.now())
        return bankAccountRepository.save(bankAccount)
    }

    private fun isIbanFormat(account: String): Boolean {
        return try {
            IbanUtil.validate(account)
            true
        } catch (ex: Iban4jException) {
            logger.info { "Invalid IBAN: $account. ${ex.message}" }
            false
        }
    }

    private fun isBicFormat(account: String): Boolean {
        return try {
            BicUtil.validate(account)
            true
        } catch (ex: BicFormatException) {
            logger.info { "Invalid BIC: $account. ${ex.message}" }
            false
        }
    }

    private fun getUser(userUuid: UUID): User =
        userRepository.findById(userUuid).orElseThrow {
            ResourceNotFoundException(ErrorCode.USER_MISSING, "Missing user with uuid: $userUuid")
        }
}
