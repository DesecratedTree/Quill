package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.render.CacheColor;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.ModelViewerPanel;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.texture.TextureDefinitionTable;
import com.desecratedtree.quill.texture.TextureLoader;
import com.desecratedtree.quill.ui.component.SimpleDocumentListener;
import io.blurite.cache.model.EmissiveTriangle;
import io.blurite.cache.model.FaceBillboard;
import io.blurite.cache.model.Model;
import io.netty.buffer.ByteBuf;

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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ModelEditorDialog extends JDialog {

    private static final int MAX_RECENT_TEXTURES = 12;

    private static final LinkedList<Integer> RECENT_TEXTURES = new LinkedList<>();

    private int modelId;

    private final Runnable onSave;

    private Model model;

    private TextureDefinitionTable textureDefinitions;

    private final ModelViewerPanel viewer = new ModelViewerPanel();

    private final JLabel faceInfo = new JLabel("No face selected");

    private final JTextField colorField = new JTextField("0", 8);

    private final JPanel colorPreview = new JPanel();

    private final DefaultListModel<Integer> modelListModel = new DefaultListModel<>();

    private final JList<Integer> modelList = new JList<>(modelListModel);

    private final JTextField modelSearchField = new JTextField();

    private final DefaultListModel<Integer> textureListModel = new DefaultListModel<>();

    private final JList<Integer> textureList = new JList<>(textureListModel);

    private final JTextField textureIdField = new JTextField(8);

    private final JTextField textureDirectionField = new JTextField("0", 6);

    private final JTextField textureSpeedField = new JTextField("0", 6);

    private final JPanel recentTexturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));

    private final DefaultListModel<String> emitterListModel = new DefaultListModel<>();

    private final JList<String> emitterList = new JList<>(emitterListModel);

    private final JComboBox<DefinitionChoice> particleIdBox = new JComboBox<>();

    private final JTextField particlePriorityField = new JTextField("0", 4);

    private final DefaultListModel<String> billboardListModel = new DefaultListModel<>();

    private final JList<String> billboardList = new JList<>(billboardListModel);

    private final JComboBox<DefinitionChoice> billboardIdBox = new JComboBox<>();

    private final JTextField billboardSkinField = new JTextField("0", 4);

    private final JTextField billboardDistanceField = new JTextField("0", 4);

    private final JTabbedPane editorTabs = new JTabbedPane();

    private int selectedFace = -1;

    private ModelEditorDialog(Window owner, int modelId, String title, Runnable onSave) {
        super(owner, title, ModalityType.MODELESS);
        this.onSave = onSave;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));

        viewer.setPreferredSize(new Dimension(680, 520));
        viewer.setFaceInteractionListener((face, clickCount) -> {
            selectedFace = face;
            viewer.setSelectedFace(face);
            updateFaceInfo();
            if (clickCount >= 2) {
                applySelectedTool();
            }
        });

        add(buildModelListPanel(), BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadModelIds();
        reloadTextureDefinitions();
        loadDefinitionChoices(particleIdBox, 27, "Particle");
        loadDefinitionChoices(billboardIdBox, 29, "Billboard");
        particleIdBox.setRenderer(new DefinitionChoiceRenderer("P"));
        billboardIdBox.setRenderer(new DefinitionChoiceRenderer("B"));
        updateRecentTextureSwatches();
        loadModel(modelId);

        pack();
        setMinimumSize(new Dimension(1450, 820));
    }

    public static void showEditor(Component parent, int modelId, String title, Runnable onSave) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        try {
            ModelEditorDialog dialog = new ModelEditorDialog(owner, modelId, title, onSave);
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Model Editor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildModelListPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setPreferredSize(new Dimension(180, 520));
        panel.setBorder(BorderFactory.createTitledBorder("Models"));

        modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer selected = modelList.getSelectedValue();
                if (selected != null && selected != modelId) {
                    loadModel(selected);
                }
            }
        });
        modelSearchField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::filterModelList));

        panel.add(modelSearchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(modelList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(viewer, BorderLayout.CENTER);

        JPanel info = new JPanel(new BorderLayout());
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Selection"),
                new EmptyBorder(4, 6, 4, 6)
        ));
        info.add(faceInfo, BorderLayout.CENTER);
        panel.add(info, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setPreferredSize(new Dimension(500, 520));
        editorTabs.addTab("Color", buildColorPanel());
        editorTabs.addTab("Texture", buildTexturePanel());
        editorTabs.addTab("Particles", buildParticlesPanel());
        editorTabs.addTab("Billboards", buildBillboardsPanel());
        panel.add(editorTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildColorPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;

        colorPreview.setPreferredSize(new Dimension(40, 24));
        colorPreview.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Packed color:"), c);
        c.gridx = 1;
        form.add(colorField, c);
        c.gridx = 2;
        form.add(colorPreview, c);

        JButton picker = new JButton("Pick Color");
        picker.addActionListener(e -> openColorPicker());
        JButton apply = new JButton("Apply To Selected Face");
        apply.addActionListener(e -> applyColor());

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        form.add(picker, c);
        c.gridy = 2;
        form.add(apply, c);

        colorField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::refreshColorPreview));
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JLabel("Blue border = selected face. Green tint = hover. Double-click paints with the active tool."),
                BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTexturePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        textureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        textureList.setFixedCellHeight(40);
        textureList.setCellRenderer(new TextureCellRenderer());
        textureList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer selected = textureList.getSelectedValue();
                if (selected != null) {
                    textureIdField.setText(String.valueOf(selected));
                }
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

        JPanel top = new JPanel(new BorderLayout(6, 6));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Texture id:"), c);
        c.gridx = 1;
        form.add(textureIdField, c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Direction:"), c);
        c.gridx = 1;
        form.add(textureDirectionField, c);

        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Speed:"), c);
        c.gridx = 1;
        form.add(textureSpeedField, c);

        JButton addTexture = new JButton("Add Texture");
        addTexture.addActionListener(e -> addTextureDuplicateOfOne());
        JButton apply = new JButton("Apply Texture");
        apply.addActionListener(e -> applyTexture());
        JButton clear = new JButton("Clear Texture");
        clear.addActionListener(e -> clearTexture());

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        form.add(addTexture, c);
        c.gridy = 4;
        form.add(apply, c);
        c.gridy = 5;
        form.add(clear, c);

        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBorder(BorderFactory.createTitledBorder("Recent Textures"));
        recentPanel.add(recentTexturePanel, BorderLayout.CENTER);

        top.add(form, BorderLayout.NORTH);
        top.add(recentPanel, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(textureList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildParticlesPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        top.add(new JLabel("Particle:"), c);
        c.gridx = 1;
        top.add(particleIdBox, c);

        c.gridx = 0;
        c.gridy = 1;
        top.add(new JLabel("Priority:"), c);
        c.gridx = 1;
        top.add(particlePriorityField, c);

        JButton add = new JButton("Attach To Selected Face");
        add.addActionListener(e -> addParticle());
        JButton remove = new JButton("Remove Selected");
        remove.addActionListener(e -> removeSelectedParticle());
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        top.add(add, c);
        c.gridy = 3;
        top.add(remove, c);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(emitterList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBillboardsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        top.add(new JLabel("Billboard:"), c);
        c.gridx = 1;
        top.add(billboardIdBox, c);

        c.gridx = 0;
        c.gridy = 1;
        top.add(new JLabel("Skin:"), c);
        c.gridx = 1;
        top.add(billboardSkinField, c);

        c.gridx = 0;
        c.gridy = 2;
        top.add(new JLabel("Distance:"), c);
        c.gridx = 1;
        top.add(billboardDistanceField, c);

        JButton add = new JButton("Attach To Selected Face");
        add.addActionListener(e -> addBillboard());
        JButton remove = new JButton("Remove Selected");
        remove.addActionListener(e -> removeSelectedBillboard());
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        top.add(add, c);
        c.gridy = 4;
        top.add(remove, c);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(billboardList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton save = new JButton("Save Model");
        save.addActionListener(e -> saveModel());
        JButton resetView = new JButton("Reset View");
        resetView.addActionListener(e -> viewer.resetView());
        panel.add(resetView);
        panel.add(save);
        return panel;
    }

    private void loadModelIds() {
        modelListModel.clear();
        for (int id : CacheManager.getModelIds()) {
            modelListModel.addElement(id);
        }
    }

    private void filterModelList() {
        String query = modelSearchField.getText().trim();
        modelListModel.clear();
        for (int id : CacheManager.getModelIds()) {
            if (query.isEmpty() || String.valueOf(id).contains(query)) {
                modelListModel.addElement(id);
            }
        }
        selectModelInList();
    }

    private void loadModel(int nextModelId) {
        Model next = ModelDecoderAdapter.loadDecodedModel(nextModelId);
        if (next == null) {
            throw new IllegalStateException("No model data found for " + nextModelId);
        }
        modelId = nextModelId;
        model = next;
        selectedFace = -1;
        setTitle("Model " + modelId + " Editor");
        refreshViewer();
        refreshLists();
        updateFaceInfo();
        selectModelInList();
    }

    private void selectModelInList() {
        for (int i = 0; i < modelListModel.size(); i++) {
            if (modelListModel.get(i) == modelId) {
                modelList.setSelectedIndex(i);
                modelList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    private void reloadTextureDefinitions() {
        textureDefinitions = TextureDefinitionTable.load();
        textureListModel.clear();
        for (Integer id : textureDefinitions.presentIds()) {
            textureListModel.addElement(id);
        }
        if (!textureListModel.isEmpty()) {
            textureList.setSelectedIndex(0);
        }
    }

    private void loadDefinitionChoices(JComboBox<DefinitionChoice> box, int index, String label) {
        box.removeAllItems();
        Map<Integer, DefinitionChoice> choices = new LinkedHashMap<>();
        for (int archiveId : CacheManager.getArchiveIds(index)) {
            int[] fileIds = CacheManager.getFileIds(index, archiveId);
            if (fileIds.length > 1) {
                for (int fileId : fileIds) {
                    choices.putIfAbsent(fileId, new DefinitionChoice(fileId, label + " " + fileId));
                }
            } else {
                choices.putIfAbsent(archiveId, new DefinitionChoice(archiveId, label + " " + archiveId));
            }
        }
        for (DefinitionChoice choice : choices.values()) {
            box.addItem(choice);
        }
        box.setEnabled(box.getItemCount() > 0);
    }

    private void maybeShowTextureMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int row = textureList.locationToIndex(e.getPoint());
        if (row < 0) {
            return;
        }
        textureList.setSelectedIndex(row);
        Integer textureId = textureList.getSelectedValue();
        if (textureId == null) {
            return;
        }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem replace = new JMenuItem("Replace");
        replace.addActionListener(a -> replaceTexture(textureId));
        JMenuItem remove = new JMenuItem("Remove");
        remove.addActionListener(a -> removeTexture(textureId));
        menu.add(replace);
        menu.add(remove);
        menu.show(textureList, e.getX(), e.getY());
    }

    private void replaceTexture(int textureId) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Replace Texture " + textureId);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Replace texture " + textureId + "?", "Replace Texture",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) {
                throw new IOException("Unsupported image.");
            }
            Path dir = Paths.get("dumps", "textures");
            Files.createDirectories(dir);
            ImageIO.write(image, "png", dir.resolve(textureId + ".png").toFile());
            TextureLoader.clearCache();
            textureList.repaint();
            updateRecentTextureSwatches();
            refreshViewer();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to replace texture: " + ex.getMessage(), "Texture",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeTexture(int textureId) {
        if (JOptionPane.showConfirmDialog(this, "Remove texture " + textureId + "?", "Remove Texture",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        textureDefinitions.remove(textureId);
        textureDefinitions.save();
        try {
            Files.deleteIfExists(Paths.get("dumps", "textures", textureId + ".png"));
        } catch (IOException ignored) {
        }
        TextureLoader.clearCache();
        reloadTextureDefinitions();
        updateRecentTextureSwatches();
        refreshViewer();
    }

    private void addTextureDuplicateOfOne() {
        int newId = textureDefinitions.addDefault();
        textureDefinitions.save();
        Path source = Paths.get("dumps", "textures", "1.png");
        Path target = Paths.get("dumps", "textures", newId + ".png");
        try {
            Files.createDirectories(target.getParent());
            if (Files.exists(source)) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to duplicate texture 1: " + ex.getMessage(), "Texture",
                    JOptionPane.ERROR_MESSAGE);
        }
        TextureLoader.clearCache();
        reloadTextureDefinitions();
        textureList.setSelectedValue(newId, true);
        textureIdField.setText(String.valueOf(newId));
    }

    private void applySelectedTool() {
        switch (editorTabs.getSelectedIndex()) {
            case 0:
                applyColor();
                break;
            case 1:
                applyTexture();
                break;
            case 2:
                addParticle();
                break;
            case 3:
                addBillboard();
                break;
            default:
                break;
        }
    }

    private void openColorPicker() {
        Color chosen = JColorChooser.showDialog(this, "Pick Face Color",
                CacheColor.toColor(parseNumber(colorField.getText(), 0)));
        if (chosen != null) {
            colorField.setText(String.valueOf(CacheColor.fromColor(chosen)));
            refreshColorPreview();
        }
    }

    private void refreshColorPreview() {
        colorPreview.setBackground(CacheColor.toColor(parseNumber(colorField.getText(), 0)));
    }

    private void applyColor() {
        if (!ensureFaceSelected()) {
            return;
        }
        model.setFaceColor(selectedFace, parseNumber(colorField.getText(), 0));
        refreshViewer();
        updateFaceInfo();
    }

    private void applyTexture() {
        if (!ensureFaceSelected()) {
            return;
        }
        int textureId = parseNumber(textureIdField.getText(), -1);
        model.setFaceTexture(selectedFace, textureId);
        model.setFaceTextureDirectionAndSpeed(selectedFace,
                parseNumber(textureDirectionField.getText(), 0),
                parseNumber(textureSpeedField.getText(), 0));
        rememberTexture(textureId);
        refreshViewer();
        updateFaceInfo();
    }

    private void clearTexture() {
        if (!ensureFaceSelected()) {
            return;
        }
        model.removeFaceTexture(selectedFace);
        refreshViewer();
        updateFaceInfo();
    }

    private void rememberTexture(int textureId) {
        if (textureId < 0) {
            return;
        }
        RECENT_TEXTURES.remove((Integer) textureId);
        RECENT_TEXTURES.addFirst(textureId);
        while (RECENT_TEXTURES.size() > MAX_RECENT_TEXTURES) {
            RECENT_TEXTURES.removeLast();
        }
        updateRecentTextureSwatches();
    }

    private void updateRecentTextureSwatches() {
        recentTexturePanel.removeAll();
        for (Integer textureId : RECENT_TEXTURES) {
            JButton swatch = new JButton();
            swatch.setPreferredSize(new Dimension(42, 42));
            swatch.setToolTipText("Texture " + textureId);
            BufferedImage image = TextureLoader.loadTexture(textureId);
            if (image != null) {
                swatch.setIcon(new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_FAST)));
            } else {
                swatch.setText(String.valueOf(textureId));
            }
            swatch.addActionListener(e -> textureIdField.setText(String.valueOf(textureId)));
            recentTexturePanel.add(swatch);
        }
        recentTexturePanel.revalidate();
        recentTexturePanel.repaint();
    }

    private void addParticle() {
        if (!ensureFaceSelected() || !particleIdBox.isEnabled()) {
            return;
        }
        DefinitionChoice selected = (DefinitionChoice) particleIdBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        List<EmissiveTriangle> emitters = new ArrayList<>();
        if (model.getEmitters() != null) {
            for (EmissiveTriangle emitter : model.getEmitters()) {
                emitters.add(emitter);
            }
        }
        emitters.add(new EmissiveTriangle(selected.id, selectedFace,
                model.getTriangleVertex1()[selectedFace],
                model.getTriangleVertex2()[selectedFace],
                model.getTriangleVertex3()[selectedFace],
                parseNumber(particlePriorityField.getText(), 0)));
        model.replaceEmitters(emitters.toArray(new EmissiveTriangle[0]));
        refreshLists();
    }

    private void removeSelectedParticle() {
        int index = emitterList.getSelectedIndex();
        if (index < 0 || model.getEmitters() == null) {
            return;
        }
        List<EmissiveTriangle> emitters = new ArrayList<>();
        for (int i = 0; i < model.getEmitters().length; i++) {
            if (i != index) {
                emitters.add(model.getEmitters()[i]);
            }
        }
        model.replaceEmitters(emitters.isEmpty() ? null : emitters.toArray(new EmissiveTriangle[0]));
        refreshLists();
    }

    private void addBillboard() {
        if (!ensureFaceSelected() || !billboardIdBox.isEnabled()) {
            return;
        }
        DefinitionChoice selected = (DefinitionChoice) billboardIdBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        List<FaceBillboard> billboards = new ArrayList<>();
        if (model.getFaceBillboards() != null) {
            for (FaceBillboard billboard : model.getFaceBillboards()) {
                billboards.add(billboard);
            }
        }
        billboards.add(new FaceBillboard(selected.id, selectedFace,
                parseNumber(billboardSkinField.getText(), 0),
                parseNumber(billboardDistanceField.getText(), 0)));
        model.replaceBillboards(billboards.toArray(new FaceBillboard[0]));
        refreshLists();
    }

    private void removeSelectedBillboard() {
        int index = billboardList.getSelectedIndex();
        if (index < 0 || model.getFaceBillboards() == null) {
            return;
        }
        List<FaceBillboard> billboards = new ArrayList<>();
        for (int i = 0; i < model.getFaceBillboards().length; i++) {
            if (i != index) {
                billboards.add(model.getFaceBillboards()[i]);
            }
        }
        model.replaceBillboards(billboards.isEmpty() ? null : billboards.toArray(new FaceBillboard[0]));
        refreshLists();
    }

    private void refreshViewer() {
        viewer.setModel(buildRenderModel());
        viewer.setSelectedFace(selectedFace);
    }

    private RenderModel buildRenderModel() {
        return ModelDecoderAdapter.fromDecodedModel(modelId, model);
    }

    private void refreshLists() {
        emitterListModel.clear();
        if (model.getEmitters() != null) {
            for (EmissiveTriangle emitter : model.getEmitters()) {
                emitterListModel.addElement("id=" + emitter.getEmitter() + " face=" + emitter.getFace() + " priority=" + emitter.getPriority());
            }
        }
        billboardListModel.clear();
        if (model.getFaceBillboards() != null) {
            for (FaceBillboard billboard : model.getFaceBillboards()) {
                billboardListModel.addElement("id=" + billboard.getId() + " face=" + billboard.getFace()
                        + " skin=" + billboard.getSkin() + " dist=" + billboard.getDistance());
            }
        }
    }

    private void updateFaceInfo() {
        if (selectedFace < 0 || selectedFace >= model.getTriangleCount()) {
            faceInfo.setText("Model " + modelId + " | No face selected");
            refreshColorPreview();
            return;
        }
        int colour = model.getTriangleColors()[selectedFace] & 0xFFFF;
        int texture = model.getTriangleTextures() != null && selectedFace < model.getTriangleTextures().length
                ? model.getTriangleTextures()[selectedFace] : -1;
        int direction = 0;
        int speed = 0;
        if (model.getTextureCoordinates() != null && selectedFace < model.getTextureCoordinates().length) {
            int coordinate = model.getTextureCoordinates()[selectedFace];
            if (coordinate >= 0) {
                if (model.getTextureDirection() != null && coordinate < model.getTextureDirection().length) {
                    direction = model.getTextureDirection()[coordinate];
                }
                if (model.getTextureSpeed() != null && coordinate < model.getTextureSpeed().length) {
                    speed = model.getTextureSpeed()[coordinate];
                }
            }
        }
        faceInfo.setText("Model " + modelId + " | Face " + selectedFace + " | color=" + colour + " | texture=" + texture
                + " | dir=" + direction + " | speed=" + speed);
        colorField.setText(String.valueOf(colour));
        if (texture >= 0) {
            textureIdField.setText(String.valueOf(texture));
        }
        textureDirectionField.setText(String.valueOf(direction));
        textureSpeedField.setText(String.valueOf(speed));
        refreshColorPreview();
    }

    private boolean ensureFaceSelected() {
        if (selectedFace >= 0 && selectedFace < model.getTriangleCount()) {
            return true;
        }
        JOptionPane.showMessageDialog(this, "Select a face first.");
        return false;
    }

    private int parseNumber(String text, int fallback) {
        try {
            return Integer.decode(text.trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private void saveModel() {
        ByteBuf buffer = model.encode();
        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);
        CacheManager.writeModelData(modelId, data);
        ModelDecoderAdapter.invalidateModel(modelId);
        if (onSave != null) {
            onSave.run();
        }
        JOptionPane.showMessageDialog(this, "Saved model " + modelId + ".");
    }

    private static final class TextureCellRenderer extends DefaultListCellRenderer {

        @Override

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Integer) {
                int textureId = (Integer) value;
                label.setText("Texture " + textureId);
                BufferedImage image = TextureLoader.loadTexture(textureId);
                if (image != null) {
                    label.setIcon(new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_FAST)));
                } else {
                    label.setIcon(null);
                }
            }
            label.setHorizontalTextPosition(SwingConstants.RIGHT);
            label.setIconTextGap(8);
            return label;
        }
    }

    private static final class DefinitionChoice {

        private final int id;

        private final String label;

        private DefinitionChoice(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override

        public String toString() {
            return label;
        }
    }

    private static final class DefinitionChoiceRenderer extends DefaultListCellRenderer {

        private final String marker;

        private DefinitionChoiceRenderer(String marker) {
            this.marker = marker;
        }

        @Override

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DefinitionChoice) {
                DefinitionChoice choice = (DefinitionChoice) value;
                label.setText(choice.label);
                label.setIcon(new ImageIcon(previewIcon(choice.id, marker)));
            }
            label.setHorizontalTextPosition(SwingConstants.RIGHT);
            label.setIconTextGap(8);
            return label;
        }

        private BufferedImage previewIcon(int id, String marker) {
            BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            int hue = Math.floorMod(id * 37, 360);
            g.setColor(Color.getHSBColor(hue / 360f, 0.45f, 0.95f));
            g.fillRoundRect(0, 0, 31, 31, 8, 8);
            g.setColor(new Color(0, 0, 0, 90));
            g.drawRoundRect(0, 0, 31, 31, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            int x = (32 - fm.stringWidth(marker)) / 2;
            int y = (32 + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(marker, x, y);
            g.dispose();
            return image;
        }
    }
}
