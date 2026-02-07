# Docker Development Environment for Minecraft Modding

A Docker-based development environment specifically configured for Minecraft mod development!

## What's Included

### For Minecraft Modding
- **Java 21** (default) - For Minecraft 1.20.4+
- **Java 17** - For Minecraft 1.18-1.20.3
- **Gradle 8.5** - Build automation tool

### Other Programming Languages
- **Python 3** with pip
- **Node.js 20** (LTS) with npm

### Development Tools
- Git
- Vim & Nano
- tmux
- Build tools (gcc, g++, make, cmake)
- Docker CLI

### Utilities
- curl, wget
- jq (JSON processor)
- htop
- tree
- Network tools (ping, netcat, etc.)

## Quick Start

### Build the container
```bash
docker-compose build
```

### Start and enter the container
```bash
docker-compose run --rm devenv
```

Or use Docker directly:
```bash
docker build -t devenv .
docker run -it -v .:/workspace devenv
```

### Start container in background
```bash
docker-compose up -d
```

### Attach to running container
```bash
docker-compose exec devenv /bin/bash
```

### Stop the container
```bash
docker-compose down
```

## Volume Mounts

- Current directory (`.`) is mounted to `/workspace` in the container
- Changes made in `/workspace` persist on your host machine
- A persistent volume `dev-home` preserves the user's home directory between container restarts

## Customization

### Add More Tools
Edit the `Dockerfile` to install additional packages:
```dockerfile
RUN apt-get update && apt-get install -y \
    your-package-here \
    && rm -rf /var/lib/apt/lists/*
```

### Expose Ports
Uncomment and modify the `ports` section in `docker-compose.yml`:
```yaml
ports:
  - "8000:8000"  # Host:Container
```

### Install Additional Python Packages
```bash
# Inside the container
pip3 install package-name
```

### Install Additional Node.js Packages
```bash
# Inside the container
npm install -g package-name
```

## Tips

- The default user is `developer` (non-root) with sudo access
- All your project files in the current directory are accessible in `/workspace`
- Use tmux for multiple terminal sessions within the container
- Docker socket is mounted, so you can use Docker commands inside the container

## Rebuild After Changes

If you modify the Dockerfile:
```bash
docker-compose build --no-cache
```
