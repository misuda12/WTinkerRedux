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

import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.events.*
import java.util.*

sealed class YamlNode(open val path: YamlPath) {
    val location: Location
        get() = path.endLocation

    abstract fun equivalentContentTo(other: YamlNode): Boolean
    abstract fun contentToString(): String
    abstract fun withPath(newPath: YamlPath): YamlNode

    protected fun replacePathOnChild(child: YamlNode, newParentPath: YamlPath): YamlPath =
        YamlPath(newParentPath.segments + child.path.segments.drop(path.segments.size))

}

data class YamlScalar(val content: String, override val path: YamlPath): YamlNode(path) {

    override fun equivalentContentTo(other: YamlNode): Boolean = other is YamlScalar && this.content == other.content
    override fun contentToString(): String = "'$content'"

    fun toByte(): Byte = convertToIntegerLikeValue(String::toByte, "byte")
    fun toShort(): Short = convertToIntegerLikeValue(String::toShort, "short")
    fun toInt(): Int = convertToIntegerLikeValue(String::toInt, "integer")
    fun toLong(): Long = convertToIntegerLikeValue(String::toLong, "long")

    private fun <T> convertToIntegerLikeValue(converter: (String, Int) -> T, description: String): T {
        try {
            return when {
                content.startsWith("0o")  -> converter(content.substring(2), 8)
                content.startsWith("0x")  -> converter(content.substring(2), 16)
                content.startsWith("-0o") -> converter("-" + content.substring(3), 8)
                content.startsWith("-0x") -> converter("-" + content.substring(3), 16)
                else -> converter(content, 10)
            }
        } catch (exception: NumberFormatException) {
            throw YamlScalarFormatException("Value '$content' is not a valid $description value", path, content)
        }
    }

    fun toFloat(): Float {
        return when (content) {
            ".inf", ".Inf", ".INF" -> Float.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Float.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Float.NaN
            else -> {
                try {
                    content.toFloat()
                } catch (exception: NumberFormatException) {
                    throw YamlScalarFormatException(
                        "Value '$content' is not a valid floating point value",
                        path,
                        content
                    )
                }
            }
        }
    }

    fun toDouble(): Double {
        return when (content) {
            ".inf", ".Inf", ".INF" -> Double.POSITIVE_INFINITY
            "-.inf", "-.Inf", "-.INF" -> Double.NEGATIVE_INFINITY
            ".nan", ".NaN", ".NAN" -> Double.NaN
            else -> {
                try {
                    content.toDouble()
                } catch (exception: NumberFormatException) {
                    throw YamlScalarFormatException(
                        "Value '$content' is not a valid floating point value",
                        path,
                        content
                    )
                }
            }
        }
    }

    fun toBoolean(): Boolean {
        return when (content) {
            "true", "True", "TRUE" -> true
            "false", "False", "FALSE" -> false
            else -> throw YamlScalarFormatException("Value '$content' is not a valid boolean, permitted choices are: true or false", path, content)
        }
    }

    fun toChar(): Char = content.singleOrNull() ?: throw YamlScalarFormatException("Value '$content' is not a valid character value", path, content)

    override fun withPath(newPath: YamlPath): YamlScalar = this.copy(path = newPath)

    override fun toString(): String = "scalar @ $path : $content"

}

data class YamlNull(override val path: YamlPath) : YamlNode(path) {
    override fun equivalentContentTo(other: YamlNode): Boolean = other is YamlNull
    override fun contentToString(): String = "null"
    override fun withPath(newPath: YamlPath): YamlNull = YamlNull(newPath)
    override fun toString(): String = "null @ $path"
}

data class YamlList(val items: List<YamlNode>, override val path: YamlPath) : YamlNode(path) {

    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlList)
            return false
        if (this.items.size != other.items.size)
            return false
        return this.items.zip(other.items).all { (mine, theirs) -> mine.equivalentContentTo(theirs) }
    }


    override fun contentToString(): String = "[" + items.joinToString(", ") { it.contentToString() } + "]"
    override fun withPath(newPath: YamlPath): YamlList {
        val updatedItems = items.map { it.withPath(replacePathOnChild(it, newPath)) }

        return YamlList(updatedItems, newPath)
    }

    override fun toString() = buildString {
        appendLine("list @ $path (size: ${items.size})")
        items.forEachIndexed { index, item ->
            appendLine("- item $index:")

            item.toString().lines().forEach { line ->
                append("  ")
                appendLine(line)
            }
        }
        trimEnd()
    }
}

