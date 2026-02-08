package com.todolist.mod.common.data;

import com.google.gson.*;
import com.todolist.mod.common.model.ResourceRequirement;

import java.lang.reflect.Type;

/**
 * Gson adapter that handles both old format (plain strings) and new format (objects with count).
 * Old: "minecraft:oak_log" → ResourceRequirement("minecraft:oak_log", 1)
 * New: {"itemId": "minecraft:oak_log", "count": 64, "collected": false}
 */
public class ResourceRequirementTypeAdapter implements JsonSerializer<ResourceRequirement>, JsonDeserializer<ResourceRequirement> {

    @Override
    public JsonElement serialize(ResourceRequirement src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("itemId", src.getItemId());
        obj.addProperty("count", src.getCount());
        obj.addProperty("collected", src.isCollected());
        return obj;
    }

    @Override
    public ResourceRequirement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Handle old format: plain string
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            return new ResourceRequirement(json.getAsString(), 1);
        }

        // Handle new format: object
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String itemId = obj.has("itemId") ? obj.get("itemId").getAsString() : "";
            int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            boolean collected = obj.has("collected") && obj.get("collected").getAsBoolean();
            return new ResourceRequirement(itemId, count, collected);
        }

        return new ResourceRequirement("minecraft:air", 1);
    }
}
