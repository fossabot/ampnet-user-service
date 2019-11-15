package com.ampnet.userservice.grpc.mailservice

import com.ampnet.mailservice.proto.Empty
import com.ampnet.mailservice.proto.MailConfirmationRequest
import com.ampnet.mailservice.proto.MailServiceGrpc
import com.ampnet.mailservice.proto.ResetPasswordRequest
import com.ampnet.userservice.config.ApplicationProperties
import io.grpc.stub.StreamObserver
import java.util.concurrent.TimeUnit
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service

@Service
class MailServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : MailService {

    companion object : KLogging()

    private val mailServiceStub: MailServiceGrpc.MailServiceStub by lazy {
        val channel = grpcChannelFactory.createChannel("mail-service")
        MailServiceGrpc.newStub(channel)
            .withDeadlineAfter(applicationProperties.grpc.mailServiceTimeout, TimeUnit.MILLISECONDS)
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
