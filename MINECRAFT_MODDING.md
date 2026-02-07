# Minecraft Modding Guide

This Docker environment is set up for Minecraft mod development!

## What's Included

- **Java 21** (default) - For Minecraft 1.20.4+
- **Java 17** - For Minecraft 1.18-1.20.3
- **Gradle 8.5** - Build tool for compiling mods
- **Git** - Version control
- **Development tools** - vim, nano, tmux, etc.

## Quick Start

### 1. Build and Start the Container

```bash
docker-compose build
docker-compose run --rm devenv
```

### 2. Choose Your Modding Framework

#### **Forge** (Recommended for beginners)
- Most popular framework
- Comprehensive API
- Lots of tutorials and documentation
- Website: https://files.minecraftforge.net/

#### **Fabric** (Lightweight alternative)
- Modern, lightweight framework
- Faster updates for new Minecraft versions
- Great performance
- Website: https://fabricmc.net/

### 3. Set Up Your Mod Project

#### For Forge:

1. Download Forge MDK from https://files.minecraftforge.net/
   ```bash
   # Inside container
   wget https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.2.0/forge-1.20.1-47.2.0-mdk.zip
   unzip forge-1.20.1-47.2.0-mdk.zip -d MyMod
   cd MyMod
   ```

2. Generate development workspace:
   ```bash
   ./gradlew genIntellijRuns
   # or for Eclipse: ./gradlew genEclipseRuns
   ```

3. Build your mod:
   ```bash
   ./gradlew build
   ```

#### For Fabric:

1. Clone the Fabric example mod:
   ```bash
   git clone https://github.com/FabricMC/fabric-example-mod.git MyMod
   cd MyMod
   ```

2. Edit `gradle.properties` to customize your mod info

3. Generate sources and build:
   ```bash
   ./gradlew genSources
   ./gradlew build
   ```

### 4. Project Structure

**Forge:**
```
MyMod/
├── src/main/java/          # Your mod code goes here
├── src/main/resources/     # Assets, configs, mod metadata
├── build.gradle            # Build configuration
└── gradle.properties       # Mod properties
```

**Fabric:**
```
MyMod/
├── src/main/java/          # Your mod code goes here
├── src/main/resources/     # Assets, configs, fabric.mod.json
├── build.gradle            # Build configuration
└── gradle.properties       # Mod properties
```

## Development Workflow

### 1. Edit Your Code
Navigate to `src/main/java/` and edit your mod files with vim or nano

### 2. Build Your Mod
```bash
./gradlew build
```
The compiled mod JAR will be in `build/libs/`

### 3. Test Your Mod
```bash
./gradlew runClient    # Launch Minecraft client with your mod
./gradlew runServer    # Launch Minecraft server with your mod
```

### 4. Common Gradle Tasks
```bash
./gradlew tasks           # List all available tasks
./gradlew clean           # Clean build files
./gradlew build           # Compile mod
./gradlew runClient       # Run Minecraft with mod
./gradlew runServer       # Run Minecraft server
```

## Switching Java Versions

If you need Java 17 instead of 21:
```bash
sudo update-alternatives --config java
```

Or set it in your build.gradle:
```gradle
java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}
```

## Useful Resources

### Forge Resources:
- **Official Docs**: https://docs.minecraftforge.net/
- **Community Wiki**: https://forge.gemwire.uk/wiki/Main_Page
- **Forums**: https://forums.minecraftforge.net/

### Fabric Resources:
- **Official Docs**: https://fabricmc.net/wiki/
- **API Javadocs**: https://maven.fabricmc.net/docs/
- **Discord**: https://discord.gg/v6v4pMv

### General:
- **Minecraft Wiki**: https://minecraft.wiki/
- **Parchment Mappings**: https://parchmentmc.org/ (better method/parameter names)

## Example Mod Code

### Simple Forge Mod (Main class):
```java
package com.yourname.yourmod;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("yourmod")
public class YourMod {
    private static final Logger LOGGER = LogManager.getLogger();

    public YourMod() {
        LOGGER.info("Your Mod is loading!");
    }
}
```

### Simple Fabric Mod (Main class):
```java
package com.yourname.yourmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YourMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("yourmod");

    @Override
    public void onInitialize() {
        LOGGER.info("Your Mod is loading!");
    }
}
```

## Tips

1. **First mod?** Start with Forge - more tutorials available
2. **Use a template** - Don't start from scratch
3. **Check Minecraft version compatibility** - Make sure your framework version matches
4. **Read the docs** - Both Forge and Fabric have great documentation
5. **Join the community** - Discord servers are very helpful

## Troubleshooting

### Gradle build fails
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Out of memory
Edit `gradle.properties` and increase:
```properties
org.gradle.jvmargs=-Xmx4G
```

### Wrong Java version
Check your Java version:
```bash
java -version
```

Happy modding! 🎮
