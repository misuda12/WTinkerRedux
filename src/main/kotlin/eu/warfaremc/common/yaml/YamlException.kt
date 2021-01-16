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

import kotlinx.serialization.SerializationException

open class YamlException(
    override val message: String,
    val path: YamlPath,
    override val cause: Throwable? = null
) : SerializationException(message, cause) {
    val location: Location = path.endLocation
    val line: Int = location.line
    val column: Int = location.column

    override fun toString(): String
            = "${this::class.qualifiedName} at ${path.toHumanReadable()} on line $line, column $column: $message"
}

class DuplicateKeyException(
    val originalPath: YamlPath,
    val duplicatePath: YamlPath,
    val key: String
) : YamlException("Duplicate key $key. It was previously given at line ${originalPath.endLocation.line}, column ${originalPath.endLocation.column}", duplicatePath) {
    val originalLocation: Location  = originalPath.endLocation
    val duplicateLocation: Location = duplicatePath.endLocation
}

class EmptyYamlDocumentException(message: String, path: YamlPath) : YamlException(message, path)

class InvalidPropertyValueException(
    val propertyName: String,
    val reason: String,
    path: YamlPath,
    cause: Throwable? = null
) : YamlException("Value for '$propertyName' is invalid: $reason", path, cause)

class MalformedYamlException(message: String, path: YamlPath) : YamlException(message, path)

class UnexpectedNullValueException(path: YamlPath) : YamlException("Unexpected null or empty value for non-null field", path)

class MissingRequiredPropertyException(
    val propertyName: String,
    path: YamlPath,
    cause: Throwable? = null
) : YamlException("Property '$propertyName' is required but it is missing", path, cause)

class UnknownPropertyException(
    val propertyName: String,
    val validPropertyNames: Set<String>,
    path: YamlPath
) : YamlException("Unknown property '$propertyName'. Known properties are: ${validPropertyNames.sorted().joinToString(", ")}", path)

class UnknownPolymorphicTypeException(
    val typeName: String,
    val validTypeNames: Set<String>,
    path: YamlPath,
    cause: Throwable? = null
) : YamlException("Unknown type '$typeName'. Known types are: ${validTypeNames.sorted().joinToString(", ")}", path, cause)

class YamlScalarFormatException(
    message: String,
    path: YamlPath,
    val originalValue: String
) : YamlException(message, path)

open class IncorrectTypeException(message: String, path: YamlPath) : YamlException(message, path)

class MissingTypeTagException(path: YamlPath) :
    IncorrectTypeException("Value is missing a type tag (eg. !<type>)", path)

class UnknownAnchorException(
    val anchorName: String,
    path: YamlPath
) : YamlException("Unknown anchor '$anchorName'", path)

class NoAnchorForExtensionException(
    val key: String,
    val extensionDefinitionPrefix: String,
    path: YamlPath
) : YamlException("The key '$key' starts with the extension definition prefix '$extensionDefinitionPrefix' but does not define an anchor", path)