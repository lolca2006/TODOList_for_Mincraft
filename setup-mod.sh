#!/bin/bash

echo "========================================="
echo "  Minecraft Mod Development Setup"
echo "========================================="
echo ""
echo "Choose your modding framework:"
echo "1) Forge (Most popular, comprehensive API)"
echo "2) Fabric (Lightweight, fast updates)"
echo ""
read -p "Enter choice (1 or 2): " choice

read -p "Enter your mod name (e.g., MyAwesomeMod): " modname
read -p "Enter Minecraft version (e.g., 1.20.1): " mcversion

case $choice in
    1)
        echo ""
        echo "Setting up Forge mod: $modname"
        echo "========================================="

        # Create mod directory
        mkdir -p "$modname"
        cd "$modname"

        # Download Forge MDK
        echo "Downloading Forge MDK..."
        # You'll need to update this URL based on the Minecraft version
        wget "https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.2.0/forge-1.20.1-47.2.0-mdk.zip" -O forge-mdk.zip
        unzip forge-mdk.zip
        rm forge-mdk.zip

        echo ""
        echo "Setup complete! Next steps:"
        echo "1. cd $modname"
        echo "2. ./gradlew genIntellijRuns  (or genEclipseRuns)"
        echo "3. ./gradlew build"
        echo "4. Edit src/main/java to create your mod"
        ;;

    2)
        echo ""
        echo "Setting up Fabric mod: $modname"
        echo "========================================="

        # Create mod directory
        mkdir -p "$modname"
        cd "$modname"

        echo "Please visit: https://fabricmc.net/develop/"
        echo "And download the Fabric Example Mod template"
        echo ""
        echo "Or use the Fabric template generator:"
        echo "https://github.com/FabricMC/fabric-example-mod"

        # Clone Fabric example mod
        git clone https://github.com/FabricMC/fabric-example-mod.git .

        echo ""
        echo "Setup complete! Next steps:"
        echo "1. Edit gradle.properties to set your mod info"
        echo "2. ./gradlew genSources"
        echo "3. ./gradlew build"
        echo "4. Edit src/main/java to create your mod"
        ;;

    *)
        echo "Invalid choice!"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "Your mod project is ready!"
echo "========================================="
