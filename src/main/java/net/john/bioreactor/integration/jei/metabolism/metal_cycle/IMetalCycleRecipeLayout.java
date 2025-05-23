package net.john.bioreactor.integration.jei.metabolism.metal_cycle;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.minecraft.client.gui.GuiGraphics;

public interface IMetalCycleRecipeLayout {
    void setRecipe(IRecipeLayoutBuilder builder, Metabolism metabolism);
    void draw(GuiGraphics graphics, Metabolism metabolism, double mouseX, double mouseY);
}
