package com.desecratedtree.quill.codec.object;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ObjectDefinitions;

public final class ObjectSaver {

    private ObjectSaver() {
    }

    public static void save(ObjectDefinitions definition) {
        CacheManager.writeObjectData(definition.id, ObjectEncoder.encode(definition));
        ObjectDefinitions.invalidate(definition.id);
    }

    public static byte[] encodeDefault(int id) {
        ObjectDefinitions definition = new ObjectDefinitions();
        definition.id = id;
        return ObjectEncoder.encode(definition);
    }
}
