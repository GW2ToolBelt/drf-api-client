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

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.*

/**
 * A message with drop information.
 *
 * @param character the name of the current character
 * @param drop      the drop
 *
 * @since   0.1.0
 */
@Serializable(with = DataMessageSerializer::class)
public data class DataMessage(
    val character: String,
    val drop: Drop
) : DrfMessage {

    /**
     * A drop.
     *
     * @param items         the dropped items. Each map entry represents a type of item where the key is the item's ID
     *                      and the value the change in the item's amount.
     * @param currencies    the dropped currencies. Each map entry represents a type of currency where the key is the
     *                      currency's ID and the value the change in the currency's amount.
     * @param magicFind     the magic find during the drop
     * @param timestamp     the ISO 8601 timestamp of the drop
     *
     * @since   0.1.0
     */
    @Serializable
    public data class Drop(
        val items: Map<Int, Int>,
        @SerialName("curr")
        val currencies: Map<Int, Int>,
        @SerialName("mf")
        val magicFind: Int,
        val timestamp: String
    )

}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = DataMessage::class)
private object DefaultDataMessageSerializer : KSerializer<DataMessage> {

    // Workaround for https://github.com/Kotlin/kotlinx.serialization/issues/2549
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.gw2tb.drf.api.model.DataMessage") {
        element("character", serialDescriptor<String>())
        element("drop", DataMessage.Drop.serializer().descriptor)
    }

}

private object DataMessageSerializer : JsonTransformingSerializer<DataMessage>(DefaultDataMessageSerializer) {

    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject["payload"] ?: error("Could not find 'payload' field in $element")

    override fun transformSerialize(element: JsonElement): JsonElement = buildJsonObject {
        put("kind", "data")
        put("payload", element)
    }

}