package com.desecratedtree.quill.texture.synth;

/**
 * Decoded texture program. Port of {@code Class348_Sub42_Sub5}: parses the
 * layer tree, wires child references, collects sprite/aux file ids and resolves
 * the three designated layers (colour, alpha, boost).
 */
public final class TextureProgram {

    public final TextureLayer[] layers;
    private final TextureLayer mainColor;
    private final TextureLayer alphaLayer;
    private final TextureLayer boostLayer;
    private final int[] spriteFileIds;
    private final int[] auxiliaryFileIds;
    /** Bytes consumed during parse (for record-format validation). */
    public final int consumed;

    public TextureProgram(byte[] record) {
        this(new TextureReader(record));
    }

    private TextureProgram(TextureReader reader) {
        int n = reader.readUnsignedByte();
        layers = new TextureLayer[n];
        int[][] childRefs = new int[n][];
        int spriteCount = 0;
        int auxCount = 0;
        for (int i = 0; i < n; i++) {
            TextureLayer layer = readLayer(reader);
            if (layer.spriteFileId() > 0) {
                spriteCount++;
            }
            if (layer.auxiliaryFileId() > 0) {
                auxCount++;
            }
            int childCount = layer.children.length;
            childRefs[i] = new int[childCount];
            for (int c = 0; c < childCount; c++) {
                childRefs[i][c] = reader.readUnsignedByte();
            }
            layers[i] = layer;
        }
        spriteFileIds = new int[spriteCount];
        auxiliaryFileIds = new int[auxCount];
        int nextSprite = 0;
        int nextAux = 0;
        for (int i = 0; i < n; i++) {
            TextureLayer layer = layers[i];
            int childCount = layer.children.length;
            for (int c = 0; c < childCount; c++) {
                layer.children[c] = layers[childRefs[i][c]];
            }
            int sprite = layer.spriteFileId();
            if (sprite > 0) {
                spriteFileIds[nextSprite++] = sprite;
            }
            int aux = layer.auxiliaryFileId();
            if (aux > 0) {
                auxiliaryFileIds[nextAux++] = aux;
            }
        }
        mainColor = layers[reader.readUnsignedByte()];
        alphaLayer = layers[reader.readUnsignedByte()];
        boostLayer = layers[reader.readUnsignedByte()];
        consumed = reader.position();
    }

    /** {@code Class348_Sub37.method3031} — parse one layer record. */
    public static TextureLayer readLayer(TextureReader reader) {
        reader.readUnsignedByte();
        int type = reader.readUnsignedByte();
        TextureLayer layer = TextureLayerType.create(type);
        layer.layerId = reader.readUnsignedByte();
        int paramCount = reader.readUnsignedByte();
        for (int i = 0; i < paramCount; i++) {
            int opcode = reader.readUnsignedByte();
            layer.readParams(reader, opcode);
        }
        layer.finalizeConfig();
        return layer;
    }

    public TextureLayer mainColor() {
        return mainColor;
    }

    public TextureLayer alphaLayer() {
        return alphaLayer;
    }

    public TextureLayer boostLayer() {
        return boostLayer;
    }

    public int[] spriteFileIds() {
        return spriteFileIds;
    }

    public int[] auxiliaryFileIds() {
        return auxiliaryFileIds;
    }

    // ---- rendering (ports of Class348_Sub42_Sub5.method3183/3185/3186) ----

    private static final int REQUEST_MAGIC = TextureLayer.REQUEST_MAGIC;
    private static final int REQUEST_MONO = TextureLayer.REQUEST_MONO;

    private static int[] channelFrom(int[][] rows) {
        return rows[0];
    }

    /**
     * {@code method3183} — opaque ARGB frame ({@code 0xAARRGGBB}).
     * Rows are emitted so {@code out[y * width + x]} is the pixel.
     */
    public int[] renderArgb(int width, int height, double gamma, SpriteProvider sprites) {
        TextureRender.spriteProvider = sprites;
        TextureRender.setSize(width, height);
        TextureRender.setGamma(gamma);
        for (TextureLayer layer : layers) {
            layer.allocate(width, height, -256);
        }
        int[] out = new int[width * height];
        int pos = 0;
        for (int y = 0; y < height; y++) {
            int[] r;
            int[] g;
            int[] b;
            if (mainColor.mono) {
                int[] m = mainColor.monoRow(y, REQUEST_MONO);
                r = m;
                g = m;
                b = m;
            } else {
                int[][] c = mainColor.colorRows(y, REQUEST_MAGIC);
                r = c[0];
                g = c[1];
                b = c[2];
            }
            int[] a;
            if (alphaLayer.mono) {
                a = alphaLayer.monoRow(y, REQUEST_MONO);
            } else {
                a = alphaLayer.colorRows(y, REQUEST_MAGIC)[0];
            }
            for (int x = 0; x < width; x++) {
                int rv = r[x] >> 4;
                if (rv > 255) rv = 255;
                if (rv < 0) rv = 0;
                int gv = g[x] >> 4;
                if (gv > 255) gv = 255;
                if (gv < 0) gv = 0;
                int bv = b[x] >> 4;
                if (bv > 255) bv = 255;
                gv = TextureRender.GAMMA[gv];
                if (bv < 0) bv = 0;
                rv = TextureRender.GAMMA[rv];
                bv = TextureRender.GAMMA[bv];
                int av;
                if (rv == 0 && gv == 0 && bv == 0) {
                    av = 0;
                } else {
                    av = a[x] >> 4;
                    if (av > 255) av = 255;
                    if (av < 0) av = 0;
                }
                out[pos++] = bv + (av << 24) + ((rv << 16) + (gv << 8));
            }
        }
        for (TextureLayer layer : layers) {
            layer.release();
        }
        return out;
    }

