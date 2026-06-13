package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.render.ItemPreviewPanel;
import javax.swing.*;
import java.awt.*;

public class RendererDemoFrame extends JFrame {

    private final JTextField itemIdField = new JTextField("4151", 8);

    private final ItemPreviewPanel previewPanel = new ItemPreviewPanel();

    public RendererDemoFrame() {
        setTitle("Quill - Renderer Demo");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(720, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Item ID:"));
        controls.add(itemIdField);
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(e -> loadItem());
        controls.add(loadButton);
        JButton sampleButton = new JButton("Load Abyssal Whip");
        sampleButton.addActionListener(e -> {
            itemIdField.setText("4151");
            loadItem();
        });
        controls.add(sampleButton);
        add(controls, BorderLayout.NORTH);
        add(previewPanel, BorderLayout.CENTER);
        SwingUtilities.invokeLater(this::loadItem);
    }

    private void loadItem() {
        try {
            int itemId = Integer.parseInt(itemIdField.getText().trim());
            previewPanel.setItem(ItemDefinitions.getItemDefinitions(itemId));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid item id.");
        }
    }
}
