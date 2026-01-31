package com.biomewhitelist;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Configuration for biome whitelist.
 *
 * Contract:
 * - whitelistedBiomes: List of biome resource locations (e.g., "minecraft:plains")
 * - fallbackBiome: Biome to use when a non-whitelisted biome would generate
 * - enabled: Master toggle for the mod functionality
 *
 * Invariant: If whitelist is empty and enabled=true, ALL biomes are allowed (no filtering).
 */
public class BiomeWhitelistConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_BIOMES;
    public static final ForgeConfigSpec.ConfigValue<String> FALLBACK_BIOME;
    public static final ForgeConfigSpec.BooleanValue FORCE_FLAT_OCEAN;
    public static final ForgeConfigSpec.IntValue OCEAN_FLOOR_LEVEL;

    // Cached set for fast lookup - rebuilt when config reloads
    private static Set<ResourceLocation> whitelistCache = Collections.emptySet();
    private static Set<ResourceLocation> oceanBiomesCache = Collections.emptySet();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Biome Whitelist Configuration");
        builder.push("general");

        ENABLED = builder
                .comment("Enable biome whitelist filtering. When false, world generates normally.")
                .define("enabled", true);

        WHITELISTED_BIOMES = builder
                .comment(
                        "List of biomes that are allowed to generate.",
                        "Use full resource locations like 'minecraft:plains' or 'modid:custom_biome'.",
                        "If empty, all biomes are allowed (whitelist disabled).",
                        "",
                        "Common vanilla biomes:",
                        "  minecraft:plains, minecraft:forest, minecraft:desert,",
                        "  minecraft:taiga, minecraft:swamp, minecraft:jungle,",
                        "  minecraft:snowy_plains, minecraft:ocean, minecraft:river,",
                        "  minecraft:beach, minecraft:mountains (now minecraft:stony_peaks),",
                        "  minecraft:savanna, minecraft:badlands, minecraft:dark_forest,",
                        "  minecraft:birch_forest, minecraft:flower_forest,",
                        "  minecraft:meadow, minecraft:grove, minecraft:snowy_slopes,",
                        "  minecraft:frozen_peaks, minecraft:jagged_peaks,",
                        "  minecraft:cherry_grove, minecraft:deep_dark",
                        "",
                        "Example for a plains-only world: [\"minecraft:plains\", \"minecraft:river\", \"minecraft:ocean\"]"
                )
                .defineListAllowEmpty(
                        List.of("whitelistedBiomes"),
                        () -> List.of("minecraft:plains"),
                        obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null
                );

        FALLBACK_BIOME = builder
                .comment(
                        "Fallback biome when a non-whitelisted biome would generate.",
                        "Must be a biome from the whitelist. If invalid, uses first whitelisted biome."
                )
                .define("fallbackBiome", "minecraft:plains");

        builder.pop();

        builder.comment("Experimental Features (Beta)");
        builder.push("beta");

        FORCE_FLAT_OCEAN = builder
                .comment(
                        "[BETA] Force ocean biomes to generate as flat terrain.",
                        "When enabled, ocean-type biomes will generate as flat ocean floor",
                        "without islands, hills, or terrain features that would normally",
                        "appear due to the underlying terrain noise.",
                        "This affects: ocean, deep_ocean, warm_ocean, lukewarm_ocean,",
                        "cold_ocean, frozen_ocean, and their deep variants."
                )
                .define("forceFlatOcean", false);

        OCEAN_FLOOR_LEVEL = builder
                .comment(
                        "[BETA] Y-level for the ocean floor when forceFlatOcean is enabled.",
                        "Default is 48, which creates a reasonable ocean depth.",
                        "Sea level is at Y=63."
                )
                .defineInRange("oceanFloorLevel", 48, -64, 62);

        builder.pop();
        SPEC = builder.build();
    }

    // All vanilla ocean biome IDs
    private static final Set<ResourceLocation> VANILLA_OCEAN_BIOMES = Set.of(
            new ResourceLocation("minecraft", "ocean"),
            new ResourceLocation("minecraft", "deep_ocean"),
            new ResourceLocation("minecraft", "warm_ocean"),
            new ResourceLocation("minecraft", "lukewarm_ocean"),
            new ResourceLocation("minecraft", "deep_lukewarm_ocean"),
            new ResourceLocation("minecraft", "cold_ocean"),
            new ResourceLocation("minecraft", "deep_cold_ocean"),
            new ResourceLocation("minecraft", "frozen_ocean"),
            new ResourceLocation("minecraft", "deep_frozen_ocean")
    );

    /**
     * Rebuilds the whitelist cache from config values.
     * Called after config load/reload.
     */
    public static void rebuildCache() {
        Set<ResourceLocation> newCache = new HashSet<>();
        Set<ResourceLocation> newOceanCache = new HashSet<>();
        for (String biome : WHITELISTED_BIOMES.get()) {
            ResourceLocation loc = ResourceLocation.tryParse(biome);
            if (loc != null) {
                newCache.add(loc);
                // Check if this is an ocean biome
                if (VANILLA_OCEAN_BIOMES.contains(loc) || loc.getPath().contains("ocean")) {
                    newOceanCache.add(loc);
                }
            } else {
                BiomeWhitelist.LOGGER.warn("[BiomeWhitelistConfig.rebuildCache]: Invalid biome resource location: {}", biome);
            }
        }
        whitelistCache = Collections.unmodifiableSet(newCache);
        oceanBiomesCache = Collections.unmodifiableSet(newOceanCache);
        BiomeWhitelist.LOGGER.info("[BiomeWhitelistConfig.rebuildCache]: Whitelist contains {} biomes ({} ocean biomes)",
                whitelistCache.size(), oceanBiomesCache.size());
    }

    /**
     * @return true if the mod filtering is enabled and whitelist is non-empty
     */
    public static boolean isFilteringActive() {
        return ENABLED.get() && !whitelistCache.isEmpty();
    }

    /**
     * @return unmodifiable set of whitelisted biome resource locations
     */
    public static Set<ResourceLocation> getWhitelistedBiomes() {
        return whitelistCache;
    }

    /**
     * @param biome the biome to check
     * @return true if biome is in whitelist (or whitelist is empty/disabled)
     */
    public static boolean isBiomeAllowed(ResourceLocation biome) {
        if (!isFilteringActive()) {
            return true;
        }
        return whitelistCache.contains(biome);
    }

    /**
     * @return the configured fallback biome, or first whitelisted biome if invalid
     */
    public static ResourceLocation getFallbackBiome() {
        ResourceLocation fallback = ResourceLocation.tryParse(FALLBACK_BIOME.get());
        if (fallback != null && whitelistCache.contains(fallback)) {
            return fallback;
        }
        // Return first whitelisted biome as fallback
        return whitelistCache.isEmpty() ? new ResourceLocation("minecraft", "plains") : whitelistCache.iterator().next();
    }

    /**
     * @return true if flat ocean mode is enabled
     */
    public static boolean isForceFlatOceanEnabled() {
        return FORCE_FLAT_OCEAN.get();
    }

    /**
     * @return the configured ocean floor Y-level
     */
    public static int getOceanFloorLevel() {
        return OCEAN_FLOOR_LEVEL.get();
    }

    /**
     * @param biome the biome to check
     * @return true if the biome is an ocean-type biome
     */
    public static boolean isOceanBiome(ResourceLocation biome) {
        return oceanBiomesCache.contains(biome) || VANILLA_OCEAN_BIOMES.contains(biome);
    }

    /**
     * @return unmodifiable set of ocean biome resource locations from the whitelist
     */
    public static Set<ResourceLocation> getOceanBiomes() {
        return oceanBiomesCache;
    }
}
