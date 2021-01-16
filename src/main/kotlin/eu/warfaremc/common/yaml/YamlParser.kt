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

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import java.io.StringReader

internal class YamlParser(source: String) {

    private val dummy = "DUMMY_FILE_NAME"
    private val loadSettings = LoadSettings.builder().setLabel(dummy).build()
    private val streamReader = StreamReader(StringReader(source), loadSettings)
    private val events = ParserImpl(streamReader, loadSettings)

    init {
        consumeEventOfType(Event.ID.StreamStart, YamlPath.root)
        if (peek(YamlPath.root).eventId == Event.ID.StreamEnd)
            throw EmptyYamlDocumentException("The YAML document is empty", YamlPath.root)
        consumeEventOfType(Event.ID.DocumentStart, YamlPath.root)
    }

    fun ensureEndOfStreamReached() {
        consumeEventOfType(Event.ID.DocumentEnd, YamlPath.root)
        consumeEventOfType(Event.ID.StreamEnd, YamlPath.root)
    }

    fun peek(path: YamlPath): Event = checkEvent(path) { events.peekEvent() }
    fun consumeEvent(path: YamlPath): Event = checkEvent(path) { events.next() }
    fun consumeEventOfType(type: Event.ID, path: YamlPath) {
        val event = consumeEvent(path)
        if (event.eventId != type)
            throw MalformedYamlException("Unexpected ${event.eventId}, expected $type", path.withError(Location(event.startMark.get().line, event.startMark.get().column)))
    }

    private fun checkEvent(path: YamlPath, retrieve: () -> Event): Event {
        try {
            return retrieve()
        } catch (exception: MarkedYamlEngineException) {
            throw translateYamlEngineException(exception, path)
        }
    }

    private fun translateYamlEngineException(exception: MarkedYamlEngineException, path: YamlPath): MalformedYamlException {
        val context = if (exception.context == null) "" else {
            val contextMark = exception.contextMark.get()
            exception.context + "\n" +
                    " at line ${contextMark.line + 1}, column ${contextMark.column + 1}:\n" +
                    contextMark.createSnippet(4, Int.MAX_VALUE) + "\n"
        }
        val problemMark = exception.problemMark.get()
        val message = context +
                translateYamlEngineExceptionMessage(exception.problem) + "\n" +
                " at line ${problemMark.line + 1}, column ${problemMark.column + 1}:\n" +
                problemMark.createSnippet(4, Int.MAX_VALUE)
        return MalformedYamlException(message, path.withError(Location(problemMark.line + 1, problemMark.column + 1)))
    }

    private fun translateYamlEngineExceptionMessage(message: String): String = when (message) {
        "mapping values are not allowed here",
        "expected <block end>, but found '<block sequence start>'",
        "expected <block end>, but found '<block mapping start>'" -> "$message (is the indentation level of this line or a line nearby incorrect?)"
        else -> message
    }
}