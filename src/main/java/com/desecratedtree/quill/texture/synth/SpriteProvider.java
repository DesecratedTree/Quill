package com.desecratedtree.quill.texture.synth;

/**
 * Source of decoded sprite frames for texture synthesis, keyed by the sprite
 * file id reported by {@link TextureLayer#spriteFileId()}. Port of the client's
 * {@code Class348.aClass45_4286} sprite index, resolved lazily.
 */
public interface SpriteProvider {

    /**
     * Returns the decoded frame for {@code fileId}, or {@code null} when the
     * sprite is unavailable. Frames are cached by the provider.
     */
    SpriteRaster sprite(int fileId);
}