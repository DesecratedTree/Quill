package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.texture.TextureDefinitionTable;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.util.ProjectPaths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class TextureDumpMain {

    private static final int PROGRESS_INTERVAL = 100;

    private TextureDumpMain() {
    }

    public static void main(String[] args) throws IOException {
        Path cachePath = args.length > 0 ? Paths.get(args[0]) : Paths.get("data", "cache");
        Path outputDir = args.length > 1 ? Paths.get(args[1]) : ProjectPaths.texturePreviewDumpDir();
        if (!Files.isDirectory(cachePath)) {
            throw new IllegalArgumentException("Cache path does not exist or is not a directory: " + cachePath.toAbsolutePath());
        }
        Files.createDirectories(outputDir);
        CacheManager.init(cachePath.toString());
        dumpTo(outputDir);
    }

    public static void dumpTo(Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        TextureLoader.clearCache();
        List<Integer> textureIds = TextureDefinitionTable.load().presentIds();
        int dumped = 0;
        int failed = 0;
        for (int i = 0; i < textureIds.size(); i++) {
            int textureId = textureIds.get(i);
            try {
                BufferedImage image = TextureLoader.previewTexture(textureId);
                if (image != null) {
                    ImageIO.write(image, "png", outputDir.resolve(textureId + ".png").toFile());
                    dumped++;
                }
            } catch (RuntimeException | IOException ex) {
                failed++;
                System.err.println("Failed to dump texture " + textureId + ": " + ex.getMessage());
            }
            if ((i + 1) % PROGRESS_INTERVAL == 0 || i + 1 == textureIds.size()) {
                System.out.println("Dumped " + (i + 1) + " / " + textureIds.size() + " textures");
            }
        }
        System.out.println("Texture dump complete. Wrote " + dumped + " files to " + outputDir.toAbsolutePath()
                + (failed > 0 ? " (" + failed + " failed)" : ""));
    }
}
