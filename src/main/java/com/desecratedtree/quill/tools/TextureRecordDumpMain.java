package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;

public final class TextureRecordDumpMain {

    private TextureRecordDumpMain() {
    }

    public static void main(String[] args) {
        String cachePath = args.length > 0 ? args[0] : "data/cache";
        CacheManager.init(cachePath);
        int[] ids = CacheManager.getArchiveIds(9);
        System.out.println("index9 archives=" + ids.length);
        for (int id : ids) {
            byte[] data = CacheManager.getTextureData(id);
            if (data == null) {
                continue;
            }
            System.out.println("TEXTURE " + id + " len=" + data.length + " first=" + hex(data, 0, Math.min(32, data.length)));
        }
        byte[] defs = CacheManager.getMaterialDefinitionsData();
        System.out.println("defs len=" + (defs == null ? -1 : defs.length));
        byte[] firstTexture = CacheManager.getTextureData(ids.length > 0 ? ids[0] : 0);
        if (firstTexture != null) {
            System.out.println("FULL first texture hex:");
            System.out.println(hex(firstTexture, 0, Math.min(256, firstTexture.length)));
        }
    }

    private static String hex(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            if (i > offset) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", data[i] & 0xFF));
        }
        return sb.toString();
    }
}