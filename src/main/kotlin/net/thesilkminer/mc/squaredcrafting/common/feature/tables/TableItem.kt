package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack

internal class TableItem(private val tier: TableTier, block: Block, properties: Properties)
    : BlockItem(block, properties) {
    private val itemData get() = this.tier.itemData

    override fun isFoil(pStack: ItemStack): Boolean = this.itemData.hasFoil
}
