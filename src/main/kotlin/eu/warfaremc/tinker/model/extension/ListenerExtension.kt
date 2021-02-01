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

package eu.warfaremc.tinker.model.extension

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

interface ExtendedListener<in T : Event> : Listener {
    fun onEvent(event: T)
}

inline fun <reified T : Event> listener(
    crossinline action: Listener.(T) -> Unit
) = object : ExtendedListener<T> {
    override fun onEvent(event: T) {
        action(event)
    }
}

inline fun <reified T : Event> Plugin.hear(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    crossinline action: Listener.(T) -> Unit
) = listener(action).also { it.register(this, priority, ignoreCancelled) }

inline fun <reified T : Event> ExtendedListener<T>.register(
    plugin: Plugin,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false
) = register<T>(plugin, priority, ignoreCancelled) { listener, event ->
    @Suppress("UNCHECKED_CAST")
    (listener as ExtendedListener<T>).onEvent(event as T)
}

inline fun <reified T : Event> Listener.register(
    plugin: Plugin,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline eventExecutor: (Listener, Event) -> Unit
) = Bukkit.getPluginManager().registerEvent(T::class.java, this, priority, eventExecutor, plugin, ignoreCancelled)

fun Listener.register(plugin: Plugin) = Bukkit.getPluginManager().registerEvents(this, plugin)

fun Listener.unregister() = HandlerList.unregisterAll(this)

val eventExpectationPool: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

inline fun <reified T : Event> Plugin.expect(
    amount: Int = 1,
    crossinline predicate: (T) -> Boolean = { true },
    timeout: Long = 0,
    timeoutUnit: TimeUnit = TimeUnit.MILLISECONDS,
    crossinline timeoutAction: () -> Unit = {},
    crossinline action: (T) -> Unit
): ExtendedListener<T> {
    var count = 0
    val listener = hear<T> {
        if (predicate(it)) {
            action(it)
            if (++count >= amount)
                unregister()
        }
    }
    if (timeout > 0) {
        eventExpectationPool.schedule({
            if (count < amount) {
                timeoutAction()
                listener.unregister()
            }
        }, timeout, timeoutUnit)
    }
    return listener
}