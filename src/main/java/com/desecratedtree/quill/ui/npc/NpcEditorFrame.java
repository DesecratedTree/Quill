package com.desecratedtree.quill.ui.npc;

import com.desecratedtree.quill.cache.DefinitionActions;
import javax.swing.*;
import java.awt.*;

public class NpcEditorFrame extends JFrame {

    public NpcEditorFrame() {
        setTitle("Quill - NPC Editor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        NpcListPanel list = new NpcListPanel();
        NpcEditorPanel editor = new NpcEditorPanel();
        list.setListener(editor::loadNpc);
        editor.setSaveListener(savedId -> {
            list.refreshData();
            list.selectNpc(savedId);
        });
        list.setContextActions(
                () -> mutateNpc(list, editor, DefinitionActions.addNpc(), "Added NPC "),
                npcId -> mutateNpc(list, editor, DefinitionActions.duplicateNpc(npcId), "Duplicated NPC to "),
                npcId -> {
                    DefinitionActions.deleteNpc(npcId);
                    list.refreshData();
                    list.selectNpc(npcId);
                    editor.loadNpc(npcId);
                }
        );
        JPanel content = new JPanel(new BorderLayout());
        content.add(list, BorderLayout.WEST);
        content.add(editor, BorderLayout.CENTER);
        add(content);
    }

    private void mutateNpc(NpcListPanel list, NpcEditorPanel editor, int npcId, String messagePrefix) {
        list.refreshData();
        if (!list.selectNpc(npcId)) {
            editor.loadNpc(npcId);
        }
        JOptionPane.showMessageDialog(this, messagePrefix + npcId + ".");
    }
}
