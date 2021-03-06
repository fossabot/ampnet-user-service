package com.ampnet.userservice.service

import com.ampnet.core.jwt.JwtTokenUtils
import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.config.JsonConfig
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.persistence.model.Role
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.repository.RefreshTokenRepository
import com.ampnet.userservice.service.impl.TokenServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@Import(JsonConfig::class, ApplicationProperties::class)
class TokenServiceTest : JpaServiceTestBase() {

    @Autowired
    lateinit var applicationProperties: ApplicationProperties
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var testContext: TestContext
    private val service: TokenService by lazy {
        TokenServiceImpl(applicationProperties, refreshTokenRepository)
    }
    private val adminRole: Role by lazy {
        roleRepository.getOne(UserRoleType.ADMIN.id)
    }

    @BeforeEach
    fun initTestContext() {
        testContext = TestContext()
    }

    @Test
    fun userWithoutUserInfoMustNotBeVerified() {
        suppose("User is missing user info") {
            testContext.user = createUser("user@missing.com")
        }

        verify("User is not verified") {
            val accessAndRefreshToken = service.generateAccessAndRefreshForUser(testContext.user)
            verifyAccessTokenVerifiedFiled(accessAndRefreshToken.accessToken, false)
        }
    }

    @Test
    fun userWithUserInfoMustBeVerified() {
        suppose("Admin is missing user info") {
            testContext.user = createUser("admin@missing.com")
            testContext.user.userInfo = createUserInfo()
            userRepository.save(testContext.user)
        }

        verify("User is verified") {
            val accessAndRefreshToken = service.generateAccessAndRefreshForUser(testContext.user)
            verifyAccessTokenVerifiedFiled(accessAndRefreshToken.accessToken, true)
        }
    }

    @Test
    fun adminWithoutUserInfoMustBeVerified() {
        suppose("Admin is missing user info") {
            testContext.user = createUser("admin@missing.com")
            setAdminRole(testContext.user)
        }

        verify("Admin is verified") {
            val accessAndRefreshToken = service.generateAccessAndRefreshForUser(testContext.user)
            verifyAccessTokenVerifiedFiled(accessAndRefreshToken.accessToken, true)
        }
    }

    @Test
    fun adminWithUserInfoMustBeVerified() {
        suppose("Admin is missing user info") {
            testContext.user = createUser("admin@missing.com")
            setAdminRole(testContext.user)
        }

        verify("Admin is verified") {
            val accessAndRefreshToken = service.generateAccessAndRefreshForUser(testContext.user)
            verifyAccessTokenVerifiedFiled(accessAndRefreshToken.accessToken, true)
        }
    }

    @Test
    @Transactional
    fun mustDeleteRefreshTokenToCreateNewOne() {
        suppose("User has refresh token") {
            testContext.user = createUser("user@mail.com")
            service.generateAccessAndRefreshForUser(testContext.user)
        }
        suppose("User request new refresh token") {
            testContext.refreshToken = service.generateAccessAndRefreshForUser(testContext.user).refreshToken
        }

        verify("User has only new refresh token") {
            val refreshToken = refreshTokenRepository.findByUserUuid(testContext.user.uuid)
            assertThat(refreshToken).isNotNull
            assertThat(refreshToken.get().token).isEqualTo(testContext.refreshToken)
        }
    }

    private fun verifyAccessTokenVerifiedFiled(accessToken: String, verified: Boolean) {
        val signingKey = applicationProperties.jwt.signingKey
        val userPrincipal = JwtTokenUtils.decodeToken(accessToken, signingKey)
        assertThat(userPrincipal.verified).isEqualTo(verified)
    }

    private fun setAdminRole(user: User) {
        user.role = adminRole
        userRepository.save(user)
    }

    private class TestContext {
        lateinit var user: User
        lateinit var refreshToken: String
    }
}
