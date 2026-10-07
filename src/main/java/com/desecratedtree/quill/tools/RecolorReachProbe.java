package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;

/**
 * Verifies that opcode-40/41 recolour and retexture pairs actually reach model
 * faces: decodes the shared base model directly, then decodes it again through
 * the item definition and counts faces that changed. A recoloured item whose
 * diff is zero for both colours and textures is a pipeline no-op and a
 * plausible cause of 'wrong colour' reports.
 */
public final class RecolorReachProbe {

    private static final int[] IDS = {
            1038, 1039, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1048,
            287, 288, 289, 290,
            6570, 20135, 1052, 1187, 3140, 6568, 11200, 11840,
            7594, 8850, 10370, 11283, 11284, 14484, 15427,
    };

    public static void main(String[] args) {
        CacheManager.init("data/cache");
        int noopColor = 0;
        int noopTexture = 0;
        for (int itemId : IDS) {
            ItemDefinitions item = ItemDefinitions.getItemDefinitions(itemId);
            if (item == null || item.modelId <= 0) {
                System.out.println("item " + itemId + ": no model");
                continue;
            }
            RenderModel with = ModelDecoderAdapter.loadItemModel(item);
            RenderModel base = ModelDecoderAdapter.loadModel(item.modelId);
            if (with == null || base == null) {
                System.out.println("item " + itemId + ": decode failed");
                continue;
            }
            int colorChanged = 0;
            int textureChanged = 0;
            int pairs = item.originalModelColors == null ? 0 : item.originalModelColors.length;
            int texPairs = item.originalTextureIds == null ? 0 : item.originalTextureIds.length;
            for (int face = 0; face < Math.min(with.faceCount, base.faceCount); face++) {
                if ((with.faceColors[face] & 0xFFFF) != (base.faceColors[face] & 0xFFFF)) {
                    colorChanged++;
                }
                int wt = with.faceTextures == null || face >= with.faceTextures.length ? -1 : with.faceTextures[face];
                int bt = base.faceTextures == null || face >= base.faceTextures.length ? -1 : base.faceTextures[face];
                if (wt != bt) {
                    textureChanged++;
                }
            }
            String colorVerdict = pairs == 0 ? "none" : (colorChanged > 0 ? "OK" : "NO-OP");
            String texVerdict = texPairs == 0 ? "none" : (textureChanged > 0 ? "OK" : "NO-OP");
            System.out.println("item " + itemId + " '" + item.name + "' pairs=" + pairs
                    + " colorFacesChanged=" + colorChanged + " [" + colorVerdict + "]"
                    + " texPairs=" + texPairs + " texFacesChanged=" + textureChanged + " [" + texVerdict + "]");
            if (pairs > 0 && colorChanged == 0) noopColor++;
            if (texPairs > 0 && textureChanged == 0) noopTexture++;
        }
        System.out.println("summary: colour no-ops=" + noopColor + " texture no-ops=" + noopTexture);
    }

    private RecolorReachProbe() {
    }
}
