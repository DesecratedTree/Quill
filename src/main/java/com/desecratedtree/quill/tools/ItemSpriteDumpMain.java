package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.SoftwareModelRenderer;
import com.desecratedtree.quill.util.ProjectPaths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ItemSpriteDumpMain {

    private static final int PROGRESS_INTERVAL = 250;

    private static final int SPRITE_WIDTH = 36;

    private static final int SPRITE_HEIGHT = 32;

    private ItemSpriteDumpMain() {
    }

    public static void main(String[] args) throws IOException {
        Path cachePath = args.length > 0 ? Paths.get(args[0]) : Paths.get("data", "cache");
        Path outputDir = args.length > 1 ? Paths.get(args[1]) : ProjectPaths.itemSpriteDumpDir();
        if (!Files.isDirectory(cachePath)) {
            throw new IllegalArgumentException("Cache path does not exist or is not a directory: " + cachePath.toAbsolutePath());
        }
        Files.createDirectories(outputDir);
        CacheManager.init(cachePath.toString());
        dumpTo(outputDir);
    }

    public static void dumpTo(Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        ItemDefinitions.clearItemsDefinitions();
        int dumped = 0;
        int failed = 0;
        int itemCount = CacheManager.getItemCount();
        for (int itemId = 0; itemId < itemCount; itemId++) {
            try {
                ItemDefinitions item = ItemDefinitions.defs(itemId);
                BufferedImage sprite = SoftwareModelRenderer.renderInventorySprite(item, SPRITE_WIDTH, SPRITE_HEIGHT);
                ImageIO.write(sprite, "png", outputDir.resolve(itemId + ".png").toFile());
                dumped++;
                if (item.stackIds != null) {
                    for (int i = 0; i < item.stackIds.length; i++) {
                        int stackItemId = item.stackIds[i];
                        if (stackItemId <= 0) continue;
                        int amount = item.stackAmounts != null && i < item.stackAmounts.length ? item.stackAmounts[i] : 0;
                        ItemDefinitions stackDef = ItemDefinitions.getItemDefinitions(stackItemId);
                        if (stackDef == null || stackDef.modelId < 0) continue;
                        ItemDefinitions preview = new ItemDefinitions(itemId, false);
                        preview.modelId = stackDef.modelId;
                        preview.modelZoom = stackDef.modelZoom;
                        preview.modelRotation1 = stackDef.modelRotation1;
                        preview.modelRotation2 = stackDef.modelRotation2;
                        preview.modelOffset1 = stackDef.modelOffset1;
                        preview.modelOffset2 = stackDef.modelOffset2;
                        preview.originalModelColors = stackDef.originalModelColors;
                        preview.modifiedModelColors = stackDef.modifiedModelColors;
                        preview.originalTextureIds = item.originalTextureIds;
                        preview.modifiedTextureIds = item.modifiedTextureIds;
                        BufferedImage stackSprite = SoftwareModelRenderer.renderInventorySprite(preview, SPRITE_WIDTH, SPRITE_HEIGHT);
                        ImageIO.write(stackSprite, "png", outputDir.resolve(itemId + "-" + amount + "x.png").toFile());
                        dumped++;
                    }
                }
            } catch (RuntimeException | IOException ex) {
                failed++;
                System.err.println("Failed to dump item sprite " + itemId + ": " + ex.getMessage());
            }
            if ((itemId + 1) % PROGRESS_INTERVAL == 0 || itemId + 1 == itemCount) {
                System.out.println("Dumped " + (itemId + 1) + " / " + itemCount + " item sprites");
            }
        }
        System.out.println("Item sprite dump complete. Wrote " + dumped + " sprites to " + outputDir.toAbsolutePath()
                + (failed > 0 ? " (" + failed + " failed)" : ""));
    }
}
