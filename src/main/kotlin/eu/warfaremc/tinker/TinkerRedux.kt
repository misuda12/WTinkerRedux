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

package eu.warfaremc.tinker

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.specifier.Greedy
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import mu.KotlinLogging
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*
import java.util.concurrent.TimeUnit

@PublishedApi
internal lateinit var tinker: TinkerRedux
    private set

@PublishedApi
internal lateinit var kguava: Cache<Any, Any>
    private set

class TinkerRedux : JavaPlugin(), CoroutineScope by MainScope() {

    val logger by lazy { KotlinLogging.logger("WTinker") }
    internal val session = UUID.randomUUID().toString()

    @PublishedApi
    internal var slimefun = object : SlimefunAddon {
        override fun getJavaPlugin(): JavaPlugin
            = this@TinkerRedux

        override fun getBugTrackerURL(): String
            = "https://github.com/misuda12/WTinkerRedux/issues"
    }

    init {
        tinker = this
        kguava = CacheBuilder.newBuilder()
            .expireAfterWrite(Long.MAX_VALUE, TimeUnit.DAYS)
            .build()
    }

    // Command stuff
    lateinit var audiences: BukkitAudiences
    lateinit var commandManager: PaperCommandManager<CommandSender>
    lateinit var commandAnnotation: AnnotationParser<CommandSender>
    lateinit var commandHelp: MinecraftHelp<CommandSender>

    override fun onDisable() {  }

    @CommandMethod("tt")
    @CommandDescription("Basic set of commands for WTinkerRedux")
    fun command(
        sender: Player
    ) {
        commandHelp.queryCommands("", sender)
    }

    @CommandMethod("tt help [query]")
    @CommandDescription("Shows commands of WTinkerRedux")
    fun commandHelp(
        sender: Player,
        @Nullable @Argument("query") @Greedy query: String?
    ) {
        commandHelp.queryCommands(query ?: "", sender)
    }

    @CommandMethod("tt give <player> <name>")
    @CommandDescription("Gives you usable tinker tool")
    @CommandPermission("tinkertools.admin")
    fun commandGive(
        sender: Player,
        @NotNull @Argument("player") player: Player,
        @NotNull @Argument("name") name: String
    ) { TODO() }

    @CommandMethod("tt name <name>")
    @CommandDescription("Renames current holding tinker tool")
    @CommandPermission("tinkertools.name")
    fun commandName(
        sender: Player,
        @NotNull @Argument("name") name: String
    ) { TODO() }

    @CommandMethod("tt addexp <amount>")
    @CommandDescription("Adds more exp to tinker tool")
    @CommandPermission("tinkertools.admin")
    fun commandAddExp(
        sender: Player,
        @NotNull @Argument("amount") name: Int
    ) { TODO() }

    @CommandMethod("tt reload")
    @CommandDescription("Reloads WTinkerRedux")
    @CommandPermission("tinkertools.admin")
    fun commandReload(
        sender: Player
    ) { TODO() }
}

fun <T> identity(t: T): T = t