package com.todolist.mod.integration.jei;

import com.todolist.mod.common.model.ResourceRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import com.todolist.mod.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;

/**
 * Safe helper class for JEI integration.
 * All JEI API calls go through JEIBridge which is only class-loaded when JEI is present.
 * Recipe lookup uses vanilla RecipeManager and works without JEI.
 */
public class JEIHelper {

    public static boolean isJEILoaded() {
        return Services.PLATFORM.isModLoaded("jei");
    }

    /**
     * Returns true if JEI is loaded AND its runtime is available.
     */
    public static boolean isJEIAvailable() {
        if (!isJEILoaded()) return false;
        return JEIBridge.isAvailable();
    }

    /**
     * Opens JEI recipe view showing how to craft this item.
     * Safe to call even if JEI is not loaded (will do nothing).
     */
    public static void showRecipesFor(ItemStack stack) {
        if (!isJEIAvailable()) return;
        JEIBridge.showRecipes(stack);
    }

    /**
     * Opens JEI uses view showing what recipes use this item.
     */
    public static void showUsesFor(ItemStack stack) {
        if (!isJEIAvailable()) return;
        JEIBridge.showUses(stack);
    }

    /**
     * Check if JEI's ingredient list is currently visible (e.g. on the right side).
     */
    public static boolean isJEIOverlayVisible() {
        if (!isJEIAvailable()) return false;
        return JEIBridge.isIngredientListVisible();
    }

    /**
     * Looks up the crafting recipe for an item and returns the ingredients as ResourceRequirements.
     * Uses vanilla RecipeManager - works WITHOUT JEI installed.
     *
     * @param targetItemId the registry name of the target item (e.g. "minecraft:oak_planks")
     * @return list of ResourceRequirements for the recipe ingredients, or empty if no recipe found
     */
    public static List<ResourceRequirement> getRecipeIngredients(String targetItemId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Collections.emptyList();

        var targetItem = BuiltInRegistries.ITEM.get(new ResourceLocation(targetItemId));
        if (targetItem == null) return Collections.emptyList();

        // Search all crafting recipes for one that outputs this item
        List<? extends net.minecraft.world.item.crafting.Recipe<?>> recipes =
                mc.level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);

        for (var recipe : recipes) {
            ItemStack result = recipe.getResultItem(mc.level.registryAccess());
            if (result.is(targetItem)) {
                return extractIngredients(recipe);
            }
        }

        // Also check smelting recipes
        var smeltingRecipes = mc.level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);
        for (var recipe : smeltingRecipes) {
            ItemStack result = recipe.getResultItem(mc.level.registryAccess());
            if (result.is(targetItem)) {
                return extractIngredients(recipe);
            }
        }

        return Collections.emptyList();
    }

    private static List<ResourceRequirement> extractIngredients(net.minecraft.world.item.crafting.Recipe<?> recipe) {
        Map<String, Integer> ingredientCounts = new LinkedHashMap<>();

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(stacks[0].getItem());
                if (key != null) {
                    ingredientCounts.merge(key.toString(), 1, Integer::sum);
                }
            }
        }

        List<ResourceRequirement> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ingredientCounts.entrySet()) {
            result.add(new ResourceRequirement(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
