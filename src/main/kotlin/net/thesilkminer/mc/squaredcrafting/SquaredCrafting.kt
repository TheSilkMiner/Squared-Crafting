package net.thesilkminer.mc.squaredcrafting

import net.minecraftforge.fml.common.Mod
import net.thesilkminer.mc.squaredcrafting.common.attachBlocksHandler
import net.thesilkminer.mc.squaredcrafting.common.attachItemsHandler
import net.thesilkminer.mc.squaredcrafting.data.onDataGeneration
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(MOD_ID)
object SquaredCrafting {
    init {
        MOD_BUS.attachBlocksHandler()
        MOD_BUS.attachItemsHandler()
        MOD_BUS.addListener(::onDataGeneration)
    }
}
