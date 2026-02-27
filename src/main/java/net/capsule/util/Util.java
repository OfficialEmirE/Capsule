package net.capsule.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {
    // SAKIN BUNDAN ÖRNEK ALMA
    public static String getFileData(String path) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Util.class.getResourceAsStream(path)));

            StringBuilder sb = new StringBuilder();
            String s = "";

            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }

            return sb.toString();
        } catch (IOException var5) {
            return """
                    {
                        "status": "error",
                        "message": "File not found"
                    }
                    """;
        }
    }

    public static String getFileNameWithoutExtension(File file) {
        return PathUtil.getFileNameWithoutExtension(file);
    }
}
