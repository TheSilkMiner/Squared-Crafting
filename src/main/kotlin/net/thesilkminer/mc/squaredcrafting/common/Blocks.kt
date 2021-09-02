@file:JvmName("0_Blocks")

package net.thesilkminer.mc.squaredcrafting.common

import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraftforge.registries.ForgeRegistries
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableBlock
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import net.thesilkminer.mc.squaredcrafting.logger
import net.thesilkminer.mc.squaredcrafting.registrationMarker
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate

//region Registration Helpers
private val blocksDeferredRegister = KDeferredRegister(ForgeRegistries.BLOCKS, MOD_ID)

internal fun KotlinEventBus.attachBlocksHandler() = blocksDeferredRegister.register(this)

private fun KDeferredRegister<Block>.makeTable(tier: TableTier): ObjectHolderDelegate<out Block> {
    val block = this.registerObject("${tier.registryName}_crafting_table") {
        TableBlock(tier, tier.blockProperties)
    }
    block.withTableBlockItem(tier)
    logger.debug(registrationMarker, "Registered table block {} for tier {}", block, tier)
    return block
}

private val TableTier.blockProperties: AbstractBlock.Properties get() = this.blockData.let { data ->
    AbstractBlock.Properties.of(data.material).apply {
        this.strength(data.strength.toFloat())
        this.sound(data.soundType)
        if (data.requiresTool) this.requiresCorrectToolForDrops()
        this.harvestLevel(data.toolLevel)
        this.harvestTool(data.tool)
    }
}
//endregion

internal val tables = TableTier.values().associateWith(blocksDeferredRegister::makeTable)

