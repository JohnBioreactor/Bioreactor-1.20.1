package net.john.bioreactor.content.kinetics.Sampling;

import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.WaterDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public class SamplingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final List<Block> blockSampled;            // facultatif
    private final ResourceLocation mobSampled;          // facultatif
    private final ContextRequirement contextReq;        // facultatif
    private final List<Output> outputs;                 // obligatoire

    public SamplingRecipe(ResourceLocation id,
                          List<Block> blockSampled,
                          ResourceLocation mobSampled,
                          ContextRequirement contextReq,
                          List<Output> outputs) {
        this.id = id;
        this.blockSampled = blockSampled;
        this.mobSampled = mobSampled;
        this.contextReq = contextReq;
        this.outputs = outputs;
    }

    /** Contexte d’exécution */
    public static class SamplingContext {
        public final Level level;
        public final BlockPos pos;
        public final BlockState blockState;
        public final ItemStack itemStack;
        public final Entity targetEntity;  // null si clic sur bloc

        public SamplingContext(Level level,
                               BlockPos pos,
                               BlockState blockState,
                               ItemStack itemStack,
                               Entity targetEntity) {
            this.level = level;
            this.pos = pos;
            this.blockState = blockState;
            this.itemStack = itemStack;
            this.targetEntity = targetEntity;
        }
    }

    /**
     * Contrainte “context” :
     * - aboveBlock : type de bloc au-dessus
     * - aboveBlockCount : nombre de blocs au-dessus
     * - fluidAbove : fluide au-dessus
     * - fluidAboveCount : nombre de fluides au-dessus
     * - deepBlock : bloc pour submersion profonde
     * - deepFluid : fluide pour submersion profonde
     * - deepThreshold : seuil pour submersion
     * - requiredNbt : tags requis (salinity, pH)
     * - minY/maxY : hauteur du clic
     */
    public record ContextRequirement(
        Block aboveBlock,
        Integer aboveBlockCount,
        ResourceLocation fluidAbove,
        Integer fluidAboveCount,
        Block deepBlock,
        ResourceLocation deepFluid,
        Integer deepThreshold,
        Map<String, Integer> requiredNbt,
        Integer minY,
        Integer maxY
    ) {}

    /** Structure de sortie */
    public static class Output {
        public final ItemStack result;
        public final float chance;
        public Output(ItemStack result, float chance) {
            this.result = result;
            this.chance = chance;
        }
    }

    public List<Block> getBlockSampled() { return blockSampled; }
    public ResourceLocation getMobSampled() { return mobSampled; }
    public ContextRequirement getContextReq() { return contextReq; }
    public List<Output> getOutputs() { return outputs; }

    /**
     * Logique “matches” : bouteille stérile + contexte > mob > bloc
     */
    public boolean matches(SamplingContext ctx) {
        String prefix = "[SamplingRecipe " + id + "]";

        // 0) TOUJOURS exiger une bouteille ou une seringue stérile
        Item inItem = ctx.itemStack.getItem();
        if (inItem != Items.GLASS_BOTTLE   &&  inItem != BioreactorItems.SYRINGE_EMPTY.get())
            return false;

        CompoundTag inTag = ctx.itemStack.getTag();
        if (inTag == null || !inTag.getBoolean("sterile"))
            return false;

        // 1) if this recipe lists specific blocks, enforce it
        if (!blockSampled.isEmpty() &&
                !blockSampled.contains(ctx.blockState.getBlock())) {
            System.out.println(prefix + " false at block sampled: " + ctx.blockState.getBlock());
            return false;
        }

        // 2) if this recipe lists a mob, enforce it
        if (mobSampled != null) {
            if (ctx.targetEntity == null) {
                System.out.println("[SamplingRecipe.java] " + prefix + " false, entity is null");
                return false;
            }
            ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(ctx.targetEntity.getType());
            if (!mobSampled.equals(rl)) {
                System.out.println("[SamplingRecipe.java] " +prefix + " false, entity mismatch: targeted " + rl + ", required " + mobSampled);
                return false;
            }
        }

        // 3) if there is a context section, enforce all of its sub‑constraints
        if (contextReq != null) {
            BlockPos pos = ctx.pos;

            // 3.a) Hauteur
            if (contextReq.minY() != null && contextReq.maxY() != null) {
                int y = pos.getY();
                if (y < contextReq.minY() || y > contextReq.maxY()) {

                    System.out.println(prefix + " Context check: Height NOT ok (Y=" + y
                            + " out of [" + contextReq.minY() + ";" + contextReq.maxY() + "])");
                    return false;
                } else {

                    System.out.println(prefix + " Context check: Height OK (Y=" + y
                            + " in [" + contextReq.minY() + ";" + contextReq.maxY() + "])");
                }
            }

            // 3.b) Above block
            if (contextReq.aboveBlock() != null) {
                BlockPos p = pos.above();
                int count = contextReq.aboveBlockCount() != null ? contextReq.aboveBlockCount() : 1;
                for (int i = 0; i < count; i++) {
                    BlockState aboveState = ctx.level.getBlockState(p);
                    if (aboveState.getBlock() != contextReq.aboveBlock()) {
                        System.out.println(prefix + " Above block fail at +" + (i + 1) + ": expected " + contextReq.aboveBlock() + ", found " + aboveState.getBlock());
                        return false;
                    }
                    p = p.above();
                }
                // si "block above" est de l'air, ne pas regarder le nombre de block au dessus
                if (contextReq.aboveBlock() != Blocks.AIR) {
                    BlockState nextAbove = ctx.level.getBlockState(p);
                    if (nextAbove.getBlock() == contextReq.aboveBlock()) {

                        System.out.println(prefix + " Too many above blocks");
                        return false;
                    }
                }
            }

            // 3.c) Above fluid avec NBT specifique
            if (contextReq.fluidAbove() != null) {
                BlockPos p = pos.above();
                int count = contextReq.fluidAboveCount() != null ? contextReq.fluidAboveCount() : 1;
                for (int i = 0; i < count; i++) {
                    FluidState fs = ctx.level.getFluidState(p);
                    if (!ForgeRegistries.FLUIDS.getKey(fs.getType()).equals(contextReq.fluidAbove())) {
                        System.out.println(prefix + " Fluid above fail at +" + (i + 1) + ": expected " + contextReq.fluidAbove() + ", found " + ForgeRegistries.FLUIDS.getKey(fs.getType()));
                        return false;
                    }
                    // Vérification des NBT pour chaque bloc de fluide
                    if (!contextReq.requiredNbt().isEmpty()) {
                        var wd = WaterDataHelper.get(ctx.level, p);
                        if (wd == null) {
                            System.out.println(prefix + " WaterDataHelper returned null for fluid_above at " + p);
                            return false;
                        }
                        for (var e : contextReq.requiredNbt().entrySet()) {
                            int actual = e.getKey().equals("salinity") ? wd.getSalinity() : wd.getPH();
                            if (actual != e.getValue()) {
                                System.out.println(prefix + " Fluid above NBT fail at " + p + ": " + e.getKey() + " expected " + e.getValue() + ", found " + actual);
                                return false;
                            }
                        }
                    }
                    p = p.above();
                }
                System.out.println(prefix + " Fluid above check passed for " + count + " blocks");
            }

            // 3.d) "Deep fluid" et "Deep block" : Nombres de blocks (ou block de fluides avec #NBT) au dessus du block ciblé
            if (contextReq.deepBlock() != null || contextReq.deepFluid() != null) {
                BlockPos p = pos.above();
                int cnt = 0;
                int threshold = contextReq.deepThreshold() != null ? contextReq.deepThreshold() : 15;

                while (cnt < threshold) {
                    // Blocks
                    if (contextReq.deepBlock() != null) {
                        BlockState bs = ctx.level.getBlockState(p);
                        if (bs.getBlock() != contextReq.deepBlock()) {
                            System.out.println(prefix + " Deep block fail at +" + (cnt + 1) + ": expected " + contextReq.deepBlock() + ", found " + bs.getBlock());
                            break;
                        }
                    // Fluides
                    } else if (contextReq.deepFluid() != null) {
                        FluidState fs = ctx.level.getFluidState(p);
                        if (!ForgeRegistries.FLUIDS.getKey(fs.getType()).equals(contextReq.deepFluid())) {
                            System.out.println(prefix + " Deep fluid fail at +" + (cnt + 1) + ": expected " + contextReq.deepFluid() + ", found " + ForgeRegistries.FLUIDS.getKey(fs.getType()));
                            break;
                        }
                        // Vérification des NBT pour chaque bloc de fluide
                        if (!contextReq.requiredNbt().isEmpty()) {
                            var wd = WaterDataHelper.get(ctx.level, p);
                            if (wd == null) {
                                System.out.println(prefix + " WaterDataHelper returned null for deep_fluid at " + p);
                                return false;
                            }
                            for (var e : contextReq.requiredNbt().entrySet()) {
                                int actual = e.getKey().equals("salinity") ? wd.getSalinity() : wd.getPH();
                                if (actual != e.getValue()) {
                                    System.out.println(prefix + " Deep fluid NBT fail at " + p + ": " + e.getKey() + " expected " + e.getValue() + ", found " + actual);
                                    return false;
                                }
                            }
                        }
                    }
                    cnt++;
                    p = p.above();
                }
                if (cnt < threshold) {
                    System.out.println(prefix + " Deep condition unmet: count " + cnt + " < threshold " + threshold);
                    return false;
                }
                System.out.println(prefix + " Deep condition check passed: " + cnt + " blocks/fluids");
            }

            // 3.e) Vérification des NBT pour le bloc cliqué (optionnel, en plus des fluides)
            // uniquement si on n’a PAS déjà un contexte de fluide (fluidAbove/deepFluid)
            if (!contextReq.requiredNbt().isEmpty()
                    && contextReq.fluidAbove() == null
                    && contextReq.deepFluid() == null) {

                var wd = WaterDataHelper.get(ctx.level, pos);
                if (wd == null) {
                    System.out.println(prefix + " WaterDataHelper returned null for clicked pos " + pos);
                    return false;
                }
                for (var e : contextReq.requiredNbt().entrySet()) {
                    int actual = e.getKey().equals("salinity") ? wd.getSalinity() : wd.getPH();
                    if (actual != e.getValue()) {
                        System.out.println(prefix + " NBT fail at clicked pos " + pos + ": " + e.getKey() + " expected " + e.getValue() + ", found " + actual);
                        return false;
                    }
                }
                System.out.println(prefix + " NBT check passed for clicked pos " + pos);
            }
        }

        System.out.println(prefix + " Match successful");
        return true;
    }

    /**
     * Assemble + son/particules/NBT
     */
    public ItemStack assemble(SamplingContext ctx) {
        // Tirage
        float roll = ctx.level.random.nextFloat(), cum = 0;
        ItemStack result = ItemStack.EMPTY;
        for (Output o : outputs) {
            cum += o.chance;
            if (roll < cum) {
                result = o.result.copy();
                break;
            }
        }
        if (result.isEmpty()) return ItemStack.EMPTY;

        // son + particules
        if (!ctx.level.isClientSide) {
            if (ctx.targetEntity instanceof LivingEntity) {
                ResourceLocation mobRL = ForgeRegistries.ENTITY_TYPES.getKey(ctx.targetEntity.getType());
                ResourceLocation soundRL = new ResourceLocation("entity." + mobRL.getPath() + ".ambient");
                SoundEvent ambient = ForgeRegistries.SOUND_EVENTS.getValue(soundRL);
                if (ambient == null) ambient = SoundEvents.PLAYER_ATTACK_STRONG;
                ctx.level.playSound(null, ctx.targetEntity.blockPosition(), ambient, SoundSource.PLAYERS, 1f, 1f);
            } else {
                SoundType st = ctx.blockState.getSoundType();
                ctx.level.playSound(null, ctx.pos, st.getBreakSound(), SoundSource.BLOCKS, st.getVolume(), st.getPitch());
            }
            ((ServerLevel)ctx.level).sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                ctx.pos.getX() + 0.5, ctx.pos.getY() + 1, ctx.pos.getZ() + 0.5,
                8, 0.3, 0.2, 0.3, 0
            );
        }

        // nbt transferts
        CompoundTag outTag = result.getOrCreateTag();
        var in = ctx.itemStack.getTag();
        if (in != null) {
            if (in.contains("oxic")) outTag.putBoolean("oxic", in.getBoolean("oxic"));
            if (in.contains("anoxic")) outTag.putBoolean("anoxic", in.getBoolean("anoxic"));
        }
        outTag.remove("sterile");
        return result;
    }

    @Override public boolean matches(Container c, Level l) { return false; }
    @Override public ItemStack assemble(Container c, RegistryAccess r) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess r) { return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).result; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return BioreactorRecipes.SAMPLING_SERIALIZER.get(); }
    @Override public RecipeType<SamplingRecipe> getType() { return BioreactorRecipes.SAMPLING_TYPE.get(); }
}
