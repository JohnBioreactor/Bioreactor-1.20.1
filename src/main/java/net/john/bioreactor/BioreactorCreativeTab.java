package net.john.bioreactor;

import net.john.bioreactor.content.block.BioreactorBlocks;
import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BioreactorCreativeTab {

    // ─── Centralisation des clés NBT ───────────────────────────────────────────
    private static final String TAG_STERILE = "sterile";
    private static final String TAG_OXIC = "oxic";
    private static final String TAG_ANOXIC = "anoxic";

    private static ItemStack createTaggedItemStack(ItemStack baseStack, boolean sterile, boolean oxic, boolean anoxic) {
        if (sterile) baseStack.getOrCreateTag().putBoolean(TAG_STERILE, true);
        if (oxic)    baseStack.getOrCreateTag().putBoolean(TAG_OXIC, true);
        if (anoxic)  baseStack.getOrCreateTag().putBoolean(TAG_ANOXIC, true);
        return baseStack;
    }


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, Bioreactor.MOD_ID
    );

    public static final RegistryObject<CreativeModeTab> BIOREACTOR_TAB = CREATIVE_MODE_TABS.register("bioreactor_tab",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.literal("Bioreactor"))
                    .icon(() -> BioreactorItems.BACTERIA_ESCHERICHIA_COLI.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {


                        /**
                         * BACTERIA
                         */
                        output.accept(createTaggedItemStack(new ItemStack(Items.GLASS_BOTTLE), true, true, false));
                        output.accept(createTaggedItemStack(new ItemStack(Items.GLASS_BOTTLE), true, false, true));
                        // output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.BIOREACTOR_EMPTY_BOTTLE.get()), true, true, false));
                        // output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.BIOREACTOR_EMPTY_BOTTLE.get()), true, false, true));


                        output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.SYRINGE_EMPTY.get()), true, true, false)); // Stérile + oxic
                        output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.SYRINGE_EMPTY.get()), true, false, true)); // Stérile + anoxic



                        output.accept(BioreactorItems.SAMPLE_SOIL.get());
                        output.accept(BioreactorItems.SAMPLE_SEAWATER_SEDIMENT.get());
                        output.accept(BioreactorItems.SAMPLE_SKY.get());



                        output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.SYRINGE_COW.get()), false, true, false));  // Oxic
                        output.accept(createTaggedItemStack(new ItemStack(BioreactorItems.SYRINGE_COW.get()), false, false, true));  // Anoxic

                        output.accept(BioreactorItems.DEAD_BIOMASS.get());
                        output.accept(BioreactorItems.ENRICHED_BACTERIA_MULTIPLE.get());

                        output.accept(BioreactorItems.BACTERIA_ESCHERICHIA_COLI.get());
                        output.accept(BioreactorItems.BACTERIA_PSEUDOMONAS_AERUGINOSA.get());

                        /**
                         * CHEMICALS
                         */
                        output.accept(BioreactorItems.GLUCOSE.get());
                        output.accept(BioreactorItems.ORGANIC_ACIDS.get());

                        output.accept(BioreactorItems.SULFUR.get());
                        output.accept(BioreactorItems.SULFATE.get());
                        output.accept(BioreactorItems.AMMONIUM.get());
                        output.accept(BioreactorItems.NITRATE.get());
                        output.accept(BioreactorItems.NITRITE.get());
                        output.accept(BioreactorItems.POWDER_H2SO4.get());
                        output.accept(BioreactorItems.POWDER_HCL.get());
                        output.accept(BioreactorItems.POWDER_NAOH.get());

                        /**
                         * BLOCKS
                         */
                        output.accept(BioreactorBlocks.ANAEROBIC_CHAMBER.get());
                        output.accept(BioreactorBlocks.SNOW_FREEZER.get());

                        /**
                         * GAZ Buckets ====== >>> TO DO : ERASE THIS CODE and use Gaz_Tank instead
                         */
                        output.accept(BioreactorItems.GAZ_AIR_BUCKET.get());
                        output.accept(BioreactorItems.GAZ_CH4_BUCKET.get());
                        output.accept(BioreactorItems.GAZ_CO2_BUCKET.get());
                        output.accept(BioreactorItems.GAZ_H2_BUCKET.get());
                        output.accept(BioreactorItems.GAZ_H2O_BUCKET.get());
                        output.accept(BioreactorItems.GAZ_H2S_BUCKET.get());
                        output.accept(BioreactorItems.GAZ_N2_BUCKET.get());
                    })
                    .build()
    );

    public static void registerCreativeTabs(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
