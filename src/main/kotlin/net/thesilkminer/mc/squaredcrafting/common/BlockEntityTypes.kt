@file:JvmName("0_BlockEntityTypes")

package net.thesilkminer.mc.squaredcrafting.common

import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Util
import net.minecraft.util.datafix.TypeReferences
import net.minecraftforge.registries.ForgeRegistries
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableBlockEntity
import net.thesilkminer.mc.squaredcrafting.logger
import net.thesilkminer.mc.squaredcrafting.registrationMarker
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate

//region Registration Helpers
private val blockEntityTypesDeferredRegister = KDeferredRegister(ForgeRegistries.TILE_ENTITIES, MOD_ID)

internal fun KotlinEventBus.attachBlockEntityTypesHandler() = blockEntityTypesDeferredRegister.register(this)

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
private fun <T : TileEntity> KDeferredRegister<TileEntityType<*>>.registerType(name: String, builderCreator: () -> TileEntityType.Builder<T>): ObjectHolderDelegate<TileEntityType<T>> {
    val type = Util.fetchChoiceType(TypeReferences.BLOCK_ENTITY, "$MOD_ID:$name")
    val builder = this.registerObject(name) { builderCreator().build(type) }
    logger.debug(registrationMarker, "Registered block entity type {}", builder as Any) // Fuck you Apache
    return builder
}
//endregion

internal val allTablesBlockEntity by blockEntityTypesDeferredRegister.registerType("table") {
    TileEntityType.Builder.of(::TableBlockEntity, *tables.values.map(ObjectHolderDelegate<out Block>::get).toTypedArray())
}
