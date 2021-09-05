package net.thesilkminer.mc.squaredcrafting.data

import com.mojang.datafixers.util.Pair as DfuPair

import net.minecraft.data.DataGenerator
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
import net.minecraft.util.IItemProvider
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

internal class LootTablesProvider(generator: DataGenerator) : LootTableProvider(generator) {
    private class Blocks : Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
        private val tables by lazy {
            TableTier.values().associate(this::tableEntry)
        }

        override fun accept(consumer: BiConsumer<ResourceLocation, LootTable.Builder>) =
            generate(consumer, this.tables, ForgeRegistries.BLOCKS.values, { it.registryName }) { it.lootTable }

        private fun tableEntry(tier: TableTier): Pair<ResourceLocation, LootTable.Builder> {
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
                .distinctBy(Pair<ResourceLocation, T>::first)
                .map { it.first to (tables[it.first] ?: throw IllegalStateException("Missing loot table ${it.first} for ${it.second}")) }
                .onEach { consumer.accept(it.first, it.second) }
                .map(Pair<ResourceLocation, LootTable.Builder>::first)
                .toSet()
                .minus(tables.keys)
                .let {
                    if (it.isNotEmpty()) {
                        throw IllegalStateException("Created loot tables for non-blocks: ${it.joinToString(transform = ResourceLocation::toString)}")
                    }
                }
        }
    }

    override fun getTables(): List<DfuPair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> =
        listOf(DfuPair.of(Supplier(::Blocks), LootParameterSets.BLOCK))

    override fun validate(map: MutableMap<ResourceLocation, LootTable>, validationTracker: ValidationTracker) {
        map.forEach { (location, table) -> LootTableManager.validate(validationTracker, location, table) }
    }
}
