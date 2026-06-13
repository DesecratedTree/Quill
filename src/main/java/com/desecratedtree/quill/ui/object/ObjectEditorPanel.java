package com.desecratedtree.quill.ui.object;

import com.desecratedtree.quill.codec.object.ObjectSaver;
import com.desecratedtree.quill.defs.ObjectDefinitions;
import com.desecratedtree.quill.defs.SequenceDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.ModelViewerPanel;
import com.desecratedtree.quill.ui.ModelEditorDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ObjectEditorPanel extends JPanel {

    private ObjectDefinitions current;

    private long animationStartMillis;

    private final JTextField name = new JTextField();

    private final JTextField sizeX = new JTextField();

    private final JTextField sizeY = new JTextField();

    private final JTextField interactive = new JTextField();

    private final JTextField solid = new JTextField();

    private final JTextField supportItems = new JTextField();

    private final JTextField blockFlag = new JTextField();

    private final JTextField brightness = new JTextField();

    private final JTextField contrast = new JTextField();

    private final JTextField culling = new JTextField();

    private final JTextField contouredGround = new JTextField();

    private final JTextField contourAmount = new JTextField();

    private final JTextField mapscene = new JTextField();

    private final JTextField mapDefinitionId = new JTextField();

    private final JTextField modelSizeX = new JTextField();

    private final JTextField modelSizeY = new JTextField();

    private final JTextField modelSizeZ = new JTextField();

    private final JTextField offsetX = new JTextField();

    private final JTextField offsetY = new JTextField();

    private final JTextField offsetZ = new JTextField();

    private final JTextField varbit = new JTextField();

    private final JTextField varp = new JTextField();

    private final JTextField transformDefault = new JTextField();

    private final JTextField ambientSoundId = new JTextField();

    private final JTextField soundRange = new JTextField();

    private final JTextField soundMinDelay = new JTextField();

    private final JTextField soundMaxDelay = new JTextField();

    private final JCheckBox mirrored = new JCheckBox("Mirrored");

    private final JCheckBox castsShadow = new JCheckBox("Casts Shadow");

    private final JCheckBox blocksLand = new JCheckBox("Blocks Land");

    private final JCheckBox ignoreOnRoute = new JCheckBox("Ignore On Route");

    private final JCheckBox blocksSky = new JCheckBox("Blocks Sky");

    private final JCheckBox delayShading = new JCheckBox("Delay Shading");

    private final JCheckBox hideMinimap = new JCheckBox("Hide Minimap");

    private final JCheckBox animateImmediately = new JCheckBox("Animate Immediately");

    private final JCheckBox isMembers = new JCheckBox("Members");

    private final JTextArea options = createTextArea(5, true);

    private final JTextArea memberOptions = createTextArea(5, true);

    private final JTextArea models = createTextArea(5, false);

    private final JTextArea animations = createTextArea(5, false);

    private final JTextArea transforms = createTextArea(5, false);

    private final JTextArea ambientSoundEffects = createTextArea(5, false);

    private final ModelViewerPanel preview = new ModelViewerPanel();

    private final JLabel modelFormatHint = new JLabel("Format: type:modelIds. Example: 10:16419,16420");

    private final JTextArea modelTypeHelp = createHelpArea(
            "Common object model types:\n"
                    + "0-3 = wall shapes\n"
                    + "4-8 = wall decorations\n"
                    + "9 = diagonal object\n"
                    + "10 = normal scenery object\n"
                    + "11 = large scenery variant\n"
                    + "22 = ground decoration\n"
                    + "Other values exist for cache-specific engine variants."
    );

    private final JButton saveButton = new JButton("Save");

    private final JButton openModelEditorButton = new JButton("Open Model Editor");

    private final JButton openModelEditorInlineButton = new JButton("Open Model Editor");

    private final JButton editParamsButton = new JButton("Edit Params");

    private JScrollPane formScrollPane;

    public ObjectEditorPanel() {
        setLayout(new BorderLayout(8, 8));
        add(createForm(), BorderLayout.CENTER);
        add(createActions(), BorderLayout.SOUTH);
        preview.setPreferredSize(new Dimension(320, 520));
        add(preview, BorderLayout.EAST);
        saveButton.addActionListener(e -> save());
        openModelEditorButton.addActionListener(e -> openModelEditor());
        openModelEditorInlineButton.addActionListener(e -> openModelEditor());
        editParamsButton.addActionListener(e -> openParamsDialog());
    }

    public void loadObject(int id) {
        current = ObjectDefinitions.get(id);
        if (current == null) {
            return;
        }
        animationStartMillis = System.currentTimeMillis();
        name.setText(current.name);
        sizeX.setText(String.valueOf(current.sizeX));
        sizeY.setText(String.valueOf(current.sizeY));
        interactive.setText(String.valueOf(current.interactive));
        solid.setText(String.valueOf(current.solid));
        supportItems.setText(String.valueOf(current.supportItems));
        blockFlag.setText(String.valueOf(current.blockFlag));
        brightness.setText(String.valueOf(current.brightness));
        contrast.setText(String.valueOf(current.contrast));
        culling.setText(String.valueOf(current.culling));
        contouredGround.setText(String.valueOf(current.contouredGround));
        contourAmount.setText(String.valueOf(current.anInt3023));
        mapscene.setText(String.valueOf(current.mapscene));
        mapDefinitionId.setText(String.valueOf(current.mapDefinitionId));
        modelSizeX.setText(String.valueOf(current.modelSizeX));
        modelSizeY.setText(String.valueOf(current.modelSizeY));
        modelSizeZ.setText(String.valueOf(current.modelSizeZ));
        offsetX.setText(String.valueOf(current.offsetX));
        offsetY.setText(String.valueOf(current.offsetY));
        offsetZ.setText(String.valueOf(current.offsetZ));
        varbit.setText(String.valueOf(current.varbit));
        varp.setText(String.valueOf(current.varp));
        transformDefault.setText(String.valueOf(current.transformDefault));
        ambientSoundId.setText(String.valueOf(current.anInt3015));
        soundRange.setText(String.valueOf(current.anInt3012));
        soundMinDelay.setText(String.valueOf(current.anInt2989));
        soundMaxDelay.setText(String.valueOf(current.anInt2971));
        mirrored.setSelected(current.mirrored);
        castsShadow.setSelected(current.castsShadow);
        blocksLand.setSelected(current.blocksLand);
        ignoreOnRoute.setSelected(current.ignoreOnRoute);
        blocksSky.setSelected(current.blocksSky);
        delayShading.setSelected(current.delayShading);
        hideMinimap.setSelected(current.hideMinimap);
        animateImmediately.setSelected(current.animateImmediately);
        isMembers.setSelected(current.isMembers);
        options.setText(joinLines(current.options));
        memberOptions.setText(joinLines(current.memberOptions));
        models.setText(formatModels(current));
        animations.setText(formatAnimations(current));
        transforms.setText(formatTransforms(current));
        ambientSoundEffects.setText(joinInts(current.anIntArray3036));
        resetScrollableInputs();
        scrollFormToTop();
        updatePreview();
    }

    private JComponent createForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(6, 6, 280, 6));
        panel.setPreferredSize(new Dimension(920, 1320));
        panel.add(createSectionRow(createBasicPanel(), createDisplayPanel()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createSectionRow(createModelsPanel(), createAnimationPanel()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createSectionRow(createSoundPanel(), createMorphPanel()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createCombinedOptionsPanel());
        panel.add(Box.createVerticalStrut(8));
        formScrollPane = new JScrollPane(panel);
        formScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return formScrollPane;
    }

    private JComponent createActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBorder(new EmptyBorder(0, 0, 2, 0));
        actions.add(saveButton);
        actions.add(openModelEditorButton);
        actions.add(editParamsButton);
        return actions;
    }

    private JPanel createSectionRow(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(left);
        row.add(right);
        return row;
    }

    private JPanel createBasicPanel() {
        JPanel panel = titledPanel("Basic Info");
        addRow(panel, "Name:", name, 0);
        addRow(panel, "Size X:", sizeX, 1);
        addRow(panel, "Size Y:", sizeY, 2);
        addRow(panel, "Interactive:", interactive, 3);
        addRow(panel, "Solid:", solid, 4);
        addRow(panel, "Support Items:", supportItems, 5);
        addRow(panel, "Block Flag:", blockFlag, 6);
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        checks.add(mirrored);
        checks.add(castsShadow);
        checks.add(blocksLand);
        checks.add(ignoreOnRoute);
        checks.add(blocksSky);
        checks.add(delayShading);
        checks.add(hideMinimap);
        checks.add(animateImmediately);
        checks.add(isMembers);
        addRow(panel, "Flags:", checks, 7);
        return panel;
    }

    private JPanel createDisplayPanel() {
        JPanel panel = titledPanel("Display And Transform");
        addRow(panel, "Brightness:", brightness, 0);
        addRow(panel, "Contrast:", contrast, 1);
        addRow(panel, "Culling:", culling, 2);
        addRow(panel, "Contour Type:", contouredGround, 3);
        addRow(panel, "Contour Value:", contourAmount, 4);
        addRow(panel, "Model Size X:", modelSizeX, 5);
        addRow(panel, "Model Size Y:", modelSizeY, 6);
        addRow(panel, "Model Size Z:", modelSizeZ, 7);
        addRow(panel, "Offset X:", offsetX, 8);
        addRow(panel, "Offset Y:", offsetY, 9);
        addRow(panel, "Offset Z:", offsetZ, 10);
        addRow(panel, "Mapscene:", mapscene, 11);
        addRow(panel, "Map Definition:", mapDefinitionId, 12);
        return panel;
    }

    private JPanel createModelsPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Models"));
        panel.setPreferredSize(new Dimension(0, 360));
        modelFormatHint.setForeground(UIManager.getColor("Label.disabledForeground"));
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(modelFormatHint, BorderLayout.NORTH);
        header.add(modelTypeHelp, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);
        panel.add(wrapArea(models), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.add(openModelEditorInlineButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAnimationPanel() {
        JPanel panel = titledPanel("Animations");
        addRow(panel, "Animation Entries:", wrapArea(animations), 0, GridBagConstraints.BOTH, 1.0);
        addRow(panel, "Format:", new JLabel("Use one line per animation. `1234` for single, or `1234:40` for weighted."), 1);
        panel.setPreferredSize(new Dimension(0, 280));
        return panel;
    }

    private JPanel createSoundPanel() {
        JPanel panel = titledPanel("Sounds");
        addRow(panel, "Ambient Sound:", ambientSoundId, 0);
        addRow(panel, "Sound Range:", soundRange, 1);
        addRow(panel, "Min Delay:", soundMinDelay, 2);
        addRow(panel, "Max Delay:", soundMaxDelay, 3);
        addRow(panel, "Sound Effect IDs:", wrapArea(ambientSoundEffects), 4, GridBagConstraints.BOTH, 1.0);
        panel.setPreferredSize(new Dimension(0, 260));
        return panel;
    }

    private JPanel createMorphPanel() {
        JPanel panel = titledPanel("Morphs");
        addRow(panel, "Varbit:", varbit, 0);
        addRow(panel, "Varp:", varp, 1);
        addRow(panel, "Default Transform:", transformDefault, 2);
        addRow(panel, "Transforms:", wrapArea(transforms), 3, GridBagConstraints.BOTH, 1.0);
        panel.setPreferredSize(new Dimension(0, 260));
        return panel;
    }

    private JPanel createCombinedOptionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createOptionsPanel("Context Options", options, 220));
        panel.add(createOptionsPanel("Members Context Options", memberOptions, 220));
        return panel;
    }

    private JPanel createOptionsPanel(String title, JTextArea area, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(0, Math.max(height, 200)));
        panel.add(wrapArea(area), BorderLayout.CENTER);
        return panel;
    }

    private JPanel titledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JTextArea createTextArea(int rows, boolean wrap) {
        JTextArea area = new JTextArea(rows, 24);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setLineWrap(wrap);
        area.setWrapStyleWord(wrap);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private JTextArea createHelpArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFocusable(false);
        area.setBorder(new EmptyBorder(0, 0, 0, 0));
        area.setFont(UIManager.getFont("Label.font"));
        area.setForeground(UIManager.getColor("Label.disabledForeground"));
        return area;
    }

    private JScrollPane wrapArea(JTextArea area) {
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        int lineHeight = area.getFontMetrics(area.getFont()).getHeight();
        int height = Math.max(130, lineHeight * 5 + 26);
        Dimension size = new Dimension(0, height);
        scrollPane.setPreferredSize(size);
        scrollPane.setMinimumSize(size);
        return scrollPane;
    }

    private void addRow(JPanel panel, String label, JComponent field, int row) {
        addRow(panel, label, field, row, GridBagConstraints.HORIZONTAL, 0.0);
    }

    private void addRow(JPanel panel, String label, JComponent field, int row, int fill, double weightY) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = fill;
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = weightY;
        panel.add(field, c);
    }

    private void save() {
        if (current == null) {
            return;
        }
        current.name = name.getText().trim().isEmpty() ? "null" : name.getText().trim();
        current.sizeX = parseInt(sizeX, current.sizeX);
        current.sizeY = parseInt(sizeY, current.sizeY);
        current.interactive = parseInt(interactive, current.interactive);
        current.solid = parseInt(solid, current.solid);
        current.supportItems = parseInt(supportItems, current.supportItems);
        current.blockFlag = parseInt(blockFlag, current.blockFlag);
        current.brightness = parseInt(brightness, current.brightness);
        current.contrast = parseInt(contrast, current.contrast);
        current.culling = parseInt(culling, current.culling);
        current.contouredGround = (byte) parseInt(contouredGround, current.contouredGround);
        current.anInt3023 = parseInt(contourAmount, current.anInt3023);
        current.mapscene = parseInt(mapscene, current.mapscene);
        current.mapDefinitionId = parseInt(mapDefinitionId, current.mapDefinitionId);
        current.modelSizeX = parseInt(modelSizeX, current.modelSizeX);
        current.modelSizeY = parseInt(modelSizeY, current.modelSizeY);
        current.modelSizeZ = parseInt(modelSizeZ, current.modelSizeZ);
        current.offsetX = parseInt(offsetX, current.offsetX);
        current.offsetY = parseInt(offsetY, current.offsetY);
        current.offsetZ = parseInt(offsetZ, current.offsetZ);
        current.varbit = parseInt(varbit, current.varbit);
        current.varp = parseInt(varp, current.varp);
        current.transformDefault = parseInt(transformDefault, current.transformDefault);
        current.anInt3015 = parseInt(ambientSoundId, current.anInt3015);
        current.anInt3012 = parseInt(soundRange, current.anInt3012);
        current.anInt2989 = parseInt(soundMinDelay, current.anInt2989);
        current.anInt2971 = parseInt(soundMaxDelay, current.anInt2971);
        current.mirrored = mirrored.isSelected();
        current.castsShadow = castsShadow.isSelected();
        current.blocksLand = blocksLand.isSelected();
        current.ignoreOnRoute = ignoreOnRoute.isSelected();
        current.blocksSky = blocksSky.isSelected();
        current.delayShading = delayShading.isSelected();
        current.hideMinimap = hideMinimap.isSelected();
        current.animateImmediately = animateImmediately.isSelected();
        current.isMembers = isMembers.isSelected();
        current.options = parseLines(options.getText(), 6);
        current.memberOptions = parseLines(memberOptions.getText(), 5);
        current.anIntArray3036 = emptyToNull(parseIntList(ambientSoundEffects.getText()));
        current.transforms = parseTransforms(transforms.getText(), current.transformDefault);
        parseModels(current, models.getText());
        parseAnimations(current, animations.getText());
        ObjectSaver.save(current);
        JOptionPane.showMessageDialog(this, "Saved!");
        loadObject(current.id);
    }

    private void openModelEditor() {
        if (current == null) {
            return;
        }
        int modelId = selectedModelId();
        if (modelId < 0) {
            JOptionPane.showMessageDialog(this, "No valid model id found for this object.");
            return;
        }
        ModelEditorDialog.showEditor(this, modelId, "Object " + current.id + " Model " + modelId, this::updatePreview);
    }

    private void openParamsDialog() {
        if (current == null) {
            return;
        }
        if (current.params == null) {
            current.params = new HashMap<>();
        }
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Object Params",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(620, 400);
        dialog.setLayout(new BorderLayout(6, 6));
        JTextArea editor = new JTextArea(formatParams(current.params), 18, 56);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(editor);
        JButton save = new JButton("Save");
        JButton close = new JButton("Close");
        save.addActionListener(e -> {
            current.params = parseParams(editor.getText());
            JOptionPane.showMessageDialog(dialog, "Saved!");
        });
        close.addActionListener(e -> dialog.dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.add(save);
        actions.add(close);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private int selectedModelId() {
        String text = models.getText();
        if (text == null || text.trim().isEmpty()) {
            return -1;
        }
        int caret = Math.max(0, models.getCaretPosition());
        String[] lines = text.split("\\R");
        int offset = 0;
        for (String line : lines) {
            int nextOffset = offset + line.length() + 1;
            if (caret <= nextOffset) {
                int[] parsed = parseModelLine(line);
                if (parsed.length > 0) {
                    return parsed[0];
                }
                break;
            }
            offset = nextOffset;
        }
        int[] flattened = flattenModelIds(current);
        return flattened.length == 0 ? -1 : flattened[0];
    }

    private int[] parseModelLine(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (trimmed.isEmpty()) {
            return new int[0];
        }
        String[] parts = trimmed.split(":", 2);
        return parseIntList(parts.length == 2 ? parts[1] : trimmed);
    }

    private void resetScrollableInputs() {
        models.setCaretPosition(0);
        animations.setCaretPosition(0);
        transforms.setCaretPosition(0);
        ambientSoundEffects.setCaretPosition(0);
        options.setCaretPosition(0);
        memberOptions.setCaretPosition(0);
    }

    private void scrollFormToTop() {
        if (formScrollPane == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> formScrollPane.getVerticalScrollBar().setValue(0));
    }

    private void updatePreview() {
        if (current == null) {
            preview.setModel(null);
            return;
        }
        int[] flattened = flattenModelIds(current);
        if (flattened.length == 0) {
            preview.setModel(null);
            return;
        }
        if (primaryAnimation(current) >= 0) {
            preview.setModelSupplier(() -> ModelDecoderAdapter.loadObjectModel(
                    current,
                    flattenModelIds(current),
                    animationFrameIndexAt(primaryAnimation(current), System.currentTimeMillis() - animationStartMillis)
            ));
            return;
        }
        preview.setModel(ModelDecoderAdapter.loadObjectModel(current, flattened, 0));
    }

    private int animationFrameIndexAt(int animationId, long elapsedMillis) {
        SequenceDefinitions sequence = SequenceDefinitions.get(animationId);
        return sequence == null ? 0 : sequence.frameIndexAtElapsedMillis(elapsedMillis);
    }

    private int primaryAnimation(ObjectDefinitions definition) {
        if (definition.animations == null) {
            return -1;
        }
        for (int animationId : definition.animations) {
            if (animationId >= 0) {
                return animationId;
            }
        }
        return -1;
    }

    private int parseInt(JTextField field, int fallback) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String joinLines(String[] values) {
        if (values == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(values[i] == null ? "" : values[i]);
        }
        return builder.toString();
    }

    private String[] parseLines(String text, int size) {
        String[] values = new String[size];
        String[] lines = text.split("\\R", -1);
        for (int i = 0; i < size; i++) {
            values[i] = i < lines.length ? emptyToNull(lines[i].trim()) : null;
        }
        if (size > 5) {
            values[5] = "Examine";
        }
        return values;
    }

    private String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private String formatModels(ObjectDefinitions definition) {
        if (definition.modelIds == null || definition.modelIds.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < definition.modelIds.length; i++) {
            if (i > 0) {
                builder.append('\n');
            }
            int type = definition.modelTypes != null && i < definition.modelTypes.length ? definition.modelTypes[i] : 10;
            builder.append(type).append(':');
            int[] group = definition.modelIds[i];
            for (int j = 0; j < group.length; j++) {
                if (j > 0) {
                    builder.append(',');
                }
                builder.append(group[j]);
            }
        }
        return builder.toString();
    }

    private void parseModels(ObjectDefinitions definition, String text) {
        String[] lines = text.split("\\R");
        List<int[]> ids = new ArrayList<>();
        List<Byte> types = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(":", 2);
            int type = 10;
            String modelPart = trimmed;
            if (parts.length == 2) {
                try {
                    type = Integer.parseInt(parts[0].trim());
                    modelPart = parts[1];
                } catch (NumberFormatException ignored) {
                }
            }
            int[] parsed = parseIntList(modelPart);
            if (parsed.length > 0) {
                ids.add(parsed);
                types.add((byte) type);
            }
        }
        definition.modelIds = ids.isEmpty() ? null : ids.toArray(new int[0][]);
        if (ids.isEmpty()) {
            definition.modelTypes = null;
            return;
        }
        definition.modelTypes = new byte[types.size()];
        for (int i = 0; i < types.size(); i++) {
            definition.modelTypes[i] = types.get(i);
        }
    }

    private String formatAnimations(ObjectDefinitions definition) {
        if (definition.animations == null || definition.animations.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean weighted = definition.percents != null && definition.percents.length > 0;
        for (int i = 0; i < definition.animations.length; i++) {
            if (definition.animations[i] < 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(definition.animations[i]);
            if (weighted) {
                int percent = i < definition.percents.length ? definition.percents[i] : 0;
                builder.append(':').append(percent);
            }
        }
        return builder.toString();
    }

    private void parseAnimations(ObjectDefinitions definition, String text) {
        String[] lines = text.split("\\R");
        List<Integer> ids = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        boolean weighted = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(":", 2);
            try {
                ids.add(Integer.parseInt(parts[0].trim()));
                if (parts.length == 2) {
                    weights.add(Integer.parseInt(parts[1].trim()));
                    weighted = true;
                } else {
                    weights.add(1);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (ids.isEmpty()) {
            definition.animations = null;
            definition.percents = null;
            return;
        }
        definition.animations = ids.stream().mapToInt(Integer::intValue).toArray();
        definition.percents = weighted || ids.size() > 1 ? weights.stream().mapToInt(Integer::intValue).toArray() : null;
    }

    private String formatTransforms(ObjectDefinitions definition) {
        if (definition.transforms == null || definition.transforms.length == 0) {
            return "";
        }
        int limit = definition.transforms.length;
        if (limit > 0 && definition.transforms[limit - 1] == definition.transformDefault) {
            limit--;
        }
        if (limit <= 0) {
            return "";
        }
        int[] visible = new int[limit];
        System.arraycopy(definition.transforms, 0, visible, 0, limit);
        return joinInts(visible);
    }

    private String joinInts(int[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    private String formatParams(Map<Integer, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(entry.getKey()).append('=').append(entry.getValue());
                });
        return builder.toString();
    }

    private HashMap<Integer, Object> parseParams(String text) {
        HashMap<Integer, Object> params = new HashMap<>();
        String[] lines = text.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            try {
                int key = Integer.parseInt(trimmed.substring(0, separator).trim());
                String valueText = trimmed.substring(separator + 1).trim();
                if (valueText.isEmpty()) {
                    continue;
                }
                Object value;
                try {
                    value = Integer.parseInt(valueText);
                } catch (NumberFormatException ex) {
                    value = valueText;
                }
                params.put(key, value);
            } catch (NumberFormatException ignored) {
            }
        }
        return params.isEmpty() ? null : params;
    }

    private int[] parseIntList(String text) {
        String normalized = text.replace(',', '\n');
        String[] parts = normalized.split("\\R");
        List<Integer> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                values.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException ignored) {
            }
        }
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    private int[] parseTransforms(String text, int defaultTransform) {
        int[] parsed = parseIntList(text);
        if (parsed.length == 0) {
            return null;
        }
        int[] encoded = new int[parsed.length + 1];
        System.arraycopy(parsed, 0, encoded, 0, parsed.length);
        encoded[encoded.length - 1] = defaultTransform;
        return encoded;
    }

    private int[] emptyToNull(int[] values) {
        return values.length == 0 ? null : values;
    }

    private int[] flattenModelIds(ObjectDefinitions definition) {
        if (definition.modelIds == null) {
            return new int[0];
        }
        List<Integer> models = new ArrayList<>();
        for (int[] group : definition.modelIds) {
            if (group == null) {
                continue;
            }
            for (int modelId : group) {
                if (modelId >= 0) {
                    models.add(modelId);
                }
            }
        }
        return models.stream().mapToInt(Integer::intValue).toArray();
    }
}
