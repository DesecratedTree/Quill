package com.desecratedtree.quill.defs;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.util.RuntimeRevision;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")

public final class ItemDefinitions {

    public static ItemDefinitions[] itemsDefinitions;
    static {
        itemsDefinitions = new ItemDefinitions[30000];
    }

    public static final int ATTACK = 0, DEFENCE = 1, STRENGTH = 2,
            HITPOINTS = 3, RANGE = 4, PRAYER = 5, MAGIC = 6, COOKING = 7,
            WOODCUTTING = 8, FLETCHING = 9, FISHING = 10, FIREMAKING = 11,
            CRAFTING = 12, SMITHING = 13, MINING = 14, HERBLORE = 15,
            AGILITY = 16, THIEVING = 17, SLAYER = 18, FARMING = 19,
            RUNECRAFTING = 20, CONSTRUCTION = 22, HUNTER = 21, SUMMONING = 23,
            DUNGEONEERING = 24;

    public static final ItemDefinitions getItemDefinitions(int itemId) {
        return defs(itemId);
    }

    public static final ItemDefinitions defs(int itemId) {
        ensureCapacity(itemId);
        if (itemId < 0 || itemId >= itemsDefinitions.length)
            itemId = 0;
        ItemDefinitions def = itemsDefinitions[itemId];
        if (def != null)
            return def;
        def = new ItemDefinitions(itemId, false);
        itemsDefinitions[itemId] = def;
        def.loadItemDefinitions();
        return def;
    }

    public static final void clearItemsDefinitions() {
        for (int i = 0; i < itemsDefinitions.length; i++)
            itemsDefinitions[i] = null;
    }

    public static void invalidate(int itemId) {
        if (itemId < 0) {
            return;
        }
        ensureCapacity(itemId);
        itemsDefinitions[itemId] = null;
    }

    private static void ensureCapacity(int itemId) {
        if (itemId < itemsDefinitions.length) {
            return;
        }
        int newSize = itemsDefinitions.length;
        while (newSize <= itemId) {
            newSize = Math.max(newSize + 1, newSize * 2);
        }
        itemsDefinitions = Arrays.copyOf(itemsDefinitions, newSize);
    }

    public static ItemDefinitions forName(String name) {
        for (ItemDefinitions definition : itemsDefinitions) {
            if (definition != null && definition.name != null && definition.name.equalsIgnoreCase(name))
                return definition;
        }
        return null;
    }

    public int id;

    public boolean loaded;

    public int modelId;

    public String name;

    public int modelZoom;

    public int modelRotation1;

    public int modelRotation2;

    public int modelOffset1;

    public int modelOffset2;

    public short[] originalTextureIds;

    public int[] modifiedTextureIds;

    public int stackable;

    public int value;

    public boolean membersOnly;

    public int maleEquip1;

    public int femaleEquip1;

    public int maleEquip2;

    public int femaleEquip2;

    public String[] groundOptions;

    public String[] inventoryOptions;

    public int[] originalModelColors;

    public int[] modifiedModelColors;

    public short[] originalTextureColors;

    public short[] modifiedTextureColors;

    public byte[] unknownArray1;

    public byte[] unknownArray3;

    public int[] unknownArray2;

    public boolean unnoted;

    public int maleEquipModelId3;

    public int femaleEquipModelId3;

    public int unknownInt1;

    public int unknownInt2;

    public int unknownInt3;

    public int unknownInt4;

    public int unknownInt5;

    public int unknownInt6;

    public int certId;

    public int certTemplateId;

    public int[] stackIds;

    public int[] stackAmounts;

    public int unknownInt7;

    public int unknownInt8;

    public int unknownInt9;

    public int unknownInt10;

    public int unknownInt11;

    public int teamId;

    public int lendId;

    public int lendTemplateId;

    public int unknownInt12;

    public int unknownInt13;

    public int unknownInt14;

