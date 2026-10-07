package com.desecratedtree.quill.codec.item;

import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.defs.DefinitionHandler;
import com.desecratedtree.quill.defs.DefinitionHandler.FieldDefinition;
import com.desecratedtree.quill.defs.ItemDefinitions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemEncoder {

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(ItemDefinitions.class, "definitions/item.toml");

    public static byte[] encode(ItemDefinitions def) {
        if (def == null) {
            throw new IllegalArgumentException("Item definition cannot be null.");
        }
        OutputStream stream = new OutputStream();
        Set<Integer> opcodes = new LinkedHashSet<>(def.presentOpcodeSet == null
                ? java.util.Collections.emptySet() : def.presentOpcodeSet);
        Map<Integer, Object> params = getField(def, "itemParams", Map.class);
        if (params != null && !params.isEmpty()) {
            opcodes.add(249);
        } else {
            opcodes.remove(249);
        }
        removeEmptyPairOpcode(opcodes, 40, getField(def, "originalModelColors", int[].class),
                getField(def, "modifiedModelColors", int[].class));
        removeEmptyPairOpcode(opcodes, 41, getField(def, "originalTextureIds", short[].class),
                getField(def, "modifiedTextureIds", int[].class));
        removeEmptyPairOpcode(opcodes, 251, def.oldModelColors, def.oldModifiedModelColors);
        removeEmptyPairOpcode(opcodes, 252, def.oldModelTextures, def.oldModifiedModelTextures);
        for (int opcode : opcodes) {
            writeOpcode(stream, def, opcode);
        }
        stream.writeByte(0);
        return stream.toByteArray();
    }

    private static void writeOpcode(OutputStream stream, ItemDefinitions def, int opcode) {
        FieldDefinition fd = HANDLER.resolve(opcode);
        if (fd == null) {
            return;
        }
        switch (fd.type) {
            case "string_array": {
                String[] arr = getFieldArray(def, fd, String[].class);
                if (arr == null || fd.index < 0 || fd.index >= arr.length || arr[fd.index] == null) return;
                stream.writeByte(opcode);
                stream.writeString(arr[fd.index]);
                return;
            }
            case "unsigned_short_array": {
                int[] arr = getFieldArray(def, fd, int[].class);
                if (arr == null || fd.index < 0 || fd.index >= arr.length) return;
                stream.writeByte(opcode);
                stream.writeShort(arr[fd.index]);
                return;
            }
            case "stack_variant": {
                int[] ids = getField(def, "stackIds", int[].class);
                int[] amounts = getField(def, "stackAmounts", int[].class);
                if (ids == null || amounts == null || fd.index < 0 || fd.index >= ids.length || fd.index >= amounts.length) return;
                stream.writeByte(opcode);
                stream.writeShort(ids[fd.index]);
                stream.writeShort(amounts[fd.index]);
                return;
            }
        }
        if (!hasPayload(def, fd)) {
            return;
        }
        stream.writeByte(opcode);
        switch (fd.type) {
            case "flag":
            case "flag_clear":
                break;
            case "unsigned_byte":
            case "byte":
                stream.writeByte(getFieldInt(def, fd));
                break;
            case "unsigned_short":
            case "short":
            case "offset_short":
                stream.writeShort(getFieldInt(def, fd));
                break;
            case "int":
                stream.writeInt(getFieldInt(def, fd));
                break;
            case "big_smart":
                stream.writeBigSmart(getFieldInt(def, fd));
                break;
            case "smart":
                stream.writeSmart(getFieldInt(def, fd));
                break;
            case "string":
                stream.writeString(getFieldString(def, fd));
                break;
            case "byte_times_5":
                stream.writeByte(getFieldInt(def, fd) / 5);
                break;
            case "color_pairs": {
                int[] orig = getField(def, "originalModelColors", int[].class);
                int[] mod = getField(def, "modifiedModelColors", int[].class);
                int count = pairedLength(orig, mod);
                if (count == 0) return;
                stream.writeByte(count);
                for (int i = 0; i < count; i++) {
                    stream.writeShort(orig[i]);
                    stream.writeShort(mod[i]);
                }
                break;
            }
            case "texture_pairs": {
                short[] orig = getField(def, "originalTextureIds", short[].class);
                int[] mod = getField(def, "modifiedTextureIds", int[].class);
                int count = pairedLength(orig, mod);
                if (count == 0) return;
                stream.writeByte(count);
                for (int i = 0; i < count; i++) {
                    stream.writeShort(orig[i] & 0xFFFF);
                    stream.writeShort(mod[i]);
                }
                break;
            }
            case "byte_array": {
                byte[] arr = getField(def, fd.field, byte[].class);
                if (arr == null || arr.length > 255) return;
                stream.writeByte(arr.length);
                for (byte b : arr) stream.writeByte(b);
                break;
            }
            case "short_array": {
                int[] arr = getField(def, fd.field, int[].class);
                if (arr == null || arr.length > 255) return;
                stream.writeByte(arr.length);
                for (int v : arr) stream.writeShort(v);
                break;
            }
            case "params": {
                Map<Integer, Object> values = getField(def, "itemParams", Map.class);
                if (values == null || values.isEmpty()) return;
                List<Map.Entry<Integer, Object>> entries = new ArrayList<>(values.entrySet());
                if (entries.size() > 255) {
                    throw new IllegalArgumentException("Item opcode 249 supports at most 255 parameters.");
                }
                stream.writeByte(entries.size());
                for (Map.Entry<Integer, Object> entry : entries) {
                    int key = entry.getKey() == null ? -1 : entry.getKey();
                    Object value = entry.getValue();
                    if (key < 0 || key > 0xFFFFFF) {
                        throw new IllegalArgumentException("Item parameter key is outside the 24-bit range: " + key);
                    }
                    if (value instanceof String) {
                        stream.writeByte(1);
                        stream.write24BitInt(key);
                        stream.writeString((String) value);
                    } else if (value instanceof Integer) {
                        stream.writeByte(0);
                        stream.write24BitInt(key);
                        stream.writeInt((Integer) value);
                    } else {
                        throw new IllegalArgumentException("Item parameter " + key + " must be a String or Integer.");
                    }
                }
                break;
            }
            case "old_model_pair":
                stream.writeBigSmart(def.oldInvModel);
                stream.writeBigSmart(def.oldInvZoom);
                break;
            case "old_recolor_pairs": {
                int count = pairedLength(def.oldModelColors, def.oldModifiedModelColors);
                if (count == 0) return;
                stream.writeByte(count);
                for (int i = 0; i < count; i++) {
                    stream.writeShort(def.oldModelColors[i]);
                    stream.writeShort(def.oldModifiedModelColors[i]);
                }
                break;
            }
            case "old_retexture_pairs": {
                int count = pairedLength(def.oldModelTextures, def.oldModifiedModelTextures);
                if (count == 0) return;
                stream.writeByte(count);
                for (int i = 0; i < count; i++) {
                    stream.writeShort(def.oldModelTextures[i]);
                    stream.writeShort(def.oldModifiedModelTextures[i]);
                }
                break;
            }
            case "old_rotation_offsets":
                stream.writeShort(def.oldModelRotation1);
                stream.writeShort(def.oldModelRotation2);
                stream.writeShort(def.oldModelOffset1);
                stream.writeShort(def.oldModelOffset2);
                break;
            case "skip_complex_44":
            case "skip_complex_45":
            case "skip_short":
            case "skip_short_signed":
                stream.writeShort(0);
                break;
            case "skip_byte":
                stream.writeByte(0);
                break;
            case "skip_2_shorts":
            case "skip_short_short":
                stream.writeShort(0); stream.writeShort(0);
                break;
            case "skip_2_bytes":
                stream.writeByte(0); stream.writeByte(0);
                break;
            case "skip_3_bytes":
                stream.writeByte(0); stream.writeByte(0); stream.writeByte(0);
                break;
            case "skip_4_bytes":
                stream.writeByte(0); stream.writeByte(0); stream.writeByte(0); stream.writeByte(0);
                break;
            case "skip_6_shorts":
                for (int i = 0; i < 6; i++) stream.writeShort(0);
                break;
            case "skip_12_bytes":
                for (int i = 0; i < 12; i++) stream.writeByte(0);
                break;
            case "skip_byte_short":
                stream.writeByte(0); stream.writeShort(0);
                break;
            case "skip_4_shorts":
                for (int i = 0; i < 4; i++) stream.writeShort(0);
                break;
            case "skip_big_smart":
                stream.writeShort(0);
                break;
            case "skip":
                break;
            case "24bit_int":
                stream.write24BitInt(getFieldInt(def, fd));
                break;
            case "byte_short_pair":
                stream.writeByte(getFieldInt(def, fd));
                if (fd.secondField != null) stream.writeShort(getFieldIntFromName(def, fd.secondField));
                break;
            default:
                HANDLER.writeOpcodeValue(def, stream, opcode, 0);
                break;
        }
    }

    private static boolean hasPayload(ItemDefinitions def, FieldDefinition fd) {
        switch (fd.type) {
            case "color_pairs":
                return pairedLength(getField(def, "originalModelColors", int[].class),
                        getField(def, "modifiedModelColors", int[].class)) > 0;
            case "texture_pairs":
                return pairedLength(getField(def, "originalTextureIds", short[].class),
                        getField(def, "modifiedTextureIds", int[].class)) > 0;
            case "byte_array": {
                byte[] values = getField(def, fd.field, byte[].class);
                return values != null && values.length <= 255;
            }
            case "short_array": {
                int[] values = getField(def, fd.field, int[].class);
                return values != null && values.length <= 255;
            }
            case "params": {
                Map<Integer, Object> values = getField(def, "itemParams", Map.class);
                return values != null && !values.isEmpty();
            }
            case "old_recolor_pairs":
                return pairedLength(def.oldModelColors, def.oldModifiedModelColors) > 0;
            case "old_retexture_pairs":
                return pairedLength(def.oldModelTextures, def.oldModifiedModelTextures) > 0;
            default:
                return true;
        }
    }

    private static int pairedLength(int[] first, int[] second) {
        return Math.min(Math.min(first == null ? 0 : first.length, second == null ? 0 : second.length), 255);
    }

    private static int pairedLength(short[] first, int[] second) {
        return Math.min(Math.min(first == null ? 0 : first.length, second == null ? 0 : second.length), 255);
    }

    private static int pairedLength(short[] first, short[] second) {
        return Math.min(Math.min(first == null ? 0 : first.length, second == null ? 0 : second.length), 255);
    }

    private static void removeEmptyPairOpcode(Set<Integer> opcodes, int opcode, int[] first, int[] second) {
        if (pairedLength(first, second) == 0) opcodes.remove(opcode);
    }

    private static void removeEmptyPairOpcode(Set<Integer> opcodes, int opcode, short[] first, int[] second) {
        if (pairedLength(first, second) == 0) opcodes.remove(opcode);
    }

    private static void removeEmptyPairOpcode(Set<Integer> opcodes, int opcode, short[] first, short[] second) {
        if (pairedLength(first, second) == 0) opcodes.remove(opcode);
    }

    private static int getFieldInt(ItemDefinitions def, FieldDefinition fd) {
        try {
            java.lang.reflect.Field f = ItemDefinitions.class.getDeclaredField(fd.field);
            f.setAccessible(true);
            return f.getInt(def);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getFieldString(ItemDefinitions def, FieldDefinition fd) {
        try {
            java.lang.reflect.Field f = ItemDefinitions.class.getDeclaredField(fd.field);
            f.setAccessible(true);
            Object val = f.get(def);
            return val == null ? "" : val.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFieldArray(ItemDefinitions def, FieldDefinition fd, Class<T> arrayClass) {
        try {
            java.lang.reflect.Field f = ItemDefinitions.class.getDeclaredField(fd.field);
            f.setAccessible(true);
            Object val = f.get(def);
            return arrayClass.isInstance(val) ? (T) val : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static int getFieldIntFromName(ItemDefinitions def, String fieldName) {
        try {
            java.lang.reflect.Field f = ItemDefinitions.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.getInt(def);
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(ItemDefinitions def, String fieldName, Class<T> type) {
        try {
            java.lang.reflect.Field f = ItemDefinitions.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(def);
            return type.isInstance(val) ? (T) val : null;
        } catch (Exception e) {
            return null;
        }
    }
}
