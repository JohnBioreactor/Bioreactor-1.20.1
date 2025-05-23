package net.john.bioreactor.content.item;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.john.bioreactor.Bioreactor;
import net.john.bioreactor.BioreactorCreativeTab;
import net.john.bioreactor.content.fluid.BioreactorFluids;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import static net.john.bioreactor.Bioreactor.MY_REGISTRATE;

public class BioreactorItems {
    static {MY_REGISTRATE.setCreativeTab(BioreactorCreativeTab.BIOREACTOR_TAB);}

    //              Add to the Creative menu -> BioreactorCreativeTab.java
    //              Create a JSON model file
    //              Add PNG file
    //              Add to lang file

    public static final ItemEntry<Item> CARBON_CYCLE_ICON = MY_REGISTRATE.item("carbon_cycle_icon", Item::new).register();
    public static final ItemEntry<Item> NITROGEN_CYCLE_ICON = MY_REGISTRATE.item("nitrogen_cycle_icon", Item::new).register();
    public static final ItemEntry<Item> SULFUR_CYCLE_ICON = MY_REGISTRATE.item("sulfur_cycle_icon", Item::new).register();
    public static final ItemEntry<Item> METAL_CYCLE_ICON = MY_REGISTRATE.item("metal_cycle_icon", Item::new).register();


    /**
     * BACTERIA
     */
    public static final ItemEntry<Item> SAMPLE_SOIL = MY_REGISTRATE.item("sample_soil", Item::new).register();

    public static final ItemEntry<Item> SAMPLE_SEAWATER_SEDIMENT = MY_REGISTRATE.item("sample_seawater_sediment", Item::new).register();

    public static final ItemEntry<Item> SAMPLE_SKY = MY_REGISTRATE.item("sample_sky", Item::new).register();

    public static final ItemEntry<Item> SYRINGE_COW = MY_REGISTRATE.item("syringe_cow", Item::new).register();

    public static final ItemEntry<Item> ENRICHED_BACTERIA_MULTIPLE = MY_REGISTRATE.item("enriched_bacteria_multiple", Item::new).register();
    public static final ItemEntry<Item> DEAD_BIOMASS = MY_REGISTRATE.item("dead_biomass", Item::new).register();

    public static final ItemEntry<Item> BACTERIA_ESCHERICHIA_COLI = MY_REGISTRATE.item("bacteria_escherichia_coli", Item::new).register();
    public static final ItemEntry<Item> BACTERIA_PSEUDOMONAS_AERUGINOSA = MY_REGISTRATE.item("bacteria_pseudomonas_aeruginosa", Item::new).register();


    /**
     * CHEMICALS
     */
    public static final ItemEntry<Item> GLUCOSE = MY_REGISTRATE.item("glucose", Item::new).register();

    public static final ItemEntry<Item> ORGANIC_ACIDS = MY_REGISTRATE.item("organic_acids", Item::new).register();
    public static final ItemEntry<Item> SULFUR = MY_REGISTRATE.item("powder_sulfur", Item::new).register();

    public static final ItemEntry<Item> SULFATE = MY_REGISTRATE.item("powder_sulfate", Item::new).register();
    public static final ItemEntry<Item> AMMONIUM = MY_REGISTRATE.item("powder_ammonium", Item::new).register();
    public static final ItemEntry<Item> NITRATE = MY_REGISTRATE.item("powder_nitrate", Item::new).register();
    public static final ItemEntry<Item> NITRITE = MY_REGISTRATE.item("powder_nitrite", Item::new).register();


    // Acid or alkalin powder
    public static final ItemEntry<Item> POWDER_HCL = MY_REGISTRATE.item("powder_hcl", Item::new).register();
    public static final ItemEntry<Item> POWDER_NAOH = MY_REGISTRATE.item("powder_naoh", Item::new).register();
    public static final ItemEntry<Item> POWDER_H2SO4 = MY_REGISTRATE.item("powder_h2so4", Item::new).register();


