/*
 * This file is part of WarfareMC, licensed under the MIT License.
 *
 * Copyright (C) 2020 WarfareMC & Team
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.warfaremc.common.yaml

import org.snakeyaml.engine.v2.common.FlowStyle

data class YamlConfiguration constructor(
    internal val encodeDefaults: Boolean = true,
    internal val strictMode: Boolean = true,
    internal val extensionDefinitionPrefix: String? = null,
    internal val polymorphismStyle: PolymorphismStyle = PolymorphismStyle.Tag,
    internal val polymorphismPropertyName: String = "type",
    internal val encodingIndentationSize: Int = 2,
    internal val breakScalarsAt: Int = 80,
    internal val sequenceStyle: SequenceStyle = SequenceStyle.Block
)

enum class PolymorphismStyle {
    Tag,
    Property
}

enum class SequenceStyle(internal val flowStyle: FlowStyle) {
    Block(FlowStyle.BLOCK),
    Flow (FlowStyle.FLOW)
}