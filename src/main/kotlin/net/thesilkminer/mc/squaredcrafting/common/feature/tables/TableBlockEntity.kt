package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraft.block.BlockState
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemStackHandler
import net.thesilkminer.mc.squaredcrafting.common.allTablesBlockEntity

internal class TableBlockEntity(tier: TableTier? = null) : TileEntity(allTablesBlockEntity) {
    private class TableItemHandler(private val blockEntity: TableBlockEntity) : ItemStackHandler() {
        init {
            // TODO("This is eww, but it works as a test")
            this.setSize(this.blockEntity._tier?.makeHandler()?.slots ?: 1)
        }

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            this.blockEntity.onInventoryChange(slot, this.getStackInSlot(slot))
        }
    }

    private companion object {
        private const val TIER_NAME_KEY = "TierName"
        private const val INVENTORY_KEY = "Inventory"
    }

    private var _tier = tier

    // TODO("Boson this Triple out")
    private val inventory = with (TableItemHandler(this)) {
        val optional: LazyOptional<IItemHandler> = LazyOptional.of { this }
        Triple(this, optional, Direction.values().associateWith { optional })
    }
    private val rawInventory: ItemStackHandler get() = this.inventory.first

    internal val tier: TableTier get() = this._tier ?: error("Tier was not yet set: this should be impossible")

    private fun onInventoryChange(slot: Int, stack: ItemStack) {
        this.setChanged()
    }

    internal fun makeMenuProvider(): INamedContainerProvider =
        TableMenuProvider(this.tier, this.rawInventory, this.level!!, this.blockPos)

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> = when (cap) {
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> when (side) {
            null -> inventory.second.cast()
            else -> inventory.third.getValue(side).cast()
        }
        else -> super.getCapability(cap, side)
    }

    override fun setRemoved() {
        super.setRemoved()
        this.inventory.second.invalidate()
        this.inventory.third.values.forEach(LazyOptional<*>::invalidate)
    }

    override fun save(tag: CompoundNBT): CompoundNBT {
        val superTag = super.save(tag)
        superTag.putString(TIER_NAME_KEY, this.tier.registryName)
        superTag.put(INVENTORY_KEY, this.rawInventory.serializeNBT())
        return superTag
    }

    override fun load(state: BlockState, tag: CompoundNBT) {
        super.load(state, tag)
        this._tier = tag.getString(TIER_NAME_KEY).let { name ->
            TableTier.values().find { it.registryName == name } ?: error("Invalid tier $name")
        }
        this.rawInventory.deserializeNBT(tag.getCompound(INVENTORY_KEY))
    }
}
