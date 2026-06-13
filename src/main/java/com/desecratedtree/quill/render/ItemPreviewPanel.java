package com.desecratedtree.quill.render;

import com.desecratedtree.quill.defs.ItemDefinitions;
import javax.swing.*;
import java.awt.*;

public class ItemPreviewPanel extends JPanel {

    private final ModelViewerPanel modelViewerPanel = new ModelViewerPanel();

    public ItemPreviewPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel modelPanel = new JPanel(new BorderLayout());
        modelPanel.setBorder(BorderFactory.createTitledBorder("Model Viewer"));
        modelPanel.add(modelViewerPanel, BorderLayout.CENTER);
        JButton resetView = new JButton("Reset View");
        resetView.addActionListener(e -> modelViewerPanel.resetView());
        modelPanel.add(resetView, BorderLayout.SOUTH);
        add(modelPanel, BorderLayout.CENTER);
    }

    public void setItem(ItemDefinitions item) {
        if (item == null || item.modelId < 0) {
            modelViewerPanel.setModel(null);
            return;
        }
        modelViewerPanel.setModel(ModelDecoderAdapter.loadItemModel(item));
    }
}
