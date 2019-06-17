package com.ampnet.userservice.controller

import com.ampnet.userservice.config.auth.TokenProvider
import com.ampnet.userservice.config.auth.UserPrincipal
import com.ampnet.userservice.controller.pojo.request.TokenRequest
import com.ampnet.userservice.controller.pojo.request.TokenRequestSocialInfo
import com.ampnet.userservice.controller.pojo.request.TokenRequestUserInfo
import com.ampnet.userservice.controller.pojo.response.AuthTokenResponse
import com.ampnet.userservice.exception.InvalidLoginMethodException
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.service.SocialService
import com.ampnet.userservice.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(
    val jwtTokenProvider: TokenProvider,
    val userService: UserService,
    val socialService: SocialService,
    val objectMapper: ObjectMapper,
    val passwordEncoder: PasswordEncoder
) {

    companion object : KLogging()

    @PostMapping("token")
    fun generateToken(@RequestBody tokenRequest: TokenRequest): ResponseEntity<AuthTokenResponse> {
        logger.debug { "Received request for token with: ${tokenRequest.loginMethod}" }
        val user: User = when (tokenRequest.loginMethod) {
            AuthMethod.EMAIL -> {
                val userInfo: TokenRequestUserInfo = objectMapper.convertValue(tokenRequest.credentials)
                val user = getUserByEmail(userInfo.email)
                validateEmailLogin(user, userInfo.password)
                user
            }
            AuthMethod.FACEBOOK -> {
                val userInfo: TokenRequestSocialInfo = objectMapper.convertValue(tokenRequest.credentials)
                val email = socialService.getFacebookEmail(userInfo.token)
                val user = getUserByEmail(email)
                validateSocialLogin(user, AuthMethod.FACEBOOK)
                user
            }
            AuthMethod.GOOGLE -> {
                val userInfo: TokenRequestSocialInfo = objectMapper.convertValue(tokenRequest.credentials)
                val email = socialService.getGoogleEmail(userInfo.token)
                val user = getUserByEmail(email)
                validateSocialLogin(user, AuthMethod.GOOGLE)
                user
            }
        }

        val token = jwtTokenProvider.generateToken(UserPrincipal(user))
        logger.debug { "User: ${user.uuid} successfully authenticated." }
        return ResponseEntity.ok(AuthTokenResponse(token))
    }

    private fun validateEmailLogin(user: User, providedPassword: String) {
        val storedPasswordHash = user.password
        if (!passwordEncoder.matches(providedPassword, storedPasswordHash)) {
            logger.debug { "User passwords do not match" }
            throw BadCredentialsException("Wrong password!")
        }
    }

    private fun validateSocialLogin(user: User, authMethod: AuthMethod) {
        if (user.authMethod != authMethod) {
            throw InvalidLoginMethodException("Invalid login method. User: ${user.uuid} try to login with: $authMethod")
        }
    }

    private fun getUserByEmail(email: String): User = userService.find(email)
            ?: throw ResourceNotFoundException(ErrorCode.USER_MISSING, "User with email: $email does not exists")
}
