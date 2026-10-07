package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.render.SoftwareModelRenderer;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ItemColorProbeTest {
    @Test
    void inspectFirstColored634Items() throws Exception {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        for (int id : CacheManager.getArchiveIds(19)) {
            for (int file : CacheManager.getFileIds(19, id)) {
                ItemDefinitions item = ItemDefinitions.getItemDefinitions((id << 8) | file);
                if (item.originalModelColors == null || item.originalModelColors.length == 0) {
                    continue;
                }
                if (item.id != 20135) {
                    continue;
                }
                RenderModel model = ModelDecoderAdapter.loadItemModel(item);
                if (model == null) {
                    continue;
                }
                int firstFrom = item.originalModelColors[0] & 0xffff;
                int firstTo = item.modifiedModelColors[0] & 0xffff;
                int matchingFaces = 0;
                int modifiedFaces = 0;
                for (int face = 0; face < model.faceColors.length; face++) {
                    int color = model.faceColors[face] & 0xffff;
                    if (color == firstFrom) matchingFaces++;
                    if (color == firstTo) modifiedFaces++;
                }
                BufferedImage rendered = SoftwareModelRenderer.renderInventorySprite(item, 36, 32);
                Set<Integer> colors = new HashSet<>();
                for (int y = 0; y < rendered.getHeight(); y++) {
                    for (int x = 0; x < rendered.getWidth(); x++) {
                        int pixel = rendered.getRGB(x, y);
                        if ((pixel >>> 24) != 0) colors.add(pixel & 0xFFFFFF);
                    }
                }
                List<Integer> sampleColors = new ArrayList<>(colors);
                System.err.println("ITEM " + item.id + " model=" + item.modelId
                        + " first=" + firstFrom + "->" + firstTo
                        + " matches=" + matchingFaces + " modified=" + modifiedFaces
                        + " renderedColors=" + colors.size() + " sample="
                        + sampleColors.subList(0, Math.min(12, sampleColors.size())));
                return;
            }
        }
    }
}
