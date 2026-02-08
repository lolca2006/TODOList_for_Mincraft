package com.todolist.mod.platform;

import java.nio.file.Path;

public interface IPlatformHelper {
    boolean isModLoaded(String modId);
    Path getConfigDir();
    boolean isDevelopmentEnvironment();
}
