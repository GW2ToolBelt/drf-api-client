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

import com.gw2tb.drf.api.DrfApiClient
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A message sent by DRF over a websocket [subscription][DrfApiClient.subscribe].
 *
 * @since   0.1.0
 */
@Serializable(with = DrfMessageSerializer::class)
public sealed interface DrfMessage

public object DrfMessageSerializer : JsonContentPolymorphicSerializer<DrfMessage>(DrfMessage::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<DrfMessage> =
        when (val kind = element.jsonObject["kind"]?.jsonPrimitive?.content ?: error("Could not find 'kind' in ${element.jsonObject}")) {
            "data" -> DataMessage.serializer()
            "session_update" -> SessionUpdateMessage.serializer()
            else -> error("Unsupported message kind: $kind")
        }

}