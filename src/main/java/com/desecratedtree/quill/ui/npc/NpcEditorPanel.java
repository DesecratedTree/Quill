package com.desecratedtree.quill.ui.npc;

import com.desecratedtree.quill.codec.npc.NpcSaver;
import com.desecratedtree.quill.defs.NpcDefinitions;
import com.desecratedtree.quill.defs.RenderAnimationDefinitions;
import com.desecratedtree.quill.defs.SequenceDefinitions;
import com.desecratedtree.quill.render.ModelDecoderAdapter;
import com.desecratedtree.quill.render.ModelViewerPanel;
import com.desecratedtree.quill.ui.component.ColorMappingsPanel;
import com.desecratedtree.quill.ui.component.SimpleDocumentListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class NpcEditorPanel extends JPanel {

    private NpcDefinitions current;

    private final JTextField name = new JTextField();

    private final JTextField combat = new JTextField();

    private final JTextField size = new JTextField();

    private final JTextField renderAnim = new JTextField();

    private final List<ModelRow> modelRows = new ArrayList<>();

    private final JPanel modelRowsPanel = new JPanel(new GridBagLayout());

    private final JTextField modelArray = new JTextField();

    private final JButton addModel = new JButton("Add Model");

    private final JTextArea options = createTextArea(5, true);

    private final JCheckBox minimap = new JCheckBox("Visible On Minimap");

    private final JCheckBox clickable = new JCheckBox("Clickable");

    private final ColorMappingsPanel colors = new ColorMappingsPanel();

    private final JTextField headIcon = new JTextField();

    private final ModelViewerPanel preview = new ModelViewerPanel();

    private final JButton saveButton = new JButton("Save");

    private IntConsumer saveListener;

    private JScrollPane formScroll;

    private boolean updatingModels;

    public NpcEditorPanel() {
        setLayout(new BorderLayout(8, 8));
        add(createForm(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        preview.setPreferredSize(new Dimension(320, 520));
        add(preview, BorderLayout.EAST);
        saveButton.addActionListener(e -> save());
        addModel.addActionListener(e -> addModel());
        renderAnim.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::updatePreview));
    }

    public void setSaveListener(IntConsumer saveListener) {
        this.saveListener = saveListener;
    }

    public void loadNpc(int id) {
        current = NpcDefinitions.get(id);
        if (current == null) {
            return;
        }
        name.setText(current.name);
        combat.setText(String.valueOf(current.combatLevel));
        size.setText(String.valueOf(current.size));
        renderAnim.setText(String.valueOf(current.renderAnimId));
        setModelIds(current.modelIds);
        options.setText(joinLines(current.options));
        minimap.setSelected(current.visibleOnMinimap);
        clickable.setSelected(current.clickable);
        colors.setMappings(current.originalModelColors, current.modifiedModelColors);
        headIcon.setText(String.valueOf(current.headIcon));
        if (formScroll != null) {
            formScroll.getVerticalScrollBar().setValue(0);
        }
        preview.resetView();
        updatePreview();
    }

    private JComponent createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(createInfoPanel(), c);
        c.gridy = 1;
        panel.add(Box.createVerticalStrut(8), c);
        c.gridy = 2;
        panel.add(createModelsPanel(), c);
        c.gridy = 3;
        panel.add(Box.createVerticalStrut(8), c);
        c.gridy = 4;
        panel.add(createOptionsPanel(), c);
        c.gridy = 5;
        panel.add(Box.createVerticalStrut(8), c);
        c.gridy = 6;
        panel.add(createFlagsPanel(), c);
        c.gridy = 7;
        panel.add(Box.createVerticalStrut(8), c);
        c.gridy = 8;
        c.weighty = 1.0;
        panel.add(createColorsPanel(), c);
        formScroll = new JScrollPane(panel);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        return formScroll;
    }

    private JPanel createInfoPanel() {
        JPanel panel = titledPanel("NPC Definition");
        addRow(panel, "Name:", name, 0);
        addRow(panel, "Combat:", combat, 1);
        addRow(panel, "Size:", size, 2);
        addRow(panel, "Render Anim:", renderAnim, 3);
        addRow(panel, "Head Icon:", headIcon, 4);
        return panel;
    }

    private JPanel createModelsPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(new TitledBorder("Models"));
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.add(addModel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        modelRowsPanel.setBorder(new EmptyBorder(4, 0, 4, 0));
        panel.add(new JScrollPane(modelRowsPanel), BorderLayout.CENTER);
        JPanel arrayPanel = new JPanel(new BorderLayout(4, 4));
        arrayPanel.add(new JLabel("Model IDs:"), BorderLayout.WEST);
        modelArray.setEditable(false);
        arrayPanel.add(modelArray, BorderLayout.CENTER);
        panel.add(arrayPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 350));
        return panel;
    }

    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Options"));
        panel.add(new JScrollPane(options), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 200));
        return panel;
    }

    private JPanel createFlagsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setBorder(new TitledBorder("Flags"));
        panel.add(minimap);
        panel.add(clickable);
        return panel;
    }

    private JPanel createColorsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Color Mappings"));
        panel.add(colors, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createFooter() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBorder(new EmptyBorder(0, 0, 2, 0));
        actions.add(saveButton);
        return actions;
    }

    private void addRow(JPanel panel, String label, JComponent field, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(field, c);
    }

    private void save() {
        if (current == null) {
            return;
        }
        current.name = name.getText().trim().isEmpty() ? "null" : name.getText().trim();
        current.combatLevel = parseInt(combat, -1);
        current.size = parseInt(size, 1);
        current.renderAnimId = parseInt(renderAnim, -1);
        current.modelIds = currentModelIds();
        current.options = options.getText().split("\n", -1);
        for (int i = 0; i < current.options.length; i++) {
            String opt = current.options[i].trim();
            current.options[i] = opt.isEmpty() ? null : opt;
        }
        current.visibleOnMinimap = minimap.isSelected();
        current.clickable = clickable.isSelected();
        current.originalModelColors = colors.originalColors();
        current.modifiedModelColors = colors.modifiedColors();
        current.headIcon = parseInt(headIcon, -1);
        NpcSaver.save(current);
        int savedId = current.id;
        loadNpc(savedId);
        if (formScroll != null) {
            formScroll.getVerticalScrollBar().setValue(0);
        }
        if (saveListener != null) {
            saveListener.accept(savedId);
        }
        JOptionPane.showMessageDialog(this, "Saved!");
    }

    private void updatePreview() {
        if (current == null) {
            preview.setModel(null);
            return;
        }
        int animId = parseInt(renderAnim, -1);
        long baseTime = System.currentTimeMillis();
        NpcDefinitions npc = current;
        preview.setModelSupplier(() -> ModelDecoderAdapter.loadNpcModel(
                npc,
                animId,
                currentModelIds(),
                colors.originalColors(),
                colors.modifiedColors(),
                animationFrameIndexAt(animId, System.currentTimeMillis() - baseTime)
        ));
    }

    private static int animationFrameIndexAt(int renderAnimId, long elapsedMillis) {
        RenderAnimationDefinitions renderAnim = RenderAnimationDefinitions.get(renderAnimId);
        if (renderAnim == null) {
            return 0;
        }
        SequenceDefinitions sequence = SequenceDefinitions.get(renderAnim.idleSequenceId);
        return sequence == null ? 0 : sequence.frameIndexAtElapsedMillis(elapsedMillis);
    }

    private void setModelIds(int[] ids) {
        updatingModels = true;
        modelRows.clear();
        if (ids != null) {
            for (int modelId : ids) {
                if (modelId >= 0) {
                    modelRows.add(createModelRow(modelId));
                }
            }
        }
        rebuildModelRows();
        modelArray.setText(join(currentModelIds()));
        updatingModels = false;
        updatePreview();
    }

    private int[] currentModelIds() {
        return modelRows.stream()
                .mapToInt(row -> parseInt(row.modelIdField, -1))
                .filter(id -> id >= 0)
                .toArray();
    }

    private void addModel() {
        modelRows.add(createModelRow(-1));
        rebuildModelRows();
        modelRowsChanged();
    }

    private ModelRow createModelRow(int modelId) {
        ModelRow row = new ModelRow();
        row.modelIdField = new JTextField(modelId >= 0 ? String.valueOf(modelId) : "", 8);
        row.modelIdField.setPreferredSize(new Dimension(80, 22));
        row.removeBtn = new JButton("x");
        row.removeBtn.setMargin(new Insets(0, 4, 0, 4));
        row.modelIdField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::modelRowsChanged));
        row.removeBtn.addActionListener(e -> {
            modelRows.remove(row);
            rebuildModelRows();
            modelRowsChanged();
        });
        return row;
    }

    private void rebuildModelRows() {
        modelRowsPanel.removeAll();
        for (int i = 0; i < modelRows.size(); i++) {
            ModelRow row = modelRows.get(i);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            modelRowsPanel.add(new JLabel(String.valueOf(i)), c);
            c.gridx = 1;
            c.weightx = 1.0;
            modelRowsPanel.add(row.modelIdField, c);
            c.gridx = 2;
            c.weightx = 0;
            modelRowsPanel.add(row.removeBtn, c);
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = modelRows.size();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        modelRowsPanel.add(Box.createVerticalGlue(), c);
        modelRowsPanel.revalidate();
        modelRowsPanel.repaint();
    }

    private void modelRowsChanged() {
        if (updatingModels) {
            return;
        }
        modelArray.setText(join(currentModelIds()));
        updatePreview();
    }

    private int parseInt(JTextField field, int fallback) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String join(int[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        return builder.toString();
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

    private JTextArea createTextArea(int rows, boolean wrap) {
        JTextArea area = new JTextArea(rows, 24);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setLineWrap(wrap);
        area.setWrapStyleWord(wrap);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private JPanel titledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static final class ModelRow {

        private JTextField modelIdField;

        private JButton removeBtn;
    }
}
