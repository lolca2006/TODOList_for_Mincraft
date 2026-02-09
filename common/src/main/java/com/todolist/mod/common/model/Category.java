package com.todolist.mod.common.model;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private int color;
    private int sortOrder;

    public Category() {}

    public Category(String name, int color, int sortOrder) {
        this.name = name;
        this.color = color;
        this.sortOrder = sortOrder;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public static List<Category> defaults() {
        List<Category> list = new ArrayList<>();
        list.add(new Category("General", 0xFFFFFFFF, 0));
        list.add(new Category("Building", 0xFF55FF55, 1));
        list.add(new Category("Mining", 0xFFFFAA00, 2));
        list.add(new Category("Redstone", 0xFFFF5555, 3));
        list.add(new Category("Farming", 0xFF55FFFF, 4));
        list.add(new Category("Exploration", 0xFFAA55FF, 5));
        return list;
    }
}
