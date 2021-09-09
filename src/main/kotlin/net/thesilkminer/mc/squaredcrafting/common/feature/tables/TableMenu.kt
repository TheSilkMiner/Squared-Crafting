package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import com.mojang.datafixers.util.Pair as DfuPair

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.IWorldPosCallable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler
import net.thesilkminer.mc.squaredcrafting.common.tableMenuType
import net.thesilkminer.mc.squaredcrafting.common.tables

internal class TableMenu(
    windowId: Int,
    private val playerInventory: PlayerInventory,
    internal val tier: TableTier,
    private val tableInventory: ItemStackHandler,
    private val levelAccess: IWorldPosCallable
) : Container(tableMenuType, windowId) {
    private class ArmorSlot(private val inventory: PlayerInventory, private val type: EquipmentSlotType, x: Int, y: Int) : Slot(inventory, 36 + type.index, x, y) {
        @Suppress("SpellCheckingInspection")
        private companion object {
            private val armorIconsAtlas = ResourceLocation("minecraft", "textures/atlas/blocks.png")
            private val armorIcons = mapOf(
                EquipmentSlotType.HEAD to ResourceLocation("minecraft", "item/empty_armor_slot_helmet"),
                EquipmentSlotType.CHEST to ResourceLocation("minecraft", "item/empty_armor_slot_chestplate"),
                EquipmentSlotType.LEGS to ResourceLocation("minecraft", "item/empty_armor_slot_leggings"),
                EquipmentSlotType.FEET to ResourceLocation("minecraft", "item/empty_armor_slot_boots")
            )
        }

        override fun getMaxStackSize(): Int = 1
        override fun mayPlace(pStack: ItemStack): Boolean = pStack.canEquip(this.type, this.inventory.player)

        override fun mayPickup(pPlayer: PlayerEntity): Boolean =
            pPlayer.isCreative || this.item.isEmpty || !EnchantmentHelper.hasBindingCurse(this.item)

        override fun getNoItemIcon(): DfuPair<ResourceLocation, ResourceLocation>? =
            DfuPair.of(armorIconsAtlas, armorIcons.getValue(this.type))
    }

    constructor(
        windowId: Int,
        playerInventory: PlayerInventory,
        tier: TableTier
    ) : this(windowId, playerInventory, tier, tier.makeHandler(), IWorldPosCallable.NULL)

    // private val recipeType get() = TODO("Figure out best way for recipe type")
    private val player get() = this.playerInventory.player
    internal val containerData = cData.getValue(this.tier)

    init {
        (0U until this.tier.size.toUInt()).forEach { j ->
            val y = this.containerData.input.y + this.containerData.inSize * j
            (0U until this.tier.size.toUInt()).forEach { i ->
                val x = this.containerData.input.x + this.containerData.inSize * i
                val index = j * this.tier.size.toUInt() + i
                this.addSlot(SlotItemHandler(this.tableInventory, index.toInt(), x.toInt(), y.toInt()))
            }
        }
        this.containerData.output.let {
            //this.addSlot(OutputSlot(...))
        }
        (0U..3U).forEach { j ->
            val y = this.containerData.player.y + this.containerData.inSize * j + (if (j == 3U) this.containerData.barDisplacement else 0U)
            (0U..8U).forEach { i ->
                val x = this.containerData.player.x + this.containerData.inSize * i
                val index = (if (j == 3U) 0U else (9U * (j + 1U))) + i

                this.addSlot(Slot(this.playerInventory, index.toInt(), x.toInt(), y.toInt()))
            }
        }
        this.containerData.armor?.forEachIndexed { index, coordinate ->
            val type = when (index) {
                0 -> EquipmentSlotType.FEET
                1 -> EquipmentSlotType.LEGS
                2 -> EquipmentSlotType.CHEST
                3 -> EquipmentSlotType.HEAD
                else -> error("Invalid ID $index")
            }
            this.addSlot(ArmorSlot(this.playerInventory, type, coordinate.x.toInt(), coordinate.y.toInt()))
        }
        this.containerData.auto?.let {
            //this.addSlot(AutoSlot(...))
        }
    }

    override fun stillValid(pPlayer: PlayerEntity): Boolean =
        stillValid(this.levelAccess, this.player, tables.getValue(tier)())
}
