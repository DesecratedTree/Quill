package com.desecratedtree.quill.render;

import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.texture.TextureUvMapper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

final class LwjglModelViewerCanvas extends AWTGLCanvas {

    private static final double NEAR_PLANE = 50.0;

    private static final double INTERNAL_RENDER_SCALE = 3.0;

    private static final double LIGHT_X;

    private static final double LIGHT_Y;

    private static final double LIGHT_Z;
    static {
        double len = Math.sqrt(0.45 * 0.45 + 0.7 * 0.7 + 0.55 * 0.55);
        LIGHT_X = -0.45 / len;
        LIGHT_Y = 0.7 / len;
        LIGHT_Z = -0.55 / len;
    }

    private final Supplier<RenderModel> modelSupplier;

    private final DoubleSupplier pitchSupplier;

    private final DoubleSupplier yawSupplier;

    private final DoubleSupplier zoomSupplier;

    private final IntSupplier hoveredFaceSupplier;

    private final IntSupplier selectedFaceSupplier;

    private final Map<Integer, Integer> textureHandles = new HashMap<>();

    private RenderModel cachedModel;

    private MeshCache cachedMesh;

    private String failureMessage;
    LwjglModelViewerCanvas(
            Supplier<RenderModel> modelSupplier,
            DoubleSupplier pitchSupplier,
            DoubleSupplier yawSupplier,
            DoubleSupplier zoomSupplier,
            IntSupplier hoveredFaceSupplier,
            IntSupplier selectedFaceSupplier
    ) throws AWTException {
        super(glData());
        this.modelSupplier = modelSupplier;
        this.pitchSupplier = pitchSupplier;
        this.yawSupplier = yawSupplier;
        this.zoomSupplier = zoomSupplier;
        this.hoveredFaceSupplier = hoveredFaceSupplier;
        this.selectedFaceSupplier = selectedFaceSupplier;
        setBackground(new Color(20, 23, 27));
    }

    private static GLData glData() {
        GLData data = new GLData();
        data.swapInterval = 1;
        data.samples = 4;
        data.majorVersion = 2;
        data.minorVersion = 1;
        return data;
    }

    @Override

    public void initGL() {
        try {
            GL.createCapabilities();
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glDisable(GL_CULL_FACE);
            glShadeModel(GL_FLAT);
            glEnable(GL_MULTISAMPLE);
            glClearColor(20f / 255f, 23f / 255f, 27f / 255f, 1f);
        } catch (RuntimeException ex) {
            failureMessage = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        }
    }

    @Override

