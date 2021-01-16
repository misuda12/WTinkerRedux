package eu.warfaremc.tinker.miscellaneous

import eu.warfaremc.tinker.model.thread.SynchronizationContext
import eu.warfaremc.tinker.model.thread.schedule
import eu.warfaremc.tinker.tinker
import org.bukkit.Effect
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player

fun playRepairEffect(player: Player, block: Block) {
    tinker.schedule(SynchronizationContext.ASYNCHRONOUS) {
        player.world.playSound(block.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f)
        player.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 1f)
        player.world.playEffect(block.location, Effect.ENDER_SIGNAL, 1f)

        waitFor(20)

        player.world.playSound(block.location, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f)
        player.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 1f)
        player.world.playEffect(block.location, Effect.ENDER_SIGNAL, 1f)

        waitFor(20)

        player.world.playEffect(block.location, Effect.MOBSPAWNER_FLAMES, 1f)
        player.world.playEffect(block.location, Effect.ENDER_SIGNAL, 1f)
        player.world.playSound(block.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
    }
}