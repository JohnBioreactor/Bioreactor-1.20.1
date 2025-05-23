package net.john.bioreactor.content.kinetics.Sampling;

import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.john.bioreactor.foundation.advancement.BioreactorAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;



@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "bioreactor")
public class SamplingEventHandler {


    private static final ResourceLocation ADV_AIR_ALTITUDE =new ResourceLocation(Bioreactor.MOD_ID, "sampling/k_air_altitude");


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock ev) {
        if (ev.getLevel().isClientSide()) return;
        Player player = ev.getEntity();
        InteractionHand hand = ev.getHand();
        ItemStack held = player.getItemInHand(hand);

        // Ici, uniquement la bouteille
        if (held.isEmpty() || held.getItem() != Items.GLASS_BOTTLE) return;

        BlockPos pos = ev.getPos();
        BlockState state = ev.getLevel().getBlockState(pos);
        var ctx = new SamplingRecipe.SamplingContext(ev.getLevel(), pos, state, held, null);

        tryApply(ev, hand, player, held, ctx);
    }

    // Cas ou le joueur click sur de l'air
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (ev.getLevel().isClientSide()) return;
        Player player = ev.getEntity();
        InteractionHand hand = ev.getHand();
        ItemStack held = player.getItemInHand(hand);

        // 1) Seulement la bouteille
        if (held.isEmpty() || held.getItem() != Items.GLASS_BOTTLE) return;

        // 2) Ray-trace “à la main” pour savoir si on a touché un bloc
        Vec3 eye  = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        Vec3 end  = eye.add(look.scale(5.0D));  // portée 5 blocs

        BlockHitResult hit = ev.getLevel().clip(new ClipContext(
                eye, end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        // 3) Si on a touché un bloc, on laisse tomber : on veut **seulement** le cas “air”
        if (hit.getType() != HitResult.Type.MISS) return;

        // 4) Sinon, on construit exactement le même SamplingContext que pour un bloc
        BlockPos pos   = player.blockPosition();
        BlockState state = ev.getLevel().getBlockState(pos);
        var ctx = new SamplingRecipe.SamplingContext(ev.getLevel(), pos, state, held, null);

        // 5) On délègue à votre tryApply existante
        tryApply(ev, hand, player, held, ctx);
    }


    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract ev) {
        if (ev.getLevel().isClientSide()) return;
        Player player = ev.getEntity();
        InteractionHand hand = ev.getHand();
        ItemStack held = player.getItemInHand(hand);

        // Ici, uniquement la seringe
        if (held.isEmpty() || held.getItem() != BioreactorItems.SYRINGE_EMPTY.get()) return;

        BlockPos pos = ev.getTarget().blockPosition();
        BlockState state = ev.getLevel().getBlockState(pos);
        var ctx = new SamplingRecipe.SamplingContext(ev.getLevel(), pos, state, held, ev.getTarget());

        tryApply(ev, hand, player, held, ctx);
    }


    private static void tryApply(PlayerInteractEvent ev,
                                 InteractionHand hand,
                                 Player player,
                                 ItemStack held,
                                 SamplingRecipe.SamplingContext ctx) {
        List<SamplingRecipe> recipes = ctx.level.getRecipeManager()
                .getAllRecipesFor(BioreactorRecipes.SAMPLING_TYPE.get());

        // Trier les recettes par spécificité
        recipes = recipes.stream()
                .sorted(Comparator.comparingInt(recipe -> {
                    var ctxReq = recipe.getContextReq();
                    if (ctxReq == null) return 0;
                    int score = 0;
                    if (ctxReq.deepFluid() != null || ctxReq.deepBlock() != null) score += 100; // Priorité élevée pour deep conditions
                    if (ctxReq.fluidAbove() != null)                              score += 50; // Priorité pour fluid_above
                    if (ctxReq.aboveBlock() != null)                              score += 25; // Priorité pour above_block
                    if (!ctxReq.requiredNbt().isEmpty())                          score += 10; // Priorité pour NBT
                    return                                                       -score;       // Tri décroissant
                }))
                .collect(Collectors.toList());

        System.out.println("[Sampling] recipes=" + recipes.size()
                + " clickedBlock=" + ctx.blockState.getBlock()
                + " y=" + ctx.pos.getY()
                + " targetEntity=" + (ctx.targetEntity != null ? ctx.targetEntity.getType() : "none"));

        recipes.forEach(r -> System.out.println("[Sampling] recipe id = " + r.getId()));


        for (var recipe : recipes) {
            if (!recipe.matches(ctx)) continue;
            ItemStack out = recipe.assemble(ctx);
            if (out.isEmpty()) continue;

            // on enlève la bouteille / seringue…
            if (!player.isCreative()) {
                held.shrink(1);
                if (held.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
            }
            // on donne le résultat
            if (!player.addItem(out)) player.drop(out, false);


            // Advancement
            if (player instanceof ServerPlayer sp) {
                BioreactorAdvancements.triggerSamplingFirstTime(sp);
            }

            if (recipe.getId().equals(ADV_AIR_ALTITUDE) && player instanceof ServerPlayer sp) {
                System.out.println("[Advancement] recipe matched adv_air_altitude → triggerAdvancement");
                BioreactorAdvancements.triggerSamplingHighAltitude(sp);
            }

            ev.setCanceled(true);
            ev.setCancellationResult(InteractionResult.SUCCESS);
            System.out.println("[Sampling] apply recipe " + recipe.getId() + " → " + out);
            return;
        }
    }
}