package net.john.bioreactor.integration.jei.sampling;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.Sampling.SamplingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class SamplingCategory implements IRecipeCategory<SamplingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Bioreactor.MOD_ID, "sampling");
    public static final RecipeType<SamplingRecipe> RECIPE_TYPE = new RecipeType<>(UID, SamplingRecipe.class);

    private final IDrawable background, icon, slotBackground, sampling_arrow, depth_arrow;
    private final Component title;

    private static final ResourceLocation SAMPLING_CUBE_TEX = new ResourceLocation(Bioreactor.MOD_ID, "textures/gui/jei/air_block.png");
    private static final ResourceLocation SAMPLING_WATER_TEX = new ResourceLocation(Bioreactor.MOD_ID, "textures/gui/jei/water_block.png");


    private static final int
            DEPTH_ARROW_X  = 86 ,  DEPTH_ARROW_Y  = 8,
            COUNT_X = 92, COUNT_Y = 9 ;
    private static final float
            DEPTH_ARROW_SCALE = 0.60f,
            COUNT_SCALE = 0.60f ;


    public SamplingCategory(IGuiHelper guiHelper) {

        this.background = guiHelper.createBlankDrawable(150, 45); // taille du fond
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(BioreactorItems.SAMPLE_SOIL)); // icône de la catégorie JEI
        this.title = Component.translatable("jei.bioreactor.sampling.title");

        this.slotBackground = guiHelper.getSlotDrawable();

        this.sampling_arrow   = guiHelper.drawableBuilder(new ResourceLocation(Bioreactor.MOD_ID, "textures/gui/jei/sampling_arrow.png"),
                0, 0, 71, 9).setTextureSize(71, 9).build();
        this.depth_arrow   = guiHelper.drawableBuilder(new ResourceLocation(Bioreactor.MOD_ID, "textures/gui/jei/depth_arrow.png"),
                0, 0, 7, 30).setTextureSize(7, 30).build();


    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          SamplingRecipe recipe,
                          IFocusGroup focuses) {

        // 1) Input : bouteille ou seringue stérile × {oxic, anoxic}
        ItemStack baseInput = recipe.getMobSampled() != null
                ? new ItemStack(BioreactorItems.SYRINGE_EMPTY.get())
                : new ItemStack(Items.GLASS_BOTTLE);
        List<ItemStack> inputVariants = new ArrayList<>();

        for (String tag : List.of("oxic", "anoxic")) {
            ItemStack copy = baseInput.copy();
            var nbt = copy.getOrCreateTag();
            nbt.putBoolean(tag, true);
            nbt.putBoolean("sterile", true);
            copy.setTag(nbt);
            inputVariants.add(copy);
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 13, 28)
                .setBackground(slotBackground, -1, -1)
                .addItemStacks(inputVariants);

        // 2) Output : même NBT que l’input sélectionné
        ItemStack baseOutput = recipe.getOutputs().get(0).result.copy();
        List<ItemStack> outputVariants = new ArrayList<>();

        for (String tag : List.of("oxic", "anoxic")) {
            ItemStack copy = baseOutput.copy();
            var nbt = copy.getOrCreateTag();
            nbt.putBoolean(tag, true);
            copy.setTag(nbt);
            outputVariants.add(copy);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 28)
                .setBackground(slotBackground, -1, -1)
                .addItemStacks(outputVariants);
    }

    @Override
    public void draw(SamplingRecipe recipe,
                     IRecipeSlotsView slotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {

        Font font = Minecraft.getInstance().font;
        // Flèche
        sampling_arrow.draw(guiGraphics, 40, 30);


        // ----------------- BLOCK 3D ou ENTITY 3D -----------------
        PoseStack ms = guiGraphics.pose();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        final int centerblockX = 65, centerblockY = 38, zLevelblock = 10;
        final int aboveOffsetY = 11, aboveOfssetZ = 5;
        final int centerentityX = 75, centerentityY = 38 +5 , zLevelentity = 10;
        float blockScale = 14f;
        float rotX = 30f, rotY = 45f;

        // ---------------------------------------------------
        //                   Block Sampled
        //---------------------------------------------------

        if (!recipe.getBlockSampled().isEmpty()) {

            BlockState sampledState = recipe.getBlockSampled().get(0).defaultBlockState();
            Block blockSampled = recipe.getBlockSampled().get(0);
            if (sampledState.isAir() || blockSampled instanceof AirBlock) {
                renderPseudoBlockCube(guiGraphics, centerblockX +3 , centerblockY - 12, SAMPLING_CUBE_TEX, blockScale, rotX, rotY);


                ms.pushPose();
                ms.translate(72, 16, 0f);
                ms.scale(0.6f, 0.6f, 1f);
                guiGraphics.drawString(font, Component.literal("Air"), 0, 0, 0x000000, false);
                ms.popPose();


            } else {

                ms.pushPose();

                // coord, taille et 3D
                ms.translate(centerblockX, centerblockY, zLevelblock);
                ms.scale(blockScale, -blockScale, blockScale);
                ms.mulPose(Axis.XP.rotationDegrees(rotX));
                ms.mulPose(Axis.YP.rotationDegrees(rotY));

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(sampledState, ms, buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                buffers.endBatch();
                ms.popPose();
            }



        }

        // ---------------------------------------------------
        //                   Above Block
        //---------------------------------------------------
        var ctx = recipe.getContextReq();
        if (ctx != null && ctx.aboveBlock() != null) {

            // 1) block 3D
            BlockState aboveState = ctx.aboveBlock().defaultBlockState();
            Block above = ctx.aboveBlock();

            if (above != null && (above.defaultBlockState().isAir() || above instanceof AirBlock)) {
                renderPseudoBlockCube(guiGraphics, centerblockX + 3, centerblockY - 25, SAMPLING_CUBE_TEX, blockScale, rotX, rotY);

                ms.pushPose();
                ms.translate(88, 13, 0f);
                ms.scale(0.6f, 0.6f, 1f);
                guiGraphics.drawString(font, Component.literal("Air"), 0, 0, 0x000000, false);
                ms.popPose();


            } else {
                ms.pushPose();

                // coord, taille et 3D
                ms.translate(centerblockX, centerblockY - aboveOffsetY, zLevelblock + aboveOfssetZ);
                ms.scale(blockScale, -blockScale, blockScale);
                ms.mulPose(Axis.XP.rotationDegrees(rotX));
                ms.mulPose(Axis.YP.rotationDegrees(rotY));

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(aboveState, ms, buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                buffers.endBatch();
                ms.popPose();
            }

            int count = ctx.aboveBlockCount() != null ? ctx.aboveBlockCount() : 1;
            if (count > 1){

                // 2) flèche
                ms.pushPose();
                ms.translate(DEPTH_ARROW_X, DEPTH_ARROW_Y, 0);
                ms.scale(DEPTH_ARROW_SCALE, DEPTH_ARROW_SCALE, 1f);
                depth_arrow.draw(guiGraphics, 0, 0);
                ms.popPose();

                // 3) Texte et Nombre
                Component countNum = Component.literal(Integer.toString(count))
                        .withStyle(ChatFormatting.BOLD);
                Component countText = Component.translatable("jei.bioreactor.sampling.count", countNum);

                ms.pushPose();
                ms.translate(COUNT_X, COUNT_Y, 0f);
                ms.scale(COUNT_SCALE, COUNT_SCALE, 1f);
                guiGraphics.drawString(font, countText, 0, 0, 0x000000, false);
                ms.popPose();
            }

        }

        // ---------------------------------------------------
        //                   Deep Block
        // ---------------------------------------------------
        if (ctx != null && ctx.deepBlock() != null) {

            BlockState deepaboveState = ctx.deepBlock().defaultBlockState();
            Block deepabove = ctx.aboveBlock();
            int count = ctx.deepThreshold() != null ? ctx.deepThreshold() : 1;

            if (deepabove != null && (deepabove.defaultBlockState().isAir() || deepabove instanceof AirBlock)) {
                renderPseudoBlockCube(guiGraphics, centerblockX + 3, centerblockY - 25, SAMPLING_CUBE_TEX, blockScale, rotX, rotY);

            } else {
                ms.pushPose();

                // 1) block 3D : coord, taille et 3D
                ms.translate(centerblockX, centerblockY - aboveOffsetY, zLevelblock + aboveOfssetZ);
                ms.scale(blockScale, -blockScale, blockScale);
                ms.mulPose(Axis.XP.rotationDegrees(rotX));
                ms.mulPose(Axis.YP.rotationDegrees(rotY));

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(deepaboveState, ms, buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
                buffers.endBatch();
                ms.popPose();


                // 2) flèche
                ms.pushPose();
                ms.translate(DEPTH_ARROW_X, DEPTH_ARROW_Y, 0);
                ms.scale(DEPTH_ARROW_SCALE, DEPTH_ARROW_SCALE, 1f);
                depth_arrow.draw(guiGraphics, 0, 0);
                ms.popPose();

                // 3) Texte et Nombre
                Component countNum = Component.literal(Integer.toString(count))
                        .withStyle(ChatFormatting.BOLD);
                Component countText = Component.translatable("jei.bioreactor.sampling.count", countNum);

                ms.pushPose();
                ms.translate(COUNT_X, COUNT_Y, 0f);
                ms.scale(COUNT_SCALE, COUNT_SCALE, 1f);
                guiGraphics.drawString(font, countText, 0, 0, 0x000000, false);
                ms.popPose();



            }

        }

        // ---------------------------------------------------
        //                   Above fluid
        // ---------------------------------------------------
        if (ctx != null && ctx.fluidAbove() != null) {

            // 1) block 3D. ⚠⚠⚠ Ne gère que l'eau : on utilise directement le png “water_block”
            renderPseudoBlockCube(guiGraphics, centerblockX + 3,centerblockY - 25, SAMPLING_WATER_TEX, blockScale, rotX, rotY);


            int count = ctx.fluidAboveCount() != null ? ctx.fluidAboveCount() : 1;
            if (count > 1){

                // 2) flèche
                ms.pushPose();
                ms.translate(DEPTH_ARROW_X, DEPTH_ARROW_Y, 0);
                ms.scale(DEPTH_ARROW_SCALE, DEPTH_ARROW_SCALE, 1f);
                depth_arrow.draw(guiGraphics, 0, 0);
                ms.popPose();

                // 3) Texte et Nombre
                Component countNum = Component.literal(Integer.toString(count))
                        .withStyle(ChatFormatting.BOLD);
                Component countText = Component.translatable("jei.bioreactor.sampling.count", countNum);

                ms.pushPose();
                ms.translate(COUNT_X, COUNT_Y, 0f);
                ms.scale(COUNT_SCALE, COUNT_SCALE, 1f);
                guiGraphics.drawString(font, countText, 0, 0, 0x000000, false);
                ms.popPose();
            }



        }

        // ---------------------------------------------------
        //                   Deep fluid
        // ---------------------------------------------------
        if (ctx != null && ctx.deepFluid() != null) {

            // block 3D. ⚠⚠⚠ Ne gère que l'eau : on utilise directement le png “water_block”
            renderPseudoBlockCube(guiGraphics, centerblockX + 3,centerblockY - 25, SAMPLING_WATER_TEX, blockScale, rotX, rotY);

            int count = ctx.deepThreshold() != null ? ctx.deepThreshold() : 1;
            if (count > 1){

                // 2) flèche
                ms.pushPose();
                ms.translate(DEPTH_ARROW_X, DEPTH_ARROW_Y, 0);
                ms.scale(DEPTH_ARROW_SCALE, DEPTH_ARROW_SCALE, 1f);
                depth_arrow.draw(guiGraphics, 0, 0);
                ms.popPose();

                // 3) Texte et Nombre
                Component countNum = Component.literal(Integer.toString(count))
                        .withStyle(ChatFormatting.BOLD);
                Component countText = Component.translatable("jei.bioreactor.sampling.count", countNum);

                ms.pushPose();
                ms.translate(COUNT_X, COUNT_Y, 0f);
                ms.scale(COUNT_SCALE, COUNT_SCALE, 1f);
                guiGraphics.drawString(font, countText, 0, 0, 0x000000, false);
                ms.popPose();
            }
        }


        // ---------------------------------------------------
        //                   NBT : salinity / pH
        //---------------------------------------------------
        if (ctx != null && ctx.requiredNbt() != null) {

            // salinity
            Integer sal = ctx.requiredNbt().get("salinity");
            if (sal != null) {
                String key = sal == 0    ? "tooltip.bioreactor.freshwater"
                                         : "tooltip.bioreactor.seawater" ;
                Component salText = Component.translatable(key);
                int salColor = sal == 0  ? 0x70ACDB
                                         : 0x00008B ;

                ms.pushPose();
                ms.translate(COUNT_X, COUNT_Y + 8 , 0f);
                ms.scale(COUNT_SCALE, COUNT_SCALE, 1f);
                guiGraphics.drawString(font, salText, 0, 0, salColor, false);
                ms.popPose();


            }

        }

        // ---------------------------------------------------
        //                   Height
        //---------------------------------------------------
        if (ctx != null &&
            ctx.minY() != null &&
            ctx.maxY() != null) {

            int minY = ctx.minY(), maxY = ctx.maxY();
            Component maxText =
                    Component.literal("Max Y level : ")
                    .append(Component.literal(Integer.toString(maxY)).withStyle(ChatFormatting.BOLD));
            Component minText =
                    Component.literal("Min Y level : ")
                    .append(Component.literal(Integer.toString(minY)).withStyle(ChatFormatting.BOLD));

            ms.pushPose();
            ms.translate(30, 3, 0f);
            ms.scale(0.5f, 0.5f, 1f);
            guiGraphics.drawString(font, maxText, 100, 0, 0x000000, false);
            guiGraphics.drawString(font, minText, 0, 0, 0x000000, false);
            ms.popPose();

        }



        // ---------------------------------------------------
        //                   Entity sampling
        // ---------------------------------------------------
        else if (recipe.getMobSampled() != null) {
            var mobRL = recipe.getMobSampled();
            var entityType = ForgeRegistries.ENTITY_TYPES.getValue(mobRL);

            if (entityType != null && Minecraft.getInstance().level != null) {
                var entity = entityType.create(Minecraft.getInstance().level);

                if (entity != null) {
                    ms.pushPose();

                    // coord, taille et 3D
                    ms.translate(centerentityX, centerentityY, zLevelentity);
                    float entityScale = 15f;
                    ms.scale(entityScale, -entityScale, entityScale);
                    ms.mulPose(Axis.YP.rotationDegrees(45f));

                    // rendering immobile : partialTicks = 0 → pas d’interpolation ni de head bobbing
                    Minecraft.getInstance().getEntityRenderDispatcher().render(entity,0.0, 0.0, 0.0, 0.0f,0.0f, ms,buffers,LightTexture.FULL_BRIGHT);
                    buffers.endBatch();
                    ms.popPose();
                }
            }
        }


    }



    /**
     * Dessine un cube isométrique « fake » avec le RenderType adapté à la texture.
     */
    private void renderPseudoBlockCube(GuiGraphics gui,
                                       int x, int y,
                                       ResourceLocation tex,
                                       float texSize,
                                       float rotX, float rotY) {
        PoseStack ms = gui.pose();
        ms.pushPose();

        // Position et orientation du cube
        ms.translate(x + texSize / 2f, y + texSize / 2f, 100f);
        float scale = texSize / 2.05f;
        ms.scale(scale, -scale, scale);
        ms.mulPose(Axis.XP.rotationDegrees(30f));
        ms.mulPose(Axis.YP.rotationDegrees(45f));

        // Activation du blending et depth-test
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Choix du shader et du RenderType lié à la texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType type = RenderType.entityTranslucent(tex);
        VertexConsumer vb = buffers.getBuffer(type);

        // Récupération de la matrice de transformation
        Matrix4f mat = ms.last().pose();

        // --- 1) Face avant (+Z) ---
        drawTexturedQuad(vb, mat,
                -1f, -1f,  1f,
                1f, -1f,  1f,
                1f,  1f,  1f,
                -1f,  1f,  1f,
                0f,  0f,  1f);

        // --- 2) Face arrière (–Z) ---
        drawTexturedQuad(vb, mat,
                1f, -1f, -1f,
                -1f, -1f, -1f,
                -1f,  1f, -1f,
                1f,  1f, -1f,
                0f,  0f, -1f);

        // --- 3) Face droite (+X) ---
        drawTexturedQuad(vb, mat,
                1f, -1f,  1f,
                1f, -1f, -1f,
                1f,  1f, -1f,
                1f,  1f,  1f,
                1f,  0f,  0f);

        // --- 4) Face gauche (–X) ---
        drawTexturedQuad(vb, mat,
                -1f, -1f, -1f,
                -1f, -1f,  1f,
                -1f,  1f,  1f,
                -1f,  1f, -1f,
                -1f,  0f,  0f);

        // --- 5) Face dessus (+Y) ---
        drawTexturedQuad(vb, mat,
                -1f,  1f, -1f,
                -1f,  1f,  1f,
                1f,  1f,  1f,
                1f,  1f, -1f,
                0f,  1f,  0f);

        // --- 6) Face dessous (–Y) ---
        drawTexturedQuad(vb, mat,
                -1f, -1f, -1f,
                1f, -1f, -1f,
                1f, -1f,  1f,
                -1f, -1f,  1f,
                0f, -1f,  0f);

        // Flush du buffer pour ce type
        buffers.endBatch(type);

        // Restauration de l’état
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        ms.popPose();
    }

    private static void drawTexturedQuad(VertexConsumer b,
                                         Matrix4f mat,
                                         float x1, float y1, float z1,
                                         float x2, float y2, float z2,
                                         float x3, float y3, float z3,
                                         float x4, float y4, float z4,
                                         float nx, float ny, float nz) {
        b.vertex(mat, x1, y1, z1).color(1f,1f,1f,1f).uv(0f,1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(nx,ny,nz).endVertex();
        b.vertex(mat, x2, y2, z2).color(1f,1f,1f,1f).uv(1f,1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(nx,ny,nz).endVertex();
        b.vertex(mat, x3, y3, z3).color(1f,1f,1f,1f).uv(1f,0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(nx,ny,nz).endVertex();
        b.vertex(mat, x4, y4, z4).color(1f,1f,1f,1f).uv(0f,0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(nx,ny,nz).endVertex();
    }



    //---------- Getters ----------
    public ResourceLocation getUid() {return UID;}
    @Override public RecipeType<SamplingRecipe> getRecipeType() {return RECIPE_TYPE;}

    @Override public Component getTitle() {return title;}

    @Override public IDrawable getBackground() {return background;}

    @Override public IDrawable getIcon() {return icon;}


}
