package com.walletconnect.push.common.model

import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.AppMetaData

sealed class EngineDO {

    data class PushRequest(
        val publicKey: String,
        val metaData: AppMetaData,
        val account: String,
    ) : EngineDO(), EngineEvent

    data class Message(
        val title: String,
        val body: String,
        val icon: String,
        val url: String,
    ): EngineDO(), EngineEvent

    data class Subscription(
        val topic: String,
        val relay: Relay,
        val metadata: AppMetaData,
    ): EngineDO() {

        data class Relay(val protocol: String, val `data`: String)
    }
}
