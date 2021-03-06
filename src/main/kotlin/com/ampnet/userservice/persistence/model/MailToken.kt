package com.ampnet.userservice.persistence.model

import java.time.ZonedDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "mail_token")
data class MailToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @ManyToOne
    @JoinColumn(name = "user_uuid")
    var user: User,

    @Column(nullable = false)
    var token: UUID,

    @Column(nullable = false)
    var createdAt: ZonedDateTime
) {
    fun isExpired(): Boolean {
        return createdAt.plusDays(1).isBefore(ZonedDateTime.now())
    }
}
