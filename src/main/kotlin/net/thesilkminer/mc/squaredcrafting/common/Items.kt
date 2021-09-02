@file:JvmName("0_Items")

package net.thesilkminer.mc.squaredcrafting.common

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraftforge.registries.ForgeRegistries
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableItem
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier
import net.thesilkminer.mc.squaredcrafting.logger
import net.thesilkminer.mc.squaredcrafting.registrationMarker
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.ObjectHolderDelegate

//region Registration Helpers
private val blockItemsDeferredRegister = KDeferredRegister(ForgeRegistries.ITEMS, MOD_ID)
private val itemsDeferredRegister = KDeferredRegister(ForgeRegistries.ITEMS, MOD_ID)

internal fun ObjectHolderDelegate<out Block>.withTableBlockItem(tier: TableTier) {
    val item = blockItemsDeferredRegister.registerObject(this.registryName.path) {
        TableItem(tier, this(), tier.itemProperties)
    }
    logger.debug(registrationMarker, "Registered table item {} for tier {}", item, tier)
}

internal fun KotlinEventBus.attachItemsHandler() {
    blockItemsDeferredRegister.register(this)
    itemsDeferredRegister.register(this)
}

private fun <T : Item> KDeferredRegister<Item>.registerItem(name: String, creator: () -> T): ObjectHolderDelegate<T> {
    val item = this.registerObject(name, creator)
    logger.debug(registrationMarker, "Registered item {}", item as Any) // Fuck you Apache
    return item
}

private val TableTier.itemProperties: Item.Properties get() = this.itemData.let { data ->
    Item.Properties().tab(mainCreativeTab).apply {
        this.rarity(data.rarity)
        this.stacksTo(data.stackSize.toInt())
    }
}
//endregion

internal val transparentDye by itemsDeferredRegister.registerItem("transparent_dye") {
    Item(Item.Properties().tab(mainCreativeTab))
}
