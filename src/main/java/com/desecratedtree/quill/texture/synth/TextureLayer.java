package com.desecratedtree.quill.texture.synth;

/**
 * Base class for 634 texture layers. Port of {@code Class348_Sub40}.
 * Field names match the reference deobfuscation where it helps transliteration.
 */
public abstract class TextureLayer {

    public static final int REQUEST_MAGIC = -1564599039;
    public static final int REQUEST_MONO = 255;

    /** {@code aClass348_Sub40Array7031} — child layers. */
    public TextureLayer[] children;
    /** {@code anInt7036} — layer id, also used as plane count on allocate. */
    public int layerId;
    /** {@code aBoolean7045} — true when the layer is monochrome-only. */
    public boolean mono;

    // Row-cache holders (port of aClass191_7032 / aClass322_7033).
    public MonoBuffer monoBuffer;
    public ColorBuffer colorBuffer;

    protected TextureLayer(int childCount, boolean mono) {
        this.children = new TextureLayer[childCount];
        this.mono = mono;
    }

    /** {@code method3036} — read a sprite-quad affine transform. */
    public static SpriteTransform readSpriteTransform(TextureReader r) {
        return new SpriteTransform(r.readSignedShort(),
                r.readSignedShort(),
                r.readSignedShort(),
                r.readSignedShort(),
                r.readUnsignedTriByte(),
                r.readUnsignedTriByte(),
                r.readUnsignedByte());
    }

    /** {@code method3037} — sprite file id this layer renders, -1 if none. */
    public int spriteFileId() {
        return -1;
    }

    /** {@code method3043} — secondary file id this layer needs, -1 if none. */
    public int auxiliaryFileId() {
        return -1;
    }

    /** {@code method3044} — finalise layer config (tables, presets). No-op until portable. */
    public void finalizeConfig() {
    }

    /** {@code method3049} — parse one parameter for the given opcode. */
    public void readParams(TextureReader r, int opcode) {
    }

    // ---- render hooks ----

    /** {@code method3045} — allocate row buffers for a w x h output. */
    public void allocate(int width, int height, int tag) {
        int planes = tag != ~layerId ? layerId : height;
        if (mono) {
            monoBuffer = new MonoBuffer(planes, height, width);
        } else {
            colorBuffer = new ColorBuffer(planes, height, width);
        }
    }

    /** {@code method3047} — colour rows for row Y; returns {R, G, B}. */
    public int[][] colorRows(int y, int tag) {
        int[][] row = colorBuffer.row(y);
        if (colorBuffer.fresh) {
            fillColorRows(row, y);
        }
        return row;
    }

    /** Subclass hook for {@code colorRows} computation into an empty row. */
    protected void fillColorRows(int[][] row, int y) {
        throw new IllegalStateException("This operation does not have a colour output");
    }

    /** {@code method3042} — monochrome row for row Y. */
    public int[] monoRow(int y, int tag) {
        int[] row = monoBuffer.row(y);
        if (monoBuffer.fresh) {
            fillMonoRow(row, y);
        }
        return row;
    }

    /** Subclass hook for {@code monoRow} computation into an empty row. */
    protected void fillMonoRow(int[] row, int y) {
        throw new IllegalStateException("This operation does not have a monochrome output");
    }

    /** {@code method3046} — free row buffers. */
    public void release() {
        monoBuffer = null;
        colorBuffer = null;
        onRelease();
    }

    /** Subclass hook invoked from {@link #release()}. */
    protected void onRelease() {
    }

    /** {@code Class258_Sub4.method1974}-style sprite fetch through the provider. */
    protected SpriteRaster sprite(int fileId) {
        return TextureRender.spriteProvider == null ? null : TextureRender.spriteProvider.sprite(fileId);
    }

    /** {@code method3039} — colour rows of child {@code index} (mono children replicated). */
    public final int[][] childColorRows(int y, int childIndex) {
        TextureLayer child = children[childIndex];
        if (child.mono) {
            int[] is = child.monoRow(y, REQUEST_MONO);
            return new int[][] {is, is, is};
        }
        return child.colorRows(y, REQUEST_MAGIC);
    }

    /** {@code method3048} — monochrome row of child {@code childIndex}. */
    public final int[] childMonoRow(int y, int childIndex) {
        TextureLayer child = children[childIndex];
        if (!child.mono) {
            return child.colorRows(y, REQUEST_MAGIC)[0];
        }
        return child.monoRow(y, REQUEST_MONO);
    }
}