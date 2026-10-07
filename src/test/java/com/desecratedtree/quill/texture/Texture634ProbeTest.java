package com.desecratedtree.quill.texture;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.render.IndexedSprite;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Texture634ProbeTest {
    @Test
    void animatedMaterialMetadataIsAvailableToItemPreviews() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        int animatedId = -1;
        for (int textureId : TextureDefinitionTable.load().presentIds()) {
            if (TextureLoader.isAnimatedTexture(textureId)) {
                animatedId = textureId;
                break;
            }
        }
        Assumptions.assumeTrue(animatedId >= 0, "634 cache has no animated material fixture");
        assertTrue(TextureLoader.previewScrollUValue(animatedId) != 0
                        || TextureLoader.previewScrollVValue(animatedId) != 0,
                "animated material must expose a non-zero scroll component");
    }

    @Test
    void inspectFirst634TextureSources() throws Exception {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        byte[] textureData = CacheManager.getTextureData(0);
        Assumptions.assumeTrue(textureData != null && textureData.length > 3);
        int count = textureData[3] & 255;
        int offset = 4;
        for (int i = 0; i < count; i++) {
            int sourceId = ((textureData[offset] & 255) << 8) | (textureData[offset + 1] & 255);
            offset += 2;
            byte[] spriteData = CacheManager.getSpriteData(sourceId);
            SpriteArchive archive = SpriteArchiveCodec.decode(spriteData);
            System.err.println("SOURCE " + sourceId + " bytes=" + (spriteData == null ? -1 : spriteData.length)
                    + " canvas=" + archive.canvasWidth + "x" + archive.canvasHeight + " sprites=" + archive.sprites.size());
            for (int spriteIndex = 0; spriteIndex < archive.sprites.size(); spriteIndex++) {
                IndexedSprite sprite = archive.sprites.get(spriteIndex);
                BufferedImage image = sprite.toBufferedImage();
                int first = image.getRGB(0, 0);
                int center = image.getRGB(Math.max(0, image.getWidth() / 2), Math.max(0, image.getHeight() / 2));
                System.err.println("  SPRITE " + spriteIndex + " size=" + image.getWidth() + "x" + image.getHeight()
                        + " palette=" + (sprite.palette == null ? -1 : sprite.palette.length)
                        + " alpha=" + (sprite.alpha == null ? 0 : 1) + " first=" + Integer.toHexString(first)
                        + " center=" + Integer.toHexString(center));
            }
        }
    }
}
