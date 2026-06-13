package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.defs.NpcDefinitions;
import com.desecratedtree.quill.defs.ObjectDefinitions;
import com.desecratedtree.quill.codec.item.ItemSaver;
import com.desecratedtree.quill.codec.npc.NpcSaver;
import com.desecratedtree.quill.codec.object.ObjectSaver;

public final class DefinitionActions {

    private DefinitionActions() {
    }

    public static int addItem() {
        int newId = CacheManager.getItemCount();
        ItemDefinitions definition = new ItemDefinitions(newId, false);
        ItemSaver.save(definition);
        ItemDefinitions.invalidate(newId);
        return newId;
    }

    public static int duplicateItem(int sourceId) {
        int newId = CacheManager.getItemCount();
        byte[] data = CacheManager.getItemData(sourceId);
        CacheManager.writeItemData(newId, data == null ? ItemSaver.encodeDefault(newId) : data.clone());
        ItemDefinitions.invalidate(newId);
        return newId;
    }

    public static void deleteItem(int itemId) {
        ItemSaver.save(new ItemDefinitions(itemId, false));
        ItemDefinitions.invalidate(itemId);
    }

    public static int addNpc() {
        int newId = CacheManager.getNpcCount();
        NpcDefinitions definition = new NpcDefinitions();
        definition.id = newId;
        NpcSaver.save(definition);
        NpcDefinitions.invalidate(newId);
        return newId;
    }

    public static int duplicateNpc(int sourceId) {
        int newId = CacheManager.getNpcCount();
        byte[] data = CacheManager.getNpcData(sourceId);
        CacheManager.writeNpcData(newId, data == null ? NpcSaver.encodeDefault(newId) : data.clone());
        NpcDefinitions.invalidate(newId);
        return newId;
    }

    public static void deleteNpc(int npcId) {
        NpcDefinitions definition = new NpcDefinitions();
        definition.id = npcId;
        NpcSaver.save(definition);
        NpcDefinitions.invalidate(npcId);
    }

    public static int addObject() {
        int newId = CacheManager.getObjectCount();
        ObjectDefinitions definition = new ObjectDefinitions();
        definition.id = newId;
        ObjectSaver.save(definition);
        ObjectDefinitions.invalidate(newId);
        return newId;
    }

    public static int duplicateObject(int sourceId) {
        int newId = CacheManager.getObjectCount();
        byte[] data = CacheManager.getObjectData(sourceId);
        CacheManager.writeObjectData(newId, data == null ? ObjectSaver.encodeDefault(newId) : data.clone());
        ObjectDefinitions.invalidate(newId);
        return newId;
    }

    public static void deleteObject(int objectId) {
        ObjectDefinitions definition = new ObjectDefinitions();
        definition.id = objectId;
        ObjectSaver.save(definition);
        ObjectDefinitions.invalidate(objectId);
    }
}
