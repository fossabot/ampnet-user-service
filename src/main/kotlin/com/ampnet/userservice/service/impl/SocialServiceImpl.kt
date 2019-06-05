package com.ampnet.userservice.service.impl

import com.ampnet.userservice.exception.ErrorCode
import com.ampnet.userservice.exception.SocialException
import com.ampnet.userservice.service.SocialService
import mu.KLogging
import org.springframework.social.NotAuthorizedException
import org.springframework.social.facebook.api.User
import org.springframework.social.facebook.api.impl.FacebookTemplate
import org.springframework.social.google.api.impl.GoogleTemplate
import org.springframework.stereotype.Service

@Service
class SocialServiceImpl : SocialService {

    companion object : KLogging()

    @Throws(SocialException::class)
    override fun getFacebookEmail(token: String): String {
        logger.debug { "Getting Facebook user info" }

        try {
            val facebook = FacebookTemplate(token)
            val userProfile = facebook.fetchObject(
                    "me",
                    User::class.java,
                    "id", "email"
            )
            logger.debug { "Received Facebook user info with mail: ${userProfile.email}" }

            return userProfile.email
        } catch (ex: NotAuthorizedException) {
            throw SocialException(ErrorCode.REG_SOCIAL, "Not authorized to get data from Facebook", ex)
        }
    }

    @Throws(SocialException::class)
    override fun getGoogleEmail(token: String): String {
        logger.debug { "Getting Google user info" }

        try {
            val template = GoogleTemplate(token)
            val userInfo = template.oauth2Operations().userinfo
            logger.debug { "Received Google user info with mail: ${userInfo.email}" }
            return userInfo.email
        } catch (ex: Exception) {
            throw SocialException(ErrorCode.REG_SOCIAL, "Cannot fetch data from Google", ex)
        }
    }
}
