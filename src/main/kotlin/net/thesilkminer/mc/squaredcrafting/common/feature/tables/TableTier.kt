package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.item.Rarity
import net.minecraftforge.common.ToolType

internal enum class TableTier(
    val size: UByte,
    val registryName: String,
    val blockData: BlockProperties,
    val itemData: ItemProperties
) {
    MEDIUM(
        5U,
        "medium",
        BlockProperties(requiresTool = false, hasInventory = false),
        ItemProperties()
    ),
    BIG(
        7U,
        "big",
        BlockProperties(strength = 5.0, toolLevel = 1),
        ItemProperties()
    ),
    DIRE(
        9U,
        "dire",
        BlockProperties(strength = 8.0, toolLevel = 2),
        ItemProperties()
    ),
    HUGE(
        11U,
        "huge",
        BlockProperties(material = Material.METAL, strength = 12.5, tool = ToolType.PICKAXE, toolLevel = 1),
        ItemProperties(rarity = Rarity.UNCOMMON)
    ),
    HUMONGOUS(
        13U,
        "humongous",
        BlockProperties(material = Material.METAL, strength = 20.9, tool = ToolType.PICKAXE, toolLevel = 2),
        ItemProperties(rarity = Rarity.UNCOMMON)
    ),
    COLOSSAL(
        15U,
        "colossal",
        BlockProperties(material = Material.METAL, strength = 30.5, tool = ToolType.PICKAXE, toolLevel = 2),
        ItemProperties(rarity = Rarity.UNCOMMON)
    ),
    TEXTURE(
        16U,
        "texture",
        BlockProperties(material = Material.WOOL, strength = 1.5, soundType = SoundType.WOOL, requiresTool = false),
        ItemProperties(rarity = Rarity.EPIC, hasFoil = true)
    ),
    ABSURD(
        17U,
        "absurd",
        BlockProperties(material = Material.HEAVY_METAL, strength = 54.0, resistance = 420.0, tool = ToolType.PICKAXE, toolLevel = 3),
        ItemProperties(rarity = Rarity.RARE)
    ),
    ASTRONOMICAL(
        19U,
        "astronomical",
        BlockProperties(material = Material.HEAVY_METAL, strength = 54.0, resistance = 1200.0, tool = ToolType.PICKAXE, toolLevel = 3),
        ItemProperties(rarity = Rarity.RARE)
    ),
    HOLY_SHIT(
        21U,
        "omg",
        BlockProperties(material = Material.HEAVY_METAL, strength = 100.0, resistance = 8064.0, soundType = SoundType.ANVIL, tool = ToolType.PICKAXE, toolLevel = 3),
        ItemProperties(rarity = Rarity.EPIC, stackSize = 1U, hasFoil = true)
    );

    internal data class BlockProperties(
        val material: Material = Material.WOOD,
        val strength: Double = 2.5,
        val resistance: Double = strength,
        val soundType: SoundType = if (material == Material.WOOD) SoundType.WOOD else SoundType.METAL,
        val requiresTool: Boolean = true,
        val tool: ToolType = ToolType.AXE,
        val toolLevel: Int = 0,
        val hasInventory: Boolean = true
    )

    internal data class ItemProperties(
        val rarity: Rarity = Rarity.COMMON,
        val stackSize: UByte = 64U,
        val hasFoil: Boolean = false
    )

    override fun toString(): String = "${this.name}@[${this.blockData}/${this.itemData}]"
}
