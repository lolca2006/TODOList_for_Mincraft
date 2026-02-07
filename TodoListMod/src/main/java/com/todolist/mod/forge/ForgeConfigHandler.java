package com.todolist.mod.forge;

import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigHandler {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new ClientConfig(builder);
        CLIENT_SPEC = builder.build();
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.IntValue hudX;
        public final ForgeConfigSpec.IntValue hudY;
        public final ForgeConfigSpec.DoubleValue hudOpacity;
        public final ForgeConfigSpec.DoubleValue hudScale;
        public final ForgeConfigSpec.BooleanValue hudVisible;
        public final ForgeConfigSpec.IntValue hudMaxItems;
        public final ForgeConfigSpec.BooleanValue showCompletedInHud;
        public final ForgeConfigSpec.BooleanValue playCompletionSound;
        public final ForgeConfigSpec.ConfigValue<String> hudActiveCategory;

        ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("HUD Overlay Settings").push("hud");

            hudX = builder
                    .comment("X position of the HUD overlay")
                    .defineInRange("x", 10, 0, 10000);
            hudY = builder
                    .comment("Y position of the HUD overlay")
                    .defineInRange("y", 10, 0, 10000);
            hudOpacity = builder
                    .comment("Opacity of the HUD overlay (0.0 = transparent, 1.0 = opaque)")
                    .defineInRange("opacity", 0.7, 0.0, 1.0);
            hudScale = builder
                    .comment("Scale of the HUD overlay")
                    .defineInRange("scale", 1.0, 0.5, 3.0);
            hudVisible = builder
                    .comment("Whether the HUD overlay is visible")
                    .define("visible", true);
            hudMaxItems = builder
                    .comment("Maximum number of items to show in the HUD overlay")
                    .defineInRange("maxItems", 8, 1, 20);
            showCompletedInHud = builder
                    .comment("Show completed items in the HUD overlay")
                    .define("showCompleted", false);

            builder.pop();

            builder.comment("General Settings").push("general");

            playCompletionSound = builder
                    .comment("Play a sound when completing a task")
                    .define("completionSound", true);
            hudActiveCategory = builder
                    .comment("Active category filter in HUD (empty = all)")
                    .define("activeCategory", "");

            builder.pop();
        }
    }
}
