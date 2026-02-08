package com.todolist.mod.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridge class that holds all direct JEI API references.
 * Only class-loaded when JEI is present (called from TodoJEIPlugin or guarded by isJEILoaded()).
 */
public class JEIBridge {
    private static IJeiRuntime runtime;

    public static void setRuntime(IJeiRuntime rt) {
        runtime = rt;
    }

    public static boolean isAvailable() {
        return runtime != null;
    }

    /**
     * Opens JEI recipe view showing how to craft this item (item as OUTPUT).
     */
    public static void showRecipes(ItemStack stack) {
        if (runtime == null || stack.isEmpty()) return;
        try {
            IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
            IFocus<ItemStack> focus = focusFactory.createFocus(
                    RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack);
            List<IFocus<?>> focuses = new ArrayList<>();
            focuses.add(focus);
            runtime.getRecipesGui().show(focuses);
        } catch (Exception ignored) {}
    }

    /**
     * Opens JEI uses view showing what recipes use this item (item as INPUT).
     */
    public static void showUses(ItemStack stack) {
        if (runtime == null || stack.isEmpty()) return;
        try {
            IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
            IFocus<ItemStack> focus = focusFactory.createFocus(
                    RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, stack);
            List<IFocus<?>> focuses = new ArrayList<>();
            focuses.add(focus);
            runtime.getRecipesGui().show(focuses);
        } catch (Exception ignored) {}
    }

    /**
     * Check if JEI's ingredient list overlay is currently showing on the right side.
     */
    public static boolean isIngredientListVisible() {
        if (runtime == null) return false;
        try {
            return runtime.getIngredientListOverlay().isListDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
