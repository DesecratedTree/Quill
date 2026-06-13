package com.desecratedtree.quill.render;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.texture.TextureUvMapper;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import static com.desecratedtree.quill.render.ModelViewerPanel.rotateX;
import static com.desecratedtree.quill.render.ModelViewerPanel.rotateY;

public final class SoftwareModelRenderer {

    private static final int ITEM_OUTLINE_COLOR = 0xFF000002;

    private static final int ITEM_SHADOW_COLOR = 3153952;

    private static final int INVENTORY_SLOT_WIDTH = 36;

    private static final int INVENTORY_SLOT_HEIGHT = 32;

    private static final int INVENTORY_CENTER_X = 16;

    private static final int INVENTORY_CENTER_Y = 16;

    private static final double INVENTORY_FOCAL_LENGTH = 512.0;

    private static final int INTERNAL_RENDER_SCALE = 3;

    private static final double NEAR_PLANE = 50.0;

    private static final double LIGHT_X;

    private static final double LIGHT_Y;

    private static final double LIGHT_Z;
    static {
        double len = Math.sqrt(0.45 * 0.45 + 0.7 * 0.7 + 0.55 * 0.55);
        LIGHT_X = -0.45 / len;
        LIGHT_Y = 0.7 / len;
        LIGHT_Z = -0.55 / len;
    }

    private SoftwareModelRenderer() {
    }

    public static BufferedImage renderInventorySprite(ItemDefinitions item, int width, int height) {
        return renderInventorySprite(item, width, height, 0, 0, 0, 0, 0);
    }

    public static BufferedImage renderInventorySprite(
            ItemDefinitions item,
            int width,
            int height,
            int previewDeltaX,
            int previewDeltaY,
            int previewDeltaZ,
            int previewOffsetX,
            int previewOffsetY
    ) {
        if (item == null) {
            return createPlaceholder(width, height, "No item model");
        }
        RenderModel model = ModelDecoderAdapter.loadItemModel(item);
        if (model == null) {
            return createPlaceholder(width, height, "No item model");
        }
        int rawWidth = Math.max(1, width);
        int rawHeight = Math.max(1, height);
        double inventoryScaleX = rawWidth / (double) INVENTORY_SLOT_WIDTH;
        double inventoryScaleY = rawHeight / (double) INVENTORY_SLOT_HEIGHT;
        double centerX = INVENTORY_CENTER_X * inventoryScaleX;
        double centerY = INVENTORY_CENTER_Y * inventoryScaleY;
        double focalLength = INVENTORY_FOCAL_LENGTH * ((inventoryScaleX + inventoryScaleY) * 0.5);
        double[] scaledX = new double[model.vertexCount];
        double[] scaledY = new double[model.vertexCount];
        double[] scaledZ = new double[model.vertexCount];
        int scaleX = item.unknownInt7 <= 0 ? 128 : item.unknownInt7;
        int scaleY = item.unknownInt8 <= 0 ? 128 : item.unknownInt8;
        int scaleZ = item.unknownInt9 <= 0 ? 128 : item.unknownInt9;
        double minScaledY = 0.0;
        double maxScaledY = 0.0;
        for (int i = 0; i < model.vertexCount; i++) {
            scaledX[i] = model.verticesX[i] * scaleX / 128.0;
            scaledY[i] = model.verticesY[i] * scaleY / 128.0;
            scaledZ[i] = model.verticesZ[i] * scaleZ / 128.0;
            if (i == 0 || scaledY[i] < minScaledY) {
                minScaledY = scaledY[i];
            }
            if (i == 0 || scaledY[i] > maxScaledY) {
                maxScaledY = scaledY[i];
            }
        }
        int effectiveX = sanitizeClientAngle(item.modelRotation1 + previewDeltaX);
        int effectiveY = sanitizeClientAngle(item.modelRotation2 + previewDeltaY);
        int effectiveZ = sanitizeClientAngle(item.unknownInt5 + previewDeltaZ);
        double zoomUnits = item.modelZoom * 4.0;
        double translateX = (item.modelOffset1 + previewOffsetX) * 4.0;
        double offsetYTerm = (item.modelOffset2 + previewOffsetY) * 4.0;
        double pitchRadians = clientAngleToRadians(effectiveX);
        double translateY = Math.sin(pitchRadians) * zoomUnits - (minScaledY / 2.0) + offsetYTerm;
        double translateZ = Math.cos(pitchRadians) * zoomUnits + offsetYTerm;
        ClientTransform transform = new ClientTransform();
        transform.rotateZ14((-effectiveZ << 3) & 0x3FFF);
        transform.rotateY14((effectiveY << 3) & 0x3FFF);
        transform.translate(translateX, translateY, translateZ);
        transform.rotateX14((effectiveX << 3) & 0x3FFF);
        return renderFaces(
                model,
                scaledX,
                scaledY,
                scaledZ,
                width,
                height,
                rawWidth,
                rawHeight,
                centerX,
                centerY,
                focalLength,
                transform::apply
        );
    }