    /**
     * ITEMS
     */
    public static final ItemEntry<Item> SYRINGE_EMPTY = MY_REGISTRATE.item("syringe_empty", Item::new).register();


//    public static final ItemEntry<BioreactorEmptyBottleItem> BIOREACTOR_EMPTY_BOTTLE = MY_REGISTRATE.
//            item("bioreactor_empty_bottle", BioreactorEmptyBottleItem::new).register();
    public static final ItemEntry<Item> BIOREACTOR_EMPTY_BOTTLE = MY_REGISTRATE.item("bioreactor_empty_bottle", Item::new).register();



    //region ------------ Custom Texture selon le NBT data tag ------------
    public static void registerItemProperties() {

        //4 variants :  Oxic + Sterile   OU   Anoxic + Sterile   OU   Oxic    OU   Anoxic
        registerTexture_OxicAnoxicSterile(Items.GLASS_BOTTLE, "glass_bottle_texture");
        registerTexture_OxicAnoxicSterile(SYRINGE_EMPTY.get(), "syringe_empty_texture");

        //2 variants : Oxic ou Anoxic
        registerTexture_OxicAnoxic(SYRINGE_COW.get(), "syringe_cow_texture");

        //2 variants : pH "normal" et "pH extreme"
        registerTexture_pHextreme(Items.WATER_BUCKET, "ph_extreme_texture");

    }

    private static void registerTexture_OxicAnoxic(Item item, String predicateName) {
        ItemProperties.register(item,new ResourceLocation(Bioreactor.MOD_ID, predicateName),(stack, world, entity, seed) -> {
            if (!stack.hasTag())
                return 0.0f;

            CompoundTag tag = stack.getTag();
            boolean anoxic = tag.getBoolean("anoxic");

            if (anoxic) return 1.0f;
            return 0.0f; // Oxic ou par défaut
        });
    }
    private static void registerTexture_OxicAnoxicSterile(Item item, String predicateName) {
        ItemProperties.register(item,new ResourceLocation(Bioreactor.MOD_ID, predicateName),(stack, world, entity, seed) -> {
                    if (!stack.hasTag())
                        return 0.0f;

                    CompoundTag tag = stack.getTag();
                    boolean sterile = tag.getBoolean("sterile");
                    boolean oxic = tag.getBoolean("oxic");
                    boolean anoxic = tag.getBoolean("anoxic");

                    if (sterile && oxic) return 2.0f;
                    if (sterile && anoxic) return 3.0f;
                    if (anoxic) return 1.0f;
                    return 0.0f; // Oxic ou par défaut
                });
    }

    private static void registerTexture_pHextreme(Item item, String predicateName) {
        ItemProperties.register(item, new ResourceLocation(Bioreactor.MOD_ID, predicateName),(stack, world, entity, seed) -> {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("pH")) {
                        int pH = tag.getInt("pH");
                        if (pH <= 4 || pH >= 10) {
                            return 1.0f; // Use the alternate texture
                        }
                    }
                    return 0.0f; // Normal texture
                });
    }


    //endregion

    /**
     * GAZ BUCKET, TO DO : ERASE THIS CODE and use Gaz_Tank instead
     */

    public static final ItemEntry<BucketItem> GAZ_AIR_BUCKET = MY_REGISTRATE
            .item("gaz_air_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_AIR_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();

    public static final ItemEntry<BucketItem> GAZ_CH4_BUCKET = MY_REGISTRATE
            .item("gaz_ch4_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_CH4_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();

    public static final ItemEntry<BucketItem> GAZ_CO2_BUCKET = MY_REGISTRATE
            .item("gaz_co2_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_CO2_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();

    public static final ItemEntry<BucketItem> GAZ_H2_BUCKET = MY_REGISTRATE
            .item("gaz_h2_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_H2_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();

    public static final ItemEntry<BucketItem> GAZ_H2O_BUCKET = MY_REGISTRATE
            .item("gaz_h2o_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_H2O_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();

    public static final ItemEntry<BucketItem> GAZ_H2S_BUCKET = MY_REGISTRATE
            .item("gaz_h2s_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_H2S_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();

    public static final ItemEntry<BucketItem> GAZ_N2_BUCKET = MY_REGISTRATE
            .item("gaz_n2_bucket", properties -> new
                    BucketItem(() -> BioreactorFluids.GAZ_N2_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)))
            .register();


    public static void register() {}

}