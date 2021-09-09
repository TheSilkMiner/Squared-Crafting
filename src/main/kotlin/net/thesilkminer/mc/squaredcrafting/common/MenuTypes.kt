@file:JvmName("0_ContainerTypes")

package net.thesilkminer.mc.squaredcrafting.common

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.network.PacketBuffer
import net.minecraftforge.common.extensions.IForgeContainerType
import net.minecraftforge.registries.ForgeRegistries
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableMenu
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import net.thesilkminer.mc.squaredcrafting.logger
import net.thesilkminer.mc.squaredcrafting.registrationMarker
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate

//region Registration Helpers
private val menuTypesDeferredRegister = KDeferredRegister(ForgeRegistries.CONTAINERS, MOD_ID)

internal fun KotlinEventBus.attachMenuTypesHandler() = menuTypesDeferredRegister.register(this)

private fun <T : Container> KDeferredRegister<ContainerType<*>>.registerType(
    name: String,
    creator: (playerInventory: PlayerInventory, packet: PacketBuffer) -> (Int) -> T
): ObjectHolderDelegate<ContainerType<T>> {
    val builder = this.registerObject(name) {
        IForgeContainerType.create { windowId, inv, data -> creator(inv, data)(windowId) }
    }
    logger.debug(registrationMarker, "Registered menu type {}", builder as Any) // Fuck you Apache
    return builder
}
//endregion

internal val tableMenuType by menuTypesDeferredRegister.registerType("table") { playerInventory, packet ->
    { id ->
        TableMenu(
            id,
            playerInventory,
            TableTier.values().find { it.registryName == packet.readUtf(15) } ?: error("Invalid tier")
        )
    }
}
