package com.ampnet.userservice.service.impl

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.enums.UserRoleType
import com.ampnet.userservice.exception.InvalidRequestException
import com.ampnet.userservice.enums.AuthMethod
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.IdentyumException
import com.ampnet.userservice.exception.ResourceAlreadyExistsException
import com.ampnet.userservice.exception.ResourceNotFoundException
import com.ampnet.userservice.persistence.model.MailToken
import com.ampnet.userservice.persistence.model.Role
import com.ampnet.userservice.persistence.model.User
import com.ampnet.userservice.persistence.model.UserInfo
import com.ampnet.userservice.persistence.repository.MailTokenRepository
import com.ampnet.userservice.persistence.repository.RoleRepository
import com.ampnet.userservice.persistence.repository.UserInfoRepository
import com.ampnet.userservice.persistence.repository.UserRepository
import com.ampnet.userservice.service.MailService
import com.ampnet.userservice.service.UserService
import com.ampnet.userservice.service.pojo.CreateUserServiceRequest
import com.ampnet.userservice.service.pojo.IdentyumUserModel
import mu.KLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userInfoRepository: UserInfoRepository,
    private val mailTokenRepository: MailTokenRepository,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder,
    private val applicationProperties: ApplicationProperties
) : UserService {

    companion object : KLogging()

    private val userRole: Role by lazy {
        roleRepository.getOne(UserRoleType.USER.id)
    }

    private val adminRole: Role by lazy {
        roleRepository.getOne(UserRoleType.ADMIN.id)
    }

    @Transactional
    override fun createUserInfo(identyumUser: IdentyumUserModel): UserInfo {
        val userInfo = createUserInfoFromIdentyumUser(identyumUser)
        return userInfoRepository.save(userInfo)
    }

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
        return user
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<User> {
        return userRepository.findAll()
    }

    @Transactional(readOnly = true)
    override fun find(username: String): User? {
        return ServiceUtils.wrapOptional(userRepository.findByEmail(username))
    }

    @Transactional(readOnly = true)
    override fun find(id: Int): User? {
        return ServiceUtils.wrapOptional(userRepository.findById(id))
    }

    @Transactional
    override fun delete(id: Int) {
        userRepository.deleteById(id)
    }

    @Transactional
    override fun confirmEmail(token: UUID): User? {
        val optionalMailToken = mailTokenRepository.findByToken(token)
        if (!optionalMailToken.isPresent) {
            return null
        }
        val mailToken = optionalMailToken.get()
        if (mailToken.isExpired()) {
            throw InvalidRequestException(ErrorCode.REG_EMAIL_EXPIRED_TOKEN,
                    "User is trying to confirm mail with expired token: $token")
        }
        val user = mailToken.user
        user.enabled = true

        mailTokenRepository.delete(mailToken)
        return userRepository.save(user)
    }

    @Transactional
    override fun resendConfirmationMail(user: User) {
        if (user.authMethod != AuthMethod.EMAIL) {
            return
        }

        mailTokenRepository.findByUserId(user.id).ifPresent {
            mailTokenRepository.delete(it)
        }
        val mailToken = createMailToken(user)
        mailService.sendConfirmationMail(user.email, mailToken.token.toString())
    }

    @Transactional
    override fun changeUserRole(userId: Int, role: UserRoleType): User {
        val user = userRepository.findById(userId).orElseThrow {
            throw InvalidRequestException(ErrorCode.USER_MISSING, "Missing user with id: $userId")
        }

        user.role = when (role) {
            UserRoleType.ADMIN -> adminRole
            UserRoleType.USER -> userRole
        }
        return userRepository.save(user)
    }

    private fun createUserFromRequest(request: CreateUserServiceRequest): User {
        val userInfo = userInfoRepository.findByIdentyumNumber(request.identyumUuid).orElseThrow {
            throw ResourceNotFoundException(ErrorCode.REG_IDENTYUM,
                "Missing UserInfo with identyum number: ${request.identyumUuid}")
        }
        val user = User::class.java.getDeclaredConstructor().newInstance().apply {
            this.email = request.email
            this.authMethod = request.authMethod
            this.createdAt = ZonedDateTime.now()
            this.role = userRole
            this.userInfo = userInfo
            this.userInfo.connected = true
            this.uuid = getRandomUuid()
            this.enabled = true
        }
        if (request.authMethod == AuthMethod.EMAIL) {
            user.enabled = applicationProperties.mail.enabled.not()
            user.password = passwordEncoder.encode(request.password.orEmpty())
        }
        return user
    }

    @Suppress("ThrowsCount")
    private fun createUserInfoFromIdentyumUser(identyumUser: IdentyumUserModel): UserInfo {
        val userInfo = UserInfo::class.java.getDeclaredConstructor().newInstance()
        val document = identyumUser.document.firstOrNull() ?: throw IdentyumException("Missing document")
        userInfo.apply {
            phoneNumber = identyumUser.phones.firstOrNull()?.phoneNumber ?: throw IdentyumException("Missing phone")
            verifiedEmail = identyumUser.emails.firstOrNull()?.email ?: throw IdentyumException("Missing email")
            country = document.countryCode
            dateOfBirth = document.dateOfBirth
            identyumNumber = identyumUser.identyumUuid
            idType = document.type
            idNumber = document.docNumber
            personalId = document.personalIdentificationNumber.value
            createdAt = ZonedDateTime.now()
            connected = false
        }
        return userInfo
    }

    private fun createMailToken(user: User): MailToken {
        val mailToken = MailToken::class.java.getConstructor().newInstance().apply {
            this.user = user
            token = getRandomUuid()
            createdAt = ZonedDateTime.now()
        }
        return mailTokenRepository.save(mailToken)
    }

    private fun getRandomUuid(): UUID = UUID.randomUUID()
}
