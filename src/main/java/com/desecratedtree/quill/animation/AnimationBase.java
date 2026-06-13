package com.desecratedtree.quill.animation;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationBase {

    private static final int BASE_INDEX = 1;

    private static final Map<Integer, AnimationBase> CACHE = new ConcurrentHashMap<>();

    public final int id;

    public final int[] transformTypes;

    public final boolean[] transformAlpha;

    public final int[] transformMasks;

    public final int[][] transformGroups;

    private AnimationBase(int id, byte[] data) {
        this.id = id;
        if (data == null || data.length == 0) {
            transformTypes = new int[0];
            transformAlpha = new boolean[0];
            transformMasks = new int[0];
            transformGroups = new int[0][];
            return;
        }
        InputStream stream = new InputStream(data);
        int count = stream.readUnsignedByte();
        transformTypes = new int[count];
        transformAlpha = new boolean[count];
        transformMasks = new int[count];
        transformGroups = new int[count][];
        for (int i = 0; i < count; i++) {
            transformTypes[i] = stream.readUnsignedByte();
            if (transformTypes[i] == 6) {
                transformTypes[i] = 2;
            }
        }
        for (int i = 0; i < count; i++) {
            transformAlpha[i] = stream.readUnsignedByte() == 1;
        }
        for (int i = 0; i < count; i++) {
            transformMasks[i] = stream.readUnsignedShort();
        }
        for (int i = 0; i < count; i++) {
            transformGroups[i] = new int[stream.readUnsignedByte()];
        }
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < transformGroups[i].length; j++) {
                transformGroups[i][j] = stream.readUnsignedByte();
            }
        }
    }

    private AnimationBase(int id, int[] transformTypes, boolean[] transformAlpha,
                          int[] transformMasks, int[][] transformGroups) {
        this.id = id;
        this.transformTypes = transformTypes;
        this.transformAlpha = transformAlpha;
        this.transformMasks = transformMasks;
        this.transformGroups = transformGroups;
    }

    public static AnimationBase create(int id, int[] transformTypes, boolean[] transformAlpha,
                                        int[] transformMasks, int[][] transformGroups) {
        return new AnimationBase(id, transformTypes, transformAlpha, transformMasks, transformGroups);
    }

    public static AnimationBase get(int id) {
        if (id < 0) {
            return null;
        }
        return CACHE.computeIfAbsent(id, key -> {
            byte[] data = CacheManager.getIndexData(BASE_INDEX, key);
            return data == null || data.length == 0 ? null : new AnimationBase(key, data);
        });
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
