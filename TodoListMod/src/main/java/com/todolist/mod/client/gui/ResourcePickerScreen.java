package com.todolist.mod.client.gui;

import com.todolist.mod.common.model.BlockGroup;
import com.todolist.mod.common.model.ResourceRequirement;
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
import net.minecraftforge.registries.ForgeRegistries;

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
        panelLeft = centerX - 150;
        panelTop = centerY - 130;
        panelRight = centerX + 150;
        panelBottom = centerY + 130;

        // Search field
        searchField = new EditBox(this.font, panelLeft + 5, panelTop + 20, 200, 16, Component.literal("Search"));
        searchField.setHint(Component.literal("Search items... @mod #tag"));
        searchField.setMaxLength(100);
        searchField.setResponder(text -> updateSearch());
        this.addRenderableWidget(searchField);

        // Count field
        countField = new EditBox(this.font, panelLeft + 210, panelTop + 20, 40, 16, Component.literal("Count"));
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

        // Block group buttons
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
                addBlockGroup(group);
            }).bounds(bx, by, btnWidth, 12).build());
            count++;
        }

        // Add selected item button
        this.addRenderableWidget(Button.builder(Component.literal("+ Add Selected"), btn -> {
            addSelectedItem();
        }).bounds(panelLeft + 5, panelBottom - 38, 80, 14).build());

        // Confirm button
        this.addRenderableWidget(Button.builder(Component.literal("Confirm"), btn -> {
            if (!pendingResources.isEmpty() && onResourcesAdded != null) {
                onResourcesAdded.accept(new ArrayList<>(pendingResources));
            }
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 100, panelBottom - 20, 45, 16).build());

        // Clear pending
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), btn -> {
            pendingResources.clear();
        }).bounds(panelRight - 50, panelBottom - 20, 45, 16).build());

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 5, panelBottom - 20, 40, 16).build());
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

        ForgeRegistries.ITEMS.forEach(item -> {
            if (searchResults.size() >= maxResults) return;
            ItemStack stack = new ItemStack(item);
            ResourceLocation regKey = ForgeRegistries.ITEMS.getKey(item);
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

    private void addSelectedItem() {
        // Not used for individual click - items are added by clicking in the grid
    }

    private void addBlockGroup(BlockGroup group) {
        int count = getCount();
        ResourceLocation tagLoc = new ResourceLocation(group.getTagId());
        TagKey<Item> tagKey = ItemTags.create(tagLoc);

        ForgeRegistries.ITEMS.forEach(item -> {
            ItemStack stack = new ItemStack(item);
            if (stack.is(tagKey)) {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                if (key != null) {
                    addToPending(key.toString(), count);
                }
            }
        });
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
        this.renderBackground(graphics);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        // Count label
        graphics.drawString(this.font, "Qty:", panelLeft + 210, panelTop + 12, 0xFFAAAAAA, false);

        // --- Search results grid ---
        int gridTop = panelTop + 90;
        int gridLeft = panelLeft + 5;
        int slotSize = 18;
        int cols = 9;
        int rows = 3;
        int startIndex = scrollOffset * cols;

        graphics.drawString(this.font, "Search Results:", gridLeft, gridTop - 10, 0xFF888888, false);

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
                    graphics.renderTooltip(this.font, stack, mouseX, mouseY);
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
                var regItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(req.getItemId()));
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

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Click on search result to add
        int gridTop = panelTop + 90;
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
                    ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemKey != null) {
                        addToPending(itemKey.toString(), getCount());
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int cols = 9;
        int totalRows = (searchResults.size() + cols - 1) / cols;
        if (delta > 0 && scrollOffset > 0) scrollOffset--;
        else if (delta < 0 && scrollOffset < totalRows - 3) scrollOffset++;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
