package com.desecratedtree.quill.texture;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.render.CacheColor;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
import com.desecratedtree.quill.texture.synth.SpriteRaster;
import com.desecratedtree.quill.texture.synth.SpriteProvider;
import com.desecratedtree.quill.texture.synth.TextureProgram;
import com.desecratedtree.quill.texture.synth.TextureProvider;
import com.desecratedtree.quill.util.ProjectPaths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;

public final class TextureLoader {

    private static final Map<Integer, BufferedImage> CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, SpriteRaster> SPRITE_CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Set<Integer>> DECODING = ThreadLocal.withInitial(HashSet::new);

    private TextureLoader() {
    }

    public static BufferedImage loadTexture(int textureId) {
        if (textureId < 0) {
            return null;
        }
        BufferedImage cached = CACHE.get(textureId);
        if (cached != null) {
            return cached;
        }
        Set<Integer> decoding = DECODING.get();
        if (!decoding.add(textureId)) {
            // A procedural texture can reference another texture through a
            // texture layer. Cyclic references must resolve as missing rather
            // than recursively entering ConcurrentHashMap.computeIfAbsent.
            return null;
        }
        try {
            BufferedImage decoded = decodeTexture(textureId);
            if (decoded != null) {
                BufferedImage existing = CACHE.putIfAbsent(textureId, decoded);
                return existing == null ? decoded : existing;
            }
            return null;
        } finally {
            decoding.remove(textureId);
            if (decoding.isEmpty()) {
                DECODING.remove();
            }
        }
    }

    public static BufferedImage previewTexture(int textureId) {
        if (textureId < 0) {
            return null;
        }
        BufferedImage decoded = loadTexture(textureId);
        if (decoded != null) {
            return decoded;
        }
        MaterialDefinition material = MaterialLoader.get(textureId);
        if (material == null) {
            return null;
        }
        return solidTexture(materialColor(material));
    }

