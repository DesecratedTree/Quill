package com.desecratedtree.quill.ui.item;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.codec.item.ItemSaver;
import com.desecratedtree.quill.ui.component.ColorMappingsPanel;
import com.desecratedtree.quill.ui.component.SimpleDocumentListener;
import com.desecratedtree.quill.ui.ModelEditorDialog;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.ModelExporter;
import com.desecratedtree.quill.render.ModelFileFormat;
import com.desecratedtree.quill.render.ModelViewerPanel;
import com.desecratedtree.quill.render.RenderModel;
import com.desecratedtree.quill.render.SoftwareModelRenderer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

public class ItemEditorPanel extends JPanel {

    private ItemDefinitions current;

    private final JButton saveButton = new JButton("Save");

    private JTextField name = new JTextField();

    private JTextField value = new JTextField();

    private JComboBox<String> equipSlot = new JComboBox<>();

    private JCheckBox stackable = new JCheckBox("Stackable");

    private JCheckBox members = new JCheckBox("Members");

    private JButton editParams = new JButton("Edit Params");

    private JButton dumpDefs = new JButton("Dump Defs");

    private JTextArea inventoryOptionsArea = new JTextArea(5, 15);

    private JTextArea groundOptionsArea = new JTextArea(5, 15);

    private JTextField modelId = new JTextField();

    private JTextField modelZoom = new JTextField();

    private JTextField rotationX = new JTextField();

    private JTextField rotationY = new JTextField();

    private JTextField offsetX = new JTextField();

    private JTextField offsetY = new JTextField();

    private JTextField maleEquip1 = new JTextField();

    private JTextField maleEquip2 = new JTextField();

    private JTextField maleEquip3 = new JTextField();

    private JTextField femaleEquip1 = new JTextField();

    private JTextField femaleEquip2 = new JTextField();

    private JTextField femaleEquip3 = new JTextField();

    private final JLabel spriteLabel = new JLabel("", SwingConstants.CENTER);

    private final JLabel stackPreviewLabel = new JLabel("Stack: 1", SwingConstants.CENTER);

    private final JButton prevStackBtn = new JButton("<");

    private final JButton nextStackBtn = new JButton(">");

    private int currentStackIndex = -1;

    private final List<JTextField> stackAmountFields = new ArrayList<>();

    private final List<JTextField> stackModelFields = new ArrayList<>();

    private final List<JButton> modelDataButtons = new ArrayList<>();

    private final List<JTextField> modelDataFields = new ArrayList<>();

    private IntConsumer saveListener;

    private final ColorMappingsPanel colorMappingsPanel = new ColorMappingsPanel();

    private static final String[] SLOT_NAMES = new String[15];
    static {
        SLOT_NAMES[0] = "Helmet";
        SLOT_NAMES[1] = "Cape";
        SLOT_NAMES[2] = "Amulet";
        SLOT_NAMES[3] = "Weapon";
        SLOT_NAMES[4] = "Chest";
        SLOT_NAMES[5] = "Off-hand / Shield";
        SLOT_NAMES[7] = "Legs";
        SLOT_NAMES[9] = "Hands";
        SLOT_NAMES[10] = "Feet";
        SLOT_NAMES[12] = "Ring";
        SLOT_NAMES[13] = "Arrows";
        SLOT_NAMES[14] = "Aura";
    }

