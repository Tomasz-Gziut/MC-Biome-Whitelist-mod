package com.biomewhitelist;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

/**
 * Biome Whitelist Mod - Restricts world generation to only whitelisted biomes.
 *
 * Design: Uses mixins to intercept biome selection during world generation
 * and redirect non-whitelisted biomes to a fallback biome from the whitelist.
 */
@Mod(BiomeWhitelist.MODID)
public class BiomeWhitelist {
    public static final String MODID = "biomewhitelist";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BiomeWhitelist() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BiomeWhitelistConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);

        // Register for config events on the mod event bus
        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onConfigLoad);
        modBus.addListener(this::onConfigReload);

        LOGGER.info("[BiomeWhitelist]: Mod initialized. Configure biomes in biomewhitelist-common.toml");
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            BiomeWhitelistConfig.rebuildCache();
            LOGGER.info("[BiomeWhitelist.onConfigLoad]: Config loaded");
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            BiomeWhitelistConfig.rebuildCache();
            LOGGER.info("[BiomeWhitelist.onConfigReload]: Config reloaded");
        }
    }
}
