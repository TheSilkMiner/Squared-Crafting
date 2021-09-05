package net.thesilkminer.mc.squaredcrafting.data

import net.minecraft.data.DataGenerator
import net.minecraft.data.IFinishedRecipe
import net.minecraft.data.RecipeProvider
import java.util.function.Consumer

internal class RecipesProvider(generator: DataGenerator) : RecipeProvider(generator) {
    override fun buildShapelessRecipes(consumer: Consumer<IFinishedRecipe>) {

    }
}
