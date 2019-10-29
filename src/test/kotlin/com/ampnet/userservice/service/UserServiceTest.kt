package com.ampnet.userservice.service

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.config.JsonConfig
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.impl.UserServiceImpl
import com.ampnet.userservice.service.pojo.CreateUserServiceRequest
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.context.annotation.Import

@Import(JsonConfig::class)
class UserServiceTest : JpaServiceTestBase() {

    private lateinit var testContext: TestContext

    @BeforeEach
    fun initTestContext() {
        testContext = TestContext()

        val properties = ApplicationProperties()
        properties.mail.confirmationNeeded = false
        testContext.applicationProperties = properties
    }

    @Test
    fun mustEnableNewAccountWithoutMailConfirmation() {
        suppose("Sending mail is disabled") {
            val properties = ApplicationProperties()
            properties.mail.confirmationNeeded = false
            testContext.applicationProperties = properties
        }
        suppose("User has no account") {
            databaseCleanerService.deleteAllUsers()
            databaseCleanerService.deleteAllMailTokens()
            testContext.email = "disabled@test.com"
        }
        suppose("User created new account") {
            val service = createUserService(testContext.applicationProperties)
            val request = CreateUserServiceRequest("first", "last", testContext.email,
                "password", AuthMethod.EMAIL)
            testContext.user = service.createUser(request)
        }

        verify("Created user account is connected and enabled") {
            assertThat(testContext.user.enabled).isTrue()
        }
        verify("Sending mail confirmation was not called") {
            Mockito.verify(mailService, Mockito.never()).sendConfirmationMail(Mockito.anyString(), Mockito.anyString())
        }
    }

    @Test
    fun mustDisableNewAccountWithMailConfirmation() {
        suppose("Sending mail is disabled") {
            val properties = ApplicationProperties()
            properties.mail.confirmationNeeded = true
            testContext.applicationProperties = properties
        }
        suppose("User has no account") {
            databaseCleanerService.deleteAllUsers()
            databaseCleanerService.deleteAllMailTokens()
            testContext.email = "enabled@test.com"
        }
        suppose("User created new account") {
            val service = createUserService(testContext.applicationProperties)
            val request = CreateUserServiceRequest("first", "last", testContext.email,
                "password", AuthMethod.EMAIL)
            testContext.user = service.createUser(request)
        }

        verify("Created user account is connected and disabled") {
            assertThat(testContext.user.enabled).isFalse()
        }
        verify("Sending mail confirmation was called") {
            val optionalMailToken = mailTokenRepository.findByUserUuid(testContext.user.uuid)
            assertThat(optionalMailToken).isPresent
            Mockito.verify(mailService, Mockito.times(1))
                    .sendConfirmationMail(testContext.user.email, optionalMailToken.get().token.toString())
        }
    }

    @Test
    fun mustThrowExceptionIfUserIsMissing() {
        verify("Service will throw exception that user is missing") {
            val service = createUserService(testContext.applicationProperties)
            val exception = assertThrows<ResourceNotFoundException> {
                service.connectUserInfo(UUID.randomUUID(), UUID.randomUUID().toString())
            }
            assertThat(exception.errorCode).isEqualTo(ErrorCode.USER_MISSING)
        }
    }

    @Test
    fun mustThrowExceptionIfUserInfoIsMissing() {
        suppose("User created account") {
            testContext.user = createUser("my@email.com")
        }
        verify("Service will throw exception that user is missing") {
            val service = createUserService(testContext.applicationProperties)
            val exception = assertThrows<ResourceNotFoundException> {
                service.connectUserInfo(testContext.user.uuid, UUID.randomUUID().toString())
            }
            assertThat(exception.errorCode).isEqualTo(ErrorCode.REG_IDENTYUM)
        }
    }

    private fun createUserService(properties: ApplicationProperties): UserService {
        return UserServiceImpl(userRepository, roleRepository, userInfoRepository, mailTokenRepository,
            mailService, passwordEncoder, properties)
    }

    private class TestContext {
        lateinit var applicationProperties: ApplicationProperties
        lateinit var email: String
        lateinit var user: User
    }
}
