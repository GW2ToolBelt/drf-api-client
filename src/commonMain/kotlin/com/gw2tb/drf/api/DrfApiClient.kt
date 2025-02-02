/*
 * Copyright (c) 2024-2025 Leon Linhart
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
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmOverloads

/**
 * An API client acts as entry-point to make requests against the DRF API.
 *
 * @since   0.1.0
 */
public class DrfApiClient private constructor(
    private val host: String,
    private val httpClient: HttpClient
) : Closeable by httpClient {

    private companion object {
        const val DEFAULT_HOST: String = "drf.rs"
    }

    /**
     * Creates a new [DrfApiClient].
     *
     * @param host  the host URL fragment of the API
     *
     * @since   0.1.0
     */
    @JvmOverloads
    public constructor(
        host: String = DEFAULT_HOST
    ): this(
        host = host,
        httpClient = HttpClient {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    )

    /**
     * Creates a new [DrfApiClient] with the given [engine].
     *
     * @param host  the host URL fragment of the API
     *
     * @since   0.1.0
     */
    @JvmOverloads
    public constructor(
        engine: HttpClientEngine,
        host: String = DEFAULT_HOST
    ): this(
        host = host,
        httpClient = HttpClient(engine) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json {
                    serializersModule
                })
            }
        }
    )

    /**
     * Returns a "cold" [Flow] that emits [messages][DrfMessage] for live updates from DRF.
     *
     * @param apiKey    the DRF API key used to subscribe to a message flow
     *
     * @since   0.1.0
     */
    public fun subscribe(apiKey: String): Flow<DrfMessage> = flow {
        val url = buildUrl {
            this.protocol = URLProtocol.WSS
            this.host = this@DrfApiClient.host
            path("/ws")
        }

        val webSocketSession = httpClient.webSocketSession(url.toString())

        try {
            webSocketSession.send(Frame.Text("Bearer $apiKey"))

            webSocketSession.incoming.receiveAsFlow()
                .map { frame -> webSocketSession.converter!!.deserialize<DrfMessage>(frame) }
                .onEach(::emit)
                .collect()

            val closeReason = webSocketSession.closeReason.await()
            if (closeReason?.code != CloseReason.Codes.NORMAL.code) {
                throw SocketClosedException(closeReason, null)
            }
        } catch (e: CancellationException) {
            throw SocketClosedException(webSocketSession.closeReason.await(), e.cause)
        } finally {
            webSocketSession.close()
        }
    }

}