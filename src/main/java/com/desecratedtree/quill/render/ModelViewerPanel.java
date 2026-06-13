package com.desecratedtree.quill.render;

import com.desecratedtree.quill.texture.MaterialDefinition;
import com.desecratedtree.quill.texture.MaterialLoader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class ModelViewerPanel extends JPanel {

    private static final double PROJECTION_SCALE = 3.0;

    private RenderModel model;

    private Supplier<RenderModel> modelSupplier;

    private double pitchDegrees = -18.0;

    private double yawDegrees = 140.0;

    private double zoom = 1.0;

    private Point lastDragPoint;

    private final Timer animationTimer;

    private IntConsumer faceClickListener;

    private BiConsumer<Integer, Integer> faceInteractionListener;

    private BiConsumer<Integer, Integer> groupClickListener;

    private int selectedFace = -1;

    private int selectedGroup = -1;

    private int hoveredFace = -1;

    private int hoveredGroup = -1;

    private String footerText;

    private final Component renderSurface;

    private final JComponent footerPanel;

    private boolean timerNeeded;

    private Projection cachedProjection;

    private RenderModel cachedProjectionModel;

    private int cachedProjectionWidth = -1;

    private int cachedProjectionHeight = -1;

    private double cachedProjectionPitch;

    private double cachedProjectionYaw;

    private double cachedProjectionZoom;

    public ModelViewerPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(20, 23, 27));
        Component surface = createRenderSurface();
        renderSurface = surface;
        footerPanel = new FooterPanel();
        add(renderSurface, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
        animationTimer = new Timer(80, e -> {
            if (modelSupplier != null && isShowing()) {
                model = modelSupplier.get();
                invalidateProjectionCache();
            }
            if (model != null && isShowing()) {
                repaintViewer();
            }
        });
        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
            }

            @Override

            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint == null) {
                    lastDragPoint = e.getPoint();
                    return;
                }
                int dx = e.getX() - lastDragPoint.x;
                int dy = e.getY() - lastDragPoint.y;
                yawDegrees += dx * 0.75;
                pitchDegrees = clamp(pitchDegrees + dy * 0.5, -89.0, 89.0);
                lastDragPoint = e.getPoint();
                repaintViewer();
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                lastDragPoint = null;
            }

            @Override

            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || model == null) {
                    return;
                }
                int face = pickFace(e.getPoint());
                if (face < 0) {
                    return;
                }
                selectedFace = face;
                hoveredFace = face;
                if (model.faceGroupMajority != null && face >= 0 && face < model.faceGroupMajority.length) {
                    int group = model.faceGroupMajority[face];
                    selectedGroup = group;
                    hoveredGroup = group;
                    if (groupClickListener != null) {
                        groupClickListener.accept(face, group);
                    }
                }
                if (faceClickListener != null) {
                    faceClickListener.accept(face);
                }
                if (faceInteractionListener != null) {
                    faceInteractionListener.accept(face, e.getClickCount());
                }
                repaintViewer();
            }

            @Override

            public void mouseMoved(MouseEvent e) {
                updateHover(e.getPoint());
            }

            @Override

            public void mouseExited(MouseEvent e) {
                if (hoveredFace != -1) {
                    hoveredFace = -1;
                    repaintViewer();
                }
            }

            @Override

            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom = clamp(zoom - e.getPreciseWheelRotation() * 0.1, 0.25, 4.0);
                repaintViewer();
            }
        };
        renderSurface.addMouseListener(mouseAdapter);
        renderSurface.addMouseMotionListener(mouseAdapter);
        renderSurface.addMouseWheelListener(mouseAdapter);
        updateTooltip();
    }

    @Override

    public void addNotify() {
        super.addNotify();
        updateAnimationTimerState();
    }

    @Override

    public void removeNotify() {
        animationTimer.stop();
        super.removeNotify();
    }

    public void setModel(RenderModel model) {
        this.modelSupplier = null;
        this.model = model;
        invalidateProjectionCache();
        updateAnimationTimerState();
        repaintViewer();
    }

    public void setModelSupplier(Supplier<RenderModel> modelSupplier) {
        this.modelSupplier = modelSupplier;
        this.model = modelSupplier == null ? null : modelSupplier.get();
        invalidateProjectionCache();
        updateAnimationTimerState();
        repaintViewer();
    }

    public void resetView() {
        pitchDegrees = -18.0;
        yawDegrees = 140.0;
        zoom = 1.0;
        invalidateProjectionCache();
        repaintViewer();
    }

    public void setFaceClickListener(IntConsumer faceClickListener) {
        this.faceClickListener = faceClickListener;
    }

    public void setFaceInteractionListener(BiConsumer<Integer, Integer> faceInteractionListener) {
        this.faceInteractionListener = faceInteractionListener;
    }

    public void setGroupClickListener(BiConsumer<Integer, Integer> groupClickListener) {
        this.groupClickListener = groupClickListener;
    }

    public void setSelectedFace(int selectedFace) {
        this.selectedFace = selectedFace;
        repaintViewer();
    }

    public void setSelectedGroup(int selectedGroup) {
        this.selectedGroup = selectedGroup;
        repaintViewer();
    }

    public int getSelectedGroup() {
        return selectedGroup;
    }

    public int getSelectedFace() {
        return selectedFace;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
        repaintViewer();
    }

    private void paintSoftwareComponent(Graphics graphics) {
        BufferedImage image = SoftwareModelRenderer.renderModelPreview(
                model,
                Math.max(1, renderSurface.getWidth()),
                Math.max(1, renderSurface.getHeight()),
                pitchDegrees,
                yawDegrees,
                zoom
        );
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.drawImage(image, 0, 0, null);
        Projection projection = projectedModel();
        if (model != null && model.faceGroupMajority != null) {
            paintGroupOverlay(g2, projection, hoveredGroup, new Color(92, 214, 120, 56), new Color(104, 232, 136, 180), 1.4f);
            paintGroupOverlay(g2, projection, selectedGroup, new Color(128, 200, 255, 40), new Color(145, 210, 255, 220), 2.6f);
        } else {
            paintFaceOverlay(g2, projection, hoveredFace, new Color(92, 214, 120, 86), new Color(104, 232, 136, 210), 1.8f);
            paintFaceOverlay(g2, projection, selectedFace, new Color(128, 200, 255, hoveredFace == selectedFace ? 52 : 30),
                    new Color(145, 210, 255, 255), 2.8f);
        }
        g2.dispose();
    }

    private void paintGroupOverlay(Graphics2D g2, Projection projection, int group, Color fill, Color stroke, float strokeWidth) {
        if (projection == null || model == null || group < 0) return;
        for (int face = 0; face < model.faceCount; face++) {
            if (model.faceGroupMajority != null && face < model.faceGroupMajority.length && model.faceGroupMajority[face] == group) {
                paintFaceOverlay(g2, projection, face, fill, stroke, strokeWidth);
            }
        }
    }

    private void paintFooter(Graphics2D g2) {
        List<String> lines = footerLines();
        if (lines.isEmpty()) {
            return;
        }
        Rectangle clip = g2.getClipBounds();
        int availableWidth = clip == null ? getWidth() : clip.width;
        int availableHeight = clip == null ? getHeight() : clip.height;
        Font font = g2.getFont().deriveFont(Font.PLAIN, 12f);
        FontMetrics metrics = g2.getFontMetrics(font);
        int paddingX = 10;
        int paddingY = 6;
        int textWidth = 0;
        for (String line : lines) {
            textWidth = Math.max(textWidth, metrics.stringWidth(line));
        }
        int boxWidth = Math.min(Math.max(0, availableWidth - 16), textWidth + paddingX * 2);
        int lineHeight = metrics.getHeight();
        int boxHeight = lineHeight * lines.size() + paddingY * 2;
        int x = 8;
        int y = Math.max(0, availableHeight - boxHeight - 8);
        g2.setColor(new Color(10, 14, 18, 200));
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 10, 10);
        g2.setColor(new Color(100, 118, 136, 220));
        g2.drawRoundRect(x, y, boxWidth, boxHeight, 10, 10);
        g2.setColor(new Color(235, 240, 245, 235));
        g2.setFont(font);
        int baseline = y + paddingY + metrics.getAscent();
        for (String line : lines) {
            g2.drawString(line, x + paddingX, baseline);
            baseline += lineHeight;
        }
    }

    private List<String> footerLines() {
        List<String> lines = new ArrayList<>();
        if (footerText != null && !footerText.trim().isEmpty()) {
            lines.add(footerText.trim());
        } else if (selectedGroup >= 0 && model != null && model.faceGroupMajority != null) {
            lines.add("Group " + selectedGroup + " selected");
            int vertexCount = countVerticesInGroup(selectedGroup);
            lines.add(vertexCount + " vertices");
        } else if (selectedFace >= 0) {
            lines.add("Face " + selectedFace + " selected");
        } else if (hoveredFace >= 0) {
            int hg = hoveredGroup >= 0 ? hoveredGroup : -1;
            lines.add(hg >= 0 ? "Group " + hg + " (face " + hoveredFace + ")" : "Face " + hoveredFace + " hovered");
        } else if (model != null) {
            lines.add("Model view");
        }
        if (model != null) {
            lines.add(String.format("Drag rotate | Wheel zoom | Zoom %.2fx", zoom));
            if (selectedGroup < 0 && selectedFace < 0) {
                lines.add(model.faceGroupMajority != null ? "Click to select group" : "Click to select face");
            }
        }
        return lines;
    }

    private int countVerticesInGroup(int group) {
        if (model == null || model.vertexSkins == null) return 0;
        int count = 0;
        for (int i = 0; i < model.vertexSkins.length; i++) {
            if (model.vertexSkins[i] == group) count++;
        }
        return count;
    }

    private void updateHover(Point point) {
        int face = pickFace(point);
        if (face != hoveredFace) {
            hoveredFace = face;
            if (face >= 0 && model != null && model.faceGroupMajority != null && face < model.faceGroupMajority.length) {
                hoveredGroup = model.faceGroupMajority[face];
            } else {
                hoveredGroup = -1;
            }
            repaintViewer();
        }
    }

    private void repaintViewer() {
        updateTooltip();
        renderSurface.repaint();
        footerPanel.revalidate();
        footerPanel.repaint();
    }

    private void updateTooltip() {
        List<String> lines = footerLines();
        setToolTipText(lines.isEmpty() ? null : String.join(" | ", lines));
    }

    private Component createRenderSurface() {
        if (!Boolean.getBoolean("quill.renderer.software")) {
            try {
                return new LwjglModelViewerCanvas(
                        () -> model,
                        () -> pitchDegrees,
                        () -> yawDegrees,
                        () -> zoom,
                        () -> hoveredFace,
                        () -> selectedFace
                );
            } catch (Throwable ignored) {
            }
        }
        return new SoftwareRenderSurface();
    }

    private void paintFaceOverlay(Graphics2D g2, Projection projection, int face, Color fill, Color stroke, float strokeWidth) {
        if (projection == null || model == null || face < 0 || face >= model.faceCount) {
            return;
        }
        int a = model.faceA[face];
        int b = model.faceB[face];
        int c = model.faceC[face];
        if (projection.viewZ[a] <= 50.0 || projection.viewZ[b] <= 50.0 || projection.viewZ[c] <= 50.0) {
            return;
        }
        Path2D path = new Path2D.Double();
        path.moveTo(projection.screenX[a] / PROJECTION_SCALE, projection.screenY[a] / PROJECTION_SCALE);
        path.lineTo(projection.screenX[b] / PROJECTION_SCALE, projection.screenY[b] / PROJECTION_SCALE);
        path.lineTo(projection.screenX[c] / PROJECTION_SCALE, projection.screenY[c] / PROJECTION_SCALE);
        path.closePath();
        Stroke oldStroke = g2.getStroke();
        g2.setColor(fill);
        g2.fill(path);
        if (stroke != null && strokeWidth > 0f) {
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.draw(path);
        }
        g2.setStroke(oldStroke);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int pickFace(Point point) {
        if (renderSurface instanceof LwjglModelViewerCanvas) {
            return ((LwjglModelViewerCanvas) renderSurface).pickFace(point);
        }
        Projection projection = projectedModel();
        if (projection == null) {
            return -1;
        }
        double sampleX = point.x * PROJECTION_SCALE;
        double sampleY = point.y * PROJECTION_SCALE;
        int pickedFace = -1;
        double pickedDepth = Double.POSITIVE_INFINITY;
        for (int face = 0; face < model.faceCount; face++) {
            int a = model.faceA[face];
            int b = model.faceB[face];
            int c = model.faceC[face];
            if (projection.viewZ[a] <= 50.0 || projection.viewZ[b] <= 50.0 || projection.viewZ[c] <= 50.0) {
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
                pickedFace = face;
            }
        }
        return pickedFace;
    }

    private Projection projectedModel() {
        if (model == null) {
            return null;
        }
        int width = Math.max(1, renderSurface.getWidth() > 0 ? renderSurface.getWidth() : getWidth());
        int height = Math.max(1, renderSurface.getHeight() > 0 ? renderSurface.getHeight() : getHeight());
        if (cachedProjection != null
                && cachedProjectionModel == model
                && cachedProjectionWidth == width
                && cachedProjectionHeight == height
                && cachedProjectionPitch == pitchDegrees
                && cachedProjectionYaw == yawDegrees
                && cachedProjectionZoom == zoom) {
            return cachedProjection;
        }
        double rawWidth = width * PROJECTION_SCALE;
        double rawHeight = height * PROJECTION_SCALE;
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
        int[] screenX = new int[model.vertexCount];
        int[] screenY = new int[model.vertexCount];
        double[] viewZ = new double[model.vertexCount];
        for (int i = 0; i < model.vertexCount; i++) {
            double x = (model.verticesX[i] - centerModelX) * fitScale;
            double y = (model.verticesY[i] - centerModelY) * fitScale;
            double z = (model.verticesZ[i] - centerModelZ) * fitScale;
            double yawX = x * Math.cos(yawRadians) + z * Math.sin(yawRadians);
            double yawZ = -x * Math.sin(yawRadians) + z * Math.cos(yawRadians);
            double pitchY = y * Math.cos(pitchRadians) - yawZ * Math.sin(pitchRadians);
            double pitchZ = y * Math.sin(pitchRadians) + yawZ * Math.cos(pitchRadians) + distance;
            viewZ[i] = pitchZ;
            double depthValue = Math.abs(pitchZ) < 50.0 ? (pitchZ < 0.0 ? -50.0 : 50.0) : pitchZ;
            screenX[i] = (int) Math.round(centerX + (yawX * focalLength) / depthValue);
            screenY[i] = (int) Math.round(centerY + (pitchY * focalLength) / depthValue);
        }
        cachedProjection = new Projection(screenX, screenY, viewZ);
        cachedProjectionModel = model;
        cachedProjectionWidth = width;
        cachedProjectionHeight = height;
        cachedProjectionPitch = pitchDegrees;
        cachedProjectionYaw = yawDegrees;
        cachedProjectionZoom = zoom;
        return cachedProjection;
    }

    private void invalidateProjectionCache() {
        cachedProjection = null;
        cachedProjectionModel = null;
        cachedProjectionWidth = -1;
        cachedProjectionHeight = -1;
    }

    private void updateAnimationTimerState() {
        timerNeeded = modelSupplier != null || modelUsesAnimatedTextures(model);
        if (!isDisplayable()) {
            return;
        }
        if (timerNeeded) {
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }
        } else {
            animationTimer.stop();
        }
    }

    private static boolean modelUsesAnimatedTextures(RenderModel model) {
        if (model == null || model.faceTextures == null) {
            return false;
        }
        for (int textureId : model.faceTextures) {
            if (textureId < 0) {
                continue;
            }
            MaterialDefinition material = MaterialLoader.get(textureId);
            if (material != null && (material.field198 != 0 || material.field211 != 0)) {
                return true;
            }
        }
        return false;
    }

    private static double edge(double x1, double y1, double x2, double y2, double px, double py) {
        return (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
    }
    static double[] rotateX(double x, double y, double z, double radians) {
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new double[]{x, y * cos - z * sin, y * sin + z * cos};
    }
    static double[] rotateY(double x, double y, double z, double radians) {
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new double[]{x * cos + z * sin, y, -x * sin + z * cos};
    }

    private static final class Projection {

        private final int[] screenX;

        private final int[] screenY;

        private final double[] viewZ;

        private Projection(int[] screenX, int[] screenY, double[] viewZ) {
            this.screenX = screenX;
            this.screenY = screenY;
            this.viewZ = viewZ;
        }
    }

    private final class SoftwareRenderSurface extends JComponent {

        @Override

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            paintSoftwareComponent(graphics);
        }
    }

    private final class FooterPanel extends JComponent {

        private FooterPanel() {
            setOpaque(false);
            setEnabled(false);
            setFocusable(false);
        }

        @Override

        public Dimension getPreferredSize() {
            List<String> lines = footerLines();
            if (lines.isEmpty()) {
                return new Dimension(0, 0);
            }
            FontMetrics metrics = getFontMetrics(getFont().deriveFont(Font.PLAIN, 12f));
            int height = metrics.getHeight() * lines.size() + 12 + 8;
            return new Dimension(10, height);
        }

        @Override

        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            paintFooter(g2);
            g2.dispose();
        }
    }
}
