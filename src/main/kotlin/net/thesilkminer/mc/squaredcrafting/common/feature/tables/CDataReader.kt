@file:JvmName("0_CDataReader")
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.thesilkminer.mc.squaredcrafting.common.feature.tables

import net.minecraftforge.fml.ModList
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption

internal data class ContainerData(
    val inSize: UByte,
    val input: Coordinate,
    val outSize: UByte,
    val output: Coordinate,
    val arrow: CoordinatePlus,
    val activeArea: CoordinatePlus?,
    val player: Coordinate,
    val barDisplacement: UByte,
    val armor: List<Coordinate>?,
    val auto: Coordinate?,
    val autoArrow: CoordinatePlus?,
    val tex: Coordinate,
    val size: UShort
) {
    internal data class Coordinate(val x: UShort, val y: UShort)
    internal data class CoordinatePlus(val coordinate: Coordinate, val h: UShort, val w: UShort)
}

private val zero = 0.toUByte()

internal val cData by lazy(::readCData)

private fun readCData() : Map<TableTier, ContainerData> {
    val file = ModList.get().getModFileById(MOD_ID).file
    val path = file.locator.findPath(file, "meta", MOD_ID, "cdata").toAbsolutePath()
    require(Files.exists(path)) { "Unable to read container data, this is a serious error" }
    return Files.newInputStream(path, StandardOpenOption.READ).use(::readCData)
}

private fun readCData(stream: InputStream): Map<TableTier, ContainerData> =
    BufferedInputStream(stream).use(::readCData)

private fun readCData(stream: BufferedInputStream): Map<TableTier, ContainerData> =
    (zero until stream.readByte()).associate { readPair(stream) }

private fun readPair(stream: BufferedInputStream): Pair<TableTier, ContainerData> =
    readTier(stream) to readTierData(stream)

private fun readTier(stream: BufferedInputStream): TableTier {
    val id = readTierId(stream)
    return TableTier.values().find { it.registryName == id } ?: error("$id is not a valid ID")
}

private fun readTierId(stream: BufferedInputStream): String =
    stream.readBytes(stream.readByte().toUInt())
        .map { (it.toShort() + 0x20.toShort()).toChar() }
        .joinToString(separator = "") { it.toString() }

private fun readTierData(stream: BufferedInputStream): ContainerData {
    val i = stream.readByte()
    val o = stream.readByte()
    val input = readCoordinates(stream)
    val out = readCoordinates(stream)
    val arrow = readCoordinatesPlus(stream)
    val activeArea = maybeReadActiveArea(stream, arrow)
    val player = readCoordinates(stream)
    val d = stream.readByte()
    val armor = maybeReadArmor(stream)
    val (auto, autoArrow) = maybeReadAuto(stream)
    val tex = readCoordinates(stream)
    val size = maybeReadSize(stream)
    return ContainerData(i, input, o, out, arrow, activeArea, player, d, armor, auto, autoArrow, tex, size)
}

private fun readCoordinates(stream: BufferedInputStream): ContainerData.Coordinate {
    val y = stream.readShort()
    val x = stream.readShort()
    return ContainerData.Coordinate(x, y)
}

private fun readCoordinatesPlus(stream: BufferedInputStream): ContainerData.CoordinatePlus {
    val coordinates = readCoordinates(stream)
    val w = stream.readByte()
    val h = stream.readByte()
    return ContainerData.CoordinatePlus(coordinates, h.toUShort(), w.toUShort())
}

private fun maybeReadActiveArea(stream: BufferedInputStream, arrow: ContainerData.CoordinatePlus): ContainerData.CoordinatePlus? =
    if (!stream.readBoolean()) null else readActiveArea(stream, arrow)

private fun readActiveArea(stream: BufferedInputStream, arrow: ContainerData.CoordinatePlus): ContainerData.CoordinatePlus =
    if (stream.readBoolean()) arrow else readCoordinatesPlus(stream)

private fun maybeReadArmor(stream: BufferedInputStream): List<ContainerData.Coordinate>? =
    if (!stream.readBoolean()) null else readArmor(stream)

private fun readArmor(stream: BufferedInputStream): List<ContainerData.Coordinate> =
    listOf(readCoordinates(stream), readCoordinates(stream), readCoordinates(stream), readCoordinates(stream))

private fun maybeReadAuto(stream: BufferedInputStream): Pair<ContainerData.Coordinate?, ContainerData.CoordinatePlus?> =
    if (!stream.readBoolean()) null to null else readAuto(stream)

private fun readAuto(stream: BufferedInputStream): Pair<ContainerData.Coordinate, ContainerData.CoordinatePlus> =
    readCoordinates(stream) to readCoordinatesPlus(stream)

private fun maybeReadSize(stream: BufferedInputStream): UShort =
    if (!stream.readBoolean()) stream.readShort() else 256U.toUShort()

private fun BufferedInputStream.readByte(): UByte {
    val b = this.read()
    require (b != -1) { "Reached end of file" }
    return b.toUByte()
}

private fun BufferedInputStream.readBytes(size: UInt): UByteArray =
    (0U until size).map { this.readByte() }.toUByteArray()

private fun BufferedInputStream.readBoolean(): Boolean =
    when (val b = this.readByte()) {
        zero -> false
        1.toUByte() -> true
        else -> error("Invalid boolean value $b")
    }

private fun BufferedInputStream.readShort(): UShort {
    val l = this.readByte()
    val h = this.readByte()
    return (h.toUInt() shl 8).toUShort() or l.toUShort()
}
