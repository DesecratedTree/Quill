package com.desecratedtree.quill.ui.item;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.defs.ItemDefinitions;
import com.desecratedtree.quill.ui.component.SimpleDocumentListener;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ItemListPanel extends JPanel {

    private static final int DEBOUNCE_DELAY = 150;

    private int totalItems;

    private String[] allNames;

    private int[] allIds;

    private final ItemTableModel tableModel = new ItemTableModel();

    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = new JTextField();

    private Consumer<Integer> listener;

    private Runnable addAction;

    private IntConsumer duplicateAction;

    private IntConsumer deleteAction;

    private SwingWorker<Void, int[]> searchWorker;

    private Timer debounceTimer = new Timer("SearchDebounce", true);

    private class ItemTableModel extends AbstractTableModel {

        private int[] visibleIds = new int[0];
        void setData(int[] ids) {
            this.visibleIds = ids;
            fireTableDataChanged();
        }
        int getIdAt(int row) {
            return visibleIds[row];
        }

        @Override public int getRowCount()    { return visibleIds.length; }

        @Override public int getColumnCount() { return 2; }

        @Override

        public String getColumnName(int col) {
            return col == 0 ? "ID" : "Name";
        }

        @Override

        public Object getValueAt(int row, int col) {
            int id = visibleIds[row];
            switch (col) {
                case 0:
                    return id;
                case 1:
                    return (allNames[id] == null || allNames[id].isEmpty()) ? "Unknown" : allNames[id];
                default:
                    return "";
            }
        }

        @Override

        public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class;
        }
    }

    public ItemListPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 600));
        reloadData();
        searchField.setToolTipText("Search by name or ID");
        add(searchField, BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(20);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer leftAlign = new DefaultTableCellRenderer();
        leftAlign.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(0).setCellRenderer(leftAlign);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        showAll();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listener != null) {
                int row = table.getSelectedRow();
                if (row >= 0)
                    listener.accept(tableModel.getIdAt(row));
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
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            private void trigger() {
                debounceTimer.cancel();
                debounceTimer = new Timer("SearchDebounce", true);
                debounceTimer.schedule(new TimerTask() {

                    @Override

                    public void run() {
                        SwingUtilities.invokeLater(() -> filter(searchField.getText()));
                    }
                }, DEBOUNCE_DELAY);
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e)  { trigger(); }

            public void removeUpdate(javax.swing.event.DocumentEvent e)  { trigger(); }

            public void changedUpdate(javax.swing.event.DocumentEvent e) { trigger(); }
        });
    }

    public void refreshData() {
        reloadData();
        filter(searchField.getText());
    }

    private void preloadNames() {
        for (int i = 0; i < totalItems; i++) {
            allIds[i] = i;
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(i);
            allNames[i] = (def != null && def.getName() != null) ? def.getName() : "";
        }
    }

    private void reloadData() {
        totalItems = CacheManager.getItemCount();
        allNames = new String[totalItems];
        allIds = new int[totalItems];
        preloadNames();
    }

    private void showAll() {
        tableModel.setData(allIds);
    }

    private void filter(String text) {
        if (searchWorker != null && !searchWorker.isDone())
            searchWorker.cancel(true);
        if (text == null || text.isEmpty()) {
            showAll();
            return;
        }
        final String query = text.trim().toLowerCase();
        searchWorker = new SwingWorker<Void, int[]>() {

            private static final int CHUNK = 500;

            private final List<Integer> results = new ArrayList<>();

            @Override

            protected Void doInBackground() {
                try {
                    int exactId = Integer.parseInt(query);
                    if (exactId >= 0 && exactId < totalItems)
                        results.add(exactId);
                } catch (NumberFormatException ignored) {}
                for (int i = 0; i < totalItems; i++) {
                    if (isCancelled()) return null;
                    String name = allNames[i].toLowerCase();
                    if (name.contains(query) && !results.contains(i))
                        results.add(i);
                    if (results.size() % CHUNK == 0 && results.size() > 0) {
                        publish(results.stream().mapToInt(Integer::intValue).toArray());
                        results.clear();
                    }
                }
                if (!results.isEmpty() && !isCancelled())
                    publish(results.stream().mapToInt(Integer::intValue).toArray());
                return null;
            }

            @Override

            protected void process(List<int[]> chunks) {
                int currentCount = tableModel.getRowCount();
                int extra = chunks.stream().mapToInt(c -> c.length).sum();
                int[] merged = new int[currentCount + extra];
                for (int i = 0; i < currentCount; i++)
                    merged[i] = tableModel.getIdAt(i);
                int pos = currentCount;
                for (int[] chunk : chunks)
                    for (int id : chunk)
                        merged[pos++] = id;
                tableModel.setData(merged);
            }

            @Override

            protected void done() {
                if (!isCancelled() && tableModel.getRowCount() > 0)
                    table.setRowSelectionInterval(0, 0);
            }
        };
        tableModel.setData(new int[0]);
        searchWorker.execute();
    }

    public void setListener(Consumer<Integer> listener) {
        this.listener = listener;
    }

    public void setContextActions(Runnable addAction, IntConsumer duplicateAction, IntConsumer deleteAction) {
        this.addAction = addAction;
        this.duplicateAction = duplicateAction;
        this.deleteAction = deleteAction;
    }

    public boolean selectItem(int id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (tableModel.getIdAt(row) == id) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                return true;
            }
        }
        return false;
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
            int itemId = tableModel.getIdAt(row);
            JMenuItem duplicateItem = new JMenuItem("Duplicate");
            duplicateItem.addActionListener(e -> {
                if (duplicateAction != null) {
                    duplicateAction.accept(itemId);
                }
            });
            menu.add(duplicateItem);
            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        "Reset item " + itemId + " to a blank default definition?",
                        "Delete Item",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION && deleteAction != null) {
                    deleteAction.accept(itemId);
                }
            });
            menu.add(deleteItem);
        }
        menu.show(event.getComponent(), event.getX(), event.getY());
    }
}