data class YamlMap(val entries: Map<YamlScalar, YamlNode>, override val path: YamlPath) : YamlNode(path) {
    init {
        val keys = entries.keys.sortedWith { a, b ->
            val lineComparison = a.location.line.compareTo(b.location.line)
            if (lineComparison != 0)
                lineComparison
            else a.location.column.compareTo(b.location.column)
        }
        keys.forEachIndexed { index, key ->
            val duplicate = keys.subList(0, index).firstOrNull { it.equivalentContentTo(key) }
            if (duplicate != null)
                throw DuplicateKeyException(duplicate.path, key.path, key.contentToString())
        }
    }

    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlMap)
            return false
        if (this.entries.size != other.entries.size)
            return false
        return this.entries.all { (thisKey, thisValue) -> other.entries.any { it.key.equivalentContentTo(thisKey) && it.value.equivalentContentTo(thisValue) } }
    }


    override fun contentToString(): String =
        "{" + entries.map { (key, value) -> "${key.contentToString()}: ${value.contentToString()}" }.joinToString(", ") + "}"

    @Suppress("UNCHECKED_CAST")
    operator fun <T : YamlNode> get(key: String): T? =
        entries.entries.firstOrNull { it.key.content == key }
            ?.value as T?

    fun getScalar(key: String): YamlScalar? = when (val node = get<YamlNode>(key)) {
        null          -> null
        is YamlScalar -> node
        else -> throw IncorrectTypeException("Value for '$key' is not a scalar", node.path)
    }

    fun getKey(key: String): YamlScalar? = entries.keys.singleOrNull { it.content == key }

    override fun withPath(newPath: YamlPath): YamlMap {
        val updatedEntries = entries.mapKeys { (k, _) -> k.withPath(replacePathOnChild(k, newPath)) }
            .mapValues { (_, v) -> v.withPath(replacePathOnChild(v, newPath)) }
        return YamlMap(updatedEntries, newPath)
    }

    override fun toString() = buildString {
        appendLine("map @ $path (size: ${entries.size})")
        entries.forEach { (key, value) ->
            appendLine("- key:")
            key.toString().lines().forEach { line ->
                append("    ")
                appendLine(line)
            }
            appendLine("  value:")
            value.toString().lines().forEach { line ->
                append("    ")
                appendLine(line)
            }
        }
        trimEnd()
    }
}

data class YamlTaggedNode(val tag: String, val innerNode: YamlNode) : YamlNode(innerNode.path) {
    override fun equivalentContentTo(other: YamlNode): Boolean {
        if (other !is YamlTaggedNode)
            return false
        if (tag != other.tag)
            return false
        return innerNode.equivalentContentTo(other.innerNode)
    }

    override fun contentToString(): String = "!$tag ${innerNode.contentToString()}"
    override fun withPath(newPath: YamlPath): YamlNode = this.copy(innerNode = innerNode.withPath(newPath))

    override fun toString(): String = "tagged '$tag': $innerNode"
}

