package com.desecratedtree.quill.tools;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.texture.MaterialDefinition;
import com.desecratedtree.quill.texture.MaterialLoader;
import com.desecratedtree.quill.texture.TextureLoader;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.TreeSet;

public final class Texture638Probe {
    public static void main(String[] args) {
        CacheManager.init("data/cache");
        Set<Integer> referencing = new TreeSet<>();
        for (int id : CacheManager.getArchiveIds(19)) {
            for (int file : CacheManager.getFileIds(19, id)) {
                ItemDefinitions item = ItemDefinitions.getItemDefinitions((id << 8) | file);
                if (item == null || item.name == null || "null".equals(item.name)) continue;
                int[] targets = item.modifiedTextureIds;
                if (targets == null) continue;
                for (int t : targets) {
                    if (t == 638) {
                        referencing.add((id << 8) | file);
                        int pairIndex = -1;
                        for (int k = 0; k < targets.length; k++) if (targets[k] == 638) { pairIndex = k; break; }
                        System.out.println("REF item=" + ((id << 8) | file) + " name=" + item.name
                                + " pairIndex=" + pairIndex);
                    }
                }
            }
        }
        System.out.println("total referencing items = " + referencing.size());
        System.out.println("index9 record 638 = " + (CacheManager.getTextureData(638) == null ? "MISSING" : "present"));
        MaterialDefinition material = MaterialLoader.get(638);
        System.out.println("material 638 = " + (material == null ? "null" : "present"));
        BufferedImage preview = TextureLoader.previewTexture(638);
        System.out.println("preview 638 = " + (preview == null ? "null" : preview.getWidth() + "x" + preview.getHeight()
                + " firstPixel=" + Integer.toHexString(preview.getRGB(0, 0))));
        // Render the first referencing item and check pixel variance
        for (int itemId : referencing) {
            ItemDefinitions item = ItemDefinitions.getItemDefinitions(itemId);
            RenderModel model = ModelDecoderAdapter.loadItemModel(item);
            if (model == null) {
                System.out.println("item " + itemId + ": no model " + item.modelId);
                continue;
            }
            int facesWith638 = 0;
            for (int face = 0; face < model.faceCount; face++) {
                int tex = model.faceTextures == null || face >= model.faceTextures.length ? -1 : model.faceTextures[face];
                if (tex == 638) facesWith638++;
            }
            System.out.println("item " + itemId + " '" + item.name + "' faces=" + model.faceCount
                    + " facesWith638=" + facesWith638);
            BufferedImage rendered = com.desecratedtree.quill.render.SoftwareModelRenderer.renderInventorySprite(item, 36, 32);
            Set<Integer> colors = new TreeSet<>();
            for (int y = 0; y < rendered.getHeight(); y++) {
                for (int x = 0; x < rendered.getWidth(); x++) {
                    int pixel = rendered.getRGB(x, y);
                    if ((pixel >>> 24) != 0) colors.add(pixel & 0xFFFFFF);
                }
            }
            System.out.println("  rendered distinct colors = " + colors.size() + " sample=" +
                    new java.util.ArrayList<>(colors).subList(0, Math.min(6, colors.size())));
        }
    }

    private Texture638Probe() {
    }
}
