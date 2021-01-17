package eu.warfaremc.tinker.listeners

import eu.warfaremc.tinker.miscellaneous.EffectTypes
import eu.warfaremc.tinker.miscellaneous.checkRecipe
import eu.warfaremc.tinker.miscellaneous.playEffect
import eu.warfaremc.tinker.model.TinkerTool
import eu.warfaremc.tinker.tinker
import org.bukkit.Material
import org.bukkit.block.Dispenser
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

@Deprecated("Redundant")
class BenchListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.on() {
        if(hand != null && hand != EquipmentSlot.HAND)
            return
        if(clickedBlock != null && clickedBlock!!.type == Material.DISPENSER) {
            if(clickedBlock!!.getRelative(0, -1, 0) is Dispenser) {
                val dispenser = clickedBlock!!.getRelative(0, -1, 0) as Dispenser
                val inventory = dispenser.inventory
                if(inventory.contents[0] != null && inventory.contents[1] != null) {
                    if(TinkerTool.isTinkerTool(inventory.contents[0])) {
                        val tool = TinkerTool.of(inventory.contents[0]) ?: return
                        if(TinkerTool.isRepairMaterial(tool, inventory.contents[1].type)) {
                            TinkerTool.repair(tool)
                            playEffect(EffectTypes.REPAIR, player, clickedBlock!!)
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

                tinker.recipes.forEachIndexed { index, shapedRecipe ->
                    if(checkRecipe(shapedRecipe, inventory)) {
                        recipe = shapedRecipe
                        return
                    }
                }

                if(recipe != null) {
                    playEffect(EffectTypes.CRAFT, player, clickedBlock!!)
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
}