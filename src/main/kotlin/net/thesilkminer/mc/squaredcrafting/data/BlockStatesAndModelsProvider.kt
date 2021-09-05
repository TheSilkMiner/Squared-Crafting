package net.thesilkminer.mc.squaredcrafting.data

import net.minecraft.data.DataGenerator
import net.minecraft.item.Item
import net.minecraft.util.Direction
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ItemModelBuilder
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.client.model.generators.ModelBuilder
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder
import net.minecraftforge.common.data.ExistingFileHelper
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import net.thesilkminer.mc.squaredcrafting.common.tables
import net.thesilkminer.mc.squaredcrafting.common.transparentDye

internal class BlockStatesAndModelsProvider(generator: DataGenerator, helper: ExistingFileHelper) : BlockStateProvider(generator, MOD_ID, helper) {
    override fun registerStatesAndModels() {
        this.addTables()

        this.itemModels().normalItem(transparentDye)
    }

    private fun addTables() {
        fun BlockModelBuilder.foot(name: String, x: Int, z: Int, texture: String): BlockModelBuilder =
            this.element("feet/$name")
                .from(x.toFloat(), 1.0F, z.toFloat())
                .to(x.toFloat() + 1.0F, 5.0F, z.toFloat() + 1.0F)
                .allFaces { direction, builder ->
                    when (direction) {
                        Direction.NORTH -> builder.texture(texture)
                            .uvs(8.0F, 6.0F, 9.0F, 10.0F)
                        Direction.SOUTH -> builder.texture(texture)
                            .uvs(7.0F, 6.0F, 8.0F, 10.0F)
                        Direction.WEST -> builder.texture(texture)
                            .uvs(7.0F, 12.0F, 8.0F, 10.0F)
                        Direction.EAST -> builder.texture(texture)
                            .uvs(8.0F, 4.0F, 9.0F, 8.0F)
                        else -> builder.texture(texture)
                    }.end()
                }
                .end()


        fun tableModel(kind: String, top: String, bottom: String, side: String) = this.models()
            .getBuilder("block/crafting_table/base_$kind")
            .element("body/table")
            .from(0.0F, 5.0F, 0.0F)
            .to(16.0F, 13.0F, 16.0F)
            .allFaces { direction, builder ->
                when (direction) {
                    Direction.UP -> builder.texture(top)
                        .uvs(0.0F, 0.0F, 16.0F, 16.0F)
                    Direction.DOWN -> builder.texture(bottom)
                        .uvs(0.0F, 0.0F, 16.0F, 16.0F)
                    else -> builder.texture(side)
                        .rotation(ModelBuilder.FaceRotation.CLOCKWISE_90)
                        .uvs(0.0F, 0.0F, 8.0F, 16.0F)
                }.end()
            }
            .end()
            .element("body/holder")
            .from(0.0F, 0.0F, 0.0F)
            .to(16.0F, 1.0F, 16.0F)
            .allFaces { direction, builder ->
                when (direction) {
                    Direction.UP, Direction.DOWN -> builder.texture(bottom)
                        .rotation(ModelBuilder.FaceRotation.CLOCKWISE_90)
                        .uvs(0.0F, 0.0F, 16.0F, 16.0F)
                    else -> builder.texture(side)
                        .rotation(ModelBuilder.FaceRotation.CLOCKWISE_90)
                        .uvs(
                            if (direction == Direction.NORTH || direction == Direction.SOUTH) 8.0F else 7.0F,
                            0.0F,
                            if (direction == Direction.NORTH || direction == Direction.SOUTH) 9.0F else 8.0F,
                            16.0F
                        )
                }.end()
            }
            .end()
            .foot("ne", 15, 0, side)
            .foot("nw", 0, 0, side)
            .foot("sw", 0, 15, side)
            .foot("se", 15, 15, side)
            .texture(top.substring(1), this.modLoc("block/crafting_table/top_$kind"))
            .texture(bottom.substring(1), this.modLoc("block/crafting_table/bottom_$kind"))
            .texture(side.substring(1), this.modLoc("block/crafting_table/side_$kind"))
            .texture("particle", this.modLoc("block/crafting_table/top_$kind"))
            .defaultTransforms()

        // DSL PLEASE!!!!!!!!
        val baseModels = TableTier.ModelKind.values().associateWith { tableModel(it.id, "#top", "#bottom", "#side") }
        val tierBasedAdditions = mapOf<TableTier, Pair<BlockModelBuilder, ItemModelBuilder>>()

        tables.forEach { (tier, block) ->
            val base = baseModels.getValue(tier.blockData.modelKind)
            val addition = tierBasedAdditions[tier]

            this.getMultipartBuilder(block())
                .part()
                .modelFile(base)
                .addModel()
                .end()
                .apply {
                    if (addition != null) {
                        this.part()
                            .modelFile(addition.first)
                            .addModel()
                            .end()
                    }
                }

            this.itemModels()
                .getBuilder("item/${block().asItem().registryName?.path}")
                .customLoader { parent, helper -> CompositeModelBuilder.begin(parent, helper) }
                .submodel("base", this.itemModels().nested().parent(base).defaultTransforms())
                .apply {
                    if (addition != null) {
                        this.submodel("element", addition.second)
                    }
                }
                .end()
                .defaultTransforms()
        }
    }

    private fun ItemModelProvider.normalItem(item: Item, texture: String = item.registryName?.path ?: error("null")) =
        this.singleTexture(
            "item/${item.registryName?.path}",
            this.mcLoc("item/generated"),
            "layer0",
            this.modLoc("item/$texture")
        )

    private fun <T : ModelBuilder<T>> T.defaultTransforms(): T =
        this.transforms()
            .transform(ModelBuilder.Perspective.GUI)
            .rotation(30.0F, 225.0F, 0.0F)
            .scale(0.625F)
            .end()
            .transform(ModelBuilder.Perspective.GROUND)
            .translation(0.0F, 3.0F, 0.0F)
            .scale(0.25F)
            .end()
            .transform(ModelBuilder.Perspective.FIXED)
            .scale(0.5F)
            .end()
            .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT)
            .rotation(75.0F, 45.0F, 0.0F)
            .translation(0.0F, 2.5F, 0.0F)
            .scale(0.375F)
            .end()
            .transform(ModelBuilder.Perspective.THIRDPERSON_LEFT)
            .rotation(75.0F, 45.0F, 0.0F)
            .translation(0.0F, 2.5F, 0.0F)
            .scale(0.375F)
            .end()
            .transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT)
            .rotation(0.0F, 45.0F, 0.0F)
            .scale(0.40F)
            .end()
            .transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT)
            .rotation(0.0F, 225.0F, 0.0F)
            .scale(0.40F)
            .end()
            .end()

    @Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER") // Just to keep my sanity
    private inline fun BlockModelBuilder.element(name: String) = this.element()
}
