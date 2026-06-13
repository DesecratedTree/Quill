package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.render.IndexedSprite;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
import com.desecratedtree.quill.util.ProjectPaths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SpriteDumpMain {

    private static final int PROGRESS_INTERVAL = 100;

    private SpriteDumpMain() {
    }

    public static void main(String[] args) throws IOException {
        Path cachePath = args.length > 0 ? Paths.get(args[0]) : Paths.get("data", "cache");
        Path outputDir = args.length > 1 ? Paths.get(args[1]) : ProjectPaths.spriteDumpDir();
        if (!Files.isDirectory(cachePath)) {
            throw new IllegalArgumentException("Cache path does not exist or is not a directory: " + cachePath.toAbsolutePath());
        }
        Files.createDirectories(outputDir);
        CacheManager.init(cachePath.toString());
        dumpTo(outputDir);
    }

    public static void dumpTo(Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        int dumped = 0;
        int failed = 0;
        int[] groupIds = CacheManager.getSpriteIds();
        for (int i = 0; i < groupIds.length; i++) {
            int groupId = groupIds[i];
            try {
                SpriteArchive archive = SpriteArchiveCodec.decode(CacheManager.getSpriteData(groupId));
                for (int spriteIndex = 0; spriteIndex < archive.sprites.size(); spriteIndex++) {
                    IndexedSprite sprite = archive.sprites.get(spriteIndex);
                    BufferedImage image = sprite.toBufferedImage();
                    ImageIO.write(image, "png", outputDir.resolve(groupId + " - " + spriteIndex + ".png").toFile());
                    dumped++;
                }
            } catch (RuntimeException | IOException ex) {
                failed++;
                System.err.println("Failed to dump sprite group " + groupId + ": " + ex.getMessage());
            }
            if ((i + 1) % PROGRESS_INTERVAL == 0 || i + 1 == groupIds.length) {
                System.out.println("Dumped " + (i + 1) + " / " + groupIds.length + " sprite groups");
            }
        }
        System.out.println("Sprite dump complete. Wrote " + dumped + " files to " + outputDir.toAbsolutePath()
                + (failed > 0 ? " (" + failed + " groups failed)" : ""));
    }
}
