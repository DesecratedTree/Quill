package com.desecratedtree.quill.codec.npc;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.NpcDefinitions;

public final class NpcSaver {

    private NpcSaver() {
    }

    public static void save(NpcDefinitions definition) {
        CacheManager.writeNpcData(definition.id, NpcEncoder.encode(definition));
        NpcDefinitions.invalidate(definition.id);
    }

    public static byte[] encodeDefault(int id) {
        NpcDefinitions definition = new NpcDefinitions();
        definition.id = id;
        return NpcEncoder.encode(definition);
    }
}
