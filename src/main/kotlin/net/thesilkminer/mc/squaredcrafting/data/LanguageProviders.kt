package net.thesilkminer.mc.squaredcrafting.data

import net.minecraft.data.DataGenerator
import net.minecraftforge.common.data.LanguageProvider
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.MOD_NAME
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import net.thesilkminer.mc.squaredcrafting.common.tables
import net.thesilkminer.mc.squaredcrafting.common.transparentDye

internal class AmericanEnglishLanguageProvider(generator: DataGenerator) : LanguageProvider(generator, MOD_ID, "en_us") {
    override fun addTranslations() {
        this.add("itemGroup.$MOD_ID.main", MOD_NAME)

        this.addTables()

        this.add(transparentDye, "Transparent Dye")
    }

    private fun addTables() {
        mapOf(
            TableTier.MEDIUM to "Medium Crafting Table",
            TableTier.BIG to "Big Crafting Table",
            TableTier.DIRE to "Dire Crafting Table",
            TableTier.HUGE to "Huge Crafting Table",
            TableTier.HUMONGOUS to "Humongous Crafting Table",
            TableTier.COLOSSAL to "Colossal Crafting Table",
            TableTier.TEXTURE to "Texture Maker",
            TableTier.ABSURD to "Absurd Crafting Table",
            TableTier.ASTRONOMICAL to "Astronomical Crafting Table",
            TableTier.HOLY_SHIT to "'You can't be Serious' Crafting Table"
        ).forEach { (tier, entry) -> this.addBlock(tables.getValue(tier), entry) }

        this.add("$MOD_ID.tooltip.table.texture_fun", "I always make textures for my mods in Minecraft")
        this.add("$MOD_ID.tooltip.table.hope_and_pray", "You DO NOT want to use this one")
    }
}

