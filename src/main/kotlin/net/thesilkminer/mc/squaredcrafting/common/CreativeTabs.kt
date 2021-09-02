@file:JvmName("0_CreativeTabs")

package net.thesilkminer.mc.squaredcrafting.common

import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableTier

internal val mainCreativeTab: ItemGroup = object : ItemGroup("$MOD_ID.main") {
    override fun makeIcon(): ItemStack =
        tables.getValue(TableTier.ABSURD)().asItem().defaultInstance.copy()
}
