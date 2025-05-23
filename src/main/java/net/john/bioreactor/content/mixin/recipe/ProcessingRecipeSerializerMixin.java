package net.john.bioreactor.content.mixin.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.john.bioreactor.content.kinetics.ICustomNBTRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(
        value = ProcessingRecipeSerializer.class,
        remap = false)

public class ProcessingRecipeSerializerMixin {

    @Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;",
            at = @At("RETURN"))

    private void readNBTSettings(ResourceLocation id, JsonObject json, CallbackInfoReturnable<ProcessingRecipe<?>> cir) throws CommandSyntaxException {
        ProcessingRecipe<?> recipe = cir.getReturnValue();
        ICustomNBTRecipe custom = (ICustomNBTRecipe) recipe;

        // --- INPUT NBT ---
        if (json.has("input_nbt") && json.get("input_nbt").isJsonObject()) {
            JsonObject obj = json.getAsJsonObject("input_nbt");
            CompoundTag inTag = new CompoundTag();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement e = entry.getValue();
                if (e.isJsonPrimitive()) {
                    JsonPrimitive p = e.getAsJsonPrimitive();
                    if (p.isBoolean()) {
                        inTag.putBoolean(key, p.getAsBoolean());
                    } else if (p.isNumber()) {
                        inTag.putDouble(key, p.getAsDouble());
                    } else if (p.isString()) {
                        inTag.putString(key, p.getAsString());
                    }
                } else if (e.isJsonObject() || e.isJsonArray()) {
                    inTag.put(key, TagParser.parseTag(e.toString()));
                }
            }
            custom.bioreactor_setInputNBT(inTag);
        }

        // --- RESULT NBT ---
        if (json.has("result_nbt") && json.get("result_nbt").isJsonObject()) {
            JsonObject obj = json.getAsJsonObject("result_nbt");
            CompoundTag outTag = new CompoundTag();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement e = entry.getValue();
                if (e.isJsonPrimitive()) {
                    JsonPrimitive p = e.getAsJsonPrimitive();
                    if (p.isBoolean()) {
                        outTag.putBoolean(key, p.getAsBoolean());
                    } else if (p.isNumber()) {
                        outTag.putDouble(key, p.getAsDouble());
                    } else if (p.isString()) {
                        outTag.putString(key, p.getAsString());
                    }
                } else if (e.isJsonObject() || e.isJsonArray()) {
                    outTag.put(key, TagParser.parseTag(e.toString()));
                }
            }
            custom.bioreactor_setResultNBT(outTag);
        }

        // Appliquer les tags NBT sur l’ingredient d’entrée et le(s) résultat(s) :
        CompoundTag inputTag = custom.bioreactor_getInputNBT();
        if (inputTag != null && !recipe.getIngredients().isEmpty()) {
            Ingredient baseIng = recipe.getIngredients().get(0);
            // On suppose une seule entrée ; on copie le 1er ItemStack et on lui applique le NBT requis
            ItemStack baseStack = baseIng.getItems().length > 0 ? baseIng.getItems()[0].copy()
                    : ItemStack.EMPTY;
            if (!baseStack.isEmpty()) {
                baseStack.setTag(inputTag.copy());
                // Remplacer l’ingrédient par un Ingredient équivalent contenant l’ItemStack taggé
                recipe.getIngredients().set(0, Ingredient.of(baseStack));
            }
        }

        CompoundTag resultTag = custom.bioreactor_getResultNBT();
        if (resultTag != null && !recipe.getRollableResults().isEmpty()) {
            // Pour chaque résultat défini, appliquer exactement le tag requis
            for (ProcessingOutput output : recipe.getRollableResults()) {
                output.getStack().setTag(resultTag.copy());
            }
        }

    }
}
