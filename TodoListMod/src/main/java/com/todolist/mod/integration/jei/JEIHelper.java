package com.todolist.mod.integration.jei;

import net.minecraftforge.fml.ModList;

/**
 * Helper class for JEI integration.
 * All JEI API calls are isolated here to avoid class-loading issues when JEI is not present.
 */
public class JEIHelper {

    public static boolean isJEILoaded() {
        return ModList.get().isLoaded("jei");
    }
}
