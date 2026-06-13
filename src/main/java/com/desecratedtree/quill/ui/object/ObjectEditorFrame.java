package com.desecratedtree.quill.ui.object;

import com.desecratedtree.quill.cache.DefinitionActions;
import javax.swing.*;
import java.awt.*;

public final class ObjectEditorFrame extends JFrame {

    public ObjectEditorFrame() {
        setTitle("Quill - Object Editor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1750, 900);
        setMinimumSize(new Dimension(1400, 760));
        setLocationRelativeTo(null);
        ObjectListPanel list = new ObjectListPanel();
        ObjectEditorPanel editor = new ObjectEditorPanel();
        list.setListener(editor::loadObject);
        list.setContextActions(
                () -> mutateObject(list, editor, DefinitionActions.addObject(), "Added object "),
                objectId -> mutateObject(list, editor, DefinitionActions.duplicateObject(objectId), "Duplicated object to "),
                objectId -> {
                    DefinitionActions.deleteObject(objectId);
                    list.refreshData();
                    list.selectObject(objectId);
                    editor.loadObject(objectId);
                }
        );
        JPanel content = new JPanel(new BorderLayout());
        content.add(list, BorderLayout.WEST);
        content.add(editor, BorderLayout.CENTER);
        add(content);
    }

    private void mutateObject(ObjectListPanel list, ObjectEditorPanel editor, int objectId, String messagePrefix) {
        list.refreshData();
        if (!list.selectObject(objectId)) {
            editor.loadObject(objectId);
        }
        JOptionPane.showMessageDialog(this, messagePrefix + objectId + ".");
    }
}
