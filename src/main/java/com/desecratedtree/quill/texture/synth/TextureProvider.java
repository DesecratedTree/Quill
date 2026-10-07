package com.desecratedtree.quill.texture.synth;

/** Resolves sprite files and auxiliary texture programs during synthesis. */
public interface TextureProvider extends SpriteProvider {
    SpriteRaster texture(int textureId);
}
