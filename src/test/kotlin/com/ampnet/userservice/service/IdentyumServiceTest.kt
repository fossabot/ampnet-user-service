package com.ampnet.userservice.service

import com.ampnet.userservice.config.ApplicationProperties
import com.ampnet.userservice.config.JsonConfig
import com.ampnet.userservice.config.RestTemplateConfig
import com.ampnet.userservice.controller.pojo.request.IdentyumPayloadRequest
import com.ampnet.userservice.service.impl.IdentyumServiceImpl
import com.ampnet.userservice.service.impl.UserServiceImpl
import com.ampnet.userservice.service.pojo.IdentyumUserModel
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate

@Disabled
@Import(JsonConfig::class, RestTemplateConfig::class, ApplicationProperties::class)
class IdentyumServiceTest : JpaServiceTestBase() {

    @Autowired
    lateinit var applicationProperties: ApplicationProperties
    @Autowired
    lateinit var restTemplate: RestTemplate

    private val identyumService: IdentyumServiceImpl by lazy {
        val userService = UserServiceImpl(userRepository, roleRepository, userInfoRepository,
            mailTokenRepository, mailService, passwordEncoder, applicationProperties)
        IdentyumServiceImpl(applicationProperties, restTemplate, objectMapper, userService)
    }

    @Test
    fun mustBeAbleToDecode() {
        val identyumPayloadRequest = loadIdentyumPayloadRequest()
        assertThat(identyumPayloadRequest.reportUuid).isEqualTo("8c99227d-5108-4b1d-bcd2-449826032f99")

        val decodedIdentyumUser = loadDecodedIdentyumPayload()
        assertThat(decodedIdentyumUser.identyumUuid).isEqualTo("ae1ee02d-8a2d-4c50-a6ca-8f0454e19f6d")

        val decryptedIdentyumUser = decryptPayload(identyumPayloadRequest)
        assertThat(decryptedIdentyumUser).isEqualTo(decodedIdentyumUser)
    }

    private fun decryptPayload(identyumPayloadRequest: IdentyumPayloadRequest): IdentyumUserModel {
        val decryptedData = identyumService.decrypt(
            identyumPayloadRequest.payload, "12345abcde", identyumPayloadRequest.reportUuid)
        return objectMapper.readValue(decryptedData)
    }

    private fun loadIdentyumPayloadRequest(): IdentyumPayloadRequest {
        val identyumResponse = getResourceAsText("/identyum/identyum-response.json")
        return objectMapper.readValue(identyumResponse)
    }

    private fun loadDecodedIdentyumPayload(): IdentyumUserModel {
        val decodedPayload = getResourceAsText("/identyum/payload.json")
        return objectMapper.readValue(decodedPayload)
    }

    private fun getResourceAsText(path: String) = object {}.javaClass.getResource(path).readText()
}