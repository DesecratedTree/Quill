package com.desecratedtree.quill.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public final class Utilities {

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

    private Utilities() {
    }

    public static boolean isDarkMode() {
        try {
            if (OS_NAME.contains("win")) {
                return readCommandOutput(
                        "reg query HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize /v AppsUseLightTheme"
                ).contains("0x0");
            }
            if (OS_NAME.contains("mac")) {
                return readCommandOutput("defaults read -g AppleInterfaceStyle").toLowerCase(Locale.ROOT).contains("dark");
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static String readCommandOutput(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        try (Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
