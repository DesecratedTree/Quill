package com.desecratedtree.quill.texture.synth;

import com.desecratedtree.quill.cache.CacheManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parse-consumption validation for the 634 texture-program records.
 *
 * The reference client (634, GregHib/void-client Class244.method1881 + Class348_Sub42_Sub5)
 * feeds the whole index-9 archive to the texture-program decoder, which stops after the
 * three layer indexes; the final 10 bytes of every archive are trailer the reference
 * client itself never reads. So a faithful port must land on data.length - 10.
 *
 * Three classes of anomaly exist in this 634 cache and are encoded here:
 * 1. IDs in {@link #MALFORMED_IDS} desync the 634 parser at the same structural points the
 *    reference client would crash on (layer type outside 0..39 -> method557 returns null ->
 *    NPE; record ends mid-parse -> AIOOBE; designated layer index out of range -> AIOOBE).
 *    These are asserted to throw; they are not format bugs in the port.
 * 2. Texture 597 parses cleanly but carries a 337-byte structured extension between the
 *    layer-index triple and the 10-byte trailer. The 634 client stops at the same point, so
 *    the port is faithful; the exact consumed offset is pinned so regressions surface.
 */
class TextureProgramParseAllTest {

    /** Records whose bytes the 634 parser cannot decode (see class javadoc). Sorted. */
    private static final int[] MALFORMED_IDS = {796, 805, 806, 821, 822, 823, 897};

    /** Id -> consumed offset pinned for records whose byte layout 634 leaves partially unread. */
    private static final int[] EXTENSION_CONSUMED = {597, 238};

    @Test
    void everyIndex9RecordIsConsumedToTrailer() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache), "cache not present");
        CacheManager.init(cache.toString());
        int[] ids = CacheManager.getArchiveIds(9);
        Assumptions.assumeTrue(ids.length > 0, "index 9 has no archives");

        int parsed = 0;
        int skipped = 0;
        int malformed = 0;
        int extension = 0;
        List<String> failures = new ArrayList<>();

        for (int id : ids) {
            byte[] data = CacheManager.getTextureData(id);
            if (data == null) {
                skipped++;
                continue;
            }
            boolean isMalformed = contains(MALFORMED_IDS, id);
            boolean isExtension = id == EXTENSION_CONSUMED[0];
            int consumed;
            boolean threw;
            try {
                TextureProgram program = new TextureProgram(data);
                consumed = program.consumed;
                threw = false;
            } catch (RuntimeException ex) {
                consumed = -1;
                threw = true;
            }
            if (isMalformed) {
                malformed++;
                if (!threw) {
                    failures.add("texture " + id + ": expected malformed record to throw");
                }
                continue;
            }
            if (threw) {
                failures.add("texture " + id + " (" + data.length + " bytes): unexpected throw");
                continue;
            }
            if (isExtension) {
                extension++;
                if (consumed != EXTENSION_CONSUMED[1]) {
                    failures.add("texture " + id + ": consumed " + consumed
                            + " of " + data.length + " bytes, expected " + EXTENSION_CONSUMED[1]);
                }
                continue;
            }
            int expected = Math.max(0, data.length - 10);
            if (consumed != expected) {
                failures.add("texture " + id + ": consumed " + consumed
                        + " of " + data.length + " bytes, expected " + expected);
            }
            parsed++;
        }
        System.err.println("index9 archives=" + ids.length + " parsed=" + parsed
                + " malformed=" + malformed + " extension=" + extension
                + " skipped=" + skipped + (failures.isEmpty() ? " ALL CONSUMED" : ""));
        if (!failures.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int shown = Math.min(failures.size(), 40);
            for (int i = 0; i < shown; i++) {
                sb.append("  ").append(failures.get(i)).append('\n');
            }
            if (failures.size() > shown) {
                sb.append("  ... and ").append(failures.size() - shown).append(" more\n");
            }
            assertTrue(failures.isEmpty(), failures.size() + " records failed:\n" + sb);
        }
    }

    private static boolean contains(int[] sorted, int value) {
        for (int v : sorted) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }
}