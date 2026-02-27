package net.capsule.util;

import java.nio.file.Path;

import me.ramazanenescik04.diken.SystemInfo;

public class StoragePaths {
    private final String homeDirectory;

    public StoragePaths() {
        this.homeDirectory = resolveHomeDirectory();
    }

    private String resolveHomeDirectory() {
        if (SystemInfo.instance.getOS() == SystemInfo.OS.LINUX) {
            String linuxHome = System.getenv("HOME");
            if (linuxHome != null) {
                return linuxHome;
            }

            String linuxUser = System.getenv("USER");
            if ("root".equals(linuxUser)) {
                return "/root";
            }

            return "/home/" + linuxUser;
        }

        return System.getProperty("user.home");
    }

    public Path getConfigPath() {
        return getDirectoryPath().resolve("config.cfg");
    }

    public Path getDirectoryPath() {
        return switch (SystemInfo.instance.getOS()) {
            case SystemInfo.OS.WINDOWS -> Path.of(homeDirectory, "AppData", "Roaming", ".capsule");
            case SystemInfo.OS.MACOS -> Path.of(homeDirectory, "Library", "Application Support", "capsule");
            default -> Path.of(homeDirectory, ".capsule");
        };
    }

    public Path getDesktopPath() {
        return Path.of(homeDirectory, "Desktop");
    }

    public Path getCachePath() {
        return getDirectoryPath().resolve("cache");
    }

    public Path getVersionsPath() {
        return getDirectoryPath().resolve("versions");
    }

    public Path getLogsPath() {
        return getDirectoryPath().resolve("logs");
    }

    public String getDirectory() {
        return getDirectoryPath().toString() + java.io.File.separator;
    }

    public String getConfigPathString() {
        return getConfigPath().toString();
    }

    public String getDesktop() {
        return getDesktopPath().toString() + java.io.File.separator;
    }
}
