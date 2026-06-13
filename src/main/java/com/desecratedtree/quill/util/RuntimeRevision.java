package com.desecratedtree.quill.util;

import com.moandjiezana.toml.Toml;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class RuntimeRevision {

    private static int revision = 634;

    private static boolean osrsMode = false;

    private RuntimeRevision() {
    }

    public static void configure() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(RuntimeRevision.class.getClassLoader().getResourceAsStream("config.toml")),
                        StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Toml toml = new Toml().read(sb.toString());
            Boolean osrs = toml.getBoolean("osrs_enabled");
            osrsMode = osrs != null && osrs;
            Long rev = toml.getLong("revision");
            if (rev != null) {
                revision = rev.intValue();
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("RuntimeRevision: could not load config.toml, using default revision " + revision);
        }
    }

    public static int getRevision() {
        return revision;
    }

    public static boolean isOsrsMode() {
        return osrsMode;
    }
}