internal class YamlNodeReader(
    private val parser: YamlParser,
    private val extensionDefinitionPrefix: String? = null
) {
    private val aliases = mutableMapOf<Anchor, YamlNode>()
    private val Event.location: Location
        get() = Location(startMark.get().line + 1, startMark.get().column + 1)

    fun read(): YamlNode = readNode(YamlPath.root)

    //region private
    private fun readNode(path: YamlPath): YamlNode = readNodeAndAnchor(path).first
    private fun readNodeAndAnchor(path: YamlPath): Pair<YamlNode, Anchor?> {
        val event = parser.consumeEvent(path)
        val node = readFromEvent(event, path)
        if (event is NodeEvent) {
            event.anchor.ifPresent {
                aliases[it] = node.withPath(YamlPath.forAliasDefinition(it.value, event.location))
            }
            return node to event.anchor.orElse(null)
        }
        return node to null
    }

    private fun readFromEvent(event: Event, path: YamlPath): YamlNode = when (event) {
        is ScalarEvent -> readScalarOrNull(event, path).maybeToTaggedNode(event.tag)
        is SequenceStartEvent -> readSequence(path).maybeToTaggedNode(event.tag)
        is MappingStartEvent -> readMapping(path).maybeToTaggedNode(event.tag)
        is AliasEvent -> readAlias(event, path)
        else -> throw MalformedYamlException("Unexpected ${event.eventId}", path.withError(event.location))
    }

    private fun readScalarOrNull(event: ScalarEvent, path: YamlPath): YamlNode {
        return if ((event.value == "null" || event.value == "") && event.isPlain)
            YamlNull(path)
        else YamlScalar(event.value, path)
    }

    private fun readSequence(path: YamlPath): YamlList {
        val items = mutableListOf<YamlNode>()
        while (true) {
            val event = parser.peek(path)
            when (event.eventId) {
                Event.ID.SequenceEnd -> {
                    parser.consumeEventOfType(Event.ID.SequenceEnd, path)
                    return YamlList(items, path)
                }
                else -> items += readNode(path.withListEntry(items.size, event.location))
            }
        }
    }

    private fun readMapping(path: YamlPath): YamlMap {
        val items = mutableMapOf<YamlScalar, YamlNode>()
        while (true) {
            val event = parser.peek(path)
            when (event.eventId) {
                Event.ID.MappingEnd -> {
                    parser.consumeEventOfType(Event.ID.MappingEnd, path)
                    return YamlMap(doMerges(items), path)
                }
                else -> {
                    val keyLocation = parser.peek(path).location
                    val key = readMapKey(path)
                    val keyNode = YamlScalar(key, path.withMapElementKey(key, keyLocation))
                    val valueLocation = parser.peek(keyNode.path).location
                    val valuePath = if (isMerged(keyNode)) path.withMerge(valueLocation) else keyNode.path.withMapElementValue(valueLocation)
                    val (value, anchor) = readNodeAndAnchor(valuePath)
                    if (path == YamlPath.root && extensionDefinitionPrefix != null && key.startsWith(extensionDefinitionPrefix)) {
                        if (anchor == null)
                            throw NoAnchorForExtensionException(key, extensionDefinitionPrefix, path.withError(event.location))
                    }
                    else items += (keyNode to value)
                }
            }
        }
    }

    private fun readMapKey(path: YamlPath): String {
        val event = parser.peek(path)
        when (event.eventId) {
            Event.ID.Scalar -> {
                parser.consumeEventOfType(Event.ID.Scalar, path)
                val scalarEvent = event as ScalarEvent
                if (scalarEvent.tag.isPresent || (scalarEvent.value == "null" && scalarEvent.isPlain))
                    throw nonScalarMapKeyException(path, event)
                return scalarEvent.value
            }
            else -> throw nonScalarMapKeyException(path, event)
        }
    }

    private fun nonScalarMapKeyException(path: YamlPath, event: Event) = MalformedYamlException("Property name must not be a list, map, null or tagged value. (To use 'null' as a property name, enclose it in quotes)", path.withError(event.location))

    private fun YamlNode.maybeToTaggedNode(tag: Optional<String>): YamlNode =
        tag.map<YamlNode> { YamlTaggedNode(it, this) }.orElse(this)

    private fun doMerges(items: Map<YamlScalar, YamlNode>): Map<YamlScalar, YamlNode> {
        val mergeEntries = items.entries.filter { (key, _) -> isMerged(key) }
        return when (mergeEntries.count()) {
            0 -> items
            1 -> when (val mappingsToMerge = mergeEntries.single().value) {
                is YamlList -> doMerges(items, mappingsToMerge.items)
                else -> doMerges(items, listOf(mappingsToMerge))
            }
            else -> throw MalformedYamlException("Cannot perform multiple '<<' merges into a map. Instead, combine all merges into a single '<<' entry", mergeEntries.second().key.path)
        }
    }

    private fun doMerges(original: Map<YamlScalar, YamlNode>, others: List<YamlNode>): Map<YamlScalar, YamlNode> {
        val merged = mutableMapOf<YamlScalar, YamlNode>()

        original.filterNot { (key, _) -> isMerged(key) }
            .forEach { (key, value) -> merged[key] = value }
        others.forEach { other ->
            when (other) {
                is YamlNull -> throw MalformedYamlException("Cannot merge a null value into a map", other.path)
                is YamlScalar -> throw MalformedYamlException("Cannot merge a scalar value into a map", other.path)
                is YamlList -> throw MalformedYamlException("Cannot merge a list value into a map", other.path)
                is YamlMap -> {
                    other.entries.forEach { (key, value) ->
                        val existingEntry = merged.entries.singleOrNull { it.key.equivalentContentTo(key) }

                        if (existingEntry == null)
                            merged[key] = value
                    }
                }
                // ignore YamlTaggedNode
                else -> {  }
            }
        }
        return merged
    }

    private fun readAlias(event: AliasEvent, path: YamlPath): YamlNode {
        val anchor = event.anchor.get()
        val resolvedNode = aliases.getOrElse(anchor) {
            throw UnknownAnchorException(anchor.value, path.withError(event.location))
        }
        return resolvedNode.withPath(path.withAliasReference(anchor.value, event.location).withAliasDefinition(anchor.value, resolvedNode.location))
    }

    private fun isMerged(key: YamlNode): Boolean = key is YamlScalar && key.content == "<<"
    private fun <T> Iterable<T>.second(): T = this.drop(1).first()
    //endregion private
}