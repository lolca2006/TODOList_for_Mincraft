FROM ubuntu:22.04

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

# Set timezone
ENV TZ=UTC

# Update and install essential packages
RUN apt-get update && apt-get install -y \
    # Basic utilities
    curl \
    wget \
    git \
    vim \
    nano \
    htop \
    tmux \
    unzip \
    zip \
    tree \
    jq \
    # Build tools
    build-essential \
    cmake \
    pkg-config \
    # Network tools
    net-tools \
    iputils-ping \
    dnsutils \
    netcat \
    # Security & SSL
    ca-certificates \
    gnupg \
    lsb-release \
    # Python
    python3 \
    python3-pip \
    python3-venv \
    # Other useful tools
    sudo \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# Install Java 21 (required for modern Minecraft modding)
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Set Java 21 as default
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip -P /tmp \
    && unzip -d /opt/gradle /tmp/gradle-8.5-bin.zip \
    && rm /tmp/gradle-8.5-bin.zip

ENV GRADLE_HOME=/opt/gradle/gradle-8.5
ENV PATH=$PATH:$GRADLE_HOME/bin

# Install Node.js (LTS) - useful for web-based tools
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Upgrade pip and install useful Python packages
RUN pip3 install --upgrade pip \
    && pip3 install \
    requests \
    black \
    pylint

# Create a non-root user (optional, but recommended)
RUN useradd -m -s /bin/bash developer \
    && echo "developer ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Set working directory
WORKDIR /workspace

# Switch to non-root user
USER developer

# Set default shell
SHELL ["/bin/bash", "-c"]

CMD ["/bin/bash"]
