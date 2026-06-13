package com.desecratedtree.quill.codec.object;

import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.defs.DefinitionHandler;
import com.desecratedtree.quill.defs.ObjectDefinitions;
import java.util.Map;

public final class ObjectEncoder {

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(ObjectDefinitions.class, "definitions/object.toml");

    private ObjectEncoder() {
    }

    public static byte[] encode(ObjectDefinitions definition) {
        OutputStream stream = new OutputStream();
        if (definition.modelIds != null && definition.modelTypes != null) {
            stream.writeByte(1);
            stream.writeByte(definition.modelIds.length);
            for (int i = 0; i < definition.modelIds.length; i++) {
                int[] models = definition.modelIds[i] == null ? new int[0] : definition.modelIds[i];
                int type = i < definition.modelTypes.length ? definition.modelTypes[i] : 10;
                stream.writeByte(type);
                stream.writeByte(models.length);
                for (int model : models) {
                    stream.writeShort(model < 0 ? 65535 : model);
                }
            }
        }
        if (definition.name != null && !"null".equals(definition.name)) {
            HANDLER.writeOpcodeValue(definition, stream, 2, definition.name);
        }
        if (definition.sizeX != 1) {
            stream.writeByte(14);
            stream.writeByte(definition.sizeX);
        }
        if (definition.sizeY != 1) {
            stream.writeByte(15);
            stream.writeByte(definition.sizeY);
        }
        if (!definition.blocksSky) {
            stream.writeByte(definition.solid == 0 ? 17 : 18);
        }
        if (definition.interactive != -1) {
            stream.writeByte(19);
            stream.writeByte(definition.interactive);
        }
        if (definition.contouredGround == 1) {
            stream.writeByte(21);
        }
        if (definition.delayShading) {
            stream.writeByte(22);
        }
        if (definition.culling == 1) {
            stream.writeByte(23);
        }
        if (definition.animations != null && definition.animations.length == 1 && definition.percents == null) {
            stream.writeByte(24);
            stream.writeShort(definition.animations[0] < 0 ? 65535 : definition.animations[0]);
        }
        if (definition.solid == 1) {
            stream.writeByte(27);
        }
        if (definition.offsetMultiplier != 64) {
            stream.writeByte(28);
            stream.writeByte(definition.offsetMultiplier >> 2);
        }
        if (definition.brightness != 0) {
            stream.writeByte(29);
            stream.writeByte(definition.brightness);
        }
        for (int i = 0; i < 5; i++) {
            String option = definition.options[i];
            if (option != null && !option.isEmpty()) {
                stream.writeByte(30 + i);
                stream.writeString(option);
            }
        }
        if (definition.contrast != 0) {
            stream.writeByte(39);
            stream.writeByte(definition.contrast / 5);
        }
        writeRecolors(definition, stream);
        writeRetextures(definition, stream);
        writePalette(definition, stream);
        if (definition.mirrored) {
            stream.writeByte(62);
        }
        if (!definition.castsShadow) {
            stream.writeByte(64);
        }
        if (definition.modelSizeX != 128) {
            HANDLER.writeOpcodeValue(definition, stream, 65, definition.modelSizeX);
        }
        if (definition.modelSizeZ != 128) {
            HANDLER.writeOpcodeValue(definition, stream, 66, definition.modelSizeZ);
        }
        if (definition.modelSizeY != 128) {
            HANDLER.writeOpcodeValue(definition, stream, 67, definition.modelSizeY);
        }
        if (definition.blockFlag != 0) {
            stream.writeByte(69);
            stream.writeByte(definition.blockFlag);
        }
        if (definition.offsetX != 0) {
            stream.writeByte(70);
            stream.writeShort(definition.offsetX >> 2);
        }
        if (definition.offsetZ != 0) {
            stream.writeByte(71);
            stream.writeShort(definition.offsetZ >> 2);
        }
        if (definition.offsetY != 0) {
            stream.writeByte(72);
            stream.writeShort(definition.offsetY >> 2);
        }
        if (definition.blocksLand) {
            stream.writeByte(73);
        }
        if (definition.ignoreOnRoute) {
            stream.writeByte(74);
        }
        if (definition.supportItems != -1) {
            stream.writeByte(75);
            stream.writeByte(definition.supportItems);
        }
        writeTransforms(definition, stream);
        if (definition.anInt3015 != -1 || definition.anInt3012 != 0) {
            stream.writeByte(78);
            stream.writeShort(definition.anInt3015 < 0 ? 65535 : definition.anInt3015);
            stream.writeByte(definition.anInt3012);
        }
        if (definition.anIntArray3036 != null) {
            stream.writeByte(79);
            stream.writeShort(definition.anInt2989);
            stream.writeShort(definition.anInt2971);
            stream.writeByte(definition.anInt3012);
            stream.writeByte(definition.anIntArray3036.length);
            for (int value : definition.anIntArray3036) {
                stream.writeShort(value < 0 ? 65535 : value);
            }
        }
        if (definition.contouredGround == 2) {
            stream.writeByte(81);
            stream.writeByte(definition.anInt3023 / 256);
        }
        if (definition.hideMinimap) {
            stream.writeByte(82);
        }
        if (!definition.aBoolean2972) {
            stream.writeByte(88);
        }
        if (!definition.animateImmediately) {
            stream.writeByte(89);
        }
        if (definition.isMembers) {
            stream.writeByte(91);
        }
        if (definition.contouredGround == 3 && definition.anInt3023 >= 0 && definition.anInt3023 < Short.MAX_VALUE) {
            stream.writeByte(93);
            stream.writeShort(definition.anInt3023);
        }
        if (definition.contouredGround == 4) {
            stream.writeByte(94);
        }
        if (definition.contouredGround == 5) {
            stream.writeByte(95);
            stream.writeShort(definition.anInt3023);
        }
        if (definition.aBoolean3056) {
            stream.writeByte(97);
        }
        if (definition.aBoolean2998) {
            stream.writeByte(98);
        }
        if (definition.anInt2987 != -1 || definition.anInt3008 != -1) {
            stream.writeByte(99);
            stream.writeByte(Math.max(0, definition.anInt2987));
            stream.writeShort(definition.anInt3008 < 0 ? 65535 : definition.anInt3008);
        }
        if (definition.anInt3038 != -1 || definition.anInt3013 != -1) {
            stream.writeByte(100);
            stream.writeByte(Math.max(0, definition.anInt3038));
            stream.writeShort(definition.anInt3013 < 0 ? 65535 : definition.anInt3013);
        }
        if (definition.anInt2958 != 0) {
            stream.writeByte(101);
            stream.writeByte(definition.anInt2958);
        }
        if (definition.mapscene != -1) {
            HANDLER.writeOpcodeValue(definition, stream, 102, definition.mapscene < 0 ? 65535 : definition.mapscene);
        }
        if (definition.culling == 0) {
            stream.writeByte(103);
        }
        if (definition.anInt3024 != 255) {
            stream.writeByte(104);
            stream.writeByte(definition.anInt3024);
        }
        if (definition.invertMapScene) {
            stream.writeByte(105);
        }
        if (definition.animations != null && definition.percents != null) {
            stream.writeByte(106);
            stream.writeByte(definition.animations.length);
            int total = 0;
            for (int percent : definition.percents) {
                total += percent;
            }
            if (total <= 0) {
                total = definition.percents.length;
            }
            for (int i = 0; i < definition.animations.length; i++) {
                stream.writeShort(definition.animations[i] < 0 ? 65535 : definition.animations[i]);
                int percent = i < definition.percents.length ? definition.percents[i] : 0;
                stream.writeByte(Math.max(0, Math.min(255, percent * 100 / total)));
            }
        }
        if (definition.mapDefinitionId != -1) {
            HANDLER.writeOpcodeValue(definition, stream, 107, definition.mapDefinitionId < 0 ? 65535 : definition.mapDefinitionId);
        }
        for (int i = 0; i < definition.memberOptions.length; i++) {
            String option = definition.memberOptions[i];
            if (option != null && !option.isEmpty()) {
                stream.writeByte(150 + i);
                stream.writeString(option);
            }
        }
        if (definition.anIntArray2981 != null) {
            stream.writeByte(160);
            stream.writeByte(definition.anIntArray2981.length);
            for (int value : definition.anIntArray2981) {
                stream.writeShort(value);
            }
        }
        if (definition.contouredGround == 3 && definition.anInt3023 >= Short.MAX_VALUE) {
            stream.writeByte(162);
            stream.writeInt(definition.anInt3023);
        }
        if (definition.aByte2974 != 0 || definition.aByte3045 != 0 || definition.aByte3052 != 0 || definition.aByte2960 != 0) {
            stream.writeByte(163);
            stream.writeByte(definition.aByte2974);
            stream.writeByte(definition.aByte3045);
            stream.writeByte(definition.aByte3052);
            stream.writeByte(definition.aByte2960);
        }
        if (definition.anInt2964 != 0) {
            HANDLER.writeOpcodeValue(definition, stream, 164, definition.anInt2964);
        }
        if (definition.anInt2963 != 0) {
            HANDLER.writeOpcodeValue(definition, stream, 165, definition.anInt2963);
        }
        if (definition.anInt3018 != 0) {
            HANDLER.writeOpcodeValue(definition, stream, 166, definition.anInt3018);
        }
        if (definition.anInt2983 != 0) {
            stream.writeByte(167);
            stream.writeShort(definition.anInt2983);
        }
        if (definition.aBoolean2961) {
            stream.writeByte(168);
        }
        if (definition.aBoolean2993) {
            stream.writeByte(169);
        }
        if (definition.anInt3032 != 960) {
            stream.writeByte(170);
            stream.writeSmart(definition.anInt3032);
        }
        if (definition.anInt2962 != 0) {
            stream.writeByte(171);
            stream.writeSmart(definition.anInt2962);
        }
        if (definition.anInt3050 != 256 || definition.anInt3020 != 256) {
            stream.writeByte(173);
            stream.writeShort(definition.anInt3050);
            stream.writeShort(definition.anInt3020);
        }
        if (definition.aBoolean2992) {
            stream.writeByte(177);
        }
        if (definition.anInt2975 != 0) {
            stream.writeByte(178);
            stream.writeByte(definition.anInt2975);
        }
        writeParams(definition, stream);
        stream.writeByte(0);
        return stream.toByteArray();
    }