    public int unknownInt15;

    public int unknownInt16;

    public int unknownInt17;

    public int unknownInt18;

    public int unknownInt19;

    public int unknownInt20;

    public int unknownInt21;

    public int unknownInt22;

    public int unknownInt23;

    public int equipSlot;

    public int equipType;

    public int oldInvModel;

    public int oldInvZoom;

    public int oldModelRotation1;

    public int oldModelRotation2;

    public int oldModelOffset1;

    public int oldModelOffset2;

    public int oldMaleEquip1;

    public int oldMaleEquip2;

    public int oldMaleEquip3;

    public int oldFemaleEquip1;

    public int oldFemaleEquip2;

    public int oldFemaleEquip3;

    public int oldEquipType;

    public short[] oldModelColors, oldModifiedModelColors;

    public short[] oldModelTextures, oldModifiedModelTextures;

    public boolean noted;

    public boolean lended;

    public HashMap<Integer, Object> itemParams;

    public HashMap<Integer, Integer> itemRequiriments;

    public int[] unknownArray5;

    public int[] unknownArray4;

    public byte[] unknownArray6;

    public int unknownValue1;

    public int unknownValue2;

    public ItemDefinitions(int id, boolean load) {
        this.id = id;
        setDefaultsVariableValues();
        setDefaultOptions();
        if (load)
            loadItemDefinitions();
    }

    public ItemDefinitions(int id) {
        this(id, true);
    }

    public ItemDefinitions() {
        this.modelId = 0;
        this.name = "";
        this.value = 1;
        this.stackable = 0;
        this.equipSlot = -1;
        this.equipType = -1;
        this.membersOnly = false;
        this.unnoted = false;
        this.maleEquip1 = -1;
        this.maleEquip2 = -1;
        this.femaleEquip1 = -1;
        this.femaleEquip2 = -1;
        this.groundOptions = new String[5];
        this.inventoryOptions = new String[5];
        this.originalModelColors = new int[0];
        this.modifiedModelColors = new int[0];
        this.originalTextureIds = new short[0];
        this.modifiedTextureIds = new int[0];
    }

    public boolean isLoaded() {
        return loaded;
    }

    public final void loadItemDefinitions() {
        byte[] data = RuntimeRevision.isOsrsMode() ? CacheManager.getOsrsItemData(id) : CacheManager.getItemData(id);
        if (data == null)
            return;
        readOpcodeValues(new InputStream(data));
        loaded = true;
        if (certTemplateId != -1 && certId != id)
            toNote();
        if (lendTemplateId != -1 && lendId != id)
            toLend();
        if (unknownValue1 != -1 && unknownValue2 != id)
            toBind();
    }

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(ItemDefinitions.class, "definitions/item.toml");

    private static final DefinitionHandler OSRS_HANDLER =
            DefinitionHandler.forType(ItemDefinitions.class, "definitions/osrs_item.toml");

    private static DefinitionHandler handler() {
        return RuntimeRevision.isOsrsMode() ? OSRS_HANDLER : HANDLER;
    }

