package com.todolist.mod.client.gui;

import com.todolist.mod.common.model.BlockGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
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

public class ItemPickerScreen extends Screen {
    private final Screen parentScreen;
    private final Consumer<String> onItemSelected;
    private EditBox searchField;
    private List<ItemStack> allItems;
    private List<ItemStack> filteredItems;
    private int scrollOffset = 0;
    private final int columns = 9;
    private final int slotSize = 20;
    private String selectedGroup = null;

    private int panelLeft, panelTop, panelRight, panelBottom;

    public ItemPickerScreen(Screen parent, Consumer<String> onItemSelected) {
        super(Component.literal("Item Picker"));
        this.parentScreen = parent;
        this.onItemSelected = onItemSelected;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        panelLeft = centerX - 130;
        panelTop = centerY - 130;
        panelRight = centerX + 130;
        panelBottom = centerY + 130;

        // Populate all items
        allItems = new ArrayList<>();
        ForgeRegistries.ITEMS.forEach(item -> allItems.add(new ItemStack(item)));
        filteredItems = new ArrayList<>(allItems);

        // Search field
        searchField = new EditBox(this.font, panelLeft + 5, panelTop + 20, panelRight - panelLeft - 10, 16,
                Component.literal("Search"));
        searchField.setHint(Component.literal("Search... @mod #tag"));
        searchField.setMaxLength(100);
        searchField.setResponder(text -> filterItems());
        this.addRenderableWidget(searchField);

        // Block group buttons
        Map<String, BlockGroup> groups = BlockGroup.getDefaults();
        int groupY = panelTop + 40;
        int groupX = panelLeft + 5;
        int btnWidth = 50;
        int count = 0;

        for (Map.Entry<String, BlockGroup> entry : groups.entrySet()) {
            BlockGroup group = entry.getValue();
            int bx = groupX + (count % 5) * (btnWidth + 2);
            int by = groupY + (count / 5) * 14;

            this.addRenderableWidget(Button.builder(
                    Component.literal(group.getName()),
                    btn -> {
                        selectedGroup = selectedGroup != null && selectedGroup.equals(group.getTagId()) ? null : group.getTagId();
                        filterItems();
                    }).bounds(bx, by, btnWidth, 12).build());
            count++;
        }

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 45, panelBottom - 20, 40, 16).build());

        // Clear button
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), btn -> {
            if (onItemSelected != null) onItemSelected.accept(null);
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelLeft + 5, panelBottom - 20, 40, 16).build());
    }

    private void filterItems() {
        String search = searchField.getValue().toLowerCase().trim();
        filteredItems = allItems.stream()
                .filter(stack -> {
                    ResourceLocation regKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    String id = regKey != null ? regKey.toString().toLowerCase() : "";
                    String modId = regKey != null ? regKey.getNamespace().toLowerCase() : "";

                    if (!search.isEmpty()) {
                        // @modid search - filter by mod namespace (like JEI)
                        if (search.startsWith("@")) {
                            String modSearch = search.substring(1);
                            if (!modId.contains(modSearch)) return false;
                        }
                        // #tag search - filter by item tags
                        else if (search.startsWith("#")) {
                            String tagSearch = search.substring(1);
                            boolean matchesTag = stack.getTags()
                                    .anyMatch(tag -> tag.location().toString().toLowerCase().contains(tagSearch));
                            if (!matchesTag) return false;
                        }
                        // Normal text search - match name or full ID
                        else {
                            String name = stack.getHoverName().getString().toLowerCase();
                            if (!name.contains(search) && !id.contains(search)) return false;
                        }
                    }
                    if (selectedGroup != null) {
                        ResourceLocation tagLoc = new ResourceLocation(selectedGroup);
                        TagKey<Item> tagKey = ItemTags.create(tagLoc);
                        return stack.is(tagKey);
                    }
                    return true;
                })
                .collect(Collectors.toList());
        scrollOffset = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        // Item grid
        int gridTop = panelTop + 72;
        int gridLeft = panelLeft + 10;
        int rows = (panelBottom - gridTop - 25) / slotSize;
        int startIndex = scrollOffset * columns;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = startIndex + row * columns + col;
                if (index >= filteredItems.size()) break;

                int x = gridLeft + col * slotSize;
                int y = gridTop + row * slotSize;

                ItemStack stack = filteredItems.get(index);

                // Slot background
                boolean hovered = mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize;
                graphics.fill(x, y, x + slotSize - 1, y + slotSize - 1, hovered ? 0x66FFFFFF : 0x22FFFFFF);

                // Render item
                graphics.renderItem(stack, x + 2, y + 2);
            }
        }

        // Item count
        String countText = filteredItems.size() + " items";
        graphics.drawString(this.font, countText, panelLeft + 50, panelBottom - 16, 0xFF888888, false);

        // Tooltip for hovered item
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = startIndex + row * columns + col;
                if (index >= filteredItems.size()) break;

                int x = gridLeft + col * slotSize;
                int y = gridTop + row * slotSize;

                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                    graphics.renderTooltip(this.font, filteredItems.get(index), mouseX, mouseY);
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int gridTop = panelTop + 72;
        int gridLeft = panelLeft + 10;
        int rows = (panelBottom - gridTop - 25) / slotSize;
        int startIndex = scrollOffset * columns;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = startIndex + row * columns + col;
                if (index >= filteredItems.size()) break;

                int x = gridLeft + col * slotSize;
                int y = gridTop + row * slotSize;

                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                    ItemStack selected = filteredItems.get(index);
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(selected.getItem());
                    if (itemId != null && onItemSelected != null) {
                        onItemSelected.accept(itemId.toString());
                    }
                    this.minecraft.setScreen(parentScreen);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int totalRows = (filteredItems.size() + columns - 1) / columns;
        int gridTop = panelTop + 72;
        int visibleRows = (panelBottom - gridTop - 25) / slotSize;

        if (delta > 0 && scrollOffset > 0) scrollOffset--;
        else if (delta < 0 && scrollOffset < totalRows - visibleRows) scrollOffset++;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
