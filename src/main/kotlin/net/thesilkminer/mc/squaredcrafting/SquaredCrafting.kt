package net.thesilkminer.mc.squaredcrafting

import net.minecraftforge.fml.common.Mod
import net.thesilkminer.mc.squaredcrafting.client.registerClientStuff
import net.thesilkminer.mc.squaredcrafting.common.attachBlockEntityTypesHandler
import net.thesilkminer.mc.squaredcrafting.common.attachBlocksHandler
import net.thesilkminer.mc.squaredcrafting.common.attachItemsHandler
import net.thesilkminer.mc.squaredcrafting.common.attachMenuTypesHandler
import net.thesilkminer.mc.squaredcrafting.data.onDataGeneration
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(MOD_ID)
object SquaredCrafting {
    init {
        MOD_BUS.attachBlockEntityTypesHandler()
        MOD_BUS.attachBlocksHandler()
        MOD_BUS.attachItemsHandler()
        MOD_BUS.attachMenuTypesHandler()
        registerClientStuff(MOD_BUS, FORGE_BUS)
        MOD_BUS.addListener(::onDataGeneration)
    }
}
