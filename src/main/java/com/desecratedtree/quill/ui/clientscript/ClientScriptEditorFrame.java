package com.desecratedtree.quill.ui.clientscript;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.ui.component.SimpleDocumentListener;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClientScriptEditorFrame extends JFrame {

    private final ScriptTableModel tableModel = new ScriptTableModel();

    private final JTable table = new JTable(tableModel);

    private final ClientScriptPanel panel = new ClientScriptPanel();

    private final JTextField filterField = new JTextField();

    private final List<ClientScriptRef> allRefs = new ArrayList<>();

    private ClientScriptCs2Service service;

    public ClientScriptEditorFrame() {
        setTitle("Quill - ClientScript Editor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1160, 760);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildListPanel(), panel);
        split.setDividerLocation(280);
        split.setDividerSize(0);
        split.setEnabled(false);
        add(split);
        refreshScripts();
        initializeDecompiler();
    }

    private JComponent buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(buildFilterPanel(), BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                ClientScriptRef ref = tableModel.getAt(table.getSelectedRow());
                this.panel.loadScript(ref);
            }
        });
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            refreshScripts();
            initializeDecompiler();
        });
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(new JLabel("Filter:"), BorderLayout.WEST);
        filterField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::applyFilter));
        panel.add(filterField, BorderLayout.CENTER);
        return panel;
    }

    private void refreshScripts() {
        allRefs.clear();
        int[] archives = CacheManager.getArchiveIds(12);
        Arrays.sort(archives);
        for (int archiveId : archives) {
            allRefs.add(new ClientScriptRef(archiveId, -1));
        }
        applyFilter();
    }

    private void initializeDecompiler() {
        table.setEnabled(false);
        filterField.setEnabled(false);
        panel.showInitializationProgress("If it looks frozen, let all the scripts decompile.", 0, Math.max(1, allRefs.size()));
        new SwingWorker<ClientScriptCs2Service, Void>() {

            @Override

            protected ClientScriptCs2Service doInBackground() {
                ClientScriptCs2Service built = new ClientScriptCs2Service();
                built.initialize(allRefs, (status, completed, total) ->
                        SwingUtilities.invokeLater(() -> panel.showInitializationProgress(status, completed, total)));
                return built;
            }

            @Override

            protected void done() {
                try {
                    service = get();
                    panel.setService(service);
                    panel.hideInitializationProgress();
                    table.setEnabled(true);
                    filterField.setEnabled(true);
                    applyFilter();
                    if (tableModel.getRowCount() > 0) {
                        table.setRowSelectionInterval(0, 0);
                    } else {
                        panel.clear();
                    }
                } catch (Exception ex) {
                    panel.showMessage("Failed to initialize CS2 tooling: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String filter = filterField.getText();
        List<ClientScriptRef> filtered = new ArrayList<>();
        if (filter == null || filter.trim().isEmpty()) {
            filtered.addAll(allRefs);
        } else {
            for (ClientScriptRef ref : allRefs) {
                if (service == null) {
                    if (ref.display().toLowerCase().contains(filter.trim().toLowerCase())) {
                        filtered.add(ref);
                    }
                } else if (service.matchesSearch(ref, filter)) {
                    filtered.add(ref);
                }
            }
        }
        tableModel.setData(filtered);
        if (!filtered.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        } else {
            panel.clear();
        }
    }

    private static final class ScriptTableModel extends AbstractTableModel {

        private List<ClientScriptRef> refs = new ArrayList<>();

        private void setData(List<ClientScriptRef> refs) {
            this.refs = refs;
            fireTableDataChanged();
        }

        private ClientScriptRef getAt(int row) {
            return refs.get(row);
        }

        @Override

        public int getRowCount() {
            return refs.size();
        }

        @Override

        public int getColumnCount() {
            return 1;
        }

        @Override

        public String getColumnName(int column) {
            return "Script";
        }

        @Override

        public Object getValueAt(int rowIndex, int columnIndex) {
            return refs.get(rowIndex).display();
        }
    }
}
