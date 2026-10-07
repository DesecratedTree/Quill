package com.desecratedtree.quill.texture.synth;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Row cache for colour layers. Port of {@code Class322}: each backing row holds
 * the three channel buffers ({@code {R, G, B}}); {@link #fresh} reports whether
 * the returned row needs recomputing this pass.
 */
final class ColorBuffer {

    private final int width;
    private final int[][][] rows;
    private final Map<Integer, Integer> assignment = new LinkedHashMap<>(16, 0.75f, true);

    /** {@code aBoolean4035} — true when {@link #row} returned an uncached row. */
    boolean fresh;

    ColorBuffer(int planes, int slots, int width) {
        this.width = width;
        this.rows = new int[Math.max(1, planes)][3][width];
    }

    int[][] row(int y) {
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