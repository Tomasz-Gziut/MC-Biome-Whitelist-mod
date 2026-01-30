package com.biomewhitelist.mixin;

import com.biomewhitelist.BiomeWhitelist;
import com.biomewhitelist.BiomeWhitelistConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

/**
 * Mixin to intercept biome selection in MultiNoiseBiomeSource.
 *
 * Design: Intercepts the getNoiseBiome method which is called during world generation
 * to determine which biome should be placed at a given location. If the selected biome
 * is not in the whitelist, it returns the fallback biome instead.
 *
 * Contract:
 * - Only modifies behavior when BiomeWhitelistConfig.isFilteringActive() returns true
 * - Non-whitelisted biomes are replaced with the configured fallback biome
 * - Thread-safe: uses only thread-safe config access
 */
@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {

    @Unique
    private Holder<Biome> biomewhitelist$cachedFallback = null;

    @Unique
    private ResourceLocation biomewhitelist$cachedFallbackKey = null;

    /**
     * Intercepts biome selection to filter based on whitelist.
     *
     * @param x Climate x coordinate
     * @param y Climate y coordinate
     * @param z Climate z coordinate
     * @param sampler Climate sampler for noise-based selection
     * @param cir Callback to potentially modify return value
     */
    @Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
    private void biomewhitelist$filterBiome(int x, int y, int z, Climate.Sampler sampler,
                                             CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!BiomeWhitelistConfig.isFilteringActive()) {
            return;
        }

        Holder<Biome> originalBiome = cir.getReturnValue();
        if (originalBiome == null) {
            return;
        }

        // Get the biome's resource location
        Optional<ResourceKey<Biome>> keyOpt = originalBiome.unwrapKey();
        if (keyOpt.isEmpty()) {
            return;
        }

        ResourceLocation biomeId = keyOpt.get().location();
        Set<ResourceLocation> whitelist = BiomeWhitelistConfig.getWhitelistedBiomes();

        // Check if biome is allowed
        if (whitelist.contains(biomeId)) {
            return;
        }

        // Biome not in whitelist - return fallback
        Holder<Biome> fallback = biomewhitelist$getFallbackBiome(originalBiome);
        if (fallback != null) {
            cir.setReturnValue(fallback);
        }
    }

    /**
     * Gets the fallback biome holder, with caching for performance.
     * Searches through the biome source's possible biomes to find the fallback.
     */
    @Unique
    private Holder<Biome> biomewhitelist$getFallbackBiome(Holder<Biome> sampleBiome) {
        ResourceLocation configuredFallback = BiomeWhitelistConfig.getFallbackBiome();

        // Check cache validity
        if (biomewhitelist$cachedFallback != null &&
            configuredFallback.equals(biomewhitelist$cachedFallbackKey)) {
            return biomewhitelist$cachedFallback;
        }

        // Search through the biome source's possible biomes to find the fallback
        BiomeSource self = (BiomeSource) (Object) this;
        for (Holder<Biome> biomeHolder : self.possibleBiomes()) {
            Optional<ResourceKey<Biome>> keyOpt = biomeHolder.unwrapKey();
            if (keyOpt.isPresent() && keyOpt.get().location().equals(configuredFallback)) {
                biomewhitelist$cachedFallback = biomeHolder;
                biomewhitelist$cachedFallbackKey = configuredFallback;
                return biomewhitelist$cachedFallback;
            }
        }

        BiomeWhitelist.LOGGER.warn("[MultiNoiseBiomeSourceMixin.getFallbackBiome]: Fallback biome {} not found in biome source's possible biomes", configuredFallback);
        return null;
    }
}
