package eu.warfaremc.tinker.miscellaneous

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

fun isItemSimilar(item: ItemStack?, SFitem: ItemStack?, lore: Boolean, data: DataType): Boolean {
    if(item == null)
        return SFitem == null
    if(SFitem == null)
        return false
    //TODO()
    return false
}

fun checkRecipe(recipe: ShapedRecipe, contents: Iterable<ItemStack>): Boolean {
    TODO()
}

enum class DataType {
    ALWAYS,
    NEVER,
    IF_COLORED;
}