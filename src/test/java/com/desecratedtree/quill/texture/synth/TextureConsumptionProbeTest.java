package com.desecratedtree.quill.texture.synth;

import com.desecratedtree.quill.cache.CacheManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class TextureConsumptionProbeTest {

    @Test
    void compareConsumptionAcrossIndices() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        Map<String, Integer> tailTally = new LinkedHashMap<>();
        int ok9 = 0;
        int short9 = 0;
        for (int id : CacheManager.getArchiveIds(9)) {
            byte[] data = CacheManager.getTextureData(id);
            if (data == null) {
                continue;
            }
            try {
                TextureProgram p = new TextureProgram(data);
                String tail = hex(data, data.length - 10, 10);
                tailTally.merge(tail, 1, Integer::sum);
                if (p.consumed == data.length) {
                    ok9++;
                } else {
                    short9++;
                }
            } catch (RuntimeException ex) {
                System.err.println("IDX9 id=" + id + " EX=" + ex);
            }
        }
        System.err.println("IDX9 ok=" + ok9 + " short=" + short9 + " tailVariants=" + tailTally.size());
        int shown = 0;
        for (Map.Entry<String, Integer> e : tailTally.entrySet()) {
            if (shown++ < 12) {
                System.err.println("  tail=" + e.getKey() + " x" + e.getValue());
            }
        }
        List<Integer> matIds = new ArrayList<>();
        for (int id : CacheManager.getMaterialIds()) {
            matIds.add(id);
            if (matIds.size() >= 12) {
                break;
            }
        }
        for (int id : matIds) {
            byte[] data = CacheManager.getMaterialData(id);
            if (data == null) {
                continue;
            }
            try {
                TextureProgram p = new TextureProgram(data);
                System.err.println("IDX26 id=" + id + " len=" + data.length + " consumed=" + p.consumed
                        + " delta=" + (data.length - p.consumed) + " layers=" + p.layers.length);
            } catch (RuntimeException ex) {
                System.err.println("IDX26 id=" + id + " len=" + data.length + " EX=" + ex);
            }
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