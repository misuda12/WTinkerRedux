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
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import eu.warfaremc.common.yaml.Yaml
import eu.warfaremc.tinker.model.TinkerToolEventHandler
import eu.warfaremc.tinker.model.extension.item
import eu.warfaremc.tinker.model.extension.meta
import eu.warfaremc.tinker.model.extension.name
import eu.warfaremc.tinker.model.extension.stringLore
import eu.warfaremc.tinker.model.serializable.Config
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import mu.KotlinLogging
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.ItemMeta
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

@PublishedApi
internal lateinit var recipe: List<ShapedRecipe>
    private set

@PublishedApi
internal lateinit var experienceChart: Map<Int, Int>
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

    override fun onEnable() {
        if (dataFolder.exists() == false)
        dataFolder.mkdirs().also { logger.info { "[IO] dataFolder ~'${dataFolder.path}' created" } }
        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()
        if (server.pluginManager.isPluginEnabled("Slimefun") == false) {
            server.pluginManager.disablePlugin(this)
            logger.error { "Slimefun has not been loaded properly, plugin is disabling ..." }
            return
        }
        val configuration = Yaml.default.decodeFromString(Config.serializer(), config.saveToString())
        logger.info { configuration.toString() }
        val executionCoordinatorFunction =
            AsynchronousCommandExecutionCoordinator.newBuilder<CommandSender>()
                .withSynchronousParsing()
                .build()
        try {
            commandManager = PaperCommandManager(
                this,
                executionCoordinatorFunction,
                ::identity,
                ::identity
            )
        } catch (exception: Exception) {
            logger.error { "Failed to initialize CommandFramework::CommandManager" }
        }
        finally {
            audiences = BukkitAudiences.create(this)
            commandHelp = MinecraftHelp("/prestige help", audiences::sender, commandManager)
            if (commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER))
                commandManager.registerBrigadier()
            if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
                commandManager.registerAsynchronousCompletions()
            val commandMetaFunction: java.util.function.Function<ParserParameters, CommandMeta> =
                java.util.function.Function<ParserParameters, CommandMeta> { parser ->
                    CommandMeta.simple()
                        .with(CommandMeta.DESCRIPTION, parser.get(StandardParameters.DESCRIPTION, "No description"))
                        .build()
                }
            commandAnnotation = AnnotationParser(
                commandManager,
                CommandSender::class.java,
                commandMetaFunction
            )
            commandAnnotation.parse(this)
            logger.info { "Successfully installed CommandFramework Cloud 1.4" }
        }

        TinkerToolEventHandler.initHandler()
    }

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

    @CommandMethod("tt give <player> <material>")
    @CommandDescription("Gives you usable tinker tool")
    @CommandPermission("tinkertools.admin")
    fun commandGive(
        sender: Player,
        @NotNull @Argument("player") player: Player,
        @NotNull @Argument("material") material: Material
    ) {
        val tool = item(material) {
            meta<ItemMeta> {
                name = "§f" + TODO("toProperCase")
                stringLore = """
                    
                """.trimIndent()
            }
        }
        val leftOver = hashMapOf<Int, ItemStack>()
        leftOver.putAll(player.inventory.addItem(tool))
        if (leftOver.isNotEmpty())
            player.world.dropItem(player.location, tool)
        sender.sendMessage("")
    }

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