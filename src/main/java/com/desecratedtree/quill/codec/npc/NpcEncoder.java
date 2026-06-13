package com.desecratedtree.quill.codec.npc;

import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.defs.DefinitionHandler;
import com.desecratedtree.quill.defs.NpcDefinitions;

public final class NpcEncoder {

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(NpcDefinitions.class, "definitions/npc.toml");

    private NpcEncoder() {
    }

    public static byte[] encode(NpcDefinitions def) {
        OutputStream stream = new OutputStream(512);
        if (def.modelIds != null && def.modelIds.length > 0) {
            stream.writeByte(1);
            stream.writeByte(def.modelIds.length);
            for (int modelId : def.modelIds) stream.writeShort(modelId < 0 ? 65535 : modelId);
        }
        if (def.name != null && !def.name.equals("null")) {
            HANDLER.writeOpcodeValue(def, stream, 2, def.name);
        }
        if (def.size != 1) {
            stream.writeByte(12);
            stream.writeByte(def.size);
        }
        if (def.options != null) {
            for (int i = 0; i < Math.min(5, def.options.length); i++) {
                String option = def.options[i];
                if (option != null && !option.trim().isEmpty()) {
                    stream.writeByte(30 + i);
                    stream.writeString(option);
                }
            }
        }
        if (def.originalModelColors != null && def.originalModelColors.length > 0) {
            stream.writeByte(40);
            stream.writeByte(def.originalModelColors.length);
            for (int i = 0; i < def.originalModelColors.length; i++) {
                stream.writeShort(def.originalModelColors[i]);
                stream.writeShort(def.modifiedModelColors[i]);
            }
        }
        if (def.originalTextureIds != null && def.originalTextureIds.length > 0) {
            stream.writeByte(41);
            stream.writeByte(def.originalTextureIds.length);
            for (int i = 0; i < def.originalTextureIds.length; i++) {
                stream.writeShort(def.originalTextureIds[i]);
                stream.writeShort(def.modifiedTextureIds[i]);
            }
        }
        if (def.headModelIds != null && def.headModelIds.length > 0) {
            stream.writeByte(60);
            stream.writeByte(def.headModelIds.length);
            for (int modelId : def.headModelIds) stream.writeShort(modelId < 0 ? 65535 : modelId);
        }
        if (def.modelTranslations != null) {
            int count = 0;
            for (int[] translation : def.modelTranslations) {
                if (translation != null && translation.length >= 3) count++;
            }
            if (count > 0) {
                stream.writeByte(121);
                stream.writeByte(count);
                for (int i = 0; i < def.modelTranslations.length; i++) {
                    int[] translation = def.modelTranslations[i];
                    if (translation == null || translation.length < 3) continue;
                    stream.writeByte(i);
                    stream.writeByte(translation[0]);
                    stream.writeByte(translation[1]);
                    stream.writeByte(translation[2]);
                }
            }
        }
        if (def.headIcon >= 0) {
            HANDLER.writeOpcodeValue(def, stream, 102, def.headIcon);
        }
        if (!def.visibleOnMinimap) stream.writeByte(93);
        if (def.combatLevel >= 0) {
            HANDLER.writeOpcodeValue(def, stream, 95, def.combatLevel);
        }
        if (def.resizeX != 128) {
            HANDLER.writeOpcodeValue(def, stream, 97, def.resizeX);
        }
        if (def.resizeY != 128) {
            HANDLER.writeOpcodeValue(def, stream, 98, def.resizeY);
        }
        if (!def.clickable) stream.writeByte(107);
        if (def.renderAnimId >= 0) {
            HANDLER.writeOpcodeValue(def, stream, 127, def.renderAnimId);
        }
        stream.writeByte(0);
        return stream.toByteArray();
    }
}
