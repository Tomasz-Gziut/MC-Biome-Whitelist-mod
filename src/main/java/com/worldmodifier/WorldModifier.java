package com.worldmodifier;

import com.mojang.logging.LogUtils;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

/**
 * World Modifier Mod - Customize world generation settings.
 *
 * Design: Uses mixins to intercept biome selection during world generation
 * and redirect non-whitelisted biomes to a fallback biome from the whitelist.
 */
@Mod(WorldModifier.MODID)
public class WorldModifier {
    public static final String MODID = "worldmodifier";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WorldModifier() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WorldModifierConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);

        // Register config screen (accessible via Mod Menu or Forge's mod list)
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> WorldModifierConfigScreen.create(parent)
                )
        );

        // Register for config events on the mod event bus
        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onConfigLoad);
        modBus.addListener(this::onConfigReload);

        LOGGER.info("[WorldModifier]: Mod initialized. Configure settings in worldmodifier-common.toml or in-game via Mods menu");
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            WorldModifierConfig.rebuildCache();
            LOGGER.info("[WorldModifier.onConfigLoad]: Config loaded");
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            WorldModifierConfig.rebuildCache();
            LOGGER.info("[WorldModifier.onConfigReload]: Config reloaded");
        }
    }
}
