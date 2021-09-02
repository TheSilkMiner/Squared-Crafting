@file:JvmName("0_DataGenerator_Shim")

package net.thesilkminer.mc.squaredcrafting.data

import com.mojang.datafixers.util.Pair
import net.minecraft.data.DataGenerator
import net.minecraft.data.IDataProvider
import net.minecraft.data.LootTableProvider
import net.minecraft.loot.ConstantRange
import net.minecraft.loot.ItemLootEntry
import net.minecraft.loot.LootParameterSet
import net.minecraft.loot.LootParameterSets
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.LootTableManager
import net.minecraft.loot.LootTables
import net.minecraft.loot.ValidationTracker
import net.minecraft.loot.conditions.SurvivesExplosion
import net.minecraft.util.Direction
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.generators.BlockModelBuilder
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ItemModelBuilder
import net.minecraftforge.client.model.generators.ModelBuilder
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder
import net.minecraftforge.common.data.ExistingFileHelper
import net.minecraftforge.common.data.LanguageProvider
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.MOD_NAME
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import net.thesilkminer.mc.squaredcrafting.common.tables
import net.thesilkminer.mc.squaredcrafting.dataGenMarker
import net.thesilkminer.mc.squaredcrafting.logger
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

private object DataGeneratorHandler {
    init {
        logger.warn(dataGenMarker, "Data generators have loaded in production: this is not good")
    }

    fun onDataGeneration(event: GatherDataEvent) {
        if (event.includeClient()) {
            event.addProvider(::SquaredBlockStatesAndModelsProvider)
            event.addProvider(::SquaredLanguageProvider)
        }
        if (event.includeServer()) {
            event.addProvider(::SquaredLootTableProvider)
        }
    }

    @JvmName("\$")
    private fun GatherDataEvent.addProvider(creator: (DataGenerator) -> IDataProvider) {
        this.generator.addProvider(creator(this.generator))
    }

    @JvmName("\$\$")
    private fun GatherDataEvent.addProvider(creator: (DataGenerator, ExistingFileHelper) -> IDataProvider) {
        this.generator.addProvider(creator(this.generator, this.existingFileHelper))
    }
}

private class SquaredBlockStatesAndModelsProvider(generator: DataGenerator, helper: ExistingFileHelper) : BlockStateProvider(generator, MOD_ID, helper) {
    override fun registerStatesAndModels() {
        this.addTables()
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
        val tierBasedAdditions = mapOf<TableTier, kotlin.Pair<BlockModelBuilder, ItemModelBuilder>>()

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

            /*
            // TODO("Add back when Forge is going to cooperate about everything")
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
             */
            this.itemModels()
                .getBuilder("item/${block().asItem().registryName?.path}")
                .parent(base)
                .defaultTransforms()
        }
    }

    private fun <T : ModelBuilder<T>> T.defaultTransforms(): T =
        this.transforms()
            .transform(ModelBuilder.Perspective.GUI)
            .rotation(30.0F, 255.0F, 0.0F) // TODO("Default is 225.0F, though 255.0F doesn't look bad")
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

private class SquaredLanguageProvider(generator: DataGenerator) : LanguageProvider(generator, MOD_ID, "en_us") {
    override fun addTranslations() {
        this.add("itemGroup.$MOD_ID.main", MOD_NAME)

        this.addTables()
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

private class SquaredLootTableProvider(generator: DataGenerator) : LootTableProvider(generator) {
    private class Blocks : Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
        private val tables by lazy {
            TableTier.values().associate(this::tableEntry)
        }

        override fun accept(consumer: BiConsumer<ResourceLocation, LootTable.Builder>) =
            generate(consumer, this.tables, ForgeRegistries.BLOCKS.values, { it.registryName }) { it.lootTable }

        private fun tableEntry(tier: TableTier): kotlin.Pair<ResourceLocation, LootTable.Builder> {
            val target = net.thesilkminer.mc.squaredcrafting.common.tables.getValue(tier)()
            return target.lootTable to this.tableTable(tier, target)
        }

        private fun tableTable(tier: TableTier, item: IItemProvider? = null): LootTable.Builder {
            val target = (item ?: net.thesilkminer.mc.squaredcrafting.common.tables.getValue(tier)()).asItem()
            return LootTable.lootTable()
                .withPool(
                    LootPool.lootPool()
                        .name(target.registryName?.path ?: "unknown")
                        .setRolls(ConstantRange.exactly(1))
                        .add(ItemLootEntry.lootTableItem(target))
                        .let { if (!tier.itemData.isExplosionResistant) it.`when`(SurvivesExplosion.survivesExplosion()) else it }
                )
        }
    }

    companion object {
        @Suppress("SameParameterValue")
        private fun <T> generate(
            consumer: BiConsumer<ResourceLocation, LootTable.Builder>,
            tables: Map<ResourceLocation, LootTable.Builder>,
            registry: Iterable<T>,
            registryNameGetter: (T) -> ResourceLocation?,
            lootTableProvider: (T) -> ResourceLocation?
        ) {
            registry.asSequence()
                .filter { registryNameGetter(it).let { name -> name != null && name.namespace == MOD_ID } }
                .map { lootTableProvider(it) to it }
                .filter { it.first != null && it.first != LootTables.EMPTY }
                .map { it.first!! to it.second }
                .distinctBy { it.first }
                .map { it.first to (tables[it.first] ?: throw IllegalStateException("Missing loot table ${it.first} for ${it.second}")) }
                .onEach { consumer.accept(it.first, it.second) }
                .map { it.first }
                .toSet()
                .minus(tables.keys)
                .let {
                    if (it.isNotEmpty()) {
                        throw IllegalStateException("Created loot tables for non-blocks: ${it.joinToString(transform = ResourceLocation::toString)}")
                    }
                }
        }
    }

    override fun getTables(): List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> =
        listOf(Pair.of(Supplier(::Blocks), LootParameterSets.BLOCK))

    override fun validate(map: MutableMap<ResourceLocation, LootTable>, validationTracker: ValidationTracker) {
        map.forEach { (location, table) -> LootTableManager.validate(validationTracker, location, table) }
    }
}

@JvmName("shimmed data generation method, do not care")
internal fun onDataGeneration(event: GatherDataEvent) {
    // This skips classloading DataGenerator in production
    DataGeneratorHandler.onDataGeneration(event)
}
