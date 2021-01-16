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

@file:Suppress("MemberVisibilityCanBePrivate")

package eu.warfaremc.common.yaml

data class YamlPath(val segments: List<YamlPathSegment>) {
        constructor(vararg segments: YamlPathSegment) : this(segments.toList())

    val endLocation: Location = segments.last().location

    init {
        if (segments.isEmpty())
            throw IllegalArgumentException("Path must contain at least one segment")
        if (segments.first() !is YamlPathSegment.Root && segments.first() !is YamlPathSegment.AliasDefinition)
            throw IllegalArgumentException("First element of path must be root segment or alias definition")
        if (segments.drop(1).contains(YamlPathSegment.Root))
            throw IllegalArgumentException("Root segment can only be first of path")
        toHumanReadable()
    }

    fun withError(location: Location): YamlPath = withSegment(YamlPathSegment.Error(location))
    fun withListEntry(index: Int, location: Location): YamlPath = withSegment(YamlPathSegment.ListEntry(index, location))
    fun withMapElementKey(key: String, location: Location): YamlPath = withSegment(YamlPathSegment.MapElementKey(key, location))
    fun withMapElementValue(location: Location): YamlPath = withSegment(YamlPathSegment.MapElementValue(location))
    fun withAliasReference(name: String, location: Location): YamlPath = withSegment(YamlPathSegment.AliasReference(name, location))
    fun withAliasDefinition(name: String, location: Location): YamlPath = withSegment(YamlPathSegment.AliasDefinition(name, location))
    fun withMerge(location: Location): YamlPath = withSegment(YamlPathSegment.Merge(location))

    fun withSegment(segment: YamlPathSegment): YamlPath = YamlPath(segments + segment)

    fun toHumanReadable(): String {
        var index = 1
        val builder = StringBuilder()
        while (index <= segments.lastIndex) {
            val segmentIndex = index
            index ++
            when (val segment = segments[segmentIndex]) {
                is YamlPathSegment.ListEntry -> {
                    builder.append('[')
                    builder.append(segment.index)
                    builder.append(']')
                }
                is YamlPathSegment.MapElementKey -> {
                    if (builder.isNotEmpty())
                        builder.append('.')
                    builder.append(segment.key)
                }
                is YamlPathSegment.AliasReference -> {
                    builder.append("->&")
                    builder.append(segment.name)
                }
                is YamlPathSegment.Merge -> {
                    builder.append(">>(merged")
                    if (index <= segments.lastIndex && segments[index] is YamlPathSegment.ListEntry) {
                        builder.append(" entry ")
                        builder.append((segments[index] as YamlPathSegment.ListEntry).index)
                        index ++
                    }
                    if (index <= segments.lastIndex && segments[index] is YamlPathSegment.AliasReference) {
                        builder.append(" &")
                        builder.append((segments[index] as YamlPathSegment.AliasReference).name)
                        index ++
                    }
                    builder.append(")")
                }
                is YamlPathSegment.Root, is YamlPathSegment.Error, is YamlPathSegment.MapElementValue, is YamlPathSegment.AliasDefinition -> {  }
            }
        }
        if (builder.isNotEmpty())
            return builder.toString()
        return "<root>"
    }

    companion object {
        val root: YamlPath = YamlPath(YamlPathSegment.Root)
        fun forAliasDefinition(name: String, location: Location): YamlPath = YamlPath(YamlPathSegment.AliasDefinition(name, location))
    }
}

sealed class YamlPathSegment(open val location: Location) {
    object Root : YamlPathSegment(Location(1, 1))
    data class ListEntry(val index: Int, override val location: Location) : YamlPathSegment(location)
    data class MapElementKey(val key: String, override val location: Location) : YamlPathSegment(location)
    data class MapElementValue(override val location: Location) : YamlPathSegment(location)
    data class AliasReference(val name: String, override val location: Location) : YamlPathSegment(location)
    data class AliasDefinition(val name: String, override val location: Location) : YamlPathSegment(location)
    data class Merge(override val location: Location) : YamlPathSegment(location)
    data class Error(override val location: Location) : YamlPathSegment(location)
}