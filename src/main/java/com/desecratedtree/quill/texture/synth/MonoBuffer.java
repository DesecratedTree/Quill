package com.desecratedtree.quill.texture.synth;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Row cache for monochrome layers. Port of {@code Class191}: {@code planes}
 * backing rows are handed out for row index {@code y} (up to {@code slots})
 * with an access-ordered LRU hint; {@link #fresh} reports whether the returned
 * row needs recomputing this pass.
 */
final class MonoBuffer {

    private final int width;
    private final int[][] rows;
    private final Map<Integer, Integer> assignment = new LinkedHashMap<>(16, 0.75f, true);

    /** {@code aBoolean2570} — true when {@link #row} returned an uncached row. */
    boolean fresh;

    MonoBuffer(int planes, int slots, int width) {
        this.width = width;
        this.rows = new int[Math.max(1, planes)][width];
    }

    int[] row(int y) {
        Integer plane = assignment.get(y);
        if (plane != null) {
            fresh = false;
            return rows[plane];
        }
        int target;
        if (assignment.size() < rows.length) {
            target = assignment.size();
        } else {
            Map.Entry<Integer, Integer> eldest = assignment.entrySet().iterator().next();
            target = eldest.getValue();
        }
        fresh = true;
        assignment.put(y, target);
        return rows[target];
    }

    void clear() {
        assignment.clear();
    }
}