    public static BufferedImage renderModelPreview(
            RenderModel model,
            int width,
            int height,
            double pitchDegrees,
            double yawDegrees,
            double zoom
    ) {
        if (model == null) {
            return createPlaceholder(width, height, "No model");
        }
        int rawWidth = Math.max(1, width * INTERNAL_RENDER_SCALE);
        int rawHeight = Math.max(1, height * INTERNAL_RENDER_SCALE);
        double[] centeredX = new double[model.vertexCount];
        double[] centeredY = new double[model.vertexCount];
        double[] centeredZ = new double[model.vertexCount];
        double centerModelX = (model.minX + model.maxX) * 0.5;
        double centerModelY = (model.minY + model.maxY) * 0.5;
        double centerModelZ = (model.minZ + model.maxZ) * 0.5;
        double maxDimension = Math.max(1.0, Math.max(model.getWidth(), Math.max(model.getHeight(), model.getDepth())));
        double fitScale = Math.min(rawWidth, rawHeight) * 0.7 / maxDimension;
        double distance = Math.min(rawWidth, rawHeight) * (1.8 / Math.max(0.2, zoom));
        double centerX = rawWidth * 0.5;
        double centerY = rawHeight * 0.55;
        double focalLength = Math.min(rawWidth, rawHeight) * 1.1;
        double pitchRadians = Math.toRadians(pitchDegrees);
        double yawRadians = Math.toRadians(yawDegrees);
        for (int i = 0; i < model.vertexCount; i++) {
            centeredX[i] = (model.verticesX[i] - centerModelX) * fitScale;
            centeredY[i] = (model.verticesY[i] - centerModelY) * fitScale;
            centeredZ[i] = (model.verticesZ[i] - centerModelZ) * fitScale;
        }
        return renderFaces(
                model,
                centeredX,
                centeredY,
                centeredZ,
                width,
                height,
                rawWidth,
                rawHeight,
                centerX,
                centerY,
                focalLength,
                point -> {
                    double[] rotated = rotateY(point[0], point[1], point[2], yawRadians);
                    rotated = rotateX(rotated[0], rotated[1], rotated[2], pitchRadians);
                    rotated[2] += distance;
                    return rotated;
                }
        );
    }

