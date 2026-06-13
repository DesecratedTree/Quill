package com.desecratedtree.quill.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ProjectPaths {

    private static final Path DUMP_ROOT = Paths.get("dump");

    private static final Path LEGACY_DUMP_ROOT = Paths.get("dumps");

    private ProjectPaths() {
    }

    public static Path itemSpriteDumpDir() {
        return DUMP_ROOT.resolve("itemsprites");
    }

    public static Path itemDefsDumpDir() {
        return DUMP_ROOT.resolve("itemdefs");
    }

    public static Path spriteDumpDir() {
        return DUMP_ROOT.resolve("sprites");
    }

    public static Path textureDumpDir() {
        return LEGACY_DUMP_ROOT.resolve("textures");
    }

    public static Path texturePreviewDumpDir() {
        return DUMP_ROOT.resolve("textures");
    }

    public static Path textureDumpFile(int textureId) {
        return textureDumpDir().resolve(textureId + ".png");
    }
}
