/*
 * Copyright (c) 2024 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gw2tb.drf.api

import com.gw2tb.drf.api.exceptions.SocketClosedException
import com.gw2tb.drf.api.model.DrfMessage
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

/**
 * An API client acts as entry-point to make requests against the DRF API.
 *
 * @since   0.1.0
 */
public class DrfApiClient private constructor(
    private val httpClient: HttpClient
) : Closeable by httpClient {

    /**
     * Creates a new [DrfApiClient].
     *
     * @since   0.1.0
     */
    public constructor(): this(
        httpClient = HttpClient {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    )

    /**
     * Creates a new [DrfApiClient] with the given [engine].
     *
     * @since   0.1.0
     */
    public constructor(engine: HttpClientEngine): this(
        httpClient = HttpClient(engine) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json {
                    serializersModule
                })
            }
        }
    )

    /**
     * Subscribes to a [Flow] of messages.
     *
     * This method opens a websocket connection to retrieve a continuous flow of live drop messages from the DRF API.
     * The websocket connection is closed when the flow is completed (either "normally" by using a terminal operator or
     * exceptionally).
     *
     * @param apiKey    the DRF API key used to subscribe to a message flow
     *
     * @throws SocketClosedException    if the websocket is closed by the remote
     *
     * @since   0.1.0
     */
    public suspend fun subscribe(apiKey: String): Flow<DrfMessage> {
        val webSocketSession = httpClient.webSocketSession("wss://drf.rs/ws")
        webSocketSession.send(Frame.Text("Bearer $apiKey"))

        return webSocketSession.incoming.consumeAsFlow()
            .onCompletion { webSocketSession.close() }
            .map { frame -> webSocketSession.converter!!.deserialize<DrfMessage>(frame) }
            .catch { e -> when {
                e is ClosedReceiveChannelException -> {
                    val closeReason = webSocketSession.closeReason.await()
                    throw SocketClosedException(closeReason, e)
                }
                else -> throw e
            }}
    }

}