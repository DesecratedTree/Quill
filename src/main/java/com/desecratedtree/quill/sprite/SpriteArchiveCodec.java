package com.desecratedtree.quill.sprite;

import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.render.IndexedSprite;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpriteArchiveCodec {

    private SpriteArchiveCodec() {
    }

    public static SpriteArchive decode(byte[] data) {
        SpriteArchive archive = new SpriteArchive();
        if (data == null || data.length < 2) {
            return archive;
        }
        InputStream input = new InputStream(data);
        input.setOffset(data.length - 2);
        int size = input.readUnsignedShort();
        input.setOffset(data.length - 7 - size * 8);
        archive.canvasWidth = input.readUnsignedShort();
        archive.canvasHeight = input.readUnsignedShort();
        int paletteSize = input.readUnsignedByte() + 1;
        IndexedSprite[] sprites = new IndexedSprite[size];
        for (int i = 0; i < size; i++) {
            sprites[i] = new IndexedSprite();
            archive.sprites.add(sprites[i]);
        }
        for (IndexedSprite sprite : sprites) {
            sprite.offsetX = input.readUnsignedShort();
        }
        for (IndexedSprite sprite : sprites) {
            sprite.offsetY = input.readUnsignedShort();
        }
        for (IndexedSprite sprite : sprites) {
            sprite.width = input.readUnsignedShort();
        }
        for (IndexedSprite sprite : sprites) {
            sprite.height = input.readUnsignedShort();
        }
        for (IndexedSprite sprite : sprites) {
            sprite.deltaWidth = archive.canvasWidth - sprite.width - sprite.offsetX;
            sprite.deltaHeight = archive.canvasHeight - sprite.height - sprite.offsetY;
        }
        input.setOffset(data.length - 7 - size * 8 - (paletteSize - 1) * 3);
        int[] palette = new int[paletteSize];
        for (int i = 1; i < paletteSize; i++) {
            palette[i] = input.read24BitInt();
            if (palette[i] == 0) {
                palette[i] = 1;
            }
        }
        input.setOffset(0);
        for (IndexedSprite sprite : sprites) {
            sprite.palette = palette;
            int area = sprite.width * sprite.height;
            sprite.raster = new byte[area];
            int setting = input.readUnsignedByte();
            boolean columnMajor = (setting & 0x1) != 0;
            boolean hasAlpha = (setting & 0x2) != 0;
            if (!columnMajor) {
                for (int pixel = 0; pixel < area; pixel++) {
                    sprite.raster[pixel] = (byte) input.readByte();
                }
            } else {
                for (int x = 0; x < sprite.width; x++) {
                    for (int y = 0; y < sprite.height; y++) {
                        sprite.raster[x + y * sprite.width] = (byte) input.readByte();
                    }
                }
            }
            if (hasAlpha) {
                byte[] alpha = new byte[area];
                boolean transparent = false;
                if (!columnMajor) {
                    for (int pixel = 0; pixel < area; pixel++) {
                        alpha[pixel] = (byte) input.readByte();
                        transparent |= alpha[pixel] != (byte) 0xFF;
                    }
                } else {
                    for (int x = 0; x < sprite.width; x++) {
                        for (int y = 0; y < sprite.height; y++) {
                            int index = x + y * sprite.width;
                            alpha[index] = (byte) input.readByte();
                            transparent |= alpha[index] != (byte) 0xFF;
                        }
                    }
                }
                if (transparent) {
                    sprite.alpha = alpha;
                }
            }
        }
        return archive;
    }

    public static byte[] encode(SpriteArchive archive) {
        List<EncodedSprite> encodedSprites = new ArrayList<>();
        LinkedHashMap<Integer, Integer> paletteMap = new LinkedHashMap<>();
        paletteMap.put(0, 0);
        int canvasWidth = archive.canvasWidth;
        int canvasHeight = archive.canvasHeight;
        for (IndexedSprite sprite : archive.sprites) {
            BufferedImage image = sprite.toBufferedImage();
            canvasWidth = Math.max(canvasWidth, sprite.offsetX + sprite.width + sprite.deltaWidth);
            canvasHeight = Math.max(canvasHeight, sprite.offsetY + sprite.height + sprite.deltaHeight);
            encodedSprites.add(encodeSprite(image, sprite, paletteMap));
        }
        if (paletteMap.size() > 256) {
            throw new IllegalArgumentException("Sprite group uses more than 255 indexed colors.");
        }
        OutputStream output = new OutputStream();
        for (EncodedSprite sprite : encodedSprites) {
            output.writeByte(sprite.hasAlpha ? 0x2 : 0x0);
            output.writeBytes(sprite.raster);
            if (sprite.hasAlpha) {
                output.writeBytes(sprite.alpha);
            }
        }
        for (Map.Entry<Integer, Integer> entry : paletteMap.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            output.write24BitInt(entry.getKey() & 0xFFFFFF);
        }
        output.writeShort(canvasWidth);
        output.writeShort(canvasHeight);
        output.writeByte(paletteMap.size() - 1);
        for (IndexedSprite sprite : archive.sprites) {
            output.writeShort(sprite.offsetX);
        }
        for (IndexedSprite sprite : archive.sprites) {
            output.writeShort(sprite.offsetY);
        }
        for (IndexedSprite sprite : archive.sprites) {
            output.writeShort(sprite.width);
        }
        for (IndexedSprite sprite : archive.sprites) {
            output.writeShort(sprite.height);
        }
        output.writeShort(archive.sprites.size());
        return output.toByteArray();
    }

    public static IndexedSprite fromBufferedImage(BufferedImage image) {
        IndexedSprite sprite = new IndexedSprite();
        sprite.width = image.getWidth();
        sprite.height = image.getHeight();
        sprite.offsetX = 0;
        sprite.offsetY = 0;
        sprite.deltaWidth = 0;
        sprite.deltaHeight = 0;
        sprite.palette = new int[]{0};
        sprite.raster = new byte[sprite.width * sprite.height];
        return sprite;
    }

    private static EncodedSprite encodeSprite(BufferedImage image, IndexedSprite sprite, LinkedHashMap<Integer, Integer> paletteMap) {
        int area = image.getWidth() * image.getHeight();
        byte[] raster = new byte[area];
        byte[] alpha = new byte[area];
        boolean hasAlpha = false;
        int[] rgb = new int[area];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
        for (int i = 0; i < area; i++) {
            Color color = new Color(rgb[i], true);
            int alphaValue = color.getAlpha();
            int rgb24 = alphaValue == 0 ? 0 : (new Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB() & 0xFFFFFF);
            Integer paletteIndex = paletteMap.get(rgb24);
            if (paletteIndex == null) {
                paletteIndex = paletteMap.size();
                paletteMap.put(rgb24, paletteIndex);
            }
            raster[i] = (byte) (paletteIndex & 0xFF);
            alpha[i] = (byte) alphaValue;
            hasAlpha |= alphaValue != 255;
        }
        EncodedSprite encoded = new EncodedSprite();
        encoded.raster = raster;
        encoded.alpha = hasAlpha ? alpha : null;
        encoded.hasAlpha = hasAlpha;
        sprite.width = image.getWidth();
        sprite.height = image.getHeight();
        return encoded;
    }

    public static BufferedImage normalizeSpriteImage(BufferedImage image) {
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        converted.getGraphics().drawImage(image, 0, 0, null);
        return converted;
    }

    public static void validatePaletteLimit(List<BufferedImage> images) {
        LinkedHashMap<Integer, Integer> paletteMap = new LinkedHashMap<>();
        paletteMap.put(0, 0);
        for (BufferedImage image : images) {
            if (image == null) {
                continue;
            }
            int area = image.getWidth() * image.getHeight();
            int[] rgb = new int[area];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
            for (int argb : rgb) {
                Color color = new Color(argb, true);
                int rgb24 = color.getAlpha() == 0 ? 0
                        : (new Color(color.getRed(), color.getGreen(), color.getBlue()).getRGB() & 0xFFFFFF);
                if (!paletteMap.containsKey(rgb24)) {
                    paletteMap.put(rgb24, paletteMap.size());
                    if (paletteMap.size() > 256) {
                        throw new IllegalArgumentException("Sprite group uses more than 255 indexed colors.");
                    }
                }
            }
        }
    }

    private static final class EncodedSprite {

        private byte[] raster;

        private byte[] alpha;

        private boolean hasAlpha;
    }
}
