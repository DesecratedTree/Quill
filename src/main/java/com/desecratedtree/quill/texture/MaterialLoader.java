package com.desecratedtree.quill.texture;

import com.desecratedtree.quill.cache.CacheManager;

public final class MaterialLoader {

    private static MaterialDefinition[] definitions;

    private MaterialLoader() {
    }
    public static MaterialDefinition get(int materialId) {
        if (materialId < 0) {
            return null;
        }
        MaterialDefinition[] loaded = definitions;
        if (loaded == null) {
            loaded = loadDefinitions();
            definitions = loaded;
        }
        if (materialId >= loaded.length) {
            return null;
        }
        return loaded[materialId];
    }
    public static void clearCache() {
        definitions = null;
    }

    private static MaterialDefinition[] loadDefinitions() {
        byte[] data = CacheManager.getMaterialDefinitionsData();
        if (data == null || data.length < 2) {
            return new MaterialDefinition[0];
        }
        try {
            Buffer buffer = new Buffer(data);
            int count = buffer.readUnsignedShort();
            MaterialDefinition[] materials = new MaterialDefinition[count];
            for (int i = 0; i < count; i++) {
                if (buffer.readUnsignedByte() == 1) {
                    materials[i] = new MaterialDefinition();
                }
            }
            for (MaterialDefinition material : materials) if (material != null) material.lowDetail = buffer.readUnsignedByte() == 0;
            for (MaterialDefinition material : materials) if (material != null) material.smallTexture = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.field204 = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.field216 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.field201 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.field213 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.field202 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.averageColor = buffer.readUnsignedShort();
            for (MaterialDefinition material : materials) if (material != null) material.field198 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.field211 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.field212 = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.repeat = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.field205 = buffer.readByte();
            for (MaterialDefinition material : materials) if (material != null) material.field217 = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.field215 = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.field218 = buffer.readUnsignedByte() == 1;
            for (MaterialDefinition material : materials) if (material != null) material.field203 = buffer.readUnsignedByte();
            for (MaterialDefinition material : materials) if (material != null) material.field206 = buffer.readInt();
            for (MaterialDefinition material : materials) if (material != null) material.field200 = buffer.readUnsignedByte();
            return materials;
        } catch (RuntimeException ignored) {
            return new MaterialDefinition[0];
        }
    }

    private static final class Buffer {

        private final byte[] data;

        private int offset;

        private Buffer(byte[] data) {
            this.data = data;
        }

        private int readUnsignedByte() {
            check(1);
            return data[offset++] & 0xFF;
        }

        private byte readByte() {
            check(1);
            return data[offset++];
        }

        private int readUnsignedShort() {
            check(2);
            return ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);
        }

        private int readInt() {
            check(4);
            return ((data[offset++] & 0xFF) << 24)
                    | ((data[offset++] & 0xFF) << 16)
                    | ((data[offset++] & 0xFF) << 8)
                    | (data[offset++] & 0xFF);
        }

        private void check(int length) {
            if (offset + length > data.length) {
                throw new IllegalArgumentException("Material table is truncated.");
            }
        }
    }
}
