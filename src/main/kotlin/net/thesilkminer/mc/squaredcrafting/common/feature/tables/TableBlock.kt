package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.state.DirectionProperty
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Direction
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.IBooleanFunction
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks
import net.thesilkminer.mc.squaredcrafting.MOD_ID

internal class TableBlock(private val tier: TableTier, properties: Properties) : Block(properties) {
    companion object {
        val facingProperty: DirectionProperty = BlockStateProperties.HORIZONTAL_FACING

        // I wish this would be DSLd
        private val shape = VoxelShapes.join(
            box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0),
            VoxelShapes.join(
                box(1.0, 1.0, 0.0, 15.0, 5.0, 16.0),
                box(0.0, 1.0, 1.0, 16.0, 5.0, 15.0),
                IBooleanFunction.OR
            ),
            IBooleanFunction.NOT_SAME
        )
    }

    private val blockData get() = this.tier.blockData

    init {
        this.registerDefaultState(this.stateDefinition.any().setValue(facingProperty, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(facingProperty)
    }

    override fun use(state: BlockState, level: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
        if (level.isClientSide) return ActionResultType.SUCCESS
        if (player !is ServerPlayerEntity) return ActionResultType.SUCCESS
        NetworkHooks.openGui(player, state.getMenuProvider(level, pos)) {
            it.writeUtf(this.tier.registryName, 15)
        }
        return ActionResultType.SUCCESS
    }

    override fun getMenuProvider(state: BlockState, level: World, pos: BlockPos): INamedContainerProvider? {
        if (this.blockData.hasInventory) {
            return (level.getBlockEntity(pos) as? TableBlockEntity)?.makeMenuProvider()
        }
        return TableMenuProvider(this.tier, this.tier.makeHandler(), level, pos)
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
            tip.add(TranslationTextComponent("$MOD_ID.tooltip.table.texture_fun").withStyle { it.withItalic(true) })
        } else if (this.tier == TableTier.HOLY_SHIT) {
            tip.add(TranslationTextComponent("$MOD_ID.tooltip.table.hope_and_pray").withStyle { it.withColor(TextFormatting.RED) })
        }
    }

    override fun getShape(state: BlockState, level: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape = shape
    override fun useShapeForLightOcclusion(pState: BlockState): Boolean = true
}