    public static void clearCache() {
        CACHE.clear();
        SPRITE_CACHE.clear();
        MaterialLoader.clearCache();
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

    /** Returns whether a model contains a material whose UVs move over time. */
    public static boolean hasAnimatedTextures(RenderModel model) {
        if (model == null || model.faceTextures == null) {
            return false;
        }
        for (int textureId : model.faceTextures) {
            if (isAnimatedTexture(textureId)) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether the cache material declares a texture scroll speed. */
    public static boolean isAnimatedTexture(int textureId) {
        if (textureId < 0) {
            return false;
        }
        MaterialDefinition material = MaterialLoader.get(textureId);
        return material != null && (material.field198 != 0 || material.field211 != 0);
    }

    private static BufferedImage decodeTexture(int textureId) {
        BufferedImage override = loadOverrideTexture(textureId);
        if (override != null) {
            return override;
        }
        byte[] textureData = CacheManager.getTextureData(textureId);
        if (textureData == null || textureData.length < 2) {
            return null;
        }
        try {
            MaterialDefinition material = MaterialLoader.get(textureId);
            return material == null ? null : renderProceduralProgram(textureData, material);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static int[] sourceSpriteIds(byte[] textureData) {
        // Index 9 contains Void's texture programs. The first byte is the
        // layer count; byte 3 is part of the first layer header, not a sprite
        // count. Sprite IDs are parameters of type-39/18 sprite layers.
        try {
            return new TextureProgram(textureData).spriteFileIds();
        } catch (RuntimeException ex) {
            return new int[0];
        }
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
        // The client advances texture coordinates in cache-size units. Keep
        // this value continuous so callers can repaint a preview without
        // rebuilding the decoded texture raster.
        int period = Math.max(1, size * 50);
        return ((System.currentTimeMillis() % period) * speed) / (double) period;
    }

    private static BufferedImage renderProceduralProgram(byte[] record, MaterialDefinition material) {
        int size = material.smallTexture ? 64 : 128;
        try {
            TextureProgram program = new TextureProgram(record);
            // Type-36 layers can reference another procedural texture. Decode
            // those dependencies before rendering: TextureRender is a shared
            // client-style context, so rendering a dependency while the parent
            // is mid-frame would overwrite the parent buffers and coordinates.
            for (int dependencyId : program.auxiliaryFileIds()) {
                loadTexture(dependencyId);
            }
            TextureProvider provider = spriteProvider();
            int[] pixels;
            // Class354.method3467 selects method3183 for type 2 or when the
            // material blend mode is not 1/7; otherwise it selects method3185.
            if (material.field200 == 2 || material.field213 != 1 && material.field213 != 7) {
                pixels = program.renderArgb(size, size, 1.0, provider);
            } else {
                pixels = program.renderRgb(size, size, 1.0, false, provider);
            }
            pixels = applyMaterialTransform(pixels, material);
            // Preserve the auxiliary material contribution for records whose
            // client program uses it as a palette source. The renderer still
            // decides whether the final result is a grayscale mask.
            if (program.auxiliaryFileIds().length > 0) {
                pixels = applyAuxiliaryMaterialTint(pixels, program.auxiliaryFileIds());
            }
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, size, size, pixels, 0, size);
            return image;
        } catch (RuntimeException ex) {
            return proceduralMaterialTexture(0, record, material);
        }
    }

    private static int[] applyAuxiliaryMaterialTint(int[] pixels, int[] auxiliaryIds) {
        int tint = 0;
        for (int auxiliaryId : auxiliaryIds) {
            MaterialDefinition auxiliary = MaterialLoader.get(auxiliaryId);
            if (auxiliary != null) {
                tint ^= auxiliary.averageColor;
            }
        }
        if (tint == 0) {
            return pixels;
        }
        int hue = tint >> 10 & 0x3F;
        int saturation = tint >> 7 & 0x07;
        if (saturation == 0) {
            hue = Math.floorMod(tint * 13, 64);
            saturation = 3;
        }
        int[] transformed = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int rgb = pixel & 0xFFFFFF;
            float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
            float outHue = Math.floorMod(Math.round(hsb[0] * 64.0f) + hue, 64) / 64.0f;
            float outSaturation = Math.max(hsb[1], saturation / 8.0f);
            transformed[i] = (pixel & 0xFF000000)
                    | (java.awt.Color.HSBtoRGB(outHue, outSaturation, hsb[2]) & 0xFFFFFF);
        }
        return transformed;
    }

    /** Applies the cache material's HSL tint to the synthesized RGB frame. */
    private static int[] applyMaterialTransform(int[] pixels, MaterialDefinition material) {
        int hueShift = material.field201;
        int saturationShift = material.field216;
        int lightnessShift = material.field202;
        if (hueShift == 0 && saturationShift == 0 && lightnessShift == 0) {
            return pixels;
        }
        int[] transformed = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int rgb = pixel & 0xFFFFFF;
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
            float hue = Math.floorMod(Math.round(hsb[0] * 64.0f) + hueShift, 64) / 64.0f;
            float saturation = Math.max(0.0f, Math.min(1.0f, hsb[1] + saturationShift / 8.0f));
            float brightness = Math.max(0.0f, Math.min(1.0f, hsb[2] + lightnessShift / 128.0f));
            int tinted = java.awt.Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
            transformed[i] = (pixel & 0xFF000000) | tinted;
        }
        return transformed;
    }

    private static TextureProvider spriteProvider() {
        return new TextureProvider() {
            @Override
            public SpriteRaster sprite(int fileId) {
                return SPRITE_CACHE.computeIfAbsent(fileId, TextureLoader::decodeSprite);
            }

            @Override
            public SpriteRaster texture(int textureId) {
                BufferedImage image = loadTexture(textureId);
                if (image == null) {
                    return null;
                }
                int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
                return new SpriteRaster(pixels, image.getWidth(), image.getHeight());
            }
        };
    }

    private static SpriteRaster decodeSprite(int fileId) {
        byte[] data = CacheManager.getSpriteData(fileId);
        if (data == null) {
            return null;
        }
        try {
            SpriteArchive archive = SpriteArchiveCodec.decode(data);
            if (archive.sprites.isEmpty()) {
                return null;
            }
            com.desecratedtree.quill.render.IndexedSprite sprite = archive.sprites.get(0);
            BufferedImage source = sprite.toBufferedImage();
            int width = Math.max(1, archive.canvasWidth);
            int height = Math.max(1, archive.canvasHeight);
            int[] pixels = new int[width * height];
            for (int y = 0; y < source.getHeight(); y++) {
                int targetY = sprite.offsetY + y;
                if (targetY < 0 || targetY >= height) continue;
                for (int x = 0; x < source.getWidth(); x++) {
                    int targetX = sprite.offsetX + x;
                    if (targetX < 0 || targetX >= width) continue;
                    pixels[targetY * width + targetX] = source.getRGB(x, y);
                }
            }
            return new SpriteRaster(pixels, width, height);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static BufferedImage proceduralMaterialTexture(int textureId, byte[] program, MaterialDefinition material) {
        int size = material.smallTexture ? 64 : 128;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int seed = textureId * 0x45D9F3B;
        for (byte value : program) {
            seed = seed * 31 + (value & 0xFF);
        }
        int packed = material.averageColor & 0xFFFF;
        int baseLightness = packed & 0x7F;
        int[] palette = materialPalette(program);
        int dark = palette[0];
        int light = palette[1];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int coarse = noise(seed, x >> 4, y >> 4);
                int fine = noise(seed ^ 0x7F4A7C15, x >> 2, y >> 2);
                int amount = Math.max(0, Math.min(255, coarse + (fine - 128) / 3));
                int rgb;
                if (palette[2] != 0) {
                    rgb = interpolateRgb(dark, light, amount);
                } else {
                    int lightness = clampMaterialLightness(baseLightness + (coarse - 128) / 3 + (fine - 128) / 8);
                    rgb = CacheColor.toRgb((packed & 0xFF80) | lightness);
                }
                image.setRGB(x, y, 0xFF000000 | rgb);
            }
        }
        return image;
    }

    private static int[] materialPalette(byte[] program) {
        int darkest = 0xFFFFFF;
        int lightest = 0;
        boolean found = false;
        for (int i = 0; i + 2 < program.length; i++) {
            int r = program[i] & 0xFF;
            int g = program[i + 1] & 0xFF;
            int b = program[i + 2] & 0xFF;
            int rgb = (r << 16) | (g << 8) | b;
            int luminance = (r * 30 + g * 59 + b * 11) / 100;
            // Void gradient records store useful RGB points as repeated channel
            // triplets (for example 9B 9B 9B and FF FF FF in Torva 724).
            if (luminance >= 24 && (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) <= 12)) {
                darkest = Math.min(darkest, rgb);
                lightest = Math.max(lightest, rgb);
                found = true;
            }
        }
        return found && darkest != lightest
                ? new int[]{darkest, lightest, 1}
                : new int[]{0, 0, 0};
    }

    private static int interpolateRgb(int first, int second, int amount) {
        int r = (((first >> 16) & 0xFF) * (255 - amount) + ((second >> 16) & 0xFF) * amount) / 255;
        int g = (((first >> 8) & 0xFF) * (255 - amount) + ((second >> 8) & 0xFF) * amount) / 255;
        int b = ((first & 0xFF) * (255 - amount) + (second & 0xFF) * amount) / 255;
        return (r << 16) | (g << 8) | b;
    }

    private static int noise(int seed, int x, int y) {
        int value = seed ^ (x * 0x1F123BB5) ^ (y * 0x5F356495);
        value = (value ^ (value >>> 16)) * 0x45D9F3B;
        value = (value ^ (value >>> 16)) * 0x45D9F3B;
        return (value ^ (value >>> 16)) & 0xFF;
    }

    private static int clampMaterialLightness(int value) {
        return Math.max(2, Math.min(126, value));
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
