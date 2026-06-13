package com.desecratedtree.quill.cache;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.archive.Archive;
import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public final class CacheManager {

    private static final int NPC_COUNT = 30000;

    private static CacheLibrary library;

    private CacheManager() {
    }

    public static void init(String path) {
        library = Objects.requireNonNull(CacheLibrary.create(path), "Unable to open cache: " + path);
    }

    public static void initCache(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Cache Folder");
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            CacheManager.init(path);
        } else {
            JOptionPane.showMessageDialog(parent, "Cache is required.");
            System.exit(0);
        }
    }

    public static byte[] getItemData(int id) {
        requireInitialized();
        return library.data(19, id >>> 8, id & 0xFF);
    }

    public static void writeItemData(int id, byte[] data) {
        requireInitialized();
        library.put(19, id >>> 8, id & 0xFF, data);
        library.index(19).update();
    }

    public static int getItemCount() {
        requireInitialized();
        int maxItemId = -1;
        for (int archiveId : getArchiveIds(19)) {
            for (int fileId : getFileIds(19, archiveId)) {
                maxItemId = Math.max(maxItemId, (archiveId << 8) | fileId);
            }
        }
        return Math.max(0, maxItemId + 1);
    }

    public static byte[] getNpcData(int id) {
        requireInitialized();
        return library.data(18, id >>> 7, id & 0x7F);
    }

    public static void writeNpcData(int id, byte[] data) {
        requireInitialized();
        library.put(18, id >>> 7, id & 0x7F, data);
        library.index(18).update();
    }

    public static int getNpcCount() {
        requireInitialized();
        int maxNpcId = -1;
        for (int archiveId : getArchiveIds(18)) {
            for (int fileId : getFileIds(18, archiveId)) {
                maxNpcId = Math.max(maxNpcId, (archiveId << 7) | fileId);
            }
        }
        return Math.max(0, maxNpcId + 1);
    }

    public static byte[] getModelData(int modelId) {
        requireInitialized();
        return library.data(7, modelId);
    }

    public static byte[] getObjectData(int id) {
        requireInitialized();
        return library.data(16, id >>> 8, id & 0xFF);
    }

    public static void writeObjectData(int id, byte[] data) {
        requireInitialized();
        library.put(16, id >>> 8, id & 0xFF, data);
        library.index(16).update();
    }

    public static int getObjectCount() {
        requireInitialized();
        int maxObjectId = -1;
        for (int archiveId : getArchiveIds(16)) {
            for (int fileId : getFileIds(16, archiveId)) {
                maxObjectId = Math.max(maxObjectId, (archiveId << 8) | fileId);
            }
        }
        return Math.max(0, maxObjectId + 1);
    }

    public static byte[] getOsrsNpcData(int id) {
        requireInitialized();
        return library.data(2, 9, id);
    }

    public static byte[] getOsrsItemData(int id) {
        requireInitialized();
        return library.data(2, 10, id);
    }

    public static byte[] getOsrsObjectData(int id) {
        requireInitialized();
        return library.data(2, 6, id);
    }

    public static byte[] getConfigData(int group, int file) {
        requireInitialized();
        return library.data(2, group, file);
    }

    public static byte[] getIndexData(int index, int archive) {
        requireInitialized();
        return library.data(index, archive);
    }

    public static byte[] getIndexData(int index, int archive, int file) {
        requireInitialized();
        return library.data(index, archive, file);
    }

    public static void writeIndexData(int index, int archive, byte[] data) {
        requireInitialized();
        library.put(index, archive, data);
        library.index(index).update();
    }

    public static void removeIndexData(int index, int archive) {
        requireInitialized();
        library.remove(index, archive);
        library.index(index).update();
    }

    public static void writeIndexData(int index, int archive, int file, byte[] data) {
        requireInitialized();
        library.put(index, archive, file, data);
        library.index(index).update();
    }

    public static int[] getModelIds() {
        requireInitialized();
        return library.index(7).archiveIds();
    }

    public static int[] getArchiveIds(int index) {
        requireInitialized();
        return library.index(index).archiveIds();
    }

    public static int[] getFileIds(int index, int archive) {
        requireInitialized();
        Archive entry = library.index(index).archive(archive);
        return entry == null ? new int[0] : entry.fileIds();
    }

    public static void writeModelData(int modelId, byte[] data) {
        requireInitialized();
        library.put(7, modelId, data);
        library.index(7).update();
    }

    public static byte[] getSpriteData(int spriteId) {
        requireInitialized();
        return library.data(8, spriteId);
    }

    public static int[] getSpriteIds() {
        requireInitialized();
        return library.index(8).archiveIds();
    }

    public static byte[] getTextureData(int textureId) {
        requireInitialized();
        return library.data(9, textureId);
    }

    public static byte[] getMaterialDefinitionsData() {
        requireInitialized();
        return library.data(26, 0, 0);
    }

    public static byte[] getMaterialData(int materialId) {
        requireInitialized();
        return library.data(26, 0, materialId);
    }

    public static int[] getMaterialIds() {
        requireInitialized();
        Archive entry = library.index(26).archive(0);
        return entry == null ? new int[0] : entry.fileIds();
    }

    private static void requireInitialized() {
        if (library == null) {
            throw new IllegalStateException("CacheManager has not been initialized.");
        }
    }
}
