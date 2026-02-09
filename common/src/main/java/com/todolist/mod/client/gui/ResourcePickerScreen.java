package com.todolist.mod.client.gui;

import com.todolist.mod.common.model.BlockGroup;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.integration.jei.JEIHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ResourcePickerScreen extends Screen {
    private final Screen parentScreen;
    private final Consumer<List<ResourceRequirement>> onResourcesAdded;

    private EditBox searchField;
    private EditBox countField;
    private final List<ResourceRequirement> pendingResources = new ArrayList<>();
    private int scrollOffset = 0;

    // Matched items from search
    private List<ItemStack> searchResults = new ArrayList<>();

    private int panelLeft, panelTop, panelRight, panelBottom;

    // Status message for feedback
    private String statusMessage = null;
    private long statusMessageTime = 0;

    public ResourcePickerScreen(Screen parent, Consumer<List<ResourceRequirement>> onResourcesAdded) {
        super(Component.literal("Add Resources"));
        this.parentScreen = parent;
        this.onResourcesAdded = onResourcesAdded;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        panelLeft = centerX - 160;
        panelTop = centerY - 135;
        panelRight = centerX + 160;
        panelBottom = centerY + 135;

        // Search field
        searchField = new EditBox(this.font, panelLeft + 5, panelTop + 20, 210, 16, Component.literal("Search"));
        searchField.setHint(Component.literal("Search items... @mod #tag"));
        searchField.setMaxLength(100);
        searchField.setResponder(text -> updateSearch());
        this.addRenderableWidget(searchField);

        // Count field
        countField = new EditBox(this.font, panelLeft + 220, panelTop + 20, 40, 16, Component.literal("Count"));
        countField.setValue("1");
        countField.setMaxLength(5);
        countField.setFilter(s -> s.matches("\\d*"));
        this.addRenderableWidget(countField);

        // Quick count buttons
        int btnY = panelTop + 40;
        this.addRenderableWidget(Button.builder(Component.literal("x1"), b -> countField.setValue("1"))
                .bounds(panelLeft + 5, btnY, 30, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("x16"), b -> countField.setValue("16"))
                .bounds(panelLeft + 38, btnY, 30, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("x64"), b -> countField.setValue("64"))
                .bounds(panelLeft + 71, btnY, 30, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("x999"), b -> countField.setValue("999"))
                .bounds(panelLeft + 104, btnY, 35, 14).build());

        // Block group buttons - clicking sets search to #tag to show all items in that category
        Map<String, BlockGroup> groups = BlockGroup.getDefaults();
        int groupY = panelTop + 58;
        int groupX = panelLeft + 5;
        int btnWidth = 48;
        int count = 0;
        for (Map.Entry<String, BlockGroup> entry : groups.entrySet()) {
            BlockGroup group = entry.getValue();
            int bx = groupX + (count % 6) * (btnWidth + 2);
            int by = groupY + (count / 6) * 14;
            this.addRenderableWidget(Button.builder(Component.literal(group.getName()), btn -> {
                // Set search to #tag - shows all items matching this tag as options to pick from
                searchField.setValue("#" + group.getTagId().replace("minecraft:", ""));
                setStatus("Showing " + group.getName() + " - click to add");
            }).bounds(bx, by, btnWidth, 12).build());
            count++;
        }

        // --- JEI Integration Buttons ---
        int jeiY = panelTop + 40;
        if (JEIHelper.isJEILoaded()) {
            // "JEI" button - opens JEI recipe view for first search result
            this.addRenderableWidget(Button.builder(Component.literal("JEI \u27A1"), btn -> {
                if (!searchResults.isEmpty()) {
                    JEIHelper.showRecipesFor(searchResults.get(0));
                } else {
                    setStatus("Search for an item first!");
                }
            }).bounds(panelRight - 65, jeiY, 60, 14).build());
        }

        // "Recipe" button - looks up recipe ingredients and adds them to pending
        this.addRenderableWidget(Button.builder(Component.literal("\u2699 Recipe"), btn -> {
            if (!searchResults.isEmpty()) {
                ItemStack first = searchResults.get(0);
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(first.getItem());
                if (key != null) {
                    List<ResourceRequirement> ingredients = JEIHelper.getRecipeIngredients(key.toString());
                    if (!ingredients.isEmpty()) {
                        int qty = getCount();
                        for (ResourceRequirement req : ingredients) {
                            addToPending(req.getItemId(), req.getCount() * qty);
                        }
                        setStatus("Added " + ingredients.size() + " recipe ingredients!");
                    } else {
                        setStatus("No recipe found for this item");
                    }
                }
            } else {
                setStatus("Search for an item first!");
            }
        }).bounds(panelLeft + 145, btnY, 60, 14).build());

        // Confirm button
        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), btn -> {
            if (!pendingResources.isEmpty() && onResourcesAdded != null) {
                onResourcesAdded.accept(new ArrayList<>(pendingResources));
            }
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 110, panelBottom - 20, 50, 16).build());

        // Clear pending
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), btn -> {
            pendingResources.clear();
        }).bounds(panelRight - 55, panelBottom - 20, 50, 16).build());

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 5, panelBottom - 20, 40, 16).build());
    }

    private void setStatus(String msg) {
        statusMessage = msg;
        statusMessageTime = System.currentTimeMillis();
    }

    private int getCount() {
        try {
            return Math.max(1, Integer.parseInt(countField.getValue()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void updateSearch() {
        String search = searchField.getValue().toLowerCase().trim();
        if (search.isEmpty()) {
            searchResults.clear();
            return;
        }

        searchResults = new ArrayList<>();
        int maxResults = 50;

        BuiltInRegistries.ITEM.forEach(item -> {
            if (searchResults.size() >= maxResults) return;
            ItemStack stack = new ItemStack(item);
            ResourceLocation regKey = BuiltInRegistries.ITEM.getKey(item);
            if (regKey == null) return;

            String name = stack.getHoverName().getString().toLowerCase();
            String id = regKey.toString().toLowerCase();
            String modId = regKey.getNamespace().toLowerCase();

            if (search.startsWith("@")) {
                if (modId.contains(search.substring(1))) searchResults.add(stack);
            } else if (search.startsWith("#")) {
                String tagSearch = search.substring(1);
                boolean matchesTag = stack.getTags()
                        .anyMatch(tag -> tag.location().toString().toLowerCase().contains(tagSearch));
                if (matchesTag) searchResults.add(stack);
            } else {
                if (name.contains(search) || id.contains(search)) searchResults.add(stack);
            }
        });
        scrollOffset = 0;
    }

    private void addBlockGroup(BlockGroup group) {
        int count = getCount();
        ResourceLocation tagLoc = ResourceLocation.parse(group.getTagId());
        TagKey<Item> tagKey = TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagLoc);

        int added = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.is(tagKey)) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                if (key != null) {
                    addToPending(key.toString(), count);
                    added++;
                }
            }
        }
        if (added > 0) {
            setStatus("Added " + added + " items from " + group.getName());
        }
    }

    private void addToPending(String itemId, int count) {
        for (ResourceRequirement req : pendingResources) {
            if (req.getItemId().equals(itemId)) {
                req.setCount(req.getCount() + count);
                return;
            }
        }
        pendingResources.add(new ResourceRequirement(itemId, count));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        // Count label
        graphics.drawString(this.font, "Qty:", panelLeft + 220, panelTop + 12, 0xFFAAAAAA, false);

        // JEI indicator
        if (JEIHelper.isJEILoaded()) {
            graphics.drawString(this.font, "\u2713 JEI", panelRight - 40, panelTop + 4, 0xFF44FF44, false);
        }

        // --- Search results grid ---
        int gridTop = panelTop + 92;
        int gridLeft = panelLeft + 5;
        int slotSize = 18;
        int cols = 9;
        int rows = 3;
        int startIndex = scrollOffset * cols;

        graphics.drawString(this.font, "Search Results (" + searchResults.size() + "):",
                gridLeft, gridTop - 10, 0xFF888888, false);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = startIndex + row * cols + col;
                if (index >= searchResults.size()) break;

                int x = gridLeft + col * slotSize;
                int y = gridTop + row * slotSize;
                ItemStack stack = searchResults.get(index);

                boolean hovered = mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize;
                graphics.fill(x, y, x + slotSize - 1, y + slotSize - 1, hovered ? 0x66FFFFFF : 0x22FFFFFF);
                graphics.renderItem(stack, x + 1, y + 1);

                if (hovered) {
                    // Show tooltip with item name + hint
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(stack.getHoverName());
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (key != null) tooltip.add(Component.literal(key.toString()).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.literal("Click: add x" + getCount()).withStyle(net.minecraft.ChatFormatting.YELLOW));
                    if (JEIHelper.isJEILoaded()) {
                        tooltip.add(Component.literal("R: show recipe (JEI)").withStyle(net.minecraft.ChatFormatting.GREEN));
                        tooltip.add(Component.literal("U: show uses (JEI)").withStyle(net.minecraft.ChatFormatting.GREEN));
                    }
                    tooltip.add(Component.literal("Shift+Click: add recipe ingredients").withStyle(net.minecraft.ChatFormatting.AQUA));
                    graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                }
            }
        }

        // --- Pending resources list ---
        int pendingTop = gridTop + rows * slotSize + 10;
        graphics.drawString(this.font, "Pending (" + pendingResources.size() + "):", gridLeft, pendingTop - 10, 0xFF88FF88, false);

        int pendingY = pendingTop;
        int maxVisible = 4;
        for (int i = 0; i < Math.min(pendingResources.size(), maxVisible); i++) {
            ResourceRequirement req = pendingResources.get(i);
            try {
                var regItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(req.getItemId()));
                if (regItem != null) {
                    ItemStack stack = new ItemStack(regItem);
                    graphics.renderItem(stack, gridLeft, pendingY);
                    String label = stack.getHoverName().getString();
                    if (label.length() > 20) label = label.substring(0, 18) + "..";
                    graphics.drawString(this.font, label + " x" + req.getCount(),
                            gridLeft + 20, pendingY + 4, 0xFFDDDDDD, false);

                    // Remove button
                    int removeX = panelRight - 20;
                    boolean rmHovered = mouseX >= removeX && mouseX <= removeX + 12 &&
                            mouseY >= pendingY + 2 && mouseY <= pendingY + 14;
                    graphics.drawString(this.font, "\u00D7", removeX + 2, pendingY + 4,
                            rmHovered ? 0xFFFF6666 : 0xFFAA4444, false);
                }
            } catch (Exception ignored) {}
            pendingY += 16;
        }
        if (pendingResources.size() > maxVisible) {
            graphics.drawString(this.font, "+" + (pendingResources.size() - maxVisible) + " more...",
                    gridLeft + 20, pendingY + 2, 0xFF888888, false);
        }

        // Status message (auto-fades after 3 seconds)
        if (statusMessage != null) {
            long age = System.currentTimeMillis() - statusMessageTime;
            if (age < 3000) {
                int alpha = age > 2000 ? (int)(255 * (1.0 - (age - 2000) / 1000.0)) : 255;
                int color = (alpha << 24) | 0x44FF44;
                graphics.drawCenteredString(this.font, statusMessage, this.width / 2, panelBottom - 36, color);
            } else {
                statusMessage = null;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    // Track which item the mouse is hovering over in the grid
    private int getHoveredGridIndex(double mouseX, double mouseY) {
        int gridTop = panelTop + 92;
        int gridLeft = panelLeft + 5;
        int slotSize = 18;
        int cols = 9;
        int rows = 3;
        int startIndex = scrollOffset * cols;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = startIndex + row * cols + col;
                if (index >= searchResults.size()) return -1;

                int x = gridLeft + col * slotSize;
                int y = gridTop + row * slotSize;

                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                    return index;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // R key = show recipe in JEI for hovered item
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R && JEIHelper.isJEILoaded()) {
            Minecraft mc = Minecraft.getInstance();
            int idx = getHoveredGridIndex(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth(),
                    mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight());
            if (idx >= 0 && idx < searchResults.size()) {
                JEIHelper.showRecipesFor(searchResults.get(idx));
                return true;
            }
        }

        // U key = show uses in JEI for hovered item
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_U && JEIHelper.isJEILoaded()) {
            Minecraft mc = Minecraft.getInstance();
            int idx = getHoveredGridIndex(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth(),
                    mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight());
            if (idx >= 0 && idx < searchResults.size()) {
                JEIHelper.showUsesFor(searchResults.get(idx));
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Click on search result to add
        int gridTop = panelTop + 92;
        int gridLeft = panelLeft + 5;
        int slotSize = 18;
        int cols = 9;
        int rows = 3;
        int startIndex = scrollOffset * cols;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = startIndex + row * cols + col;
                if (index >= searchResults.size()) break;

                int x = gridLeft + col * slotSize;
                int y = gridTop + row * slotSize;

                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                    ItemStack stack = searchResults.get(index);
                    ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (itemKey != null) {
                        // Shift+Click = add recipe ingredients
                        if (hasShiftDown()) {
                            List<ResourceRequirement> ingredients = JEIHelper.getRecipeIngredients(itemKey.toString());
                            if (!ingredients.isEmpty()) {
                                int qty = getCount();
                                for (ResourceRequirement req : ingredients) {
                                    addToPending(req.getItemId(), req.getCount() * qty);
                                }
                                setStatus("Added " + ingredients.size() + " recipe ingredients!");
                            } else {
                                setStatus("No recipe found");
                            }
                        } else {
                            // Normal click = add item with count
                            addToPending(itemKey.toString(), getCount());
                        }
                    }
                    return true;
                }
            }
        }

        // Click on pending resource remove button
        int pendingTop = gridTop + rows * slotSize + 10;
        int pendingY = pendingTop;
        int maxVisible = 4;
        for (int i = 0; i < Math.min(pendingResources.size(), maxVisible); i++) {
            int removeX = panelRight - 20;
            if (mouseX >= removeX && mouseX <= removeX + 12 &&
                    mouseY >= pendingY + 2 && mouseY <= pendingY + 14) {
                pendingResources.remove(i);
                return true;
            }
            pendingY += 16;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        int cols = 9;
        int totalRows = (searchResults.size() + cols - 1) / cols;
        if (scrollDeltaY > 0 && scrollOffset > 0) scrollOffset--;
        else if (scrollDeltaY < 0 && scrollOffset < totalRows - 3) scrollOffset++;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
