package com.worldmodifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration for World Modifier.
 *
 * Contract:
 * - biomeList: List of biome resource locations (e.g., "minecraft:plains")
 * - mode: Controls filtering behavior (whitelist, blacklist, or disabled)
 * - preset: Predefined configurations (default, endless_ocean, or custom)
 * - Non-allowed biomes are replaced with a fallback biome from the allowed set
 *
 * Invariant: If biome list is empty and mode is not disabled, ALL biomes are allowed (no filtering).
 */
public class WorldModifierConfig {

    // ==================== ENUMS ====================

    public enum FilterMode {
        DISABLED,
        WHITELIST,
        BLACKLIST
    }

    // ==================== DEFAULT CONSTANTS (Vanilla Minecraft) ====================

    public static final int DEFAULT_SEA_LEVEL = 63;
    public static final int DEFAULT_BEDROCK_LEVEL = -64;
    public static final int DEFAULT_MAX_HEIGHT = 512;
    public static final List<String> DEFAULT_BIOMES = List.of();

    // ==================== CONFIG SPEC ====================

    public static final ForgeConfigSpec SPEC;

    // Mode
    public static final ForgeConfigSpec.EnumValue<FilterMode> MODE;

    // Biome Settings
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BIOME_LIST;

    // World Settings
    public static final ForgeConfigSpec.IntValue SEA_LEVEL;
    public static final ForgeConfigSpec.IntValue BEDROCK_LEVEL;
    public static final ForgeConfigSpec.IntValue MAX_HEIGHT;

    // ==================== RUNTIME CACHE ====================

    private static Set<ResourceLocation> biomeCache = Collections.emptySet();
    private static List<ResourceLocation> biomeListCache = Collections.emptyList();

    // ==================== CONFIG INITIALIZATION ====================

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // -------------------- Biome Filtering Section --------------------
        builder.comment(
                "===========================================",
                "         WORLD MODIFIER CONFIGURATION      ",
                "===========================================",
                "",
                "===========================================",
                "            BIOME FILTERING                ",
                "===========================================",
                "",
                "Control which biomes can generate in your world."
        );
        builder.push("biomes");

        MODE = builder
                .comment(
                        "Filtering mode:",
                        "",
                        "  DISABLED  - No filtering, all biomes generate normally",
                        "  WHITELIST - ONLY biomes in the list below can generate",
                        "  BLACKLIST - All biomes EXCEPT those in the list can generate"
                )
                .defineEnum("mode", FilterMode.DISABLED);

        BIOME_LIST = builder
                .comment(
                        "",
                        "List of biomes for filtering (used by WHITELIST and BLACKLIST modes).",
                        "Use full resource locations like 'minecraft:plains' or 'modid:custom_biome'.",
                        "If empty, no filtering occurs (all biomes allowed).",
                        "",
                        "Common vanilla biomes:",
                        "  Plains:    minecraft:plains, minecraft:sunflower_plains",
                        "  Forest:    minecraft:forest, minecraft:birch_forest, minecraft:dark_forest",
                        "  Desert:    minecraft:desert",
                        "  Taiga:     minecraft:taiga, minecraft:old_growth_pine_taiga",
                        "  Jungle:    minecraft:jungle, minecraft:sparse_jungle, minecraft:bamboo_jungle",
                        "  Swamp:     minecraft:swamp, minecraft:mangrove_swamp",
                        "  Ocean:     minecraft:ocean, minecraft:deep_ocean, minecraft:warm_ocean",
                        "  Mountain:  minecraft:meadow, minecraft:grove, minecraft:jagged_peaks",
                        "  Savanna:   minecraft:savanna, minecraft:savanna_plateau",
                        "  Badlands:  minecraft:badlands, minecraft:eroded_badlands",
                        "  Snowy:     minecraft:snowy_plains, minecraft:ice_spikes",
                        "  Special:   minecraft:cherry_grove, minecraft:deep_dark, minecraft:mushroom_fields",
                        "",
                        "Example - Plains only: [\"minecraft:plains\", \"minecraft:river\", \"minecraft:ocean\"]"
                )
                .defineListAllowEmpty(
                        List.of("list"),
                        () -> List.of(
                                "minecraft:ocean",
                                "minecraft:deep_ocean",
                                "minecraft:warm_ocean",
                                "minecraft:lukewarm_ocean",
                                "minecraft:deep_lukewarm_ocean",
                                "minecraft:cold_ocean",
                                "minecraft:deep_cold_ocean",
                                "minecraft:frozen_ocean",
                                "minecraft:deep_frozen_ocean"
                        ),
                        obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null
                );

        builder.pop();

        // -------------------- World Generation Section --------------------
        builder.comment(
                "",
                "===========================================",
                "           WORLD GENERATION               ",
                "===========================================",
                "",
                "Customize world height and water levels."
        );
        builder.push("world");

