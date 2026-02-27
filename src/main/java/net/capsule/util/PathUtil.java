package net.capsule.util;

import java.io.File;
import java.util.regex.Pattern;

public final class PathUtil {
    private static final Pattern EXT = Pattern.compile("(?<=.)\\.[^.]+$");

    private PathUtil() {
    }

    public static String getFileNameWithoutExtension(File file) {
        return EXT.matcher(file.getName()).replaceAll("");
    }
}
