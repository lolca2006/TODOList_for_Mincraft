package com.todolist.mod.common.data;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDTypeAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
    @Override
    public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
    }

    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) return null;
        return UUID.fromString(json.getAsString());
    }
}
