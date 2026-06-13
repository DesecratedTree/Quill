package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.util.ProjectPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ItemDefsDumpMain {

    private static final int PROGRESS_INTERVAL = 250;

    private ItemDefsDumpMain() {
    }

    public static void main(String[] args) throws IOException {
        Path cachePath = args.length > 0 ? Paths.get(args[0]) : Paths.get("data", "cache");
        Path outputDir = args.length > 1 ? Paths.get(args[1]) : ProjectPaths.itemDefsDumpDir();
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
                String dump = ItemDefinitions.defs(itemId).dumpOpcodeText();
                Files.write(outputDir.resolve(itemId + ".txt"), dump.getBytes(StandardCharsets.UTF_8));
                dumped++;
            } catch (RuntimeException | IOException ex) {
                failed++;
                System.err.println("Failed to dump item defs " + itemId + ": " + ex.getMessage());
            }
            if ((itemId + 1) % PROGRESS_INTERVAL == 0 || itemId + 1 == itemCount) {
                System.out.println("Dumped " + (itemId + 1) + " / " + itemCount + " item defs");
            }
        }
        System.out.println("Item defs dump complete. Wrote " + dumped + " files to " + outputDir.toAbsolutePath()
                + (failed > 0 ? " (" + failed + " failed)" : ""));
    }
}
