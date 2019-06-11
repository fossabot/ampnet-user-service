package com.ampnet.userservice.persistence.model

import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "user_info")
data class UserInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    var webSessionUuid: String,

    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = false)
    var lastName: String,

    @Column(nullable = false)
    var verifiedEmail: String,

    @Column(nullable = false, length = 32)
    var phoneNumber: String,

    @Column(nullable = false)
    var country: String,

    @Column(nullable = false, length = 10)
    var dateOfBirth: String,

    @Column(nullable = false)
    var identyumNumber: String,

    @Column(nullable = false, length = 32)
    var idType: String,

    @Column(nullable = false)
    var idNumber: String,

    @Column(nullable = false)
    var personalId: String,

    @Column(nullable = false)
    var createdAt: ZonedDateTime,

    @Column(nullable = false)
    var connected: Boolean
)
