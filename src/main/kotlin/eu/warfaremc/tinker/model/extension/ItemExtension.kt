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

package eu.warfaremc.tinker.model.extension

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

inline fun item(type: Material, body: ItemStack.() -> Unit) =
    ItemStack(type).apply(body)

inline fun item(copy: ItemStack, body: ItemStack.() -> Unit) =
    ItemStack(copy).apply(body)

inline fun <reified T : ItemMeta> ItemStack.meta(body: T.() -> Unit) {
    val newMeta = itemMeta(type, body)
    itemMeta = newMeta
}

inline fun ItemStack.enchant(unsafe: Boolean = false, body: EnchantmentExtension.() -> Unit) {
    val addMethod = if (unsafe) ::addUnsafeEnchantment else ::addEnchantment
    EnchantmentExtension().apply(body).let {
        it.set.forEach { container ->
            val (enchantment, level) = container
            addMethod(enchantment, level)
        }
    }
}