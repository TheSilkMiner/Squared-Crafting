package net.thesilkminer.mc.squaredcrafting.client.feature.tables

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.thesilkminer.mc.squaredcrafting.MOD_ID
import net.thesilkminer.mc.squaredcrafting.common.feature.tables.TableMenu

internal class TableScreen(
    private val tableMenu: TableMenu,
    playerInventory: PlayerInventory,
    name: ITextComponent
) : ContainerScreen<TableMenu>(tableMenu, playerInventory, name) {
    private val tier = this.tableMenu.tier
    private val containerData = this.tableMenu.containerData
    private val backgroundLocation = ResourceLocation(MOD_ID, "textures/gui/container/table/${this.tier.registryName}.png")

    init {
        this.imageWidth = this.containerData.tex.x.toInt()
        this.imageHeight = this.containerData.tex.y.toInt()
    }

    override fun render(poseStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.renderBackground(poseStack)
        super.render(poseStack, mouseX, mouseY, partialTicks)
        this.renderTooltip(poseStack, mouseX, mouseY)
    }

    override fun renderLabels(pMatrixStack: MatrixStack, pX: Int, pY: Int) {
        // TODO("Labels, maybe")
    }

    @Suppress("DEPRECATION")
    override fun renderBg(poseStack: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        this.minecraft?.textureManager?.bind(this.backgroundLocation)
        val x = (this.width - this.imageWidth) / 2
        val y = (this.height - this.imageHeight) / 2
        blit(poseStack, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.containerData.size.toInt(), this.containerData.size.toInt())
    }
}
