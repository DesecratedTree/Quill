package com.desecratedtree.quill.ui.item;

import com.desecratedtree.quill.cache.DefinitionActions;
import com.desecratedtree.quill.cache.DecodedCache;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ItemEditorFrame extends JFrame {

    public ItemEditorFrame() {
        setTitle("Quill - Item Editor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        ItemListPanel itemListPanel = new ItemListPanel();
        ItemEditorPanel itemEditorPanel = new ItemEditorPanel();
        JPanel content = new JPanel(new BorderLayout());
        content.add(itemListPanel, BorderLayout.WEST);
        content.add(itemEditorPanel, BorderLayout.CENTER);
        add(content);
        itemListPanel.setListener(itemEditorPanel::loadItem);
        itemEditorPanel.setSaveListener(savedId -> {
            itemListPanel.refreshData();
            itemListPanel.selectItem(savedId);
        });
        itemListPanel.setContextActions(
                () -> mutateItem(itemListPanel, itemEditorPanel, DefinitionActions.addItem(), "Added item "),
                itemId -> mutateItem(itemListPanel, itemEditorPanel, DefinitionActions.duplicateItem(itemId), "Duplicated item to "),
                itemId -> {
                    DefinitionActions.deleteItem(itemId);
                    itemListPanel.refreshData();
                    itemListPanel.selectItem(itemId);
                    itemEditorPanel.loadItem(itemId);
                }
        );
        addWindowListener(new WindowAdapter() {

            @Override

            public void windowClosed(WindowEvent e) {
                DecodedCache.clear();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ItemEditorFrame().setVisible(true));
    }

    private void mutateItem(ItemListPanel listPanel, ItemEditorPanel editorPanel, int itemId, String messagePrefix) {
        listPanel.refreshData();
        if (!listPanel.selectItem(itemId)) {
            editorPanel.loadItem(itemId);
        }
        JOptionPane.showMessageDialog(this, messagePrefix + itemId + ".");
    }
}
