# World Modifier Mod

A Minecraft Forge mod for 1.20.1 that allows you to customize world generation settings. Control which biomes generate, adjust sea level, bedrock depth, and build height to create unique world configurations.

## Features

- **Biome Filtering**: Whitelist or blacklist specific biomes
- **Smart Replacement**: Non-allowed biomes are replaced with a fallback biome, maintaining natural-sized regions
- **Custom Sea Level**: Adjust the world's sea level (-1999 to 1999)
- **Custom Bedrock Level**: Control the world's depth (-2000 to 2000)
- **Custom Max Height**: Adjust the build limit (-2000 to 2000)
- **Hot Reload**: Configuration changes take effect on newly generated chunks
- **Mod Support**: Works with modded biomes using their full resource locations

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
config/worldmodifier-common.toml
```

### Configuration Sections

#### Biome Filtering (`[biomes]`)

##### `mode` (default: `DISABLED`)
Controls biome filtering behavior:
- `DISABLED` - No filtering, all biomes generate normally
- `WHITELIST` - Only biomes in the list can generate
- `BLACKLIST` - All biomes except those in the list can generate

##### `list` (default: ocean biomes)
List of biomes for filtering. Use full resource locations like `minecraft:plains`.

When a non-allowed biome would generate, it is replaced with the first biome from the list (or `minecraft:plains` if empty).

#### World Generation (`[world]`)

##### `seaLevel` (default: `63`)
Y coordinate where water surface generates. Vanilla Minecraft default is 63.
- Range: -1999 to 1999
- Higher values = more water coverage
- Lower values = less water coverage

##### `bedrockLevel` (default: `-64`)
Y level where bedrock generates (bottom of the world). Vanilla Minecraft default is -64.
- Range: -2000 to 2000
- Higher values = shallower world
- Lower values = deeper world
- Note: Rounded down to nearest multiple of 16

##### `maxHeight` (default: `512`)
Maximum build height (top of the world). Vanilla Minecraft default is 512.
- Range: -2000 to 2000
- Higher values = taller build limit
- Lower values = lower sky
- Note: Rounded up to nearest multiple of 16

## Example Configurations

### Vanilla World (Default)
```toml
[biomes]
mode = "DISABLED"

[world]
seaLevel = 63
bedrockLevel = -64
maxHeight = 512
```

### Plains-Only World (Whitelist)
```toml
[biomes]
mode = "WHITELIST"
list = ["minecraft:plains", "minecraft:river", "minecraft:ocean"]

[world]
seaLevel = 63
```

### No Deserts (Blacklist)
```toml
[biomes]
mode = "BLACKLIST"
list = ["minecraft:desert", "minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"]
```

### Flooded World
```toml
[biomes]
mode = "DISABLED"

[world]
seaLevel = 100
```

### Deep World
```toml
[biomes]
mode = "DISABLED"

[world]
bedrockLevel = -256
maxHeight = 512
```

### Forest Survival (Whitelist)
```toml
[biomes]
mode = "WHITELIST"
list = [
    "minecraft:forest",
    "minecraft:birch_forest",
    "minecraft:dark_forest",
    "minecraft:flower_forest",
    "minecraft:river",
    "minecraft:ocean"
]
```

### Winter World (Whitelist)
```toml
[biomes]
mode = "WHITELIST"
list = [
    "minecraft:snowy_plains",
    "minecraft:snowy_taiga",
    "minecraft:snowy_slopes",
    "minecraft:frozen_peaks",
    "minecraft:ice_spikes",
    "minecraft:frozen_river",
    "minecraft:frozen_ocean",
    "minecraft:deep_frozen_ocean"
]
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

1. **Always include rivers and oceans** in your whitelist if you want water bodies, otherwise all water areas will be replaced with the fallback biome.

2. **Test on a new world** - changes only affect newly generated chunks.

3. **For modded biomes**, use the format `modid:biome_name` (e.g., `biomesoplenty:redwood_forest`).

4. **Hot reload**: Edit the config file while the game is running, and changes will apply to newly generated chunks.

5. **Blacklist mode** is useful when you want most biomes but want to exclude specific ones (like removing all desert biomes).

## Troubleshooting

**Q: My world looks completely flat/uniform**
A: Check that your biome list contains biomes that actually exist, and verify the mode is set correctly.

**Q: Changes aren't taking effect**
A: Changes only affect newly generated chunks. Travel to unexplored areas to see the new configuration.

**Q: The mod isn't working at all**
A: Make sure `mode` is set to `WHITELIST` or `BLACKLIST` (not `DISABLED`) and that your biome list is not empty.

**Q: Sea level/bedrock changes aren't visible**
A: World generation settings only affect newly generated chunks. Create a new world to see full effects.

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

This project is open source.
