package net.john.bioreactor.content.mixin.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.john.bioreactor.content.kinetics.ICustomNBTRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Injecte la lecture de input_nbt / result_nbt
 * et applique les tags directement sur l’ingrédient
 * pour que JEI affiche la bonne texture / tooltip.
 */
@Mixin(
        value = SimpleCookingSerializer.class,
        remap = false)

public class SimpleCookingSerializerMixin {

    @SuppressWarnings("unchecked")
    @Inject(
            method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;",
            at = @At("RETURN"))

    private void readNBTSettings(ResourceLocation id, JsonObject json,
                                 CallbackInfoReturnable<AbstractCookingRecipe> cir)
            throws CommandSyntaxException {
        AbstractCookingRecipe recipe = cir.getReturnValue();
        if (!(recipe instanceof ICustomNBTRecipe custom)) return;

        // --- INPUT NBT ---
        if (json.has("input_nbt") && json.get("input_nbt").isJsonObject()) {
            JsonObject obj = json.getAsJsonObject("input_nbt");
            CompoundTag inTag = new CompoundTag();
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                String key = e.getKey();
                JsonElement je = e.getValue();
                if (je.isJsonPrimitive()) {
                    JsonPrimitive p = je.getAsJsonPrimitive();
                    if (p.isBoolean())    inTag.putBoolean(key, p.getAsBoolean());
                    else if (p.isNumber()) inTag.putDouble(key, p.getAsDouble());
                    else if (p.isString()) inTag.putString(key, p.getAsString());
                } else {
                    inTag.put(key, TagParser.parseTag(je.toString()));
                }
            }
            custom.bioreactor_setInputNBT(inTag);
        }

        // --- RESULT NBT ---
        if (json.has("result_nbt") && json.get("result_nbt").isJsonObject()) {
            JsonObject obj = json.getAsJsonObject("result_nbt");
            CompoundTag outTag = new CompoundTag();
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                String key = e.getKey();
                JsonElement je = e.getValue();
                if (je.isJsonPrimitive()) {
                    JsonPrimitive p = je.getAsJsonPrimitive();
                    if (p.isBoolean())    outTag.putBoolean(key, p.getAsBoolean());
                    else if (p.isNumber()) outTag.putDouble(key, p.getAsDouble());
                    else if (p.isString()) outTag.putString(key, p.getAsString());
                } else {
                    outTag.put(key, TagParser.parseTag(je.toString()));
                }
            }
            custom.bioreactor_setResultNBT(outTag);
        }

        // --- 3) appliquer le NBT sur l’ingrédient pour JEI ---
        CompoundTag inputTag = custom.bioreactor_getInputNBT();
        if (inputTag != null && !recipe.getIngredients().isEmpty()) {
            Ingredient baseIng = recipe.getIngredients().get(0);
            ItemStack[] items = baseIng.getItems();
            if (items.length > 0) {
                ItemStack taggedInput = items[0].copy();
                taggedInput.setTag(inputTag.copy());
                recipe.getIngredients().set(0, Ingredient.of(taggedInput));
            }
        }

        // (Le résultat sera taggé via l’injection sur getResultItem ci-dessous.)

    }
}
