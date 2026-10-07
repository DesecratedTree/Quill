package com.desecratedtree.quill.cache;

import com.desecratedtree.quill.render.CacheColor;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.texture.MaterialDefinition;
import com.desecratedtree.quill.texture.MaterialLoader;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.texture.synth.TextureProgram;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialFaceProbeTest {
    @Test
    void inspectReportedMaterialFaces() {
        Path cache = Paths.get("data", "cache");
        Assumptions.assumeTrue(Files.isDirectory(cache));
        CacheManager.init(cache.toString());
        inspect(2833, 91);
        inspect(30054, 117);
    }

    private static void inspect(int modelId, int face) {
        RenderModel model = ModelDecoderAdapter.loadModel(modelId);
        assertNotNull(model, "missing model " + modelId);
        assertTrue(face < model.faceCount, "missing face " + face + " in model " + modelId);
        int packed = model.faceColors[face] & 0xFFFF;
        int texture = model.faceTextures == null ? -1 : model.faceTextures[face];
        MaterialDefinition material = texture < 0 ? null : MaterialLoader.get(texture);
        byte[] record = texture < 0 ? null : CacheManager.getTextureData(texture);
        if (record != null) {
            TextureProgram program = new TextureProgram(record);
            System.err.println("RAW " + texture + "=" + hex(record));
            System.err.println("PROGRAM " + texture + " sources=" + java.util.Arrays.toString(program.spriteFileIds())
                    + " auxiliary=" + java.util.Arrays.toString(program.auxiliaryFileIds())
                    + " main=" + program.mainColor().getClass().getSimpleName()
                    + " layers=" + layerSummary(program));
            for (int auxiliary : program.auxiliaryFileIds()) {
                byte[] auxiliaryRecord = CacheManager.getTextureData(auxiliary);
                if (auxiliaryRecord != null) {
                    TextureProgram auxiliaryProgram = new TextureProgram(auxiliaryRecord);
                    System.err.println("  AUX MATERIAL " + auxiliary + "=" + materialValues(MaterialLoader.get(auxiliary)));
                    System.err.println("  AUX RAW " + auxiliary + "=" + hex(auxiliaryRecord)
                            + " main=" + auxiliaryProgram.mainColor().getClass().getSimpleName()
                            + " sources=" + java.util.Arrays.toString(auxiliaryProgram.spriteFileIds())
                            + " auxiliary=" + java.util.Arrays.toString(auxiliaryProgram.auxiliaryFileIds())
                            + " layers=" + layerSummary(auxiliaryProgram));
                    for (int nested : auxiliaryProgram.auxiliaryFileIds()) {
                        byte[] nestedRecord = CacheManager.getTextureData(nested);
                        if (nestedRecord != null) {
                            TextureProgram nestedProgram = new TextureProgram(nestedRecord);
                            System.err.println("    NESTED " + nested + "=" + hex(nestedRecord)
                                    + " main=" + nestedProgram.mainColor().getClass().getSimpleName()
                                    + " aux=" + java.util.Arrays.toString(nestedProgram.auxiliaryFileIds())
                                    + " layers=" + layerSummary(nestedProgram));
                        }
                    }
                }
            }
            java.awt.image.BufferedImage rendered = TextureLoader.previewTexture(texture);
            int chroma = 0;
            java.util.Set<Integer> renderedColors = new java.util.HashSet<>();
            for (int y = 0; y < rendered.getHeight(); y++) {
                for (int x = 0; x < rendered.getWidth(); x++) {
                    int rgb = rendered.getRGB(x, y) & 0xFFFFFF;
                    renderedColors.add(rgb);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    if (Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue)) > 8) chroma++;
                }
            }
            System.err.println("PROGRAM COLORS " + renderedColors.size() + " chromatic=" + chroma + " sample=" + new java.util.ArrayList<>(renderedColors).subList(0, Math.min(8, renderedColors.size())));
            if (texture == 249 || texture == 327) assertTrue(chroma > 0, "material " + texture + " is grayscale");
        }
        System.err.println("MODEL " + modelId + " FACE " + face
                + " packed=" + packed + " rgb=" + String.format("%06X", CacheColor.toRgb(packed))
                + " texture=" + texture
                + " renderType=" + (model.faceRenderTypes == null ? -1 : model.faceRenderTypes[face])
                + " texCoord=" + (model.textureCoordinates == null ? -1 : model.textureCoordinates[face])
                + " material=" + materialValues(material)
                + " preview=" + (texture < 0 ? "null" : TextureLoader.previewTexture(texture).getRGB(0, 0)));
    }

    private static String layerSummary(TextureProgram program) {
        StringBuilder result = new StringBuilder();
        for (com.desecratedtree.quill.texture.synth.TextureLayer layer : program.layers) {
            if (result.length() > 0) result.append(',');
            result.append(layer.getClass().getSimpleName()).append('/').append(layer.children.length);
            for (com.desecratedtree.quill.texture.synth.TextureLayer child : layer.children) {
                result.append('>').append(child.getClass().getSimpleName());
            }
        }
        return result.toString();
    }

    private static String hex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte value : data) result.append(String.format("%02X", value & 0xFF));
        return result.toString();
    }

    private static String materialValues(MaterialDefinition material) {
        if (material == null) return "null";
        return "field201=" + materialValue(material, "field201")
                + " field216=" + materialValue(material, "field216")
                + " field213=" + materialValue(material, "field213")
                + " field202=" + materialValue(material, "field202")
                + " field203=" + materialValue(material, "field203")
                + " field205=" + materialValue(material, "field205")
                + " field206=" + materialValue(material, "field206")
                + " field200=" + materialValue(material, "field200")
                + " average=" + materialValue(material, "averageColor");
    }

    private static Object materialValue(MaterialDefinition material, String fieldName) {
        try {
            java.lang.reflect.Field field = MaterialDefinition.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(material);
        } catch (ReflectiveOperationException ex) {
            return "?";
        }
    }
}
