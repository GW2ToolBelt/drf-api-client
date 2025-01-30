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

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.*

/**
 * A message that is sent when session information changes.
 *
 * @param character the name of the current character
 * @param level     the character's level
 * @param map       the ID of the current map
 * @param start     the ISO 8601 timestamp of the session start
 * @param end       the ISO 8601 timestamp of the session end. This information might be missing if the session is still
 *                  ongoing.
 *
 * @since   0.1.0
 */
@Serializable(with = SessionUpdateMessageSerializer::class)
public data class SessionUpdateMessage(
    val character: String,
    val level: Int,
    val map: Int,
    val start: String,
    val end: String?
) : DrfMessage

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SessionUpdateMessage::class)
private object DefaultSessionUpdateMessageSerializer : KSerializer<SessionUpdateMessage> {

    // Workaround for https://github.com/Kotlin/kotlinx.serialization/issues/2549
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.gw2tb.drf.api.model.SessionUpdateMessage") {
        element("character", serialDescriptor<String>())
        element("level", serialDescriptor<Int>())
        element("map", serialDescriptor<Int>())
        element("start", serialDescriptor<String>())
        element("end", serialDescriptor<String>())
    }

}

private object SessionUpdateMessageSerializer : JsonTransformingSerializer<SessionUpdateMessage>(DefaultSessionUpdateMessageSerializer) {

    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject["payload"] ?: error("Could not find 'payload' field in $element")

    override fun transformSerialize(element: JsonElement): JsonElement = buildJsonObject {
        put("kind", "session_update")
        put("payload", element)
    }

}