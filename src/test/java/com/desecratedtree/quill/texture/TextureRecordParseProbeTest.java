package com.desecratedtree.quill.texture;

import com.desecratedtree.quill.cache.CacheManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class TextureRecordParseProbeTest {

    @Test
    void dumpFirstTextureRecordBytes() throws Exception {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        int[] ids = CacheManager.getArchiveIds(9);
        Assumptions.assumeTrue(ids.length > 0);
        byte[] data = CacheManager.getTextureData(ids[0]);
        System.err.println("TEXTURE " + ids[0] + " len=" + data.length);
        dump(data, 0, Math.min(128, data.length));
        int tail = Math.min(48, data.length);
        System.err.println("TAIL " + (data.length - tail) + ".." + data.length);
        dump(data, data.length - tail, tail);
    }

    private static void dump(byte[] data, int from, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < from + length; i++) {
            if (i > from) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", data[i] & 0xFF));
        }
        System.err.println(sb);
    }
}