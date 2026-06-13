package com.desecratedtree.quill.defs;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.util.RuntimeRevision;
import java.util.Arrays;
import java.util.HashMap;

public final class NpcDefinitions {

    private static NpcDefinitions[] DEFINITIONS = new NpcDefinitions[30000];

    public int id;

    public String name = "null";

    public int size = 1;

    public int combatLevel = -1;

    public int renderAnimId = -1;

    public int resizeX = 128;

    public int resizeY = 128;

    public boolean visibleOnMinimap = true;

    public boolean clickable = true;

    public int[] modelIds = new int[0];

    public int[][] modelTranslations;

    public int[] headModelIds = new int[0];

    public String[] options = new String[5];

    public int[] originalModelColors = new int[0];

    public int[] modifiedModelColors = new int[0];

    public short[] originalTextureIds = new short[0];

    public int[] modifiedTextureIds = new int[0];

    public int headIcon = -1;

    public HashMap<Integer, Object> params;

    public static NpcDefinitions get(int id) {
        ensureCapacity(id);
        if (id < 0 || id >= DEFINITIONS.length) {
            return null;
        }
        NpcDefinitions cached = DEFINITIONS[id];
        if (cached != null) {
            return cached;
        }
        NpcDefinitions definition = new NpcDefinitions();
        definition.id = id;
        byte[] data = RuntimeRevision.isOsrsMode() ? CacheManager.getOsrsNpcData(id) : CacheManager.getNpcData(id);
        if (data != null) {
            try {
                definition.decode(new InputStream(data));
            } catch (RuntimeException ex) {
                definition.name = "npc_" + id;
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
            DefinitionHandler.forType(NpcDefinitions.class, "definitions/npc.toml");

    private static final DefinitionHandler OSRS_HANDLER =
            DefinitionHandler.forType(NpcDefinitions.class, "definitions/osrs_npc.toml");

    private static DefinitionHandler handler() {
        return RuntimeRevision.isOsrsMode() ? OSRS_HANDLER : HANDLER;
    }

    private void decodeOpcode(InputStream stream, int opcode) {
        handler().read(this, stream, opcode);
    }
}
