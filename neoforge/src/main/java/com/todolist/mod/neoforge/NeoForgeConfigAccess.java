package com.todolist.mod.neoforge;

import com.todolist.mod.platform.IConfigAccess;

public class NeoForgeConfigAccess implements IConfigAccess {
    @Override public boolean hudVisible() { return NeoForgeConfigHandler.CLIENT.hudVisible.get(); }
    @Override public void setHudVisible(boolean v) { NeoForgeConfigHandler.CLIENT.hudVisible.set(v); }
    @Override public int hudX() { return NeoForgeConfigHandler.CLIENT.hudX.get(); }
    @Override public void setHudX(int x) { NeoForgeConfigHandler.CLIENT.hudX.set(x); }
    @Override public int hudY() { return NeoForgeConfigHandler.CLIENT.hudY.get(); }
    @Override public void setHudY(int y) { NeoForgeConfigHandler.CLIENT.hudY.set(y); }
    @Override public float hudOpacity() { return NeoForgeConfigHandler.CLIENT.hudOpacity.get().floatValue(); }
    @Override public void setHudOpacity(float v) { NeoForgeConfigHandler.CLIENT.hudOpacity.set((double) v); }
    @Override public float hudScale() { return NeoForgeConfigHandler.CLIENT.hudScale.get().floatValue(); }
    @Override public void setHudScale(float v) { NeoForgeConfigHandler.CLIENT.hudScale.set((double) v); }
    @Override public int hudMaxItems() { return NeoForgeConfigHandler.CLIENT.hudMaxItems.get(); }
    @Override public void setHudMaxItems(int v) { NeoForgeConfigHandler.CLIENT.hudMaxItems.set(v); }
    @Override public boolean showCompletedInHud() { return NeoForgeConfigHandler.CLIENT.showCompletedInHud.get(); }
    @Override public void setShowCompletedInHud(boolean v) { NeoForgeConfigHandler.CLIENT.showCompletedInHud.set(v); }
    @Override public String hudActiveCategory() { return NeoForgeConfigHandler.CLIENT.hudActiveCategory.get(); }
    @Override public void setHudActiveCategory(String cat) { NeoForgeConfigHandler.CLIENT.hudActiveCategory.set(cat); }
    @Override public boolean completionSoundEnabled() { return NeoForgeConfigHandler.CLIENT.playCompletionSound.get(); }
    @Override public void setCompletionSoundEnabled(boolean v) { NeoForgeConfigHandler.CLIENT.playCompletionSound.set(v); }
    @Override public boolean inventoryOverlayEnabled() { return NeoForgeConfigHandler.CLIENT.inventoryOverlayEnabled.get(); }
    @Override public int inventoryOverlayMaxItems() { return NeoForgeConfigHandler.CLIENT.inventoryOverlayMaxItems.get(); }
}
