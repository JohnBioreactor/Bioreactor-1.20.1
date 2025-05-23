package net.john.bioreactor.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIProcessingFanPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation("bioreactor", "jei_processing_fan");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                Items.GLASS_BOTTLE,
                // Ici la lambda prend 2 params : (stack, context)
                (stack, context) -> {
                    CompoundTag tag = stack.getOrCreateTag();
                    if (tag.isEmpty()) {
                        return ""; // pas de NBT → même sous-type par défaut
                    }
                    StringBuilder key = new StringBuilder();
                    if (tag.getBoolean("sterile")) key.append("sterile;");
                    if (tag.getBoolean("oxic"))    key.append("oxic;");
                    if (tag.getBoolean("anoxic"))  key.append("anoxic;");
                    return key.toString();
                }
        );
    }




}
