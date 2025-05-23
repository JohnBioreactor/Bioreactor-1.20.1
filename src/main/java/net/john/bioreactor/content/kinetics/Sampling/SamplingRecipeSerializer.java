package net.john.bioreactor.content.kinetics.Sampling;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class SamplingRecipeSerializer implements RecipeSerializer<SamplingRecipe> {

    @Override
    public SamplingRecipe fromJson(ResourceLocation id, JsonObject json) {

        // --- block_sampled (optionnel) ---
        List<Block> blocks = new ArrayList<>();
        if (json.has("block_sampled")) {
            JsonArray arr = GsonHelper.getAsJsonArray(json, "block_sampled");
            for (JsonElement e : arr) {
                Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e.getAsString()));
                if (b != null) {
                    blocks.add(b);
                }
            }
        }

        // mob_sampled
        ResourceLocation mobId = json.has("mob_sampled") ? new ResourceLocation(GsonHelper.getAsString(json, "mob_sampled")) : null;

        // ---------- context ----------
        SamplingRecipe.ContextRequirement ctxReq = null;
        if (json.has("context")) {
            JsonObject c = json.getAsJsonObject("context");

            // above_block
            Block aboveBlock = null;
            Integer aboveBlockCount = null;
            if (c.has("above_block")) {
                aboveBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GsonHelper.getAsString(c, "above_block")));
                aboveBlockCount = GsonHelper.getAsInt(c, "count", 1); // Par défaut 1 si non spécifié
            }

            // fluid_above
            ResourceLocation fluidAbove = null;
            Integer fluidAboveCount = null;
            if (c.has("fluid_above")) {
                JsonObject fa = c.getAsJsonObject("fluid_above");
                fluidAbove = new ResourceLocation(GsonHelper.getAsString(fa, "name"));
                fluidAboveCount = GsonHelper.getAsInt(fa, "count", 1);
            }

            // deep_block
            Block deepBlock = null;
            if (c.has("deep_block")) {
                deepBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(GsonHelper.getAsString(c, "deep_block")));
            }

            // deep_fluid
            ResourceLocation deepFluid = null;
            if (c.has("deep_fluid")) {
                deepFluid = new ResourceLocation(GsonHelper.getAsString(c, "deep_fluid"));
            }

            // deep_threshold
            Integer deepThreshold = null;
            if (c.has("deep_threshold")) {
                deepThreshold = GsonHelper.getAsInt(c, "deep_threshold", 15);
            }

            // nbt
            Map<String, Integer> tags = new HashMap<>();
            if (c.has("nbt")) {
                for (var ent : c.getAsJsonObject("nbt").entrySet()) {
                    tags.put(ent.getKey(), ent.getValue().getAsInt());
                }
            }

            // height
            Integer minY = null, maxY = null;
            if (c.has("height")) {
                JsonObject h = c.getAsJsonObject("height");
                minY = GsonHelper.getAsInt(h, "min", Integer.MIN_VALUE);
                maxY = GsonHelper.getAsInt(h, "max", Integer.MAX_VALUE);
            }

            // constructeur
            ctxReq = new SamplingRecipe.ContextRequirement(
                aboveBlock, aboveBlockCount,
                fluidAbove, fluidAboveCount,
                deepBlock, deepFluid, deepThreshold,
                tags,
                minY, maxY
            );
        }

        // outputs
        List<SamplingRecipe.Output> outputs = new ArrayList<>();
        for (JsonElement e : json.getAsJsonArray("outputs")) {
            JsonObject o = e.getAsJsonObject();

            //Item
            ItemStack stack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(o, "item")))));

            // Probabilité : "chance"
            float chance = GsonHelper.getAsFloat(o, "chance", 1.0F);

            outputs.add(new SamplingRecipe.Output(stack, chance));
        }

        return new SamplingRecipe(id, blocks, mobId, ctxReq, outputs);
    }

    @Override
    public SamplingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

        // --- blocks ---
        int bCount = buf.readInt();
        List<Block> blocks = new ArrayList<>();
        for (int i = 0; i < bCount; i++) {
            Block b = ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation());
            if (b != null) {
                blocks.add(b);
            }
        }

        // mob
        ResourceLocation mobId = buf.readBoolean() ? buf.readResourceLocation() : null;

        // context
        SamplingRecipe.ContextRequirement ctxReq = null;
        if (buf.readBoolean()) {

            // Block above
            Block aboveBlock = buf.readBoolean() ? ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()) : null;
            Integer aboveBlockCount = buf.readBoolean() ? buf.readInt() : null;

            // fluid above
            ResourceLocation fluidAbove = buf.readBoolean() ? buf.readResourceLocation() : null;
            Integer fluidAboveCount = buf.readBoolean() ? buf.readInt() : null;

            // Deep fluid and deep block
            Block deepBlock = buf.readBoolean() ? ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()) : null;
            ResourceLocation deepFluid = buf.readBoolean() ? buf.readResourceLocation() : null;
            Integer deepThreshold = buf.readBoolean() ? buf.readInt() : null;

            // NBT
            int tnbt = buf.readInt();
            Map<String, Integer> tags = new HashMap<>();
            for (int i = 0; i < tnbt; i++) {
                tags.put(buf.readUtf(), buf.readInt());
            }

            // hauteur
            Integer minY = buf.readBoolean() ? buf.readInt() : null;
            Integer maxY = buf.readBoolean() ? buf.readInt() : null;

            ctxReq = new SamplingRecipe.ContextRequirement(
                aboveBlock, aboveBlockCount,
                fluidAbove, fluidAboveCount,
                deepBlock, deepFluid, deepThreshold,
                tags,
                minY, maxY
            );
        }

        // outputs
        int oCount = buf.readInt();
        List<SamplingRecipe.Output> outputs = new ArrayList<>();
        for (int i = 0; i < oCount; i++) {
            ItemStack stack = buf.readItem();
            float chance = buf.readFloat();
            outputs.add(new SamplingRecipe.Output(stack, chance));
        }

        return new SamplingRecipe(id, blocks, mobId, ctxReq, outputs);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, SamplingRecipe recipe) {

        // blocks
        buf.writeInt(recipe.getBlockSampled().size());
        for (Block b : recipe.getBlockSampled()) {
            buf.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(b)));
        }

        // mob
        buf.writeBoolean(recipe.getMobSampled() != null);
        if (recipe.getMobSampled() != null)
            buf.writeResourceLocation(recipe.getMobSampled());

        // context
        buf.writeBoolean(recipe.getContextReq() != null);
        if (recipe.getContextReq() != null) {
            var c = recipe.getContextReq();

            // Above block
            buf.writeBoolean(c.aboveBlock() != null);
            if (c.aboveBlock() != null) buf.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(c.aboveBlock()));
            buf.writeBoolean(c.aboveBlockCount() != null);
            if (c.aboveBlockCount() != null) buf.writeInt(c.aboveBlockCount());

            // Fluid above
            buf.writeBoolean(c.fluidAbove() != null);
            if (c.fluidAbove() != null) buf.writeResourceLocation(c.fluidAbove());
            buf.writeBoolean(c.fluidAboveCount() != null);
            if (c.fluidAboveCount() != null) buf.writeInt(c.fluidAboveCount());

            // Deep block et deep fluid
            buf.writeBoolean(c.deepBlock() != null);
            if (c.deepBlock() != null) buf.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(c.deepBlock()));
            buf.writeBoolean(c.deepFluid() != null);
            if (c.deepFluid() != null) buf.writeResourceLocation(c.deepFluid());
            buf.writeBoolean(c.deepThreshold() != null);
            if (c.deepThreshold() != null) buf.writeInt(c.deepThreshold());
            buf.writeInt(c.requiredNbt().size());

            // NBT
            for (var e : c.requiredNbt().entrySet()) {
                buf.writeUtf(e.getKey());
                buf.writeInt(e.getValue());
            }

            // hauteur
            buf.writeBoolean(c.minY() != null);
            if (c.minY() != null) buf.writeInt(c.minY());
            buf.writeBoolean(c.maxY() != null);
            if (c.maxY() != null) buf.writeInt(c.maxY());
        }

        // outputs
        buf.writeInt(recipe.getOutputs().size());
        for (var o : recipe.getOutputs()) {
            buf.writeItem(o.result);    // item
            buf.writeFloat(o.chance);   // probabilité
        }
    }
}