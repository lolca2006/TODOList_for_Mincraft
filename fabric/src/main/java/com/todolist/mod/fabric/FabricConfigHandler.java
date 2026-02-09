package com.todolist.mod.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.todolist.mod.Constants;
import com.todolist.mod.platform.IConfigAccess;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfigHandler implements IConfigAccess {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData data = new ConfigData();
    private static boolean loaded = false;

    private static Path getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("todolistmod.json");
    }

    public static void load() {
        Path file = getConfigFile();
        if (Files.exists(file)) {
            try {
                String json = Files.readString(file);
                data = GSON.fromJson(json, ConfigData.class);
                if (data == null) data = new ConfigData();
            } catch (Exception e) {
                Constants.LOGGER.error("Failed to load config", e);
                data = new ConfigData();
            }
        }
        loaded = true;
    }

    public static void save() {
        try {
            Path file = getConfigFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            Constants.LOGGER.error("Failed to save config", e);
        }
    }

    private static void ensureLoaded() {
        if (!loaded) load();
    }

    // --- IConfigAccess implementation ---

    @Override public boolean hudVisible() { ensureLoaded(); return data.hudVisible; }
    @Override public void setHudVisible(boolean v) { data.hudVisible = v; save(); }
    @Override public int hudX() { ensureLoaded(); return data.hudX; }
    @Override public void setHudX(int x) { data.hudX = x; save(); }
    @Override public int hudY() { ensureLoaded(); return data.hudY; }
    @Override public void setHudY(int y) { data.hudY = y; save(); }
    @Override public float hudOpacity() { ensureLoaded(); return data.hudOpacity; }
    @Override public void setHudOpacity(float v) { data.hudOpacity = v; save(); }
    @Override public float hudScale() { ensureLoaded(); return data.hudScale; }
    @Override public void setHudScale(float v) { data.hudScale = v; save(); }
    @Override public int hudMaxItems() { ensureLoaded(); return data.hudMaxItems; }
    @Override public void setHudMaxItems(int v) { data.hudMaxItems = v; save(); }
    @Override public boolean showCompletedInHud() { ensureLoaded(); return data.showCompletedInHud; }
    @Override public void setShowCompletedInHud(boolean v) { data.showCompletedInHud = v; save(); }
    @Override public String hudActiveCategory() { ensureLoaded(); return data.hudActiveCategory; }
    @Override public void setHudActiveCategory(String cat) { data.hudActiveCategory = cat; save(); }
    @Override public boolean completionSoundEnabled() { ensureLoaded(); return data.completionSound; }
    @Override public void setCompletionSoundEnabled(boolean v) { data.completionSound = v; save(); }
    @Override public boolean inventoryOverlayEnabled() { ensureLoaded(); return data.inventoryOverlayEnabled; }
    @Override public int inventoryOverlayMaxItems() { ensureLoaded(); return data.inventoryOverlayMaxItems; }

    // --- Config data class ---

    private static class ConfigData {
        boolean hudVisible = true;
        int hudX = 10;
        int hudY = 10;
        float hudOpacity = 0.7f;
        float hudScale = 1.0f;
        int hudMaxItems = 8;
        boolean showCompletedInHud = false;
        String hudActiveCategory = "";
        boolean completionSound = true;
        boolean inventoryOverlayEnabled = true;
        int inventoryOverlayMaxItems = 15;
    }
}
