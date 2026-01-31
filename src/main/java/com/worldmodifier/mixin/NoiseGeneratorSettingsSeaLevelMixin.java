package com.worldmodifier.mixin;

import com.worldmodifier.WorldModifierConfig;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override the sea level in NoiseGeneratorSettings.
 * This affects terrain generation around sea level.
 */
@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsSeaLevelMixin {

    @Inject(method = "seaLevel", at = @At("HEAD"), cancellable = true)
    private void worldmodifier$overrideSeaLevel(CallbackInfoReturnable<Integer> cir) {
        int seaLevel = WorldModifierConfig.getSeaLevel();
        if (seaLevel != WorldModifierConfig.DEFAULT_SEA_LEVEL) {
            cir.setReturnValue(seaLevel);
        }
    }
}
