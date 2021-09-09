@file:JvmName("0_TableTierExtensions")

package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraftforge.items.ItemStackHandler

internal fun TableTier.makeHandler(): ItemStackHandler =
    ItemStackHandler(this.size.toInt().let { it * it })
