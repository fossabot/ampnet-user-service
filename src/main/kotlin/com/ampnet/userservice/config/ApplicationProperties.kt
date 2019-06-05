package com.ampnet.userservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.ampnet.userservice")
class ApplicationProperties {
    var jwt: JwtProperties = JwtProperties()
    val mail: MailProperties = MailProperties()
    val fileStorage: FileStorageProperties = FileStorageProperties()
    val identyum: IdentyumProperties = IdentyumProperties()
}

class JwtProperties {
    lateinit var signingKey: String
    var validityInMinutes: Int = 60
}

class MailProperties {
    lateinit var sender: String
    lateinit var confirmationBaseLink: String
    lateinit var organizationInvitationsLink: String
    var enabled: Boolean = false
}

class FileStorageProperties {
    lateinit var url: String
    lateinit var bucket: String
    lateinit var folder: String
}

class IdentyumProperties {
    lateinit var url: String
    lateinit var username: String
    lateinit var password: String
    lateinit var key: String
}
