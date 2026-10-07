package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.texture.synth.TextureProgram;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Item6570TextureProbeTest {

    @Test
    void fireCapeFace20UsesVoidTextureSources() {
        Path cache = Paths.get("data", "cache");
        assertTrue(Files.isDirectory(cache), "634 cache fixture is unavailable");
        CacheManager.init(cache.toString());

        ItemDefinitions item = ItemDefinitions.getItemDefinitions(6570);
        RenderModel model = ModelDecoderAdapter.loadItemModel(item);
        assertNotNull(model);
        assertTrue(model.faceCount > 20);

        int textureId = model.faceTextures == null ? -1 : model.faceTextures[20];
        assertEquals(40, textureId, "fire cape face 20 should use texture 40");

        for (int candidate : new int[]{0, 1, 2, 40, 205, 236, 294}) {
            byte[] candidateData = CacheManager.getTextureData(candidate);
            if (candidateData != null) {
                System.err.println("TEXTURE " + candidate + " RAW " + rawBytes(candidateData));
            }
        }

        byte[] textureData = CacheManager.getTextureData(textureId);
        assertNotNull(textureData);
        assertTrue(textureData.length >= 6);
        System.err.println("TEXTURE 40 RAW " + rawBytes(textureData));
        TextureProgram program = new TextureProgram(textureData);
        System.err.println("TEXTURE 40 PROGRAM SOURCES=" + java.util.Arrays.toString(program.spriteFileIds()));
        assertTrue(program.spriteFileIds().length > 0, "texture 40 should reference Void sprite layers");
        BufferedImage preview = TextureLoader.previewTexture(textureId);
        assertNotNull(preview);
        System.err.println("TEXTURE 40 PREVIEW size=" + preview.getWidth() + "x" + preview.getHeight()
                + " colors=" + colors(preview));
        BufferedImage expectedSource = firstSprite(program.spriteFileIds()[0]);
        assertNotNull(expectedSource, "fire cape source sprite is unavailable");
        assertEquals(expectedSource.getWidth(), preview.getWidth());
        assertEquals(expectedSource.getHeight(), preview.getHeight());
        assertTrue(hasColoredPixel(preview), "fire cape texture 40 should decode colored source pixels");
        assertTrue(colors(preview).size() > 1, "fire cape texture 40 should preserve source sprite variation");
    }

    private static boolean contains(int[] values, int expected) {
        for (int value : values) {
            if (value == expected) {
                return true;
            }
        }
        return false;
    }

    private static BufferedImage firstSprite(int spriteId) {
        SpriteArchive archive = SpriteArchiveCodec.decode(CacheManager.getSpriteData(spriteId));
        return archive.sprites.isEmpty() ? null : archive.sprites.get(0).toBufferedImage();
    }

    private static String rawBytes(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) result.append(' ');
            result.append(String.format("%02X", data[i] & 0xFF));
        }
        return result.toString();
    }

    private static int unsignedShort(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    private static Set<Integer> colors(BufferedImage image) {
        Set<Integer> result = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                result.add(image.getRGB(x, y) & 0xFFFFFF);
            }
        }
        return result;
    }

    private static boolean hasColoredPixel(BufferedImage image) {
        for (int color : colors(image)) {
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            if (Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)) >= 20) {
                return true;
            }
        }
        return false;
    }
}
