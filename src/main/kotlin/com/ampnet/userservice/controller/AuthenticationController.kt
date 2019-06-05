package com.ampnet.userservice.controller

import com.ampnet.userservice.config.auth.TokenProvider
import com.ampnet.userservice.controller.pojo.request.TokenRequest
import com.ampnet.userservice.controller.pojo.request.TokenRequestSocialInfo
import com.ampnet.userservice.controller.pojo.request.TokenRequestUserInfo
import com.ampnet.userservice.controller.pojo.response.AuthTokenResponse
import com.ampnet.userservice.exception.InvalidLoginMethodException
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.service.SocialService
import com.ampnet.userservice.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(
    val authenticationManager: AuthenticationManager,
    val jwtTokenUtil: TokenProvider,
    val userService: UserService,
    val socialService: SocialService,
    val objectMapper: ObjectMapper
) {

    companion object : KLogging()

    @PostMapping("token")
    fun generateToken(@RequestBody tokenRequest: TokenRequest): ResponseEntity<AuthTokenResponse> {
        logger.debug { "Received request for token with: ${tokenRequest.loginMethod}" }
        val usernamePasswordAuthenticationToken = when (tokenRequest.loginMethod) {
            AuthMethod.EMAIL -> {
                val userInfo: TokenRequestUserInfo = objectMapper.convertValue(tokenRequest.credentials)
                validateLoginParamsOrThrowException(userInfo.email, AuthMethod.EMAIL)
                UsernamePasswordAuthenticationToken(userInfo.email, userInfo.password)
            }
            AuthMethod.FACEBOOK -> {
                val userInfo: TokenRequestSocialInfo = objectMapper.convertValue(tokenRequest.credentials)
                val email = socialService.getFacebookEmail(userInfo.token)
                validateLoginParamsOrThrowException(email, AuthMethod.FACEBOOK)
                UsernamePasswordAuthenticationToken(email, null)
            }
            AuthMethod.GOOGLE -> {
                val userInfo: TokenRequestSocialInfo = objectMapper.convertValue(tokenRequest.credentials)
                val email = socialService.getGoogleEmail(userInfo.token)
                validateLoginParamsOrThrowException(email, AuthMethod.GOOGLE)
                UsernamePasswordAuthenticationToken(email, null)
            }
        }
        val authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken)
        SecurityContextHolder.getContext().authentication = authentication
        val token = jwtTokenUtil.generateToken(authentication)

        logger.debug { "User successfully authenticated." }
        return ResponseEntity.ok(AuthTokenResponse(token))
    }

    private fun validateLoginParamsOrThrowException(email: String, loginMethod: AuthMethod) {
        val storedUser = userService.find(email)
                ?: throw ResourceNotFoundException(ErrorCode.USER_MISSING, "User with email: $email does not exists")
        val authMethod = storedUser.authMethod
        if (authMethod != loginMethod) {
            throw InvalidLoginMethodException("Invalid method. Try to login using ${authMethod.name}")
        }
    }
}
