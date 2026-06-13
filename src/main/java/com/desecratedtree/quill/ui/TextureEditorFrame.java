package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.texture.TextureDefinitionTable;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.util.ProjectPaths;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class TextureEditorFrame extends JFrame {

    private final DefaultListModel<Integer> textureListModel = new DefaultListModel<>();

    private final JList<Integer> textureList = new JList<>(textureListModel);

    private final JLabel previewLabel = new JLabel("", SwingConstants.CENTER);

    private final JLabel metaLabel = new JLabel("No texture selected", SwingConstants.CENTER);

    private TextureDefinitionTable textureDefinitions;

    public TextureEditorFrame() {
        setTitle("Texture Editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(content);
        textureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        textureList.setCellRenderer(new TextureListRenderer());
        textureList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePreview();
            }
        });
        textureList.addMouseListener(new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                maybeShowTextureMenu(e);
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                maybeShowTextureMenu(e);
            }
        });
        content.add(new JScrollPane(textureList), BorderLayout.WEST);
        JPanel previewPanel = new JPanel(new BorderLayout(0, 8));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        JScrollPane previewScroll = new JScrollPane(previewLabel);
        previewPanel.add(previewScroll, BorderLayout.CENTER);
        previewPanel.add(metaLabel, BorderLayout.SOUTH);
        content.add(previewPanel, BorderLayout.CENTER);
        MouseAdapter previewMenuListener = new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                maybeShowPreviewMenu(e);
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                maybeShowPreviewMenu(e);
            }
        };
        previewLabel.addMouseListener(previewMenuListener);
        metaLabel.addMouseListener(previewMenuListener);
        previewScroll.addMouseListener(previewMenuListener);
        reloadTextures();
    }

    private void reloadTextures() {
        textureDefinitions = TextureDefinitionTable.load();
        textureListModel.clear();
        for (Integer id : textureDefinitions.presentIds()) {
            textureListModel.addElement(id);
        }
        if (!textureListModel.isEmpty()) {
            textureList.setSelectedIndex(0);
        } else {
            updatePreview();
        }
    }

    private Integer selectedTextureId() {
        return textureList.getSelectedValue();
    }

    private void updatePreview() {
        Integer textureId = selectedTextureId();
        if (textureId == null) {
            previewLabel.setIcon(null);
            previewLabel.setText("No texture selected");
            metaLabel.setText("No texture selected");
            return;
        }
        BufferedImage image = TextureLoader.previewTexture(textureId);
        if (image == null) {
            previewLabel.setIcon(null);
            previewLabel.setText("No preview");
            metaLabel.setText("Texture " + textureId);
            return;
        }
        previewLabel.setText(null);
        previewLabel.setIcon(new ImageIcon(scaleImageNearest(image, 512, 512)));
        metaLabel.setText("Texture " + textureId + " | " + image.getWidth() + "x" + image.getHeight());
    }

    private void addTexture() {
        int newId = textureDefinitions.addDefault();
        textureDefinitions.save();
        Path source = ProjectPaths.textureDumpFile(1);
        Path target = ProjectPaths.textureDumpFile(newId);
        try {
            Files.createDirectories(target.getParent());
            if (Files.exists(source)) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to add texture: " + ex.getMessage(), "Texture", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TextureLoader.clearCache();
        reloadTextures();
        textureList.setSelectedValue(newId, true);
    }

    private void replaceTexture() {
        Integer textureId = selectedTextureId();
        if (textureId == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Replace Texture " + textureId);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) {
                throw new IOException("Unsupported image.");
            }
            ImageIO.write(toClientTextureRgb(image), "png", ProjectPaths.textureDumpFile(textureId).toFile());
            TextureLoader.clearCache();
            updatePreview();
            textureList.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to replace texture: " + ex.getMessage(), "Texture", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTexture() {
        Integer textureId = selectedTextureId();
        if (textureId == null) {
            return;
        }
        BufferedImage image = TextureLoader.previewTexture(textureId);
        if (image == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Texture " + textureId);
        chooser.setSelectedFile(new File(textureId + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            ImageIO.write(toClientTextureRgb(image), "png", chooser.getSelectedFile());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save texture: " + ex.getMessage(), "Texture", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeTexture() {
        Integer textureId = selectedTextureId();
        if (textureId == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove texture " + textureId + "?", "Remove Texture",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Path target = ProjectPaths.textureDumpFile(textureId);
            Files.createDirectories(target.getParent());
            BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.dispose();
            ImageIO.write(image, "png", target.toFile());
            TextureLoader.clearCache();
            updatePreview();
            textureList.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to remove texture: " + ex.getMessage(), "Texture", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void maybeShowTextureMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        Integer textureId = textureIdAt(e.getPoint());
        createTexturePopupMenu(textureId).show(e.getComponent(), e.getX(), e.getY());
    }

    private void maybeShowPreviewMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        createTexturePopupMenu(selectedTextureId()).show(e.getComponent(), e.getX(), e.getY());
    }

    private Integer textureIdAt(Point point) {
        int row = textureList.locationToIndex(point);
        if (row < 0) {
            return null;
        }
        Rectangle bounds = textureList.getCellBounds(row, row);
        if (bounds == null || !bounds.contains(point)) {
            return null;
        }
        textureList.setSelectedIndex(row);
        return textureListModel.get(row);
    }

    private JPopupMenu createTexturePopupMenu(Integer textureId) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = new JMenuItem("Add Texture");
        add.addActionListener(e -> addTexture());
        menu.add(add);
        JMenuItem replace = new JMenuItem("Replace Texture");
        replace.setEnabled(textureId != null);
        replace.addActionListener(e -> {
            if (textureId != null) {
                textureList.setSelectedValue(textureId, true);
                replaceTexture();
            }
        });
        menu.add(replace);
        JMenuItem save = new JMenuItem("Save Texture As PNG");
        save.setEnabled(textureId != null);
        save.addActionListener(e -> {
            if (textureId != null) {
                textureList.setSelectedValue(textureId, true);
                saveTexture();
            }
        });
        menu.add(save);
        JMenuItem remove = new JMenuItem("Remove Texture");
        remove.setEnabled(textureId != null);
        remove.addActionListener(e -> {
            if (textureId != null) {
                textureList.setSelectedValue(textureId, true);
                removeTexture();
            }
        });
        menu.add(remove);
        return menu;
    }

    private static BufferedImage toClientTextureRgb(BufferedImage image) {
        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = converted.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, converted.getWidth(), converted.getHeight());
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return converted;
    }

    private static BufferedImage scaleImageNearest(BufferedImage image, int maxWidth, int maxHeight) {
        double scale = Math.min(
                maxWidth / (double) Math.max(1, image.getWidth()),
                maxHeight / (double) Math.max(1, image.getHeight()));
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int drawX = (maxWidth - width) / 2;
        int drawY = (maxHeight - height) / 2;
        graphics.drawImage(image, drawX, drawY, width, height, null);
        graphics.dispose();
        return scaled;
    }

    private static final class TextureListRenderer extends DefaultListCellRenderer {

        @Override

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Integer) {
                int textureId = (Integer) value;
                label.setText("Texture " + textureId);
                BufferedImage image = TextureLoader.previewTexture(textureId);
                label.setIcon(image == null ? null : new ImageIcon(scaleImageNearest(image, 48, 48)));
                label.setHorizontalTextPosition(SwingConstants.RIGHT);
            }
            return label;
        }
    }
}
