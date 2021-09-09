package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.util.IWorldPosCallable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.items.ItemStackHandler

internal class TableMenuProvider(
    private val tier: TableTier,
    private val handler: ItemStackHandler,
    private val level: World,
    private val pos: BlockPos
) : INamedContainerProvider {

    override fun createMenu(windowId: Int, playerInventory: PlayerInventory, player: PlayerEntity): Container =
        TableMenu(windowId, playerInventory, this.tier, this.handler, IWorldPosCallable.create(this.level, this.pos))

    override fun getDisplayName(): ITextComponent = TranslationTextComponent("container.crafting") // TODO("")
}
