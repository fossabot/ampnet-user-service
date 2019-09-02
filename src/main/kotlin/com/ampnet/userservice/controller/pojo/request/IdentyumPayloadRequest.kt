package com.ampnet.userservice.controller.pojo.request

import com.fasterxml.jackson.annotation.JsonProperty

data class IdentyumPayloadRequest(
    @JsonProperty("webSessionUuid")
    val webSessionUuid: String,

    @JsonProperty("productUuid")
    val productUuid: String,

    @JsonProperty("reportUuid")
    val reportUuid: String,

    @JsonProperty("reportName")
    val reportName: String,

    val version: Int,

    @JsonProperty("outputFormat")
    val outputFormat: String,

    @JsonProperty("payloadFormat")
    val payloadFormat: String,

    @JsonProperty("processStatus")
    val processStatus: String,

    val payload: String,

    @JsonProperty("payloadSignature")
    val payloadSignature: String,

    @JsonProperty("tsCreated")
    val tsCreated: String
) {
    override fun toString(): String {
        return "IdentyumPayloadRequest(webSessionUuid=$webSessionUuid, productUuid=$productUuid " +
            "reportUuid=$reportUuid, " + "reportName=$reportName, version=$version, outputFormat=$outputFormat, " +
            "payloadFormat=$payloadFormat, " + "processStatus=$processStatus, " + "tsCreated=$tsCreated)"
    }
}
