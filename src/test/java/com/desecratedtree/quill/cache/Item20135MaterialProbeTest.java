package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.render.SoftwareModelRenderer;
import com.desecratedtree.quill.texture.MaterialDefinition;
import com.desecratedtree.quill.texture.MaterialLoader;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.texture.synth.TextureProgram;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Item20135MaterialProbeTest {
    @Test
    void inspectTorvaFullHelmMaterials() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        ItemDefinitions item = ItemDefinitions.getItemDefinitions(20135);
        RenderModel model = ModelDecoderAdapter.loadItemModel(item);
        assertNotNull(model);
        Set<Integer> textureIds = new HashSet<>();
        if (model.faceTextures != null) {
            for (int texture : model.faceTextures) if (texture >= 0) textureIds.add(texture);
        }
        System.err.println("ITEM 20135 name=" + item.name + " model=" + item.modelId
                + " faces=" + model.faceCount + " textures=" + textureIds
                + " recolors=" + (item.originalModelColors == null ? -1 : item.originalModelColors.length));
        for (int textureId : textureIds) {
            byte[] textureData = CacheManager.getTextureData(textureId);
            System.err.println("RAW " + textureId + "=" + (textureData == null ? "null" : hex(textureData)));
            if (textureData != null) {
                try {
                    TextureProgram program = new TextureProgram(textureData);
                    System.err.println("PROGRAM " + textureId + " sources=" + java.util.Arrays.toString(program.spriteFileIds())
                            + " main=" + program.mainColor().getClass().getSimpleName()
                            + " alpha=" + program.alphaLayer().getClass().getSimpleName()
                            + " boost=" + program.boostLayer().getClass().getSimpleName());
                    for (int i = 0; i < program.layers.length; i++) {
                        System.err.println("  LAYER " + i + " " + program.layers[i].getClass().getSimpleName()
                                + " id=" + program.layers[i].layerId + " mono=" + program.layers[i].mono
                                + " children=" + childNames(program.layers[i]));
                        System.err.println("  FIELDS " + fieldValues(program.layers[i]));
                    }
                    try {
                        float[] values = program.renderRgba(32, 32, 1.0, false, fileId -> null);
                        int min = 255, max = 0, chroma = 0;
                        for (int i = 0; i < values.length; i += 4) {
                            int red = Math.round(values[i + 2] * 255.0f);
                            int green = Math.round(values[i + 1] * 255.0f);
                            int blue = Math.round(values[i] * 255.0f);
                            min = Math.min(min, Math.min(red, Math.min(green, blue)));
                            max = Math.max(max, Math.max(red, Math.max(green, blue)));
                            chroma = Math.max(chroma, Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)));
                        }
                        System.err.println("PROGRAM RGBA min=" + min + " max=" + max + " chroma=" + chroma);
                    } catch (RuntimeException ex) {
                        System.err.println("PROGRAM RENDER ERROR " + ex);
                    }
                } catch (RuntimeException ex) {
                    System.err.println("PROGRAM ERROR " + ex);
                }
            }
            MaterialDefinition material = MaterialLoader.get(textureId);
            BufferedImage preview = TextureLoader.previewTexture(textureId);
            System.err.println("MATERIAL " + textureId + "=" + materialValues(material)
                    + " preview=" + (preview == null ? "null" : preview.getWidth() + "x" + preview.getHeight())
                    + " colors=" + (preview == null ? 0 : colors(preview)));
        }
        BufferedImage image = SoftwareModelRenderer.renderInventorySprite(item, 36, 32);
        assertTrue(imageColors(image) > 1, "Torva full helm should retain procedural material variation");
        assertTrue(image.getWidth() > 0);
    }

    private static int imageColors(BufferedImage image) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if ((pixel >>> 24) != 0) colors.add(pixel & 0xFFFFFF);
            }
        }
        return colors.size();
    }

    private static String childNames(com.desecratedtree.quill.texture.synth.TextureLayer layer) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < layer.children.length; i++) {
            if (i > 0) result.append(',');
            result.append(layer.children[i] == null ? "null" : layer.children[i].getClass().getSimpleName());
        }
        return result.append(']').toString();
    }

    private static String fieldValues(Object value) {
        StringBuilder result = new StringBuilder();
        for (java.lang.reflect.Field field : value.getClass().getDeclaredFields()) {
            if (!field.getName().startsWith("anInt") && !field.getName().startsWith("aShort")) continue;
            try {
                field.setAccessible(true);
                if (result.length() > 0) result.append(' ');
                result.append(field.getName()).append('=').append(field.get(value));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return result.toString();
    }

    private static String hex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte value : data) result.append(String.format("%02X", value & 0xFF));
        return result.toString();
    }

    private static int colors(BufferedImage image) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) colors.add(image.getRGB(x, y) & 0xFFFFFF);
            }
        }
        return colors.size();
    }

    private static String materialValues(MaterialDefinition material) {
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
}
