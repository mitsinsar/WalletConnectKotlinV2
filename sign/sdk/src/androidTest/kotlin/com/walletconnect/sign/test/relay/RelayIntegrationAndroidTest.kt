package com.walletconnect.sign.test.relay

import com.walletconnect.android.CoreClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.utils.WCIntegrationActivityScenarioRule
import junit.framework.Assert.fail
import org.junit.Rule
import org.junit.Test

class RelayIntegrationAndroidTest {
    @get:Rule
    val activityRule = WCIntegrationActivityScenarioRule()

    private fun globalOnError(error: Sign.Model.Error) {
        fail(error.throwable.stackTraceToString())
    }

    private fun setDelegates(walletDelegate: SignClient.WalletDelegate, dappDelegate: SignClient.DappDelegate) {
        activityRule.walletClient.setWalletDelegate(walletDelegate)
        activityRule.dappClient.setDappDelegate(dappDelegate)
    }

    @Test
    fun pairAndPing() {
        val walletDelegate = object : SignClient.WalletDelegate {
            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {}
            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {}
            override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {}
            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {}
            override fun onError(error: Sign.Model.Error) {
                globalOnError(error)
            }

            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                val namespaces: Map<String, Sign.Model.Namespace.Session> = mapOf(
                    "eip155" to Sign.Model.Namespace.Session(listOf("eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"),
                        listOf("someMethod"),
                        listOf("someEvent"),
                        null)
                )

                activityRule.walletClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, namespaces), onError = ::globalOnError)
            }

        }

        val dappDelegate = object : SignClient.DappDelegate {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {}
            override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {}
            override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {}
            override fun onSessionExtend(session: Sign.Model.Session) {}
            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {}
            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {}
            override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {}
            override fun onError(error: Sign.Model.Error) {
                globalOnError(error)
            }

            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                activityRule.dappClient.ping(Sign.Params.Ping(approvedSession.topic), object : Sign.Listeners.SessionPing {
                    override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
                        activityRule.close()
                    }

                    override fun onError(pingError: Sign.Model.Ping.Error) {
                        fail(pingError.error.stackTraceToString())
                    }
                })
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        activityRule.launch(10L) {
            CoreClient.Pairing.create()?.let { pairing ->
                val namespaces: Map<String, Sign.Model.Namespace.Proposal> = mapOf(
                    "eip155" to Sign.Model.Namespace.Proposal(listOf("eip155:1"), listOf("someMethod"), listOf("someEvent"), null)
                )

                val connectParams = Sign.Params.Connect(
                    namespaces = namespaces,
                    pairing = pairing
                )

                activityRule.dappClient.connect(connectParams,
                    onSuccess = {
                        activityRule.walletClient.pair(Sign.Params.Pair(pairing.uri), onError = ::globalOnError)
                    },
                    onError = {
                        globalOnError(it)
                    }
                )
            }
        }
    }
}