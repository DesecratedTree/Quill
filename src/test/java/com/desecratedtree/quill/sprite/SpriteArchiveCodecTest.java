package com.desecratedtree.quill.sprite;

import com.desecratedtree.quill.render.IndexedSprite;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpriteArchiveCodecTest {

    @Test
    void imagePixelsAndAlphaSurviveRoundTrip() {
        BufferedImage source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, 0xFFFF0000);
        source.setRGB(1, 0, 0x8000FF00);
        source.setRGB(2, 0, 0x00000000);
        source.setRGB(0, 1, 0xFF0000FF);
        source.setRGB(1, 1, 0xFFFFFFFF);
        source.setRGB(2, 1, 0x40010203);

        SpriteArchive archive = new SpriteArchive();
        archive.sprites.add(SpriteArchiveCodec.fromBufferedImage(source));
        byte[] encoded = SpriteArchiveCodec.encode(archive);
        SpriteArchive decoded = SpriteArchiveCodec.decode(encoded);
        IndexedSprite sprite = decoded.sprites.get(0);
        BufferedImage result = sprite.toBufferedImage();

        assertEquals(3, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(source.getRGB(0, 0), result.getRGB(0, 0));
        assertEquals(source.getRGB(1, 0), result.getRGB(1, 0));
        assertEquals(source.getRGB(2, 0), result.getRGB(2, 0));
        assertEquals(source.getRGB(0, 1), result.getRGB(0, 1));
        assertEquals(source.getRGB(1, 1), result.getRGB(1, 1));
        assertEquals(source.getRGB(2, 1), result.getRGB(2, 1));
        assertNotNull(sprite.alpha);
    }
}
