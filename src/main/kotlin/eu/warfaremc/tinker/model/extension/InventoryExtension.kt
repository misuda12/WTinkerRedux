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

import eu.warfaremc.tinker.model.thread.schedule
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

const val ROW_SIZE = 9
typealias Slot = Pair<Int, Int>

inline fun inventory(
    rows: Int = 3,
    owner: InventoryHolder? = null,
    title: String = InventoryType.CHEST.defaultTitle,
    body: Inventory.() -> Unit
) = Bukkit.createInventory(owner, ROW_SIZE * rows, title).apply(body)

fun slot(slot: Slot) =
    slot(slot.first, slot.second)

fun slot(row: Int, column: Int): Int {
    Validate.isTrue(row >= 0, "Row must not be negative")
    Validate.isTrue(column in 0 until ROW_SIZE, "Column must be >= 0 and < $ROW_SIZE")
    return row * ROW_SIZE + column
}

fun slots(vararg slots: Slot): Iterable<Int> =
    slots.map(::slot)

fun Int.toSlot(): Slot =
    this / ROW_SIZE to this % ROW_SIZE

infix fun <T> Iterable<T>.except(element: T) =
    this - element

infix fun <T> Iterable<T>.except(elements: Iterable<T>) =
    this - elements

inline fun Inventory.forEachSlot(action: (Int, Int) -> Unit) {
    forEachSlotLinear {
        val (row, column) = it.toSlot()
        action(row, column)
    }
}

inline fun Inventory.forEachSlotLinear(action: (Int) -> Unit) {
    for (slot in all)
        action(slot)
}

fun items(formatString: String, bindings: Map<Char, ItemStack?>): Array<ItemStack?> {
    val rows = formatString.split("\n".toRegex())
    return rows.asSequence()
        .map { it.toCharArray() }
        .map { it.map(bindings::get) }
        .reduce { one, two -> one + two }
        .toTypedArray()
}

operator fun Inventory.get(index: Int): ItemStack? = contents[index]

operator fun Inventory.get(indices: Iterable<Int>) = indices.map { this[it] }

operator fun Inventory.set(index: Int, item: ItemStack?) {
    this.setItem(index, item)
}

operator fun Inventory.set(indices: Iterable<Int>, item: ItemStack?) {
    for (i in indices) {
        this[i] = item
    }
}

operator fun Inventory.set(indices: Iterable<Int>, items: Iterable<ItemStack?>) {
    val indexIterator = indices.iterator()
    val itemIterator = items.iterator()
    while (indexIterator.hasNext() && itemIterator.hasNext()) {
        this[indexIterator.next()] = itemIterator.next()
    }
}

operator fun Inventory.plusAssign(item: ItemStack) {
    this.addItem(item)
}

operator fun Inventory.plusAssign(items: Iterable<ItemStack>) {
    this.addItem(*items.toList().toTypedArray())
}

operator fun Inventory.minusAssign(item: ItemStack) {
    this.remove(item)
}

operator fun Inventory.minusAssign(items: Iterable<ItemStack>) {
    this.removeItem(*items.toList().toTypedArray())
}

operator fun Inventory.minusAssign(material: Material) {
    this.remove(material)
}

fun Inventory.openTo(plugin: Plugin, player: Player) = plugin.schedule { player.openInventory(this@openTo) }

fun Inventory.row(index: Int): IntRange {
    checkBounds(index, 0 until rows, "Row")
    val start = slot(index, 0)
    return start until start + ROW_SIZE
}

fun Inventory.column(index: Int): IntProgression {
    checkBounds(index, 0 until 9, "Column")
    val start = slot(0, index)
    return start until (lastRowIndex * ROW_SIZE + index + 1) step 9
}

fun Inventory.borders(padding: Int = 0): Iterable<Int> {
    Validate.isTrue(padding >= 0, "Padding must not be negative")
    val borders = mutableSetOf<Int>()
    for (i in 0..padding) {
        borders.addAll(row(0 + i))
        borders.addAll(row(lastRowIndex - i))
        borders.addAll(column(0 + i))
        borders.addAll(column(8 - i))
    }
    return borders
}

val Inventory.corners: Iterable<Int>
    get() = slots(0 to 0, 0 to ROW_SIZE - 1, lastRowIndex to 0, lastRowIndex to ROW_SIZE - 1)

val Inventory.lastRowIndex: Int
    get() = rows - 1

val Inventory.all: IntRange
    get() = contents.indices

val Inventory.rows: Int
    get() = size / ROW_SIZE

private fun checkBounds(index: Int, bounds: IntRange, name: String) {
    if (index !in bounds)
        throw IndexOutOfBoundsException("$name index out of bounds")
}