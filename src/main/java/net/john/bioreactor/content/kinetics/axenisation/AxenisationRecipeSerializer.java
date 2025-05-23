package net.john.bioreactor.content.kinetics.axenisation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AxenisationRecipeSerializer implements RecipeSerializer<AxenisationRecipe> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public AxenisationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonArray inputsArray = GsonHelper.getAsJsonArray(json, "inputs");
        List<ItemStack> inputItems = new ArrayList<>();
        LOGGER.debug("Parsing inputs for recipe " + recipeId);
        for (JsonElement element : inputsArray) {
            String itemId = element.getAsString();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null) {
                LOGGER.error("Item not found in registry: " + itemId + ", defaulting to air");
                item = Items.AIR;
            }
            ItemStack stack = new ItemStack(item);
            inputItems.add(stack);
            LOGGER.debug("Added input item: " + stack.getItem().getDescriptionId());
        }

        List<BacteriaData> bacteria = parseBacteria(json.getAsJsonObject("bacteria"));

        // Pas besoin de passer les métabolismes ici, ils sont chargés dans le constructeur de AxenisationRecipe
        return new AxenisationRecipe(recipeId, inputItems, bacteria);
    }

    @Override
    public @Nullable AxenisationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int inputCount = buffer.readVarInt(); // Nombre d'items d'entrée
        List<ItemStack> inputItems = new ArrayList<>();
        for (int i = 0; i < inputCount; i++) {
            inputItems.add(buffer.readItem()); // Lecture des ItemStacks
        }

        int bacteriaCount = buffer.readVarInt(); // Nombre de bactéries
        List<BacteriaData> bacteria = new ArrayList<>();
        for (int i = 0; i < bacteriaCount; i++) {
            String bacteriaId = buffer.readUtf();
            ItemStack axenicOutput = buffer.readItem();
            Map<String, ConditionRange> conditions = new HashMap<>();

            int conditionCount = buffer.readVarInt(); // Nombre de conditions
            for (int j = 0; j < conditionCount; j++) {
                String conditionKey = buffer.readUtf();
                List<String> greenValues = readStringList(buffer);
                List<String> yellowValues = readStringList(buffer);
                conditions.put(conditionKey, new ConditionRange(greenValues, yellowValues));
            }

            // Lire l'affinité oxygen (la chaîne stockée, puis conversion en enum)
            String affinityStr = buffer.readUtf();
            o2_affinity affinity;
            try {
                affinity = o2_affinity.valueOf(affinityStr);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid o2_affinity value: " + affinityStr);
            }
            bacteria.add(new BacteriaData(bacteriaId, axenicOutput, conditions, affinity));
        }

        // Pas besoin de passer les métabolismes ici, ils sont chargés dans le constructeur de AxenisationRecipe

        return new AxenisationRecipe(recipeId, inputItems, bacteria);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AxenisationRecipe recipe) {
        buffer.writeVarInt(recipe.getInputItems().size());
        for (ItemStack input : recipe.getInputItems()) {
            buffer.writeItem(input);
        }

        List<BacteriaData> bacteria = recipe.getTargetBacteria();
        buffer.writeVarInt(bacteria.size());
        for (BacteriaData bacteriaData : bacteria) {
            buffer.writeUtf(bacteriaData.getBacteriaId());
            buffer.writeItem(bacteriaData.getAxenicOutput());

            Map<String, ConditionRange> conditions = bacteriaData.getConditions();
            buffer.writeVarInt(conditions.size());
            for (Map.Entry<String, ConditionRange> entry : conditions.entrySet()) {
                buffer.writeUtf(entry.getKey());
                writeStringList(buffer, entry.getValue().getGreenValues());
                writeStringList(buffer, entry.getValue().getYellowValues());
            }

            // Écriture de l'o2_affinity pour la bactérie
            buffer.writeUtf(bacteriaData.getOxygenAffinity().name());
        }
    }
    private List<BacteriaData> parseBacteria(JsonObject bacteriaJson) {
        List<BacteriaData> bacteriaList = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : bacteriaJson.entrySet()) {
            String bacteriaId = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();

            String itemId = GsonHelper.getAsString(data, "item");
            ItemStack axenicOutput = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)));


            // Récupérer l'affinité directement au niveau racine (pas dans conditions)
            String affinityStr = GsonHelper.getAsString(data, "o2_affinity");
            o2_affinity affinity;
            try {
                affinity = o2_affinity.valueOf(affinityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid o2_affinity value: " + affinityStr);
            }


            /** CONDITIONS = Mes variables vert, jaune, rouge
             */
            JsonObject conditionsJson = GsonHelper.getAsJsonObject(data, "conditions");
            Map<String, ConditionRange> conditions = new HashMap<>();

            JsonObject salinityJson = conditionsJson.getAsJsonObject("salinity");
            List<String> salinityGreen = parseStringList(salinityJson.getAsJsonArray("green"));
            List<String> salinityYellow = parseStringList(salinityJson.getAsJsonArray("yellow"));
            conditions.put("salinity", new ConditionRange(salinityGreen, salinityYellow));

            JsonObject pHJson = conditionsJson.getAsJsonObject("pH");
            List<String> pHGreen = parseStringList(pHJson.getAsJsonArray("green"));
            List<String> pHYellow = parseStringList(pHJson.getAsJsonArray("yellow"));
            conditions.put("pH", new ConditionRange(pHGreen, pHYellow));

            JsonObject tempJson = conditionsJson.getAsJsonObject("temperature");
            List<String> tempGreen = parseStringList(tempJson.getAsJsonArray("green"));
            List<String> tempYellow = parseStringList(tempJson.getAsJsonArray("yellow"));
            conditions.put("temperature", new ConditionRange(tempGreen, tempYellow));

            // Ici on ne lit plus "o2_presence" mais on ne s'en sert pas puisque l'affinité est définie au niveau racine

            JsonObject metabolismJson = conditionsJson.getAsJsonObject("metabolism");
            List<String> metabolismGreen = parseStringList(metabolismJson.getAsJsonArray("green"));
            List<String> metabolismYellow = parseStringList(metabolismJson.getAsJsonArray("yellow"));
            conditions.put("metabolism", new ConditionRange(metabolismGreen, metabolismYellow));

            bacteriaList.add(new BacteriaData(bacteriaId, axenicOutput, conditions, affinity));
        }
        return bacteriaList;
    }

    private List<String> parseStringList(JsonArray array) {
        List<String> result = new ArrayList<>();
        if (array != null) {
            for (JsonElement element : array) {
                result.add(element.getAsString());
            }
        }
        return result;
    }

    private void writeStringList(FriendlyByteBuf buffer, List<String> list) {
        buffer.writeVarInt(list.size());
        for (String value : list) {
            buffer.writeUtf(value);
        }
    }

    private List<String> readStringList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buffer.readUtf());
        }
        return list;
    }
}