package com.desecratedtree.quill.texture;

import com.desecratedtree.quill.render.CacheColor;
import com.desecratedtree.quill.util.ProjectPaths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TextureLoader {

    private static final Map<Integer, BufferedImage> CACHE = new ConcurrentHashMap<>();

    private TextureLoader() {
    }

    public static BufferedImage loadTexture(int textureId) {
        if (textureId < 0) {
            return null;
        }
        return CACHE.computeIfAbsent(textureId, TextureLoader::decodeTexture);
    }

    public static BufferedImage previewTexture(int textureId) {
        if (textureId < 0) {
            return null;
        }
        BufferedImage override = loadOverrideTexture(textureId);
        if (override != null) {
            return override;
        }
        MaterialDefinition material = MaterialLoader.get(textureId);
        if (material == null) {
            return null;
        }
        int rgb = materialColor(material);
        if (material.field198 != 0 || material.field211 != 0) {
            return animatedTexture(textureId, rgb, material.smallTexture ? 64 : 128);
        }
        return solidTexture(rgb);
    }

    public static void clearCache() {
        CACHE.clear();
    }
    public static double scrollU(int textureId) {
        MaterialDefinition material = MaterialLoader.get(textureId);
        return scroll(textureId, material == null ? 0 : material.field198);
    }
    public static double scrollV(int textureId) {
        MaterialDefinition material = MaterialLoader.get(textureId);
        return scroll(textureId, material == null ? 0 : material.field211);
    }
    static boolean isNearWhiteMaterial(int textureId) {
        MaterialDefinition material = MaterialLoader.get(textureId);
        return material != null && isNearWhite(materialColor(material));
    }

    public static int previewScrollUValue(int textureId) {
        MaterialDefinition material = MaterialLoader.get(textureId);
        return material == null ? 0 : material.field198;
    }

    public static int previewScrollVValue(int textureId) {
        MaterialDefinition material = MaterialLoader.get(textureId);
        return material == null ? 0 : material.field211;
    }
    public static boolean hasOverrideTexture(int textureId) {
        return textureId >= 0 && overrideTextureFile(textureId).isFile();
    }

    private static BufferedImage decodeTexture(int textureId) {
        BufferedImage override = loadOverrideTexture(textureId);
        if (override != null) {
            return override;
        }
        return null;
    }

    private static BufferedImage loadOverrideTexture(int textureId) {
        File file = overrideTextureFile(textureId);
        if (!file.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (IOException ex) {
            return null;
        }
    }

    private static File overrideTextureFile(int textureId) {
        return ProjectPaths.textureDumpFile(textureId).toFile();
    }

    private static int materialColor(MaterialDefinition material) {
        int rgb = CacheColor.toRgb(material.averageColor);
        if (isNearWhite(rgb)) {
            int fieldColor = material.field206 & 0xFFFFFF;
            if (fieldColor != 0 && !isNearWhite(fieldColor)) {
                return fieldColor;
            }
        }
        return rgb;
    }
    static boolean isNearWhite(int rgb) {
        return ((rgb >> 16) & 0xFF) > 235 && ((rgb >> 8) & 0xFF) > 235 && (rgb & 0xFF) > 235;
    }

    private static double scroll(int textureId, byte speed) {
        if (speed == 0) {
            return 0.0;
        }
        MaterialDefinition material = MaterialLoader.get(textureId);
        int size = material != null && material.smallTexture ? 64 : 128;
        int period = Math.max(1, size * 50);
        return ((System.currentTimeMillis() % period) * speed) / (double) period;
    }

    private static BufferedImage solidTexture(int rgb) {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        int argb = 0xFF000000 | rgb;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }

    private static BufferedImage animatedTexture(int textureId, int rgb, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int accent = mix(rgb, 0xFFFFFF, 0.28);
        int shadow = mix(rgb, 0x000000, 0.22);
        int secondary = mix(rgb, 0xFFC040, 0.18);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int wave = Math.floorMod(x * 3 + y * 5 + textureId * 11, 37);
                int flame = Math.floorMod(x * 2 - y * 7 + textureId * 17, 53);
                int color = rgb;
                if (wave < 6) {
                    color = accent;
                } else if (flame < 8) {
                    color = secondary;
                } else if (wave > 30) {
                    color = shadow;
                }
                image.setRGB(x, y, 0xFF000000 | color);
            }
        }
        return image;
    }

    private static int mix(int rgb, int other, double amount) {
        double base = 1.0 - amount;
        int red = (int) (((rgb >> 16) & 0xFF) * base + ((other >> 16) & 0xFF) * amount);
        int green = (int) (((rgb >> 8) & 0xFF) * base + ((other >> 8) & 0xFF) * amount);
        int blue = (int) ((rgb & 0xFF) * base + (other & 0xFF) * amount);
        return (red << 16) | (green << 8) | blue;
    }
}