    private static BufferedImage renderFaces(
            RenderModel model,
            double[] baseX,
            double[] baseY,
            double[] baseZ,
            int width,
            int height,
            int rawWidth,
            int rawHeight,
            double centerX,
            double centerY,
            double focalLength,
            VertexTransformer transformer
    ) {
        double[] viewX = new double[model.vertexCount];
        double[] viewY = new double[model.vertexCount];
        double[] viewZ = new double[model.vertexCount];
        int[] screenX = new int[model.vertexCount];
        int[] screenY = new int[model.vertexCount];
        for (int i = 0; i < model.vertexCount; i++) {
            double[] transformed = transformer.apply(new double[]{baseX[i], baseY[i], baseZ[i]});
            viewX[i] = transformed[0];
            viewY[i] = transformed[1];
            viewZ[i] = transformed[2];
            double depthValue = transformed[2];
            if (Math.abs(depthValue) < NEAR_PLANE) {
                depthValue = depthValue < 0.0 ? -NEAR_PLANE : NEAR_PLANE;
            }
            screenX[i] = (int) Math.round(centerX + (transformed[0] * focalLength) / depthValue);
            screenY[i] = (int) Math.round(centerY + (transformed[1] * focalLength) / depthValue);
        }
        BufferedImage raw = new BufferedImage(rawWidth, rawHeight, BufferedImage.TYPE_INT_ARGB);
        double[] depthBuffer = new double[rawWidth * rawHeight];
        Arrays.fill(depthBuffer, Double.POSITIVE_INFINITY);
        List<PendingFace> translucentFaces = new ArrayList<>();
        for (int face = 0; face < model.faceCount; face++) {
            if (isNonSolidFace(model, face)) {
                continue;
            }
            int a = model.faceA[face];
            int b = model.faceB[face];
            int c = model.faceC[face];
            if (viewZ[a] <= NEAR_PLANE || viewZ[b] <= NEAR_PLANE || viewZ[c] <= NEAR_PLANE) {
                continue;
            }
            double cross =
                    (screenX[b] - screenX[a]) * (double) (screenY[c] - screenY[a])
                            - (screenY[b] - screenY[a]) * (double) (screenX[c] - screenX[a]);
            if (cross >= 0.0) {
                continue;
            }
            double nx = (viewY[b] - viewY[a]) * (viewZ[c] - viewZ[a]) - (viewZ[b] - viewZ[a]) * (viewY[c] - viewY[a]);
            double ny = (viewZ[b] - viewZ[a]) * (viewX[c] - viewX[a]) - (viewX[b] - viewX[a]) * (viewZ[c] - viewZ[a]);
            double nz = (viewX[b] - viewX[a]) * (viewY[c] - viewY[a]) - (viewY[b] - viewY[a]) * (viewX[c] - viewX[a]);
            double normalLength = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (normalLength == 0.0) {
                continue;
            }
            nx /= normalLength;
            ny /= normalLength;
            nz /= normalLength;
            double brightness = clamp(0.55 + nx * LIGHT_X + ny * LIGHT_Y + nz * LIGHT_Z, 0.28, 1.0);
            int alpha = faceAlpha(model.faceAlphas, face);
            TexturedFill texturedFill = textureFill(model, face, brightness, alpha);
            int color = texturedFill == null ? shadedFaceColor(model.faceColors, alpha, face, brightness) : 0;
            if (alpha == 0) {
                continue;
            }
            if (alpha < 255) {
                translucentFaces.add(new PendingFace(
                        screenX[a],
                        screenY[a],
                        viewZ[a],
                        screenX[b],
                        screenY[b],
                        viewZ[b],
                        screenX[c],
                        screenY[c],
                        viewZ[c],
                        color,
                        texturedFill
                ));
                continue;
            }
            rasterizeFace(raw, depthBuffer, screenX[a], screenY[a], viewZ[a], screenX[b], screenY[b], viewZ[b],
                    screenX[c], screenY[c], viewZ[c], color, texturedFill, true);
        }
        translucentFaces.sort(Comparator.comparingDouble(PendingFace::averageDepth).reversed());
        for (PendingFace face : translucentFaces) {
            rasterizeFace(raw, depthBuffer, face.x1, face.y1, face.z1, face.x2, face.y2, face.z2, face.x3, face.y3,
                    face.z3, face.argb, face.textureFill, false);
        }
        BufferedImage postProcessed = applyInventorySpritePostProcess(raw);
        if (rawWidth == width && rawHeight == height) {
            return postProcessed;
        }
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D downsample = scaled.createGraphics();
        downsample.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        downsample.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        downsample.drawImage(postProcessed, 0, 0, width, height, null);
        downsample.dispose();
        return scaled;
    }

    private static BufferedImage applyInventorySpritePostProcess(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        pixels = applySpriteOutline(pixels, width, height, ITEM_OUTLINE_COLOR);
        applySpriteShadow(pixels, width, height, ITEM_SHADOW_COLOR);
        for (int i = 0; i < pixels.length; i++) {
            if ((pixels[i] & 0x00FFFFFF) != 0) {
                pixels[i] |= 0xFF000000;
            } else {
                pixels[i] = 0;
            }
        }
        BufferedImage postProcessed = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        postProcessed.setRGB(0, 0, width, height, pixels, 0, width);
        return postProcessed;
    }

