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

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

inline fun <reified T : ItemMeta> itemMeta(material: Material, body: T.() -> Unit) =
    Bukkit.getItemFactory().getItemMeta(material)
        .let { it as? T }
        ?.apply(body)
        ?: throw IllegalArgumentException("ItemMeta for provided material does not match actual type parameters")

var ItemMeta.stringLore: String?
    get() = lore?.joinToString("\n")
    set(value) {
        lore = value?.split("\n".toRegex())
    }

var ItemMeta.name: String?
    get() = if (hasDisplayName()) displayName else null
    set(value) {
        setDisplayName(value)
    }

var ItemMeta.modelData: Int?
    get() = if (hasCustomModelData()) customModelData else null
    set(value) {
        setCustomModelData(value)
    }

fun ItemMeta.flag(vararg flags: ItemFlag) = addItemFlags(*flags)

fun ItemMeta.flag(flag: ItemFlag) = addItemFlags(flag)

inline fun ItemMeta.attributes(body: Attributes.() -> Unit) {
    val attributes = Attributes().apply(body)
    val modifiers = attributes.modifiers
    attributeModifiers = ArrayListMultimap.create(
        if (attributeModifiers == null) ArrayListMultimap.create() else attributeModifiers!!
    ).also { it.putAll(modifiers) }
}

inline fun ItemMeta.enchant(ignoringRestrictions: Boolean = false, body: EnchantmentExtension.() -> Unit) {
    EnchantmentExtension().apply(body).set.forEach {
        addEnchant(it.enchantment, it.level, ignoringRestrictions)
    }
}

class Attributes {

    private val _modifiers: Multimap<Attribute, AttributeModifier> = ArrayListMultimap.create()
    val modifiers: Multimap<Attribute, AttributeModifier>
        get() = ArrayListMultimap.create(_modifiers)

    fun modify(attribute: Attribute) = ModifierNode(attribute)

    inner class ModifierNode internal constructor(private val attribute: Attribute) {
        infix fun with(modifier: AttributeModifier) {
            _modifiers.put(attribute, modifier)
        }

        infix fun with(modifiers: Iterable<AttributeModifier>) {
            this@Attributes._modifiers.putAll(attribute, modifiers)
        }
    }
}