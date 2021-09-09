@file:JvmName("0_Client_Shim")

package net.thesilkminer.mc.squaredcrafting.client

import net.minecraft.client.gui.ScreenManager
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import net.thesilkminer.mc.squaredcrafting.client.feature.tables.TableScreen
import net.thesilkminer.mc.squaredcrafting.common.tableMenuType
import net.thesilkminer.mc.squaredcrafting.lifecycleMarker
import net.thesilkminer.mc.squaredcrafting.logger
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus

private object ClientHandler {
    fun register(mod: KotlinEventBus, forge: KotlinEventBus) {
        mod.addListener(this::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            ScreenManager.register(tableMenuType, ::TableScreen)
        }
    }
}

@JvmName("shimmed client proxy method, do not care")
internal fun registerClientStuff(mod: KotlinEventBus, forge: KotlinEventBus) {
    // This skips classloading client stuff on servers
    if (!FMLEnvironment.dist.isClient) return
    logger.debug(lifecycleMarker, "Dist is client. If you see this on a server, STUFF HAS GONE WRONG")

    ClientHandler.register(mod, forge)
}
