package com.desecratedtree.quill.ui.npc;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.NpcDefinitions;
import com.desecratedtree.quill.ui.component.SimpleDocumentListener;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class NpcListPanel extends JPanel {

    private int[] allIds;

    private String[] allNames;

    private final NpcTableModel tableModel = new NpcTableModel();

    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = new JTextField();

    private Consumer<Integer> listener;

    private Runnable addAction;

    private IntConsumer duplicateAction;

    private IntConsumer deleteAction;

    public NpcListPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 600));
        reloadData();
        add(searchField, BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        table.setShowGrid(false);
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer leftAlign = new DefaultTableCellRenderer();
        leftAlign.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftAlign);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        tableModel.setData(allIds);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null && table.getSelectedRow() >= 0) {
                listener.accept(tableModel.getIdAt(table.getSelectedRow()));
            }
        });
        table.addMouseListener(new MouseAdapter() {

            @Override

            public void mousePressed(MouseEvent e) {
                showContextMenu(e);
            }

            @Override

            public void mouseReleased(MouseEvent e) {
                showContextMenu(e);
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        searchField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(() -> filter(searchField.getText())));
    }

    public void setListener(Consumer<Integer> listener) {
        this.listener = listener;
    }

    public void setContextActions(Runnable addAction, IntConsumer duplicateAction, IntConsumer deleteAction) {
        this.addAction = addAction;
        this.duplicateAction = duplicateAction;
        this.deleteAction = deleteAction;
    }

    public void refreshData() {
        reloadData();
        filter(searchField.getText());
    }

    public boolean selectNpc(int id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.getIdAt(row) == id) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                return true;
            }
        }
        return false;
    }

    private void reloadData() {
        int count = CacheManager.getNpcCount();
        allIds = new int[count];
        allNames = new String[count];
        for (int i = 0; i < count; i++) {
            allIds[i] = i;
            NpcDefinitions definition = NpcDefinitions.get(i);
            allNames[i] = definition == null || definition.name == null ? "" : definition.name;
        }
    }

    private void filter(String text) {
        if (text == null || text.trim().isEmpty()) {
            tableModel.setData(allIds);
            return;
        }
        String query = text.trim().toLowerCase();
        List<Integer> ids = new ArrayList<>();
        try {
            int id = Integer.parseInt(query);
            if (id >= 0 && id < allIds.length) {
                ids.add(id);
            }
        } catch (NumberFormatException ignored) {
        }
        for (int id : allIds) {
            if (allNames[id].toLowerCase().contains(query) && !ids.contains(id)) {
                ids.add(id);
            }
        }
        tableModel.setData(ids.stream().mapToInt(Integer::intValue).toArray());
    }

    private void showContextMenu(MouseEvent event) {
        if (!event.isPopupTrigger()) {
            return;
        }
        int row = table.rowAtPoint(event.getPoint());
        if (row >= 0) {
            table.setRowSelectionInterval(row, row);
        }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add");
        addItem.addActionListener(e -> {
            if (addAction != null) {
                addAction.run();
            }
        });
        menu.add(addItem);
        if (row >= 0) {
            int npcId = tableModel.getIdAt(row);
            JMenuItem duplicateItem = new JMenuItem("Duplicate");
            duplicateItem.addActionListener(e -> {
                if (duplicateAction != null) {
                    duplicateAction.accept(npcId);
                }
            });
            menu.add(duplicateItem);
            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        "Reset NPC " + npcId + " to a blank default definition?",
                        "Delete NPC",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION && deleteAction != null) {
                    deleteAction.accept(npcId);
                }
            });
            menu.add(deleteItem);
        }
        menu.show(event.getComponent(), event.getX(), event.getY());
    }

    private final class NpcTableModel extends AbstractTableModel {

        private int[] visibleIds = new int[0];

        private void setData(int[] ids) {
            visibleIds = ids;
            fireTableDataChanged();
        }

        private int getIdAt(int row) {
            return visibleIds[row];
        }

        @Override

        public int getRowCount() {
            return visibleIds.length;
        }

        @Override

        public int getColumnCount() {
            return 2;
        }

        @Override

        public String getColumnName(int column) {
            return column == 0 ? "ID" : "Name";
        }

        @Override

        public Object getValueAt(int rowIndex, int columnIndex) {
            int id = visibleIds[rowIndex];
            return columnIndex == 0 ? id : (allNames[id] == null || allNames[id].trim().isEmpty() ? "Unknown" : allNames[id]);
        }
    }
}
