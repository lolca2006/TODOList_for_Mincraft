# Todo List Mod

[![Build](https://github.com/lolca2006/TODOList_for_Mincraft/actions/workflows/build.yml/badge.svg)](https://github.com/lolca2006/TODOList_for_Mincraft/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/v/release/lolca2006/TODOList_for_Mincraft?include_prereleases)](https://github.com/lolca2006/TODOList_for_Mincraft/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)

A full-featured in-game todo list for Minecraft with HUD overlay, categories, multiplayer support, resource tracking, JEI integration, Litematica import, and more.

**Supports Forge and Fabric!**

## Features

- **In-Game Todo List** &mdash; Create, edit, delete, and reorder tasks without leaving the game
- **HUD Overlay** &mdash; Persistent on-screen display showing your current tasks (draggable, scalable, configurable)
- **Categories** &mdash; Organize tasks with color-coded categories and tab filtering
- **Resource Tracking** &mdash; Attach item/block requirements to tasks; tracks inventory progress automatically
- **Inventory Overlay** &mdash; See required resources directly on container screens
- **Multiplayer Support** &mdash; Server-synced tasks with visibility controls (Private / Team / Public) and player assignment
- **JEI Integration** &mdash; Drag items from JEI directly into task resource requirements
- **Litematica Import** &mdash; Import material lists from Litematica schematics as todo items
- **Completion Sound** &mdash; Satisfying sound effect when all resources for a task are gathered
- **Drag & Drop** &mdash; Reorder tasks by dragging them in the list

## Downloads

| Version | Forge | Fabric |
|---------|-------|--------|
| 1.20.1 | [Download](https://github.com/lolca2006/TODOList_for_Mincraft/releases/latest) | [Download](https://github.com/lolca2006/TODOList_for_Mincraft/releases/latest) |

You can also find this mod on:
- [Modrinth](https://modrinth.com/) (coming soon)
- [CurseForge](https://www.curseforge.com/) (coming soon)

## Installation

1. Install [Minecraft Forge](https://files.minecraftforge.net/) or [Fabric Loader](https://fabricmc.net/) + [Fabric API](https://modrinth.com/mod/fabric-api) for your Minecraft version
2. Download the correct JAR for your mod loader from [Releases](https://github.com/lolca2006/TODOList_for_Mincraft/releases)
3. Place the JAR file in your `mods/` folder
4. Launch Minecraft

## Usage

- Press **K** (default) to open the Todo List screen
- Press **H** to toggle the HUD overlay
- Right-click tasks to edit, assign resources, or change visibility
- Drag tasks to reorder them
- Use the settings screen to customize HUD position, scale, and opacity

### Optional Integrations

| Mod | Benefit |
|-----|---------|
| [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) | Drag items from JEI into resource requirements |
| [Litematica](https://www.curseforge.com/minecraft/mc-mods/litematica) | Import schematic material lists as todos |

## Building from Source

### Prerequisites
- Java 17 (JDK)
- Git

### Build

```bash
git clone https://github.com/lolca2006/TODOList_for_Mincraft.git
cd TODOList_for_Mincraft/TodoListMod
./gradlew build
```

Output JARs:
- `forge/build/libs/todolistmod-forge-1.20.1-*.jar`
- `fabric/build/libs/todolistmod-fabric-1.20.1-*.jar`

### Project Structure

```
TodoListMod/
├── common/     # Shared code (models, GUI, data, integrations)
├── forge/      # Forge-specific (networking, config, events)
├── fabric/     # Fabric-specific (networking, config, events)
├── buildSrc/   # Gradle convention plugins
└── .github/    # CI/CD workflows
```

This project uses the [MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template) architecture with Java `ServiceLoader` for platform abstraction &mdash; no runtime API dependencies.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License &mdash; see the [LICENSE](LICENSE) file for details.
