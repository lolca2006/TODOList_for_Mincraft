package com.todolist.mod.client.gui.widgets;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SearchBarWidget extends EditBox {
    private final Consumer<String> onSearch;

    public SearchBarWidget(Font font, int x, int y, int width, int height, Consumer<String> onSearch) {
        super(font, x, y, width, height, Component.literal("Search"));
        this.onSearch = onSearch;
        this.setHint(Component.literal("Search todos..."));
        this.setMaxLength(100);
        this.setResponder(text -> {
            if (onSearch != null) onSearch.accept(text);
        });
    }
}
