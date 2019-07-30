package com.ampnet.userservice.controller

import com.ampnet.userservice.controller.pojo.request.BankAccountRequest
import com.ampnet.userservice.controller.pojo.response.BankAccountListResponse
import com.ampnet.userservice.controller.pojo.response.BankAccountResponse
import com.ampnet.userservice.persistence.model.BankAccount
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.repository.BankAccountRepository
import com.ampnet.userservice.security.WithMockCrowdfoundUser
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZonedDateTime
import java.util.UUID

class BankAccountControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var bankAccountRepository: BankAccountRepository

    private val bankAccountPath = "/bank-account"
    private val user: User by lazy {
        databaseCleanerService.deleteAllUsers()
        createUser(defaultEmail, uuid = UUID.fromString("8a733721-9bb3-48b1-90b9-6463ac1493eb"))
    }
    private lateinit var testContext: TestContext

    @BeforeEach
    fun init() {
        user.uuid
        databaseCleanerService.deleteAllBankAccounts()
        testContext = TestContext()
    }

    @Test
    @WithMockCrowdfoundUser(uuid = "8a733721-9bb3-48b1-90b9-6463ac1493eb")
    fun mustBeAbleToGetBankAccounts() {
        suppose("User has multiple bank accounts") {
            val ibanAccount = createBankAccount(testContext.iban)
            val bicAccount = createBankAccount(testContext.bic, "BIC")
            testContext.bankAccounts = listOf(ibanAccount, bicAccount)
        }

        verify("User can get a list of bank accounts") {
            val result = mockMvc.perform(get(bankAccountPath))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()

            val bankAccounts: BankAccountListResponse = objectMapper.readValue(result.response.contentAsString)
            assertThat(bankAccounts.bankAccounts).hasSize(2)
            assertThat(bankAccounts.bankAccounts.map { it.account })
                .containsAll(testContext.bankAccounts.map { it.account })
        }
    }

    @Test
    @WithMockCrowdfoundUser(uuid = "8a733721-9bb3-48b1-90b9-6463ac1493eb")
    fun mustBeAbleToCreateIbanBankAccount() {
        verify("User can create IBAN bank account") {
            val request = BankAccountRequest(testContext.iban)
            val result = mockMvc.perform(
                post(bankAccountPath)
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()

            val bankAccount: BankAccountResponse = objectMapper.readValue(result.response.contentAsString)
            assertThat(bankAccount.account).isEqualTo(testContext.iban)
            assertThat(bankAccount.format).isEqualTo("IBAN")
            assertThat(bankAccount.createdAt).isBeforeOrEqualTo(ZonedDateTime.now())
            assertThat(bankAccount.id).isNotNull()
        }
        verify("Bank account is stored") {
            val accounts = bankAccountRepository.findByUserUuid(user.uuid)
            assertThat(accounts).hasSize(1)
            val bankAccount = accounts.first()
            assertThat(bankAccount.account).isEqualTo(testContext.iban)
            assertThat(bankAccount.format).isEqualTo("IBAN")
            assertThat(bankAccount.createdAt).isBeforeOrEqualTo(ZonedDateTime.now())
            assertThat(bankAccount.id).isNotNull()
        }
    }

    @Test
    @WithMockCrowdfoundUser(uuid = "8a733721-9bb3-48b1-90b9-6463ac1493eb")
    fun mustBeAbleToCreateBicBankAccount() {
        verify("User can create BIC bank account") {
            val request = BankAccountRequest(testContext.bic)
            val result = mockMvc.perform(
                post(bankAccountPath)
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()

            val bankAccount: BankAccountResponse = objectMapper.readValue(result.response.contentAsString)
            assertThat(bankAccount.account).isEqualTo(testContext.bic)
            assertThat(bankAccount.format).isEqualTo("BIC")
            assertThat(bankAccount.createdAt).isBeforeOrEqualTo(ZonedDateTime.now())
            assertThat(bankAccount.id).isNotNull()
        }
        verify("Bank account is stored") {
            val accounts = bankAccountRepository.findByUserUuid(user.uuid)
            assertThat(accounts).hasSize(1)
            val bankAccount = accounts.first()
            assertThat(bankAccount.account).isEqualTo(testContext.bic)
            assertThat(bankAccount.format).isEqualTo("BIC")
            assertThat(bankAccount.createdAt).isBeforeOrEqualTo(ZonedDateTime.now())
            assertThat(bankAccount.id).isNotNull()
        }
    }

    @Test
    @WithMockCrowdfoundUser(uuid = "8a733721-9bb3-48b1-90b9-6463ac1493eb")
    fun mustBeAbleToDeleteAccount() {
        suppose("User has a bank accounts") {
            val ibanAccount = createBankAccount(testContext.iban)
            testContext.bankAccounts = listOf(ibanAccount)
        }

        verify("User can delete the bank account") {
            mockMvc.perform(delete("$bankAccountPath/${testContext.bankAccounts.first().id}"))
                .andExpect(status().isOk)
        }
        verify("Bank account is deleted") {
            val accounts = bankAccountRepository.findByUserUuid(user.uuid)
            assertThat(accounts).hasSize(0)
        }
    }

    @Test
    @WithMockCrowdfoundUser(uuid = "8a733721-9bb3-48b1-90b9-6463ac1493eb")
    fun mustReturnBadRequestForInvalidBankAccount() {
        verify("User cannot create invalid bank account") {
            val request = BankAccountRequest("invalid-bank-account")
            mockMvc.perform(
                post(bankAccountPath)
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
        }
    }

    private fun createBankAccount(account: String, format: String = "IBAN"): BankAccount {
        val bankAccount = BankAccount(0, user, account, format, ZonedDateTime.now())
        return bankAccountRepository.save(bankAccount)
    }

    private class TestContext {
        lateinit var bankAccounts: List<BankAccount>
        val iban = "HR1723600001101234565"
        val bic = "DABAIE2D"
    }
}
