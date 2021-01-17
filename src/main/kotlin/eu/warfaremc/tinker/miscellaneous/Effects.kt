package eu.warfaremc.tinker.miscellaneous

import eu.warfaremc.tinker.model.thread.SynchronizationContext
import eu.warfaremc.tinker.model.thread.schedule
import eu.warfaremc.tinker.tinker
import org.bukkit.Effect
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

import org.bukkit.Bukkit




fun playEffect(type: EffectTypes, player: Player, block: Block) {
    when (type) {
        EffectTypes.REPAIR -> {
            tinker.schedule(SynchronizationContext.ASYNCHRONOUS) {
                repeat(2) {
                    player.world.playSound(block.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f)
                    player.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 1f)
                    player.world.playEffect(block.location, Effect.ENDER_SIGNAL, 1f)

                    waitFor(20)
                }

                player.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 1f)
                player.world.playEffect(block.location, Effect.ENDER_SIGNAL, 1f)
                player.world.playSound(block.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
            }
        }
        EffectTypes.CRAFT -> {
            tinker.schedule(SynchronizationContext.ASYNCHRONOUS) {

                repeat(2){
                    player.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f)
                    player.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 1f)
                    player.getWorld().playEffect(block.getLocation(), Effect.ENDER_SIGNAL, 1f)

                    waitFor(20)
                }

                player.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 1f)
                player.getWorld().playEffect(block.getLocation(), Effect.ENDER_SIGNAL, 1f)
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)

            }
        }
    }

}

enum class EffectTypes {
    REPAIR, CRAFT
}