package com.todolist.mod.integration.jei;

import com.todolist.mod.client.gui.TodoEditScreen;
import com.todolist.mod.common.model.ResourceRequirement;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows dragging items from JEI's ingredient list directly into the TodoEditScreen.
 * Dropped items get added as resources to the current todo being edited.
 */
public class TodoEditGhostHandler implements IGhostIngredientHandler<TodoEditScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(TodoEditScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        // Only accept ItemStack ingredients
        if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
            // The entire resource area is a valid drop target
            targets.add(new Target<I>() {
                @Override
                public Rect2i getArea() {
                    // The resource section area of the edit screen
                    int panelLeft = (screen.width - 300) / 2;
                    int panelTop = (screen.height - 260) / 2;
                    return new Rect2i(panelLeft + 10, panelTop + 118, 280, 100);
                }

                @Override
                public void accept(I i) {
                    if (i instanceof ItemStack stack) {
                        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        if (key != null) {
                            // Access resources list via the screen's public method
                            screen.addResourceFromJEI(key.toString(), Math.max(1, stack.getCount()));
                        }
                    }
                }
            });
        }

        return targets;
    }

    @Override
    public void onComplete() {
        // Nothing to clean up
    }
}
