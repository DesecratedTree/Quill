package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.render.IndexedSprite;
import com.desecratedtree.quill.sprite.SpriteArchive;
import com.desecratedtree.quill.sprite.SpriteArchiveCodec;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SpriteEditorFrame extends JFrame {

    private static final int PREVIEW_SIZE = 64;

    private static final int VIEW_SIZE = 512;

    private final DefaultListModel<Integer> groupListModel = new DefaultListModel<>();

    private final JList<Integer> groupList = new JList<>(groupListModel);

    private final JPanel spriteGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

    private final JLabel groupMetaLabel = new JLabel("No group selected");

    private int selectedGroupId = -1;

    private int selectedSpriteIndex = -1;

    private SpriteArchive currentArchive;

    private Timer pendingSpriteSelectionTimer;

    public SpriteEditorFrame() {
        setTitle("Sprite Editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(content);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer groupId = groupList.getSelectedValue();
                if (groupId != null) {
                    loadGroup(groupId);
                }
            }
        });
        groupList.addMouseListener(new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int index = groupList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle bounds = groupList.getCellBounds(index, index);
                    if (bounds != null && bounds.contains(e.getPoint())) {
                        groupList.setSelectedIndex(index);
                    } else {
                        index = -1;
                    }
                }
                createGroupListPopupMenu(index >= 0 ? groupListModel.get(index) : null)
                        .show(e.getComponent(), e.getX(), e.getY());
            }
        });
        JScrollPane groupScrollPane = new JScrollPane(groupList);
        groupScrollPane.setPreferredSize(new Dimension(180, 0));
        content.add(groupScrollPane, BorderLayout.WEST);
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBorder(BorderFactory.createTitledBorder("Sprite Group"));
        center.add(groupMetaLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(spriteGrid), BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);
        spriteGrid.addMouseListener(new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                createGridPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        });
        reloadGroups();
    }

    private void reloadGroups() {
        int[] ids = CacheManager.getSpriteIds();
        Arrays.sort(ids);
        groupListModel.clear();
        for (int id : ids) {
            groupListModel.addElement(id);
        }
        if (!groupListModel.isEmpty()) {
            if (selectedGroupId >= 0) {
                groupList.setSelectedValue(selectedGroupId, true);
            } else {
                groupList.setSelectedIndex(0);
            }
        } else {
            selectedGroupId = -1;
            currentArchive = null;
            selectedSpriteIndex = -1;
            rebuildSpriteGrid();
        }
    }

    private void loadGroup(int groupId) {
        selectedGroupId = groupId;
        selectedSpriteIndex = -1;
        currentArchive = SpriteArchiveCodec.decode(CacheManager.getSpriteData(groupId));
        rebuildSpriteGrid();
    }

    private void rebuildSpriteGrid() {
        spriteGrid.removeAll();
        if (currentArchive == null) {
            groupMetaLabel.setText("No group selected");
            spriteGrid.revalidate();
            spriteGrid.repaint();
            return;
        }
        groupMetaLabel.setText("Group " + selectedGroupId + " | sprites=" + currentArchive.sprites.size());
        for (int i = 0; i < currentArchive.sprites.size(); i++) {
            final int spriteIndex = i;
            IndexedSprite sprite = currentArchive.sprites.get(i);
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(110, 120));
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setText("<html><center>#" + i + "<br>" + sprite.width + "x" + sprite.height + "</center></html>");
            button.setIcon(new ImageIcon(scaleSprite(sprite.toBufferedImage(), PREVIEW_SIZE, PREVIEW_SIZE)));
            button.setBorder(BorderFactory.createLineBorder(i == selectedSpriteIndex ? Color.BLUE : Color.GRAY, i == selectedSpriteIndex ? 2 : 1));
            button.addMouseListener(new MouseAdapter() {

                @Override

                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.getClickCount() >= 2) {
                            cancelPendingSpriteSelection();
                            selectedSpriteIndex = spriteIndex;
                            showSpritePreview(spriteIndex);
                            return;
                        }
                        scheduleSpriteSelection(spriteIndex);
                    }
                    showPopup(e);
                }

                @Override

                public void mouseReleased(MouseEvent e) {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.isPopupTrigger()) {
                        cancelPendingSpriteSelection();
                    }
                    if (!e.isPopupTrigger()) {
                        return;
                    }
                    cancelPendingSpriteSelection();
                    selectedSpriteIndex = spriteIndex;
                    createSpritePopupMenu(spriteIndex).show(e.getComponent(), e.getX(), e.getY());
                }
            });
            spriteGrid.add(button);
        }
        spriteGrid.revalidate();
        spriteGrid.repaint();
    }

    private void scheduleSpriteSelection(int spriteIndex) {
        cancelPendingSpriteSelection();
        pendingSpriteSelectionTimer = new Timer(225, e -> {
            selectedSpriteIndex = spriteIndex;
            rebuildSpriteGrid();
            pendingSpriteSelectionTimer = null;
        });
        pendingSpriteSelectionTimer.setRepeats(false);
        pendingSpriteSelectionTimer.start();
    }

    private void cancelPendingSpriteSelection() {
        if (pendingSpriteSelectionTimer != null) {
            pendingSpriteSelectionTimer.stop();
            pendingSpriteSelectionTimer = null;
        }
    }

    private void addGroup() {
        List<BufferedImage> images = chooseImages(true);
        if (images.isEmpty()) {
            return;
        }
        if (!validatePaletteLimit(images)) {
            return;
        }
        int newId = nextGroupId();
        String input = JOptionPane.showInputDialog(this, "Sprite group id:", String.valueOf(newId));
        if (input == null) {
            return;
        }
        int groupId;
        try {
            groupId = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid group id.", "Sprite Editor", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SpriteArchive archive = new SpriteArchive();
        for (BufferedImage image : images) {
            archive.sprites.add(SpriteArchiveCodec.fromBufferedImage(image));
        }
        writeGroup(groupId, archive, images);
        selectedGroupId = groupId;
        reloadGroups();
    }

    private void replaceGroup() {
        if (selectedGroupId < 0 || currentArchive == null) {
            return;
        }
        List<BufferedImage> images = chooseImages(true);
        if (images.isEmpty()) {
            return;
        }
        if (!validatePaletteLimit(images)) {
            return;
        }
        SpriteArchive archive = new SpriteArchive();
        for (int i = 0; i < images.size(); i++) {
            IndexedSprite sprite = SpriteArchiveCodec.fromBufferedImage(images.get(i));
            if (i < currentArchive.sprites.size()) {
                IndexedSprite existing = currentArchive.sprites.get(i);
                sprite.offsetX = existing.offsetX;
                sprite.offsetY = existing.offsetY;
                sprite.deltaWidth = existing.deltaWidth;
                sprite.deltaHeight = existing.deltaHeight;
            }
            archive.sprites.add(sprite);
        }
        writeGroup(selectedGroupId, archive, images);
        loadGroup(selectedGroupId);
    }

    private void removeGroup() {
        if (selectedGroupId < 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove sprite group " + selectedGroupId + "?", "Sprite Editor",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        CacheManager.removeIndexData(8, selectedGroupId);
        selectedGroupId = -1;
        reloadGroups();
    }

    private void addSpriteToGroup() {
        if (currentArchive == null) {
            return;
        }
        List<BufferedImage> images = chooseImages(true);
        if (images.isEmpty()) {
            return;
        }
        List<BufferedImage> finalImages = currentGroupImages();
        finalImages.addAll(images);
        if (!validatePaletteLimit(finalImages)) {
            return;
        }
        for (BufferedImage image : images) {
            currentArchive.sprites.add(SpriteArchiveCodec.fromBufferedImage(image));
        }
        saveCurrentGroup(images);
    }

    private void replaceSelectedSprite() {
        if (currentArchive == null || selectedSpriteIndex < 0 || selectedSpriteIndex >= currentArchive.sprites.size()) {
            return;
        }
        replaceSprite(selectedSpriteIndex);
    }

    private void saveSelectedSprite() {
        if (currentArchive == null || selectedSpriteIndex < 0 || selectedSpriteIndex >= currentArchive.sprites.size()) {
            return;
        }
        saveSprite(selectedSpriteIndex);
    }

    private void replaceSprite(int spriteIndex) {
        if (currentArchive == null || spriteIndex < 0 || spriteIndex >= currentArchive.sprites.size()) {
            return;
        }
        List<BufferedImage> images = chooseImages(false);
        if (images.isEmpty()) {
            return;
        }
        BufferedImage image = images.get(0);
        List<BufferedImage> finalImages = currentGroupImages();
        finalImages.set(spriteIndex, image);
        if (!validatePaletteLimit(finalImages)) {
            return;
        }
        IndexedSprite existing = currentArchive.sprites.get(spriteIndex);
        IndexedSprite replacement = SpriteArchiveCodec.fromBufferedImage(image);
        replacement.offsetX = existing.offsetX;
        replacement.offsetY = existing.offsetY;
        replacement.deltaWidth = existing.deltaWidth;
        replacement.deltaHeight = existing.deltaHeight;
        currentArchive.sprites.set(spriteIndex, replacement);
        selectedSpriteIndex = spriteIndex;
        saveCurrentGroup(images);
    }

    private void removeSelectedSprite() {
        if (currentArchive == null || selectedSpriteIndex < 0 || selectedSpriteIndex >= currentArchive.sprites.size()) {
            return;
        }
        currentArchive.sprites.remove(selectedSpriteIndex);
        selectedSpriteIndex = -1;
        saveCurrentGroup(null);
    }

    private void saveCurrentGroup(List<BufferedImage> sourceImages) {
        writeGroup(selectedGroupId, currentArchive, sourceImages);
        loadGroup(selectedGroupId);
    }

    private void writeGroup(int groupId, SpriteArchive archive, List<BufferedImage> images) {
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < archive.sprites.size() && i < images.size(); i++) {
                IndexedSprite sprite = archive.sprites.get(i);
                sprite.width = images.get(i).getWidth();
                sprite.height = images.get(i).getHeight();
            }
        }
        try {
            byte[] encoded = SpriteArchiveCodec.encode(buildArchiveWithImages(archive, images));
            CacheManager.writeIndexData(8, groupId, encoded);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save sprite group: " + ex.getMessage(), "Sprite Editor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SpriteArchive buildArchiveWithImages(SpriteArchive archive, List<BufferedImage> images) {
        if (images == null) {
            images = new ArrayList<>(archive.sprites.size());
            for (IndexedSprite sprite : archive.sprites) {
                images.add(sprite.toBufferedImage());
            }
        }
        SpriteArchive rebuilt = new SpriteArchive();
        for (int i = 0; i < archive.sprites.size(); i++) {
            IndexedSprite original = archive.sprites.get(i);
            IndexedSprite copy = SpriteArchiveCodec.fromBufferedImage(images.get(i));
            copy.offsetX = original.offsetX;
            copy.offsetY = original.offsetY;
            copy.deltaWidth = original.deltaWidth;
            copy.deltaHeight = original.deltaHeight;
            rebuilt.sprites.add(copy);
        }
        rebuilt.canvasWidth = archive.canvasWidth;
        rebuilt.canvasHeight = archive.canvasHeight;
        return rebuilt;
    }

    private List<BufferedImage> chooseImages(boolean multiple) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(multiple ? "Select Sprite PNGs" : "Select Sprite PNG");
        chooser.setMultiSelectionEnabled(multiple);
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return new ArrayList<>();
        }
        File[] files = multiple ? chooser.getSelectedFiles() : new File[] {chooser.getSelectedFile()};
        List<BufferedImage> images = new ArrayList<>();
        for (File file : files) {
            try {
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    throw new IOException("Unsupported image: " + file.getName());
                }
                images.add(SpriteArchiveCodec.normalizeSpriteImage(image));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load " + file.getName() + ": " + ex.getMessage(),
                        "Sprite Editor", JOptionPane.ERROR_MESSAGE);
                return new ArrayList<>();
            }
        }
        return images;
    }

    private int nextGroupId() {
        int[] ids = CacheManager.getSpriteIds();
        int next = 0;
        for (int id : ids) {
            next = Math.max(next, id + 1);
        }
        return next;
    }

    private List<BufferedImage> currentGroupImages() {
        List<BufferedImage> images = new ArrayList<>(currentArchive == null ? 0 : currentArchive.sprites.size());
        if (currentArchive != null) {
            for (IndexedSprite sprite : currentArchive.sprites) {
                images.add(sprite.toBufferedImage());
            }
        }
        return images;
    }

    private boolean validatePaletteLimit(List<BufferedImage> images) {
        try {
            SpriteArchiveCodec.validatePaletteLimit(images);
            return true;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sprite Editor", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private JPopupMenu createGroupListPopupMenu(Integer groupId) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = new JMenuItem("Add Group");
        add.addActionListener(e -> addGroup());
        menu.add(add);
        if (groupId != null) {
            JMenuItem remove = new JMenuItem("Remove Group");
            remove.addActionListener(e -> {
                groupList.setSelectedValue(groupId, true);
                removeGroup();
            });
            menu.add(remove);
        }
        return menu;
    }

    private JPopupMenu createGridPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = new JMenuItem("Add Sprite");
        add.addActionListener(e -> addSpriteToGroup());
        add.setEnabled(currentArchive != null);
        menu.add(add);
        return menu;
    }

    private JPopupMenu createSpritePopupMenu(int spriteIndex) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = new JMenuItem("Add Sprite");
        add.addActionListener(e -> addSpriteToGroup());
        JMenuItem view = new JMenuItem("View Sprite");
        view.addActionListener(e -> showSpritePreview(spriteIndex));
        JMenuItem replace = new JMenuItem("Replace Sprite");
        replace.addActionListener(e -> replaceSprite(spriteIndex));
        JMenuItem save = new JMenuItem("Save Sprite As PNG");
        save.addActionListener(e -> saveSprite(spriteIndex));
        JMenuItem remove = new JMenuItem("Remove Sprite");
        remove.addActionListener(e -> {
            selectedSpriteIndex = spriteIndex;
            removeSelectedSprite();
        });
        menu.add(add);
        menu.add(view);
        menu.add(replace);
        menu.add(save);
        menu.add(remove);
        return menu;
    }

    private void showSpritePreview(int spriteIndex) {
        if (currentArchive == null || spriteIndex < 0 || spriteIndex >= currentArchive.sprites.size()) {
            return;
        }
        selectedSpriteIndex = spriteIndex;
        rebuildSpriteGrid();
        IndexedSprite sprite = currentArchive.sprites.get(spriteIndex);
        BufferedImage image = sprite.toBufferedImage();
        JDialog dialog = new JDialog(this, "Sprite " + selectedGroupId + ":" + spriteIndex, false);
        dialog.setLayout(new BorderLayout(0, 8));
        JLabel imageLabel = new JLabel(new ImageIcon(scaleSprite(image, VIEW_SIZE, VIEW_SIZE)));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        imageLabel.addMouseListener(new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                createSpritePopupMenu(spriteIndex).show(e.getComponent(), e.getX(), e.getY());
            }
        });
        dialog.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        JLabel meta = new JLabel("Group " + selectedGroupId + " | sprite #" + spriteIndex + " | "
                + image.getWidth() + "x" + image.getHeight(), SwingConstants.CENTER);
        dialog.add(meta, BorderLayout.SOUTH);
        dialog.setSize(560, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveSprite(int spriteIndex) {
        if (currentArchive == null || spriteIndex < 0 || spriteIndex >= currentArchive.sprites.size()) {
            return;
        }
        BufferedImage image = currentArchive.sprites.get(spriteIndex).toBufferedImage();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Sprite " + selectedGroupId + ":" + spriteIndex);
        chooser.setSelectedFile(new File(selectedGroupId + "-" + spriteIndex + ".png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            ImageIO.write(image, "png", chooser.getSelectedFile());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save sprite: " + ex.getMessage(),
                    "Sprite Editor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static BufferedImage scaleSprite(BufferedImage image, int maxWidth, int maxHeight) {
        int scale = Math.max(1, Math.min(maxWidth / Math.max(1, image.getWidth()), maxHeight / Math.max(1, image.getHeight())));
        BufferedImage scaled = new BufferedImage(image.getWidth() * scale, image.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        graphics.dispose();
        return scaled;
    }
}
