package com.desecratedtree.quill.codec;

import java.nio.charset.StandardCharsets;

public final class OutputStream {

    private byte[] buffer;

    public int offset = 0;

    private int bitPosition = 0;

    private static final int[] BIT_MASK = new int[32];
    static {
        for (int i = 0; i < 32; i++) {
            BIT_MASK[i] = (1 << i) - 1;
        }
    }

    public OutputStream() {
        this.buffer = new byte[16];
    }

    public OutputStream(int capacity) {
        this.buffer = new byte[capacity];
    }

    public OutputStream(byte[] buffer) {
        this.buffer = buffer;
        this.offset = buffer.length;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
        this.offset = 0;
    }

    private void checkCapacity(int position) {
        if (position >= buffer.length) {
            byte[] newBuffer = new byte[position + 16];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }

    public void writeByte(int value) {
        checkCapacity(offset);
        buffer[offset++] = (byte) value;
    }

    public void writeShort(int value) {
        writeByte(value >> 8);
        writeByte(value);
    }

    public void writeShortLE(int value) {
        writeByte(value);
        writeByte(value >> 8);
    }

    public void writeInt(int value) {
        writeByte(value >> 24);
        writeByte(value >> 16);
        writeByte(value >> 8);
        writeByte(value);
    }

    public void writeIntLE(int value) {
        writeByte(value);
        writeByte(value >> 8);
        writeByte(value >> 16);
        writeByte(value >> 24);
    }

    public void write24BitInt(int value) {
        writeByte(value >> 16);
        writeByte(value >> 8);
        writeByte(value);
    }

    public void writeBigSmart(int value) {
        if (value >= Short.MAX_VALUE) {
            writeInt(value - Integer.MAX_VALUE - 1);
        } else {
            writeShort(value >= 0 ? value : 32767);
        }
    }

    public void writeSmart(int value) {
        if (value >= 128) {
            writeShort(value + 32768);
        } else {
            writeByte(value);
        }
    }

    public void writeString(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
        checkCapacity(offset + bytes.length + 1);
        System.arraycopy(bytes, 0, buffer, offset, bytes.length);
        offset += bytes.length;
        writeByte(0);
    }

    public void writeGJString2(String string) {
        byte[] packed = new byte[256];
        int length = packGJString2(0, packed, string);
        writeByte(0);
        writeBytes(packed, 0, length);
        writeByte(0);
    }

    public void writeBytes(byte[] data) {
        checkCapacity(offset + data.length);
        System.arraycopy(data, 0, buffer, offset, data.length);
        offset += data.length;
    }

    public void writeBytes(byte[] data, int off, int len) {
        checkCapacity(offset + len - off);
        System.arraycopy(data, off, buffer, offset, len - off);
        offset += len - off;
    }

    public void writeLong(long l) {
        writeByte((int) (l >> 56));
        writeByte((int) (l >> 48));
        writeByte((int) (l >> 40));
        writeByte((int) (l >> 32));
        writeByte((int) (l >> 24));
        writeByte((int) (l >> 16));
        writeByte((int) (l >> 8));
        writeByte((int) l);
    }

    public void initBitAccess() {
        bitPosition = offset * 8;
    }

    public void finishBitAccess() {
        offset = (bitPosition + 7) / 8;
    }

    public void writeBits(int numBits, int value) {
        int bytePos = bitPosition >> 3;
        int bitOffset = 8 - (bitPosition & 7);
        bitPosition += numBits;
        for (; numBits > bitOffset; bitOffset = 8) {
            checkCapacity(bytePos);
            buffer[bytePos] &= ~BIT_MASK[bitOffset];
            buffer[bytePos++] |= (value >> (numBits - bitOffset)) & BIT_MASK[bitOffset];
            numBits -= bitOffset;
        }
        checkCapacity(bytePos);
        if (numBits == bitOffset) {
            buffer[bytePos] &= ~BIT_MASK[bitOffset];
            buffer[bytePos] |= value & BIT_MASK[bitOffset];
        } else {
            buffer[bytePos] &= ~(BIT_MASK[numBits] << (bitOffset - numBits));
            buffer[bytePos] |= (value & BIT_MASK[numBits]) << (bitOffset - numBits);
        }
    }

    public byte[] toByteArray() {
        byte[] data = new byte[offset];
        System.arraycopy(buffer, 0, data, 0, offset);
        return data;
    }

    private static int packGJString2(int pos, byte[] buffer, String string) {
        int length = string.length();
        int offset = pos;
        for (int index = 0; index < length; index++) {
            int ch = string.charAt(index);
            if (ch > 127) {
                if (ch > 2047) {
                    buffer[offset++] = (byte) ((ch | 919275) >> 12);
                    buffer[offset++] = (byte) (128 | ((ch >> 6) & 63));
                    buffer[offset++] = (byte) (128 | (ch & 63));
                } else {
                    buffer[offset++] = (byte) ((ch | 12309) >> 6);
                    buffer[offset++] = (byte) (128 | (ch & 63));
                }
            } else {
                buffer[offset++] = (byte) ch;
            }
        }
        return offset - pos;
    }

    public void writeSmart1Or2Minus1(int value) {
        if (value >= -1 && value < 127) {
            writeByte(value + 1);
        } else {
            writeShort(value + 32769);
        }
    }

    public void writeShortSmart(int value) {
        if (value >= -64 && value < 64) {
            writeByte(value + 64);
        } else {
            writeShort(value + 49152);
        }
    }

    public void writeByteC(int i) {
        writeByte(-i);
    }

    public void writeByte128(int i) {
        writeByte(i + 128);
    }

    public void writeShortLE128(int i) {
        writeByte(i + 128);
        writeByte(i >> 8);
    }

    public void writeShort128(int i) {
        writeByte(i >> 8);
        writeByte(i + 128);
    }
}
