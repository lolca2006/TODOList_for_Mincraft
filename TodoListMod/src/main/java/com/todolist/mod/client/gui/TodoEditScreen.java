package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.Category;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.common.model.TodoItem;
import com.todolist.mod.common.model.TodoVisibility;
import com.todolist.mod.integration.jei.JEIHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TodoEditScreen extends Screen {
    private final Screen parentScreen;
    private final TodoItem item;
    private EditBox textInput;
    private String selectedCategory;
    private TodoVisibility selectedVisibility;
    private final List<ResourceRequirement> resources;
    private int itemScrollOffset = 0;

    private int panelLeft, panelTop, panelRight, panelBottom;
    private static final int PANEL_W = 300, PANEL_H = 260;

    // Status message
    private String statusMessage = null;
    private long statusMessageTime = 0;

    public TodoEditScreen(Screen parent, TodoItem item) {
        super(Component.literal("Edit Todo"));
        this.parentScreen = parent;
        this.item = item;
        this.selectedCategory = item.getCategory();
        this.selectedVisibility = item.getVisibility();
        this.resources = item.getResources().stream()
                .map(ResourceRequirement::copy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected void init() {
        super.init();
        panelLeft = (this.width - PANEL_W) / 2;
        panelTop = (this.height - PANEL_H) / 2;
        panelRight = panelLeft + PANEL_W;
        panelBottom = panelTop + PANEL_H;

        // Text input
        textInput = new EditBox(this.font, panelLeft + 10, panelTop + 28, PANEL_W - 20, 18, Component.literal("Text"));
        textInput.setMaxLength(200);
        textInput.setValue(item.getText());
        this.addRenderableWidget(textInput);

        // Category buttons
        List<Category> cats = ClientTodoManager.getCategories();
        int cx = panelLeft + 10;
        int cy = panelTop + 58;
        for (int i = 0; i < Math.min(cats.size(), 6); i++) {
            Category cat = cats.get(i);
            boolean sel = cat.getName().equals(selectedCategory);
            Button btn = Button.builder(Component.literal(cat.getName()), b -> {
                selectedCategory = cat.getName();
                this.rebuildWidgets();
            }).bounds(cx, cy, 46, 14).build();
            if (sel) btn.active = false;
            this.addRenderableWidget(btn);
            cx += 48;
            if (cx + 46 > panelRight - 10) { cx = panelLeft + 10; cy += 16; }
        }

        // Visibility toggle
        int visY = panelTop + 96;
        this.addRenderableWidget(Button.builder(
                Component.literal(selectedVisibility == TodoVisibility.PRIVATE ? "Private" : "Shared"),
                btn -> {
                    selectedVisibility = selectedVisibility == TodoVisibility.PRIVATE ? TodoVisibility.SHARED : TodoVisibility.PRIVATE;
                    btn.setMessage(Component.literal(selectedVisibility == TodoVisibility.PRIVATE ? "Private" : "Shared"));
                }).bounds(panelLeft + 10, visY, 60, 16).build());

        // Add resource button -> opens ResourcePickerScreen
        this.addRenderableWidget(Button.builder(Component.literal("+ Resources"), btn -> {
            this.minecraft.setScreen(new ResourcePickerScreen(this, newResources -> {
                for (ResourceRequirement req : newResources) {
                    boolean merged = false;
                    for (ResourceRequirement existing : resources) {
                        if (existing.getItemId().equals(req.getItemId())) {
                            existing.setCount(existing.getCount() + req.getCount());
                            merged = true;
                            break;
                        }
                    }
                    if (!merged) {
                        resources.add(req.copy());
                    }
                }
            }));
        }).bounds(panelLeft + 80, visY, 80, 16).build());

        // JEI button - opens ResourcePickerScreen with JEI features
        if (JEIHelper.isJEILoaded()) {
            this.addRenderableWidget(Button.builder(Component.literal("JEI +"), btn -> {
                // Open ResourcePickerScreen (which has JEI integration)
                this.minecraft.setScreen(new ResourcePickerScreen(this, newResources -> {
                    for (ResourceRequirement req : newResources) {
                        boolean merged = false;
                        for (ResourceRequirement existing : resources) {
                            if (existing.getItemId().equals(req.getItemId())) {
                                existing.setCount(existing.getCount() + req.getCount());
                                merged = true;
                                break;
                            }
                        }
                        if (!merged) {
                            resources.add(req.copy());
                        }
                    }
                }));
            }).bounds(panelRight - 50, visY, 40, 16).build());
        }

        // Save button
        this.addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            String newText = textInput.getValue().trim();
            if (!newText.isEmpty()) {
                ClientTodoManager.editTodo(item.getId(), newText, selectedCategory, selectedVisibility, resources);
            }
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 10, panelBottom - 28, 60, 18).build());

        // Delete button
        this.addRenderableWidget(Button.builder(Component.literal("Delete"), btn -> {
            ClientTodoManager.deleteTodo(item.getId());
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 80, panelBottom - 28, 60, 18).build());

        // Cancel button
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 70, panelBottom - 28, 60, 18).build());
    }

    private void setStatus(String msg) {
        statusMessage = msg;
        statusMessageTime = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        // Labels
        graphics.drawString(this.font, "Category:", panelLeft + 10, panelTop + 50, 0xFFAAAAAA, false);
        graphics.drawString(this.font, "Visibility:", panelLeft + 10, panelTop + 88, 0xFFAAAAAA, false);

        // Resources section
        int itemsY = panelTop + 118;
        String resLabel = "Resources (" + resources.size() + "):";
        graphics.drawString(this.font, resLabel, panelLeft + 10, itemsY, 0xFFAAAAAA, false);

        // Hint text
        if (JEIHelper.isJEILoaded()) {
            graphics.drawString(this.font, "R=Recipe  U=Uses  S=+Ingredients",
                    panelLeft + 10 + this.font.width(resLabel) + 6, itemsY, 0xFF556655, false);
        } else {
            graphics.drawString(this.font, "S=+Recipe Ingredients",
                    panelLeft + 10 + this.font.width(resLabel) + 6, itemsY, 0xFF556655, false);
        }
        itemsY += 14;

        int maxVisible = 5;
        for (int i = itemScrollOffset; i < Math.min(resources.size(), itemScrollOffset + maxVisible); i++) {
            ResourceRequirement req = resources.get(i);
            int iy = itemsY + (i - itemScrollOffset) * 18;

            try {
                var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.getItemId()));
                if (regItem != null) {
                    ItemStack stack = new ItemStack(regItem);

                    // Row background
                    boolean rowHovered = mouseX >= panelLeft + 10 && mouseX <= panelRight - 10 && mouseY >= iy && mouseY < iy + 16;
                    if (rowHovered) {
                        graphics.fill(panelLeft + 10, iy, panelRight - 10, iy + 16, 0x22FFFFFF);
                    }

                    // Item icon (half scale)
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.75f, 0.75f, 1.0f);
                    graphics.renderItem(stack, (int)((panelLeft + 12) / 0.75f), (int)((iy) / 0.75f));
                    graphics.pose().popPose();

                    // Item name
                    String name = stack.getHoverName().getString();
                    if (name.length() > 22) name = name.substring(0, 20) + "..";
                    graphics.drawString(this.font, name, panelLeft + 28, iy + 4, 0xFFDDDDDD, false);

                    // Count
                    String countStr = "x" + req.getCount();
                    graphics.drawString(this.font, countStr, panelRight - 55, iy + 4, 0xFFFFFF00, false);

                    // Remove button
                    int removeX = panelRight - 22;
                    boolean rmHovered = mouseX >= removeX && mouseX <= removeX + 12 && mouseY >= iy + 2 && mouseY <= iy + 14;
                    graphics.drawString(this.font, "\u00D7", removeX + 2, iy + 4,
                            rmHovered ? 0xFFFF6666 : 0xFFAA4444, false);

                    // Tooltip on hover with JEI hints
                    if (rowHovered) {
                        List<Component> tooltip = new ArrayList<>();
                        tooltip.add(stack.getHoverName());
                        tooltip.add(Component.literal(req.getItemId() + " x" + req.getCount())
                                .withStyle(ChatFormatting.GRAY));
                        if (JEIHelper.isJEILoaded()) {
                            tooltip.add(Component.literal("R: show recipe (JEI)").withStyle(ChatFormatting.GREEN));
                            tooltip.add(Component.literal("U: show uses (JEI)").withStyle(ChatFormatting.GREEN));
                        }
                        tooltip.add(Component.literal("S: add recipe ingredients").withStyle(ChatFormatting.AQUA));
                        tooltip.add(Component.literal("Click \u00D7 to remove").withStyle(ChatFormatting.RED));
                        graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (resources.isEmpty()) {
            graphics.drawString(this.font, "No resources added yet", panelLeft + 10, itemsY + 6, 0xFF666666, false);
        }

        // Created by info
        if (item.getCreatedByName() != null) {
            String byText = "By: " + item.getCreatedByName();
            graphics.drawString(this.font, byText, panelRight - this.font.width(byText) - 10, panelTop + 88, 0xFF888888, false);
        }

        // Status message (auto-fades after 3 seconds)
        if (statusMessage != null) {
            long age = System.currentTimeMillis() - statusMessageTime;
            if (age < 3000) {
                int alpha = age > 2000 ? (int)(255 * (1.0 - (age - 2000) / 1000.0)) : 255;
                int color = (alpha << 24) | 0x44FF44;
                graphics.drawCenteredString(this.font, statusMessage, this.width / 2, panelBottom - 42, color);
            } else {
                statusMessage = null;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Gets the resource index at the given mouse position, or -1 if none.
     */
    private int getResourceIndexAt(double mouseY) {
        int itemsY = panelTop + 132;
        int maxVisible = 5;
        for (int i = itemScrollOffset; i < Math.min(resources.size(), itemScrollOffset + maxVisible); i++) {
            int iy = itemsY + (i - itemScrollOffset) * 18;
            if (mouseY >= iy && mouseY < iy + 16) return i;
        }
        return -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Get mouse position for hovered resource
        double mx = this.minecraft.mouseHandler.xpos() * this.width / this.minecraft.getWindow().getWidth();
        double my = this.minecraft.mouseHandler.ypos() * this.height / this.minecraft.getWindow().getHeight();

        if (mx >= panelLeft + 10 && mx <= panelRight - 10) {
            int hoveredIdx = getResourceIndexAt(my);
            if (hoveredIdx >= 0 && hoveredIdx < resources.size()) {
                ResourceRequirement req = resources.get(hoveredIdx);
                var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.getItemId()));
                if (regItem != null) {
                    ItemStack stack = new ItemStack(regItem);

                    // R = Show recipe in JEI
                    if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
                        if (JEIHelper.isJEILoaded()) {
                            JEIHelper.showRecipesFor(stack);
                            return true;
                        }
                    }

                    // U = Show uses in JEI
                    if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_U) {
                        if (JEIHelper.isJEILoaded()) {
                            JEIHelper.showUsesFor(stack);
                            return true;
                        }
                    }

                    // S = Add recipe ingredients to this todo's resources
                    if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_S) {
                        List<ResourceRequirement> ingredients = JEIHelper.getRecipeIngredients(req.getItemId());
                        if (!ingredients.isEmpty()) {
                            for (ResourceRequirement ing : ingredients) {
                                boolean merged = false;
                                for (ResourceRequirement existing : resources) {
                                    if (existing.getItemId().equals(ing.getItemId())) {
                                        existing.setCount(existing.getCount() + ing.getCount() * req.getCount());
                                        merged = true;
                                        break;
                                    }
                                }
                                if (!merged) {
                                    resources.add(new ResourceRequirement(ing.getItemId(), ing.getCount() * req.getCount()));
                                }
                            }
                            setStatus("Added " + ingredients.size() + " ingredients for " + stack.getHoverName().getString());
                        } else {
                            setStatus("No recipe found for " + stack.getHoverName().getString());
                        }
                        return true;
                    }
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int itemsY = panelTop + 132;
        int maxVisible = 5;

        for (int i = itemScrollOffset; i < Math.min(resources.size(), itemScrollOffset + maxVisible); i++) {
            int iy = itemsY + (i - itemScrollOffset) * 18;

            // Remove button click
            int removeX = panelRight - 22;
            if (mouseX >= removeX && mouseX <= removeX + 12 && mouseY >= iy + 2 && mouseY <= iy + 14) {
                resources.remove(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && itemScrollOffset > 0) itemScrollOffset--;
        else if (delta < 0 && itemScrollOffset < resources.size() - 5) itemScrollOffset++;
        return true;
    }

    /**
     * Called by JEI ghost ingredient handler when an item is dragged from JEI into this screen.
     */
    public void addResourceFromJEI(String itemId, int count) {
        for (ResourceRequirement existing : resources) {
            if (existing.getItemId().equals(itemId)) {
                existing.setCount(existing.getCount() + count);
                setStatus("Added +" + count + " to existing resource");
                return;
            }
        }
        resources.add(new ResourceRequirement(itemId, count));
        setStatus("Added resource from JEI!");
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
