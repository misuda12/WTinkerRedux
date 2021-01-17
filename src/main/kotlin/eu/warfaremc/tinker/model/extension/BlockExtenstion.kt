package eu.warfaremc.tinker.model.extension

import org.bukkit.block.Block

fun Block.isOfType(type: String?): Boolean {
    if(type == null)
        return false
    return this.type.name.contains("_${type.toUpperCase()}")
}
