# Planned Features

All planned features have been implemented!

## Completed Features

### Filter Modes (Implemented)
- **DISABLED**: No filtering, vanilla world generation
- **WHITELIST**: Only biomes in the list can generate
- **BLACKLIST**: All biomes except those in the list can generate

### World Generation Settings (Implemented)
- **Sea Level**: Adjustable from -1999 to 1999 (vanilla: 63)
- **Bedrock Level**: Adjustable from -2000 to 2000 (vanilla: -64)
- **Max Height**: Adjustable from -2000 to 2000 (vanilla: 512)

### Configuration (Implemented)
- Config file at `config/worldmodifier-common.toml`
- Organized into `[biomes]` and `[world]` sections
- Hot reload support for newly generated chunks
