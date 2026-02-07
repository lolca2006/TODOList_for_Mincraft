package com.todolist.mod.common.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockGroup {
    private final String name;
    private final String tagId;

    public BlockGroup(String name, String tagId) {
        this.name = name;
        this.tagId = tagId;
    }

    public String getName() { return name; }
    public String getTagId() { return tagId; }

    public static Map<String, BlockGroup> getDefaults() {
        Map<String, BlockGroup> groups = new LinkedHashMap<>();
        groups.put("logs", new BlockGroup("Logs", "minecraft:logs"));
        groups.put("planks", new BlockGroup("Planks", "minecraft:planks"));
        groups.put("stone", new BlockGroup("Stone", "minecraft:stone_bricks"));
        groups.put("ores", new BlockGroup("Ores", "minecraft:coal_ores"));
        groups.put("wool", new BlockGroup("Wool", "minecraft:wool"));
        groups.put("sand", new BlockGroup("Sand", "minecraft:sand"));
        groups.put("glass", new BlockGroup("Glass", "minecraft:impermeable"));
        groups.put("slabs", new BlockGroup("Slabs", "minecraft:slabs"));
        groups.put("stairs", new BlockGroup("Stairs", "minecraft:stairs"));
        groups.put("fences", new BlockGroup("Fences", "minecraft:fences"));
        groups.put("doors", new BlockGroup("Doors", "minecraft:doors"));
        groups.put("flowers", new BlockGroup("Flowers", "minecraft:flowers"));
        groups.put("crops", new BlockGroup("Crops", "minecraft:crops"));
        return groups;
    }
}
