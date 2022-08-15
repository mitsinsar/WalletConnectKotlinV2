@file:JvmSynthetic

package com.walletconnect.sign.common.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MetaDataVO(
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "icons")
    val icons: List<String>,
    @Json(name = "redirect")
    val redirect: RedirectVO? = null,
)