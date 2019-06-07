package com.ampnet.userservice.service

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.config.JsonConfig
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.InvalidRequestException
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.impl.UserServiceImpl
import com.ampnet.userservice.service.pojo.IdentyumUserModel
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.context.annotation.Import
import java.time.ZonedDateTime

@Import(JsonConfig::class)
class UserServiceTest : JpaServiceTestBase() {

    private val admin: User by lazy {
        databaseCleanerService.deleteAllUsers()
        createUser("admin@test.com", "Admin", "User")
    }
    private val user: User by lazy {
        admin.id
        createUser("user@test.com", "Invited", "User")
    }

    private lateinit var testContext: TestContext

    @BeforeEach
    fun initTestContext() {
        testContext = TestContext()

        val properties = ApplicationProperties()
        properties.mail.enabled = false
        testContext.applicationProperties = properties
    }

    @Test
    fun mustEnableNewAccountWithoutMailConfirmation() {
        suppose("Sending mail is disabled") {
            val properties = ApplicationProperties()
            properties.mail.enabled = false
            testContext.applicationProperties = properties
        }
        suppose("User has no account") {
            testContext.email = "disabled@test.com"
            userRepository.findByEmail(testContext.email).ifPresent {
                databaseCleanerService.deleteAllMailTokens()
                userRepository.delete(it)
            }
        }
        // suppose("User created new account") {
        //     val service = createUserService(testContext.applicationProperties)
        //    testContext.mailUser = service.createUser(createUserServiceRequest())
        // }

        verify("Created user account is enabled") {
            assertThat(user.enabled).isTrue()
        }
        verify("Sending mail confirmation was not called") {
            Mockito.verify(mailService, Mockito.never()).sendConfirmationMail(Mockito.anyString(), Mockito.anyString())
        }
    }

    @Test
    fun mustDisableNewAccountWithMailConfirmation() {
        suppose("Sending mail is disabled") {
            val properties = ApplicationProperties()
            properties.mail.enabled = true
            testContext.applicationProperties = properties
        }
        suppose("User has no account") {
            testContext.email = "enabled@test.com"
            userRepository.findByEmail(testContext.email).ifPresent {
                databaseCleanerService.deleteAllMailTokens()
                userRepository.delete(it)
            }
        }
        suppose("User created new account") {
            val service = createUserService(testContext.applicationProperties)
            val userInfo = createUserInfo()
            testContext.mailUser = service.createUser(
                    userInfo.identyumNumber, testContext.email, "password", AuthMethod.EMAIL)
        }

        verify("Created user account is enabled") {
            assertThat(testContext.mailUser.enabled).isFalse()
        }
        verify("Sending mail confirmation was called") {
            val optionalMailToken = mailTokenRepository.findByUserId(testContext.mailUser.id)
            assertThat(optionalMailToken).isPresent
            Mockito.verify(mailService, Mockito.times(1))
                    .sendConfirmationMail(testContext.mailUser.email, optionalMailToken.get().token.toString())
        }
    }

    @Test
    fun mustBeAbleToChangeUserRoleToAdmin() {
        suppose("There is user with user role") {
            user.role = roleRepository.getOne(UserRoleType.USER.id)
        }

        verify("Service can change user role to admin role") {
            val service = createUserService(testContext.applicationProperties)
            service.changeUserRole(user.id, UserRoleType.ADMIN)
        }
        verify("User has admin role") {
            val userWithNewRole = userRepository.findById(user.id)
            assertThat(userWithNewRole).isPresent
            assertThat(userWithNewRole.get().role.id).isEqualTo(UserRoleType.ADMIN.id)
        }
    }

    @Test
    fun mustBeAbleToChangeUserRoleToUser() {
        suppose("There is user with user role") {
            user.role = roleRepository.getOne(UserRoleType.USER.id)
        }

        verify("Service can change user role to admin role") {
            val service = createUserService(testContext.applicationProperties)
            service.changeUserRole(user.id, UserRoleType.USER)
        }
        verify("User has admin role") {
            val userWithNewRole = userRepository.findById(user.id)
            assertThat(userWithNewRole).isPresent
            assertThat(userWithNewRole.get().role.id).isEqualTo(UserRoleType.USER.id)
        }
    }

    @Test
    fun mustThrowExceptionForChangeRoleOfNonExistingUser() {
        verify("Service will throw exception") {
            val service = createUserService(testContext.applicationProperties)
            val exception = assertThrows<InvalidRequestException> {
                service.changeUserRole(0, UserRoleType.ADMIN)
            }
            assertThat(exception.errorCode).isEqualTo(ErrorCode.USER_MISSING)
        }
    }

    @Test
    fun mustBeAbleToCreateUserFromIdentyumUser() {
        admin.id
        val userIdentyumJson = """
            {
                "identyumUuid": "1234-1234-1234-1234",
                "emails": [{
                    "type": "DEFAULT",
                    "email": "neki.mail@mail.com"
                }],
                "phones": [{
                    "type": "MOBILE",
                    "phoneNumber": "+385989999888"
                }],
                "document": [
                    {
                        "type": "PERSONAL_ID_CARD",
                        "countryCode": "HRV",
                        "firstName": "NETKO",
                        "lastName": "NEKO",
                        "docNumber": "112661111",
                        "citizenship": "HRV",
                        "address": {
                            "city": " GRAD ZAGREB",
                            "county": "ZAGREB",
                            "streetAndNumber": "ULICA NEGDJE 3"
                        },
                        "issuingAuthority": "PU ZAGREBAČKA",
                        "personalIdentificationNumber": {
                            "type": "OIB",
                            "value": "11111111111"
                        },
                        "resident": true,
                        "documentBilingual": false,
                        "permanent": false,
                        "docFrontImg": "base64 of image",
                        "docBackImg": "base64 of image",
                        "docFaceImg": "base64 of image",
                        "dateOfBirth": "1950-01-01",
                        "dateOfExpiry": "2021-01-11",
                        "dateOfIssue": "2016-01-11"
                    }
                ]
            }
        """.trimIndent()

        val identyumUser: IdentyumUserModel = objectMapper.readValue(userIdentyumJson)
        val document = identyumUser.document.first()
        val service = createUserService(testContext.applicationProperties)
        service.createUserInfo(identyumUser)

        val optionalUserInfo = userInfoRepository.findByIdentyumNumber("1234-1234-1234-1234")
        assertThat(optionalUserInfo).isPresent
        val userInfo = optionalUserInfo.get()
        assertThat(userInfo.id).isNotNull()
        assertThat(userInfo.idType).isEqualTo(document.type)
        assertThat(userInfo.idNumber).isEqualTo(document.docNumber)
        assertThat(userInfo.country).isEqualTo(document.countryCode)
        assertThat(userInfo.dateOfBirth).isEqualTo(document.dateOfBirth)
        assertThat(userInfo.personalId).isEqualTo(document.personalIdentificationNumber.value)
        assertThat(userInfo.identyumNumber).isEqualTo(identyumUser.identyumUuid)
        assertThat(userInfo.createdAt).isBeforeOrEqualTo(ZonedDateTime.now())
    }

    private fun createUserService(properties: ApplicationProperties): UserService {
        return UserServiceImpl(userRepository, roleRepository, userInfoRepository, mailTokenRepository, mailService,
            passwordEncoder, properties)
    }

    private class TestContext {
        lateinit var applicationProperties: ApplicationProperties
        lateinit var email: String
        lateinit var mailUser: User
    }
}
