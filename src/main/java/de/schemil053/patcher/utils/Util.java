package de.schemil053.patcher.utils;

import java.io.File;

public class Util {
    public enum OS {
        LINUX, SOLARIS, WINDOWS, MACOS, UNKNOWN;
    }

    public static OS getPlatform() {
        String str = System.getProperty("os.name").toLowerCase();
        if (str.contains("win")) {
            return OS.WINDOWS;
        }
        if (str.contains("mac")) {
            return OS.MACOS;
        }
        if (str.contains("solaris")) {
            return OS.SOLARIS;
        }
        if (str.contains("sunos")) {
            return OS.SOLARIS;
        }
        if (str.contains("linux")) {
            return OS.LINUX;
        }
        if (str.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    private static File workDir = null;

    public static File getWorkingDirectory() {
        if (workDir == null) {
            workDir = getWorkingDirectory("minecraft");
        }
        return workDir;
    }

    public static File getWorkingDirectory(String dir) {
        File file;
        String home = System.getProperty("user.home", ".");
        switch (getPlatform()) {
            case LINUX:
            case SOLARIS: {
                file = new File(home, '.' + dir + '/');
                break;
            }
            case WINDOWS: {
                String appdata = System.getenv("APPDATA");
                String search = (appdata != null) ? appdata : home;
                file = new File(search, '.' + dir + '/');
                break;
            }
            case MACOS: {
                file = new File(home, "Library/Application Support/" + dir);
                break;
            }
            default: {
                file = new File(home, dir + '/');
                break;
            }
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + file);
        }
        return file;
    }
}
