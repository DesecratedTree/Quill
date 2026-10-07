package com.desecratedtree.quill.texture.synth;

import com.desecratedtree.quill.cache.CacheManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class TextureRecordByteProbeTest {

    @Test
    void dumpHeadsAndTails() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        for (int id : new int[] {0, 4, 6, 12, 22}) {
            byte[] data = CacheManager.getTextureData(id);
            if (data == null) {
                continue;
            }
            System.err.println("ID " + id + " len=" + data.length + " HEAD=" + hex(data, 0, Math.min(16, data.length)));
            System.err.println("ID " + id + " TAIL=" + hex(data, Math.max(0, data.length - 16), Math.min(16, data.length)));
            TextureProgram program = new TextureProgram(data);
            System.err.println("ID " + id + " consumed=" + program.consumed + " layers=" + program.layers.length);
        }
    }

    private static String hex(byte[] data, int from, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < from + length; i++) {
            if (i > from) {
                sb.append(' ');
            }
            sb.append(String.format("%02x", data[i] & 0xFF));
        }
        return sb.toString();
    }
}