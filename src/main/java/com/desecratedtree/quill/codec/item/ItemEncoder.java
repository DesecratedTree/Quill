package com.desecratedtree.quill.codec.item;

import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.defs.DefinitionHandler;
import com.desecratedtree.quill.defs.DefinitionHandler.FieldDefinition;
import com.desecratedtree.quill.defs.ItemDefinitions;

public class ItemEncoder {

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(ItemDefinitions.class, "definitions/item.toml");

    public static byte[] encode(ItemDefinitions def) {
        OutputStream stream = new OutputStream();
        for (int opcode : def.presentOpcodeSet) {
            writeOpcode(stream, def, opcode);
        }
        stream.writeByte(0);
        return stream.toByteArray();
    }

    private static void writeOpcode(OutputStream stream, ItemDefinitions def, int opcode) {
        FieldDefinition fd = HANDLER.resolve(opcode);
        if (fd == null) return;

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

        stream.writeByte(opcode);

        switch (fd.type) {
            case "flag":
            case "flag_clear":
                break;
            case "unsigned_byte":
                stream.writeByte(getFieldInt(def, fd));
                break;
            case "byte":
                stream.writeByte(getFieldInt(def, fd));
                break;
            case "unsigned_short":
                stream.writeShort(getFieldInt(def, fd));
                break;
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
                if (orig != null && mod != null && orig.length > 0) {
                    stream.writeByte(orig.length);
                    for (int i = 0; i < orig.length && i < mod.length; i++) {
                        stream.writeShort(orig[i]);
                        stream.writeShort(mod[i]);
                    }
                }
                break;
            }
            case "texture_pairs": {
                short[] orig = getField(def, "originalTextureIds", short[].class);
                int[] mod = getField(def, "modifiedTextureIds", int[].class);
                if (orig != null && mod != null && orig.length > 0) {
                    stream.writeByte(orig.length);
                    for (int i = 0; i < orig.length && i < mod.length; i++) {
                        stream.writeShort(orig[i] & 0xFFFF);
                        stream.writeShort(mod[i]);
                    }
                }
                break;
            }
            case "byte_array": {
                byte[] arr = getField(def, fd.field, byte[].class);
                if (arr != null) {
                    stream.writeByte(arr.length);
                    for (byte b : arr) stream.writeByte(b);
                }
                break;
            }
            case "short_array": {
                int[] arr = getField(def, fd.field, int[].class);
                if (arr != null) {
                    stream.writeByte(arr.length);
                    for (int v : arr) stream.writeShort(v);
                }
                break;
            }
            case "params": {
                java.util.Map<Integer, Object> params = getField(def, "itemParams", java.util.Map.class);
                if (params != null && !params.isEmpty()) {
                    stream.writeByte(params.size());
                    params.forEach((key, value) -> {
                        if (value instanceof String) {
                            stream.writeByte(1);
                            stream.write24BitInt(key);
                            stream.writeString((String) value);
                        } else {
                            stream.writeByte(0);
                            stream.write24BitInt(key);
                            stream.writeInt((Integer) value);
                        }
                    });
                }
                break;
            }
            case "old_model_pair": {
                stream.writeBigSmart(def.oldInvModel);
                stream.writeBigSmart(def.oldInvZoom);
                break;
            }
            case "old_recolor_pairs": {
                if (def.oldModelColors != null) {
                    stream.writeByte(def.oldModelColors.length);
                    for (int i = 0; i < def.oldModelColors.length; i++) {
                        stream.writeShort(def.oldModelColors[i]);
                        stream.writeShort(def.oldModifiedModelColors[i]);
                    }
                }
                break;
            }
            case "old_retexture_pairs": {
                if (def.oldModelTextures != null) {
                    stream.writeByte(def.oldModelTextures.length);
                    for (int i = 0; i < def.oldModelTextures.length; i++) {
                        stream.writeShort(def.oldModelTextures[i]);
                        stream.writeShort(def.oldModifiedModelTextures[i]);
                    }
                }
                break;
            }
            case "old_rotation_offsets": {
                stream.writeShort(def.oldModelRotation1);
                stream.writeShort(def.oldModelRotation2);
                stream.writeShort(def.oldModelOffset1);
                stream.writeShort(def.oldModelOffset2);
                break;
            }
            case "skip_complex_44": {
                stream.writeShort(0);
                break;
            }
            case "skip_complex_45": {
                stream.writeShort(0);
                break;
            }
            case "skip_byte":
                stream.writeByte(0);
                break;
            case "skip_short":
                stream.writeShort(0);
                break;
            case "skip_2_shorts":
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
            case "skip_short_short":
                stream.writeShort(0); stream.writeShort(0);
                break;
            case "skip_short_signed":
                stream.writeShort(0);
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
            return val != null ? val.toString() : "";
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
            if (arrayClass.isInstance(val)) return (T) val;
        } catch (Exception e) { }
        return null;
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
            if (type.isInstance(val)) return (T) val;
        } catch (Exception e) { }
        return null;
    }
}
