package com.desecratedtree.quill.animation;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.codec.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationFrame {

    private static final int FRAME_INDEX = 0;

    private static final Map<Integer, AnimationFrame> CACHE = new ConcurrentHashMap<>();

    public final AnimationBase base;

    public final int[] transformIndices;

    public final int[] transformX;

    public final int[] transformY;

    public final int[] transformZ;

    public AnimationFrame(byte[] data, AnimationBase base) {
        this.base = base;
        InputStream header = new InputStream(data);
        InputStream values = new InputStream(data);
        header.readUnsignedByte();
        int baseId = header.readUnsignedShort();
        if (base == null || base.id != baseId) {
            throw new IllegalArgumentException("Animation base mismatch");
        }
        int count = header.readUnsignedByte();
        values.setOffset(header.getOffset() + count);
        int[] indices = new int[count];
        int[] x = new int[count];
        int[] y = new int[count];
        int[] z = new int[count];
        int used = 0;
        int lastOrigin = -1;
        int lastInsertedOrigin = -1;
        for (int i = 0; i < count; i++) {
            int type = base.transformTypes[i];
            if (type == 0) {
                lastOrigin = i;
            }
            int flags = header.readUnsignedByte();
            if (flags <= 0) {
                continue;
            }
            if (type == 0) {
                lastInsertedOrigin = i;
            }
            if (type == 1 || type == 2 || type == 3) {
                if (lastOrigin > lastInsertedOrigin) {
                    indices[used] = lastOrigin;
                    x[used] = 0;
                    y[used] = 0;
                    z[used] = 0;
                    used++;
                    lastInsertedOrigin = lastOrigin;
                }
            }
            int defaultValue = type == 3 || type == 10 ? 128 : 0;
            indices[used] = i;
            x[used] = (flags & 0x1) != 0 ? values.readShortSmart() : defaultValue;
            y[used] = (flags & 0x2) != 0 ? values.readShortSmart() : defaultValue;
            z[used] = (flags & 0x4) != 0 ? values.readShortSmart() : defaultValue;
            if (type == 2 || type == 9) {
                x[used] = x[used] << 2 & 0x3FFF;
                y[used] = y[used] << 2 & 0x3FFF;
                z[used] = z[used] << 2 & 0x3FFF;
            }
            used++;
        }
        transformIndices = copy(indices, used);
        transformX = copy(x, used);
        transformY = copy(y, used);
        transformZ = copy(z, used);
    }

    public static AnimationFrame get(int frameId) {
        if (frameId < 0) {
            return null;
        }
        return CACHE.computeIfAbsent(frameId, AnimationFrame::decode);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static AnimationFrame decode(int frameId) {
        int archive = frameId >>> 16;
        int file = frameId & 0xFFFF;
        byte[] data = CacheManager.getIndexData(FRAME_INDEX, archive, file);
        if (data == null || data.length == 0) {
            return null;
        }
        InputStream stream = new InputStream(data);
        stream.setOffset(1);
        AnimationBase base = AnimationBase.get(stream.readUnsignedShort());
        return base == null ? null : new AnimationFrame(data, base);
    }

    private static int[] copy(int[] source, int length) {
        int[] result = new int[length];
        System.arraycopy(source, 0, result, 0, length);
        return result;
    }

    private AnimationFrame(AnimationBase base, int[] transformIndices,
                           int[] transformX, int[] transformY, int[] transformZ) {
        this.base = base;
        this.transformIndices = transformIndices;
        this.transformX = transformX;
        this.transformY = transformY;
        this.transformZ = transformZ;
    }

    public static AnimationFrame createFromTransforms(AnimationBase base,
                                                       int[] transformIndices,
                                                       int[] transformX,
                                                       int[] transformY,
                                                       int[] transformZ) {
        return new AnimationFrame(base, transformIndices, transformX, transformY, transformZ);
    }

    public byte[] encode() {
        OutputStream out = new OutputStream();
        out.writeByte(1);
        out.writeShort(base.id);
        out.writeByte(base.transformTypes.length);
        int valuePos = 0;
        int[] tempX = new int[base.transformTypes.length];
        int[] tempY = new int[base.transformTypes.length];
        int[] tempZ = new int[base.transformTypes.length];
        int[] flags = new int[base.transformTypes.length];
        for (int i = 0; i < base.transformTypes.length; i++) {
            if (valuePos < transformIndices.length && transformIndices[valuePos] == i) {
                int type = base.transformTypes[i];
                int defaultValue = type == 3 || type == 10 ? 128 : 0;
                int f = 0;
                int xv = transformX[valuePos];
                int yv = transformY[valuePos];
                int zv = transformZ[valuePos];
                if (type == 2 || type == 9) {
                    xv = (xv >> 2) & 0x3FFF;
                    yv = (yv >> 2) & 0x3FFF;
                    zv = (zv >> 2) & 0x3FFF;
                }
                if (xv != defaultValue) f |= 0x1;
                if (yv != defaultValue) f |= 0x2;
                if (zv != defaultValue) f |= 0x4;
                flags[i] = f;
                tempX[i] = xv;
                tempY[i] = yv;
                tempZ[i] = zv;
                valuePos++;
            } else {
                flags[i] = 0;
            }
        }
        for (int f : flags) {
            out.writeByte(f);
        }
        for (int i = 0; i < base.transformTypes.length; i++) {
            if ((flags[i] & 0x1) != 0) out.writeShortSmart(tempX[i]);
            if ((flags[i] & 0x2) != 0) out.writeShortSmart(tempY[i]);
            if ((flags[i] & 0x4) != 0) out.writeShortSmart(tempZ[i]);
        }
        return out.toByteArray();
    }

    public void save(int frameId) {
        int archive = frameId >>> 16;
        int file = frameId & 0xFFFF;
        CacheManager.writeIndexData(0, archive, file, encode());
        CACHE.put(frameId, this);
    }

    public void save() {
        int archive = base.id;
        int file = -1;
        for (Map.Entry<Integer, AnimationFrame> entry : CACHE.entrySet()) {
            if (entry.getValue() == this) {
                int frameId = entry.getKey();
                archive = frameId >>> 16;
                file = frameId & 0xFFFF;
                break;
            }
        }
        if (file < 0) return;
        CacheManager.writeIndexData(0, archive, file, encode());
    }
}
