package com.ampnet.userservice.controller

import com.ampnet.userservice.controller.pojo.request.MailCheckRequest
import com.ampnet.userservice.controller.pojo.response.MailCheckResponse
import com.ampnet.userservice.exception.InvalidRequestException
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid
import javax.validation.Validator

@RestController
class RegistrationController(
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val validator: Validator
) {
    companion object : KLogging()

    // TODO: define signup flow
//    @PostMapping("/signup")
//    fun createUser(@RequestBody request: SignupRequest): ResponseEntity<UserResponse> {
//        logger.debug { "Received request to sign up with method: ${request.signupMethod}" }
//        val createUserRequest = createUserRequest(request)
//        validateRequestOrThrow(createUserRequest)
//        val user = userService.create(createUserRequest)
//        return ResponseEntity.ok(UserResponse(user))
//    }

    @GetMapping("/mail-confirmation")
    fun mailConfirmation(@RequestParam("token") token: String): ResponseEntity<Void> {
        logger.debug { "Received to confirm mail with token: $token" }
        try {
            val tokenUuid = UUID.fromString(token)
            userService.confirmEmail(tokenUuid)?.let {
                logger.info { "Confirmed email for user: ${it.email}" }
                return ResponseEntity.ok().build()
            }
            logger.info { "User trying to confirm mail with non existing token: $tokenUuid" }
            return ResponseEntity.notFound().build()
        } catch (ex: IllegalArgumentException) {
            logger.warn { "User send token which is not UUID: $token" }
            throw InvalidRequestException(ErrorCode.REG_EMAIL_INVALID_TOKEN, "Token: $token is not in a valid format.")
        }
    }

    @GetMapping("/mail-confirmation/resend")
    fun resendMailConfirmation(): ResponseEntity<Any> {
        val userPrincipal = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.debug { "User ${userPrincipal.email} requested to resend mail confirmation link" }
        userService.find(userPrincipal.email)?.let {
            userService.resendConfirmationMail(it)
            return ResponseEntity.ok().build()
        }
        logger.warn { "User ${userPrincipal.email} missing in database, trying to resend mail confirmation" }
        return ResponseEntity.notFound().build()
    }

    @PostMapping("/mail-check")
    fun checkIfMailExists(@RequestBody @Valid request: MailCheckRequest): ResponseEntity<MailCheckResponse> {
        logger.debug { "Received request to check if email exists: $request" }
        val emailUsed = userService.find(request.email) != null
        return ResponseEntity.ok(MailCheckResponse(request.email, emailUsed))
    }
}
