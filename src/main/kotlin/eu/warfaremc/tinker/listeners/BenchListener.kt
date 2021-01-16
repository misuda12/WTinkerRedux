package eu.warfaremc.tinker.listeners

import eu.warfaremc.tinker.miscellaneous.playRepairEffect
import eu.warfaremc.tinker.model.TinkerTool
import org.bukkit.Material
import org.bukkit.block.Dispenser
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

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
                            playRepairEffect(player, clickedBlock!!)
                        } else {
                            if((tool.modificationSpace ?: 0) > 0) {
                                TODO("Add mod")
                            } else {
                                player.sendMessage("§cSorry, that tool doesn't have enough extra modifiers")
                            }
                        }
                    }
                }
            }
        }

        TODO("Recipes")


    }

}