package com.ampnet.userservice.service

interface MailService {
    fun sendConfirmationMail(to: String, token: String)
}
