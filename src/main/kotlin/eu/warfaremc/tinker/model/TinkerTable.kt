package eu.warfaremc.tinker.model

import eu.warfaremc.tinker.miscellaneous.playEffect
import eu.warfaremc.tinker.tinker
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine
import me.mrCookieSlime.Slimefun.Objects.Category
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem
import me.mrCookieSlime.Slimefun.api.Slimefun
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Dispenser
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

class TinkerTable(
    category: Category?,
    item: SlimefunItemStack?,
    recipe: Array<out ItemStack>?,
    machineRecipes: Array<out ItemStack>?,
    trigger: BlockFace?
) : MultiBlockMachine(category, item, recipe, machineRecipes, trigger) {

    override fun onInteract(player: Player?, block: Block?) {
        if(block == null || player == null)
            return

        val machine = SlimefunItem.getByID("TINKER_TABLE")
        if(Slimefun.hasUnlocked(player, machine, true)) { //TODO
            var dispenser: Dispenser? = null
            if(block.getRelative(0, -1, 0) is Dispenser)
                dispenser = block.getRelative(0, -1, 0) as Dispenser
            else
                return
            var inventory = dispenser.inventory;

            if(inventory.contents[0] != null && inventory.contents[1] != null) {
                if(TinkerTool.isTinkerTool(inventory.contents[0])) {
                    val tool = TinkerTool.of(inventory.contents[0]) ?: return
                    if(TinkerTool.isRepairMaterial(tool, inventory.contents[1].type)) {
                        tool.repair()
                        playEffect(player, block)
                    } else {
                        if((tool.modificationSpace ?: 0) > 0) {
                            player.sendMessage("added mod") // remove with finished todo
                            TODO("Add mod")
                        } else {
                            player.sendMessage("§cSorry, that tool doesn't have enough extra modifier space")
                        }
                    }
                }
            }

            var recipe: ShapedRecipe? = null
/*
            tinker.recipes.forEach {
                if(checkRecipe(it, inventory)) {
                    recipe = it
                    return
                }
            }
*/
            if(recipe != null) {
                playEffect(player, block)
                // Replace all itemstacks in dispenser inventory with the same itemstacks with one less item, or null if amount is 1
                inventory.forEachIndexed { index, itemStack ->
                    if(itemStack == null)
                        return
                    var replace: ItemStack?

                    if(recipe!!.ingredientMap.containsValue(inventory.contents[index])) // make sure we used that ingredient in recipe
                        return

                    if(inventory.contents[index].amount < 1)
                        replace = null
                    else
                        replace = ItemStack(itemStack).apply { amount -= 1 }

                    inventory.setItem(index, replace)
                }

                player.inventory.addItem(recipe!!.result)
            }
        }
    }
}