    public void paintGL() {
        if (failureMessage != null) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            swapBuffers();
            return;
        }
        RenderModel model = modelSupplier.get();
        int width = Math.max(1, getFramebufferWidth() > 0 ? getFramebufferWidth() : getWidth());
        int height = Math.max(1, getFramebufferHeight() > 0 ? getFramebufferHeight() : getHeight());
        int rawWidth = Math.max(1, (int) Math.round(width * INTERNAL_RENDER_SCALE));
        int rawHeight = Math.max(1, (int) Math.round(height * INTERNAL_RENDER_SCALE));
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (model != null) {
            renderModel(model, rawWidth, rawHeight);
        }
        swapBuffers();
    }

    @Override

    public void paint(Graphics graphics) {
        render();
    }

    @Override

    public void update(Graphics graphics) {
        paint(graphics);
    }
    int pickFace(Point point) {
        RenderModel model = modelSupplier.get();
        if (model == null) {
            return -1;
        }
        int width = Math.max(1, getFramebufferWidth() > 0 ? getFramebufferWidth() : getWidth());
        int height = Math.max(1, getFramebufferHeight() > 0 ? getFramebufferHeight() : getHeight());
        int rawWidth = Math.max(1, (int) Math.round(width * INTERNAL_RENDER_SCALE));
        int rawHeight = Math.max(1, (int) Math.round(height * INTERNAL_RENDER_SCALE));
        MeshCache mesh = meshCache(model);
        Projection projection = projectModel(mesh, rawWidth, rawHeight, pitchSupplier.getAsDouble(),
                yawSupplier.getAsDouble(), zoomSupplier.getAsDouble());
        double scaleX = rawWidth / (double) Math.max(1, getWidth());
        double scaleY = rawHeight / (double) Math.max(1, getHeight());
        double sampleX = point.x * scaleX;
        double sampleY = point.y * scaleY;
        int pickedFace = -1;
        double pickedDepth = Double.POSITIVE_INFINITY;
        for (FaceInfo face : mesh.faces) {
            int a = face.a;
            int b = face.b;
            int c = face.c;
            if (projection.viewZ[a] <= NEAR_PLANE || projection.viewZ[b] <= NEAR_PLANE || projection.viewZ[c] <= NEAR_PLANE) {
                continue;
            }
            double cross =
                    (projection.screenX[b] - projection.screenX[a]) * (double) (projection.screenY[c] - projection.screenY[a])
                            - (projection.screenY[b] - projection.screenY[a]) * (double) (projection.screenX[c] - projection.screenX[a]);
            if (cross >= 0.0) {
                continue;
            }
            double area = edge(projection.screenX[a], projection.screenY[a], projection.screenX[b], projection.screenY[b],
                    projection.screenX[c], projection.screenY[c]);
            if (area == 0.0) {
                continue;
            }
            double w1 = edge(projection.screenX[b], projection.screenY[b], projection.screenX[c], projection.screenY[c], sampleX, sampleY) / area;
            double w2 = edge(projection.screenX[c], projection.screenY[c], projection.screenX[a], projection.screenY[a], sampleX, sampleY) / area;
            double w3 = edge(projection.screenX[a], projection.screenY[a], projection.screenX[b], projection.screenY[b], sampleX, sampleY) / area;
            if (w1 < 0.0 || w2 < 0.0 || w3 < 0.0) {
                continue;
            }
            double depth = projection.viewZ[a] * w1 + projection.viewZ[b] * w2 + projection.viewZ[c] * w3;
            if (depth < pickedDepth) {
                pickedDepth = depth;
                pickedFace = face.index;
            }
        }
        return pickedFace;
    }

    @Override

    public void removeNotify() {
        try {
            super.removeNotify();
        } catch (NullPointerException ignored) {
        } finally {
            cachedModel = null;
            cachedMesh = null;
            textureHandles.clear();
        }
    }

    private void renderModel(RenderModel model, int rawWidth, int rawHeight) {
        MeshCache mesh = meshCache(model);
        Projection projection = projectModel(mesh, rawWidth, rawHeight, pitchSupplier.getAsDouble(),
                yawSupplier.getAsDouble(), zoomSupplier.getAsDouble());
        double farPlane = Math.max(NEAR_PLANE + 1.0, projection.maxViewZ + 512.0);
        configurePerspective(rawWidth, rawHeight, projection.focalLength, farPlane);
        List<PendingFace> opaqueFaces = new ArrayList<>(mesh.faces.length);
        List<PendingFace> translucentFaces = new ArrayList<>();
        glDepthMask(true);
        for (FaceInfo face : mesh.faces) {
            PendingFace pending = buildFace(face, projection, farPlane, mesh);
            if (pending == null) {
                continue;
            }
            if (pending.alpha >= 250) {
                opaqueFaces.add(pending);
            } else {
                translucentFaces.add(pending);
            }
        }
        opaqueFaces.sort(Comparator.comparingDouble(PendingFace::averageDepth).reversed());
        for (PendingFace face : opaqueFaces) {
            drawFace(face, true);
        }
        translucentFaces.sort(Comparator.comparingDouble(PendingFace::averageDepth).reversed());
        glDepthMask(false);
        for (PendingFace face : translucentFaces) {
            drawFace(face, false);
        }
        glDepthMask(true);
        drawHighlight(model, projection, farPlane, hoveredFaceSupplier.getAsInt(),
                new Color(92, 214, 120, 86), new Color(104, 232, 136, 210), 1.8f);
        drawHighlight(model, projection, farPlane, selectedFaceSupplier.getAsInt(),
                new Color(128, 200, 255, hoveredFaceSupplier.getAsInt() == selectedFaceSupplier.getAsInt() ? 52 : 30),
                new Color(145, 210, 255, 255), 2.8f);
    }

    private PendingFace buildFace(FaceInfo face, Projection projection, double farPlane, MeshCache mesh) {
        int a = face.a;
        int b = face.b;
        int c = face.c;
        if (projection.viewZ[a] <= NEAR_PLANE || projection.viewZ[b] <= NEAR_PLANE || projection.viewZ[c] <= NEAR_PLANE) {
            return null;
        }
        double cross =
                (projection.screenX[b] - projection.screenX[a]) * (double) (projection.screenY[c] - projection.screenY[a])
                        - (projection.screenY[b] - projection.screenY[a]) * (double) (projection.screenX[c] - projection.screenX[a]);
        if (cross >= 0.0) {
            return null;
        }
        double rotatedNormalX = face.normalX * projection.yawCos + face.normalZ * projection.yawSin;
        double rotatedNormalZ = -face.normalX * projection.yawSin + face.normalZ * projection.yawCos;
        double rotatedNormalY = face.normalY * projection.pitchCos - rotatedNormalZ * projection.pitchSin;
        double rotatedNormalDepth = face.normalY * projection.pitchSin + rotatedNormalZ * projection.pitchCos;
        double brightness = clamp(0.55 + rotatedNormalX * LIGHT_X + rotatedNormalY * LIGHT_Y + rotatedNormalDepth * LIGHT_Z, 0.28, 1.0);
        TexturedFace texturedFace = texturedFace(face.textureInfo, brightness, face.alpha);
        int argb = texturedFace == null ? shadedFaceColor(mesh.model.faceColors, face.alpha, face.index, brightness) : 0;
        return new PendingFace(
                projection.viewX[a], projection.viewY[a], -projection.viewZ[a],
                projection.viewX[b], projection.viewY[b], -projection.viewZ[b],
                projection.viewX[c], projection.viewY[c], -projection.viewZ[c],
                face.alpha, argb, texturedFace,
                (projection.viewZ[a] + projection.viewZ[b] + projection.viewZ[c]) / 3.0
        );
    }

    private void drawFace(PendingFace face, boolean opaquePass) {
        if (face.texturedFace != null) {
            drawTexturedFace(face, opaquePass);
            return;
        }
        glDisable(GL_TEXTURE_2D);
        if (opaquePass) {
            glDisable(GL_BLEND);
        } else {
            glEnable(GL_BLEND);
        }
        float red = ((face.argb >> 16) & 0xFF) / 255f;
        float green = ((face.argb >> 8) & 0xFF) / 255f;
        float blue = (face.argb & 0xFF) / 255f;
        float alpha = ((face.argb >>> 24) & 0xFF) / 255f;
        glColor4f(red, green, blue, alpha);
        glBegin(GL_TRIANGLES);
        glVertex3d(face.x1, -face.y1, face.z1);
        glVertex3d(face.x2, -face.y2, face.z2);
        glVertex3d(face.x3, -face.y3, face.z3);
        glEnd();
    }

    private void drawTexturedFace(PendingFace face, boolean opaquePass) {
        int textureHandle = textureHandle(face.texturedFace.textureId, face.texturedFace.image);
        if (textureHandle == 0) {
            drawFace(face.withoutTexture(), opaquePass);
            return;
        }
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureHandle);
        if (opaquePass) {
            glDisable(GL_BLEND);
        } else {
            glEnable(GL_BLEND);
        }
        float brightness = (float) face.texturedFace.brightness;
        glColor4f(brightness, brightness, brightness, face.texturedFace.alpha / 255f);
        glBegin(GL_TRIANGLES);
        glTexCoord2d(face.texturedFace.u1 + face.texturedFace.scrollU, face.texturedFace.v1 + face.texturedFace.scrollV);
        glVertex3d(face.x1, -face.y1, face.z1);
        glTexCoord2d(face.texturedFace.u2 + face.texturedFace.scrollU, face.texturedFace.v2 + face.texturedFace.scrollV);
        glVertex3d(face.x2, -face.y2, face.z2);
        glTexCoord2d(face.texturedFace.u3 + face.texturedFace.scrollU, face.texturedFace.v3 + face.texturedFace.scrollV);
        glVertex3d(face.x3, -face.y3, face.z3);
        glEnd();
    }

    private void drawHighlight(RenderModel model, Projection projection, double farPlane, int face,
                               Color fill, Color stroke, float lineWidth) {
        if (face < 0 || face >= model.faceCount) {
            return;
        }
        int a = model.faceA[face];
        int b = model.faceB[face];
        int c = model.faceC[face];
        if (projection.viewZ[a] <= NEAR_PLANE || projection.viewZ[b] <= NEAR_PLANE || projection.viewZ[c] <= NEAR_PLANE) {
            return;
        }
        configurePerspective(Math.max(1, projection.rawWidth), Math.max(1, projection.rawHeight), projection.focalLength, farPlane);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glColor4f(fill.getRed() / 255f, fill.getGreen() / 255f, fill.getBlue() / 255f, fill.getAlpha() / 255f);
        glBegin(GL_TRIANGLES);
        glVertex3d(projection.viewX[a], -projection.viewY[a], -projection.viewZ[a]);
        glVertex3d(projection.viewX[b], -projection.viewY[b], -projection.viewZ[b]);
        glVertex3d(projection.viewX[c], -projection.viewY[c], -projection.viewZ[c]);
        glEnd();
        glLineWidth(lineWidth);
        glColor4f(stroke.getRed() / 255f, stroke.getGreen() / 255f, stroke.getBlue() / 255f, stroke.getAlpha() / 255f);
        glBegin(GL_LINE_LOOP);
        glVertex3d(projection.viewX[a], -projection.viewY[a], -projection.viewZ[a]);
        glVertex3d(projection.viewX[b], -projection.viewY[b], -projection.viewZ[b]);
        glVertex3d(projection.viewX[c], -projection.viewY[c], -projection.viewZ[c]);
        glEnd();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
    }

    private Projection projectModel(MeshCache mesh, int rawWidth, int rawHeight, double pitchDegrees, double yawDegrees, double zoom) {
        double maxDimension = Math.max(1.0, Math.max(mesh.model.getWidth(), Math.max(mesh.model.getHeight(), mesh.model.getDepth())));
        double fitScale = Math.min(rawWidth, rawHeight) * 0.7 / maxDimension;
        double distance = Math.min(rawWidth, rawHeight) * (1.8 / Math.max(0.2, zoom));
        double centerX = rawWidth * 0.5;
        double centerY = rawHeight * 0.5;
        double focalLength = Math.min(rawWidth, rawHeight) * 1.1;
        double pitchRadians = Math.toRadians(pitchDegrees);
        double yawRadians = Math.toRadians(yawDegrees);
        double yawSin = Math.sin(yawRadians);
        double yawCos = Math.cos(yawRadians);
        double pitchSin = Math.sin(pitchRadians);
        double pitchCos = Math.cos(pitchRadians);
        double maxViewZ = NEAR_PLANE;
        for (int i = 0; i < mesh.model.vertexCount; i++) {
            double x = mesh.centeredX[i] * fitScale;
            double y = mesh.centeredY[i] * fitScale;
            double z = mesh.centeredZ[i] * fitScale;
            double rotatedX = x * yawCos + z * yawSin;
            double rotatedZ = -x * yawSin + z * yawCos;
            double rotatedY = y * pitchCos - rotatedZ * pitchSin;
            rotatedZ = y * pitchSin + rotatedZ * pitchCos;
            rotatedZ += distance;
            mesh.viewX[i] = rotatedX;
            mesh.viewY[i] = rotatedY;
            mesh.viewZ[i] = rotatedZ;
            maxViewZ = Math.max(maxViewZ, rotatedZ);
            double depthValue = Math.abs(rotatedZ) < NEAR_PLANE ? (rotatedZ < 0.0 ? -NEAR_PLANE : NEAR_PLANE) : rotatedZ;
            mesh.screenX[i] = (int) Math.round(centerX + (rotatedX * focalLength) / depthValue);
            mesh.screenY[i] = (int) Math.round(centerY + (rotatedY * focalLength) / depthValue);
        }
        return new Projection(mesh.screenX, mesh.screenY, mesh.viewX, mesh.viewY, mesh.viewZ, maxViewZ,
                yawSin, yawCos, pitchSin, pitchCos, rawWidth, rawHeight, focalLength);
    }

    private void configurePerspective(int rawWidth, int rawHeight, double focalLength, double farPlane) {
        double halfWidth = rawWidth * 0.5 * NEAR_PLANE / focalLength;
        double halfHeight = rawHeight * 0.5 * NEAR_PLANE / focalLength;
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glFrustum(-halfWidth, halfWidth, -halfHeight, halfHeight, NEAR_PLANE, farPlane);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    private MeshCache meshCache(RenderModel model) {
        if (cachedMesh != null && cachedModel == model) {
            return cachedMesh;
        }
        double[] centeredX = new double[model.vertexCount];
        double[] centeredY = new double[model.vertexCount];
        double[] centeredZ = new double[model.vertexCount];
        double centerModelX = (model.minX + model.maxX) * 0.5;
        double centerModelY = (model.minY + model.maxY) * 0.5;
        double centerModelZ = (model.minZ + model.maxZ) * 0.5;
        for (int i = 0; i < model.vertexCount; i++) {
            centeredX[i] = model.verticesX[i] - centerModelX;
            centeredY[i] = model.verticesY[i] - centerModelY;
            centeredZ[i] = model.verticesZ[i] - centerModelZ;
        }
        List<FaceInfo> faces = new ArrayList<>(model.faceCount);
        for (int faceIndex = 0; faceIndex < model.faceCount; faceIndex++) {
            if (isNonSolidFace(model, faceIndex)) {
                continue;
            }
            int alpha = faceAlpha(model.faceAlphas, faceIndex);
            if (alpha == 0) {
                continue;
            }
            int a = model.faceA[faceIndex];
            int b = model.faceB[faceIndex];
            int c = model.faceC[faceIndex];
            double abX = centeredX[b] - centeredX[a];
            double abY = centeredY[b] - centeredY[a];
            double abZ = centeredZ[b] - centeredZ[a];
            double acX = centeredX[c] - centeredX[a];
            double acY = centeredY[c] - centeredY[a];
            double acZ = centeredZ[c] - centeredZ[a];
            double normalX = abY * acZ - abZ * acY;
            double normalY = abZ * acX - abX * acZ;
            double normalZ = abX * acY - abY * acX;
            double normalLength = Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
            if (normalLength == 0.0) {
                continue;
            }
            TextureInfo textureInfo = textureInfo(model, faceIndex);
            faces.add(new FaceInfo(faceIndex, a, b, c, alpha,
                    normalX / normalLength, normalY / normalLength, normalZ / normalLength, textureInfo));
        }
        cachedModel = model;
        cachedMesh = new MeshCache(model, centeredX, centeredY, centeredZ, faces.toArray(new FaceInfo[0]));
        return cachedMesh;
    }

    private int textureHandle(int textureId, BufferedImage image) {
        Integer handle = textureHandles.get(textureId);
        if (handle != null) {
            return handle;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                buffer.put((byte) ((argb >> 16) & 0xFF));
                buffer.put((byte) ((argb >> 8) & 0xFF));
                buffer.put((byte) (argb & 0xFF));
                buffer.put((byte) ((argb >>> 24) & 0xFF));
            }
        }
        buffer.flip();
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        textureHandles.put(textureId, texture);
        return texture;
    }

    private static boolean isNonSolidFace(RenderModel model, int face) {
        return model.faceRenderTypes != null
                && face < model.faceRenderTypes.length
                && model.faceRenderTypes[face] >= 2;
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

    private static int applyBrightness(int channel, double brightness) {
        double ambient = 24.0;
        return (int) clamp(ambient + channel * brightness, 0.0, 255.0);
    }

    private static int faceAlpha(int[] faceAlphas, int index) {
        if (faceAlphas != null && index < faceAlphas.length) {
            return 255 - (faceAlphas[index] & 0xFF);
        }
        return 255;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double edge(double x1, double y1, double x2, double y2, double px, double py) {
        return (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
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

    private static TexturedFace texturedFace(TextureInfo info, double brightness, int alpha) {
        if (info == null) {
            return null;
        }
        return new TexturedFace(info.textureId, info.image, brightness, alpha,
                info.u1, info.v1, info.u2, info.v2, info.u3, info.v3,
                TextureLoader.scrollU(info.textureId), TextureLoader.scrollV(info.textureId));
    }

    private static TextureInfo textureInfo(RenderModel model, int face) {
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
        return new TextureInfo(textureId, texture,
                uv.u1, uv.v1, uv.u2, uv.v2, uv.u3, uv.v3,
                TextureLoader.scrollU(textureId), TextureLoader.scrollV(textureId));
    }

    private static final class Projection {

        private final int[] screenX;

        private final int[] screenY;

        private final double[] viewX;

        private final double[] viewY;

        private final double[] viewZ;

        private final double maxViewZ;

        private final double yawSin;

        private final double yawCos;

        private final double pitchSin;

        private final double pitchCos;

        private final int rawWidth;

        private final int rawHeight;

        private final double focalLength;

        private Projection(int[] screenX, int[] screenY, double[] viewX, double[] viewY, double[] viewZ, double maxViewZ,
                           double yawSin, double yawCos, double pitchSin, double pitchCos,
                           int rawWidth, int rawHeight, double focalLength) {
            this.screenX = screenX;
            this.screenY = screenY;
            this.viewX = viewX;
            this.viewY = viewY;
            this.viewZ = viewZ;
            this.maxViewZ = maxViewZ;
            this.yawSin = yawSin;
            this.yawCos = yawCos;
            this.pitchSin = pitchSin;
            this.pitchCos = pitchCos;
            this.rawWidth = rawWidth;
            this.rawHeight = rawHeight;
            this.focalLength = focalLength;
        }
    }

    private static final class MeshCache {

        private final RenderModel model;

        private final double[] centeredX;

        private final double[] centeredY;

        private final double[] centeredZ;

        private final double[] viewX;

        private final double[] viewY;

        private final double[] viewZ;

        private final int[] screenX;

        private final int[] screenY;

        private final FaceInfo[] faces;

        private MeshCache(RenderModel model, double[] centeredX, double[] centeredY, double[] centeredZ, FaceInfo[] faces) {
            this.model = model;
            this.centeredX = centeredX;
            this.centeredY = centeredY;
            this.centeredZ = centeredZ;
            this.viewX = new double[model.vertexCount];
            this.viewY = new double[model.vertexCount];
            this.viewZ = new double[model.vertexCount];
            this.screenX = new int[model.vertexCount];
            this.screenY = new int[model.vertexCount];
            this.faces = faces;
        }
    }

    private static final class FaceInfo {

        private final int index;

        private final int a;

        private final int b;

        private final int c;

        private final int alpha;

        private final double normalX;

        private final double normalY;

        private final double normalZ;

        private final TextureInfo textureInfo;

        private FaceInfo(int index, int a, int b, int c, int alpha,
                         double normalX, double normalY, double normalZ, TextureInfo textureInfo) {
            this.index = index;
            this.a = a;
            this.b = b;
            this.c = c;
            this.alpha = alpha;
            this.normalX = normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
            this.textureInfo = textureInfo;
        }
    }

    private static final class PendingFace {

        private final double x1;

        private final double y1;

        private final double z1;

        private final double x2;

        private final double y2;

        private final double z2;

        private final double x3;

        private final double y3;

        private final double z3;

        private final int alpha;

        private final int argb;

        private final TexturedFace texturedFace;

        private final double averageDepth;

        private PendingFace(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3,
                            int alpha, int argb, TexturedFace texturedFace, double averageDepth) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;
            this.alpha = alpha;
            this.argb = argb;
            this.texturedFace = texturedFace;
            this.averageDepth = averageDepth;
        }

        private double averageDepth() {
            return averageDepth;
        }

        private PendingFace withoutTexture() {
            return new PendingFace(x1, y1, z1, x2, y2, z2, x3, y3, z3, alpha,
                    0xFFFFFFFF, null, averageDepth);
        }
    }

    private static final class TextureInfo {

        private final int textureId;

        private final BufferedImage image;

        private final float u1;

        private final float v1;

        private final float u2;

        private final float v2;

        private final float u3;

        private final float v3;

        private final double scrollU;

        private final double scrollV;

        private TextureInfo(int textureId, BufferedImage image, float u1, float v1, float u2, float v2,
                            float u3, float v3, double scrollU, double scrollV) {
            this.textureId = textureId;
            this.image = image;
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

    private static final class TexturedFace {

        private final int textureId;

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

        private TexturedFace(int textureId, BufferedImage image, double brightness, int alpha,
                             float u1, float v1, float u2, float v2, float u3, float v3,
                             double scrollU, double scrollV) {
            this.textureId = textureId;
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