    public ItemEditorPanel() {
        setLayout(new BorderLayout());
        add(createEditorPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        saveButton.addActionListener(e -> save());
        installPreviewListeners();
    }

    public void setSaveListener(IntConsumer saveListener) {
        this.saveListener = saveListener;
    }

    private static class ParamRow {
        JComboBox<String> keyBox;
        JTextField valueField;
        JButton removeBtn;
        int lastKey = -1;
    }

    private static final HashMap<Integer, String> PARAM_PRESETS = new HashMap<>();
    static {
        PARAM_PRESETS.put(0, "Stab Attack");
        PARAM_PRESETS.put(1, "Slash Attack");
        PARAM_PRESETS.put(2, "Crush Attack");
        PARAM_PRESETS.put(3, "Magic Attack");
        PARAM_PRESETS.put(4, "Ranged Attack");
        PARAM_PRESETS.put(5, "Stab Defence");
        PARAM_PRESETS.put(6, "Slash Defence");
        PARAM_PRESETS.put(7, "Crush Defence");
        PARAM_PRESETS.put(8, "Magic Defence");
        PARAM_PRESETS.put(9, "Ranged Defence");
        PARAM_PRESETS.put(11, "Prayer Bonus");
        PARAM_PRESETS.put(417, "Summoning Defence");
        PARAM_PRESETS.put(641, "Strength Bonus (/10)");
        PARAM_PRESETS.put(643, "Ranged Strength Bonus (/10)");
        PARAM_PRESETS.put(644, "Render Anim ID");
        PARAM_PRESETS.put(685, "Magic Damage");
        PARAM_PRESETS.put(967, "Absorb Melee");
        PARAM_PRESETS.put(968, "Absorb Range");
        PARAM_PRESETS.put(969, "Absorb Mage");
        PARAM_PRESETS.put(14, "Attack Speed");
        PARAM_PRESETS.put(686, "Special Bar Key");
    }

    private JScrollPane createEditorPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JPanel basicWrapper = new JPanel(new BorderLayout(8, 0));
        basicWrapper.setBorder(BorderFactory.createTitledBorder("Basic Info"));
        JPanel inventorySpritePanel = new JPanel();
        inventorySpritePanel.setLayout(new BoxLayout(inventorySpritePanel, BoxLayout.Y_AXIS));
        inventorySpritePanel.setPreferredSize(new Dimension(132, 170));
        inventorySpritePanel.setMinimumSize(new Dimension(132, 170));
        spriteLabel.setPreferredSize(new Dimension(108, 96));
        spriteLabel.setMaximumSize(new Dimension(132, 108));
        spriteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inventorySpritePanel.add(spriteLabel);
        stackPreviewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel stackNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        prevStackBtn.setMargin(new Insets(1, 4, 1, 4));
        nextStackBtn.setMargin(new Insets(1, 4, 1, 4));
        stackNav.add(prevStackBtn);
        stackNav.add(stackPreviewLabel);
        stackNav.add(nextStackBtn);
        prevStackBtn.setEnabled(false);
        nextStackBtn.setEnabled(false);
        prevStackBtn.addActionListener(e -> cycleStackPreview(-1));
        nextStackBtn.addActionListener(e -> cycleStackPreview(1));
        inventorySpritePanel.add(stackNav);
        JButton viewStackModel = new JButton("Edit Stack Defs");
        viewStackModel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewStackModel.setMargin(new Insets(1, 6, 1, 6));
        viewStackModel.addActionListener(e -> openStackModelViewer());
        inventorySpritePanel.add(viewStackModel);
        JPanel basicPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bgc = new GridBagConstraints();
        bgc.insets = new Insets(2, 2, 2, 2);
        bgc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        bgc.gridx = 0; bgc.gridy = row;
        basicPanel.add(new JLabel("Name:"), bgc);
        bgc.gridx = 1; bgc.weightx = 1.0;
        basicPanel.add(name, bgc);
        row++;
        bgc.gridx = 0; bgc.gridy = row; bgc.weightx = 0;
        basicPanel.add(new JLabel("Value:"), bgc);
        bgc.gridx = 1; bgc.weightx = 1.0;
        basicPanel.add(value, bgc);
        row++;
        bgc.gridx = 0; bgc.gridy = row;
        basicPanel.add(new JLabel("Equip Slot:"), bgc);
        JPanel equipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        equipRow.add(equipSlot);
        equipRow.add(stackable);
        equipRow.add(members);
        equipRow.add(editParams);
        equipRow.add(dumpDefs);
        editParams.addActionListener(e -> openParamsDialog());
        dumpDefs.addActionListener(e -> openDefsDumpDialog());
        bgc.gridx = 1;
        basicPanel.add(equipRow, bgc);
        basicWrapper.add(inventorySpritePanel, BorderLayout.WEST);
        basicWrapper.add(basicPanel, BorderLayout.CENTER);
        gbc.gridy++;
        mainPanel.add(basicWrapper, gbc);
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        GridBagConstraints ogc = new GridBagConstraints();
        ogc.insets = new Insets(2, 2, 2, 2);
        ogc.anchor = GridBagConstraints.NORTHWEST;
        JPanel invPanel = new JPanel(new BorderLayout(2, 2));
        invPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2), "Inventory"));
        inventoryOptionsArea.setLineWrap(true);
        inventoryOptionsArea.setWrapStyleWord(true);
        JScrollPane invScroll = new JScrollPane(inventoryOptionsArea);
        invScroll.setPreferredSize(new Dimension(140, 90));
        invPanel.add(invScroll, BorderLayout.CENTER);
        JPanel groundPanel = new JPanel(new BorderLayout(2, 2));
        groundPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2), "Ground"));
        groundOptionsArea.setLineWrap(true);
        groundOptionsArea.setWrapStyleWord(true);
        JScrollPane groundScroll = new JScrollPane(groundOptionsArea);
        groundScroll.setPreferredSize(new Dimension(140, 90));
        groundPanel.add(groundScroll, BorderLayout.CENTER);
        ogc.gridx = 0;
        optionsPanel.add(invPanel, ogc);
        ogc.gridx = 1;
        optionsPanel.add(groundPanel, ogc);
        gbc.gridy++;
        mainPanel.add(optionsPanel, gbc);
        JPanel modelPanel = new JPanel(new GridBagLayout());
        modelPanel.setBorder(BorderFactory.createTitledBorder("Model"));
        GridBagConstraints mg = new GridBagConstraints();
        mg.insets = new Insets(2, 2, 2, 2);
        mg.fill = GridBagConstraints.HORIZONTAL;
        addModelIdField(modelPanel, "Inventory Model:", modelId, mg, 0, 0);
        addModelField(modelPanel, "Model Zoom:", modelZoom, mg, 0, 1);
        addModelField(modelPanel, "Rotation X:", rotationX, mg, 0, 2);
        addModelField(modelPanel, "Rotation Y:", rotationY, mg, 0, 3);
        addModelField(modelPanel, "Offset X:", offsetX, mg, 0, 4);
        addModelField(modelPanel, "Offset Y:", offsetY, mg, 0, 5);
        addModelIdField(modelPanel, "Male Equip 1:", maleEquip1, mg, 1, 0);
        addModelIdField(modelPanel, "Male Equip 2:", maleEquip2, mg, 1, 1);
        addModelIdField(modelPanel, "Male Equip 3:", maleEquip3, mg, 1, 2);
        addModelIdField(modelPanel, "Female Equip 1:", femaleEquip1, mg, 1, 3);
        addModelIdField(modelPanel, "Female Equip 2:", femaleEquip2, mg, 1, 4);
        addModelIdField(modelPanel, "Female Equip 3:", femaleEquip3, mg, 1, 5);
        gbc.gridy++;
        mainPanel.add(modelPanel, gbc);
        JPanel stackPanel = new JPanel(new BorderLayout(4, 4));
        stackPanel.setBorder(BorderFactory.createTitledBorder("Stack Variants"));
        JPanel stackRowsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints sr = new GridBagConstraints();
        sr.insets = new Insets(1, 2, 1, 2);
        sr.fill = GridBagConstraints.HORIZONTAL;
        sr.gridy = 0;
        sr.gridx = 0; sr.weightx = 0; stackRowsPanel.add(new JLabel("#", SwingConstants.CENTER), sr);
        sr.gridx = 1; sr.weightx = 0.5; stackRowsPanel.add(new JLabel("Amount"), sr);
        sr.gridx = 2; sr.weightx = 0.5; stackRowsPanel.add(new JLabel("Item ID"), sr);
        for (int i = 0; i < 10; i++) {
            sr.gridy = i + 1;
            sr.gridx = 0; sr.weightx = 0;
            JLabel idxLabel = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            idxLabel.setPreferredSize(new Dimension(24, 24));
            stackRowsPanel.add(idxLabel, sr);
            sr.gridx = 1; sr.weightx = 0.5;
            JTextField amtField = new JTextField(6);
            stackAmountFields.add(amtField);
            stackRowsPanel.add(amtField, sr);
            sr.gridx = 2; sr.weightx = 0.5;
            JTextField modelField = new JTextField(8);
            stackModelFields.add(modelField);
            stackRowsPanel.add(modelField, sr);
        }
        stackPanel.add(stackRowsPanel, BorderLayout.WEST);
        gbc.gridy++;
        mainPanel.add(stackPanel, gbc);
        JPanel colorsPanel = new JPanel(new BorderLayout());
        colorsPanel.setBorder(BorderFactory.createTitledBorder("Colors"));
        colorsPanel.add(colorMappingsPanel, BorderLayout.CENTER);
        colorMappingsPanel.setPreferredSize(new Dimension(0, 150));
        colorMappingsPanel.setChangeListener(this::updatePreview);
        gbc.gridy++;
        mainPanel.add(colorsPanel, gbc);
        equipSlot.addItem("(None)");
        for (String s : SLOT_NAMES) {
            if (s != null) equipSlot.addItem(s);
        }
        return scrollPane;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.add(saveButton);
        return footer;
    }

    private void addModelField(JPanel panel, String label, JTextField field,
                               GridBagConstraints mg, int col, int row) {
        mg.gridx = col * 2;
        mg.gridy = row;
        mg.anchor = GridBagConstraints.EAST;
        mg.weightx = 0;
        panel.add(new JLabel(label), mg);
        mg.gridx = col * 2 + 1;
        mg.anchor = GridBagConstraints.WEST;
        mg.weightx = 1;
        panel.add(field, mg);
    }

    private void addModelIdField(JPanel panel, String label, JTextField field,
                                 GridBagConstraints mg, int col, int row) {
        mg.gridx = col * 2;
        mg.gridy = row;
        mg.anchor = GridBagConstraints.EAST;
        mg.weightx = 0;
        panel.add(new JLabel(label), mg);
        JPanel rowPanel = new JPanel(new BorderLayout(4, 0));
        rowPanel.add(field, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        JButton view = new JButton("View");
        JButton edit = new JButton("Edit");
        JButton save = new JButton("Save");
        JButton replace = new JButton("Replace");
        view.setMargin(new Insets(1, 6, 1, 6));
        edit.setMargin(new Insets(1, 6, 1, 6));
        save.setMargin(new Insets(1, 6, 1, 6));
        replace.setMargin(new Insets(1, 6, 1, 6));
        view.addActionListener(e -> openModelViewer(label, field));
        edit.addActionListener(e -> openModelEditor(label, field));
        save.addActionListener(e -> saveModelData(field));
        replace.addActionListener(e -> replaceModelData(field));
        buttons.add(view);
        buttons.add(edit);
        buttons.add(save);
        buttons.add(replace);
        rowPanel.add(buttons, BorderLayout.EAST);
        modelDataButtons.add(view);
        modelDataButtons.add(edit);
        modelDataButtons.add(save);
        modelDataButtons.add(replace);
        modelDataFields.add(field);
        mg.gridx = col * 2 + 1;
        mg.anchor = GridBagConstraints.WEST;
        mg.weightx = 1;
        panel.add(rowPanel, mg);
    }

    private Integer parseModelId(JTextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hasUsableModelId(JTextField field) {
        Integer id = parseModelId(field);
        return id != null && id != -1;
    }

    private void updateModelDataButtons() {
        for (int fieldIndex = 0; fieldIndex < modelDataFields.size(); fieldIndex++) {
            JTextField field = modelDataFields.get(fieldIndex);
            boolean enabled = hasUsableModelId(field);
            int buttonIndex = fieldIndex * 4;
            modelDataButtons.get(buttonIndex).setEnabled(enabled);
            modelDataButtons.get(buttonIndex + 1).setEnabled(enabled);
            modelDataButtons.get(buttonIndex + 2).setEnabled(enabled);
            modelDataButtons.get(buttonIndex + 3).setEnabled(enabled);
        }
    }

    private void openModelViewer(String label, JTextField field) {
        Integer id = parseModelId(field);
        if (id == null || id == -1) return;
        RenderModel renderModel = field == modelId
                ? ModelDecoderAdapter.loadItemModel(buildInventorySpriteItem(buildPreviewItem()))
                : ModelDecoderAdapter.loadModel(id);
        if (renderModel == null) {
            JOptionPane.showMessageDialog(this, "No data found for model " + id + ".");
            return;
        }
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                label.replace(":", "") + " " + id,
                Dialog.ModalityType.MODELESS
        );
        ModelViewerPanel viewer = new ModelViewerPanel();
        viewer.setPreferredSize(new Dimension(360, 260));
        viewer.setModel(renderModel);
        JButton resetView = new JButton("Reset View");
        resetView.addActionListener(e -> viewer.resetView());
        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        content.add(viewer, BorderLayout.CENTER);
        content.add(resetView, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openModelEditor(String label, JTextField field) {
        Integer id = parseModelId(field);
        if (id == null || id == -1) return;
        ModelEditorDialog.showEditor(this, id, label.replace(":", "") + " " + id + " Editor", this::updatePreview);
    }

    private void saveModelData(JTextField field) {
        Integer id = parseModelId(field);
        if (id == null || id == -1) return;
        byte[] data = CacheManager.getModelData(id);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "No data found for model " + id + ".");
            return;
        }
        JComboBox<ModelFileFormat> fileTypeBox = new JComboBox<>(ModelFileFormat.values());
        JPanel options = new JPanel(new GridLayout(0, 2, 6, 6));
        options.add(new JLabel("File type:"));
        options.add(fileTypeBox);
        if (JOptionPane.showConfirmDialog(
                this,
                options,
                "Save Model",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }
        ModelFileFormat fileType = (ModelFileFormat) fileTypeBox.getSelectedItem();
        if (fileType == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Model");
        chooser.setSelectedFile(new File(id + fileType.extension()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(), ModelExporter.export(id, data, fileType));
            JOptionPane.showMessageDialog(this, "Saved model " + id + " as " + fileType + ".");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save model: " + ex.getMessage(),
                    "Save Model", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Failed to convert model: " + ex.getMessage(),
                    "Save Model", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void replaceModelData(JTextField field) {
        Integer id = parseModelId(field);
        if (id == null || id == -1) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Replace Model " + id);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            byte[] data = Files.readAllBytes(chooser.getSelectedFile().toPath());
            CacheManager.writeModelData(id, data);
            ModelDecoderAdapter.invalidateModel(id);
            updatePreview();
            JOptionPane.showMessageDialog(this, "Replaced model " + id + ".");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to replace model: " + ex.getMessage(),
                    "Replace Model", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseIntSafe(JTextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String capitalizeFirstLetter(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String formatOptionsForDisplay(String[] options) {
        if (options == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            if (opt == null || opt.trim().isEmpty()) {
                sb.append("(None)");
            } else {
                sb.append(capitalizeFirstLetter(opt.trim()));
            }
            if (i < options.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void loadItem(int id) {
        current = ItemDefinitions.getItemDefinitions(id);
        if (current == null) return;
        name.setText(current.name);
        value.setText(String.valueOf(current.value));
        stackable.setSelected(current.stackable == 1);
        members.setSelected(current.membersOnly);
        inventoryOptionsArea.setText(formatOptionsForDisplay(current.inventoryOptions));
        groundOptionsArea.setText(formatOptionsForDisplay(current.groundOptions));
        equipSlot.setEnabled(true);
        if (current.equipSlot >= 0 &&
                current.equipSlot < SLOT_NAMES.length &&
                SLOT_NAMES[current.equipSlot] != null) {
            equipSlot.setSelectedItem(SLOT_NAMES[current.equipSlot]);
        } else {
            equipSlot.setSelectedItem("(None)");
            equipSlot.setEnabled(false);
        }
        modelId.setText(String.valueOf(current.modelId));
        modelZoom.setText(String.valueOf(current.modelZoom));
        rotationX.setText(String.valueOf(current.modelRotation1));
        rotationY.setText(String.valueOf(current.modelRotation2));
        offsetX.setText(String.valueOf(current.modelOffset1));
        offsetY.setText(String.valueOf(current.modelOffset2));
        maleEquip1.setText(String.valueOf(current.maleEquip1));
        maleEquip2.setText(String.valueOf(current.maleEquip2));
        maleEquip3.setText(String.valueOf(current.maleEquipModelId3));
        femaleEquip1.setText(String.valueOf(current.femaleEquip1));
        femaleEquip2.setText(String.valueOf(current.femaleEquip2));
        femaleEquip3.setText(String.valueOf(current.femaleEquipModelId3));
        colorMappingsPanel.setMappings(current.originalModelColors, current.modifiedModelColors);
        loadStackVariants();
        updateModelDataButtons();
        updatePreview();
    }

    private void loadStackVariants() {
        boolean hasVariants = current.stackIds != null;
        currentStackIndex = hasVariants ? 0 : -1;
        for (int i = 0; i < 10; i++) {
            int id = current.stackIds != null && i < current.stackIds.length ? current.stackIds[i] : 0;
            int amt = current.stackAmounts != null && i < current.stackAmounts.length ? current.stackAmounts[i] : 0;
            stackModelFields.get(i).setText(id > 0 ? String.valueOf(id) : "");
            stackAmountFields.get(i).setText(amt > 0 ? String.valueOf(amt) : "");
        }
        prevStackBtn.setEnabled(hasVariants);
        nextStackBtn.setEnabled(hasVariants);
        stackPreviewLabel.setText("Stack: 1");
    }

    private void save() {
        if (current == null) return;
        current.name = name.getText();
        current.value = parseIntSafe(value);
        current.stackable = stackable.isSelected() ? 1 : 0;
        current.membersOnly = members.isSelected();
        current.inventoryOptions = Arrays.stream(inventoryOptionsArea.getText().split("\n"))
                .map(s -> s.equalsIgnoreCase("(None)") ? "" : capitalizeFirstLetter(s.toLowerCase()))
                .toArray(String[]::new);
        current.groundOptions = Arrays.stream(groundOptionsArea.getText().split("\n"))
                .map(s -> s.equalsIgnoreCase("(None)") ? "" : capitalizeFirstLetter(s.toLowerCase()))
                .toArray(String[]::new);
        String selectedSlot = (String) equipSlot.getSelectedItem();
        current.equipSlot = -1;
        for (int i = 0; i < SLOT_NAMES.length; i++) {
            if (SLOT_NAMES[i] != null && SLOT_NAMES[i].equals(selectedSlot)) {
                current.equipSlot = i;
                break;
            }
        }
        current.modelId = parseIntSafe(modelId);
        current.modelZoom = parseIntSafe(modelZoom);
        current.modelRotation1 = parseIntSafe(rotationX);
        current.modelRotation2 = parseIntSafe(rotationY);
        current.modelOffset1 = parseIntSafe(offsetX);
        current.modelOffset2 = parseIntSafe(offsetY);
        current.maleEquip1 = parseIntSafe(maleEquip1);
        current.maleEquip2 = parseIntSafe(maleEquip2);
        current.maleEquipModelId3 = parseIntSafe(maleEquip3);
        current.femaleEquip1 = parseIntSafe(femaleEquip1);
        current.femaleEquip2 = parseIntSafe(femaleEquip2);
        current.femaleEquipModelId3 = parseIntSafe(femaleEquip3);
        current.originalModelColors = colorMappingsPanel.originalColors();
        current.modifiedModelColors = colorMappingsPanel.modifiedColors();
        int stackCount = 0;
        for (int i = 0; i < 10; i++) {
            if (!stackModelFields.get(i).getText().trim().isEmpty()) stackCount = i + 1;
        }
        if (stackCount > 0) {
            current.stackIds = new int[stackCount];
            current.stackAmounts = new int[stackCount];
            for (int i = 0; i < stackCount; i++) {
                current.stackIds[i] = parseIntSafe(stackModelFields.get(i));
                current.stackAmounts[i] = parseIntSafe(stackAmountFields.get(i));
            }
        } else {
            current.stackIds = null;
            current.stackAmounts = null;
        }
        ItemSaver.save(current);
        int savedId = current.id;
        loadItem(savedId);
        if (saveListener != null) {
            saveListener.accept(savedId);
        }
        JOptionPane.showMessageDialog(this, "Saved!");
    }

    private void installPreviewListeners() {
        DocumentListener listener = new DocumentListener() {

            @Override

            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override

            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override

            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        };
        modelId.getDocument().addDocumentListener(listener);
        modelZoom.getDocument().addDocumentListener(listener);
        rotationX.getDocument().addDocumentListener(listener);
        rotationY.getDocument().addDocumentListener(listener);
        offsetX.getDocument().addDocumentListener(listener);
        offsetY.getDocument().addDocumentListener(listener);
        maleEquip1.getDocument().addDocumentListener(listener);
        maleEquip2.getDocument().addDocumentListener(listener);
        maleEquip3.getDocument().addDocumentListener(listener);
        femaleEquip1.getDocument().addDocumentListener(listener);
        femaleEquip2.getDocument().addDocumentListener(listener);
        femaleEquip3.getDocument().addDocumentListener(listener);
    }

    private void updatePreview() {
        ItemDefinitions preview = buildPreviewItem();
        updateInventorySprite(buildInventorySpriteItem(preview));
        updateModelDataButtons();
    }

    private void cycleStackPreview(int direction) {
        java.util.List<Integer> valid = new java.util.ArrayList<>();
        valid.add(-1);
        for (int i = 0; i < 10; i++) {
            String text = stackModelFields.get(i).getText().trim();
            if (!text.isEmpty()) valid.add(i);
        }
        if (valid.size() == 1) return;
        int idx = valid.indexOf(currentStackIndex);
        idx = (idx + direction + valid.size()) % valid.size();
        currentStackIndex = valid.get(idx);
        previewStackVariant(currentStackIndex);
    }

    private void previewStackVariant(int index) {
        ItemDefinitions preview;
        if (index < 0) {
            stackPreviewLabel.setText("Stack: 1");
            preview = buildPreviewItem();
            if (preview == null) return;
        } else {
            String itemIdText = stackModelFields.get(index).getText().trim();
            String amtText = stackAmountFields.get(index).getText().trim();
            if (itemIdText.isEmpty()) return;
            int stackItemId = parseIntSafe(stackModelFields.get(index));
            if (stackItemId <= 0) return;
            stackPreviewLabel.setText("Stack: x" + (amtText.isEmpty() ? "?" : amtText));
            ItemDefinitions stackDef = ItemDefinitions.getItemDefinitions(stackItemId);
            if (stackDef == null || stackDef.modelId < 0) return;
            preview = new ItemDefinitions(current.id, false);
            preview.modelId = stackDef.modelId;
            preview.modelZoom = stackDef.modelZoom;
            preview.modelRotation1 = stackDef.modelRotation1;
            preview.modelRotation2 = stackDef.modelRotation2;
            preview.modelOffset1 = stackDef.modelOffset1;
            preview.modelOffset2 = stackDef.modelOffset2;
            preview.originalModelColors = stackDef.originalModelColors;
            preview.modifiedModelColors = stackDef.modifiedModelColors;
            preview.originalTextureIds = current.originalTextureIds;
            preview.modifiedTextureIds = current.modifiedTextureIds;
        }
        updateInventorySprite(preview);
    }

    private void openStackModelViewer() {
        java.util.List<Integer> valid = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (!stackModelFields.get(i).getText().trim().isEmpty()) valid.add(i);
        }
        if (valid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No stack variants defined.");
            return;
        }
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Stack Models",
                Dialog.ModalityType.MODELESS
        );
        int cols = Math.min(5, valid.size());
        int rows = (valid.size() + cols - 1) / cols;
        JPanel grid = new JPanel(new GridLayout(rows, cols, 6, 6));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int idx : valid) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            String itemIdText = stackModelFields.get(idx).getText().trim();
            String amtText = stackAmountFields.get(idx).getText().trim();
            JLabel label = new JLabel("x" + (amtText.isEmpty() ? "?" : amtText) + " (item " + itemIdText + ")", SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
            int stackItemId = parseIntSafe(stackModelFields.get(idx));
            JLabel sprite = new JLabel("", SwingConstants.CENTER);
            if (stackItemId > 0) {
                ItemDefinitions stackDef = ItemDefinitions.getItemDefinitions(stackItemId);
                if (stackDef != null && stackDef.modelId >= 0) {
                    ItemDefinitions preview = new ItemDefinitions(current.id, false);
                    preview.modelId = stackDef.modelId;
                    preview.modelZoom = stackDef.modelZoom;
                    preview.modelRotation1 = stackDef.modelRotation1;
                    preview.modelRotation2 = stackDef.modelRotation2;
                    preview.modelOffset1 = stackDef.modelOffset1;
                    preview.modelOffset2 = stackDef.modelOffset2;
                    preview.originalModelColors = stackDef.originalModelColors;
                    preview.modifiedModelColors = stackDef.modifiedModelColors;
                    BufferedImage img = SoftwareModelRenderer.renderInventorySprite(preview, 36, 32);
                    sprite.setIcon(new ImageIcon(img));
                } else {
                    sprite.setText("No def");
                }
            } else {
                sprite.setText("No ID");
            }
            cell.add(sprite, BorderLayout.CENTER);
            cell.add(label, BorderLayout.SOUTH);
            cell.setPreferredSize(new Dimension(100, 100));
            grid.add(cell);
        }
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setPreferredSize(new Dimension(100 * cols + 30, 100 * rows + 30));
        dialog.add(scroll);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateInventorySprite(ItemDefinitions item) {
        if (item == null || item.modelId < 0) {
            spriteLabel.setIcon(null);
            spriteLabel.setText("No item selected");
            return;
        }
        spriteLabel.setText(null);
        BufferedImage sprite = SoftwareModelRenderer.renderInventorySprite(item, 36, 32);
        int targetWidth = Math.max(32, spriteLabel.getWidth() > 0 ? spriteLabel.getWidth() : spriteLabel.getPreferredSize().width);
        int targetHeight = Math.max(32, spriteLabel.getHeight() > 0 ? spriteLabel.getHeight() : spriteLabel.getPreferredSize().height);
        spriteLabel.setIcon(new ImageIcon(scaleSpriteForPreview(sprite, targetWidth, targetHeight)));
    }

    private BufferedImage scaleSpriteForPreview(BufferedImage sprite, int targetWidth, int targetHeight) {
        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int scale = Math.max(1, Math.min(targetWidth / Math.max(1, sprite.getWidth()), targetHeight / Math.max(1, sprite.getHeight())));
        int drawWidth = sprite.getWidth() * scale;
        int drawHeight = sprite.getHeight() * scale;
        int drawX = (targetWidth - drawWidth) / 2;
        int drawY = (targetHeight - drawHeight) / 2;
        graphics.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
        graphics.dispose();
        return canvas;
    }

    private ItemDefinitions buildPreviewItem() {
        if (current == null) {
            return null;
        }
        ItemDefinitions preview = new ItemDefinitions(current.id, false);
        preview.name = current.name;
        preview.modelId = parseIntSafe(modelId);
        preview.modelZoom = parseIntSafe(modelZoom);
        preview.modelRotation1 = parseIntSafe(rotationX);
        preview.modelRotation2 = parseIntSafe(rotationY);
        preview.modelOffset1 = parseIntSafe(offsetX);
        preview.modelOffset2 = parseIntSafe(offsetY);
        preview.unknownInt5 = current.unknownInt5;
        preview.unknownInt7 = current.unknownInt7;
        preview.unknownInt8 = current.unknownInt8;
        preview.unknownInt9 = current.unknownInt9;
        preview.originalModelColors = colorMappingsPanel.originalColors();
        preview.modifiedModelColors = colorMappingsPanel.modifiedColors();
        preview.originalTextureIds = current.originalTextureIds;
        preview.modifiedTextureIds = current.modifiedTextureIds;
        return preview;
    }

    private ItemDefinitions buildInventorySpriteItem(ItemDefinitions preview) {
        if (preview == null || current == null) {
            return preview;
        }
        ItemDefinitions notedPreview = resolveNotedPreviewItem();
        return notedPreview == null ? preview : notedPreview;
    }

    private ItemDefinitions resolveNotedPreviewItem() {
        if (current.certTemplateId == -1 || current.certId == -1 || current.certId == current.id) {
            return null;
        }
        ItemDefinitions realItem = ItemDefinitions.getItemDefinitions(current.certId);
        if (realItem == null || realItem.modelId < 0 || realItem.modelId == 0) {
            return null;
        }
        ItemDefinitions preview = new ItemDefinitions(current.id, false);
        preview.name = current.name;
        preview.modelId = realItem.modelId;
        preview.modelZoom = realItem.modelZoom;
        preview.modelRotation1 = realItem.modelRotation1;
        preview.modelRotation2 = realItem.modelRotation2;
        preview.modelOffset1 = realItem.modelOffset1;
        preview.modelOffset2 = realItem.modelOffset2;
        preview.unknownInt5 = realItem.unknownInt5;
        preview.unknownInt7 = realItem.unknownInt7;
        preview.unknownInt8 = realItem.unknownInt8;
        preview.unknownInt9 = realItem.unknownInt9;
        preview.originalModelColors = realItem.originalModelColors;
        preview.modifiedModelColors = realItem.modifiedModelColors;
        preview.originalTextureIds = realItem.originalTextureIds;
        preview.modifiedTextureIds = realItem.modifiedTextureIds;
        return preview;
    }

    private void openParamsDialog() {
        if (current == null) return;
        if (current.itemParams == null)
            current.itemParams = new HashMap<>();
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Params Editor",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(600, 380);
        dialog.setLayout(new BorderLayout(5, 5));
        JPanel table = new JPanel(new GridBagLayout());
        JScrollPane scroll = new JScrollPane(table);
        java.util.List<ParamRow> rows = new ArrayList<>();
        Runnable rebuild = () -> {
            table.removeAll();
            int y = 0;
            for (ParamRow r : rows) {
                GridBagConstraints c = new GridBagConstraints();
                c.insets = new Insets(2, 2, 2, 2);
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridy = y++;
                c.gridx = 0;
                c.weightx = 0.45;
                table.add(r.keyBox, c);
                c.gridx = 1;
                c.weightx = 0.5;
                table.add(r.valueField, c);
                c.gridx = 2;
                c.weightx = 0;
                table.add(r.removeBtn, c);
            }
            dialog.revalidate();
            dialog.repaint();
        };
        for (Map.Entry<Integer, Object> e : current.itemParams.entrySet()) {
            rows.add(createRow(e.getKey(), e.getValue(), rows, rebuild));
        }
        JButton add = new JButton("+");
        JButton save = new JButton("Save");
        JButton close = new JButton("Close");
        add.addActionListener(e -> {
            rows.add(createRow(-1, "", rows, rebuild));
            rebuild.run();
        });
        save.addActionListener(e -> {
            current.itemParams.clear();
            for (ParamRow r : rows) {
                try {
                    String keyText = r.keyBox.getEditor().getItem().toString().trim();
                    if (keyText.isEmpty()) continue;
                    int key = parseParamKey(keyText);
                    String val = r.valueField.getText().trim();
                    if (val.isEmpty()) continue;
                    Object value;
                    try {
                        value = Integer.parseInt(val);
                    } catch (Exception ex) {
                        value = val;
                    }
                    current.itemParams.put(key, value);
                } catch (Exception ignored) {}
            }
            JOptionPane.showMessageDialog(dialog, "Saved!");
        });
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        bottom.add(add);
        bottom.add(save);
        bottom.add(close);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        rebuild.run();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openDefsDumpDialog() {
        if (current == null) {
            return;
        }
        String dump = currentItemDumpText();
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Item Def Dump " + current.getId(),
                Dialog.ModalityType.MODELESS
        );
        dialog.setLayout(new BorderLayout(6, 6));
        JTextArea textArea = new JTextArea(dump, 28, 72);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JButton copy = new JButton("Copy");
        copy.addActionListener(e -> Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(textArea.getText()), null));
        JButton save = new JButton("Save As " + current.getId() + ".txt");
        save.addActionListener(e -> saveCurrentDefsDump());
        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.add(copy);
        actions.add(save);
        actions.add(close);
        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String currentItemDumpText() {
        if (current == null) {
            return "";
        }
        try {
            return current.dumpOpcodeText();
        } catch (RuntimeException ex) {
            return "item_id: " + current.getId() + "\nerror: " + ex.getMessage();
        }
    }

    private void saveCurrentDefsDump() {
        if (current == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Item Def Dump");
        chooser.setSelectedFile(new File(current.getId() + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(), currentItemDumpText().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save item def dump: " + ex.getMessage(),
                    "Item Editor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ParamRow createRow(int key, Object value,
                               java.util.List<ParamRow> rows,
                               Runnable rebuild) {
        ParamRow r = new ParamRow();
        JComboBox<String> box = new JComboBox<>();
        box.setEditable(true);
        box.setMaximumRowCount(18);
        box.setPreferredSize(new Dimension(180, 22));
        PARAM_PRESETS.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(e -> box.addItem(e.getKey() + " = " + e.getValue()));
        if (key != -1 && PARAM_PRESETS.containsKey(key))
            box.setSelectedItem(key + " = " + PARAM_PRESETS.get(key));
        else if (key != -1)
            box.setSelectedItem(String.valueOf(key));
        JTextField val = new JTextField(value == null ? "" : value.toString());
        val.setPreferredSize(new Dimension(80, 22));
        JButton remove = new JButton("x");
        remove.setMargin(new Insets(0, 4, 0, 4));
        remove.addActionListener(e -> {
            rows.remove(r);
            rebuild.run();
        });
        r.keyBox = box;
        r.valueField = val;
        r.removeBtn = remove;
        return r;
    }

    private int parseParamKey(String keyText) {
        int labelSeparator = keyText.indexOf('=');
        if (labelSeparator >= 0) {
            keyText = keyText.substring(0, labelSeparator).trim();
        }
        return Integer.parseInt(keyText);
    }
}
