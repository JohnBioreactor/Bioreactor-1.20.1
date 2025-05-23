package net.john.bioreactor;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.john.bioreactor.content.block.BioreactorBlocks;
import net.john.bioreactor.content.entity.BioreactorBlockEntity;
import net.john.bioreactor.content.fluid.BioreactorFluidTypes;
import net.john.bioreactor.content.fluid.BioreactorFluids;
import net.john.bioreactor.content.item.BioreactorItems;
import net.john.bioreactor.content.kinetics.BioreactorRecipes;
import net.john.bioreactor.foundation.data.BioreactorRegistrate;
import net.john.bioreactor.foundation.waterphsalinity.minecraft.ChunkCapabilityAttacher;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@Mod(Bioreactor.MOD_ID)
public class Bioreactor {

    public static final String MOD_ID = "bioreactor";
    public static BioreactorRegistrate MY_REGISTRATE;



    // ─── Clés NBT centralisées ───────────────────────────────────────────────────
    public static final String TAG_STERILE = "sterile";
    public static final String TAG_OXIC    = "oxic";
    public static final String TAG_ANOXIC  = "anoxic";



    // ─── Déclarations de Deferred Registers ──────────────────────────────────────
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);


    public Bioreactor() {
        onCtor();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ChunkCapabilityAttacher.class);
    }


    public static void onCtor() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        MY_REGISTRATE = BioreactorRegistrate.create(MOD_ID);
        MY_REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, net.createmod.catnip.lang.FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
        MY_REGISTRATE.registerEventListeners(modEventBus);

        // Enregistrement des Deferred Registers
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BioreactorCreativeTab.registerCreativeTabs(modEventBus);

        // Enregistrement des éléments de votre mod
        BioreactorBlocks.register();
        BioreactorFluids.register(modEventBus);
        BioreactorFluidTypes.register(modEventBus);
        BioreactorRecipes.register(modEventBus);
        BioreactorSoundEvents.registerSounds();
        BioreactorBlockEntity.register();
        BioreactorItems.register();
        BioreactorPartials.init();

        // Enregistrement des renderers uniquement sur le client
        //DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BioreactorRenderer.register(modEventBus));


        // Enregistrer l'événement FMLCommonSetupEvent
        modEventBus.addListener(Bioreactor::commonSetup);

    }

//    private void clientSetup(final FMLClientSetupEvent event) {
//        event.enqueueWork(SmallBioreactorRenderer::init); // Initialisation différée dans le thread principal
//    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BioreactorNetwork.register();
            // now the channel + WaterChunkSyncPacket is ready
        });
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BioreactorBlockEntity.register();
            //BioreactorTags.registerAllItemProperties();



            //Allows for the fluid to be translucid
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.ANAEROBIC_CHAMBER.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_AIR_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_AIR_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_AIR_FLUIDBLOCK.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_CH4_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_CH4_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_CH4_FLUIDBLOCK.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_CO2_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_CO2_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_CO2_FLUIDBLOCK.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_H2_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_H2_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_H2_FLUIDBLOCK.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_H2O_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_H2O_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_H2O_FLUIDBLOCK.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_H2S_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_H2S_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_H2S_FLUIDBLOCK.get(), RenderType.translucent());

            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_N2_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorFluids.GAZ_N2_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BioreactorBlocks.GAZ_N2_FLUIDBLOCK.get(), RenderType.translucent());
        });
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
