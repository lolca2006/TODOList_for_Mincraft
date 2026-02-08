package com.todolist.mod.platform;

public interface IConfigAccess {
    // HUD
    boolean hudVisible();
    void setHudVisible(boolean v);
    int hudX();
    void setHudX(int x);
    int hudY();
    void setHudY(int y);
    float hudOpacity();
    void setHudOpacity(float v);
    float hudScale();
    void setHudScale(float v);
    int hudMaxItems();
    void setHudMaxItems(int v);
    boolean showCompletedInHud();
    void setShowCompletedInHud(boolean v);
    String hudActiveCategory();
    void setHudActiveCategory(String cat);
    boolean completionSoundEnabled();
    void setCompletionSoundEnabled(boolean v);

    // Inventory Overlay
    boolean inventoryOverlayEnabled();
    int inventoryOverlayMaxItems();
}