    private static int[] applySpriteOutline(int[] pixels, int width, int height, int outlineColor) {
        int[] outlined = new int[pixels.length];
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = pixels[index];
                if (value == 0) {
                    if ((x > 0 && pixels[index - 1] != 0)
                            || (y > 0 && pixels[index - width] != 0)
                            || (x < width - 1 && pixels[index + 1] != 0)
                            || (y < height - 1 && pixels[index + width] != 0)) {
                        value = outlineColor;
                    }
                }
                outlined[index++] = value;
            }
        }
        return outlined;
    }

    private static void applySpriteShadow(int[] pixels, int width, int height, int shadowColor) {
        for (int y = height - 1; y > 0; y--) {
            int rowOffset = y * width;
            for (int x = width - 1; x > 0; x--) {
                int index = rowOffset + x;
                if (pixels[index] == 0 && pixels[index - 1 - width] != 0) {
                    pixels[index] = shadowColor;
                }
            }
        }
    }

    private static BufferedImage createPlaceholder(int width, int height, String text) {
        BufferedImage image = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(28, 30, 34));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(new Color(60, 65, 73));
        graphics.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
        graphics.setColor(new Color(220, 220, 220));
        graphics.setFont(graphics.getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics metrics = graphics.getFontMetrics();
        int x = Math.max(6, (image.getWidth() - metrics.stringWidth(text)) / 2);
        int y = Math.max(metrics.getAscent() + 6, image.getHeight() / 2);
        graphics.drawString(text, x, y);
        graphics.dispose();
        return image;
    }

    private static void rasterizeTriangle(
            BufferedImage image,
            double[] depthBuffer,
            int x1,
            int y1,
            double z1,
            int x2,
            int y2,
            double z2,
            int x3,
            int y3,
            double z3,
            int argb
    ) {
        rasterizeTriangle(image, depthBuffer, x1, y1, z1, x2, y2, z2, x3, y3, z3, argb, true);
    }

    private static void rasterizeFace(
            BufferedImage image,
            double[] depthBuffer,
            int x1,
            int y1,
            double z1,
            int x2,
            int y2,
            double z2,
            int x3,
            int y3,
            double z3,
            int argb,
            TexturedFill textureFill,
            boolean writeDepth
    ) {
        if (textureFill == null) {
            rasterizeTriangle(image, depthBuffer, x1, y1, z1, x2, y2, z2, x3, y3, z3, argb, writeDepth);
            return;
        }
        rasterizeTexturedTriangle(image, depthBuffer, x1, y1, z1, x2, y2, z2, x3, y3, z3, textureFill, writeDepth);
    }

    private static void rasterizeTriangle(
            BufferedImage image,
            double[] depthBuffer,
            int x1,
            int y1,
            double z1,
            int x2,
            int y2,
            double z2,
            int x3,
            int y3,
            double z3,
            int argb,
            boolean writeDepth
    ) {
        int minX = Math.max(0, Math.min(x1, Math.min(x2, x3)));
        int maxX = Math.min(image.getWidth() - 1, Math.max(x1, Math.max(x2, x3)));
        int minY = Math.max(0, Math.min(y1, Math.min(y2, y3)));
        int maxY = Math.min(image.getHeight() - 1, Math.max(y1, Math.max(y2, y3)));
        if (minX > maxX || minY > maxY) {
            return;
        }
        double area = edge(x1, y1, x2, y2, x3, y3);
        if (area == 0.0) {
            return;
        }
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double sampleX = x + 0.5;
                double sampleY = y + 0.5;
                double w1 = edge(x2, y2, x3, y3, sampleX, sampleY) / area;
                double w2 = edge(x3, y3, x1, y1, sampleX, sampleY) / area;
                double w3 = edge(x1, y1, x2, y2, sampleX, sampleY) / area;
                if (w1 < 0.0 || w2 < 0.0 || w3 < 0.0) {
                    continue;
                }
                double depth = z1 * w1 + z2 * w2 + z3 * w3;
                int index = y * image.getWidth() + x;
                if (depth >= depthBuffer[index]) {
                    continue;
                }
                if (writeDepth) {
                    depthBuffer[index] = depth;
                    image.setRGB(x, y, argb);
                } else {
                    image.setRGB(x, y, blend(argb, image.getRGB(x, y)));
                }
            }
        }
    }

    private static void rasterizeTexturedTriangle(
            BufferedImage image,
            double[] depthBuffer,
            int x1,
            int y1,
            double z1,
            int x2,
            int y2,
            double z2,
            int x3,
            int y3,
            double z3,
            TexturedFill fill,
            boolean writeDepth
    ) {
        int minX = Math.max(0, Math.min(x1, Math.min(x2, x3)));
        int maxX = Math.min(image.getWidth() - 1, Math.max(x1, Math.max(x2, x3)));
        int minY = Math.max(0, Math.min(y1, Math.min(y2, y3)));
        int maxY = Math.min(image.getHeight() - 1, Math.max(y1, Math.max(y2, y3)));
        if (minX > maxX || minY > maxY) {
            return;
        }
        double area = edge(x1, y1, x2, y2, x3, y3);
        if (area == 0.0) {
            return;
        }
        double invZ1 = 1.0 / z1;
        double invZ2 = 1.0 / z2;
        double invZ3 = 1.0 / z3;
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double sampleX = x + 0.5;
                double sampleY = y + 0.5;
                double w1 = edge(x2, y2, x3, y3, sampleX, sampleY) / area;
                double w2 = edge(x3, y3, x1, y1, sampleX, sampleY) / area;
                double w3 = edge(x1, y1, x2, y2, sampleX, sampleY) / area;
                if (w1 < 0.0 || w2 < 0.0 || w3 < 0.0) {
                    continue;
                }
                double invDepth = invZ1 * w1 + invZ2 * w2 + invZ3 * w3;
                if (invDepth <= 0.0) {
                    continue;
                }
                double depth = 1.0 / invDepth;
                int index = y * image.getWidth() + x;
                if (depth >= depthBuffer[index]) {
                    continue;
                }
                int argb = sampleTexture(fill, w1, w2, w3, invZ1, invZ2, invZ3, invDepth);
                if (((argb >>> 24) & 0xFF) == 0) {
                    continue;
                }
                if (writeDepth) {
                    depthBuffer[index] = depth;
                    image.setRGB(x, y, argb);
                } else {
                    image.setRGB(x, y, blend(argb, image.getRGB(x, y)));
                }
            }
        }
    }

    private static double edge(double x1, double y1, double x2, double y2, double px, double py) {
        return (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
    }

    private static int shadedFaceColor(short[] faceColors, int alpha, int index, double brightness) {
        int packed = faceColors != null && index < faceColors.length ? faceColors[index] & 0xFFFF : 0;
        int rgb = CacheColor.toRgb(packed);
        int baseRed = (rgb >> 16) & 0xFF;
        int baseGreen = (rgb >> 8) & 0xFF;
        int baseBlue = rgb & 0xFF;
        int red = applyBrightness(baseRed, brightness);
        int green = applyBrightness(baseGreen, brightness);
        int blue = applyBrightness(baseBlue, brightness);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int faceAlpha(int[] faceAlphas, int index) {
        if (faceAlphas != null && index < faceAlphas.length) {
            return 255 - (faceAlphas[index] & 0xFF);
        }
        return 255;
    }

    private static TexturedFill textureFill(RenderModel model, int face, double brightness, int alpha) {
        if (model.faceTextures == null || face >= model.faceTextures.length) {
            return null;
        }
        int textureId = model.faceTextures[face];
        if (textureId < 0) {
            return null;
        }
        BufferedImage texture = TextureLoader.previewTexture(textureId);
        if (texture == null) {
            return null;
        }
        if (!TextureLoader.hasOverrideTexture(textureId) && isFlatTexture(texture)) {
            return null;
        }
        TextureUvMapper.UvTriangle uv = TextureUvMapper.map(model, face);
        return new TexturedFill(
                texture,
                brightness,
                alpha,
                uv.u1,
                uv.v1,
                uv.u2,
                uv.v2,
                uv.u3,
                uv.v3,
                TextureLoader.scrollU(textureId),
                TextureLoader.scrollV(textureId)
        );
    }

    private static boolean isFlatTexture(BufferedImage image) {
        int stepX = Math.max(1, image.getWidth() / 8);
        int stepY = Math.max(1, image.getHeight() / 8);
        int samples = 0;
        int min = 255;
        int max = 0;
        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x = 0; x < image.getWidth(); x += stepX) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                int brightness = (red + green + blue) / 3;
                min = Math.min(min, brightness);
                max = Math.max(max, brightness);
                samples++;
            }
        }
        return samples > 0 && max - min <= 8;
    }

    private static int sampleTexture(
            TexturedFill fill,
            double w1,
            double w2,
            double w3,
            double invZ1,
            double invZ2,
            double invZ3,
            double invDepth
    ) {
        double u = (fill.u1 * w1 * invZ1 + fill.u2 * w2 * invZ2 + fill.u3 * w3 * invZ3) / invDepth + fill.scrollU;
        double v = (fill.v1 * w1 * invZ1 + fill.v2 * w2 * invZ2 + fill.v3 * w3 * invZ3) / invDepth + fill.scrollV;
        u = u - Math.floor(u);
        v = v - Math.floor(v);
        int textureX = Math.min(fill.image.getWidth() - 1, Math.max(0, (int) Math.floor(u * fill.image.getWidth())));
        int textureY = Math.min(fill.image.getHeight() - 1, Math.max(0, (int) Math.floor(v * fill.image.getHeight())));
        int texel = fill.image.getRGB(textureX, textureY);
        int alpha = ((texel >>> 24) & 0xFF) * fill.alpha / 255;
        int red = applyBrightness((texel >> 16) & 0xFF, fill.brightness);
        int green = applyBrightness((texel >> 8) & 0xFF, fill.brightness);
        int blue = applyBrightness(texel & 0xFF, fill.brightness);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static boolean isNonSolidFace(RenderModel model, int face) {
        return model.faceRenderTypes != null
                && face < model.faceRenderTypes.length
                && model.faceRenderTypes[face] >= 2;
    }

    private static int applyBrightness(int channel, double brightness) {
        double ambient = 24.0;
        return (int) clamp(ambient + channel * brightness, 0.0, 255.0);
    }

    private static int blend(int source, int destination) {
        int sourceAlpha = (source >>> 24) & 0xFF;
        if (sourceAlpha <= 0) {
            return destination;
        }
        if (sourceAlpha >= 255) {
            return source;
        }
        int destinationAlpha = (destination >>> 24) & 0xFF;
        int outAlpha = sourceAlpha + destinationAlpha * (255 - sourceAlpha) / 255;
        if (outAlpha == 0) {
            return 0;
        }
        int sourceRed = (source >> 16) & 0xFF;
        int sourceGreen = (source >> 8) & 0xFF;
        int sourceBlue = source & 0xFF;
        int destinationRed = (destination >> 16) & 0xFF;
        int destinationGreen = (destination >> 8) & 0xFF;
        int destinationBlue = destination & 0xFF;
        int red = (sourceRed * sourceAlpha + destinationRed * destinationAlpha * (255 - sourceAlpha) / 255) / outAlpha;
        int green = (sourceGreen * sourceAlpha + destinationGreen * destinationAlpha * (255 - sourceAlpha) / 255) / outAlpha;
        int blue = (sourceBlue * sourceAlpha + destinationBlue * destinationAlpha * (255 - sourceAlpha) / 255) / outAlpha;
        return (outAlpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int sanitizeClientAngle(int value) {
        return value & 2047;
    }

    private static double clientAngleToRadians(int value) {
        return (value & 2047) * (Math.PI * 2.0 / 2048.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @FunctionalInterface

    private interface VertexTransformer {
        double[] apply(double[] point);
    }

    private static final class ClientTransform {

        private double aFloat5672 = 1.0;

        private double aFloat5673 = 0.0;

        private double aFloat5669 = 0.0;

        private double aFloat5655 = 0.0;

        private double aFloat5678 = 1.0;

        private double aFloat5666 = 0.0;

        private double aFloat5662 = 0.0;

        private double aFloat5680 = 0.0;

        private double aFloat5664 = 1.0;

        private double aFloat5686 = 0.0;

        private double aFloat5685 = 0.0;

        private double aFloat5681 = 0.0;

        private void translate(double x, double y, double z) {
            aFloat5686 += x;
            aFloat5685 += y;
            aFloat5681 += z;
        }

        private void rotateX14(int angle14) {
            double radians = clientAngle14ToRadians(angle14);
            double sin = Math.sin(radians);
            double cos = Math.cos(radians);
            double oldAFloat5655 = aFloat5655;
            double oldAFloat5678 = aFloat5678;
            double oldAFloat5666 = aFloat5666;
            double oldAFloat5685 = aFloat5685;
            aFloat5655 = oldAFloat5655 * cos - aFloat5662 * sin;
            aFloat5662 = oldAFloat5655 * sin + aFloat5662 * cos;
            aFloat5678 = oldAFloat5678 * cos - aFloat5680 * sin;
            aFloat5666 = oldAFloat5666 * cos - aFloat5664 * sin;
            aFloat5680 = aFloat5680 * cos + oldAFloat5678 * sin;
            aFloat5685 = oldAFloat5685 * cos - aFloat5681 * sin;
            aFloat5664 = aFloat5664 * cos + oldAFloat5666 * sin;
            aFloat5681 = oldAFloat5685 * sin + aFloat5681 * cos;
        }

        private void rotateY14(int angle14) {
            double radians = clientAngle14ToRadians(angle14);
            double sin = Math.sin(radians);
            double cos = Math.cos(radians);
            double oldAFloat5672 = aFloat5672;
            double oldAFloat5673 = aFloat5673;
            double oldAFloat5669 = aFloat5669;
            double oldAFloat5686 = aFloat5686;
            aFloat5672 = oldAFloat5672 * cos + sin * aFloat5662;
            aFloat5673 = cos * oldAFloat5673 + sin * aFloat5680;
            aFloat5662 = aFloat5662 * cos - sin * oldAFloat5672;
            aFloat5669 = cos * oldAFloat5669 + sin * aFloat5664;
            aFloat5680 = cos * aFloat5680 - sin * oldAFloat5673;
            aFloat5664 = cos * aFloat5664 - sin * oldAFloat5669;
            aFloat5686 = oldAFloat5686 * cos + sin * aFloat5681;
            aFloat5681 = cos * aFloat5681 - sin * oldAFloat5686;
        }

        private void rotateZ14(int angle14) {
            double radians = clientAngle14ToRadians(angle14);
            double sin = Math.sin(radians);
            double cos = Math.cos(radians);
            double oldAFloat5672 = aFloat5672;
            double oldAFloat5673 = aFloat5673;
            double oldAFloat5669 = aFloat5669;
            double oldAFloat5686 = aFloat5686;
            aFloat5672 = -(sin * aFloat5655) + cos * oldAFloat5672;
            aFloat5673 = cos * oldAFloat5673 - aFloat5678 * sin;
            aFloat5655 = cos * aFloat5655 + oldAFloat5672 * sin;
            aFloat5669 = cos * oldAFloat5669 - aFloat5666 * sin;
            aFloat5678 = cos * aFloat5678 + sin * oldAFloat5673;
            aFloat5666 = sin * oldAFloat5669 + cos * aFloat5666;
            aFloat5686 = -(sin * aFloat5685) + cos * oldAFloat5686;
            aFloat5685 = cos * aFloat5685 + sin * oldAFloat5686;
        }

        private double[] apply(double[] point) {
            double x = point[0];
            double y = point[1];
            double z = point[2];
            return new double[]{
                    z * aFloat5669 + (aFloat5673 * y + x * aFloat5672) + aFloat5686,
                    x * aFloat5655 + y * aFloat5678 + z * aFloat5666 + aFloat5685,
                    aFloat5681 + (aFloat5680 * y + x * aFloat5662 + z * aFloat5664)
            };
        }
    }

    private static double clientAngle14ToRadians(int value) {
        return (value & 0x3FFF) * (Math.PI * 2.0 / 16384.0);
    }

    private static final class PendingFace {

        private final int x1;

        private final int y1;

        private final double z1;

        private final int x2;

        private final int y2;

        private final double z2;

        private final int x3;

        private final int y3;

        private final double z3;

        private final int argb;

        private final TexturedFill textureFill;

        private PendingFace(
                int x1,
                int y1,
                double z1,
                int x2,
                int y2,
                double z2,
                int x3,
                int y3,
                double z3,
                int argb,
                TexturedFill textureFill
        ) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;
            this.argb = argb;
            this.textureFill = textureFill;
        }

        private double averageDepth() {
            return (z1 + z2 + z3) / 3.0;
        }
    }

    private static final class TexturedFill {

        private final BufferedImage image;

        private final double brightness;

        private final int alpha;

        private final float u1;

        private final float v1;

        private final float u2;

        private final float v2;

        private final float u3;

        private final float v3;

        private final double scrollU;

        private final double scrollV;

        private TexturedFill(
                BufferedImage image,
                double brightness,
                int alpha,
                float u1,
                float v1,
                float u2,
                float v2,
                float u3,
                float v3,
                double scrollU,
                double scrollV
        ) {
            this.image = image;
            this.brightness = brightness;
            this.alpha = alpha;
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
            this.u3 = u3;
            this.v3 = v3;
            this.scrollU = scrollU;
            this.scrollV = scrollV;
        }
    }
}
