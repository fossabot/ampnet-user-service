package com.ampnet.userservice.service

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.config.JsonConfig
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.InvalidRequestException
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.impl.UserServiceImpl
import com.ampnet.userservice.service.pojo.IdentyumDocumentModel
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
            databaseCleanerService.deleteAllUsers()
            databaseCleanerService.deleteAllMailTokens()
            testContext.email = "disabled@test.com"
        }
        suppose("User created new account") {
            val service = createUserService(testContext.applicationProperties)
            val userInfo = createUserInfo()
            testContext.user = service.createUser(
                userInfo.identyumNumber, testContext.email, "password", AuthMethod.EMAIL)
        }

        verify("Created user account is connected and enabled") {
            assertThat(testContext.user.userInfo.connected).isTrue()
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
            properties.mail.enabled = true
            testContext.applicationProperties = properties
        }
        suppose("User has no account") {
            databaseCleanerService.deleteAllUsers()
            databaseCleanerService.deleteAllMailTokens()
            testContext.email = "enabled@test.com"
        }
        suppose("User created new account") {
            val service = createUserService(testContext.applicationProperties)
            val userInfo = createUserInfo()
            testContext.user = service.createUser(
                    userInfo.identyumNumber, testContext.email, "password", AuthMethod.EMAIL)
        }

        verify("Created user account is connected and disabled") {
            assertThat(testContext.user.userInfo.connected).isTrue()
            assertThat(testContext.user.enabled).isFalse()
        }
        verify("Sending mail confirmation was called") {
            val optionalMailToken = mailTokenRepository.findByUserId(testContext.user.id)
            assertThat(optionalMailToken).isPresent
            Mockito.verify(mailService, Mockito.times(1))
                    .sendConfirmationMail(testContext.user.email, optionalMailToken.get().token.toString())
        }
    }

    @Test
    fun mustBeAbleToChangeUserRoleToAdmin() {
        suppose("There is user with user role") {
            databaseCleanerService.deleteAllUsers()
            testContext.user = createUser("user@test.com", "Invited", "User")
            testContext.user.role = roleRepository.getOne(UserRoleType.USER.id)
        }

        verify("Service can change user role to admin role") {
            val service = createUserService(testContext.applicationProperties)
            service.changeUserRole(testContext.user.id, UserRoleType.ADMIN)
        }
        verify("User has admin role") {
            val userWithNewRole = userRepository.findById(testContext.user.id)
            assertThat(userWithNewRole).isPresent
            assertThat(userWithNewRole.get().role.id).isEqualTo(UserRoleType.ADMIN.id)
        }
    }

    @Test
    fun mustBeAbleToChangeUserRoleToUser() {
        suppose("There is user with user role") {
            databaseCleanerService.deleteAllUsers()
            testContext.user = createUser("user@test.com", "Invited", "User")
            testContext.user.role = roleRepository.getOne(UserRoleType.USER.id)
        }

        verify("Service can change user role to admin role") {
            val service = createUserService(testContext.applicationProperties)
            service.changeUserRole(testContext.user.id, UserRoleType.USER)
        }
        verify("User has admin role") {
            val userWithNewRole = userRepository.findById(testContext.user.id)
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
        suppose("There are no users") {
            databaseCleanerService.deleteAllUsers()
        }

        verify("Identyum request in proper format") {
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
            testContext.identyumUser = objectMapper.readValue(userIdentyumJson)
            testContext.identyumDocument = testContext.identyumUser.document.first()
        }
        verify("Service can create UserInfo from IdentyumUser") {
            val service = createUserService(testContext.applicationProperties)
            service.createUserInfo(testContext.identyumUser)
        }
        verify("UserInfo is created") {
            val optionalUserInfo = userInfoRepository.findByIdentyumNumber("1234-1234-1234-1234")
            assertThat(optionalUserInfo).isPresent
            val userInfo = optionalUserInfo.get()
            assertThat(userInfo.id).isNotNull()
            assertThat(userInfo.idType).isEqualTo(testContext.identyumDocument.type)
            assertThat(userInfo.idNumber).isEqualTo(testContext.identyumDocument.docNumber)
            assertThat(userInfo.country).isEqualTo(testContext.identyumDocument.countryCode)
            assertThat(userInfo.dateOfBirth).isEqualTo(testContext.identyumDocument.dateOfBirth)
            assertThat(userInfo.personalId).isEqualTo(testContext.identyumDocument.personalIdentificationNumber.value)
            assertThat(userInfo.identyumNumber).isEqualTo(testContext.identyumUser.identyumUuid)
            assertThat(userInfo.createdAt).isBeforeOrEqualTo(ZonedDateTime.now())
            assertThat(userInfo.connected).isFalse()
        }
    }

    private fun createUserService(properties: ApplicationProperties): UserService {
        return UserServiceImpl(userRepository, roleRepository, userInfoRepository, mailTokenRepository, mailService,
            passwordEncoder, properties)
    }

    private class TestContext {
        lateinit var applicationProperties: ApplicationProperties
        lateinit var email: String
        lateinit var user: User
        lateinit var identyumUser: IdentyumUserModel
        lateinit var identyumDocument: IdentyumDocumentModel
    }
}
