package eu.warfaremc.tinker.model

import eu.warfaremc.tinker.model.extension.meta
import eu.warfaremc.tinker.model.extension.stringLore
import eu.warfaremc.tinker.tinker
import eu.warfaremc.tinker.model.extension.intValue
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.text.MessageFormat

class TinkerTool private constructor(val item: ItemStack) {
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
        fun isTinkerTool(item: ItemStack): Boolean {
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

    var experience: Int
        get() = this.get("experience", PersistentDataType.INTEGER)?: 0
        set(value) = this.set("experience", PersistentDataType.INTEGER, value).also { updateLore() }

    var durability: Int
        get() = this.get("durability", PersistentDataType.INTEGER)?: 0
        set(value) = this.set("durability", PersistentDataType.INTEGER, value).also { updateLore() }

    var level: Int
        get() = this.get("level", PersistentDataType.INTEGER) ?: 0
        set(value) = this.set("level", PersistentDataType.INTEGER, value).also { updateLore() }

    var modificationSpace: Int
        get() = this.get("modificationSpace", PersistentDataType.INTEGER) ?: 0
        set(value) = this.set("modificationSpace", PersistentDataType.INTEGER, value).also { updateLore() }

    var renamed: Boolean
        get() = (this.get("renamed", PersistentDataType.INTEGER) ?: 0) > 0
        set(value) = this.set("renamed", PersistentDataType.INTEGER, value.intValue).also { updateLore() }


    init {
        item.meta<ItemMeta> {
            isUnbreakable = true
        }
    }

    fun repair() {
        TODO()
    }

    private fun updateLore() {
        if(this.item.hasItemMeta())
            this.item.itemMeta!!.stringLore =
                MessageFormat.format(
                    TinkerTool.LORE_PATTERN,
                    TinkerTool.levelDescription(level),
                    experience,
                    modificationSpace,
                    "$durability / {${item.type.maxDurability}}"
                ) //TODO add modification
    }

    //TODO Rework
    private fun <T, Z> get(key: String?, type: PersistentDataType<T, Z>): Z? {
        if (key == null)
            return null
        if (!this.item.hasItemMeta())
            return null

        return this.item.itemMeta!!.persistentDataContainer.get(NamespacedKey(tinker, key), type)
    }

    //TODO Rework
    private fun <T, Z> set(key: String?, type: PersistentDataType<T, Z>, value: Z) {
        if (key == null)
            return
        if (!this.item.hasItemMeta())
            return

        this.item.itemMeta!!.persistentDataContainer.set(NamespacedKey(tinker, key), type, value)
    }

}