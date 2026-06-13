package com.desecratedtree.quill.defs;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.util.RuntimeRevision;
import java.util.Arrays;
import java.util.HashMap;

public final class ObjectDefinitions {

    private static ObjectDefinitions[] DEFINITIONS = new ObjectDefinitions[40000];

    public int id;

    public int[][] modelIds;

    public byte[] modelTypes;

    public String name = "null";

    public int sizeX = 1;

    public int sizeY = 1;

    public boolean blocksSky = true;

    public int solid = 2;

    public int interactive = -1;

    public byte contouredGround;

    public boolean delayShading;

    public int offsetMultiplier = 64;

    public int brightness;

    public String[] options = new String[6];

    public String[] memberOptions = new String[5];

    public int contrast;

    public short[] originalColours = new short[0];

    public short[] modifiedColours = new short[0];

    public short[] originalTextureColours = new short[0];

    public short[] modifiedTextureColours = new short[0];

    public byte[] recolourPalette = new byte[0];

    public boolean mirrored;

    public boolean castsShadow = true;

    public int modelSizeX = 128;

    public int modelSizeZ = 128;

    public int modelSizeY = 128;

    public int blockFlag;

    public int offsetX;

    public int offsetZ;

    public int offsetY;

    public boolean blocksLand;

    public boolean ignoreOnRoute;

    public int supportItems = -1;

    public int varbit = -1;

    public int varp = -1;

    public int[] transforms;

    public int transformDefault = -1;

    public int anInt3015 = -1;

    public int anInt3012;

    public int anInt2989;

    public int anInt2971;

    public int[] anIntArray3036;

    public int anInt3023 = -1;

    public boolean hideMinimap;

    public boolean aBoolean2972 = true;

    public boolean animateImmediately = true;

    public boolean isMembers;

    public boolean aBoolean3056;

    public boolean aBoolean2998;

    public int anInt2987 = -1;

    public int anInt3008 = -1;

    public int anInt3038 = -1;

    public int anInt3013 = -1;

    public int anInt2958;

    public int mapscene = -1;

    public int culling = -1;

    public int anInt3024 = 255;

    public boolean invertMapScene;

    public int[] animations;

    public int[] percents;

    public int mapDefinitionId = -1;

    public int[] anIntArray2981;

    public byte aByte2974;

    public byte aByte3045;

    public byte aByte3052;

    public byte aByte2960;

    public int anInt2964;

    public int anInt2963;

    public int anInt3018;

    public int anInt2983;

    public boolean aBoolean2961;

    public boolean aBoolean2993;

    public int anInt3032 = 960;

    public int anInt2962;

    public int anInt3050 = 256;

    public int anInt3020 = 256;

    public boolean aBoolean2992;

    public int anInt2975;

    public HashMap<Integer, Object> params;

    public static ObjectDefinitions get(int id) {
        ensureCapacity(id);
        if (id < 0 || id >= DEFINITIONS.length) {
            return null;
        }
        ObjectDefinitions cached = DEFINITIONS[id];
        if (cached != null) {
            return cached;
        }
        ObjectDefinitions definition = new ObjectDefinitions();
        definition.id = id;
        byte[] data = RuntimeRevision.isOsrsMode() ? CacheManager.getOsrsObjectData(id) : CacheManager.getObjectData(id);
        if (data != null && data.length > 0) {
            try {
                definition.decode(new InputStream(data));
            } catch (RuntimeException ex) {
                definition.name = "object_" + id;
            }
        }
        DEFINITIONS[id] = definition;
        return definition;
    }

    public static void invalidate(int id) {
        if (id < 0) {
            return;
        }
        ensureCapacity(id);
        DEFINITIONS[id] = null;
    }

    public static void clearDefinitions() {
        Arrays.fill(DEFINITIONS, null);
    }

    private static void ensureCapacity(int id) {
        if (id < 0 || id < DEFINITIONS.length) {
            return;
        }
        int newSize = DEFINITIONS.length;
        while (newSize <= id) {
            newSize = Math.max(newSize + 1, newSize * 2);
        }
        DEFINITIONS = Arrays.copyOf(DEFINITIONS, newSize);
    }

    private void decode(InputStream stream) {
        while (stream.getRemaining() > 0) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0) {
                return;
            }
            decodeOpcode(stream, opcode);
        }
    }

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(ObjectDefinitions.class, "definitions/object.toml");

    private static final DefinitionHandler OSRS_HANDLER =
            DefinitionHandler.forType(ObjectDefinitions.class, "definitions/osrs_object.toml");

    private static DefinitionHandler handler() {
        return RuntimeRevision.isOsrsMode() ? OSRS_HANDLER : HANDLER;
    }

    private void decodeOpcode(InputStream stream, int opcode) {
        handler().read(this, stream, opcode);
    }
}
