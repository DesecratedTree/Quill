package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.IndexedSprite;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
import com.desecratedtree.quill.texture.TextureDefinitionTable;
import com.desecratedtree.quill.texture.TextureLoader;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Cache634ProbeTest {

    @Test
    void representative634ArchivesDecodeReadOnly() {
        Path cachePath = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cachePath), "634 cache fixture is unavailable");
        CacheManager.init(cachePath.toString());

        assertTrue(CacheManager.getArchiveIds(19).length > 0, "item index is empty");
        assertTrue(CacheManager.getSpriteIds().length > 0, "sprite index is empty");
        assertTrue(CacheManager.getMaterialIds().length > 0, "material index is empty");

        int itemId = firstFileBackedId(19, 8);
        ItemDefinitions item = ItemDefinitions.getItemDefinitions(itemId);
        assertNotNull(item);
        assertTrue(item.isLoaded(), "representative item did not decode");

        int spriteGroup = CacheManager.getSpriteIds()[0];
        SpriteArchive spriteArchive = SpriteArchiveCodec.decode(CacheManager.getSpriteData(spriteGroup));
        assertTrue(!spriteArchive.sprites.isEmpty(), "representative sprite group is empty");
        IndexedSprite sprite = spriteArchive.sprites.get(0);
        BufferedImage spriteImage = sprite.toBufferedImage();
        assertTrue(spriteImage.getWidth() > 0 && spriteImage.getHeight() > 0);

        TextureDefinitionTable textureDefinitions = TextureDefinitionTable.load();
        if (!textureDefinitions.presentIds().isEmpty()) {
            BufferedImage texture = TextureLoader.previewTexture(textureDefinitions.presentIds().get(0));
            assertNotNull(texture, "representative texture has no preview");
            assertTrue(texture.getWidth() > 0 && texture.getHeight() > 0);
        }
    }

    private static int firstFileBackedId(int index, int archive) {
        int[] files = CacheManager.getFileIds(index, archive);
        assertTrue(files.length > 0, "archive " + archive + " is empty in index " + index);
        return (archive << 8) | files[0];
    }
}
