package net.john.bioreactor.integration.jei.metabolism.nitrogen_cycle;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.metabolism.Metabolism;
import net.john.bioreactor.integration.jei.metabolism.ArrowHelper;
import net.john.bioreactor.integration.jei.metabolism.LayoutHelper;
import net.john.bioreactor.integration.jei.metabolism.RecipeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Layout_glucose_x_nitrate implements INitrogenCycleRecipeLayout {

    private final IDrawable slotBackground, electronLogo;

    private static final IIngredientType<FluidStack> FLUID_STACK = new IIngredientType<>() {
        @Override public Class<? extends FluidStack> getIngredientClass() {
            return FluidStack.class;
        }
    };

    // coordonnées statiques
    private static final int OUTPUT_CO2_X = 10;
    private static final int OUTPUT_CO2_Y = 83;

    private static final int OUTPUT_H2S_X = 120;
    private static final int OUTPUT_H2S_Y = 83;

    public Layout_glucose_x_nitrate(IGuiHelper guiHelper) {
        this.slotBackground = guiHelper.getSlotDrawable();
        this.electronLogo   = guiHelper.drawableBuilder(new ResourceLocation(Bioreactor.MOD_ID, "textures/gui/jei/electron.png"),0, 0, 32, 32).setTextureSize(32, 32).build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Metabolism metabolism) {

        // Item en entrée : glucose
        if (!metabolism.getRequiredItems().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 17)
                    .setBackground(slotBackground, -1, -1)
                    .addItemStack(metabolism.getRequiredItems().get(0));
        }

        // Item en entrée : nitrate
        if (!metabolism.getRequiredItems().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 120, 17)
                    .setBackground(slotBackground, -1, -1)
                    .addItemStack(metabolism.getRequiredItems().get(1));
        }



        // Fluide en sortie : CO₂
        List<FluidStack> outFluids = RecipeHelper.getFluidStacks(metabolism.getOutputFluids());
        for (FluidStack fs : outFluids) {
            String key = ForgeRegistries.FLUIDS.getKey(fs.getFluid()).toString();
            if ("bioreactor:gaz_co2_source".equals(key)) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_CO2_X, OUTPUT_CO2_Y)
                        .setBackground(slotBackground, -1, -1)
                        .addFluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
            }
        }

        // Fluide en sortie : H₂S
        for (FluidStack fs : outFluids) {
            String key = ForgeRegistries.FLUIDS.getKey(fs.getFluid()).toString();
            if ("bioreactor:gaz_h2s_source".equals(key)) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_H2S_X, OUTPUT_H2S_Y)
                        .setBackground(slotBackground, -1, -1)
                        .addFluidStack(fs.getFluid(), fs.getAmount(), fs.getTag());
            }
        }


    }


    @Override
    public void draw(GuiGraphics graphics, Metabolism metabolism, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;


        //region 1) ---------------- Titre ----------------
        graphics.pose().pushPose();
        graphics.pose().scale(0.75F, 0.75F, 1F);
        graphics.drawString(
                font,
                Component.translatable("jei.bioreactor.carbon_cycle.header.glucose_x_o2")
                        .withStyle(ChatFormatting.UNDERLINE),
                (int)(2 / 0.75F),
                -2,
                0x404040,
                false
        );
        graphics.pose().popPose();
        //endregion

        // 2) ----- Sous‑titre : C/e- donor/acceptor ; organo/litho et hétéro/Auto-trophe ----------------
        //region   ***Texte "C and electron donor"     &     tooltip "Organotrophy" + "Hétérotrophy"***

        String C_electron_donor_key = "jei.bioreactor.trophictype.C_electron_donor";
        Component C_and_electron_donor_tip1 = Component.translatable("jei.bioreactor.tooltip.electron_donor.organotroph");
        Component C_and_electron_donor_tip2 = Component.translatable("jei.bioreactor.tooltip.C_donor.heterotroph");
        int C_and_electron_donor_X = 8;
        int C_and_electron_donor_Y = 10;
        float C_and_electron_donor_scale = 0.55F;
        int C_and_electron_donor_tooltip_maxWidth = 200;

        // Texte
        graphics.pose().pushPose();
        graphics.pose().scale(C_and_electron_donor_scale, C_and_electron_donor_scale, 1F);
        graphics.drawString(font,Component.translatable(C_electron_donor_key).withStyle(ChatFormatting.BOLD),
                (int)(C_and_electron_donor_X / C_and_electron_donor_scale),(int)(C_and_electron_donor_Y / C_and_electron_donor_scale),0x404040,false);
        graphics.pose().popPose();

        // Hit-box + Tooltip
        int C_electron_donor_key_textWidth  = font.width(Component.translatable(C_electron_donor_key).withStyle(ChatFormatting.BOLD));
        int C_electron_donor_key_textHeight = font.lineHeight;

        if (mouseX >= C_and_electron_donor_X && mouseX <= C_and_electron_donor_X + (C_electron_donor_key_textWidth  * C_and_electron_donor_scale) && mouseY >= C_and_electron_donor_Y && mouseY <= C_and_electron_donor_Y + (C_electron_donor_key_textHeight * C_and_electron_donor_scale)) {

            List<FormattedCharSequence> C_and_electron_donor_lines = new ArrayList<>();
            C_and_electron_donor_lines.addAll(mc.font.split(C_and_electron_donor_tip1, C_and_electron_donor_tooltip_maxWidth));
            C_and_electron_donor_lines.addAll(mc.font.split(C_and_electron_donor_tip2, C_and_electron_donor_tooltip_maxWidth));

            graphics.renderTooltip(font, C_and_electron_donor_lines, (int) mouseX, (int) mouseY);
        }
        //endregion

        //region   ***Texte "electron "Acceptor"     &     tolltip : "Respi Aérobie/Anaérobie"***
        String electron_acceptor_key = "jei.bioreactor.trophictype.electron_acceptor";
        Component electron_acceptor_oxygen = Component.translatable("jei.bioreactor.tooltip.electron_acceptor.oxygen");
        Component electron_acceptor_not_oxygen = Component.translatable("jei.bioreactor.tooltip.electron_acceptor.not_oxygen");

        int electron_acceptor_X = 115; // ⚠ modify this ⚠     xx pour milieu ; 115 pour à droite
        int electron_acceptor_Y = 10;
        float electron_acceptor_scale = 0.55F;
        int electron_acceptor_tooltip_maxWidth = 200;

        // Texte
        graphics.pose().pushPose();
        graphics.pose().scale(electron_acceptor_scale, electron_acceptor_scale, 1F);
        graphics.drawString(font,Component.translatable(electron_acceptor_key).withStyle(ChatFormatting.BOLD),
                (int)(electron_acceptor_X / electron_acceptor_scale),(int)(electron_acceptor_Y / electron_acceptor_scale),0x404040,false);
        graphics.pose().popPose();

        // Hit-box + Tooltip
        int electron_acceptor_key_textWidth  = font.width(Component.translatable(electron_acceptor_key).withStyle(ChatFormatting.BOLD));
        int electron_acceptor_key_textHeight = font.lineHeight;

        if (mouseX >= electron_acceptor_X && mouseX <= electron_acceptor_X + (electron_acceptor_key_textWidth  * electron_acceptor_scale) && mouseY >= electron_acceptor_Y && mouseY <= electron_acceptor_Y + (electron_acceptor_key_textHeight * electron_acceptor_scale)) {

            // extraction des FluidStack d’entrée
            List<FluidStack> inFluids = RecipeHelper.getFluidStacks(metabolism.getRequiredFluids());

            // détection du gaz air
            boolean electron_acceptor_hasAir = inFluids.stream().anyMatch(fs -> Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fs.getFluid())).toString()
                                    .equals("bioreactor:gaz_air_source")
                    );

            // sélection de la clé de traduction : Respiration aérobie ou anaérobie
            Component electron_acceptor_tip = electron_acceptor_hasAir ? electron_acceptor_oxygen : electron_acceptor_not_oxygen;

            List<FormattedCharSequence> electron_acceptor_lines = mc.font.split(electron_acceptor_tip, electron_acceptor_tooltip_maxWidth);
            graphics.renderTooltip(mc.font, electron_acceptor_lines, (int) mouseX, (int) mouseY);
        }
        //endregion


        //region 3) ---------------- Autres textes ---------------
        graphics.pose().pushPose();
        graphics.pose().scale(0.70F, 0.70F, 1F);
        graphics.drawString(font,
                Component.translatable("jei.bioreactor.cell_carbon"),
                (int)(60  / 0.70F),
                (int)(45  / 0.70F),
                0x404040,
                false
        );
        graphics.drawString(font,
                Component.literal("H₂O"),
                (int)(124 / 0.70F),
                (int)(90  / 0.70F),
                0x404040,
                false
        );
        graphics.pose().popPose();
        //endregion

        //region 4) ---------------- Flèches ----------------
        ArrowHelper.draw(graphics, "straight", "blue",   17, 30, 17, 78);
        ArrowHelper.draw(graphics, "straight", "black",  19, 50, 53, 50);
        ArrowHelper.draw(graphics, "straight", "red",   127, 30,127, 78);
        ArrowHelper.draw(graphics, "straight","yellow", 19, 65,123, 65);
        //endregion

        //region 5) ---------------- Logos ----------------
        // electron
        int[][] positions = {
                {23, 15}, {100, 44}, {77, 56}, {135, 81}
        };
        LayoutHelper.drawElectronLogos(graphics, electronLogo, 6f, positions);
        //endregion

        //region 6) ---------------- Quantités fluides ----------------
        // Sortie
        List<FluidStack> outFluids = RecipeHelper.getFluidStacks(metabolism.getOutputFluids());
        for (FluidStack fs : outFluids) {
            String key = Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fs.getFluid())).toString();
            if ("bioreactor:gaz_co2_source".equals(key)) {LayoutHelper.drawFluidAmount(graphics, font, slotBackground,
                        OUTPUT_CO2_X, OUTPUT_CO2_Y,
                    fs);
            }

            if ("bioreactor:gaz_h2s_source".equals(key)) {LayoutHelper.drawFluidAmount(graphics, font, slotBackground,
                    OUTPUT_H2S_X, OUTPUT_H2S_Y,
                    fs);
            }
        }

        //endregion


    }



}
