package com.biomewhitelist.mixin;

import com.biomewhitelist.BiomeWhitelist;
import com.biomewhitelist.BiomeWhitelistConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

/**
 * Mixin to intercept biome selection in TheEndBiomeSource.
 *
 * Design: Same approach as MultiNoiseBiomeSourceMixin but for the End dimension.
 */
@Mixin(TheEndBiomeSource.class)
public class TheEndBiomeSourceMixin {

    @Unique
    private Holder<Biome> biomewhitelist$cachedFallback = null;

    @Unique
    private ResourceLocation biomewhitelist$cachedFallbackKey = null;

    @Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
    private void biomewhitelist$filterBiome(int x, int y, int z,
                                             net.minecraft.world.level.biome.Climate.Sampler sampler,
                                             CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!BiomeWhitelistConfig.isFilteringActive()) {
            return;
        }

        Holder<Biome> originalBiome = cir.getReturnValue();
        if (originalBiome == null) {
            return;
        }

        Optional<ResourceKey<Biome>> keyOpt = originalBiome.unwrapKey();
        if (keyOpt.isEmpty()) {
            return;
        }

        ResourceLocation biomeId = keyOpt.get().location();
        Set<ResourceLocation> whitelist = BiomeWhitelistConfig.getWhitelistedBiomes();

        if (whitelist.contains(biomeId)) {
            return;
        }

        Holder<Biome> fallback = biomewhitelist$getFallbackBiome(originalBiome);
        if (fallback != null) {
            cir.setReturnValue(fallback);
        }
    }

    @Unique
    private Holder<Biome> biomewhitelist$getFallbackBiome(Holder<Biome> sampleBiome) {
        ResourceLocation configuredFallback = BiomeWhitelistConfig.getFallbackBiome();

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

        BiomeWhitelist.LOGGER.warn("[TheEndBiomeSourceMixin.getFallbackBiome]: Fallback biome {} not found in biome source's possible biomes", configuredFallback);
        return null;
    }
}
