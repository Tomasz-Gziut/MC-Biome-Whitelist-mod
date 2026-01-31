package com.biomewhitelist.mixin;

import com.biomewhitelist.BiomeWhitelistConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Mixin to force flat terrain for ocean biomes when the beta option is enabled.
 *
 * Design: Intercepts after noise-based terrain generation completes and flattens
 * any columns where the biome is an ocean type.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    @Unique
    private static final int SEA_LEVEL = 63;

    @Unique
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();

    @Unique
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();

    @Unique
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /**
     * Intercepts after doFill completes to flatten ocean terrain.
     * doFill is responsible for placing the initial stone/water blocks.
     */
    @Inject(method = "doFill", at = @At("TAIL"))
    private void biomewhitelist$flattenOceanTerrain(
            net.minecraft.world.level.levelgen.blending.Blender blender,
            net.minecraft.world.level.StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk,
            int minCellY,
            int cellHeight,
            CallbackInfoReturnable<ChunkAccess> cir) {

        if (!BiomeWhitelistConfig.isForceFlatOceanEnabled()) {
            return;
        }

        int oceanFloor = BiomeWhitelistConfig.getOceanFloorLevel();
        ChunkPos chunkPos = chunk.getPos();

        // Access biome source by casting this to ChunkGenerator
        BiomeSource biomeSource = ((ChunkGenerator) (Object) this).getBiomeSource();

        // Process each column in the chunk
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = chunkPos.getMinBlockX() + localX;
                int worldZ = chunkPos.getMinBlockZ() + localZ;

                // Get biome at sea level for this column
                Holder<Biome> biomeHolder = biomeSource.getNoiseBiome(
                        worldX >> 2, SEA_LEVEL >> 2, worldZ >> 2,
                        randomState.sampler()
                );

                if (!biomewhitelist$isOceanBiome(biomeHolder)) {
                    continue;
                }

                // Flatten this column
                biomewhitelist$flattenColumn(chunk, localX, localZ, oceanFloor);
            }
        }
    }

    /**
     * Flattens a single column to create ocean floor at the specified level.
     */
    @Unique
    private void biomewhitelist$flattenColumn(ChunkAccess chunk, int localX, int localZ, int oceanFloor) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        // From ocean floor down: keep existing terrain (stone/deepslate)
        // At ocean floor: gravel (ocean floor)
        // From ocean floor+1 to sea level: water
        // Above sea level: air

        for (int y = maxY - 1; y >= minY; y--) {
            BlockPos pos = new BlockPos(localX, y, localZ);

            if (y > SEA_LEVEL) {
                // Above sea level: air
                chunk.setBlockState(pos, AIR, false);
            } else if (y > oceanFloor && y <= SEA_LEVEL) {
                // Between ocean floor and sea level: water
                chunk.setBlockState(pos, WATER, false);
            } else if (y == oceanFloor) {
                // Ocean floor: gravel
                chunk.setBlockState(pos, GRAVEL, false);
            }
            // Below ocean floor: leave existing terrain (stone/deepslate)
        }
    }

    /**
     * Checks if the given biome holder is an ocean biome.
     */
    @Unique
    private boolean biomewhitelist$isOceanBiome(Holder<Biome> biomeHolder) {
        if (biomeHolder == null) {
            return false;
        }

        Optional<ResourceKey<Biome>> keyOpt = biomeHolder.unwrapKey();
        if (keyOpt.isEmpty()) {
            return false;
        }

        ResourceLocation biomeId = keyOpt.get().location();
        return BiomeWhitelistConfig.isOceanBiome(biomeId);
    }
}
