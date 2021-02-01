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

package eu.warfaremc.tinker.model.serializable

import com.google.gson.annotations.SerializedName
import eu.warfaremc.tinker.model.TinkerTool
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@Serializable
data class Modifier(
    @SerialName("enabled")
    @SerializedName("enabled")
    val enabled: Boolean,
    @SerialName("random-bonus")
    @SerializedName("random-bonus")
    val randomBonus: Boolean,
    @SerialName("random-bonus-rarity")
    @SerializedName("random-bonus-rarity")
    val randomBonusRarity: Double,
    @SerialName("allow-bench")
    @SerializedName("allow-bench")
    val allowBench: Boolean,
    @SerialName("mod-item")
    @SerializedName("mod-item")
    val material: Material,
    @SerialName("max-level")
    @SerializedName("max-level")
    val maxLevel: Int,
    @SerialName("max-random-bonus-level")
    @SerializedName("max-random-bonus-level")
    val maxRandomBonusLevel: Int,
    @SerialName("mod-identifier")
    @SerializedName("mod-identifier")
    val identifier: String,
    @SerialName("allowed-tools")
    @SerializedName("allowed-tools")
    val allowedTools: List<Material>
) : java.io.Serializable {

    var lore: String? = null

    companion object {

        @JvmStatic
        operator fun get(name: String): Modifier? {
            TODO()
        }

        @JvmStatic
        fun getAll(): List<Modifier> {
            TODO()
        }

        @JvmStatic
        fun getAllEnabled(): List<Modifier> = getAll().filter { it.enabled }

        @JvmStatic
        fun getAllEnabledBench(item: ItemStack?): List<Modifier> {
            if (item == null)
                return emptyList()
            TODO()
        }

        @JvmStatic
        fun getAllEnabledRandom(item: ItemStack?): List<Modifier> {
            if (item == null)
                return emptyList()
            TODO()
        }

        @JvmStatic
        fun addModifier(player: Player?, item: ItemStack?, name: String?) {
            addModifier(player, item, name, false)
        }

        @JvmStatic
        fun addModifier(player: Player?, item: ItemStack?, name: String?, bench: Boolean) {
            TODO()
        }

        @JvmStatic
        fun addRandomModifier(player: Player?, item: ItemStack?) {
            if (player == null || item == null)
                return
            val modifiers = getAllEnabledRandom(item)
            if (modifiers.isEmpty())
                return
            val chances = hashMapOf<Modifier, Double>()
            var next = 0.0
            val prev = 0.0
            modifiers.forEach { next += it.randomBonusRarity; chances[it] = next }
            val random = Random().nextDouble() * next
            modifiers.forEach {
                if (random > prev && random <= chances[it]!!) {
                    addModifier(player, item, it.lore, false)
                    return
                }
            }
        }

        @JvmStatic
        fun addBenchModifier(player: Player?, item: ItemStack?, name: String?) {
            if (player == null || item == null || name == null)
                return
            val tool = TinkerTool(item)
            TODO()
        }

        @JvmStatic
        fun getModifierFromLore(lore: String?): Modifier? = getAll().find { it.lore == lore }
    }
}