package com.ampnet.userservice.service.impl

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.InvalidRequestException
import com.ampnet.userservice.exception.ResourceAlreadyExistsException
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.persistence.model.ForgotPasswordToken
import com.ampnet.userservice.persistence.model.MailToken
import com.ampnet.userservice.persistence.model.Role
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.repository.ForgotPasswordTokenRepository
import com.ampnet.userservice.persistence.repository.MailTokenRepository
import com.ampnet.userservice.persistence.repository.RoleRepository
import com.ampnet.userservice.persistence.repository.UserInfoRepository
import com.ampnet.userservice.persistence.repository.UserRepository
import com.ampnet.userservice.grpc.mailservice.MailService
import com.ampnet.userservice.service.UserService
import com.ampnet.userservice.service.pojo.CreateUserServiceRequest
import java.time.ZonedDateTime
import java.util.UUID
import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userInfoRepository: UserInfoRepository,
    private val mailTokenRepository: MailTokenRepository,
    private val forgotPasswordTokenRepository: ForgotPasswordTokenRepository,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder,
    private val applicationProperties: ApplicationProperties
) : UserService {

    companion object : KLogging()

    private val userRole: Role by lazy { roleRepository.getOne(UserRoleType.USER.id) }

    @Transactional
    override fun createUser(request: CreateUserServiceRequest): User {
        if (userRepository.findByEmail(request.email).isPresent) {
            throw ResourceAlreadyExistsException(ErrorCode.REG_USER_EXISTS,
                "Trying to create user with email that already exists: ${request.email}")
        }

        val user = createUserFromRequest(request)
        userRepository.save(user)
        if (user.authMethod == AuthMethod.EMAIL && user.enabled.not()) {
            val mailToken = createMailToken(user)
            mailService.sendConfirmationMail(user.email, mailToken.token.toString())
        }
        logger.debug { "Created user: ${user.email}" }
        return user
    }

    @Transactional(readOnly = true)
    override fun find(email: String): User? {
        return ServiceUtils.wrapOptional(userRepository.findByEmail(email))
    }

    @Transactional(readOnly = true)
    override fun find(userUuid: UUID): User? {
        return ServiceUtils.wrapOptional(userRepository.findById(userUuid))
    }

    @Transactional
    override fun confirmEmail(token: UUID): User? {
        ServiceUtils.wrapOptional(mailTokenRepository.findByToken(token))?.let { mailToken ->
            if (mailToken.isExpired()) {
                throw InvalidRequestException(ErrorCode.REG_EMAIL_EXPIRED_TOKEN,
                    "User is trying to confirm mail with expired token: $token")
            }
            val user = mailToken.user
            user.enabled = true

            mailTokenRepository.delete(mailToken)
            logger.debug { "Email confirmed for user: ${user.email}" }
            return userRepository.save(user)
        }
        return null
    }

    @Transactional
    override fun resendConfirmationMail(user: User) {
        if (user.authMethod != AuthMethod.EMAIL) {
            return
        }

        mailTokenRepository.findByUserUuid(user.uuid).ifPresent {
            mailTokenRepository.delete(it)
        }
        val mailToken = createMailToken(user)
        mailService.sendConfirmationMail(user.email, mailToken.token.toString())
    }

    @Transactional
    override fun changePassword(user: User, oldPassword: String, newPassword: String): User {
        if (user.authMethod != AuthMethod.EMAIL) {
            throw InvalidRequestException(ErrorCode.AUTH_INVALID_LOGIN_METHOD, "Cannot change password")
        }
        if (passwordEncoder.matches(oldPassword, user.password).not()) {
            throw InvalidRequestException(ErrorCode.USER_DIFFERENT_PASSWORD, "Invalid old password")
        }
        logger.info { "Changing password for user: ${user.uuid}" }
        user.password = passwordEncoder.encode(newPassword)
        return userRepository.save(user)
    }

    @Transactional
    override fun changePasswordWithToken(token: UUID, newPassword: String): User {
        val forgotToken = forgotPasswordTokenRepository.findByToken(token).orElseThrow {
            throw ResourceNotFoundException(ErrorCode.AUTH_FORGOT_TOKEN_MISSING, "Missing forgot token: $token")
        }
        if (forgotToken.isExpired()) {
            throw InvalidRequestException(ErrorCode.AUTH_FORGOT_TOKEN_EXPIRED, "Expired token: $token")
        }
        val user = forgotToken.user
        forgotPasswordTokenRepository.delete(forgotToken)
        user.password = passwordEncoder.encode(newPassword)
        logger.info { "Changing password using forgot password token for user: ${user.email}" }
        return userRepository.save(user)
    }

    @Transactional
    override fun generateForgotPasswordToken(email: String): Boolean {
        val user = find(email) ?: return false
        if (user.authMethod != AuthMethod.EMAIL) {
            throw InvalidRequestException(ErrorCode.AUTH_INVALID_LOGIN_METHOD, "Cannot change password")
        }
        logger.info { "Generating forgot password token for user: ${user.email}" }
        val forgotPasswordToken = ForgotPasswordToken(0, user, UUID.randomUUID(), ZonedDateTime.now())
        forgotPasswordTokenRepository.save(forgotPasswordToken)
        mailService.sendResetPasswordMail(user.email, forgotPasswordToken.token.toString())
        return true
    }

    private fun createUserFromRequest(request: CreateUserServiceRequest): User {
        val userInfo = userInfoRepository.findByWebSessionUuid(request.webSessionUuid).orElseThrow {
            throw ResourceNotFoundException(ErrorCode.REG_IDENTYUM,
                "Missing UserInfo with Identyum webSessionUuid: ${request.webSessionUuid}")
        }
        val user = User(
            UUID.randomUUID(),
            userInfo.firstName,
            userInfo.lastName,
            request.email,
            null,
            request.authMethod,
            userInfo,
            userRole,
            ZonedDateTime.now(),
            true
        )
        if (request.authMethod == AuthMethod.EMAIL) {
            user.enabled = applicationProperties.mail.confirmationNeeded.not()
            user.password = passwordEncoder.encode(request.password.orEmpty())
        }
        userInfo.connected = true
        return user
    }

    private fun createMailToken(user: User): MailToken {
        val mailToken = MailToken(0, user, UUID.randomUUID(), ZonedDateTime.now())
        return mailTokenRepository.save(mailToken)
    }
}
