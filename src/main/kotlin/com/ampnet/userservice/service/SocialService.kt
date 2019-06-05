package com.ampnet.userservice.service

import com.ampnet.userservice.exception.SocialException

interface SocialService {

    @Throws(SocialException::class)
    fun getFacebookEmail(token: String): String

    @Throws(SocialException::class)
    fun getGoogleEmail(token: String): String
}
