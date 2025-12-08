package com.internetofthings.displaycontroller.websocket

import com.internetofthings.displaycontroller.helpers.GPSDAO
import com.internetofthings.displaycontroller.models.GPSModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.min

class GPSWebSocketClient(
    private val espIp: String,
    private val dao: GPSDAO,
    private val onGpsData: (GPSModel) -> Unit
) {
    private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()
    private var retryCount = 0
    private val maxDelay = 60L // maximum delay in seconds

    fun connect() {
        val request = Request.Builder().url("ws://$espIp/ws").build()

        client.newWebSocket(request, object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Connected to ESP32 WebSocket.")
                retryCount = 0
                webSocket.send("SYNC")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val gpsData = GPSModel(
                        latitude = json.getDouble("lat"),
                        longitude = json.getDouble("lng"),
                        time = json.getString("time"),
                    )

                    GlobalScope.launch { dao.insert(gpsData) }

                    // TODO: update UI with gpsData
                    onGpsData(gpsData)
                } catch (e: Exception) { println("Invalid JSON: $text") }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket error: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closing: $code $reason")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        retryCount++
        val delay = calculateDelay(retryCount)
        println("Reconnecting in $delay seconds...")

        Thread {
            try {
                TimeUnit.SECONDS.sleep(delay)
                connect()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun calculateDelay(attempt: Int): Long {
        // exponential backoff: 2^attempt, capped at maxDelay
        return min((1 shl attempt).toLong(), maxDelay)
    }
}

