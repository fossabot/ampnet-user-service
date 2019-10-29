package com.ampnet.userservice.service

import com.ampnet.userservice.exception.SocialException
import com.ampnet.userservice.service.pojo.SocialUser

interface SocialService {

    @Throws(SocialException::class)
    fun getFacebookEmail(token: String): SocialUser

    @Throws(SocialException::class)
    fun getGoogleEmail(token: String): SocialUser
}
