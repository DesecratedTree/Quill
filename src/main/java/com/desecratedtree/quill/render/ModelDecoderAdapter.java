package com.desecratedtree.quill.render;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.animation.AnimationBase;
import com.desecratedtree.quill.animation.AnimationFrame;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.defs.NpcDefinitions;
import com.desecratedtree.quill.defs.ObjectDefinitions;
import com.desecratedtree.quill.defs.RenderAnimationDefinitions;
import com.desecratedtree.quill.defs.SequenceDefinitions;
import com.desecratedtree.quill.texture.MaterialLoader;
import com.desecratedtree.quill.texture.TextureLoader;
import io.blurite.cache.model.MeshDecodingOption;
import io.blurite.cache.model.Model;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelDecoderAdapter {

    private static final Map<Integer, RenderModel> CACHE = new ConcurrentHashMap<>();

    private static final MeshDecodingOption[] PREVIEW_DECODING_OPTIONS = {
            MeshDecodingOption.PreserveOriginalData
    };

    private ModelDecoderAdapter() {
    }

    public static RenderModel loadModel(int modelId) {
        if (modelId < 0) {
            return null;
        }
        return CACHE.computeIfAbsent(modelId, ModelDecoderAdapter::decodeModel);
    }

    public static void invalidateModel(int modelId) {
        CACHE.remove(modelId);
    }

    public static void clearCache() {
        CACHE.clear();
        TextureLoader.clearCache();
        MaterialLoader.clearCache();
    }

    public static Model loadDecodedModel(int modelId) {
        byte[] data = CacheManager.getModelData(modelId);
        if (data == null || data.length == 0) {
            return null;
        }
        return Model.Companion.decode(
                modelId,
                Unpooled.wrappedBuffer(data),
                PREVIEW_DECODING_OPTIONS
        );
    }

    public static RenderModel loadItemModel(ItemDefinitions item) {
        if (item == null) {
            return null;
        }
        RenderModel base = loadModel(item.modelId);
        if (base == null) {
            return null;
        }
        return applyItemColors(base, item);
    }

    public static RenderModel loadCompositeModel(int id, int[] modelIds, int[] originalColors, int[] modifiedColors,
                                                 short[] originalTextures, int[] modifiedTextures) {
        return loadCompositeModel(id, modelIds, originalColors, modifiedColors, originalTextures, modifiedTextures,
                null, null, null, 128, 128, 128, 0, 0, 0, false);
    }

    public static RenderModel loadObjectModel(ObjectDefinitions object, int[] modelIds, int sequenceFrameIndex) {
        if (object == null) {
            return null;
        }
        int animationId = primaryObjectAnimation(object);
        AnimationFrame animationFrame = null;
        if (animationId >= 0) {
            SequenceDefinitions sequence = SequenceDefinitions.get(animationId);
            if (sequence != null) {
                animationFrame = AnimationFrame.get(sequence.primaryFrameId(sequenceFrameIndex));
            }
        }
        return loadCompositeModel(
                object.id,
                modelIds,
                toIntArray(object.originalColours),
                toIntArray(object.modifiedColours),
                object.originalTextureColours,
                toIntArray(object.modifiedTextureColours),
                null,
                null,
                animationFrame,
                object.modelSizeX,
                object.modelSizeY,
                object.modelSizeZ,
                object.offsetX,
                object.offsetY,
                object.offsetZ,
                false
        );
    }

    public static RenderModel loadNpcModel(NpcDefinitions npc, int[] modelIds, int[] originalColors, int[] modifiedColors) {
        return loadNpcModel(npc, npc == null ? -1 : npc.renderAnimId, modelIds, originalColors, modifiedColors, 0);
    }

    public static RenderModel loadNpcModel(NpcDefinitions npc, int renderAnimId, int[] modelIds, int[] originalColors, int[] modifiedColors,
                                           int sequenceFrameIndex) {
        if (npc == null) {
            return null;
        }
        RenderAnimationDefinitions renderAnimation = RenderAnimationDefinitions.get(renderAnimId);
        SequenceDefinitions idleSequence = renderAnimation == null ? null : SequenceDefinitions.get(renderAnimation.idleSequenceId);
        return loadCompositeModel(
                npc.id,
                modelIds,
                originalColors,
                modifiedColors,
                npc.originalTextureIds,
                npc.modifiedTextureIds,
                npc.modelTranslations,
                renderAnimation == null ? null : renderAnimation.modelTransforms,
                idleSequence == null ? null : AnimationFrame.get(idleSequence.primaryFrameId(sequenceFrameIndex)),
                npc.resizeX,
                npc.resizeY,
                npc.resizeX,
                0,
                0,
                0,
                true
        );
    }

    private static RenderModel loadCompositeModel(int id, int[] modelIds, int[] originalColors, int[] modifiedColors,
                                                  short[] originalTextures, int[] modifiedTextures,
                                                  int[][] modelTranslations, int[][] renderAnimationTransforms,
                                                  AnimationFrame animationFrame,
                                                  int resizeX, int resizeY, int resizeZ,
                                                  int offsetX, int offsetY, int offsetZ,
                                                  boolean upscaleLegacyModels) {
        if (modelIds == null || modelIds.length == 0) {
            return null;
        }
        List<Model> models = new ArrayList<>();
        for (int i = 0; i < modelIds.length; i++) {
            int modelId = modelIds[i];
            byte[] data = CacheManager.getModelData(modelId);
            if (data == null || data.length == 0) {
                continue;
            }
            Model model = Model.Companion.decode(modelId, Unpooled.wrappedBuffer(data), PREVIEW_DECODING_OPTIONS);
            if (upscaleLegacyModels && model.version() <= 12) {
                model.resize(512, 512, 512);
            }
            applyNpcModelTranslation(model, modelTranslations, i);
            applyRenderAnimationTransform(model, renderAnimationTransforms, i);
            models.add(model);
        }
        if (models.isEmpty()) {
            return null;
        }
        Model merged = models.size() == 1 ? models.get(0) : Model.Companion.merge(id, models);
        ModelBounds stableBounds = boundsAfterTransform(merged, resizeX, resizeY, resizeZ, offsetX, offsetY, offsetZ);
        applyAnimationFrame(merged, animationFrame);
        if (resizeX != 128 || resizeY != 128 || resizeZ != 128) {
            merged.resize(resizeX, resizeY, resizeZ);
        }
        if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
            merged.translate(offsetX, offsetY, offsetZ);
        }
        RenderModel renderModel = fromDecodedModel(id, merged, stableBounds);
        return applyColorsAndTextures(renderModel, originalColors, modifiedColors, originalTextures, modifiedTextures);
    }

    private static void applyAnimationFrame(Model model, AnimationFrame frame) {
        if (frame == null || model.getVertexSkins() == null) {
            return;
        }
        int[] xs = model.getVertexPositionsX();
        int[] ys = model.getVertexPositionsY();
        int[] zs = model.getVertexPositionsZ();
        for (int i = 0; i < model.getVertexCount(); i++) {
            xs[i] <<= 4;
            ys[i] <<= 4;
            zs[i] <<= 4;
        }
        int centerX = 0;
        int centerY = 0;
        int centerZ = 0;
        AnimationBase base = frame.base;
        for (int i = 0; i < frame.transformIndices.length; i++) {
            int transformIndex = frame.transformIndices[i];
            if (transformIndex < 0 || transformIndex >= base.transformTypes.length) {
                continue;
            }
            int type = base.transformTypes[transformIndex];
            int[] groups = base.transformGroups[transformIndex];
            int mask = base.transformMasks[transformIndex];
            int x = frame.transformX[i];
            int y = frame.transformY[i];
            int z = frame.transformZ[i];
            if (type == 0) {
                int[] center = computeAnimationCenter(model, groups, mask, x, y, z);
                centerX = center[0];
                centerY = center[1];
                centerZ = center[2];
            } else if (type == 1) {
                translateAnimationGroups(model, groups, mask, x, y, z);
            } else if (type == 2) {
                rotateAnimationGroups(model, groups, mask, centerX, centerY, centerZ, x, y, z);
            } else if (type == 3) {
                scaleAnimationGroups(model, groups, mask, centerX, centerY, centerZ, x, y, z);
            }
        }
        for (int i = 0; i < model.getVertexCount(); i++) {
            xs[i] = xs[i] + 7 >> 4;
            ys[i] = ys[i] + 7 >> 4;
            zs[i] = zs[i] + 7 >> 4;
        }
    }

    private static int[] computeAnimationCenter(Model model, int[] groups, int mask, int offsetX, int offsetY, int offsetZ) {
        int[] vertices = animationVertices(model, groups, mask);
        int[] xs = model.getVertexPositionsX();
        int[] ys = model.getVertexPositionsY();
        int[] zs = model.getVertexPositionsZ();
        if (vertices.length == 0) {
            return new int[]{offsetX << 4, offsetY << 4, offsetZ << 4};
        }
        long x = 0;
        long y = 0;
        long z = 0;
        for (int vertex : vertices) {
            x += xs[vertex];
            y += ys[vertex];
            z += zs[vertex];
        }
        return new int[]{
                (int) (x / vertices.length) + (offsetX << 4),
                (int) (y / vertices.length) + (offsetY << 4),
                (int) (z / vertices.length) + (offsetZ << 4)
        };
    }

    private static void translateAnimationGroups(Model model, int[] groups, int mask, int offsetX, int offsetY, int offsetZ) {
        int[] xs = model.getVertexPositionsX();
        int[] ys = model.getVertexPositionsY();
        int[] zs = model.getVertexPositionsZ();
        offsetX <<= 4;
        offsetY <<= 4;
        offsetZ <<= 4;
        for (int vertex : animationVertices(model, groups, mask)) {
            xs[vertex] += offsetX;
            ys[vertex] += offsetY;
            zs[vertex] += offsetZ;
        }
    }

    private static void rotateAnimationGroups(Model model, int[] groups, int mask, int centerX, int centerY, int centerZ,
                                              int angleX, int angleY, int angleZ) {
        int[] xs = model.getVertexPositionsX();
        int[] ys = model.getVertexPositionsY();
        int[] zs = model.getVertexPositionsZ();
        for (int vertex : animationVertices(model, groups, mask)) {
            int x = xs[vertex] - centerX;
            int y = ys[vertex] - centerY;
            int z = zs[vertex] - centerZ;
            if (angleZ != 0) {
                int sin = sin14(angleZ);
                int cos = cos14(angleZ);
                int nextX = (x * cos + y * sin + 8192) >> 14;
                y = (y * cos - x * sin + 8192) >> 14;
                x = nextX;
            }
            if (angleX != 0) {
                int sin = sin14(angleX);
                int cos = cos14(angleX);
                int nextY = (y * cos - z * sin + 8192) >> 14;
                z = (y * sin + z * cos + 8192) >> 14;
                y = nextY;
            }
            if (angleY != 0) {
                int sin = sin14(angleY);
                int cos = cos14(angleY);
                int nextX = (z * sin + x * cos + 8192) >> 14;
                z = (z * cos - x * sin + 8192) >> 14;
                x = nextX;
            }
            xs[vertex] = x + centerX;
            ys[vertex] = y + centerY;
            zs[vertex] = z + centerZ;
        }
    }

    private static void scaleAnimationGroups(Model model, int[] groups, int mask, int centerX, int centerY, int centerZ,
                                             int scaleX, int scaleY, int scaleZ) {
        int[] xs = model.getVertexPositionsX();
        int[] ys = model.getVertexPositionsY();
        int[] zs = model.getVertexPositionsZ();
        for (int vertex : animationVertices(model, groups, mask)) {
            xs[vertex] -= centerX;
            ys[vertex] -= centerY;
            zs[vertex] -= centerZ;
            xs[vertex] = xs[vertex] * scaleX / 128;
            ys[vertex] = ys[vertex] * scaleY / 128;
            zs[vertex] = zs[vertex] * scaleZ / 128;
            xs[vertex] += centerX;
            ys[vertex] += centerY;
            zs[vertex] += centerZ;
        }
    }

    private static int[] animationVertices(Model model, int[] groups, int mask) {
        int[] skins = model.getVertexSkins();
        if (skins == null || groups == null || groups.length == 0) {
            return new int[0];
        }
        short[] partMasks = model.getVertexPartMasks();
        int count = 0;
        for (int i = 0; i < skins.length; i++) {
            if (contains(groups, skins[i]) && includesPartMask(partMasks, i, mask)) {
                count++;
            }
        }
        int[] vertices = new int[count];
        int index = 0;
        for (int i = 0; i < skins.length; i++) {
            if (contains(groups, skins[i]) && includesPartMask(partMasks, i, mask)) {
                vertices[index++] = i;
            }
        }
        return vertices;
    }

    private static boolean includesPartMask(short[] partMasks, int vertex, int mask) {
        return partMasks == null || mask == 65535 || (mask & (partMasks[vertex] & 0xFFFF)) != 0;
    }

    private static boolean contains(int[] values, int value) {
        for (int current : values) {
            if (current == value) {
                return true;
            }
        }
        return false;
    }

    private static int sin14(int angle) {
        return (int) Math.round(Math.sin((angle & 0x3FFF) * Math.PI / 8192.0) * 16384.0);
    }

    private static int cos14(int angle) {
        return (int) Math.round(Math.cos((angle & 0x3FFF) * Math.PI / 8192.0) * 16384.0);
    }

    private static void applyNpcModelTranslation(Model model, int[][] modelTranslations, int modelIndex) {
        if (modelTranslations == null || modelIndex < 0 || modelIndex >= modelTranslations.length) {
            return;
        }
        int[] translation = modelTranslations[modelIndex];
        if (translation == null || translation.length < 3) {
            return;
        }
        model.translate(translation[0], translation[1], translation[2]);
    }

    private static void applyRenderAnimationTransform(Model model, int[][] transforms, int modelIndex) {
        if (transforms == null || modelIndex < 0 || modelIndex >= transforms.length) {
            return;
        }
        int[] transform = transforms[modelIndex];
        if (transform == null || transform.length < 6) {
            return;
        }
        int yaw = transform[4];
        int pitch = transform[3];
        int roll = transform[5];
        if (yaw != 0 || pitch != 0 || roll != 0) {
            model.rotate(yaw, pitch, roll);
        }
        if (transform[0] != 0 || transform[1] != 0 || transform[2] != 0) {
            model.translate(transform[0], transform[1], transform[2]);
        }
    }

    private static RenderModel decodeModel(int modelId) {
        byte[] data = CacheManager.getModelData(modelId);
        if (data == null || data.length == 0) {
            return null;
        }
        Model decoded = Model.Companion.decode(
                modelId,
                Unpooled.wrappedBuffer(data),
                PREVIEW_DECODING_OPTIONS
        );
        if (decoded.version() <= 12) {
            decoded.resize(512, 512, 512);
        }
        return fromDecodedModel(modelId, decoded);
    }

    public static RenderModel fromDecodedModel(int modelId, Model decoded) {
        return fromDecodedModel(modelId, decoded, null);
    }

    private static RenderModel fromDecodedModel(int modelId, Model decoded, ModelBounds boundsOverride) {
        int[] x = cloneOrEmpty(decoded.getVertexPositionsX(), decoded.getVertexCount());
        int[] y = cloneOrEmpty(decoded.getVertexPositionsY(), decoded.getVertexCount());
        int[] z = cloneOrEmpty(decoded.getVertexPositionsZ(), decoded.getVertexCount());
        int[] a = cloneOrEmpty(decoded.getTriangleVertex1(), decoded.getTriangleCount());
        int[] b = cloneOrEmpty(decoded.getTriangleVertex2(), decoded.getTriangleCount());
        int[] c = cloneOrEmpty(decoded.getTriangleVertex3(), decoded.getTriangleCount());
        short[] colors = cloneOrEmpty(decoded.getTriangleColors(), decoded.getTriangleCount());
        int[] alphas = cloneOrNull(decoded.getTriangleAlphas());
        int[] renderTypes = cloneOrNull(decoded.getTriangleRenderTypes());
        int[] textures = cloneOrNull(decoded.getTriangleTextures());
        int[] textureCoordinates = cloneOrNull(decoded.getTextureCoordinates());
        int[] textureRenderTypes = cloneOrNull(decoded.getTextureRenderTypes());
        int[] textureVertexA = cloneOrNull(decoded.getTextureTriangleVertex1());
        int[] textureVertexB = cloneOrNull(decoded.getTextureTriangleVertex2());
        int[] textureVertexC = cloneOrNull(decoded.getTextureTriangleVertex3());
        int[] textureScaleX = cloneOrNull(decoded.getTextureScaleX());
        int[] textureScaleY = cloneOrNull(decoded.getTextureScaleY());
        int[] textureScaleZ = cloneOrNull(decoded.getTextureScaleZ());
        int[] textureRotation = cloneOrNull(decoded.getTextureRotation());
        int[] textureDirection = cloneOrNull(decoded.getTextureDirection());
        int[] textureSpeed = cloneOrNull(decoded.getTextureSpeed());
        int[] textureTransU = cloneOrNull(decoded.getTextureTransU());
        int[] textureTransV = cloneOrNull(decoded.getTextureTransV());
        ModelBounds bounds = boundsOverride == null ? boundsForVertices(x, y, z) : boundsOverride;
        return new RenderModel(
                modelId,
                decoded.getVertexCount(),
                decoded.getTriangleCount(),
                x,
                y,
                z,
                a,
                b,
                c,
                colors,
                alphas,
                renderTypes,
                textures,
                textureCoordinates,
                textureRenderTypes,
                textureVertexA,
                textureVertexB,
                textureVertexC,
                textureScaleX,
                textureScaleY,
                textureScaleZ,
                textureRotation,
                textureDirection,
                textureSpeed,
                textureTransU,
                textureTransV,
                bounds.minX,
                bounds.maxX,
                bounds.minY,
                bounds.maxY,
                bounds.minZ,
                bounds.maxZ
        );
    }

    private static ModelBounds boundsForVertices(int[] x, int[] y, int[] z) {
        if (x.length > 0) {
            int minX = x[0];
            int maxX = x[0];
            int minY = y[0];
            int maxY = y[0];
            int minZ = z[0];
            int maxZ = z[0];
            for (int i = 1; i < x.length; i++) {
                minX = Math.min(minX, x[i]);
                maxX = Math.max(maxX, x[i]);
                minY = Math.min(minY, y[i]);
                maxY = Math.max(maxY, y[i]);
                minZ = Math.min(minZ, z[i]);
                maxZ = Math.max(maxZ, z[i]);
            }
            return new ModelBounds(minX, maxX, minY, maxY, minZ, maxZ);
        }
        return new ModelBounds(0, 0, 0, 0, 0, 0);
    }

    private static ModelBounds boundsAfterTransform(Model model, int resizeX, int resizeY, int resizeZ,
                                                    int offsetX, int offsetY, int offsetZ) {
        int[] x = model.getVertexPositionsX();
        int[] y = model.getVertexPositionsY();
        int[] z = model.getVertexPositionsZ();
        if (resizeX == 128 && resizeY == 128 && resizeZ == 128 && offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            return boundsForVertices(x, y, z);
        }
        int[] resizedX = new int[model.getVertexCount()];
        int[] resizedY = new int[model.getVertexCount()];
        int[] resizedZ = new int[model.getVertexCount()];
        for (int i = 0; i < model.getVertexCount(); i++) {
            resizedX[i] = x[i] * resizeX / 128 + offsetX;
            resizedY[i] = y[i] * resizeY / 128 + offsetY;
            resizedZ[i] = z[i] * resizeZ / 128 + offsetZ;
        }
        return boundsForVertices(resizedX, resizedY, resizedZ);
    }

    private static int primaryObjectAnimation(ObjectDefinitions object) {
        if (object.animations == null || object.animations.length == 0) {
            return -1;
        }
        for (int animationId : object.animations) {
            if (animationId >= 0) {
                return animationId;
            }
        }
        return -1;
    }

    private static int[] toIntArray(short[] values) {
        if (values == null) {
            return new int[0];
        }
        int[] converted = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            converted[i] = values[i] & 0xFFFF;
        }
        return converted;
    }

    private static RenderModel applyItemColors(RenderModel base, ItemDefinitions item) {
        return applyColorsAndTextures(base, item.originalModelColors, item.modifiedModelColors,
                item.originalTextureIds, item.modifiedTextureIds);
    }

    private static RenderModel applyColorsAndTextures(RenderModel base, int[] originalColors, int[] modifiedColors,
                                                      short[] originalTextures, int[] modifiedTextures) {
        boolean hasColors = originalColors != null && modifiedColors != null
                && originalColors.length > 0 && modifiedColors.length > 0;
        boolean hasTextures = originalTextures != null && modifiedTextures != null
                && originalTextures.length > 0 && modifiedTextures.length > 0
                && base.faceTextures != null;
        if (!hasColors && !hasTextures) {
            return base;
        }
        short[] recolored = base.faceColors.clone();
        int[] retextured = base.faceTextures == null ? null : base.faceTextures.clone();
        if (hasColors) {
            int count = Math.min(originalColors.length, modifiedColors.length);
            for (int i = 0; i < count; i++) {
                int from = originalColors[i] & 0xFFFF;
                int to = modifiedColors[i] & 0xFFFF;
                for (int face = 0; face < recolored.length; face++) {
                    if ((recolored[face] & 0xFFFF) == from) {
                        recolored[face] = (short) to;
                        if (retextured != null && face < retextured.length) {
                            retextured[face] = -1;
                        }
                    }
                }
            }
        }
        if (hasTextures) {
            int count = Math.min(originalTextures.length, modifiedTextures.length);
            for (int i = 0; i < count; i++) {
                int from = originalTextures[i] & 0xFFFF;
                int to = modifiedTextures[i] & 0xFFFF;
                for (int face = 0; face < retextured.length; face++) {
                    if ((retextured[face] & 0xFFFF) == from) {
                        retextured[face] = to;
                    }
                }
            }
        }
        return new RenderModel(
                base.id,
                base.vertexCount,
                base.faceCount,
                base.verticesX,
                base.verticesY,
                base.verticesZ,
                base.faceA,
                base.faceB,
                base.faceC,
                recolored,
                base.faceAlphas,
                base.faceRenderTypes,
                retextured,
                base.textureCoordinates,
                base.textureRenderTypes,
                base.textureVertexA,
                base.textureVertexB,
                base.textureVertexC,
                base.textureScaleX,
                base.textureScaleY,
                base.textureScaleZ,
                base.textureRotation,
                base.textureDirection,
                base.textureSpeed,
                base.textureTransU,
                base.textureTransV,
                base.minX,
                base.maxX,
                base.minY,
                base.maxY,
                base.minZ,
                base.maxZ
        );
    }

    private static int[] cloneOrEmpty(int[] source, int length) {
        if (source == null) {
            return new int[length];
        }
        return source.clone();
    }

    private static short[] cloneOrEmpty(short[] source, int length) {
        if (source == null) {
            return new short[length];
        }
        return source.clone();
    }

    private static int[] cloneOrNull(int[] source) {
        return source == null ? null : source.clone();
    }

    private static final class ModelBounds {

        private final int minX;

        private final int maxX;

        private final int minY;

        private final int maxY;

        private final int minZ;

        private final int maxZ;

        private ModelBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }
}
