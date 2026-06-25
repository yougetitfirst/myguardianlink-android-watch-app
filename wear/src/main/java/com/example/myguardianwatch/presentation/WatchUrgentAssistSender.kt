package com.example.myguardianwatch.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WatchUrgentAssistSender(private val context: Context) {

    sealed class SendResult {
        data object Success : SendResult()
        data object NoPhoneConnected : SendResult()
        data class Error(val message: String) : SendResult()
    }

    suspend fun sendUrgentAssist(): SendResult = withContext(Dispatchers.IO) {
        runCatching {
            val nodes = resolveTargetNodes()
            if (nodes.isEmpty()) {
                return@withContext SendResult.NoPhoneConnected
            }

            val messageClient = Wearable.getMessageClient(context)
            var delivered = false
            var lastError: String? = null

            for (node in nodes) {
                val messageSent = runCatching {
                    messageClient.sendMessage(
                        node.id,
                        WatchUrgentAssistBridge.MESSAGE_PATH,
                        WatchUrgentAssistBridge.MESSAGE_PAYLOAD.toByteArray(),
                    ).await()
                    true
                }.getOrElse { error ->
                    lastError = error.message
                    false
                }

                val activityOpened = openPhoneApp(node.id)
                if (messageSent || activityOpened) {
                    delivered = true
                }
            }

            when {
                delivered -> SendResult.Success
                !lastError.isNullOrBlank() -> SendResult.Error(lastError.orEmpty())
                else -> SendResult.Error("Unable to reach phone app.")
            }
        }.getOrElse { error ->
            SendResult.Error(error.message.orEmpty().ifBlank { "Unable to reach phone app." })
        }
    }

    private suspend fun resolveTargetNodes() = runCatching {
        val capabilityClient = Wearable.getCapabilityClient(context)
        val capabilityInfo = capabilityClient.getCapability(
            WatchUrgentAssistBridge.CAPABILITY_URGENT_ASSIST,
            CapabilityClient.FILTER_REACHABLE,
        ).await()
        capabilityInfo.nodes.toList()
    }.getOrElse {
        emptyList()
    }.ifEmpty {
        Wearable.getNodeClient(context).connectedNodes.await()
    }

    private suspend fun openPhoneApp(nodeId: String): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WatchUrgentAssistBridge.URGENT_ASSIST_DEEP_LINK))
                .addCategory(Intent.CATEGORY_BROWSABLE)
            RemoteActivityHelper(context).startRemoteActivity(intent, nodeId)
            true
        }.getOrDefault(false)
    }
}
