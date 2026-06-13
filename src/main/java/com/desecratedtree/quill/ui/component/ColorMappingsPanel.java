package com.desecratedtree.quill.ui.component;

import com.desecratedtree.quill.render.CacheColor;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class ColorMappingsPanel extends JPanel {

    private final JPanel rowsPanel = new JPanel(new GridBagLayout());

    private final List<ColorRow> rows = new ArrayList<>();

    private Runnable changeListener = () -> {};

    public ColorMappingsPanel() {
        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(rowsPanel), BorderLayout.CENTER);
        JButton add = new JButton("Add Color");
        add.addActionListener(e -> {
            rows.add(new ColorRow(0, 0));
            rebuild();
            changeListener.run();
        });
        add(add, BorderLayout.SOUTH);
        rebuild();
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener == null ? () -> {} : changeListener;
    }

    public void setMappings(int[] originals, int[] modified) {
        rows.clear();
        int count = Math.min(originals == null ? 0 : originals.length, modified == null ? 0 : modified.length);
        for (int i = 0; i < count; i++) {
            rows.add(new ColorRow(originals[i], modified[i]));
        }
        rebuild();
    }

    public int[] originalColors() {
        return rows.stream().mapToInt(row -> row.original()).toArray();
    }

    public int[] modifiedColors() {
        return rows.stream().mapToInt(row -> row.modified()).toArray();
    }

    private void rebuild() {
        rowsPanel.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        addHeader("From", c, 0);
        addHeader("Hex", c, 1);
        addHeader("To", c, 2);
        addHeader("Hex", c, 3);
        addHeader("", c, 4);
        for (ColorRow row : rows) {
            c.gridy++;
            addField(row.fromField, c, 0);
            addLabel(row.fromHex, c, 1);
            addField(row.toField, c, 2);
            addLabel(row.toHex, c, 3);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            JButton pick = new JButton("Pick");
            JButton remove = new JButton("x");
            pick.setMargin(new Insets(1, 5, 1, 5));
            remove.setMargin(new Insets(1, 5, 1, 5));
            pick.addActionListener(e -> pickColor(row));
            remove.addActionListener(e -> {
                rows.remove(row);
                rebuild();
                changeListener.run();
            });
            actions.add(pick);
            actions.add(remove);
            c.gridx = 4;
            c.weightx = 0;
            rowsPanel.add(actions, c);
        }
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private void addHeader(String text, GridBagConstraints c, int x) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        addLabel(label, c, x);
    }

    private void addField(JTextField field, GridBagConstraints c, int x) {
        c.gridx = x;
        c.weightx = 1;
        rowsPanel.add(field, c);
    }

    private void addLabel(JComponent label, GridBagConstraints c, int x) {
        c.gridx = x;
        c.weightx = 0;
        rowsPanel.add(label, c);
    }

    private void pickColor(ColorRow row) {
        Color selected = JColorChooser.showDialog(this, "Choose Replacement Color", CacheColor.toColor(row.modified()));
        if (selected == null) {
            return;
        }
        row.toField.setText(String.valueOf(CacheColor.fromColor(selected)));
        row.updateLabels();
        changeListener.run();
    }

    private final class ColorRow {

        private final JTextField fromField = new JTextField(6);

        private final JTextField toField = new JTextField(6);

        private final JLabel fromHex = new JLabel();

        private final JLabel toHex = new JLabel();

        private ColorRow(int from, int to) {
            fromField.setText(String.valueOf(from & 0xFFFF));
            toField.setText(String.valueOf(to & 0xFFFF));
            fromField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(() -> {
                updateLabels();
                changeListener.run();
            }));
            toField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(() -> {
                updateLabels();
                changeListener.run();
            }));
            updateLabels();
        }

        private int original() {
            return parseCacheColor(fromField.getText());
        }

        private int modified() {
            return parseCacheColor(toField.getText());
        }

        private void updateLabels() {
            fromHex.setText(CacheColor.toHex(original()));
            toHex.setText(CacheColor.toHex(modified()));
        }
    }

    private static int parseCacheColor(String text) {
        try {
            text = text.trim();
            if (text.startsWith("#")) {
                return CacheColor.fromColor(new Color(Integer.parseInt(text.substring(1), 16)));
            }
            if (text.startsWith("0x") || text.startsWith("0X")) {
                return Integer.parseInt(text.substring(2), 16) & 0xFFFF;
            }
            return Integer.parseInt(text) & 0xFFFF;
        } catch (RuntimeException ignored) {
            return 0;
        }
    }
}
