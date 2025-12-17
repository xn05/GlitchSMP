# GlitchSMP

A Minecraft plugin for Spigot/Paper servers that introduces "Glitch" abilities, allowing players to equip and use special powers with unique effects and cooldowns. Inspired by SMP servers with custom mechanics, GlitchSMP adds a layer of strategic gameplay through glitch-based items.

## Features

- **Glitch Abilities**: Over 15 unique glitch abilities that players can equip and activate.
- **Custom Items**: Glitch items use custom model data (requires resource pack) for visual distinction.
- **Cooldown System**: Each ability has its own cooldown to balance gameplay.
- **GUI Interface**: Easy-to-use GUI for selecting and equipping glitches.
- **Actionbar HUD**: Displays equipped glitches with Unicode glyphs for quick identification.
- **Persistent Data**: Abilities and cooldowns persist across server restarts.

## List of Glitches

| Glitch Name | Cooldown | Description |
|-------------|----------|-------------|
| Crash | 4m       | Crash the server or cause disruptions (specific mechanics vary). |
| Redstone | 2m 30s   | Redstone-related effects. |
| Dream | 5m       | Dream-themed abilities. |
| Freeze | 1m 30s   | Freeze enemies or time. |
| Bedrock | 2m       | Bedrock manipulation. |
| Immortality | 5m       | Temporary invincibility. |
| Fake Block | 2m       | Create illusionary blocks. |
| Teleport | 2m       | Teleportation abilities. |
| Virus | 2m       | Spread effects to enemies. |
| Horsetamer | 10m      | Horse-related powers. |
| Invis | 4m       | Invisibility effects. |
| Windburst | 30s      | Wind or knockback effects. |
| Enchanter | 3m       | Enchantment enhancements. |
| Shulker | 4m       | Access crafting table, ender chest, and anvil through a GUI. |
| Xray | 4m       | Highlight nearby players and ores with glowing effects. |
| Gravity | 1m       | Manipulate gravity. |
| Duality | 3m       | For 10s: Freeze hit enemies for 2s, burn nearby enemies within 2 blocks. Freeze + burn nullify each other. |

*Note: Some glitch descriptions are placeholders; refer to in-game tooltips for exact mechanics.*

## Installation

1. **Prerequisites**:
   - Java 21+
   - Spigot or Paper server (1.20+ recommended)

2. **Build the Plugin**:
   ```bash
   git clone <repository-url>
   cd glitchsmp
   mvn clean package
   ```

3. **Install**:
   - Copy `target/glitchsmp-1.0.jar` to your server's `plugins/` folder.
   - Restart the server.

4. **Resource Pack**:
   - The plugin uses custom model data and Unicode glyphs. Provide a resource pack to players for full visual experience.

## Usage

### Commands

- `/glitch help` - Show help menu.
- `/glitch give <player> <glitch_id>` - Give a glitch item to a player (OP required).
- `/glitch equip <slot1|slot2> <glitch_id>` - Equip a glitch to a slot.
- `/glitch unequip <slot1|slot2>` - Unequip a glitch from a slot.
- `/glitch glitches` - Open the glitch selection GUI.
- `/glitch reload` - Reload the plugin (OP required).

### Permissions

- `glitchsmp.give` - Allow giving glitch items.
- `glitchsmp.equip` - Allow equipping glitches.
- `glitchsmp.unequip` - Allow unequipping glitches.
- `glitchsmp.gui` - Allow opening the glitch GUI.
- `glitchsmp.reload` - Allow reloading the plugin.

### Equipping Glitches

1. Obtain a glitch item (via command or GUI).
2. Right-click the item to equip it to an available slot (slot1 or slot2).
3. Use the assigned key (configured in controls) to activate the ability.
4. Monitor cooldowns via the actionbar HUD.

### Controls

- Configure activation keys in the plugin's control handler (default: right-click or offhand swap).

## Configuration

- Edit `plugins/GlitchSMP/config.yml` for settings (if available).
- Abilities are hardcoded; modify source code for customizations.

## Development

- **Dependencies**: Maven, Spigot API.
- **Building**: `mvn compile`
- **Testing**: Deploy to a test server.
- **Contributing**: Fork, make changes, submit PRs.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues or questions, open an issue on the repository or contact the maintainers.

---

*</content>
