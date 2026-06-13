package com.desecratedtree.quill.codec.item;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;

public class ItemSaver {

    public static void save(ItemDefinitions def) {
        CacheManager.writeItemData(def.getId(), ItemEncoder.encode(def));
        ItemDefinitions.invalidate(def.getId());
    }

    public static byte[] encodeDefault(int id) {
        return ItemEncoder.encode(new ItemDefinitions(id, false));
    }
}
