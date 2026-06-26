package com.desecratedtree.quill.defs;

import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.util.RuntimeRevision;
import com.moandjiezana.toml.Toml;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DefinitionHandler {

    private static final Map<String, DefinitionHandler> CACHE = new HashMap<>();

    private final Class<?> targetClass;

    private final Map<Integer, Map<Integer, FieldDefinition>> revisionOpcodes = new LinkedHashMap<>();

    private final Map<String, Field> fieldCache = new HashMap<>();

    private DefinitionHandler(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public static DefinitionHandler forType(Class<?> targetClass, String resourcePath) {
        DefinitionHandler handler = CACHE.get(resourcePath);
        if (handler == null) {
            handler = new DefinitionHandler(targetClass);
            handler.load(resourcePath);
            CACHE.put(resourcePath, handler);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")

    private void load(String resourcePath) {
        String content = readResource(resourcePath);
        if (content == null || content.isEmpty()) return;
        Toml toml = new Toml().read(content);
        Map<Integer, Map<Integer, FieldDefinition>> raw = new HashMap<>();
        for (String tableName : toml.toMap().keySet()) {
            if (!tableName.matches("\\d+")) continue;
            int rev = Integer.parseInt(tableName);
            Toml section = toml.getTable(tableName);
            Map<Integer, FieldDefinition> opcodeMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : section.toMap().entrySet()) {
                try {
                    int opcode = Integer.parseInt(entry.getKey());
                    Map<String, Object> def = (Map<String, Object>) entry.getValue();
                    FieldDefinition fd = new FieldDefinition(
                            (String) def.get("field"),
                            (String) def.get("type"),
                            def.containsKey("index") ? ((Number) def.get("index")).intValue() : -1
                    );
                    opcodeMap.put(opcode, fd);
                } catch (NumberFormatException ignored) {}
            }
            raw.put(rev, opcodeMap);
        }
        Map<Integer, FieldDefinition> base = raw.get(634);
        if (base == null) {
            revisionOpcodes.putAll(raw);
            return;
        }
        revisionOpcodes.put(634, base);
        for (Map.Entry<Integer, Map<Integer, FieldDefinition>> entry : raw.entrySet()) {
            int rev = entry.getKey();
            if (rev == 634) continue;
            Map<Integer, FieldDefinition> merged = new LinkedHashMap<>(base);
            merged.putAll(entry.getValue());
            revisionOpcodes.put(rev, merged);
        }
    }

    private Field getField(String name) {
        if (name == null || name.isEmpty()) return null;
        Field f = fieldCache.get(name);
        if (f != null) return f;
        try {
            f = targetClass.getDeclaredField(name);
            f.setAccessible(true);
            fieldCache.put(name, f);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public FieldDefinition resolve(int opcode) {
        Map<Integer, FieldDefinition> overrides = revisionOpcodes.get(RuntimeRevision.getRevision());
        if (overrides != null) {
            FieldDefinition def = overrides.get(opcode);
            if (def != null) return def;
        }
        Map<Integer, FieldDefinition> base = revisionOpcodes.get(634);
        return base != null ? base.get(opcode) : null;
    }

    public boolean has(int opcode) {
        for (Map<Integer, FieldDefinition> map : revisionOpcodes.values()) {
            if (map.containsKey(opcode)) return true;
        }
        return false;
    }

    public void read(Object target, InputStream stream, int opcode) {
        FieldDefinition def = resolve(opcode);
        if (def != null) {
            readField(target, stream, def, opcode);
        }
    }

    @SuppressWarnings("unchecked")

    private void readField(Object target, InputStream stream, FieldDefinition def, int opcode) {
        try {
            switch (def.type) {
                case "unsigned_byte": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readUnsignedByte());
                    break;
                }
                case "byte": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readByte());
                    break;
                }
                case "unsigned_short": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readUnsignedShort());
                    break;
                }
                case "short": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readShort());
                    break;
                }
                case "int": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readInt());
                    break;
                }
                case "big_smart": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readBigSmart());
                    break;
                }
                case "smart": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readUnsignedSmart());
                    break;
                }
                case "string": {
                    Field f = getField(def.field);
                    if (f != null) f.set(target, stream.readString());
                    break;
                }
                case "24bit_int": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.read24BitInt());
                    break;
                }
                case "flag": {
                    Field f = getField(def.field);
                    if (f != null) {
                        if (f.getType() == boolean.class) f.setBoolean(target, true);
                        else if (f.getType() == int.class) f.setInt(target, 1);
                    }
                    break;
                }
                case "flag_clear": {
                    Field f = getField(def.field);
                    if (f != null && f.getType() == boolean.class) f.setBoolean(target, false);
                    break;
                }
                case "offset_short": {
                    Field f = getField(def.field);
                    if (f != null) {
                        int val = stream.readUnsignedShort();
                        if (val > 32767) val -= 65536;
                        f.setInt(target, val);
                    }
                    break;
                }
                case "byte_times_5": {
                    Field f = getField(def.field);
                    if (f != null) f.setInt(target, stream.readByte() * 5);
                    break;
                }
                case "string_array": {
                    String val = stream.readString();
                    Field f = getField(def.field);
                    if (f != null) {
                        String[] arr = (String[]) f.get(target);
                        if (arr != null && def.index >= 0 && def.index < arr.length) {
                            arr[def.index] = val;
                        }
                    }
                    break;
                }
                case "unsigned_short_array": {
                    int val = stream.readUnsignedShort();
                    Field f = getField(def.field);
                    if (f != null) {
                        int[] arr = (int[]) f.get(target);
                        if (arr == null) {
                            arr = new int[10];
                            f.set(target, arr);
                        }
                        if (def.index >= 0 && def.index < arr.length) {
                            arr[def.index] = val;
                        }
                    }
                    break;
                }
                case "skip": break;
                case "skip_byte": stream.readUnsignedByte(); break;
                case "skip_short": stream.readUnsignedShort(); break;
                case "skip_2_shorts": stream.readUnsignedShort(); stream.readUnsignedShort(); break;
                case "skip_2_bytes": stream.readByte(); stream.readByte(); break;
                case "skip_3_bytes": stream.readByte(); stream.readByte(); stream.readByte(); break;
                case "skip_4_bytes": stream.readByte(); stream.readByte(); stream.readByte(); stream.readByte(); break;
                case "skip_6_shorts": for (int i = 0; i < 6; i++) stream.readUnsignedShort(); break;
                case "skip_12_bytes": for (int i = 0; i < 12; i++) stream.readByte(); break;
                case "skip_byte_short": stream.readUnsignedByte(); stream.readUnsignedShort(); break;
                case "skip_short_short": stream.readUnsignedShort(); stream.readUnsignedShort(); break;
                case "skip_short_signed": stream.readShort(); break;
                case "skip_big_smart": {
                    if ((stream.getBuffer()[stream.getOffset()] & 0xff) < 128) {
                        stream.readUnsignedShort();
                    } else {
                        stream.readInt();
                    }
                    break;
                }
                case "skip_4_shorts":
                    for (int i = 0; i < 4; i++) stream.readUnsignedShort();
                    break;
                case "color_pairs": {
                    int len = stream.readUnsignedByte();
                    int[] orig = new int[len];
                    int[] mod = new int[len];
                    for (int i = 0; i < len; i++) {
                        orig[i] = stream.readUnsignedShort();
                        mod[i] = stream.readUnsignedShort();
                    }
                    setField("originalModelColors", orig, target);
                    setField("modifiedModelColors", mod, target);
                    break;
                }
                case "texture_pairs": {
                    int len = stream.readUnsignedByte();
                    short[] orig = new short[len];
                    int[] mod = new int[len];
                    for (int i = 0; i < len; i++) {
                        orig[i] = (short) stream.readUnsignedShort();
                        mod[i] = stream.readUnsignedShort();
                    }
                    setField("originalTextureIds", orig, target);
                    setField("modifiedTextureIds", mod, target);
                    break;
                }
                case "byte_array": {
                    int len = stream.readUnsignedByte();
                    byte[] arr = new byte[len];
                    for (int i = 0; i < len; i++) arr[i] = (byte) stream.readByte();
                    setField(def.field, arr, target);
                    break;
                }
                case "byte_array_skip": {
                    int len = stream.readUnsignedByte();
                    stream.skip(len);
                    break;
                }
                case "params": {
                    int len = stream.readUnsignedByte();
                    Map<Integer, Object> params = new HashMap<>(len);
                    for (int i = 0; i < len; i++) {
                        boolean isString = stream.readUnsignedByte() == 1;
                        int key = stream.read24BitInt();
                        Object val = isString ? stream.readString() : stream.readInt();
                        params.put(key, val);
                    }
                    Field f = getField(def.field);
                    if (f != null) {
                        Object existing = f.get(target);
                        if (existing instanceof Map) {
                            ((Map<Integer, Object>) existing).putAll(params);
                        } else {
                            f.set(target, params);
                        }
                    }
                    break;
                }
                case "stack_variant": {
                    int stackId = stream.readUnsignedShort();
                    int stackAmount = stream.readUnsignedShort();
                    setArrayField("stackIds", stackId, def.index, target);
                    setArrayField("stackAmounts", stackAmount, def.index, target);
                    break;
                }
                case "short_array": {
                    int len = stream.readUnsignedByte();
                    int[] arr = new int[len];
                    for (int i = 0; i < len; i++) arr[i] = stream.readUnsignedShort();
                    setField(def.field, arr, target);
                    break;
                }
                case "skip_complex_44": {
                    int val = stream.readUnsignedShort();
                    int arraySize = 0;
                    for (int i = val; i > 0; i >>= 1) arraySize++;
                    byte[] arr = new byte[arraySize];
                    byte offset = 0;
                    for (int idx = 0; idx < arraySize; idx++) {
                        if ((val & 1 << idx) > 0) arr[idx] = offset++;
                        else arr[idx] = -1;
                    }
                    setField("unknownArray3", arr, target);
                    break;
                }
                case "skip_complex_45": {
                    int val = (short) stream.readUnsignedShort();
                    int arraySize = 0;
                    for (int i = val; i > 0; i >>= 1) arraySize++;
                    byte[] arr = new byte[arraySize];
                    byte offset = 0;
                    for (int idx = 0; idx < arraySize; idx++) {
                        if ((val & 1 << idx) > 0) { arr[idx] = offset; offset++; }
                        else arr[idx] = -1;
                    }
                    setField("unknownArray6", arr, target);
                    break;
                }
                case "old_model_pair": {
                    int invModel = stream.readBigSmart();
                    int invZoom = stream.readBigSmart();
                    setFieldInt("oldInvModel", invModel, target);
                    setFieldInt("oldInvZoom", invZoom, target);
                    break;
                }
                case "old_recolor_pairs": {
                    int len = stream.readUnsignedByte();
                    short[] orig = new short[len];
                    short[] mod = new short[len];
                    for (int i = 0; i < len; i++) {
                        orig[i] = (short) stream.readUnsignedShort();
                        mod[i] = (short) stream.readUnsignedShort();
                    }
                    setField("oldModelColors", orig, target);
                    setField("oldModifiedModelColors", mod, target);
                    break;
                }
                case "old_retexture_pairs": {
                    int len = stream.readUnsignedByte();
                    short[] orig = new short[len];
                    short[] mod = new short[len];
                    for (int i = 0; i < len; i++) {
                        orig[i] = (short) stream.readUnsignedShort();
                        mod[i] = (short) stream.readUnsignedShort();
                    }
                    setField("oldModelTextures", orig, target);
                    setField("oldModifiedModelTextures", mod, target);
                    break;
                }
                case "old_rotation_offsets": {
                    setFieldInt("oldModelRotation1", stream.readUnsignedShort(), target);
                    setFieldInt("oldModelRotation2", stream.readUnsignedShort(), target);
                    setFieldInt("oldModelOffset1", stream.readUnsignedShort(), target);
                    setFieldInt("oldModelOffset2", stream.readUnsignedShort(), target);
                    break;
                }
                case "head_icons": {
                    int headIcon = stream.readUnsignedShort();
                    if (headIcon == 65535) headIcon = -1;
                    setFieldInt(def.field, headIcon, target);
                    break;
                }
                case "model_array": {
                    int count = stream.readUnsignedByte();
                    int[] arr = new int[count];
                    for (int i = 0; i < count; i++) arr[i] = normalizeId(stream.readUnsignedShort());
                    setField(def.field, arr, target);
                    break;
                }
                case "osrs_model_array_no_shape": {
                    List<Integer> list = new ArrayList<>();
                    int id = stream.readUnsignedShort();
                    while (id != 65535) {
                        list.add(normalizeId(id));
                        id = stream.readUnsignedShort();
                    }
                    int[] arr = list.stream().mapToInt(i -> i).toArray();
                    setField(def.field, arr, target);
                    break;
                }
                case "model_array_742": {
                    int count = stream.readUnsignedByte();
                    int[] arr = new int[count];
                    for (int i = 0; i < count; i++) arr[i] = stream.readBigSmart();
                    setField(def.field, arr, target);
                    break;
                }
                case "head_model_array": {
                    int count = stream.readUnsignedByte();
                    int[] arr = new int[count];
                    for (int i = 0; i < count; i++) arr[i] = normalizeId(stream.readUnsignedShort());
                    setField(def.field, arr, target);
                    break;
                }
                case "head_model_array_742": {
                    int count = stream.readUnsignedByte();
                    int[] arr = new int[count];
                    for (int i = 0; i < count; i++) arr[i] = stream.readBigSmart();
                    setField(def.field, arr, target);
                    break;
                }
                case "model_translations": {
                    int count = stream.readUnsignedByte();
                    int[][] translations = null;
                    Field fModels = getField("modelIds");
                    if (fModels != null) {
                        int[] models = (int[]) fModels.get(target);
                        if (models != null && models.length > 0) {
                            translations = new int[models.length][];
                        }
                    }
                    for (int i = 0; i < count; i++) {
                        int modelIndex = stream.readUnsignedByte();
                        int[] t = new int[]{ stream.readByte(), stream.readByte(), stream.readByte() };
                        if (translations != null && modelIndex >= 0 && modelIndex < translations.length) {
                            translations[modelIndex] = t;
                        }
                    }
                    setField("modelTranslations", translations, target);
                    break;
                }
                case "skip_complex_106": {
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    int count = stream.readUnsignedByte();
                    for (int i = 0; i <= count; i++) stream.readUnsignedShort();
                    break;
                }
                case "skip_complex_118": {
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    int count = stream.readUnsignedByte();
                    for (int i = 0; i <= count; i++) stream.readUnsignedShort();
                    break;
                }
                case "skip_complex_134": {
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    stream.readUnsignedByte();
                    break;
                }
                case "skip_complex_160": {
                    int count = stream.readUnsignedByte();
                    for (int i = 0; i < count; i++) stream.readUnsignedShort();
                    break;
                }
                case "model_def": {
                    int length = stream.readUnsignedByte();
                    int[][] modelIds = new int[length][];
                    byte[] modelTypes = new byte[length];
                    for (int i = 0; i < length; i++) {
                        modelTypes[i] = (byte) stream.readByte();
                        int count = stream.readUnsignedByte();
                        modelIds[i] = new int[count];
                        for (int j = 0; j < count; j++) {
                            modelIds[i][j] = normalizeId(stream.readUnsignedShort());
                        }
                    }
                    setField("modelIds", modelIds, target);
                    setField("modelTypes", modelTypes, target);
                    break;
                }
                case "model_def_742": {
                    int length = stream.readUnsignedByte();
                    int[][] modelIds = new int[length][];
                    byte[] modelTypes = new byte[length];
                    for (int i = 0; i < length; i++) {
                        modelTypes[i] = (byte) stream.readByte();
                        int count = stream.readUnsignedByte();
                        modelIds[i] = new int[count];
                        for (int j = 0; j < count; j++) {
                            modelIds[i][j] = stream.readBigSmart();
                        }
                    }
                    setField("modelIds", modelIds, target);
                    setField("modelTypes", modelTypes, target);
                    break;
                }
                case "object_model_def_742_5": {
                    int length = stream.readUnsignedByte();
                    int[][] modelIds = new int[length][];
                    byte[] modelTypes = new byte[length];
                    for (int i = 0; i < length; i++) {
                        modelTypes[i] = (byte) stream.readByte();
                        int count = stream.readUnsignedByte();
                        modelIds[i] = new int[count];
                        for (int j = 0; j < count; j++) {
                            modelIds[i][j] = stream.readBigSmart();
                        }
                    }
                    setField("modelIds", modelIds, target);
                    setField("modelTypes", modelTypes, target);
                    int extraLen = stream.readUnsignedByte();
                    for (int i = 0; i < extraLen; i++) {
                        stream.readByte();
                        int subCount = stream.readUnsignedByte();
                        for (int j = 0; j < subCount; j++) {
                            stream.readBigSmart();
                        }
                    }
                    break;
                }
                case "object_props_17": {
                    setFieldBoolean("blocksSky", false, target);
                    setFieldInt("solid", 0, target);
                    break;
                }
                case "object_contour_21": {
                    setFieldByte("contouredGround", (byte) 1, target);
                    break;
                }
                case "object_culling_23": {
                    setFieldInt("culling", 1, target);
                    break;
                }
                case "single_animation": {
                    setField("animations", new int[]{ normalizeId(stream.readUnsignedShort()) }, target);
                    break;
                }
                case "single_animation_742": {
                    setField("animations", new int[]{ stream.readBigSmart() }, target);
                    break;
                }
                case "object_solid_27": {
                    setFieldInt("solid", 1, target);
                    break;
                }
                case "object_offset_mult": {
                    setFieldInt("offsetMultiplier", stream.readUnsignedByte() << 2, target);
                    break;
                }
                case "object_contrast": {
                    setFieldInt("contrast", stream.readByte() * 5, target);
                    break;
                }
                case "object_offset": {
                    setFieldInt(def.field, (stream.readShort() << 2), target);
                    break;
                }
                case "transforms": {
                    readTransforms(target, stream, false);
                    break;
                }
                case "transforms_extended": {
                    readTransforms(target, stream, true);
                    break;
                }
                case "object_78": {
                    setFieldInt("anInt3015", normalizeId(stream.readUnsignedShort()), target);
                    setFieldInt("anInt3012", stream.readUnsignedByte(), target);
                    break;
                }
                case "object_79": {
                    int anInt2989 = stream.readUnsignedShort();
                    int anInt2971 = stream.readUnsignedShort();
                    int anInt3012 = stream.readUnsignedByte();
                    int count = stream.readUnsignedByte();
                    int[] arr = new int[count];
                    for (int i = 0; i < count; i++) arr[i] = normalizeId(stream.readUnsignedShort());
                    setFieldInt("anInt2989", anInt2989, target);
                    setFieldInt("anInt2971", anInt2971, target);
                    setFieldInt("anInt3012", anInt3012, target);
                    setField("anIntArray3036", arr, target);
                    break;
                }
                case "object_contour_81": {
                    setFieldByte("contouredGround", (byte) 2, target);
                    setFieldInt("anInt3023", stream.readUnsignedByte() * 256, target);
                    break;
                }
                case "object_contour_93": {
                    setFieldByte("contouredGround", (byte) 3, target);
                    setFieldInt("anInt3023", stream.readUnsignedShort(), target);
                    break;
                }
                case "object_contour_94": {
                    setFieldByte("contouredGround", (byte) 4, target);
                    break;
                }
                case "object_contour_95": {
                    setFieldByte("contouredGround", (byte) 5, target);
                    setFieldInt("anInt3023", stream.readUnsignedShort(), target);
                    break;
                }
                case "object_99": {
                    setFieldInt("anInt2987", stream.readUnsignedByte(), target);
                    setFieldInt("anInt3008", stream.readUnsignedShort(), target);
                    break;
                }
                case "object_100": {
                    setFieldInt("anInt3038", stream.readUnsignedByte(), target);
                    setFieldInt("anInt3013", stream.readUnsignedShort(), target);
                    break;
                }
                case "object_culling_103": {
                    setFieldInt("culling", 0, target);
                    break;
                }
                case "object_106": {
                    int count = stream.readUnsignedByte();
                    int[] anims = new int[count];
                    int[] percs = new int[count];
                    int total = 0;
                    for (int i = 0; i < count; i++) {
                        anims[i] = normalizeId(stream.readUnsignedShort());
                        percs[i] = stream.readUnsignedByte();
                        total += percs[i];
                    }
                    if (total > 0) {
                        for (int i = 0; i < count; i++) percs[i] = 65535 * percs[i] / total;
                    }
                    setField("animations", anims, target);
                    setField("percents", percs, target);
                    break;
                }
                case "object_106_742": {
                    int count = stream.readUnsignedByte();
                    int[] anims = new int[count];
                    int[] percs = new int[count];
                    int total = 0;
                    for (int i = 0; i < count; i++) {
                        anims[i] = stream.readBigSmart();
                        percs[i] = stream.readUnsignedByte();
                        total += percs[i];
                    }
                    if (total > 0) {
                        for (int i = 0; i < count; i++) percs[i] = 65535 * percs[i] / total;
                    }
                    setField("animations", anims, target);
                    setField("percents", percs, target);
                    break;
                }
                case "object_160": {
                    int count = stream.readUnsignedByte();
                    int[] arr = new int[count];
                    for (int i = 0; i < count; i++) {
                        arr[i] = stream.readUnsignedShort();
                    }
                    setField(def.field, arr, target);
                    break;
                }
                case "object_contour_162": {
                    setFieldByte("contouredGround", (byte) 3, target);
                    setFieldInt("anInt3023", stream.readInt(), target);
                    break;
                }
                case "object_163": {
                    setFieldByte("aByte2974", (byte) stream.readByte(), target);
                    setFieldByte("aByte3045", (byte) stream.readByte(), target);
                    setFieldByte("aByte3052", (byte) stream.readByte(), target);
                    setFieldByte("aByte2960", (byte) stream.readByte(), target);
                    break;
                }
                case "object_173": {
                    setFieldInt("anInt3050", stream.readUnsignedShort(), target);
                    setFieldInt("anInt3020", stream.readUnsignedShort(), target);
                    break;
                }
                case "sequence_frames": {
                    int count = stream.readUnsignedShort();
                    int[] durations = new int[count];
                    int[] frameIds = new int[count];
                    for (int i = 0; i < count; i++) durations[i] = stream.readUnsignedShort();
                    for (int i = 0; i < count; i++) frameIds[i] = stream.readUnsignedShort();
                    for (int i = 0; i < count; i++) frameIds[i] |= stream.readUnsignedShort() << 16;
                    setField("frameDurations", durations, target);
                    setField("frameIds", frameIds, target);
                    break;
                }
                case "skip_complex_3": {
                    int count = stream.readUnsignedByte();
                    for (int i = 0; i < count; i++) stream.readUnsignedByte();
                    break;
                }
                case "sequence_secondary_frames": {
                    int count = stream.readUnsignedByte();
                    int[] ids = new int[count];
                    for (int i = 0; i < count; i++) ids[i] = stream.readUnsignedShort();
                    for (int i = 0; i < count; i++) ids[i] |= stream.readUnsignedShort() << 16;
                    setField("secondaryFrameIds", ids, target);
                    break;
                }
                case "skip_sequence_13": {
                    int count = stream.readUnsignedShort();
                    for (int i = 0; i < count; i++) {
                        int subCount = stream.readUnsignedByte();
                        if (subCount > 0) {
                            stream.read24BitInt();
                            for (int j = 1; j < subCount; j++) stream.readUnsignedShort();
                        }
                    }
                    break;
                }
                case "skip_sequence_20": {
                    stream.readUnsignedByte();
                    stream.readUnsignedShort();
                    stream.readUnsignedShort();
                    break;
                }
                case "render_anim_sequences": {
                    int idle = stream.readUnsignedShort();
                    int walk = stream.readUnsignedShort();
                    setFieldInt("idleSequenceId", idle == 65535 ? -1 : idle, target);
                    setFieldInt("walkSequenceId", walk == 65535 ? -1 : walk, target);
                    break;
                }
                case "render_anim_transforms": {
                    Field f = getField("modelTransforms");
                    if (f == null) break;
                    int[][] transforms = (int[][]) f.get(target);
                    if (transforms == null) {
                        transforms = new int[12][];
                        f.set(target, transforms);
                    }
                    int modelIndex = stream.readUnsignedByte();
                    int[] transform = new int[6];
                    for (int i = 0; i < 6; i++) transform[i] = stream.readShort();
                    if (modelIndex >= 0 && modelIndex < transforms.length) {
                        transforms[modelIndex] = transform;
                    }
                    break;
                }
                case "skip_render_52": {
                    int count = stream.readUnsignedByte();
                    for (int i = 0; i < count; i++) {
                        stream.readUnsignedShort();
                        stream.readUnsignedByte();
                    }
                    break;
                }
                case "skip_render_56": {
                    stream.readUnsignedByte();
                    stream.readShort();
                    stream.readShort();
                    stream.readShort();
                    break;
                }
                default:
                    System.err.println("DefinitionHandler: unhandled type '" + def.type + "' for opcode " + opcode);
            }
        } catch (Exception e) {
            System.err.println("DefinitionHandler: error reading opcode " + opcode + " (" + def + "): " + e.getMessage());
        }
    }

    private void readTransforms(Object target, InputStream stream, boolean extended) {
        try {
            int varbit = normalizeId(stream.readUnsignedShort());
            int varp = normalizeId(stream.readUnsignedShort());
            int transformDefault = -1;
            if (extended) {
                transformDefault = normalizeId(stream.readUnsignedShort());
            }
            int count = stream.readUnsignedByte();
            int[] transforms = new int[count + 2];
            for (int i = 0; i <= count; i++) {
                transforms[i] = normalizeId(stream.readUnsignedShort());
            }
            transforms[count + 1] = transformDefault;
            setFieldInt("varbit", varbit, target);
            setFieldInt("varp", varp, target);
            setField("transforms", transforms, target);
            setFieldInt("transformDefault", transformDefault, target);
        } catch (Exception e) {
            System.err.println("DefinitionHandler: error reading transforms: " + e.getMessage());
        }
    }

    private void setField(String name, Object value, Object target) {
        Field f = getField(name);
        if (f != null) {
            try { f.set(target, value); } catch (Exception ignored) {}
        }
    }

    private void setFieldInt(String name, int value, Object target) {
        Field f = getField(name);
        if (f != null) {
            try { f.setInt(target, value); } catch (Exception ignored) {}
        }
    }

    private void setFieldBoolean(String name, boolean value, Object target) {
        Field f = getField(name);
        if (f != null) {
            try { f.setBoolean(target, value); } catch (Exception ignored) {}
        }
    }

    private void setFieldByte(String name, byte value, Object target) {
        Field f = getField(name);
        if (f != null) {
            try { f.setByte(target, value); } catch (Exception ignored) {}
        }
    }

    private void setArrayField(String name, int value, int index, Object target) {
        Field f = getField(name);
        if (f == null) return;
        try {
            int[] arr = (int[]) f.get(target);
            if (arr == null) {
                arr = new int[Math.max(index + 1, 10)];
                f.set(target, arr);
            }
            if (index >= 0 && index < arr.length) {
                arr[index] = value;
            }
        } catch (Exception ignored) {}
    }

    public void writeOpcode(Object target, OutputStream stream, int opcode) {
        FieldDefinition def = resolve(opcode);
        if (def == null || def.field == null || def.field.isEmpty()) return;
        try {
            Field f = getField(def.field);
            if (f == null) return;
            stream.writeByte(opcode);
            writeFieldData(f.get(target), stream, def);
        } catch (Exception e) {
            System.err.println("DefinitionHandler: error writing opcode " + opcode + ": " + e.getMessage());
        }
    }

    public void writeOpcodeValue(Object target, OutputStream stream, int opcode, int value) {
        FieldDefinition def = resolve(opcode);
        if (def == null) return;
        try {
            stream.writeByte(opcode);
            writeFieldData(value, stream, def);
        } catch (Exception e) {
            System.err.println("DefinitionHandler: error writing opcode " + opcode + ": " + e.getMessage());
        }
    }

    public void writeOpcodeValue(Object target, OutputStream stream, int opcode, String value) {
        FieldDefinition def = resolve(opcode);
        if (def == null) return;
        try {
            stream.writeByte(opcode);
            writeFieldData(value, stream, def);
        } catch (Exception e) {
            System.err.println("DefinitionHandler: error writing opcode " + opcode + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")

    private void writeFieldData(Object target, OutputStream stream, FieldDefinition def) {
        if (target == null) return;
        switch (def.type) {
            case "unsigned_byte":
                stream.writeByte(((Number) target).intValue());
                break;
            case "byte":
            case "byte_times_5":
            case "unsigned_short":
            case "offset_short":
                stream.writeShort(((Number) target).intValue());
                break;
            case "short":
                stream.writeShort(((Number) target).intValue());
                break;
            case "int":
                stream.writeInt(((Number) target).intValue());
                break;
            case "big_smart":
                stream.writeBigSmart(((Number) target).intValue());
                break;
            case "smart":
                stream.writeSmart(((Number) target).intValue());
                break;
            case "string":
                stream.writeString((String) target);
                break;
            case "24bit_int":
                stream.write24BitInt(((Number) target).intValue());
                break;
            case "string_array": {
                String[] arr = (String[]) target;
                if (def.index >= 0 && def.index < arr.length && arr[def.index] != null) {
                    stream.writeString(arr[def.index]);
                }
                break;
            }
        }
    }

    private static int normalizeId(int id) {
        return id == 65535 ? -1 : id;
    }

    private static String readResource(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(DefinitionHandler.class.getClassLoader().getResourceAsStream(path)),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("DefinitionHandler: could not load " + path + ": " + e.getMessage());
        }
        return sb.toString();
    }

    public static class FieldDefinition {

        public final String field;

        public final String type;

        public final int index;
        FieldDefinition(String field, String type, int index) {
            this.field = field;
            this.type = type;
            this.index = index;
        }

        @Override

        public String toString() {
            return "FieldDef{field='" + field + "', type='" + type + "', index=" + index + "}";
        }
    }
}
