package me.ddggdd135.slimeae.api.autocraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.*;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.slimeae.api.items.ItemStorage;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public final class CraftTaskSerializer {

    private CraftTaskSerializer() {}

    public static byte[] serializeRecipe(CraftingRecipe recipe) {
        JsonObject obj = new JsonObject();
        obj.addProperty("craftType", recipe.getCraftType().name());
        obj.add("input", serializeItemArray(recipe.getInput()));
        obj.add("output", serializeItemArray(recipe.getOutput()));
        return obj.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static CraftingRecipe deserializeRecipe(byte[] data) {
        JsonObject obj =
                JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
        String typeName = obj.get("craftType").getAsString();
        CraftType craftType = CraftType.fromName(typeName);
        if (craftType == null) return null;
        ItemStack[] input = deserializeItemArray(obj.getAsJsonArray("input"));
        ItemStack[] output = deserializeItemArray(obj.getAsJsonArray("output"));
        if (input == null || output == null) return null;
        return new CraftingRecipe(craftType, input, output);
    }

    public static byte[] serializeSteps(List<CraftStep> steps) {
        JsonArray arr = new JsonArray();
        for (int i = 0; i < steps.size(); i++) {
            CraftStep step = steps.get(i);
            JsonObject obj = new JsonObject();
            obj.addProperty("index", i);
            obj.add(
                    "recipe",
                    JsonParser.parseString(new String(serializeRecipe(step.getRecipe()), StandardCharsets.UTF_8)));
            obj.addProperty("amount", step.getAmount());
            JsonArray devices = new JsonArray();
            for (Location loc : step.getRunningDevices()) {
                devices.add(serializeLocation(loc));
            }
            obj.add("runningDevices", devices);
            obj.addProperty("virtualRunning", step.getVirtualRunning());
            obj.addProperty("virtualProcess", step.getVirtualProcess());
            arr.add(obj);
        }
        return arr.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static List<CraftStep> deserializeSteps(byte[] data) {
        JsonArray arr =
                JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonArray();
        List<CraftStep> steps = new ArrayList<>();
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            byte[] recipeBytes = obj.get("recipe").toString().getBytes(StandardCharsets.UTF_8);
            CraftingRecipe recipe = deserializeRecipe(recipeBytes);
            if (recipe == null) return null;
            long amount = obj.get("amount").getAsLong();
            CraftStep step = new CraftStep(recipe, amount);
            if (obj.has("runningDevices")) {
                for (JsonElement devElem : obj.getAsJsonArray("runningDevices")) {
                    Location loc = deserializeLocation(devElem.getAsJsonObject());
                    if (loc != null) step.addRunningDevice(loc);
                }
            }
            step.setVirtualRunning(
                    obj.has("virtualRunning") ? obj.get("virtualRunning").getAsInt() : 0);
            step.setVirtualProcess(
                    obj.has("virtualProcess") ? obj.get("virtualProcess").getAsInt() : 0);
            steps.add(step);
        }
        return steps;
    }

    public static byte[] serializeDeps(Map<CraftStep, Set<CraftStep>> deps, List<CraftStep> steps) {
        Map<CraftStep, Integer> stepIndex = new IdentityHashMap<>();
        for (int i = 0; i < steps.size(); i++) {
            stepIndex.put(steps.get(i), i);
        }
        JsonObject obj = new JsonObject();
        for (Map.Entry<CraftStep, Set<CraftStep>> entry : deps.entrySet()) {
            Integer idx = stepIndex.get(entry.getKey());
            if (idx == null) continue;
            JsonArray depArr = new JsonArray();
            for (CraftStep dep : entry.getValue()) {
                Integer depIdx = stepIndex.get(dep);
                if (depIdx != null) depArr.add(depIdx);
            }
            obj.add(String.valueOf(idx), depArr);
        }
        return obj.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Map<CraftStep, Set<CraftStep>> deserializeDeps(byte[] data, List<CraftStep> steps) {
        Map<CraftStep, Set<CraftStep>> deps = new HashMap<>();
        JsonObject obj =
                JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            int idx = Integer.parseInt(entry.getKey());
            if (idx < 0 || idx >= steps.size()) continue;
            CraftStep step = steps.get(idx);
            Set<CraftStep> depSet = new HashSet<>();
            for (JsonElement e : entry.getValue().getAsJsonArray()) {
                int depIdx = e.getAsInt();
                if (depIdx >= 0 && depIdx < steps.size()) {
                    depSet.add(steps.get(depIdx));
                }
            }
            if (!depSet.isEmpty()) deps.put(step, depSet);
        }
        return deps;
    }

    public static byte[] serializeStorage(ItemStorage storage) {
        JsonArray arr = new JsonArray();
        for (Map.Entry<ItemKey, Long> entry : storage.getStorageUnsafe().keyEntrySet()) {
            if (entry.getValue() <= 0) continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("item", SerializeUtils.object2String(entry.getKey().getItemStack()));
            obj.addProperty("amount", entry.getValue());
            arr.add(obj);
        }
        return arr.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static ItemStorage deserializeStorage(byte[] data) {
        ItemStorage storage = new ItemStorage();
        JsonArray arr =
                JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonArray();
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String itemStr = obj.get("item").getAsString();
            long amount = obj.get("amount").getAsLong();
            Object deserialized = SerializeUtils.string2Object(itemStr);
            if (deserialized instanceof ItemStack itemStack) {
                storage.addItem(new ItemKey(itemStack), amount);
            }
        }
        return storage;
    }

    public static String serializeCompletedSteps(Set<CraftStep> completed, List<CraftStep> allSteps) {
        Map<CraftStep, Integer> stepIndex = new IdentityHashMap<>();
        for (int i = 0; i < allSteps.size(); i++) {
            stepIndex.put(allSteps.get(i), i);
        }
        StringBuilder sb = new StringBuilder();
        for (CraftStep step : completed) {
            Integer idx = stepIndex.get(step);
            if (idx != null) {
                if (sb.length() > 0) sb.append(",");
                sb.append(idx);
            }
        }
        return sb.toString();
    }

    public static Set<Integer> deserializeCompletedIndices(String data) {
        Set<Integer> indices = new HashSet<>();
        if (data == null || data.isEmpty()) return indices;
        for (String s : data.split(",")) {
            try {
                indices.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return indices;
    }

    private static JsonArray serializeItemArray(ItemStack[] items) {
        JsonArray arr = new JsonArray();
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("item", SerializeUtils.object2String(item));
            obj.addProperty("amount", item.getAmount());
            arr.add(obj);
        }
        return arr;
    }

    private static ItemStack[] deserializeItemArray(JsonArray arr) {
        List<ItemStack> list = new ArrayList<>();
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String itemStr = obj.get("item").getAsString();
            int amount = obj.get("amount").getAsInt();
            Object deserialized = SerializeUtils.string2Object(itemStr);
            if (deserialized instanceof ItemStack itemStack) {
                itemStack.setAmount(amount);
                list.add(itemStack);
            } else {
                return null;
            }
        }
        return list.toArray(new ItemStack[0]);
    }

    private static JsonObject serializeLocation(Location loc) {
        JsonObject obj = new JsonObject();
        obj.addProperty("world", loc.getWorld().getName());
        obj.addProperty("x", loc.getBlockX());
        obj.addProperty("y", loc.getBlockY());
        obj.addProperty("z", loc.getBlockZ());
        return obj;
    }

    private static Location deserializeLocation(JsonObject obj) {
        String worldName = obj.get("world").getAsString();
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        int x = obj.get("x").getAsInt();
        int y = obj.get("y").getAsInt();
        int z = obj.get("z").getAsInt();
        return new Location(world, x, y, z);
    }
}
