package net.john.bioreactor.foundation.data;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.*;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BioreactorRegistrate extends AbstractRegistrate<net.john.bioreactor.foundation.data.BioreactorRegistrate> {
    private static final Map<RegistryEntry<?>, RegistryObject<CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    @Nullable
    protected Function<Item, TooltipModifier> currentTooltipModifierFactory;
    @Nullable
    protected RegistryObject<CreativeModeTab> currentTab;

    protected BioreactorRegistrate(String modid) {
        super(modid);
    }

    public static net.john.bioreactor.foundation.data.BioreactorRegistrate create(String modid) {
        return new net.john.bioreactor.foundation.data.BioreactorRegistrate(modid);
    }

    public static boolean isInCreativeTab(RegistryEntry<?> entry, RegistryObject<CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public net.john.bioreactor.foundation.data.BioreactorRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
        currentTooltipModifierFactory = factory;
        return self();
    }

    @Nullable
    public Function<Item, TooltipModifier> getTooltipModifierFactory() {
        return currentTooltipModifierFactory;
    }



    @Nullable
    public net.john.bioreactor.foundation.data.BioreactorRegistrate setCreativeTab(RegistryObject<CreativeModeTab> tab) {
        currentTab = tab;
        return self();
    }
    public RegistryObject<CreativeModeTab> getCreativeTab() {
        return currentTab;
    }



    @Override
    public net.john.bioreactor.foundation.data.BioreactorRegistrate registerEventListeners(IEventBus bus) {
        return super.registerEventListeners(bus);
    }


    @Override
    protected <R, T extends R> RegistryEntry<T> accept(String name,
                                                       ResourceKey<? extends Registry<R>> type,
                                                       Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
                                                       NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {

        RegistryEntry<T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (type.equals(Registries.ITEM) && currentTooltipModifierFactory != null) {
            try {
                Item item = (Item) entry.get();
                TooltipModifier modifier = TooltipModifier.mapNull(currentTooltipModifierFactory.apply(item));
                TooltipModifier.REGISTRY.register(item, modifier);
            } catch (NullPointerException e) {
                // L'entrée n'est pas encore enregistrée : on ignore le tooltip pour le moment
            }
        }
        if (currentTab != null) {
            TAB_LOOKUP.put(entry, currentTab);
        }
        return entry;
    }





    /**
     * Méthode pour enregistrer les BlockEntity
     */
    @Override
    public <T extends BlockEntity> CreateBlockEntityBuilder<T, net.john.bioreactor.foundation.data.BioreactorRegistrate> blockEntity(String name,
                                                                                                                                               BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return blockEntity(self(), name, factory);
    }

    @Override
    public <T extends BlockEntity, P> CreateBlockEntityBuilder<T, P> blockEntity(P parent, String name,
                                                                                 BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return (CreateBlockEntityBuilder<T, P>) entry(name,
                (callback) -> CreateBlockEntityBuilder.create(this, parent, name, callback, factory));
    }

    /**
     * Méthode pour enregistrer les Entity
     */
    @Override
    public <T extends Entity> CreateEntityBuilder<T, net.john.bioreactor.foundation.data.BioreactorRegistrate> entity(String name,
                                                                                                                                EntityType.EntityFactory<T> factory, MobCategory classification) {
        return this.entity(self(), name, factory, classification);
    }

    @Override
    public <T extends Entity, P> CreateEntityBuilder<T, P> entity(P parent, String name,
                                                                  EntityType.EntityFactory<T> factory, MobCategory classification) {
        return (CreateEntityBuilder<T, P>) this.entry(name, (callback) -> {
            return CreateEntityBuilder.create(this, parent, name, callback, factory, classification);
        });
    }


    /**
     * Palettes
     */

    public <T extends Block> BlockBuilder<T, net.john.bioreactor.foundation.data.BioreactorRegistrate> paletteStoneBlock(String name,
            NonNullFunction<BlockBehaviour.Properties, T> factory, NonNullSupplier<Block> propertiesFrom, boolean worldGenStone,
    boolean hasNaturalVariants) {
        BlockBuilder<T, net.john.bioreactor.foundation.data.BioreactorRegistrate> builder = super.block(name, factory).initialProperties(propertiesFrom)
                .transform(pickaxeOnly())
                .blockstate(hasNaturalVariants ? BlockStateGen.naturalStoneTypeBlock(name) : (c, p) -> {
                    final String location = "block/palettes/stone_types/" + c.getName();
                    p.simpleBlock(c.get(), p.models()
                            .cubeAll(c.getName(), p.modLoc(location)));
                })
                .tag(BlockTags.DRIPSTONE_REPLACEABLE)
                .tag(BlockTags.AZALEA_ROOT_REPLACEABLE)
                .tag(BlockTags.MOSS_REPLACEABLE)
                .tag(BlockTags.LUSH_GROUND_REPLACEABLE)
                .item()
                .model((c, p) -> p.cubeAll(c.getName(),
                        p.modLoc(hasNaturalVariants ? "block/palettes/stone_types/natural/" + name + "_1"
                                : "block/palettes/stone_types/" + c.getName())))
                .build();
        return builder;
    }

    public BlockBuilder<Block, net.john.bioreactor.foundation.data.BioreactorRegistrate> paletteStoneBlock(String name, NonNullSupplier<Block> propertiesFrom,
    boolean worldGenStone, boolean hasNaturalVariants) {
        return paletteStoneBlock(name, Block::new, propertiesFrom, worldGenStone, hasNaturalVariants);
    }






    /**
     * For registering fluids with no buckets/blocks
     */
    public class VirtualFluidBuilder<T extends ForgeFlowingFluid, P> extends FluidBuilder<T, P> {

        public VirtualFluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                   ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory,
                                   NonNullFunction<Properties, T> sourceFactory,
                                   NonNullFunction<Properties, T> flowingFactory
        ) {
            super(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, flowingFactory);
            source(sourceFactory);
        }

        @Override
        public NonNullSupplier<T> asSupplier() {
            return this::getEntry;
        }
    }


        /**
         * standard Fluids
         **/

    public FluidBuilder<ForgeFlowingFluid.Flowing, net.john.bioreactor.foundation.data.BioreactorRegistrate> standardFluid(String name) {
        return fluid(
                name,
                new ResourceLocation(getModid(), "fluid/" + name + "_still"),
                new ResourceLocation(getModid(), "fluid/" + name + "_flow"));
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, net.john.bioreactor.foundation.data.BioreactorRegistrate> standardFluid(String name,
            FluidBuilder.FluidTypeFactory typeFactory) {
        return fluid(
                name,
                new ResourceLocation(getModid(), "fluid/" + name + "_still"),
                new ResourceLocation(getModid(), "fluid/" + name + "_flow"),
                typeFactory);
    }


    public static FluidType defaultFluidType(FluidType.Properties properties, ResourceLocation stillTexture,
            ResourceLocation flowingTexture) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return stillTexture;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowingTexture;
                    }
                });
            }
        };
    }

    /**
     * Util
     */

    public static <T extends Block> NonNullConsumer<? super T> casingConnectivity(
            BiConsumer<T, CasingConnectivity> consumer) {
        return entry -> onClient(() -> () -> registerCasingConnectivity(entry, consumer));
    }

    public static <T extends Block> NonNullConsumer<? super T> blockModel(
            Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
        return entry -> onClient(() -> () -> registerBlockModel(entry, func));
    }

    public static <T extends Item> NonNullConsumer<? super T> itemModel(
            Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
        return entry -> onClient(() -> () -> registerItemModel(entry, func));
    }

    public static <T extends Block> NonNullConsumer<? super T> connectedTextures(
            Supplier<ConnectedTextureBehaviour> behavior) {
        return entry -> onClient(() -> () -> registerCTBehviour(entry, behavior));
    }

    protected static void onClient(Supplier<Runnable> toRun) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, toRun);
    }

    @OnlyIn(Dist.CLIENT)
    private static <T extends Block> void registerCasingConnectivity(T entry,
            BiConsumer<T, CasingConnectivity> consumer) {
        consumer.accept(entry, CreateClient.CASING_CONNECTIVITY);
    }

    @OnlyIn(Dist.CLIENT)
    private static void registerBlockModel(Block entry,
            Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(entry);
        CreateClient.MODEL_SWAPPER.getCustomBlockModels()
                .register(id, func.get());
    }

    @OnlyIn(Dist.CLIENT)
    private static void registerItemModel(Item entry,
            Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> func) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(entry);
        CreateClient.MODEL_SWAPPER.getCustomItemModels()
                .register(id, func.get());
    }

    @OnlyIn(Dist.CLIENT)
    private static void registerCTBehviour(Block entry, Supplier<ConnectedTextureBehaviour> behaviorSupplier) {
        ConnectedTextureBehaviour behavior = behaviorSupplier.get();
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(entry);
        CreateClient.MODEL_SWAPPER.getCustomBlockModels()
                .register(id, model -> new CTModel(model, behavior));
    }


}