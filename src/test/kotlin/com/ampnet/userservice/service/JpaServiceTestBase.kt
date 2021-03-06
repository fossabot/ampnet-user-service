package com.ampnet.userservice.service

import com.ampnet.userservice.TestBase
import com.ampnet.userservice.config.DatabaseCleanerService
import com.ampnet.userservice.config.PasswordEncoderConfig
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.grpc.mailservice.MailService
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.model.UserInfo
import com.ampnet.userservice.persistence.repository.ForgotPasswordTokenRepository
import com.ampnet.userservice.persistence.repository.MailTokenRepository
import com.ampnet.userservice.persistence.repository.RoleRepository
import com.ampnet.userservice.persistence.repository.UserInfoRepository
import com.ampnet.userservice.persistence.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.ZonedDateTime
import java.util.UUID
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional(propagation = Propagation.SUPPORTS)
@Import(DatabaseCleanerService::class, PasswordEncoderConfig::class)
abstract class JpaServiceTestBase : TestBase() {

    @Autowired
    protected lateinit var databaseCleanerService: DatabaseCleanerService
    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    protected lateinit var roleRepository: RoleRepository
    @Autowired
    protected lateinit var userRepository: UserRepository
    @Autowired
    protected lateinit var mailTokenRepository: MailTokenRepository
    @Autowired
    protected lateinit var forgotPasswordTokenRepository: ForgotPasswordTokenRepository
    @Autowired
    protected lateinit var userInfoRepository: UserInfoRepository
    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected val mailService: MailService = Mockito.mock(MailService::class.java)

    protected fun createUser(
        email: String,
        firstName: String = "first",
        lastName: String = "last",
        password: String? = null,
        authMethod: AuthMethod = AuthMethod.EMAIL
    ): User {
        val user = User(
            UUID.randomUUID(),
            firstName,
            lastName,
            email,
            password,
            authMethod,
            null,
            roleRepository.getOne(UserRoleType.USER.id),
            ZonedDateTime.now(),
            true
        )
        return userRepository.save(user)
    }

    protected fun createUserInfo(
        webSessionUuid: String = UUID.randomUUID().toString(),
        first: String = "firstname",
        last: String = "lastname",
        email: String = "email@mail.com",
        disabled: Boolean = false
    ): UserInfo {
        val userInfo = UserInfo::class.java.getDeclaredConstructor().newInstance().apply {
            firstName = first
            lastName = last
            verifiedEmail = email
            phoneNumber = "+3859"
            country = "HRV"
            dateOfBirth = "2002-07-01"
            identyumNumber = UUID.randomUUID().toString()
            this.webSessionUuid = webSessionUuid
            documentType = "ID"
            documentNumber = "1242342"
            citizenship = "HRV"
            resident = true
            addressCity = "city"
            addressCounty = "county"
            addressStreet = "street"
            createdAt = ZonedDateTime.now()
            connected = false
            this.deactivated = disabled
        }
        return userInfoRepository.save(userInfo)
    }
}
