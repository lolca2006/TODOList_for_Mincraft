package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.common.model.ResourceRequirement;
import com.todolist.mod.integration.litematica.LitematicaImporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportScreen extends Screen {
    private final Screen parentScreen;

    private int panelLeft, panelTop, panelRight, panelBottom;
    private static final int PANEL_W = 300, PANEL_H = 240;

    private List<File> litematicFiles = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    // Preview after parsing
    private List<ResourceRequirement> previewResources = null;
    private String previewName = null;
    private int previewScroll = 0;
    private String statusMessage = null;

    public ImportScreen(Screen parent) {
        super(Component.literal("Import Resources"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        panelLeft = (this.width - PANEL_W) / 2;
        panelTop = (this.height - PANEL_H) / 2;
        panelRight = panelLeft + PANEL_W;
        panelBottom = panelTop + PANEL_H;

        scanForFiles();

        // Import as new Todo button
        this.addRenderableWidget(Button.builder(Component.literal("Import as Todo"), btn -> {
            if (previewResources != null && !previewResources.isEmpty()) {
                String todoText = previewName != null ? "Build: " + previewName : "Imported Build";
                ClientTodoManager.addTodo(todoText, "General",
                        com.todolist.mod.common.model.TodoVisibility.PRIVATE, new ArrayList<>(previewResources));
                statusMessage = "Imported " + previewResources.size() + " resources!";
                previewResources = null;
                previewName = null;
            }
        }).bounds(panelLeft + 10, panelBottom - 28, 90, 18).build());

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(panelRight - 55, panelBottom - 28, 45, 18).build());
    }

    private void scanForFiles() {
        litematicFiles.clear();
        Minecraft mc = Minecraft.getInstance();
        File gameDir = mc.gameDirectory;

        // Look in schematics folder (Litematica default)
        File schematicsDir = new File(gameDir, "schematics");
        if (schematicsDir.exists() && schematicsDir.isDirectory()) {
            scanDirectory(schematicsDir);
        }

        // Also check direct .litematic files in game dir
        File[] rootFiles = gameDir.listFiles((dir, name) -> name.endsWith(".litematic"));
        if (rootFiles != null) {
            for (File f : rootFiles) litematicFiles.add(f);
        }
    }

    private void scanDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f);
            } else if (f.getName().endsWith(".litematic")) {
                litematicFiles.add(f);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Panel
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 4, 0xFFFFAA00);

        if (previewResources != null) {
            renderPreview(graphics, mouseX, mouseY);
        } else {
            renderFileList(graphics, mouseX, mouseY);
        }

        // Status message
        if (statusMessage != null) {
            graphics.drawCenteredString(this.font, statusMessage, this.width / 2, panelBottom - 42, 0xFF44DD44);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderFileList(GuiGraphics graphics, int mouseX, int mouseY) {
        int y = panelTop + 22;
        graphics.drawString(this.font, "Litematica Files (.litematic):", panelLeft + 10, y, 0xFFAAAAAA, false);
        y += 14;

        if (litematicFiles.isEmpty()) {
            graphics.drawString(this.font, "No .litematic files found", panelLeft + 10, y + 10, 0xFF666666, false);
            graphics.drawString(this.font, "Place files in: .minecraft/schematics/", panelLeft + 10, y + 24, 0xFF555555, false);
            return;
        }

        int maxVisible = 10;
        for (int i = scrollOffset; i < Math.min(litematicFiles.size(), scrollOffset + maxVisible); i++) {
            File file = litematicFiles.get(i);
            boolean hovered = mouseX >= panelLeft + 10 && mouseX <= panelRight - 10 && mouseY >= y && mouseY < y + 14;
            boolean selected = i == selectedIndex;

            if (selected) {
                graphics.fill(panelLeft + 10, y, panelRight - 10, y + 14, 0x44FFAA00);
            } else if (hovered) {
                graphics.fill(panelLeft + 10, y, panelRight - 10, y + 14, 0x22FFFFFF);
            }

            String name = file.getName();
            if (name.length() > 36) name = name.substring(0, 34) + "..";
            int color = selected ? 0xFFFFAA00 : (hovered ? 0xFFFFFFFF : 0xFFCCCCCC);
            graphics.drawString(this.font, name, panelLeft + 14, y + 3, color, false);

            // File size
            String size = formatFileSize(file.length());
            graphics.drawString(this.font, size, panelRight - this.font.width(size) - 14, y + 3, 0xFF888888, false);

            y += 16;
        }

        if (litematicFiles.size() > maxVisible) {
            graphics.drawString(this.font, "Scroll for more (" + litematicFiles.size() + " files)",
                    panelLeft + 10, y + 4, 0xFF666666, false);
        }
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        int y = panelTop + 22;
        graphics.drawString(this.font, "Preview: " + (previewName != null ? previewName : "Import"),
                panelLeft + 10, y, 0xFFFFAA00, false);
        y += 14;

        graphics.drawString(this.font, previewResources.size() + " unique resources",
                panelLeft + 10, y, 0xFF88FF88, false);

        // Back to file list
        int backX = panelRight - 60;
        boolean backHovered = mouseX >= backX && mouseX <= backX + 50 && mouseY >= y && mouseY <= y + 12;
        graphics.drawString(this.font, "[< Back]", backX, y, backHovered ? 0xFFFFFFFF : 0xFF888888, false);
        y += 16;

        // Resource list
        int maxVisible = 8;
        for (int i = previewScroll; i < Math.min(previewResources.size(), previewScroll + maxVisible); i++) {
            ResourceRequirement req = previewResources.get(i);

            try {
                var regItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        net.minecraft.resources.ResourceLocation.parse(req.getItemId()));
                if (regItem != null) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(regItem);

                    graphics.pose().pushPose();
                    graphics.pose().scale(0.75f, 0.75f, 1.0f);
                    graphics.renderItem(stack, (int)((panelLeft + 12) / 0.75f), (int)(y / 0.75f));
                    graphics.pose().popPose();

                    String name = stack.getHoverName().getString();
                    if (name.length() > 24) name = name.substring(0, 22) + "..";
                    graphics.drawString(this.font, name, panelLeft + 28, y + 4, 0xFFDDDDDD, false);

                    String count = "x" + req.getCount();
                    graphics.drawString(this.font, count, panelRight - this.font.width(count) - 14, y + 4, 0xFFFFFF00, false);
                }
            } catch (Exception ignored) {}

            y += 16;
        }

        if (previewResources.size() > maxVisible) {
            graphics.drawString(this.font, "+" + (previewResources.size() - maxVisible) + " more (scroll)",
                    panelLeft + 28, y + 2, 0xFF888888, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (previewResources != null) {
            // Back button in preview
            int backX = panelRight - 60;
            int backY = panelTop + 36;
            if (mouseX >= backX && mouseX <= backX + 50 && mouseY >= backY && mouseY <= backY + 12) {
                previewResources = null;
                previewName = null;
                previewScroll = 0;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // File list clicks
        int y = panelTop + 36;
        int maxVisible = 10;
        for (int i = scrollOffset; i < Math.min(litematicFiles.size(), scrollOffset + maxVisible); i++) {
            if (mouseX >= panelLeft + 10 && mouseX <= panelRight - 10 && mouseY >= y && mouseY < y + 14) {
                selectedIndex = i;
                // Double-click or just select and parse
                File file = litematicFiles.get(i);
                try {
                    Map<String, Integer> materials = LitematicaImporter.importFromFile(file);
                    previewResources = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                        previewResources.add(new ResourceRequirement(entry.getKey(), entry.getValue()));
                    }
                    previewName = file.getName().replace(".litematic", "");
                    previewScroll = 0;
                    statusMessage = null;
                } catch (Exception e) {
                    statusMessage = "Error: " + e.getMessage();
                }
                return true;
            }
            y += 16;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (previewResources != null) {
            if (scrollDeltaY > 0 && previewScroll > 0) previewScroll--;
            else if (scrollDeltaY < 0 && previewScroll < previewResources.size() - 8) previewScroll++;
        } else {
            if (scrollDeltaY > 0 && scrollOffset > 0) scrollOffset--;
            else if (scrollDeltaY < 0 && scrollOffset < litematicFiles.size() - 10) scrollOffset++;
        }
        return true;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return (bytes / 1024) + " KB";
        return String.format("%.1f MB", bytes / 1048576.0);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
