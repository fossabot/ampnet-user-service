package com.ampnet.userservice.service.impl

import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.userservice.service.MailService
import io.grpc.stub.StreamObserver
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service

@Service
class MailServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory
) : MailService {

    companion object : KLogging()

    private val mailServiceStub: MailServiceGrpc.MailServiceStub by lazy {
        val channel = grpcChannelFactory.createChannel("mail-service")
        MailServiceGrpc.newStub(channel)
    }

    override fun sendConfirmationMail(email: String, token: String) {
        logger.debug { "Sending confirmation mail to: $email" }
        val request = MailConfirmationRequest.newBuilder()
            .setEmail(email)
            .setToken(token)
            .build()

        mailServiceStub.sendMailConfirmation(request, getStreamObserver("confirmation mail to: $email"))
    }

    override fun sendResetPasswordMail(email: String, token: String) {
        logger.debug { "Sending reset password mail to: $email" }
        val request = ResetPasswordRequest.newBuilder()
            .setEmail(email)
            .setToken(token)
            .build()

        mailServiceStub.sendResetPassword(request, getStreamObserver("forgot password mail to: $email"))
    }

    private fun getStreamObserver(message: String) = object : StreamObserver<Empty> {
        override fun onNext(value: Empty?) {
            logger.info { "Successfully sent $message" }
        }

        override fun onError(t: Throwable?) {
            logger.warn { "Failed to sent $message. ${t?.message}" }
        }

        override fun onCompleted() {
            // sending completed
        }
    }
}