    public final void readOpcodeValues(InputStream stream) {
        while (true) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            handler().read(this, stream, opcode);
        }
    }

    public String dumpOpcodeText() {
        byte[] data = CacheManager.getItemData(id);
        if (data == null) {
            return "item_id: " + id + "\nerror: no item data found";
        }
        InputStream stream = new InputStream(data);
        StringBuilder dump = new StringBuilder();
        appendDumpLine(dump, "item_id", id);
        while (true) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0) {
                break;
            }
            appendOpcodeDumpLine(dump, stream, opcode);
        }
        return dump.toString();
    }

    private void appendOpcodeDumpLine(StringBuilder dump, InputStream stream, int opcode) {
        if (opcode == 1) {
            appendDumpLine(dump, "model_id", stream.readUnsignedShort());
        } else if (opcode == 2) {
            appendDumpLine(dump, "name", stream.readString());
        } else if (opcode == 4) {
            appendDumpLine(dump, "model_zoom", stream.readUnsignedShort());
        } else if (opcode == 5) {
            appendDumpLine(dump, "model_rotation_x", stream.readUnsignedShort());
        } else if (opcode == 6) {
            appendDumpLine(dump, "model_rotation_y", stream.readUnsignedShort());
        } else if (opcode == 7) {
            int value = stream.readUnsignedShort();
            if (value > 32767) value -= 65536;
            appendDumpLine(dump, "model_offset_x", value);
        } else if (opcode == 8) {
            int value = stream.readUnsignedShort();
            if (value > 32767) value -= 65536;
            appendDumpLine(dump, "model_offset_y", value);
        } else if (opcode == 11) {
            appendDumpLine(dump, "stackable", true);
        } else if (opcode == 12) {
            appendDumpLine(dump, "value", stream.readInt());
        } else if (opcode == 13) {
            appendDumpLine(dump, "equip_slot", stream.readUnsignedByte());
        } else if (opcode == 14) {
            appendDumpLine(dump, "equip_type", stream.readUnsignedByte());
        } else if (opcode == 16) {
            appendDumpLine(dump, "members_only", true);
        } else if (opcode == 18) {
            appendDumpLine(dump, "opcode_18", stream.readUnsignedShort());
        } else if (opcode == 23) {
            appendDumpLine(dump, "male_equip_1", stream.readUnsignedShort());
        } else if (opcode == 24) {
            appendDumpLine(dump, "male_equip_2", stream.readUnsignedShort());
        } else if (opcode == 25) {
            appendDumpLine(dump, "female_equip_1", stream.readUnsignedShort());
        } else if (opcode == 26) {
            appendDumpLine(dump, "female_equip_2", stream.readUnsignedShort());
        } else if (opcode == 27) {
            appendDumpLine(dump, "opcode_27", stream.readUnsignedByte());
        } else if (opcode >= 30 && opcode < 35) {
            appendDumpLine(dump, "ground_option_" + (opcode - 30), stream.readString());
        } else if (opcode >= 35 && opcode < 40) {
            appendDumpLine(dump, "inventory_option_" + (opcode - 35), stream.readString());
        } else if (opcode == 40) {
            int length = stream.readUnsignedByte();
            List<String> mappings = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                mappings.add(stream.readUnsignedShort() + "->" + stream.readUnsignedShort());
            }
            appendDumpLine(dump, "recolor_map", joinList(mappings));
        } else if (opcode == 41) {
            int length = stream.readUnsignedByte();
            List<String> mappings = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                mappings.add(stream.readUnsignedShort() + "->" + stream.readUnsignedShort());
            }
            appendDumpLine(dump, "retexture_map", joinList(mappings));
        } else if (opcode == 42) {
            int length = stream.readUnsignedByte();
            byte[] values = new byte[length];
            for (int index = 0; index < length; index++) {
                values[index] = (byte) stream.readByte();
            }
            appendDumpLine(dump, "opcode_42", Arrays.toString(values));
        } else if (opcode == 44) {
            appendDumpLine(dump, "opcode_44", stream.readUnsignedShort());
        } else if (opcode == 45) {
            appendDumpLine(dump, "opcode_45", stream.readUnsignedShort());
        } else if (opcode == 65) {
            appendDumpLine(dump, "unnoted", true);
        } else if (opcode == 78) {
            appendDumpLine(dump, "male_equip_3", stream.readUnsignedShort());
        } else if (opcode == 79) {
            appendDumpLine(dump, "female_equip_3", stream.readUnsignedShort());
        } else if (opcode == 90) {
            appendDumpLine(dump, "male_head_1", stream.readUnsignedShort());
        } else if (opcode == 91) {
            appendDumpLine(dump, "female_head_1", stream.readUnsignedShort());
        } else if (opcode == 92) {
            appendDumpLine(dump, "male_head_2", stream.readUnsignedShort());
        } else if (opcode == 93) {
            appendDumpLine(dump, "female_head_2", stream.readUnsignedShort());
        } else if (opcode == 95) {
            appendDumpLine(dump, "inventory_rotation_z", stream.readUnsignedShort());
        } else if (opcode == 96) {
            appendDumpLine(dump, "opcode_96", stream.readUnsignedByte());
        } else if (opcode == 97) {
            appendDumpLine(dump, "cert_id", stream.readUnsignedShort());
        } else if (opcode == 98) {
            appendDumpLine(dump, "cert_template_id", stream.readUnsignedShort());
        } else if (opcode >= 100 && opcode < 110) {
            int stackId = stream.readUnsignedShort();
            int stackAmount = stream.readUnsignedShort();
            appendDumpLine(dump, "stack_variant_" + (opcode - 100), "item=" + stackId + ", amount=" + stackAmount);
        } else if (opcode == 110) {
            appendDumpLine(dump, "resize_x", stream.readUnsignedShort());
        } else if (opcode == 111) {
            appendDumpLine(dump, "resize_y", stream.readUnsignedShort());
        } else if (opcode == 112) {
            appendDumpLine(dump, "resize_z", stream.readUnsignedShort());
        } else if (opcode == 113) {
            appendDumpLine(dump, "ambient", stream.readByte());
        } else if (opcode == 114) {
            appendDumpLine(dump, "contrast", stream.readByte() * 5);
        } else if (opcode == 115) {
            appendDumpLine(dump, "team_id", stream.readUnsignedByte());
        } else if (opcode == 121) {
            appendDumpLine(dump, "lend_id", stream.readUnsignedShort());
        } else if (opcode == 122) {
            appendDumpLine(dump, "lend_template_id", stream.readUnsignedShort());
        } else if (opcode == 124) {
            List<Integer> values = new ArrayList<>(6);
            for (int i = 0; i < 6; i++) {
                values.add(stream.readUnsignedShort());
            }
            appendDumpLine(dump, "opcode_124", joinList(values));
        } else if (opcode == 125) {
            appendDumpLine(dump, "male_wear_offsets", stream.readByte() + ", " + stream.readByte() + ", " + stream.readByte());
        } else if (opcode == 126) {
            appendDumpLine(dump, "female_wear_offsets", stream.readByte() + ", " + stream.readByte() + ", " + stream.readByte());
        } else if (opcode == 127) {
            appendDumpLine(dump, "cursor_1", "type=" + stream.readUnsignedByte() + ", sprite=" + stream.readUnsignedShort());
        } else if (opcode == 128) {
            appendDumpLine(dump, "cursor_2", "type=" + stream.readUnsignedByte() + ", sprite=" + stream.readUnsignedShort());
        } else if (opcode == 129) {
            appendDumpLine(dump, "opcode_129", "type=" + stream.readUnsignedByte() + ", value=" + stream.readUnsignedShort());
        } else if (opcode == 130) {
            appendDumpLine(dump, "opcode_130", "type=" + stream.readUnsignedByte() + ", value=" + stream.readUnsignedShort());
        } else if (opcode == 132) {
            int length = stream.readUnsignedByte();
            List<Integer> quests = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                quests.add(stream.readUnsignedShort());
            }
            appendDumpLine(dump, "quests", joinList(quests));
        } else if (opcode == 134) {
            appendDumpLine(dump, "pick_size_shift", stream.readUnsignedByte());
        } else if (opcode == 139) {
            appendDumpLine(dump, "bind_link_id", stream.readUnsignedShort());
        } else if (opcode == 140) {
            appendDumpLine(dump, "bind_template_id", stream.readUnsignedShort());
        } else if (opcode >= 142 && opcode < 147) {
            appendDumpLine(dump, "opcode_" + opcode, stream.readUnsignedShort());
        } else if (opcode == 147) {
            appendDumpLine(dump, "opcode_147", stream.readUnsignedShort());
        } else if (opcode >= 150 && opcode < 155) {
            appendDumpLine(dump, "opcode_" + opcode, stream.readUnsignedShort());
        } else if (opcode == 242) {
            appendDumpLine(dump, "legacy_inventory_model", stream.readBigSmart());
            appendDumpLine(dump, "legacy_inventory_zoom", stream.readBigSmart());
        } else if (opcode == 243) {
            appendDumpLine(dump, "legacy_male_equip_3", stream.readBigSmart());
        } else if (opcode == 244) {
            appendDumpLine(dump, "legacy_female_equip_3", stream.readBigSmart());
        } else if (opcode == 245) {
            appendDumpLine(dump, "legacy_male_equip_2", stream.readBigSmart());
        } else if (opcode == 246) {
            appendDumpLine(dump, "legacy_female_equip_2", stream.readBigSmart());
        } else if (opcode == 247) {
            appendDumpLine(dump, "legacy_male_equip_1", stream.readBigSmart());
        } else if (opcode == 248) {
            appendDumpLine(dump, "legacy_female_equip_1", stream.readBigSmart());
        } else if (opcode == 249) {
            int length = stream.readUnsignedByte();
            List<String> params = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                boolean stringInstance = stream.readUnsignedByte() == 1;
                int key = stream.read24BitInt();
                Object value = stringInstance ? stream.readString() : stream.readInt();
                params.add(key + "=" + value);
            }
            appendDumpLine(dump, "params", joinList(params));
        } else if (opcode == 250) {
            appendDumpLine(dump, "legacy_equip_type", stream.readUnsignedByte());
        } else if (opcode == 251) {
            int length = stream.readUnsignedByte();
            List<String> mappings = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                mappings.add(stream.readUnsignedShort() + "->" + stream.readUnsignedShort());
            }
            appendDumpLine(dump, "legacy_recolor_map", joinList(mappings));
        } else if (opcode == 252) {
            int length = stream.readUnsignedByte();
            List<String> mappings = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                mappings.add(stream.readUnsignedShort() + "->" + stream.readUnsignedShort());
            }
            appendDumpLine(dump, "legacy_retexture_map", joinList(mappings));
        } else if (opcode == 253) {
            appendDumpLine(dump, "legacy_rotation_x", stream.readUnsignedShort());
            appendDumpLine(dump, "legacy_rotation_y", stream.readUnsignedShort());
            appendDumpLine(dump, "legacy_offset_x", stream.readUnsignedShort());
            appendDumpLine(dump, "legacy_offset_y", stream.readUnsignedShort());
        } else {
            appendDumpLine(dump, "unhandled_opcode_" + opcode, "(dump aborted)");
            throw new IllegalArgumentException("Unhandled opcode " + opcode + " for item " + id);
        }
    }

    private void appendDumpLine(StringBuilder dump, String key, Object value) {
        dump.append(key).append(": ").append(value).append('\n');
    }

    private String joinList(List<?> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(values.get(index));
        }
        builder.append(']');
        return builder.toString();
    }

    public void toNote() {
        ItemDefinitions realItem = defs(certId);
        membersOnly = realItem.membersOnly;
        value = realItem.value;
        name = realItem.name;
        stackable = 1;
        noted = true;
    }

    public void toLend() {
        ItemDefinitions realItem = defs(lendId);
        originalModelColors = realItem.originalModelColors;
        maleEquipModelId3 = realItem.maleEquipModelId3;
        femaleEquipModelId3 = realItem.femaleEquipModelId3;
        teamId = realItem.teamId;
        value = 0;
        membersOnly = realItem.membersOnly;
        name = realItem.name;
        inventoryOptions = new String[5];
        groundOptions = realItem.groundOptions;
        if (realItem.inventoryOptions != null)
            for (int i = 0; i < 4; i++)
                inventoryOptions[i] = realItem.inventoryOptions[i];
        inventoryOptions[4] = "Discard";
        maleEquip1 = realItem.maleEquip1;
        maleEquip2 = realItem.maleEquip2;
        femaleEquip1 = realItem.femaleEquip1;
        femaleEquip2 = realItem.femaleEquip2;
        itemParams = realItem.itemParams;
        equipSlot = realItem.equipSlot;
        equipType = realItem.equipType;
        lended = true;
    }

    public void toBind() {
        ItemDefinitions realItem = defs(unknownValue2);
        originalModelColors = realItem.originalModelColors;
        maleEquipModelId3 = realItem.maleEquipModelId3;
        femaleEquipModelId3 = realItem.femaleEquipModelId3;
        teamId = realItem.teamId;
        value = 0;
        membersOnly = realItem.membersOnly;
        name = realItem.name;
        inventoryOptions = new String[5];
        groundOptions = realItem.groundOptions;
        if (realItem.inventoryOptions != null)
            for (int i = 0; i < 4; i++)
                inventoryOptions[i] = realItem.inventoryOptions[i];
        inventoryOptions[4] = "Discard";
        maleEquip1 = realItem.maleEquip1;
        maleEquip2 = realItem.maleEquip2;
        femaleEquip1 = realItem.femaleEquip1;
        femaleEquip2 = realItem.femaleEquip2;
        itemParams = realItem.itemParams;
        equipSlot = realItem.equipSlot;
        equipType = realItem.equipType;
    }

    public void setDefaultOptions() {
        groundOptions = new String[]{ null, null, "take", null, null };
        inventoryOptions = new String[]{ null, null, null, null, "drop" };
    }

    public void setDefaultsVariableValues() {
        name = "null";
        maleEquip1 = -1;
        maleEquip2 = -1;
        femaleEquip1 = -1;
        femaleEquip2 = -1;
        modelZoom = 2000;
        lendId = -1;
        lendTemplateId = -1;
        certId = -1;
        certTemplateId = -1;
        unknownInt9 = 128;
        value = 1;
        maleEquipModelId3 = -1;
        femaleEquipModelId3 = -1;
        unknownValue1 = -1;
        unknownValue2 = -1;
        teamId = -1;
        equipSlot = -1;
        equipType = -1;
        oldInvModel = -1;
        oldInvZoom = -1;
        oldModelRotation1 = -1;
        oldModelRotation2 = -1;
        oldModelOffset1 = -1;
        oldModelOffset2 = -1;
        oldMaleEquip1 = -1;
        oldMaleEquip2 = -1;
        oldMaleEquip3 = -1;
        oldFemaleEquip1 = -1;
        oldFemaleEquip2 = -1;
        oldFemaleEquip3 = -1;
        oldEquipType = -1;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName()      { return name; }

    public int getId()           { return id; }

    public int getEquipSlot()    { return equipSlot; }

    public int getEquipType()    { return equipType; }

    public int getModelId()      { return modelId; }

    public int getModelZoom()    { return modelZoom; }

    public int getModelOffset1() { return modelOffset1; }

    public int getModelOffset2() { return modelOffset2; }

    public int getLendId()       { return lendId; }

    public int getFemaleWornModelId1() { return femaleEquip1; }

    public int getFemaleWornModelId2() { return femaleEquip2; }

    public int getMaleWornModelId1()   { return maleEquip1; }

    public int getMaleWornModelId2()   { return maleEquip2; }

    public boolean isWearItem()     { return equipSlot != -1; }

    public boolean isOverSized()    { return modelZoom > 5000; }

    public boolean isLended()       { return lended; }

    public boolean isMembersOnly()  { return membersOnly; }

    public boolean isStackable()    { return stackable == 1; }

    public boolean isNoted()        { return noted; }

    public boolean isDestroyItem() {
        if (inventoryOptions == null) return false;
        for (String option : inventoryOptions)
            if (option != null && option.equalsIgnoreCase("destroy"))
                return true;
        return false;
    }

    public boolean containsOption(int i, String option) {
        if (inventoryOptions == null || inventoryOptions.length <= i || inventoryOptions[i] == null)
            return false;
        return inventoryOptions[i].equals(option);
    }

    public boolean containsOption(String option) {
        if (inventoryOptions == null) return false;
        for (String o : inventoryOptions)
            if (o != null && o.equals(option))
                return true;
        return false;
    }

    public int getCertId() {
        if (!canNote()) return -1;
        return certId;
    }

    public boolean canNote() {
        if (certId == -1) return false;
        if (name.contains("Extreme")) return false;
        String notedName = ItemDefinitions.getItemDefinitions(certId).name;
        return name.equalsIgnoreCase(notedName);
    }

    public HashMap<Integer, Object> getItemParams() { return itemParams; }

    public Object getClientScriptData(int id) {
        if (itemParams == null) return false;
        return itemParams.get(id);
    }

    private int getScriptInt(int key) {
        if (itemParams == null) return 0;
        Object v = itemParams.get(key);
        return (v instanceof Integer) ? (int) v : 0;
    }

    public int getStageOnDeath() {
        if (itemParams == null) return 0;
        Object v = itemParams.get(1397);
        return (v instanceof Integer) ? (Integer) v : 0;
    }

    public int getArchiveId()    { return getId() >>> 8; }

    public int getFileId()       { return 0xff & getId(); }

    public int getStabAttack()        { return getScriptInt(0); }

    public int getSlashAttack()       { return getScriptInt(1); }

    public int getCrushAttack()       { return getScriptInt(2); }

    public int getMagicAttack()       { return getScriptInt(3); }

    public int getRangeAttack()       { return getScriptInt(4); }

    public int getStabDef()           { return getScriptInt(5); }

    public int getSlashDef()          { return getScriptInt(6); }

    public int getCrushDef()          { return getScriptInt(7); }

    public int getMagicDef()          { return getScriptInt(8); }

    public int getRangeDef()          { return getScriptInt(9); }

    public int getPrayerBonus()       { return getScriptInt(11); }

    public int getMagicDamage()       { return getScriptInt(685); }

    public int getAbsorveMeleeBonus() { return getScriptInt(967); }

    public int getAbsorveRangeBonus() { return getScriptInt(968); }

    public int getAbsorveMageBonus()  { return getScriptInt(969); }

    public int getStrengthBonus()     { return getScriptInt(641) / 10; }

    public int getRangedStrBonus()    { return getScriptInt(643) / 10; }

    public int getSummoningDef() {
        if (id > 25439 || itemParams == null) return 0;
        return getScriptInt(417);
    }

    public int getAttackSpeed() {
        if (itemParams == null) return 4;
        Object v = itemParams.get(14);
        return (v instanceof Integer) ? (int) v : 4;
    }

    public int getRenderAnimId() {
        if (itemParams == null) return 1426;
        Object v = itemParams.get(644);
        return (v instanceof Integer) ? (Integer) v : 1426;
    }

    public int getCombatInterfaceType() {
        if (itemParams == null) return -1;
        Object v = itemParams.get(686);
        return (v instanceof Integer) ? (Integer) v : -1;
    }

    public boolean hasSpecialBar() {
        if (getName().toLowerCase().contains("whip") || getId() == 23695) return true;
        if (itemParams == null) return false;
        Object v = itemParams.get(687);
        return (v instanceof Integer) && (Integer) v == 1;
    }

    public int getQuestId() {
        if (itemParams == null) return -1;
        Object v = itemParams.get(861);
        return (v instanceof Integer) ? (Integer) v : -1;
    }
}
