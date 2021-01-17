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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelsAndExp(
    @SerialName("kills")
    @SerializedName("kills")
    val kills: Kills,
    @SerialName("block-breaks")
    @SerializedName("block-breaks")
    val blockBreaks: BlockBreaks,
    @SerialName("level-up")
    @SerializedName("level-up")
    val levelUp: LevelUp
) : java.io.Serializable {
    @Serializable
    data class Kills(
        @SerialName("exp-per-kill")
        @SerializedName("exp-per-kill")
        val expPerKill: Int,
        @SerialName("special-cases")
        @SerializedName("special-cases")
        val specialCases: List<String>
    ) : java.io.Serializable

    @Serializable
    data class BlockBreaks(
        @SerialName("exp-per-block-break")
        @SerializedName("exp-per-block-break")
        val expPerBlockBreak: Int,
        @SerialName("special-cases")
        @SerializedName("special-cases")
        val specialCases: List<String>
    ) : java.io.Serializable

    @Serializable
    data class LevelUp(
        @SerialName("max-tool-level")
        @SerializedName("max-tool-level")
        val maxToolLevel: Int,
        @SerialName("exp-for-first-level")
        @SerializedName("exp-for-first-level")
        val expForFirstLevel: Int,
        @SerialName("exp-curve-multiplier")
        @SerializedName("exp-curve-multiplier")
        val expCurveMultiplier: Double,
        @SerialName("exp-curve-exponent")
        @SerializedName("exp-curve-exponent")
        val expCurveExponent: Double,
        @SerialName("repair-on-level-up")
        @SerializedName("repair-on-level-up")
        val repairOnLevelUp: Boolean,
        @SerialName("extra-modifier-chance")
        @SerializedName("extra-modifier-chance")
        val extraModifierChance: Double,
        @SerialName("random-bonus-modifier-chance")
        @SerializedName("random-bonus-modifier-chance")
        val randomBonusModifierChance: Double,
        @SerialName("material-upgrades")
        @SerializedName("material-upgrades")
        val materialUpgrades: MaterialUpgrades
    ) : java.io.Serializable {
        @Serializable
        data class MaterialUpgrades(
            @SerialName("enabled")
            @SerializedName("enabled")
            val enabled: Boolean,
            @SerialName("material-upgrade-levels")
            @SerializedName("material-upgrade-levels")
            val materialUpgradeLevels: List<String>
        ) : java.io.Serializable
    }
}