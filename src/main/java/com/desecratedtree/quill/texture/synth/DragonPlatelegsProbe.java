package com.desecratedtree.quill.texture.synth;

import com.desecratedtree.quill.cache.CacheManager;

public final class DragonPlatelegsProbe {
    public static void main(String[] args) {
        CacheManager.init("data/cache");
        byte[] raw = CacheManager.getTextureData(249);
        TextureProgram program = new TextureProgram(raw);
        final int size = 8;
        TextureProvider provider = new TextureProvider() {
            @Override public SpriteRaster sprite(int fileId) { return null; }
            @Override public SpriteRaster texture(int textureId) {
                int[] pixels = new int[size * size];
                for (int y = 0; y < size; y++)
                    for (int x = 0; x < size; x++)
                        pixels[y * size + x] = 0xFF000000 | ((0x40 + x * 4) << 16) | ((0x10 + y * 4) << 8) | 0x08;
                return new SpriteRaster(pixels, size, size);
            }
        };
        TextureRender.spriteProvider = provider;
        TextureRender.setSize(size, size);
        for (TextureLayer layer : program.layers) layer.allocate(size, size, 0);
        TextureLayer main = program.mainColor();
        int[][] row = main.colorRows(0, TextureLayer.REQUEST_MAGIC);
        System.out.println("main(LayerSub29) y0 ch0[0..2]=" + row[0][0] + "," + row[0][1] + "," + row[0][2]
                + " ch1[0..2]=" + row[1][0] + "," + row[1][1] + "," + row[1][2]
                + " ch2[0..2]=" + row[2][0] + "," + row[2][1] + "," + row[2][2]);
        TextureLayer child = main.children.length > 0 ? main.children[0] : null;
        if (child != null) {
            int[] mono = child.monoRow(0, TextureLayer.REQUEST_MONO);
            System.out.println("child " + child.getClass().getSimpleName() + " mono y0 [0..2]=" + mono[0] + "," + mono[1] + "," + mono[2]);
            int[] ramped = program.layers[1].monoRow(0, TextureLayer.REQUEST_MONO);
            System.out.println("ramped(LayerSub14) y0 [0..2]=" + ramped[0] + "," + ramped[1] + "," + ramped[2]);
        }
        int[] boosted = program.layers[2].monoRow(0, TextureLayer.REQUEST_MONO);
        System.out.println("boost(LayerSub15) y0 [0..2]=" + boosted[0] + "," + boosted[1] + "," + boosted[2]);
        // Also inspect the real source if present in the cache: type-36 reads sprite of texture anInt9380=244.
        int[] frame = program.renderRgb(size, size, 1.0, false, provider);
        for (int y = 0; y < 2; y++) {
            System.out.println("frame y=" + y + " x0..2=" + String.format("%06X,%06X,%06X", frame[y * size] & 0xFFFFFF,
                    frame[y * size + 1] & 0xFFFFFF, frame[y * size + 2] & 0xFFFFFF));
        }
    }
}
