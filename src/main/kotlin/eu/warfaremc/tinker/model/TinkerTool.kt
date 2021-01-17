package eu.warfaremc.tinker.model

import eu.warfaremc.tinker.model.extension.isOfType
import eu.warfaremc.tinker.model.extension.meta
import eu.warfaremc.tinker.model.extension.stringLore
import eu.warfaremc.tinker.tinker
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.SmithingInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.text.MessageFormat
import java.util.*

class TinkerTool constructor(val item: ItemStack) {
    companion object {
        private const val LEGACY_TOOL_IDENTIFIER = "&dTinker Tool"
        private const val LEGACY_BROKEN_TOOL_IDENTIFIER = "&4BROKEN TOOL"
        private const val LORE_PATTERN = """
                    §7Skill Level: {0}\n
                    §7Exp: {1}\n
                    §7Zbývá modifierů: {2}\n
                    §7Durabilita: {3}\n
                    §7Modifiery: {4}\n
        """

        @JvmStatic
        fun levelDescription(level: Int): String? {
            TODO("get from cfg")
        }

        @JvmStatic
        fun isTinkerTool(item: ItemStack?): Boolean {
            if (item == null)
                return false

            return isLegacy(item) ||
                    (item.hasItemMeta() &&
                            item.itemMeta!!.persistentDataContainer.has(
                                NamespacedKey(tinker, "level"),
                                PersistentDataType.INTEGER
                            ))
        }

        @JvmStatic
        fun isRepairMaterial(tool: TinkerTool, type: Material): Boolean {
            val repairMat: Material = when (tool.item.type) {
                Material.BOW, Material.WOODEN_SWORD, Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL -> Material.OAK_WOOD
                Material.STONE_SWORD, Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL -> Material.STONE
                Material.IRON_SWORD, Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL -> Material.IRON_BLOCK
                Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL -> Material.DIAMOND_BLOCK
                else -> return false
            }
            return repairMat == type
        }

        fun fixLegacy(item: ItemStack): ItemStack? {
            TODO()
        }

        @JvmStatic
        fun isLegacy(item: ItemStack): Boolean {
            return if (item.hasItemMeta() && item.itemMeta!!.hasLore())
                item.itemMeta!!.lore!![0].contains(LEGACY_TOOL_IDENTIFIER) ||
                        item.itemMeta!!.lore!![0].contains(LEGACY_BROKEN_TOOL_IDENTIFIER)
            else
                false
        }

        @JvmStatic
        fun of(item: ItemStack): TinkerTool? {
            val fixed: ItemStack
            if (isTinkerTool(item)) {
                fixed = if (isLegacy(item))
                    fixLegacy(item) ?: return null
                else
                    item
                return TinkerTool(fixed)
            }
            return null
        }

    }

    var uuid: UUID
        private set

    var broken: Boolean = false
        get() = if(durability < 0) false else durability - wear <= 0
        private set

    var experience: Int
        get() = this.get("experience") as Int
        set(value) = this.set("experience", value).also { update() }

    var durability: Int = -1

    var wear: Int
        get() = this.get("durability") as Int
        set(value) = this.set("durability", value).also { update() }

    var level: Int
        get() = this.get("level") as Int ?: 0
        set(value) = this.set("level", value).also { update() }

    var modificationSpace: Int
        get() = this.get("modificationSpace") as Int
        set(value) = this.set("modificationSpace", value).also { update() }

    var renamed: Boolean
        get() = this.get("renamed") as Boolean
        set(value) = this.set("renamed", value).also { update() }


    init {
        item.meta<ItemMeta> {
            isUnbreakable = true
        }
        durability = item.type.maxDurability.toInt()
        uuid = item.itemMeta!!.persistentDataContainer.get(NamespacedKey(tinker, "uuid"), PersistentDataType.BYTE_ARRAY).let {
            try {
                val buffer = ByteBuffer.wrap(it)
                UUID(buffer.long, buffer.long)
            } catch (exception: BufferUnderflowException) {
                UUID.randomUUID()
            }
        }
    }

    fun repair() {
        wear = 0
    }

     private fun update() {
        if (wear >= item.type.maxDurability)
            TODO("break")

        if (this.item.hasItemMeta())
            this.item.itemMeta!!.stringLore =
                MessageFormat.format(
                    TinkerTool.LORE_PATTERN,
                    TinkerTool.levelDescription(level),
                    experience,
                    modificationSpace,
                    "$wear / {${item.type.maxDurability}}"
                ) //TODO add modification
    }

    private fun get(key: String?): Any? {
        TODO("Rework")
    }

    private fun set(key: String?, value: Any?) {
        TODO("Rework")
    }
}

class TinkerToolEventHandler : Listener {
    companion object {
        fun initHandler() = Bukkit.getPluginManager().registerEvents(TinkerToolEventHandler(), tinker)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun PlayerInteractEvent.handle() {
        if (action == Action.RIGHT_CLICK_BLOCK)
            if (item != null)
                if (clickedBlock != null)
                    if (TinkerTool.isTinkerTool(item))
                        if (item!!.isOfType("AXE")) {
                            if (
                                clickedBlock!!.isOfType("LOG")
                                || clickedBlock!!.isOfType("WOOD")
                                || clickedBlock!!.type == Material.WARPED_HYPHAE
                                || clickedBlock!!.type == Material.WARPED_STEM
                                || clickedBlock!!.type == Material.CRIMSON_STEM
                                || clickedBlock!!.type == Material.CRIMSON_HYPHAE
                            ) {
                                isCancelled = true
                            }

                            if (item!!.isOfType("SHOVEL")) {
                                if (
                                    clickedBlock!!.type == Material.GRASS_BLOCK || clickedBlock!!.type == Material.GRASS_PATH
                                ) {
                                    isCancelled = true
                                }
                            }
                        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun InventoryClickEvent.handle() {
        if (clickedInventory is SmithingInventory)
            if (TinkerTool.isTinkerTool(cursor)) {
                isCancelled = true
                whoClicked.sendMessage("§cTuto akci nelze provádět s Tinker Toolem.")
            }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.handle() {
        isCancelled = handleCommonToolInteraction(player)

        if(isCancelled)
            return

        if (TinkerTool.isTinkerTool(player.inventory.itemInMainHand))
            return //TODO("item in off hand fix")

        if (block.type.hardness == 0f)
            return
        //TODO("Special exp cases")

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun EntityShootBowEvent.handle() {
        if(entity is Player)
            isCancelled = handleCommonToolInteraction(entity as Player)

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun EntityDamageByEntityEvent.handle() {
        if(damager is Player)
            isCancelled = handleCommonToolInteraction(damager as Player)
    }

    private fun handleCommonToolInteraction(player: Player): Boolean {
        val tool = TinkerTool.of(player.inventory.itemInMainHand) ?: return false

        if (tool.broken) {
            player.sendMessage("§cTento item je rozbitý! Oprav ho v Tinker Tablu.")
            return true
        }
        tool.wear += 1

       if(tool.broken)
           player.sendMessage("§cItem se rozbil.")

       player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
       return false
    }
}