package com.desecratedtree.quill.render;

import com.desecratedtree.quill.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;

public final class TextureDefinitionTable {

    private final List<Entry> entries;

    private TextureDefinitionTable(List<Entry> entries) {
        this.entries = entries;
    }

    public static TextureDefinitionTable load() {
        byte[] data = CacheManager.getMaterialDefinitionsData();
        if (data == null || data.length < 2) {
            return new TextureDefinitionTable(new ArrayList<>());
        }
        Buffer buffer = new Buffer(data);
        int count = buffer.readUnsignedShort();
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Entry entry = new Entry(i);
            entry.present = buffer.readUnsignedByte() == 1;
            entries.add(entry);
        }

        List<Entry> presentEntries = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.present) {
                presentEntries.add(entry);
            }
        }

        for (Entry entry : presentEntries) entry.lowDetail = buffer.readUnsignedByte() == 0;
        for (Entry entry : presentEntries) entry.smallTexture = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.field204 = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.field216 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.field201 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.field213 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.field202 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.averageColor = buffer.readUnsignedShort();
        for (Entry entry : presentEntries) entry.field198 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.field211 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.field212 = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.repeat = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.field205 = (byte) buffer.readByte();
        for (Entry entry : presentEntries) entry.field217 = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.field215 = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.field218 = buffer.readUnsignedByte() == 1;
        for (Entry entry : presentEntries) entry.field203 = buffer.readUnsignedByte();
        for (Entry entry : presentEntries) entry.field206 = buffer.readInt();
        for (Entry entry : presentEntries) entry.field200 = buffer.readUnsignedByte();

        return new TextureDefinitionTable(entries);
    }

    public List<Integer> presentIds() {
        List<Integer> ids = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.present) {
                ids.add(entry.id);
            }
        }
        return ids;
    }

    public int addDefault() {
        for (Entry entry : entries) {
            if (!entry.present) {
                reset(entry);
                return entry.id;
            }
        }
        Entry entry = new Entry(entries.size());
        reset(entry);
        entries.add(entry);
        return entry.id;
    }

    public boolean remove(int id) {
        if (id < 0 || id >= entries.size()) {
            return false;
        }
        entries.get(id).present = false;
        return true;
    }

    public void save() {
        List<Entry> presentEntries = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.present) {
                presentEntries.add(entry);
            }
        }
        Output out = new Output();
        out.writeShort(entries.size());
        for (Entry entry : entries) out.writeByte(entry.present ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.lowDetail ? 0 : 1);
        for (Entry entry : presentEntries) out.writeByte(entry.smallTexture ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.field204 ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.field216);
        for (Entry entry : presentEntries) out.writeByte(entry.field201);
        for (Entry entry : presentEntries) out.writeByte(entry.field213);
        for (Entry entry : presentEntries) out.writeByte(entry.field202);
        for (Entry entry : presentEntries) out.writeShort(entry.averageColor);
        for (Entry entry : presentEntries) out.writeByte(entry.field198);
        for (Entry entry : presentEntries) out.writeByte(entry.field211);
        for (Entry entry : presentEntries) out.writeByte(entry.field212 ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.repeat ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.field205);
        for (Entry entry : presentEntries) out.writeByte(entry.field217 ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.field215 ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.field218 ? 1 : 0);
        for (Entry entry : presentEntries) out.writeByte(entry.field203);
        for (Entry entry : presentEntries) out.writeInt(entry.field206);
        for (Entry entry : presentEntries) out.writeByte(entry.field200);
        CacheManager.writeIndexData(26, 0, 0, out.toByteArray());
    }

    private static void reset(Entry entry) {
        entry.present = true;
        entry.lowDetail = false;
        entry.smallTexture = false;
        entry.field204 = false;
        entry.field216 = 0;
        entry.field201 = 0;
        entry.field213 = 0;
        entry.field202 = 0;
        entry.averageColor = 0;
        entry.field198 = 0;
        entry.field211 = 0;
        entry.field212 = false;
        entry.repeat = true;
        entry.field205 = 0;
        entry.field217 = false;
        entry.field215 = false;
        entry.field218 = false;
        entry.field203 = 0;
        entry.field206 = 0;
        entry.field200 = 0;
    }

    private static final class Buffer {

        private final byte[] data;

        private int offset;

        private Buffer(byte[] data) {
            this.data = data;
        }

        private int readUnsignedByte() {
            return data[offset++] & 0xFF;
        }

        private int readByte() {
            return data[offset++];
        }

        private int readUnsignedShort() {
            return ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);
        }

        private int readInt() {
            return ((data[offset++] & 0xFF) << 24)
                    | ((data[offset++] & 0xFF) << 16)
                    | ((data[offset++] & 0xFF) << 8)
                    | (data[offset++] & 0xFF);
        }
    }

    private static final class Output {

        private final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        private void writeByte(int value) {
            out.write(value & 0xFF);
        }

        private void writeShort(int value) {
            writeByte(value >> 8);
            writeByte(value);
        }

        private void writeInt(int value) {
            writeByte(value >> 24);
            writeByte(value >> 16);
            writeByte(value >> 8);
            writeByte(value);
        }

        private byte[] toByteArray() {
            return out.toByteArray();
        }
    }

    private static final class Entry {

        private final int id;

        private boolean present;

        private boolean lowDetail;

        private boolean smallTexture;

        private boolean field204;

        private byte field216;

        private byte field201;

        private byte field213;

        private byte field202;

        private int averageColor;

        private byte field198;

        private byte field211;

        private boolean field212;

        private boolean repeat;

        private byte field205;

        private boolean field217;

        private boolean field215;

        private boolean field218;

        private int field203;

        private int field206;

        private int field200;

        private Entry(int id) {
            this.id = id;
        }
    }
}
