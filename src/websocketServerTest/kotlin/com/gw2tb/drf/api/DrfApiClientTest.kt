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

import app.cash.turbine.test
import com.gw2tb.drf.api.exceptions.SocketClosedException
import com.gw2tb.drf.api.model.SessionUpdateMessage
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DrfApiClientTest {

    @Test
    fun test_RegularCompletion() = runTest {
        // Ktor does not support websockets in MockEngine, so we actually have to spin up a whole application.
        lateinit var closeReason: Deferred<CloseReason?>

        val server = embeddedServer(TestEngine) {
            install(WebSockets)

            routing {
                webSocket("/ws") {
                    println("foobar")

                    closeReason = this.closeReason
                    assertEquals("Bearer <some-secret-token>", (incoming.receive() as Frame.Text).readText())

                    send(Frame.Text("""{"kind":"session_update","payload":{"character":"John Doe","level":42,"map":1517,"start":"2024-03-28T20:27:20.496005890Z","end":null}}"""))
                    send(Frame.Text("""{"kind":"session_update","payload":{"character":"John Doe","level":42,"map":1517,"start":"2024-03-28T20:27:20.496005890Z","end":"2024-03-28T20:44:56.123511224Z"}}"""))
                }
            }
        }
        server.start()

        try {
            val drfClient = DrfApiClient(server.engine.engine)
            val flow = drfClient.subscribe("<some-secret-token>")

            flow.test {
                assertEquals(
                    expected = SessionUpdateMessage(
                        character = "John Doe",
                        level = 42,
                        map = 1517,
                        start = "2024-03-28T20:27:20.496005890Z",
                        end = null
                    ),
                    actual = awaitItem()
                )

                assertEquals(
                    expected = SessionUpdateMessage(
                        character = "John Doe",
                        level = 42,
                        map = 1517,
                        start = "2024-03-28T20:27:20.496005890Z",
                        end = "2024-03-28T20:44:56.123511224Z"
                    ),
                    actual = awaitItem()
                )

                awaitComplete()
            }

            closeReason.await()
        } finally {
            server.stop()
        }
     }

    @Test
    fun test_AuthException() = testApplication {
        // Ktor does not support websockets in MockEngine, so we actually have to spin up a whole application.
        val server = embeddedServer(TestEngine) {
            install(WebSockets)

            routing {
                webSocket("/ws") {
                    assertEquals("Bearer <some-secret-token>", (incoming.receive() as Frame.Text).readText())
                    close(CloseReason(CloseReason.Codes.NOT_CONSISTENT, ""))

                    // dead messages but useful for testing
                    send(Frame.Text("""{"kind":"session_update","payload":{"character":"John Doe","level":42,"map":1517,"start":"2024-03-28T20:27:20.496005890Z","end":null}}"""))
                    send(Frame.Text("""{"kind":"session_update","payload":{"character":"John Doe","level":42,"map":1517,"start":"2024-03-28T20:27:20.496005890Z","end":"2024-03-28T20:44:56.123511224Z"}}"""))
                }
            }
        }
        server.start()

        try {
            val drfClient = DrfApiClient(engine = server.engine.engine)
            val flow = drfClient.subscribe("<some-secret-token>")

            assertFailsWith<SocketClosedException> { flow.collect() }
        } finally {
            server.stop()
        }
    }

}