package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.animation.AnimationBase;
import com.desecratedtree.quill.animation.AnimationFrame;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.defs.NpcDefinitions;
import com.desecratedtree.quill.defs.RenderAnimationDefinitions;
import com.desecratedtree.quill.defs.SequenceDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;

public final class DecodedCache {

    private DecodedCache() {
    }

    public static void clear() {
        ItemDefinitions.clearItemsDefinitions();
        NpcDefinitions.clearDefinitions();
        SequenceDefinitions.clearCache();
        RenderAnimationDefinitions.clearCache();
        AnimationFrame.clearCache();
        AnimationBase.clearCache();
        ModelDecoderAdapter.clearCache();
    }
}
