package com.desecratedtree.quill.ui;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Shared visual language for Quill's Swing workspace. */
public final class UiStyles {
    public static final Color SURFACE = new Color(30, 34, 42);
    public static final Color SURFACE_RAISED = new Color(38, 43, 53);
    public static final Color SURFACE_SUBTLE = new Color(25, 29, 36);
    public static final Color ACCENT = new Color(103, 168, 255);
    public static final Color ACCENT_SOFT = new Color(54, 91, 139);
    public static final Color TEXT_MUTED = new Color(157, 166, 181);
    public static final Color SUCCESS = new Color(99, 202, 151);
    public static final int GAP = 12;
    public static final int OUTER = 20;

    private UiStyles() { }

    public static void configureDefaults() {
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 7);
        UIManager.put("Component.minimumWidth", 90);
        UIManager.put("Button.minimumHeight", 32);
        UIManager.put("TextField.minimumHeight", 32);
        UIManager.put("ComboBox.minimumHeight", 32);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("TitlePane.unifiedBackground", true);
    }

    public static void styleRoot(JComponent root) {
        root.setBackground(SURFACE_SUBTLE);
        root.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
    }

    public static void stylePanel(JComponent panel) {
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));
    }

    public static void styleCard(JComponent panel) {
        panel.setBackground(SURFACE_RAISED);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderColor: #414958");
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #67a8ff; foreground: #101722");
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 8; borderColor: #414958");
        return button;
    }

    public static JLabel eyebrow(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setForeground(ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        return label;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(ACCENT_SOFT);
        table.setSelectionForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 11f));
    }

    public static void styleScrollPane(JScrollPane pane) {
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getViewport().setBackground(SURFACE);
    }
}
