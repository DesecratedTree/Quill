package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.render.SoftwareModelRenderer;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.render.IndexedSprite;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class Item19037ProbeTest {
    @Test
    void inspectItem19037() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        ItemDefinitions item = ItemDefinitions.getItemDefinitions(19037);
        RenderModel base = ModelDecoderAdapter.loadModel(item.modelId);
        RenderModel model = ModelDecoderAdapter.loadItemModel(item);
        System.err.println("ITEM id=" + item.id + " name=" + item.name + " model=" + item.modelId
                + " recolors=" + pairs(item.originalModelColors, item.modifiedModelColors)
                + " retextures=" + pairs(item.originalTextureIds, item.modifiedTextureIds)
                + " baseFaces=" + (base == null ? -1 : base.faceCount)
                + " renderedFaces=" + (model == null ? -1 : model.faceCount));
        if (base != null && model != null) {
            Set<Integer> baseColors = new HashSet<>();
            Set<Integer> renderedColors = new HashSet<>();
            for (short color : base.faceColors) baseColors.add(color & 0xFFFF);
            for (short color : model.faceColors) renderedColors.add(color & 0xFFFF);
            int texturedFaces = 0;
            for (int texture : model.faceTextures == null ? new int[0] : model.faceTextures) {
                if (texture >= 0) texturedFaces++;
            }
            Set<Integer> textureIds = new HashSet<>();
            for (int texture : model.faceTextures == null ? new int[0] : model.faceTextures) {
                if (texture >= 0) textureIds.add(texture);
            }
            System.err.println("TEXTURES=" + texturedFaces + " textureArray="
                    + (model.faceTextures == null ? "null" : model.faceTextures.length)
                    + " ids=" + textureIds);
            System.err.println("BASE COLORS=" + baseColors);
            System.err.println("RENDERED COLORS=" + renderedColors);
        }
        for (int textureId : new int[]{294, 236, 205}) {
            System.err.println("  MATERIAL=" + materialValues(com.desecratedtree.quill.texture.MaterialLoader.get(textureId)));
            byte[] textureData = CacheManager.getTextureData(textureId);
            StringBuilder bytes = new StringBuilder();
            for (int i = 0; i < (textureData == null ? 0 : textureData.length); i++) {
                if (i > 0) bytes.append(' ');
                bytes.append(String.format("%02X", textureData[i] & 0xFF));
            }
            BufferedImage texture = TextureLoader.previewTexture(textureId);
            Set<Integer> textureColors = new HashSet<>();
            if (texture != null) {
                for (int y = 0; y < texture.getHeight(); y++) {
                    for (int x = 0; x < texture.getWidth(); x++) {
                        if ((texture.getRGB(x, y) >>> 24) != 0) textureColors.add(texture.getRGB(x, y) & 0xFFFFFF);
                    }
                }
            }
            BufferedImage texturedPreview = TextureLoader.previewTexture(236);
            assertNotNull(texturedPreview, "item 19037 texture 236 has no preview");
            assertTrue(texturedPreview.getWidth() > 0 && texturedPreview.getHeight() > 0,
                    "item 19037 texture 236 should decode to a non-empty image");
            System.err.println("TEXTURE id=" + textureId + " bytes="
                    + (textureData == null ? -1 : textureData.length) + " raw=" + bytes
                    + " preview=" + (texture == null ? "null" : texture.getWidth() + "x" + texture.getHeight())
                    + " colors=" + textureColors.size());
            if (textureData != null && textureData.length >= 2) {
                int sourceId = ((textureData[0] & 0xFF) << 8) | (textureData[1] & 0xFF);
                SpriteArchive source = SpriteArchiveCodec.decode(CacheManager.getSpriteData(sourceId));
                IndexedSprite sprite = source.sprites.isEmpty() ? null : source.sprites.get(0);
                System.err.println("  CORRECT SOURCE id=" + sourceId + " size="
                        + (sprite == null ? "null" : sprite.width + "x" + sprite.height)
                        + " palette=" + (sprite == null || sprite.palette == null ? -1 : sprite.palette.length));
                if (sprite != null) {
                    StringBuilder palette = new StringBuilder();
                    for (int color : sprite.palette) {
                        if (palette.length() > 0) palette.append(',');
                        palette.append(String.format("%06X", color & 0xFFFFFF));
                    }
                    System.err.println("  CORRECT PALETTE=" + palette);
                }
            }
            if (textureData != null && textureData.length > 3) {
                int sourceCount = textureData[3] & 0xFF;
                for (int i = 0; i < sourceCount; i++) {
                    int offset = 4 + i * 2;
                    if (offset + 1 >= textureData.length) break;
                    int sourceId = ((textureData[offset] & 0xFF) << 8) | (textureData[offset + 1] & 0xFF);
                    SpriteArchive source = SpriteArchiveCodec.decode(CacheManager.getSpriteData(sourceId));
                    IndexedSprite sprite = source.sprites.isEmpty() ? null : source.sprites.get(0);
                    if (sprite != null) {
                        StringBuilder palette = new StringBuilder();
                        for (int color : sprite.palette) {
                            if (palette.length() > 0) palette.append(',');
                            palette.append(String.format("%06X", color & 0xFFFFFF));
                        }
                        System.err.println("    PALETTE=" + palette);
                    }
                }
            }
        }

        BufferedImage image = SoftwareModelRenderer.renderInventorySprite(item, 36, 32);
        assertTrue(image.getWidth() > 0, "item 19037 render should produce an image");
        Set<Integer> pixels = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) pixels.add(image.getRGB(x, y) & 0xFFFFFF);
            }
        }
        System.err.println("PIXELS count=" + pixels.size() + " colors=" + pixels);
    }

    private static boolean hasColoredPixel(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                if (Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)) >= 20) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String materialValues(Object material) {
        if (material == null) return "null";
        StringBuilder result = new StringBuilder();
        for (String fieldName : new String[]{"averageColor", "field198", "field211", "field216", "field201", "field213", "field202", "field205", "field203", "field206", "field200", "smallTexture", "lowDetail"}) {
            try {
                java.lang.reflect.Field field = material.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                if (result.length() > 0) result.append(' ');
                result.append(fieldName).append('=').append(field.get(material));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return result.toString();
    }
    private static String pairs(int[] from, int[] to) {
        if (from == null || to == null) return "null";
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < Math.min(from.length, to.length); i++) {
            if (i > 0) result.append(", ");
            result.append((from[i] & 0xFFFF)).append("->").append(to[i] & 0xFFFF);
        }
        return result.append(']').toString();
    }

    private static String pairs(short[] from, int[] to) {
        if (from == null || to == null) return "null";
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < Math.min(from.length, to.length); i++) {
            if (i > 0) result.append(", ");
            result.append((from[i] & 0xFFFF)).append("->").append(to[i] & 0xFFFF);
        }
        return result.append(']').toString();
    }
}
