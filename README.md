# Biome Whitelist Mod

A Minecraft Forge mod for 1.20.1 that restricts world generation to only specified biomes. Perfect for creating themed worlds, custom modpacks, or specialized server experiences.

## Features

- **Biome Filtering**: Only allow specific biomes to generate in your world
- **Fallback Biome**: Non-whitelisted biomes are replaced with a configurable fallback biome
- **Hot Reload**: Configuration changes take effect without restarting the game
- **Mod Support**: Works with modded biomes using their full resource locations
- **Beta Feature**: Optional flat ocean floor generation

## Requirements

- Minecraft 1.20.1
- Forge 47.2.0 or higher
- Java 17

## Installation

1. Download the mod JAR file
2. Place it in your Minecraft `mods` folder
3. Launch the game with Forge

## Configuration

After first launch, a configuration file will be created at:
```
config/biomewhitelist-common.toml
```

### Configuration Options

#### `enabled` (default: `true`)
Master toggle for the mod. Set to `false` to disable biome filtering entirely.

#### `whitelistedBiomes` (default: `["minecraft:plains"]`)
List of biomes that are allowed to generate. Use full resource locations.

**Important**: If this list is empty, all biomes are allowed (whitelist is disabled).

#### `fallbackBiome` (default: `"minecraft:plains"`)
The biome used when a non-whitelisted biome would normally generate. Must be a biome from your whitelist.

#### `forceFlatOcean` (Beta, default: `false`)
When enabled, ocean biomes generate with a flat floor instead of underwater terrain features.

#### `oceanFloorLevel` (Beta, default: `48`)
Y-level for the ocean floor when `forceFlatOcean` is enabled. Sea level is at Y=63.

## Example Configurations

### Plains-Only World
```toml
[general]
enabled = true
whitelistedBiomes = ["minecraft:plains", "minecraft:river", "minecraft:ocean"]
fallbackBiome = "minecraft:plains"
```

### Forest Survival
```toml
[general]
enabled = true
whitelistedBiomes = [
    "minecraft:forest",
    "minecraft:birch_forest",
    "minecraft:dark_forest",
    "minecraft:flower_forest",
    "minecraft:river",
    "minecraft:ocean"
]
fallbackBiome = "minecraft:forest"
```

### Desert Challenge
```toml
[general]
enabled = true
whitelistedBiomes = [
    "minecraft:desert",
    "minecraft:badlands",
    "minecraft:eroded_badlands",
    "minecraft:river",
    "minecraft:warm_ocean"
]
fallbackBiome = "minecraft:desert"
```

### Winter World
```toml
[general]
enabled = true
whitelistedBiomes = [
    "minecraft:snowy_plains",
    "minecraft:snowy_taiga",
    "minecraft:snowy_slopes",
    "minecraft:frozen_peaks",
    "minecraft:ice_spikes",
    "minecraft:frozen_river",
    "minecraft:frozen_ocean",
    "minecraft:deep_frozen_ocean"
]
fallbackBiome = "minecraft:snowy_plains"
```

## Common Vanilla Biomes

Here's a reference list of common biome IDs:

| Category | Biomes |
|----------|--------|
| **Plains/Meadow** | `minecraft:plains`, `minecraft:sunflower_plains`, `minecraft:meadow` |
| **Forest** | `minecraft:forest`, `minecraft:birch_forest`, `minecraft:dark_forest`, `minecraft:flower_forest`, `minecraft:cherry_grove` |
| **Taiga** | `minecraft:taiga`, `minecraft:old_growth_pine_taiga`, `minecraft:old_growth_spruce_taiga` |
| **Jungle** | `minecraft:jungle`, `minecraft:sparse_jungle`, `minecraft:bamboo_jungle` |
| **Desert/Badlands** | `minecraft:desert`, `minecraft:badlands`, `minecraft:eroded_badlands`, `minecraft:wooded_badlands` |
| **Snowy** | `minecraft:snowy_plains`, `minecraft:snowy_taiga`, `minecraft:snowy_slopes`, `minecraft:ice_spikes`, `minecraft:frozen_peaks`, `minecraft:jagged_peaks` |
| **Mountain** | `minecraft:stony_peaks`, `minecraft:grove`, `minecraft:snowy_slopes`, `minecraft:jagged_peaks`, `minecraft:frozen_peaks` |
| **Swamp** | `minecraft:swamp`, `minecraft:mangrove_swamp` |
| **Ocean** | `minecraft:ocean`, `minecraft:deep_ocean`, `minecraft:warm_ocean`, `minecraft:lukewarm_ocean`, `minecraft:cold_ocean`, `minecraft:frozen_ocean` |
| **River/Beach** | `minecraft:river`, `minecraft:frozen_river`, `minecraft:beach`, `minecraft:stony_shore` |
| **Cave** | `minecraft:deep_dark`, `minecraft:dripstone_caves`, `minecraft:lush_caves` |

## Tips

1. **Always include rivers and oceans** in your whitelist if you want water bodies, otherwise all water areas will be replaced with your fallback biome.

2. **Test on a new world** - the whitelist only affects newly generated chunks.

3. **For modded biomes**, use the format `modid:biome_name` (e.g., `biomesoplenty:redwood_forest`).

4. **Hot reload**: Edit the config file while the game is running, and changes will apply to newly generated chunks.

## Troubleshooting

**Q: My world looks completely flat/uniform**
A: Check that your whitelist contains biomes that actually exist. Also ensure the fallback biome is in your whitelist.

**Q: Changes aren't taking effect**
A: Changes only affect newly generated chunks. Travel to unexplored areas to see the new configuration.

**Q: The mod isn't working at all**
A: Make sure `enabled = true` in your config and that your whitelist is not empty.

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

This project is open source.
