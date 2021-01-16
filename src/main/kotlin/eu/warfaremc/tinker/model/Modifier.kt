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

package eu.warfaremc.tinker.model

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.Serializable

data class Modifier(
    val name: String,
    val lore: String,
    val maxLevel: Int,
    val maxRandomLevel: Int,
    val minLevel: Int = 0,
    val rarity: Double = 0.0,
    val item: ItemStack,
    val materials: List<Material> = emptyList(),
    var enabled: Boolean,
    var randomBonus: Boolean,
    var allowBench: Boolean
) : Serializable {
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
        fun getAllEnabled(): List<Modifier> {
            return getAll().filter { it.enabled }
        }

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
        fun addModifier(player: Player?, item: ItemStack?, name: String?) { addModifier(player, item, name, false) }

        @JvmStatic
        fun addModifier(player: Player?, item: ItemStack?, name: String?, bench: Boolean) {
            TODO()
        }

        fun addRandomModifier(player: Player?, item: ItemStack?) {
            if (player == null || item == null)
                return
            val modifiers = getAllEnabledRandom(item)
            if (modifiers.isEmpty())
                return
            val chances = hashMapOf<Modifier, Double>()
            var current = 0.0
            modifiers.forEach {  }
        }

        @JvmStatic
        fun addBenchModifier(player: Player?, item: ItemStack?, name: String?) {
            if (player == null || item == null || name == null)
                return
            TODO()
        }

        @JvmStatic
        fun getByLore(lore: String?): Modifier?
            = getAll().find { it.lore == lore }

    }
}