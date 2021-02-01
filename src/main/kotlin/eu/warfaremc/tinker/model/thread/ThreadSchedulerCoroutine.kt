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

@file:Suppress("unused")

package eu.warfaremc.tinker.model.thread

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import kotlin.coroutines.*

fun Plugin.schedule(
    context: SynchronizationContext = SynchronizationContext.SYNC,
    block: suspend BukkitSchedulerController.() -> Unit
): CoroutineTask = server.scheduler.schedule(this, context, block)

fun BukkitScheduler.schedule(
    plugin: Plugin,
    context: SynchronizationContext = SynchronizationContext.SYNC,
    block: suspend BukkitSchedulerController.() -> Unit
): CoroutineTask {
    val controller = BukkitSchedulerController(plugin, this)
    val task: suspend BukkitSchedulerController.() -> Unit = {
        try {
            start(context)
            block()
        } finally {
            close()
        }
    }
    task.createCoroutine(receiver = controller, completion = controller).resume(Unit)
    return CoroutineTask(controller)
}

@RestrictsSuspension
class BukkitSchedulerController(val plugin: Plugin, private val scheduler: BukkitScheduler) : Continuation<Unit> {
    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    private var schedulerDelegate: TaskScheduler = TaskSchedulerNonRepeating(plugin, scheduler)

    val currentTask: BukkitTask?
        get() = schedulerDelegate.currentTask
    val isRepeating: Boolean
        get() = schedulerDelegate is TaskSchedulerRepeating

    internal suspend fun start(context: SynchronizationContext) = suspendCoroutine<Unit> { continuation ->
        schedulerDelegate.doContextSwitch(context) { continuation.resume(Unit) }
    }

    internal fun close() {
        currentTask?.cancel()
    }

    override fun resumeWith(result: Result<Unit>) {
        close()
        result.getOrThrow()
    }

    suspend fun waitFor(ticks: Long): Long = suspendCoroutine { continuation ->
        schedulerDelegate.doWait(ticks, continuation::resume)
    }

    suspend fun yield(): Long = suspendCoroutine { continuation ->
        schedulerDelegate.doYield(continuation::resume)
    }

    suspend fun switchContext(context: SynchronizationContext): Boolean = suspendCoroutine { continuation ->
        schedulerDelegate.doContextSwitch(context, continuation::resume)
    }

    suspend fun newContext(context: SynchronizationContext): Unit = suspendCoroutine { continuation ->
        schedulerDelegate.forceNewContext(context) { continuation.resume(Unit) }
    }

    suspend fun repeating(resolution: Long): Long = suspendCoroutine { continuation ->
        schedulerDelegate = TaskSchedulerRepeating(resolution, plugin, scheduler)
        schedulerDelegate.forceNewContext(currentContext()) { continuation.resume(0) }
    }
}

class CoroutineTask internal constructor(private val controller: BukkitSchedulerController) {

    val plugin: Plugin = controller.plugin
    val currentTask: BukkitTask? = controller.currentTask
    val isSync: Boolean = controller.currentTask?.isSync ?: false
    val isAsynchronous: Boolean = (controller.currentTask?.isSync ?: true) == false

    fun cancel() {
        controller.resume(Unit)
    }
}

enum class SynchronizationContext {

    SYNC,
    ASYNCHRONOUS

}

private class RepetitionContinuation(val resume: (Long) -> Unit, val delay: Long = 0) {

    var passedTicks = 0L
    private var resumed = false

    fun tryResume(passedTicks: Long) {
        if (resumed)
            throw error("Already resumed")
        this.passedTicks = passedTicks
        if (this.passedTicks >= delay) {
            resumed = true
            resume(this.passedTicks)
        }
    }
}

private interface TaskScheduler {

    val currentTask: BukkitTask?

    fun doWait(ticks: Long, task: (Long) -> Unit)

    fun doYield(task: (Long) -> Unit)

    fun doContextSwitch(context: SynchronizationContext, task: (Boolean) -> Unit)

    fun forceNewContext(context: SynchronizationContext, task: () -> Unit)

}

private class TaskSchedulerNonRepeating(val plugin: Plugin, val scheduler: BukkitScheduler) : TaskScheduler {
    override var currentTask: BukkitTask? = null

    override fun doWait(ticks: Long, task: (Long) -> Unit) {
        runTaskLater(ticks) { task(ticks) }
    }

    override fun doYield(task: (Long) -> Unit) {
        doWait(0, task)
    }

    override fun doContextSwitch(context: SynchronizationContext, task: (Boolean) -> Unit) {
        val current = currentContext()
        if (context == current)
            task(false)
        else forceNewContext(context) { task(true) }
    }

    override fun forceNewContext(context: SynchronizationContext, task: () -> Unit) {
        runTask(context) { task() }
    }

    private fun runTask(context: SynchronizationContext = currentContext(), task: () -> Unit) {
        currentTask = when (context) {
            SynchronizationContext.SYNC -> scheduler.runTask(plugin, task)
            SynchronizationContext.ASYNCHRONOUS -> scheduler.runTaskAsynchronously(plugin, task)
        }
    }

    private fun runTaskLater(ticks: Long, context: SynchronizationContext = currentContext(), task: () -> Unit) {
        currentTask = when (context) {
            SynchronizationContext.SYNC -> scheduler.runTaskLater(plugin, task, ticks)
            SynchronizationContext.ASYNCHRONOUS -> scheduler.runTaskLaterAsynchronously(plugin, task, ticks)
        }
    }
}

private class TaskSchedulerRepeating(
    val interval: Long,
    val plugin: Plugin,
    val scheduler: BukkitScheduler
) : TaskScheduler {
    override var currentTask: BukkitTask? = null
    private var nextContinuation: RepetitionContinuation? = null

    override fun doWait(ticks: Long, task: (Long) -> Unit) {
        nextContinuation = RepetitionContinuation(task, ticks)
    }

    override fun doYield(task: (Long) -> Unit) {
        nextContinuation = RepetitionContinuation(task)
    }

    override fun doContextSwitch(context: SynchronizationContext, task: (Boolean) -> Unit) {
        val current = currentContext()
        if (current == context)
            task(false)
        else forceNewContext(context) { task(true) }
    }

    override fun forceNewContext(context: SynchronizationContext, task: () -> Unit) {
        doYield { task() }
        runTaskTimer(context)
    }

    private fun runTaskTimer(context: SynchronizationContext) {
        currentTask?.cancel()
        val task: () -> Unit = { nextContinuation?.tryResume(interval) }
        currentTask = when (context) {
            SynchronizationContext.SYNC -> scheduler.runTaskTimer(plugin, task, 0L, interval)
            SynchronizationContext.ASYNCHRONOUS -> scheduler.runTaskTimerAsynchronously(plugin, task, 0L, interval)
        }
    }
}

private fun currentContext(): SynchronizationContext =
    if (Bukkit.isPrimaryThread()) SynchronizationContext.SYNC else SynchronizationContext.ASYNCHRONOUS