package net.john.bioreactor.integration.jei.metabolism.nitrogen_cycle;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.john.bioreactor.integration.jei.metabolism.LayoutHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Cette classe enregistre une seule catégorie (donc une page JEI) pour le cycle du N,
 * puis délègue l’affichage propre à chaque recette via un layout handler spécifique.
 */
public class NitrogenCycleCategory implements IRecipeCategory<NitrogenCycleRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Bioreactor.MOD_ID, "nitrogen_cycle");
    // Définition du RecipeType pour JEI (nécessaire dans les versions récentes de JEI)
    public static final RecipeType<NitrogenCycleRecipe> RECIPE_TYPE = new RecipeType<>(UID, NitrogenCycleRecipe.class);

    private final IDrawable
            background, metaboBackground, icon,
            metaboSpecificityFrame,
            requireLightFalse, requireLightTrue,
            o2AerobicIcon, o2MicroIcon, o2AnaerobicIcon;
    private final Component title;

    private final IGuiHelper guiHelper;
    // Map d'association entre l'ID de recette et le layout handler correspondant
    private final Map<String, INitrogenCycleRecipeLayout> layoutHandlers = new HashMap<>();

    public NitrogenCycleCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;


        this.background = guiHelper.createBlankDrawable(160, 100);
        this.metaboBackground = guiHelper.drawableBuilder(new ResourceLocation(Bioreactor.MOD_ID, "textures/gui/jei/background_nitrogen_cycle.png"),
                        0, 0,168, 108).setTextureSize(168, 108).build();


        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BioreactorItems.NITROGEN_CYCLE_ICON));
        title = Component.translatable("jei.bioreactor.nitrogen_cycle.title");

        // Enregistrement des layout handlers pour chaque recette du cycle du N
        layoutHandlers.put("glucose_x_nitrate", new Layout_glucose_x_nitrate(guiHelper));


        // 7.1) cadre de spécificité
        metaboSpecificityFrame = guiHelper.drawableBuilder(new ResourceLocation(Bioreactor.MOD_ID,"textures/gui/jei/frame_nitrogen.png"),
                        0, 0, 28, 43).setTextureSize(28, 43).build();

        // 7.2) requireLight
        ResourceLocation rlTex = new ResourceLocation(Bioreactor.MOD_ID,"textures/gui/jei/require_light_logo.png");
        requireLightFalse = guiHelper.drawableBuilder(rlTex, 1, 0,  9, 16).setTextureSize(13, 33).build();
        requireLightTrue  = guiHelper.drawableBuilder(rlTex, 0, 19, 12, 13).setTextureSize(13, 33).build();

        // 7.3) o2 requirement
        ResourceLocation o2Tex = new ResourceLocation(Bioreactor.MOD_ID,"textures/gui/jei/o2_requirement_logo.png");
        o2AerobicIcon      = guiHelper.drawableBuilder(o2Tex, 0,  0, 37, 17).setTextureSize(37, 53).build();
        o2MicroIcon        = guiHelper.drawableBuilder(o2Tex, 0, 18, 37, 17).setTextureSize(37, 53).build();
        o2AnaerobicIcon    = guiHelper.drawableBuilder(o2Tex, 0, 36, 37, 17).setTextureSize(37, 53).build();


    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, NitrogenCycleRecipe recipe, mezz.jei.api.recipe.IFocusGroup focuses) {
        Metabolism metabolism = recipe.getMetabolism();
        String id = metabolism.getName();
        INitrogenCycleRecipeLayout layout = layoutHandlers.get(id);
        if (layout != null) {
            layout.setRecipe(builder, metabolism);
        }
    }

    @Override
    public void draw(NitrogenCycleRecipe recipe,
                     IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {

        // 1) fond commun
        metaboBackground.draw(guiGraphics, -4, -4);

        // 2) layout spécifique
        Metabolism metabolism = recipe.getMetabolism();
        INitrogenCycleRecipeLayout layout = layoutHandlers.get(metabolism.getName());
        if (layout != null) {
            layout.draw(guiGraphics, metabolism, mouseX, mouseY);
        }

        // 3) dessin des spécificités communes
        LayoutHelper.drawMetabolismSpecificities(
                guiGraphics,
                Minecraft.getInstance().font,
                metabolism,
                mouseX, mouseY,
                requireLightFalse, requireLightTrue,
                o2AerobicIcon,    o2MicroIcon,    o2AnaerobicIcon,
                metaboSpecificityFrame
        );

    }


    //---------- Getters ----------
    public ResourceLocation getUid() {return UID;}

    @Override
    public RecipeType<NitrogenCycleRecipe> getRecipeType() {return RECIPE_TYPE;}

    @Override
    public Component getTitle() {return title;}

    @Override
    public IDrawable getBackground() {return background;}

    @Override
    public IDrawable getIcon() {return icon;}

}
