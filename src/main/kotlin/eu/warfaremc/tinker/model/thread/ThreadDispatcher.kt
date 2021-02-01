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

package eu.warfaremc.tinker.model.thread

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import kotlin.coroutines.CoroutineContext


private val scheduler
    get() = Bukkit.getScheduler()

@OptIn(InternalCoroutinesApi::class)
class BukkitDispatcher(private val plugin: JavaPlugin, private val async: Boolean = false) : CoroutineDispatcher(),
    Delay {
    private val runTaskLater: (Plugin, Runnable, Long) -> BukkitTask =
        if (async) scheduler::runTaskLaterAsynchronously else scheduler::runTaskLater

    private val runTask: (Plugin, Runnable) -> BukkitTask =
        if (async) scheduler::runTaskAsynchronously else scheduler::runTask

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (context.isActive == false)
            return
        if (async == false && Bukkit.isPrimaryThread())
            block.run()
        else runTask(plugin, block)
    }

    @ExperimentalCoroutinesApi
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val task = runTaskLater(
            plugin,
            Runnable {
                continuation.apply { resumeUndispatched(Unit) }
            },
            timeMillis / 50
        )
        continuation.invokeOnCancellation { task.cancel() }
    }
}

fun JavaPlugin.dispatcher(async: Boolean = false) = BukkitDispatcher(this, async)