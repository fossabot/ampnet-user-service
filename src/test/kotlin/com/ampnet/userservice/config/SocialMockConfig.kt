package com.ampnet.userservice.config

import com.ampnet.userservice.service.SocialService
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("SocialMockConfig")
@Configuration
class SocialMockConfig {

    @Bean
    @Primary
    fun getSocialService(): SocialService {
        return Mockito.mock(SocialService::class.java)
    }
}
