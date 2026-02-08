package com.todolist.mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String CATEGORY = "key.categories.todolistmod";

    public static final KeyMapping OPEN_TODO_KEY = new KeyMapping(
            "key.todolistmod.open_todo",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY
    );

    public static final KeyMapping TOGGLE_HUD_KEY = new KeyMapping(
            "key.todolistmod.toggle_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static final KeyMapping EDIT_HUD_KEY = new KeyMapping(
            "key.todolistmod.edit_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY
    );
}
