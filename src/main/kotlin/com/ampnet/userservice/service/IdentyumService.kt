package com.ampnet.userservice.service

import com.ampnet.userservice.controller.pojo.request.IdentyumPayloadRequest

interface IdentyumService {
    fun getToken(): String
    fun storeUser(request: IdentyumPayloadRequest)
}
