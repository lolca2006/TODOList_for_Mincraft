package com.todolist.mod.client.gui;

import com.todolist.mod.client.ClientTodoManager;
import com.todolist.mod.client.gui.widgets.CategoryTabBar;
import com.todolist.mod.client.gui.widgets.SearchBarWidget;
import com.todolist.mod.client.gui.widgets.TodoListWidget;
import com.todolist.mod.common.model.TodoVisibility;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TodoScreen extends Screen {
    private TodoListWidget todoListWidget;
    private CategoryTabBar categoryTabBar;
    private SearchBarWidget searchBar;
    private EditBox newTaskInput;
    private Button addButton;
    private Button shareButton;
    private Button categoryButton;
    private Button settingsButton;

    private TodoVisibility currentVisibility = TodoVisibility.PRIVATE;
    private String currentCategory = "General";

    // Panel dimensions
    private int panelLeft, panelTop, panelRight, panelBottom;
    private int panelWidth = 310;
    private int panelHeight = 250;

    public TodoScreen() {
        super(Component.translatable("gui.todolistmod.title"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        panelLeft = centerX - panelWidth / 2;
        panelTop = centerY - panelHeight / 2;
        panelRight = panelLeft + panelWidth;
        panelBottom = panelTop + panelHeight;

        // Category tab bar at the top
        categoryTabBar = new CategoryTabBar(panelLeft + 5, panelTop + 18, panelWidth - 80, 16,
                category -> {
                    if (todoListWidget != null) todoListWidget.setCategoryFilter(category);
                    if (category != null) currentCategory = category;
                });
        this.addRenderableWidget(categoryTabBar);

        // Search bar
        searchBar = new SearchBarWidget(this.font, panelLeft + 5, panelTop + 38, panelWidth / 2 - 5, 16,
                text -> { if (todoListWidget != null) todoListWidget.setSearchFilter(text); });
        this.addRenderableWidget(searchBar);

        // Category manager button
        categoryButton = Button.builder(Component.literal("\u2630"), btn -> {
            this.minecraft.setScreen(new CategoryManagerScreen(this));
        }).bounds(panelRight - 72, panelTop + 18, 16, 16).build();
        this.addRenderableWidget(categoryButton);

        // Settings button
        settingsButton = Button.builder(Component.literal("\u2699"), btn -> {
            this.minecraft.setScreen(new SettingsScreen(this));
        }).bounds(panelRight - 52, panelTop + 18, 16, 16).build();
        this.addRenderableWidget(settingsButton);

        // Import button (Litematica etc.)
        Button importButton = Button.builder(Component.literal("\u2B07"), btn -> {
            this.minecraft.setScreen(new ImportScreen(this));
        }).bounds(panelRight - 32, panelTop + 18, 16, 16).build();
        this.addRenderableWidget(importButton);

        // Todo list widget (scrollable)
        int listTop = panelTop + 58;
        int listBottom = panelBottom - 52;
        todoListWidget = new TodoListWidget(this.minecraft, panelWidth - 10, listBottom - listTop,
                listTop, 24);
        todoListWidget.setX(panelLeft + 5);
        this.addWidget(todoListWidget);

        // New task input
        newTaskInput = new EditBox(this.font, panelLeft + 5, panelBottom - 44, panelWidth - 100, 18,
                Component.literal("New task"));
        newTaskInput.setMaxLength(200);
        newTaskInput.setHint(Component.literal("Enter a new task..."));
        this.addRenderableWidget(newTaskInput);

        // Visibility toggle button
        shareButton = Button.builder(Component.literal(getVisibilityLabel()), btn -> {
            currentVisibility = currentVisibility == TodoVisibility.PRIVATE ?
                    TodoVisibility.SHARED : TodoVisibility.PRIVATE;
            btn.setMessage(Component.literal(getVisibilityLabel()));
        }).bounds(panelLeft + panelWidth - 90, panelBottom - 44, 40, 18).build();
        this.addRenderableWidget(shareButton);

        // Add button
        addButton = Button.builder(Component.literal("+"), btn -> addNewTask())
                .bounds(panelRight - 45, panelBottom - 44, 40, 18).build();
        this.addRenderableWidget(addButton);

        refreshList();
    }

    private void addNewTask() {
        String text = newTaskInput.getValue().trim();
        if (!text.isEmpty()) {
            ClientTodoManager.addTodo(text, currentCategory, currentVisibility, new java.util.ArrayList<>());
            newTaskInput.setValue("");
            // List will update on next sync from server
        }
    }

    private String getVisibilityLabel() {
        return currentVisibility == TodoVisibility.PRIVATE ? "\uD83D\uDD12" : "\uD83D\uDD13";
    }

    public void refreshList() {
        if (todoListWidget != null) {
            todoListWidget.refreshItems();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Panel background with gradient
        graphics.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2, 0xFF333333);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDD111111);

        // Title bar
        graphics.fill(panelLeft, panelTop, panelRight, panelTop + 16, 0xFF1A1A2E);
        graphics.drawString(this.font, this.title, panelLeft + 8, panelTop + 4, 0xFFFFAA00, false);

        // Stats
        long completed = ClientTodoManager.getTodoList().getCompletedCount();
        int total = ClientTodoManager.getTodoList().size();
        String stats = completed + "/" + total;
        graphics.drawString(this.font, stats, panelRight - this.font.width(stats) - 8, panelTop + 4,
                0xFF88FF88, false);

        // Section dividers
        graphics.fill(panelLeft + 4, panelTop + 55, panelRight - 4, panelTop + 56, 0xFF333333);
        graphics.fill(panelLeft + 4, panelBottom - 50, panelRight - 4, panelBottom - 49, 0xFF333333);

        // Footer info
        String footerText = currentVisibility == TodoVisibility.SHARED ? "Shared Todo" : "Private Todo";
        graphics.drawString(this.font, footerText, panelLeft + 8, panelBottom - 22,
                currentVisibility == TodoVisibility.SHARED ? 0xFFFFCC00 : 0xFF888888, false);

        // Render the list
        if (todoListWidget != null) {
            todoListWidget.render(graphics, mouseX, mouseY, partialTick);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (newTaskInput.isFocused() && keyCode == 257) { // Enter
            addNewTask();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        refreshList();
    }
}
