package com.desecratedtree.quill.codec.item;

import com.desecratedtree.quill.codec.OutputStream;
import com.desecratedtree.quill.defs.DefinitionHandler;
import com.desecratedtree.quill.defs.ItemDefinitions;

public class ItemEncoder {

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(ItemDefinitions.class, "definitions/item.toml");

    public static byte[] encode(ItemDefinitions def) {
        OutputStream stream = new OutputStream();
        if (def.modelId != 0) {
            HANDLER.writeOpcodeValue(def, stream, 1, def.modelId);
        }
        if (def.name != null) {
            HANDLER.writeOpcodeValue(def, stream, 2, def.name);
        }
        if (def.modelZoom != 2000) {
            HANDLER.writeOpcodeValue(def, stream, 4, def.modelZoom);
        }
        if (def.modelRotation1 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 5, def.modelRotation1);
        }
        if (def.modelRotation2 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 6, def.modelRotation2);
        }
        if (def.modelOffset1 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 7, def.modelOffset1);
        }
        if (def.modelOffset2 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 8, def.modelOffset2);
        }
        if (def.stackable == 1) {
            stream.writeByte(11);
        }
        if (def.value != 1) {
            HANDLER.writeOpcodeValue(def, stream, 12, def.value);
        }
        if (def.equipSlot != -1) {
            HANDLER.writeOpcodeValue(def, stream, 13, def.equipSlot);
        }
        if (def.equipType != -1) {
            HANDLER.writeOpcodeValue(def, stream, 14, def.equipType);
        }
        if (def.membersOnly) {
            stream.writeByte(16);
        }
        if (def.maleEquip1 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 23, def.maleEquip1);
        }
        if (def.maleEquip2 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 24, def.maleEquip2);
        }
        if (def.femaleEquip1 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 25, def.femaleEquip1);
        }
        if (def.femaleEquip2 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 26, def.femaleEquip2);
        }
        if (def.groundOptions != null) {
            for (int i = 0; i < def.groundOptions.length; i++) {
                String option = def.groundOptions[i];
                if (option != null && !option.isEmpty()) {
                    stream.writeByte(30 + i);
                    stream.writeString(option);
                }
            }
        }
        if (def.inventoryOptions != null) {
            for (int i = 0; i < def.inventoryOptions.length; i++) {
                String option = def.inventoryOptions[i];
                if (option != null && !option.isEmpty()) {
                    stream.writeByte(35 + i);
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
        if (def.unknownArray1 != null) {
            stream.writeByte(42);
            stream.writeByte(def.unknownArray1.length);
            for (byte b : def.unknownArray1) {
                stream.writeByte(b);
            }
        }
        if (def.unnoted) {
            stream.writeByte(65);
        }
        if (def.maleEquipModelId3 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 78, def.maleEquipModelId3);
        }
        if (def.femaleEquipModelId3 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 79, def.femaleEquipModelId3);
        }
        if (def.certId != -1) {
            HANDLER.writeOpcodeValue(def, stream, 97, def.certId);
        }
        if (def.certTemplateId != -1) {
            HANDLER.writeOpcodeValue(def, stream, 98, def.certTemplateId);
        }
        if (def.stackIds != null) {
            for (int i = 0; i < def.stackIds.length; i++) {
                if (def.stackIds[i] != 0) {
                    stream.writeByte(100 + i);
                    stream.writeShort(def.stackIds[i]);
                    stream.writeShort(def.stackAmounts[i]);
                }
            }
        }
        if (def.unknownInt7 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 110, def.unknownInt7);
        }
        if (def.unknownInt8 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 111, def.unknownInt8);
        }
        if (def.unknownInt9 != 128) {
            HANDLER.writeOpcodeValue(def, stream, 112, def.unknownInt9);
        }
        if (def.unknownInt10 != 0) {
            HANDLER.writeOpcodeValue(def, stream, 113, def.unknownInt10);
        }
        if (def.unknownInt11 != 0) {
            stream.writeByte(114);
            stream.writeByte(def.unknownInt11 / 5);
        }
        if (def.teamId != -1) {
            HANDLER.writeOpcodeValue(def, stream, 115, def.teamId);
        }
        if (def.lendId != -1) {
            HANDLER.writeOpcodeValue(def, stream, 121, def.lendId);
        }
        if (def.lendTemplateId != -1) {
            HANDLER.writeOpcodeValue(def, stream, 122, def.lendTemplateId);
        }
        if (def.itemParams != null && !def.itemParams.isEmpty()) {
            stream.writeByte(249);
            stream.writeByte(def.itemParams.size());
            def.itemParams.forEach((key, value) -> {
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
        if (def.oldInvModel != -1) {
            stream.writeByte(242);
            stream.writeBigSmart(def.oldInvModel);
            stream.writeBigSmart(def.oldInvZoom);
        }
        if (def.oldMaleEquip3 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 243, def.oldMaleEquip3);
        }
        if (def.oldFemaleEquip3 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 244, def.oldFemaleEquip3);
        }
        if (def.oldMaleEquip2 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 245, def.oldMaleEquip2);
        }
        if (def.oldFemaleEquip2 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 246, def.oldFemaleEquip2);
        }
        if (def.oldMaleEquip1 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 247, def.oldMaleEquip1);
        }
        if (def.oldFemaleEquip1 != -1) {
            HANDLER.writeOpcodeValue(def, stream, 248, def.oldFemaleEquip1);
        }
        if (def.oldEquipType != -1) {
            HANDLER.writeOpcodeValue(def, stream, 250, def.oldEquipType);
        }
        if (def.oldModelColors != null) {
            stream.writeByte(251);
            stream.writeByte(def.oldModelColors.length);
            for (int i = 0; i < def.oldModelColors.length; i++) {
                stream.writeShort(def.oldModelColors[i]);
                stream.writeShort(def.oldModifiedModelColors[i]);
            }
        }
        if (def.oldModelTextures != null) {
            stream.writeByte(252);
            stream.writeByte(def.oldModelTextures.length);
            for (int i = 0; i < def.oldModelTextures.length; i++) {
                stream.writeShort(def.oldModelTextures[i]);
                stream.writeShort(def.oldModifiedModelTextures[i]);
            }
        }
        if (def.oldModelRotation1 != -1) {
            stream.writeByte(253);
            stream.writeShort(def.oldModelRotation1);
            stream.writeShort(def.oldModelRotation2);
            stream.writeShort(def.oldModelOffset1);
            stream.writeShort(def.oldModelOffset2);
        }
        stream.writeByte(0);
        return stream.toByteArray();
    }
}
