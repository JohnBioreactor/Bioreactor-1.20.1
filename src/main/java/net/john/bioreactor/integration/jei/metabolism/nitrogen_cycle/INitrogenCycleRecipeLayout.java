package net.john.bioreactor.integration.jei.metabolism.nitrogen_cycle;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.minecraft.client.gui.GuiGraphics;

public interface INitrogenCycleRecipeLayout {
    void setRecipe(IRecipeLayoutBuilder builder, Metabolism metabolism);
    void draw(GuiGraphics graphics, Metabolism metabolism, double mouseX, double mouseY);
}
