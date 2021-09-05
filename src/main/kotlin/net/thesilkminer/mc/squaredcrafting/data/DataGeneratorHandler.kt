@file:JvmName("0_DataGenerator_Shim")

package net.thesilkminer.mc.squaredcrafting.data

import net.minecraft.data.DataGenerator
import net.minecraft.data.IDataProvider
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.thesilkminer.mc.squaredcrafting.dataGenMarker
import net.thesilkminer.mc.squaredcrafting.logger

private object DataGeneratorHandler {
    init {
        logger.warn(dataGenMarker, "Data generators have loaded in production: this is not good")
    }

    fun onDataGeneration(event: GatherDataEvent) {
        if (event.includeClient()) {
            event.addProvider(::AmericanEnglishLanguageProvider)
            event.addProvider(::BlockStatesAndModelsProvider)
        }
        if (event.includeServer()) {
            event.addProvider(::LootTablesProvider)
            event.addProvider(::RecipesProvider)
        }
    }

    @JvmName("\$")
    private inline fun GatherDataEvent.addProvider(creator: (DataGenerator) -> IDataProvider) {
        this.generator.addProvider(creator(this.generator))
    }

    @JvmName("\$\$")
    private inline fun GatherDataEvent.addProvider(creator: (DataGenerator, ExistingFileHelper) -> IDataProvider) {
        this.generator.addProvider(creator(this.generator, this.existingFileHelper))
    }
}

@JvmName("shimmed data generation method, do not care")
internal fun onDataGeneration(event: GatherDataEvent) {
    // This skips classloading DataGenerator in production
    DataGeneratorHandler.onDataGeneration(event)
}
