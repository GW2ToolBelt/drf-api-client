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
package com.gw2tb.drf.api.model

import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DataMessageTest {

    @Test
    fun testDecodeFromJson() {
        val json = Json

        assertEquals(
            expected = DataMessage(
                character = "John Doe",
                drop = DataMessage.Drop(
                    items = mapOf(84731 to 3, 86181 to 1, 100109 to -1),
                    currencies = mapOf(2 to 1320, 73 to -26),
                    magicFind = 367,
                    timestamp = "2024-03-28T20:22:59.527482750Z"
                )
            ),
            actual = json.decodeFromString("""{"kind":"data","payload":{"character":"John Doe","drop":{"items":{"84731":3,"86181":1,"100109":-1},"curr":{"2":1320,"73":-26},"mf":367,"timestamp":"2024-03-28T20:22:59.527482750Z"}}}""")
        )

        assertEquals(
            expected = DataMessage(
                character = "John Doe",
                drop = DataMessage.Drop(
                    items = mapOf(),
                    currencies = mapOf(2 to 1320, 73 to -26),
                    magicFind = 367,
                    timestamp = "2024-03-28T20:22:59.527482750Z"
                )
            ),
            actual = json.decodeFromString("""{"kind":"data","payload":{"character":"John Doe","drop":{"items":{},"curr":{"2":1320,"73":-26},"mf":367,"timestamp":"2024-03-28T20:22:59.527482750Z"}}}""")
        )

        assertEquals(
            expected = DataMessage(
                character = "John Doe",
                drop = DataMessage.Drop(
                    items = mapOf(84731 to 3, 86181 to 1, 100109 to -1),
                    currencies = mapOf(),
                    magicFind = 367,
                    timestamp = "2024-03-28T20:22:59.527482750Z"
                )
            ),
            actual = json.decodeFromString("""{"kind":"data","payload":{"character":"John Doe","drop":{"items":{"84731":3,"86181":1,"100109":-1},"curr":{},"mf":367,"timestamp":"2024-03-28T20:22:59.527482750Z"}}}""")
        )
    }

    @Test
    fun testEncodeToJson() {
        val json = Json

        assertEquals(
            expected = buildJsonObject {
                put("kind", "data")
                putJsonObject("payload") {
                    put("character", "John Doe")
                    putJsonObject("drop") {
                        putJsonObject("items") {
                            put("84731", 3)
                            put("86181", 1)
                            put("100109", -1)
                        }
                        putJsonObject("curr") {
                            put("2", 1320)
                            put("73", -26)
                        }
                        put("mf", 367)
                        put("timestamp", "2024-03-28T20:22:59.527482750Z")
                    }
                }
            },
            actual = json.encodeToJsonElement(DataMessage(
                character = "John Doe",
                drop = DataMessage.Drop(
                    items = mapOf(84731 to 3, 86181 to 1, 100109 to -1),
                    currencies = mapOf(2 to 1320, 73 to -26),
                    magicFind = 367,
                    timestamp = "2024-03-28T20:22:59.527482750Z"
                )
            ))
        )

        assertEquals(
            expected = buildJsonObject {
                put("kind", "data")
                putJsonObject("payload") {
                    put("character", "John Doe")
                    putJsonObject("drop") {
                        putJsonObject("items") {}
                        putJsonObject("curr") {
                            put("2", 1320)
                            put("73", -26)
                        }
                        put("mf", 367)
                        put("timestamp", "2024-03-28T20:22:59.527482750Z")
                    }
                }
            },
            actual = json.encodeToJsonElement(DataMessage(
                character = "John Doe",
                drop = DataMessage.Drop(
                    items = mapOf(),
                    currencies = mapOf(2 to 1320, 73 to -26),
                    magicFind = 367,
                    timestamp = "2024-03-28T20:22:59.527482750Z"
                )
            ))
        )

        assertEquals(
            expected = buildJsonObject {
                put("kind", "data")
                putJsonObject("payload") {
                    put("character", "John Doe")
                    putJsonObject("drop") {
                        putJsonObject("items") {
                            put("84731", 3)
                            put("86181", 1)
                            put("100109", -1)
                        }
                        putJsonObject("curr") {}
                        put("mf", 367)
                        put("timestamp", "2024-03-28T20:22:59.527482750Z")
                    }
                }
            },
            actual = json.encodeToJsonElement(DataMessage(
                character = "John Doe",
                drop = DataMessage.Drop(
                    items = mapOf(84731 to 3, 86181 to 1, 100109 to -1),
                    currencies = mapOf(),
                    magicFind = 367,
                    timestamp = "2024-03-28T20:22:59.527482750Z"
                )
            ))
        )
    }

}