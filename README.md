# NoArmor

[![Build & Test](https://github.com/kvmhl/no-armor/actions/workflows/build.yml/badge.svg)](https://github.com/kvmhl/no-armor/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/v/release/kvmhl/no-armor)](https://github.com/kvmhl/no-armor/releases/latest)

A lightweight [PaperMC](https://papermc.io/) plugin for Minecraft 1.21.11+ that lets you customize which armor, tools, and inventory slots players can use — with visual feedback showing restricted slots as greyed out.

## Features

- 🛡️ **Block specific armor pieces** — Prevent players from equipping certain helmets, chestplates, leggings, or boots
- ⚔️ **Restrict tools and weapons** — Block swords, pickaxes, bows, or any item in the game
- 📦 **Inventory slot restrictions** — Grey out entire hotbar slots or inventory rows
- 👁️ **Visual feedback** — Restricted slots show a grey glass pane so players know they can't use them
- 🚫 **Crafting prevention** — Restricted items cannot be crafted
- 📥 **Pickup prevention** — Restricted items cannot be picked up from the ground
- 🔄 **Hot reload** — Change config without restarting the server
- 🔓 **Bypass permission** — Allow certain players/ranks to ignore restrictions
- 📋 **400+ items pre-configured** — Every Minecraft item is listed for easy customization

## Installation

1. Download the latest release JAR from [Releases](https://github.com/kvmhl/no-armor/releases/latest)
2. Place in your Paper server's `plugins/` folder
3. Start the server
4. Edit `plugins/NoArmor/config.yml` to customize restrictions

## Configuration

### Restricting Items

In `config.yml`, set any item to `false` to block it:

```yaml
allowed-items:
  # Block all diamond armor
  diamond_helmet: false
  diamond_chestplate: false
  diamond_leggings: false
  diamond_boots: false
  
  # Block netherite weapons
  netherite_sword: false
  netherite_axe: false
```

**When an item is blocked:**
- Players cannot equip/use it
- Players cannot craft it
- Players cannot pick it up from the ground

### Restricting Inventory Slots

Block entire inventory slots using slot numbers, ranges, or keywords:

```yaml
restricted-slots:
  enabled: true
  slots:
    - "0-2"      # Block first 3 hotbar slots
    - "9-17"     # Block top row of main inventory
    - "offhand"  # Block offhand slot
    - "armor"    # Block all armor slots (36-39)
```

**Slot Reference:**
| Slots | Description |
|-------|-------------|
| 0-8 | Hotbar |
| 9-35 | Main inventory (3 rows) |
| 36-39 | Armor (boots, leggings, chestplate, helmet) |
| 40 | Offhand |

**Keywords:** `hotbar`, `main`, `armor`, `offhand`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/noarmor reload` | Reload configuration | `noarmor.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `noarmor.admin` | Access to admin commands | OP |
| `noarmor.bypass` | Bypass all item/slot restrictions | false |

## Building from Source

Requires Java 21 and Maven:

```bash
git clone https://github.com/kvmhl/no-armor.git
cd no-armor
mvn clean package
```

The plugin JAR will be at `target/NoArmor-1.0-SNAPSHOT.jar`

### Running Tests

```bash
mvn test
```

## Requirements

- **Paper** 1.21.11 or newer
- **Java** 21 or newer

## License

MIT License - See [LICENSE](LICENSE) for details.
