package com.ampnet.userservice.service.impl

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.controller.pojo.request.IdentyumPayloadRequest
import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.IdentyumException
import com.ampnet.userservice.exception.InternalException
import com.ampnet.userservice.service.IdentyumService
import com.ampnet.userservice.service.UserService
import com.ampnet.userservice.service.pojo.IdentyumUserModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class IdentyumServiceImpl(
    private val applicationProperties: ApplicationProperties,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val userService: UserService
) : IdentyumService {

    companion object : KLogging()

    private val fieldUsername = "username"
    private val fieldPassword = "password"

    override fun getToken(): String {
        val request = generateIdentyumRequest()
        try {
            val response = restTemplate.postForEntity<String>(applicationProperties.identyum.url, request)
            if (response.statusCode.is2xxSuccessful) {
                response.body?.let {
                    return it
                }
            }
            throw InternalException(ErrorCode.REG_IDENTYUM,
                    "Could not get Identyum token. Status code: ${response.statusCode.value()}. Body: ${response.body}")
        } catch (ex: RestClientException) {
            throw InternalException(ErrorCode.REG_IDENTYUM, "Could not reach Identyum", ex)
        }
    }

    override fun storeUser(request: IdentyumPayloadRequest) {
        try {
            val decryptedData = decrypt(request.payload, applicationProperties.identyum.key, request.reportUuid)
            val identyumUser: IdentyumUserModel = objectMapper.readValue(decryptedData)
            userService.createUserInfo(identyumUser)
        } catch (ex: IdentyumException) {
            logger.error("Could not store Identyum user", ex)
        }
    }

    fun decrypt(value: String, key: String, reportUuid: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            val keyMD5 = md.digest(key.toByteArray())
            val ivMD5 = md.digest(reportUuid.toByteArray())
            val iv = IvParameterSpec(ivMD5)
            val skeySpec = SecretKeySpec(keyMD5, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val decrypted = cipher.doFinal(Base64.getDecoder().decode(value))
            return String(decrypted)
        } catch (ex: Exception) {
            throw IdentyumException("Could not decode Identyum payload", ex)
        }
    }

    private fun generateIdentyumRequest(): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val map = LinkedMultiValueMap<String, String>()
        map[fieldUsername] = applicationProperties.identyum.username
        map[fieldPassword] = applicationProperties.identyum.password
        return HttpEntity(map, headers)
    }
}
