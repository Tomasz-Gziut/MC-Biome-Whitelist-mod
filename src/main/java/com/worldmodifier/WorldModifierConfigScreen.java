package com.worldmodifier;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-game configuration screen for World Modifier.
 * Uses Cloth Config API for the GUI.
 */
public class WorldModifierConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("World Modifier Configuration"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Check if a world is currently loaded
        boolean worldLoaded = Minecraft.getInstance().level != null;

        // ==================== BIOME FILTERING CATEGORY ====================
        ConfigCategory biomeCategory = builder.getOrCreateCategory(
                Component.literal("Biome Filtering")
        );

        biomeCategory.addEntry(
                entryBuilder.startEnumSelector(
                                Component.literal("Filter Mode"),
                                WorldModifierConfig.FilterMode.class,
                                WorldModifierConfig.MODE.get()
                        )
                        .setDefaultValue(WorldModifierConfig.FilterMode.WHITELIST)
                        .setTooltip(
                                Component.literal("DISABLED - No filtering, all biomes generate"),
                                Component.literal("WHITELIST - Only listed biomes can generate"),
                                Component.literal("BLACKLIST - All biomes except listed ones generate"),
                                Component.literal(""),
                                Component.literal("\u00A7aAffects newly generated chunks only")
                        )
                        .setSaveConsumer(WorldModifierConfig.MODE::set)
                        .build()
        );

        // Convert the config list to a mutable ArrayList for the GUI
        List<String> currentBiomes = new ArrayList<>(WorldModifierConfig.BIOME_LIST.get());

        biomeCategory.addEntry(
                entryBuilder.startStrList(
                                Component.literal("Biome List"),
                                currentBiomes
                        )
                        .setDefaultValue(WorldModifierConfig.DEFAULT_BIOMES)
                        .setTooltip(
                                Component.literal("Biomes for whitelist/blacklist filtering."),
                                Component.literal("Use format: minecraft:biome_name"),
                                Component.literal(""),
                                Component.literal("\u00A7aAffects newly generated chunks only")
                        )
                        .setSaveConsumer(list -> WorldModifierConfig.BIOME_LIST.set(new ArrayList<>(list)))
                        .build()
        );

        // ==================== WORLD SETTINGS CATEGORY ====================
        ConfigCategory worldCategory = builder.getOrCreateCategory(
                Component.literal("World Settings")
        );

        if (worldLoaded) {
            worldCategory.addEntry(
                    entryBuilder.startTextDescription(
                            Component.literal("\u00A7c\u00A7lWorld Settings Locked\u00A7r\n\n" +
                                    "World generation settings cannot be\n" +
                                    "changed while a world is loaded.\n\n" +
                                    "To change these settings:\n" +
                                    "1. Return to main menu\n" +
                                    "2. Open Mods > World Modifier > Config\n" +
                                    "3. Change settings\n" +
                                    "4. Create a new world")
                    ).build()
            );

            worldCategory.addEntry(
                    entryBuilder.startTextDescription(
                            Component.literal("\u00A77Sea Level: \u00A7f" + WorldModifierConfig.getSeaLevel() +
                                    " \u00A7c(locked)")
                    ).build()
            );

            worldCategory.addEntry(
                    entryBuilder.startTextDescription(
                            Component.literal("\u00A77Bedrock Level: \u00A7f" + WorldModifierConfig.getBedrockLevel() +
                                    " \u00A7c(locked)")
                    ).build()
            );

            worldCategory.addEntry(
                    entryBuilder.startTextDescription(
                            Component.literal("\u00A77Max Height: \u00A7f" + WorldModifierConfig.getMaxHeight() +
                                    " \u00A7c(locked)")
                    ).build()
            );
        } else {

            // Use AtomicInteger to track current values for cross-field validation
            AtomicInteger seaLevelValue = new AtomicInteger(WorldModifierConfig.SEA_LEVEL.get());
            AtomicInteger bedrockLevelValue = new AtomicInteger(WorldModifierConfig.BEDROCK_LEVEL.get());
            AtomicInteger maxHeightValue = new AtomicInteger(WorldModifierConfig.MAX_HEIGHT.get());

            worldCategory.addEntry(
                    entryBuilder.startIntField(
                                    Component.literal("Sea Level"),
                                    WorldModifierConfig.SEA_LEVEL.get()
                            )
                            .setDefaultValue(WorldModifierConfig.DEFAULT_SEA_LEVEL)
                            .setMin(-1999)
                            .setMax(1999)
                            .setTooltip(
                                    Component.literal("Sea level (water surface Y coordinate)"),
                                    Component.literal("Vanilla default: 63"),
                                    Component.literal(""),
                                    Component.literal("Must be above Bedrock Level"),
                                    Component.literal("Must be below Max Height")
                            )
                            .setSaveConsumer(value -> {
                                seaLevelValue.set(value);
                                WorldModifierConfig.SEA_LEVEL.set(value);
                            })
                            .setErrorSupplier(value -> {
                                seaLevelValue.set(value);
                                if (value <= bedrockLevelValue.get()) {
                                    return Optional.of(Component.literal("Must be above Bedrock Level (" + bedrockLevelValue.get() + ")"));
                                }
                                if (value >= maxHeightValue.get()) {
                                    return Optional.of(Component.literal("Must be below Max Height (" + maxHeightValue.get() + ")"));
                                }
                                return Optional.empty();
                            })
                            .build()
            );

            worldCategory.addEntry(
                    entryBuilder.startIntField(
                                    Component.literal("Bedrock Level"),
                                    WorldModifierConfig.BEDROCK_LEVEL.get()
                            )
                            .setDefaultValue(WorldModifierConfig.DEFAULT_BEDROCK_LEVEL)
                            .setMin(-2000)
                            .setMax(2000)
                            .setTooltip(
                                    Component.literal("Bottom of the world (Y coordinate)"),
                                    Component.literal("Vanilla default: -64"),
                                    Component.literal("Rounded to nearest multiple of 16"),
                                    Component.literal(""),
                                    Component.literal("Must be below Sea Level"),
                                    Component.literal("Must be below Max Height")
                            )
                            .setSaveConsumer(value -> {
                                bedrockLevelValue.set(value);
                                WorldModifierConfig.BEDROCK_LEVEL.set(value);
                            })
                            .setErrorSupplier(value -> {
                                bedrockLevelValue.set(value);
                                if (value >= seaLevelValue.get()) {
                                    return Optional.of(Component.literal("Must be below Sea Level (" + seaLevelValue.get() + ")"));
                                }
                                if (value >= maxHeightValue.get()) {
                                    return Optional.of(Component.literal("Must be below Max Height (" + maxHeightValue.get() + ")"));
                                }
                                return Optional.empty();
                            })
                            .build()
            );

            worldCategory.addEntry(
                    entryBuilder.startIntField(
                                    Component.literal("Max Height"),
                                    WorldModifierConfig.MAX_HEIGHT.get()
                            )
                            .setDefaultValue(WorldModifierConfig.DEFAULT_MAX_HEIGHT)
                            .setMin(-2000)
                            .setMax(2000)
                            .setTooltip(
                                    Component.literal("Maximum build height (Y coordinate)"),
                                    Component.literal("Vanilla default: 512"),
                                    Component.literal("Rounded to nearest multiple of 16"),
                                    Component.literal(""),
                                    Component.literal("Must be above Sea Level"),
                                    Component.literal("Must be above Bedrock Level")
                            )
                            .setSaveConsumer(value -> {
                                maxHeightValue.set(value);
                                WorldModifierConfig.MAX_HEIGHT.set(value);
                            })
                            .setErrorSupplier(value -> {
                                maxHeightValue.set(value);
                                if (value <= seaLevelValue.get()) {
                                    return Optional.of(Component.literal("Must be above Sea Level (" + seaLevelValue.get() + ")"));
                                }
                                if (value <= bedrockLevelValue.get()) {
                                    return Optional.of(Component.literal("Must be above Bedrock Level (" + bedrockLevelValue.get() + ")"));
                                }
                                return Optional.empty();
                            })
                            .build()
            );
        }

        // Save callback to rebuild cache after saving
        builder.setSavingRunnable(() -> {
            WorldModifierConfig.rebuildCache();
            // Re-open the config screen to show updated values
            Minecraft.getInstance().setScreen(create(parent));
        });

        return builder.build();
    }
}