    private static void writeRecolors(ObjectDefinitions definition, OutputStream stream) {
        if (definition.originalColours == null || definition.modifiedColours == null || definition.originalColours.length == 0) {
            return;
        }
        int count = Math.min(definition.originalColours.length, definition.modifiedColours.length);
        stream.writeByte(40);
        stream.writeByte(count);
        for (int i = 0; i < count; i++) {
            stream.writeShort(definition.originalColours[i] & 0xFFFF);
            stream.writeShort(definition.modifiedColours[i] & 0xFFFF);
        }
    }

    private static void writeRetextures(ObjectDefinitions definition, OutputStream stream) {
        if (definition.originalTextureColours == null || definition.modifiedTextureColours == null
                || definition.originalTextureColours.length == 0) {
            return;
        }
        int count = Math.min(definition.originalTextureColours.length, definition.modifiedTextureColours.length);
        stream.writeByte(41);
        stream.writeByte(count);
        for (int i = 0; i < count; i++) {
            stream.writeShort(definition.originalTextureColours[i] & 0xFFFF);
            stream.writeShort(definition.modifiedTextureColours[i] & 0xFFFF);
        }
    }

    private static void writePalette(ObjectDefinitions definition, OutputStream stream) {
        if (definition.recolourPalette == null || definition.recolourPalette.length == 0) {
            return;
        }
        stream.writeByte(42);
        stream.writeByte(definition.recolourPalette.length);
        for (byte value : definition.recolourPalette) {
            stream.writeByte(value);
        }
    }

