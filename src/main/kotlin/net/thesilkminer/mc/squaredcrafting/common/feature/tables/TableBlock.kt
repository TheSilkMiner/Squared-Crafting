package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.state.DirectionProperty
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader
import net.thesilkminer.mc.squaredcrafting.MOD_ID

internal class TableBlock(private val tier: TableTier, properties: Properties) : Block(properties) {
    companion object {
        val facingProperty: DirectionProperty = BlockStateProperties.HORIZONTAL_FACING
    }

    private val blockData get() = this.tier.blockData

    init {
        this.registerDefaultState(this.stateDefinition.any().setValue(facingProperty, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(facingProperty)
    }

    override fun hasTileEntity(state: BlockState?): Boolean {
        return if (this.blockData.hasInventory) true else super.hasTileEntity(state)
    }

    override fun createTileEntity(state: BlockState?, level: IBlockReader?): TileEntity? {
        return if (this.blockData.hasInventory) null else super.createTileEntity(state, level)
    }

    override fun appendHoverText(stack: ItemStack, level: IBlockReader?, tip: MutableList<ITextComponent>, advancedFlag: ITooltipFlag) {
        super.appendHoverText(stack, level, tip, advancedFlag)

        if (this.tier == TableTier.TEXTURE) {
            tip.add(TranslationTextComponent("$MOD_ID.tooltip.table.texture_fun"))
        } else if (this.tier == TableTier.HOLY_SHIT) {
            tip.add(TranslationTextComponent("$MOD_ID.tooltip.table.hope_and_pray"))
        }
    }
}
