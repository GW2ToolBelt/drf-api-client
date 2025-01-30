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
package com.gw2tb.drf.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.json.Json.Default.decodeFromJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionUpdateMessageTest {

    @Test
    fun testDecodeFromJson() {
        val json = Json

        assertEquals(
            expected = SessionUpdateMessage(
                character = "John Doe",
                level = 42,
                map = 1517,
                start = "2024-03-28T20:27:20.496005890Z",
                end = null
            ),
            actual = json.decodeFromString("""{"kind":"session_update","payload":{"character":"John Doe","level":42,"map":1517,"start":"2024-03-28T20:27:20.496005890Z","end":null}}""")
        )

        assertEquals(
            expected = SessionUpdateMessage(
                character = "John Doe",
                level = 42,
                map = 1517,
                start = "2024-03-28T20:27:20.496005890Z",
                end = "2024-03-28T20:44:56.123511224Z"
            ),
            actual = json.decodeFromString("""{"kind":"session_update","payload":{"character":"John Doe","level":42,"map":1517,"start":"2024-03-28T20:27:20.496005890Z","end":"2024-03-28T20:44:56.123511224Z"}}""")
        )
    }

    @Test
    fun testEncodeToJson() {
        val json = Json

        assertEquals(
            expected = buildJsonObject {
                put("kind", "session_update")
                putJsonObject("payload") {
                    put("character", "John Doe")
                    put("level", 42)
                    put("map", 1517)
                    put("start", "2024-03-28T20:27:20.496005890Z")

                    @OptIn(ExperimentalSerializationApi::class)
                    put("end", null)
                }
            },
            actual = json.encodeToJsonElement(SessionUpdateMessage(
                character = "John Doe",
                level = 42,
                map = 1517,
                start = "2024-03-28T20:27:20.496005890Z",
                end = null
            ))
        )

        assertEquals(
            expected = buildJsonObject {
                put("kind", "session_update")
                putJsonObject("payload") {
                    put("character", "John Doe")
                    put("level", 42)
                    put("map", 1517)
                    put("start", "2024-03-28T20:27:20.496005890Z")
                    put("end", "2024-03-28T20:44:56.123511224Z")
                }
            },
            actual = json.encodeToJsonElement(SessionUpdateMessage(
                character = "John Doe",
                level = 42,
                map = 1517,
                start = "2024-03-28T20:27:20.496005890Z",
                end = "2024-03-28T20:44:56.123511224Z"
            ))
        )
    }

}