    private static void writeTransforms(ObjectDefinitions definition, OutputStream stream) {
        if (definition.transforms == null || definition.transforms.length == 0) {
            return;
        }
        boolean extended = definition.transformDefault != -1;
        stream.writeByte(extended ? 92 : 77);
        stream.writeShort(definition.varbit < 0 ? 65535 : definition.varbit);
        stream.writeShort(definition.varp < 0 ? 65535 : definition.varp);
        if (extended) {
            stream.writeShort(definition.transformDefault < 0 ? 65535 : definition.transformDefault);
        }
        int count = definition.transforms.length - 2;
        stream.writeByte(Math.max(0, count));
        for (int i = 0; i <= count; i++) {
            int value = i < definition.transforms.length ? definition.transforms[i] : -1;
            stream.writeShort(value < 0 ? 65535 : value);
        }
    }

    private static void writeParams(ObjectDefinitions definition, OutputStream stream) {
        if (definition.params == null || definition.params.isEmpty()) {
            return;
        }
        stream.writeByte(249);
        stream.writeByte(definition.params.size());
        for (Map.Entry<Integer, Object> entry : definition.params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                stream.writeByte(1);
                stream.write24BitInt(entry.getKey());
                stream.writeString((String) value);
            } else {
                stream.writeByte(0);
                stream.write24BitInt(entry.getKey());
                stream.writeInt(value instanceof Number ? ((Number) value).intValue() : 0);
            }
        }
    }
}
