package com.ampnet.userservice.grpc.mailservice

interface MailService {
    fun sendConfirmationMail(email: String, token: String)
    fun sendResetPasswordMail(email: String, token: String)
}
