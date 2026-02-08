package com.todolist.mod.forge;

import com.todolist.mod.platform.IConfigAccess;

public class ForgeConfigAccess implements IConfigAccess {
    @Override public boolean hudVisible() { return ForgeConfigHandler.CLIENT.hudVisible.get(); }
    @Override public void setHudVisible(boolean v) { ForgeConfigHandler.CLIENT.hudVisible.set(v); }
    @Override public int hudX() { return ForgeConfigHandler.CLIENT.hudX.get(); }
    @Override public void setHudX(int x) { ForgeConfigHandler.CLIENT.hudX.set(x); }
    @Override public int hudY() { return ForgeConfigHandler.CLIENT.hudY.get(); }
    @Override public void setHudY(int y) { ForgeConfigHandler.CLIENT.hudY.set(y); }
    @Override public float hudOpacity() { return ForgeConfigHandler.CLIENT.hudOpacity.get().floatValue(); }
    @Override public void setHudOpacity(float v) { ForgeConfigHandler.CLIENT.hudOpacity.set((double) v); }
    @Override public float hudScale() { return ForgeConfigHandler.CLIENT.hudScale.get().floatValue(); }
    @Override public void setHudScale(float v) { ForgeConfigHandler.CLIENT.hudScale.set((double) v); }
    @Override public int hudMaxItems() { return ForgeConfigHandler.CLIENT.hudMaxItems.get(); }
    @Override public void setHudMaxItems(int v) { ForgeConfigHandler.CLIENT.hudMaxItems.set(v); }
    @Override public boolean showCompletedInHud() { return ForgeConfigHandler.CLIENT.showCompletedInHud.get(); }
    @Override public void setShowCompletedInHud(boolean v) { ForgeConfigHandler.CLIENT.showCompletedInHud.set(v); }
    @Override public String hudActiveCategory() { return ForgeConfigHandler.CLIENT.hudActiveCategory.get(); }
    @Override public void setHudActiveCategory(String cat) { ForgeConfigHandler.CLIENT.hudActiveCategory.set(cat); }
    @Override public boolean completionSoundEnabled() { return ForgeConfigHandler.CLIENT.playCompletionSound.get(); }
    @Override public void setCompletionSoundEnabled(boolean v) { ForgeConfigHandler.CLIENT.playCompletionSound.set(v); }
    @Override public boolean inventoryOverlayEnabled() { return ForgeConfigHandler.CLIENT.inventoryOverlayEnabled.get(); }
    @Override public int inventoryOverlayMaxItems() { return ForgeConfigHandler.CLIENT.inventoryOverlayMaxItems.get(); }
}
