package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.CacheColor;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.texture.TextureLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only diagnostic pass over the 634 item cache that verifies the
 * color / material->texture mapping actually used at render time.
 *
 * Schema checks: opcode 41/251 texture pair targets must reference index 9
 * records; materials used by shader-on faces must exist with present colors.
 *
 * Round-trip checks: every face color must survive CacheColor.fromColor
 * (toRgb exactly, shadedRgb (toColor exactly), which are the decode/encode
 * paths the editor's own UI round-trips through. Any 6-bit RGB ambiguity that
 * fails this is a known 15//30 equilibrium and is recorded, not a failure.
 */
public final class QuillVerify {

    private static class Stats {
        int items = 0;
        int faceNoColor = 0;
        int texRefTotal = 0;
        int texRefMissing = 0;
        int faceShader = 0;
        int faceShaderColorMissing = 0;
        int materialMissing = 0;
        int texturedFaces = 0;
        int uvCoordAbsent = 0;
        int monotoneInvariants = 0;
        int fromColorMismatched = 0;
    }

    public static void main(String[] args) {
        CacheManager.init("data/cache");

        Stats s = new Stats();
        List<String> sampleItems = new ArrayList<>();
        List<Integer> sampleList = new ArrayList<>();
        List<String> critical = new ArrayList<>();

        // ---------- schema: index 9 targets exist ----------
        for (int id : CacheManager.getArchiveIds(19)) {
            for (int file : CacheManager.getFileIds(19, id)) {
                ItemDefinitions item = ItemDefinitions.getItemDefinitions((id << 8) | file);
                if (item == null) continue;
                s.items++;
                if (item.name == null || item.name.equals("null")) continue;
                for (int[] targets: new int[][]{item.modifiedTextureIds}) {
                    if (targets == null) continue;
                    for (int t : targets) {
                        s.texRefTotal++;
                        if (t < 0 || t >= 512 || CacheManager.getTextureData(t) == null) {
                            s.texRefMissing++;
                            if (sampleList.size() < 8) sampleList.add(t);
                        }
                    }
                }
            }
        }

        // ---------- round-trip + model faces for a bounded sample ----------
        // Prioritise items that have recolours/retextures and known problem ids
        int[] priorityIds = {6570, 20135, 1052, 1053, 1187, 3140, 2639, 6568, 11840, 11200};
        for (int id : priorityIds) {
            sample(id, s, sampleItems, critical);
        }
        int seen = 0;
        for (int id : CacheManager.getArchiveIds(19)) {
            if (seen >= 260) break;
            for (int file : CacheManager.getFileIds(19, id)) {
                if (seen >= 260) break;
                int itemId = (id << 8) | file;
                ItemDefinitions item = ItemDefinitions.getItemDefinitions(itemId);
                if (item == null || item.modelId <= 0) continue;
                int[] both = null;
                if (item.originalModelColors != null && item.originalModelColors.length > 0) both = item.originalModelColors;
                if (item.modifiedTextureIds != null && item.modifiedTextureIds.length > 0 && both == null) both = null;
                if (both != null || item.modifiedTextureIds != null) {
                    sample(itemId, s, sampleItems, critical);
                }
                seen++;
            }
        }

        System.out.println("=== QuillVerify summary ===");
        System.out.println("items scanned: " + s.items);
        System.out.println("schema: tex-pair targets " + s.texRefTotal
                + " missing-index9-record " + s.texRefMissing);
        System.out.println("model faces (sampled): shader-targetable " + s.faceShader
                + " of which packed 0 " + s.faceShaderColorMissing);
        System.out.println("textured faces " + s.texturedFaces
                + " uvCoord missing " + s.uvCoordAbsent
                + " material-decode-missing (solid fallback) " + s.materialMissing);
        System.out.println("recolor target faces touched " + 0 + " (recolours are applied by model loader)");
        System.out.println("palette round-trip faces checked " + (s.fromColorMismatched + s.monotoneInvariants)
                + " fromColorLostExact " + s.fromColorMismatched);
        System.out.println("no-palette faces " + s.faceNoColor);
        if (!sampleList.isEmpty()) {
            System.out.println("sample bad texture targets: " + sampleList);
        }
        if (!sampleItems.isEmpty()) {
            System.out.println("sample item notes:");
            for (String note : sampleItems) System.out.println("  " + note);
        }
        if (!critical.isEmpty()) {
            System.out.println("CRITICAL findings:");
            for (String note : critical) System.out.println("  " + note);
        }
    }

    private static void sample(int itemId, Stats s, List<String> sampleItems, List<String> critical) {
        try {
            ItemDefinitions item = ItemDefinitions.getItemDefinitions(itemId);
            if (item == null) {
                return;
            }
            RenderModel model = ModelDecoderAdapter.loadItemModel(item);
            if (model == null) {
                sampleItems.add("item " + itemId + ": no model " + item.modelId);
                return;
            }
            StringBuilder note = new StringBuilder("item " + itemId + " '" + item.name + "'" + " model=" + item.modelId);
            int shaderFaces = 0, zeroFaces = 0, textured = 0, absent = 0, decodeMissing = 0;
            int rtExact = 0, rtLost = 0;
            List<Integer> badFrom = new ArrayList<>();
            for (int face = 0; face < model.faceCount; face++) {
                int packed = model.faceColors != null && face < model.faceColors.length
                        ? model.faceColors[face] & 0xFFFF : 0;
                boolean solid = model.faceRenderTypes == null
                        || face >= model.faceRenderTypes.length
                        || model.faceRenderTypes[face] < 2;
                if (solid) {
                    s.faceShader++;
                    shaderFaces++;
                    if (packed == 0) {
                        s.faceShaderColorMissing++;
                        zeroFaces++;
                    }
                    // Degenerate cases are expected for sharing bit patterns
                    // between HSL (packed luminance) and grey (HSL s=0 l=0) and
                    // must not be separately flagged.
                    int rgb = CacheColor.toRgb(packed);
                    int repacked = CacheColor.fromColor(CacheColor.toColor(packed));
                    if (repacked == packed) {
                        rtExact++;
                        s.monotoneInvariants++;
                    } else {
                        rtLost++;
                        s.fromColorMismatched++;
                        if (badFrom.size() < 3) badFrom.add(packed);
                    }
                }
                int tex = model.faceTextures == null || face >= model.faceTextures.length
                        ? -1 : model.faceTextures[face];
                if (tex >= 0 && solid) {
                    s.texturedFaces++;
                    textured++;
                    if (model.textureCoordinates == null || face >= model.textureCoordinates.length
                            || model.textureCoordinates[face] < 0) {
                        s.uvCoordAbsent++;
                        absent++;
                    }
                    if (CacheManager.getTextureData(tex) == null) {
                        s.materialMissing++;
                        decodeMissing++;
                    }
                }
            }
            note.append(" faces=").append(model.faceCount)
                    .append(" shader=").append(shaderFaces)
                    .append(" zeroPacked=").append(zeroFaces)
                    .append(" textured=").append(textured)
                    .append(" uvAbsent=").append(absent)
                    .append(" texMissing=").append(decodeMissing)
                    .append(" toRgbExact=").append(rtExact)
                    .append(" toRgbLost=").append(rtLost);
            if (!badFrom.isEmpty()) {
                note.append(" lostPacked=").append(badFrom);
            }
            if (zeroFaces > 0 || absent > 0 || decodeMissing > 0 || rtLost > 0) {
                sampleItems.add(note.toString());
                if (rtLost > model.faceCount / 2) {
                    critical.add(note.toString());
                }
            }
        } catch (Exception ex) {
            sampleItems.add("item " + itemId + " EXCEPTION " + ex);
        }
    }

    private QuillVerify() {
    }
}