    /**
     * {@code method3185} — opaque RGB frame ({@code 0xFFRRGGBB}); rows may be
     * emitted bottom-up when {@code flipped}.
     */
    public int[] renderRgb(int width, int height, double gamma, boolean flipped, SpriteProvider sprites) {
        TextureRender.spriteProvider = sprites;
        TextureRender.setSize(width, height);
        TextureRender.setGamma(gamma);
        for (TextureLayer layer : layers) {
            layer.allocate(width, height, -256);
        }
        int[] out = new int[width * height];
        int start;
        int step;
        int end;
        if (flipped) {
            start = height - 1;
            step = -1;
            end = -1;
        } else {
            start = 0;
            step = 1;
            end = height;
        }
        int pos = 0;
        for (int y = 0; y < height; y++) {
            int[] r;
            int[] g;
            int[] b;
            if (mainColor.mono) {
                int[] m = mainColor.monoRow(y, REQUEST_MONO);
                r = m;
                g = m;
                b = m;
            } else {
                int[][] c = mainColor.colorRows(y, REQUEST_MAGIC);
                r = c[0];
                g = c[1];
                b = c[2];
            }
            for (int x = start; x != end; x += step) {
                int rv = r[x] >> 4;
                if (rv > 255) rv = 255;
                if (rv < 0) rv = 0;
                int gv = g[x] >> 4;
                if (gv > 255) gv = 255;
                if (gv < 0) gv = 0;
                int bv = b[x] >> 4;
                if (bv > 255) bv = 255;
                gv = TextureRender.GAMMA[gv];
                rv = TextureRender.GAMMA[rv];
                if (bv < 0) bv = 0;
                bv = TextureRender.GAMMA[bv];
                int rgb = (gv << 8) + (rv << 16) + bv;
                if (rgb != 0) {
                    rgb |= ~0xffffff;
                }
                out[pos++] = rgb;
            }
        }
        for (int i = 0; i < layers.length; i++) {
            layers[i].release();
        }
        return out;
    }

    /**
     * {@code method3186} — float RGBA frame, R,G,B,A per pixel (no gamma).
     * Colours are 0..1 scaled by the boost layer.
     */
    public float[] renderRgba(int width, int height, double gamma, boolean flipped, SpriteProvider sprites) {
        TextureRender.spriteProvider = sprites;
        TextureRender.setSize(width, height);
        TextureRender.setGamma(gamma);
        for (TextureLayer layer : layers) {
            layer.allocate(width, height, -256);
        }
        float[] out = new float[4 * width * height];
        int pos = 0;
        for (int y = 0; y < height; y++) {
            int[] r;
            int[] g;
            int[] b;
            if (mainColor.mono) {
                int[] m = mainColor.monoRow(y, REQUEST_MONO);
                r = m;
                g = m;
                b = m;
            } else {
                int[][] c = mainColor.colorRows(y, REQUEST_MAGIC);
                r = c[0];
                g = c[1];
                b = c[2];
            }
            int[] a;
            if (alphaLayer.mono) {
                a = alphaLayer.monoRow(y, REQUEST_MONO);
            } else {
                a = alphaLayer.colorRows(y, REQUEST_MAGIC)[0];
            }
            int[] boost;
            if (boostLayer.mono) {
                boost = boostLayer.monoRow(y, REQUEST_MONO);
            } else {
                boost = boostLayer.colorRows(y, REQUEST_MAGIC)[0];
            }
            if (flipped) {
                pos = y << 2;
            }
            for (int x = 0; x < width; x++) {
                float f = (float) a[x] / 4096.0F;
                float scale = ((31.0F * (float) boost[x] / 4096.0F + 1.0F) / 4096.0F);
                if (!(f < 0.0F)) {
                    if (f > 1.0F) f = 1.0F;
                } else {
                    f = 0.0F;
                }
                out[pos++] = (float) r[x] * scale;
                out[pos++] = scale * (float) g[x];
                out[pos++] = (float) b[x] * scale;
                out[pos++] = f;
                if (flipped) {
                    pos += -4 + (width << 2);
                }
            }
        }
        for (TextureLayer layer : layers) {
            layer.release();
        }
        return out;
    }
}