        SEA_LEVEL = builder
                .comment(
                        "Sea level (Y coordinate where water surface generates).",
                        "",
                        "  Vanilla default: 63",
                        "  Higher values = more water coverage",
                        "  Lower values = less water coverage",
                        "",
                        "Range: -1999 to 1999"
                )
                .defineInRange("seaLevel", DEFAULT_SEA_LEVEL, -1999, 1999);

        BEDROCK_LEVEL = builder
                .comment(
                        "",
                        "Bedrock level (bottom of the world).",
                        "",
                        "  Vanilla default: -64",
                        "  Higher values = shallower world",
                        "  Lower values = deeper world",
                        "",
                        "Note: Rounded down to nearest multiple of 16 (Minecraft requirement).",
                        "Range: -2000 to 2000"
                )
                .defineInRange("bedrockLevel", DEFAULT_BEDROCK_LEVEL, -2000, 2000);

        MAX_HEIGHT = builder
                .comment(
                        "",
                        "Maximum build height (top of the world).",
                        "",
                        "  Vanilla default: 512",
                        "  Higher values = taller build limit",
                        "  Lower values = lower sky",
                        "",
                        "Note: Rounded up to nearest multiple of 16 (Minecraft requirement).",
                        "Range: -2000 to 2000"
                )
                .defineInRange("maxHeight", DEFAULT_MAX_HEIGHT, -2000, 2000);

        builder.pop();

        SPEC = builder.build();
    }

    // ==================== CACHE MANAGEMENT ====================

    /**
     * Rebuilds the biome cache from config values.
     * Called after config load/reload.
     */
    public static void rebuildCache() {
        List<String> biomeStrings = new ArrayList<>(BIOME_LIST.get());

        Set<ResourceLocation> newCache = new HashSet<>();
        List<ResourceLocation> newList = new ArrayList<>();

        for (String biome : biomeStrings) {
            ResourceLocation loc = ResourceLocation.tryParse(biome);
            if (loc != null) {
                newCache.add(loc);
                newList.add(loc);
            } else {
                WorldModifier.LOGGER.warn("[WorldModifierConfig] Invalid biome: {}", biome);
            }
        }

        biomeCache = Collections.unmodifiableSet(newCache);
        biomeListCache = Collections.unmodifiableList(newList);

        WorldModifier.LOGGER.info(
                "[WorldModifierConfig] Loaded - Mode: {}, Biomes: {}, Sea: {}, Bedrock: {}, MaxHeight: {}",
                getMode(), biomeCache.size(), getSeaLevel(), getBedrockLevel(), getMaxHeight()
        );
    }

    // ==================== MODE & STATE QUERIES ====================

    /**
     * @return the current filter mode
     */
    public static FilterMode getMode() {
        return MODE.get();
    }

    /**
     * @return true if biome filtering is active
     */
    public static boolean isFilteringActive() {
        FilterMode mode = getMode();
        if (mode == FilterMode.DISABLED) {
            return false;
        }
        return !biomeCache.isEmpty();
    }

    /**
     * @return true if world modifications are active (any setting differs from vanilla defaults)
     */
    public static boolean isWorldModificationActive() {
        return getSeaLevel() != DEFAULT_SEA_LEVEL ||
               getBedrockLevel() != DEFAULT_BEDROCK_LEVEL ||
               getMaxHeight() != DEFAULT_MAX_HEIGHT;
    }

    // ==================== BIOME QUERIES ====================

    /**
     * @return unmodifiable set of biomes in the filter list
     */
    public static Set<ResourceLocation> getBiomeList() {
        return biomeCache;
    }

    /**
     * @param biome the biome to check
     * @return true if biome is allowed to generate based on current mode
     */
    public static boolean isBiomeAllowed(ResourceLocation biome) {
        FilterMode mode = getMode();

        if (mode == FilterMode.DISABLED || biomeCache.isEmpty()) {
            return true;
        }

        boolean inList = biomeCache.contains(biome);

        if (mode == FilterMode.WHITELIST) {
            return inList;
        } else {
            return !inList;
        }
    }

    /**
     * @return fallback biome when original is not allowed
     */
    public static ResourceLocation getFallbackBiome() {
        if (biomeListCache.isEmpty()) {
            return new ResourceLocation("minecraft", "plains");
        }
        return biomeListCache.get(0);
    }

    // ==================== WORLD SETTING QUERIES ====================

    /**
     * @return the configured sea level
     */
    public static int getSeaLevel() {
        return SEA_LEVEL.get();
    }

    /**
     * @return the configured bedrock level
     */
    public static int getBedrockLevel() {
        return BEDROCK_LEVEL.get();
    }

    /**
     * @return the configured max height
     */
    public static int getMaxHeight() {
        return MAX_HEIGHT.get();
    }
}
