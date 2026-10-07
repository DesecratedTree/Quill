package com.desecratedtree.quill.texture.synth;

/**
 * Byte reader for the 634 texture-program records. Faithful top-for-top port of
 * the client's {@code Class348_Sub49}. Method bodies are transcribed verbatim
 * from the reference deobfuscation so every addend and mask matches.
 */
public final class TextureReader {

    private byte[] data;
    private int offset;

    public TextureReader(byte[] data) {
        this.data = data;
        this.offset = 0;
    }

    public int position() {
        return offset;
    }

    /** {@code method3387} — unsigned byte. */
    public int readUnsignedByte() {
        return data[offset++] & 0xff;
    }

    /** {@code method3388} — raw signed byte. */
    public int readByte() {
        return data[offset++];
    }

    /** {@code method3330} — unsigned short, big-endian. */
    public int readUnsignedShort() {
        offset += 2;
        return ((0xff & data[-1 + offset])
                + ((data[-2 + offset] << -423866104 & 0xff00)));
    }

    /** {@code method3351} — unsigned 24-bit, big-endian. */
    public int readUnsignedTriByte() {
        offset += 3;
        return ((0xff00 & (data[-2 + offset] << -1798246168))
                + (((data[-3 + offset] & 0xff) << -963972240)
                        - -((data[-1 + offset]) & 0xff)));
    }

    /** {@code method3372} — signed short, big-endian. */
    public int readSignedShort() {
        offset += 2;
        int i = ((data[-1 + offset] & 0xff)
                + ((data[-2 + offset] & 0xff) << 2010075272));
        if ((i ^ 0xffffffff) < -32768) {
            i -= 65536;
        }
        return i;
    }

    /** {@code method3363} — unsigned 24-bit, little-endian. */
    public int readTriByteLE() {
        offset += 3;
        return (((data[offset - 3] & 0xff)
                + (((data[-2 + offset]) << -1243326136 & 0xff00)
                        + ((data[-1 + offset]) << -1985557744 & 0xff0000))));
    }

    /** {@code method3369} — signed 24-bit, big-endian. */
    public int readSignedTriByte() {
        offset += 3;
        int i = (((data[-1 + offset] & 0xff)
                + ((0xff0000 & (data[offset + -3] << 567264144))
                        + ((0xff & data[offset + -2]) << -270410424))));
        if ((i ^ 0xffffffff) < -8388608) {
            i -= 16777216;
        }
        return i;
    }

    /** {@code method3362} — "smart": short if >= 128 else byte-64. */
    public int readSmart() {
        int v = data[offset] & 0xff;
        if (v < 128) {
            return readUnsignedByte() - 64;
        }
        return readUnsignedShort() - 49152;
    }

    /** {@code method3385} — int, big-endian (verbatim). */
    public int readInt() {
        offset += 4;
        return ((0xff & data[offset - 1])
                + (((data[-4 + offset] & 0xff) << -684944360)
                        + (0xff0000 & (data[-3 + offset] << -950421808)))
                        - -(data[-2 + offset] & 0xff << -1279370072));
    }

    /** {@code method3342} — (128 - byte) & 255. */
    public int readInvertedByte() {
        return (-(data[offset++]) + 128 & 0xff);
    }

    /** {@code method3377} — zero-terminated byte string. */
    public String readCString() {
        int start = offset;
        while ((data[offset++] ^ 0xffffffff) != -1) {
            /* empty */
        }
        int length = -1 + offset - start;
        if (length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) (data[start + i] & 0xff));
        }
        return sb.toString();
    }

    /** {@code method3384} — null-checked C string. */
    public String readNullableString() {
        if (data[offset] == 0) {
            offset++;
            return null;
        }
        return readCString();
    }
}