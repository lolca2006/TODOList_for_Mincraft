package com.todolist.mod.integration.litematica;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.util.*;

/**
 * Imports material lists from Litematica .litematic files.
 * Parses NBT-compressed schematic data to extract block counts.
 */
public class LitematicaImporter {

    // Blocks to exclude from material lists
    private static final Set<String> EXCLUDED_BLOCKS = Set.of(
            "minecraft:air", "minecraft:cave_air", "minecraft:void_air",
            "minecraft:water", "minecraft:lava",
            "minecraft:flowing_water", "minecraft:flowing_lava",
            "minecraft:light", "minecraft:barrier",
            "minecraft:structure_void", "minecraft:budding_amethyst"
    );

    /**
     * Imports a .litematic file and returns a map of block ID -> count.
     */
    public static Map<String, Integer> importFromFile(File file) throws Exception {
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getName());
        }
        if (!file.getName().endsWith(".litematic")) {
            throw new IllegalArgumentException("Not a .litematic file");
        }

        CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
        if (root == null) {
            throw new IllegalArgumentException("Could not read NBT data");
        }

        return parseLitematica(root);
    }

    private static Map<String, Integer> parseLitematica(CompoundTag root) {
        Map<String, Integer> result = new LinkedHashMap<>();

        // Litematica format stores regions under "Regions" tag
        if (!root.contains("Regions", Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("No regions found in schematic");
        }

        CompoundTag regions = root.getCompound("Regions");
        for (String regionName : regions.getAllKeys()) {
            CompoundTag region = regions.getCompound(regionName);
            parseRegion(region, result);
        }

        // Sort by count descending
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(result.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        Map<String, Integer> sortedResult = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            sortedResult.put(entry.getKey(), entry.getValue());
        }
        return sortedResult;
    }

    private static void parseRegion(CompoundTag region, Map<String, Integer> result) {
        // Get block state palette
        if (!region.contains("BlockStatePalette", Tag.TAG_LIST)) return;

        ListTag palette = region.getList("BlockStatePalette", Tag.TAG_COMPOUND);
        if (palette.isEmpty()) return;

        // Build palette index -> block name mapping
        String[] paletteNames = new String[palette.size()];
        for (int i = 0; i < palette.size(); i++) {
            CompoundTag entry = palette.getCompound(i);
            String name = entry.getString("Name");
            paletteNames[i] = name;
        }

        // Get region size to calculate total blocks
        CompoundTag size = region.getCompound("Size");
        int sizeX = Math.abs(size.getInt("x"));
        int sizeY = Math.abs(size.getInt("y"));
        int sizeZ = Math.abs(size.getInt("z"));
        long totalBlocks = (long) sizeX * sizeY * sizeZ;

        if (totalBlocks == 0 || !region.contains("BlockStates", Tag.TAG_LONG_ARRAY)) return;

        long[] blockStates = region.getLongArray("BlockStates");
        int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));

        // Parse packed block states
        long mask = (1L << bits) - 1;
        int blocksPerLong = 64 / bits;

        for (long blockIndex = 0; blockIndex < totalBlocks; blockIndex++) {
            int longIndex = (int) (blockIndex / blocksPerLong);
            int bitOffset = (int) ((blockIndex % blocksPerLong) * bits);

            if (longIndex >= blockStates.length) break;

            int paletteIndex = (int) ((blockStates[longIndex] >>> bitOffset) & mask);

            // Handle spanning across longs
            if (bitOffset + bits > 64 && longIndex + 1 < blockStates.length) {
                int bitsInNext = bitOffset + bits - 64;
                paletteIndex |= (int) ((blockStates[longIndex + 1] & ((1L << bitsInNext) - 1)) << (bits - bitsInNext));
            }

            if (paletteIndex >= 0 && paletteIndex < paletteNames.length) {
                String blockName = paletteNames[paletteIndex];
                if (!EXCLUDED_BLOCKS.contains(blockName)) {
                    result.merge(blockName, 1, Integer::sum);
                }
            }
        }
    }
}
