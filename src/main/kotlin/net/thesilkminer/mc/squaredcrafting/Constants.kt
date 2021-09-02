@file:JvmName("0_Constants")

package net.thesilkminer.mc.squaredcrafting

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager

internal const val MOD_ID = "squared_crafting"
internal const val MOD_NAME = "Squared Crafting"

internal val logger by lazy { LogManager.getLogger(MOD_NAME) }

internal val dataGenMarker by lazy { MarkerManager.getMarker("Data Generation") }
internal val lifecycleMarker by lazy { MarkerManager.getMarker("Lifecycle") }
internal val registrationMarker by lazy { MarkerManager.getMarker("Registration") }
