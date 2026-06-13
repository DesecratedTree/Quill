package com.desecratedtree.quill.ui.clientscript;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.OutputStream;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public final class ClientScriptPanel extends JPanel {

    private ClientScriptCs2Service service;

    private ClientScriptRef currentRef;

    private final JTextField scriptRef = new JTextField();

    private final JTextField byteLength = new JTextField();

    private final JTextField scriptName = new JTextField();

    private final JTextField locals = new JTextField();

    private final JTextField arguments = new JTextField();

    private final JTextArea notes = new JTextArea(5, 24);

    private final JTextArea summary = new JTextArea(7, 24);

    private final JTextArea sourceCode = new JTextArea(24, 64);

    private final JTextArea assembly = new JTextArea(24, 64);

    private final JTextArea log = new JTextArea(8, 64);

    private final JTextArea rawHex = new JTextArea(20, 40);

    private final ClientScriptOpcodeTableModel opcodeTableModel = new ClientScriptOpcodeTableModel();

    private final JTable opcodeTable = new JTable(opcodeTableModel);

    private final JButton reload = new JButton("Decompile");

    private final JButton saveSource = new JButton("Save Source");

    private final JButton saveRaw = new JButton("Save Raw Hex");

    private final JProgressBar initializationProgress = new JProgressBar();

    public ClientScriptPanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        configureEditor(rawHex, true);
        configureEditor(sourceCode, false);
        configureEditor(assembly, false);
        configureEditor(summary, false);
        configureEditor(log, false);
        summary.setEditable(false);
        assembly.setEditable(false);
        log.setEditable(false);
        initializationProgress.setStringPainted(true);
        initializationProgress.setVisible(false);
        opcodeTable.setAutoCreateRowSorter(true);
        opcodeTable.setFillsViewportHeight(true);
        opcodeTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reload.addActionListener(e -> {
            if (currentRef != null) {
                loadScript(currentRef);
            }
        });
        saveSource.addActionListener(e -> saveSourceScript());
        saveRaw.addActionListener(e -> saveScript());
        setEditingEnabled(false);
    }

    public void setService(ClientScriptCs2Service service) {
        this.service = service;
        notes.setText(service.revisionSummary());
        setEditingEnabled(true);
    }

    public void loadScript(ClientScriptRef ref) {
        currentRef = ref;
        if (service == null) {
            showMessage("CS2 tooling is still initializing.");
            return;
        }
        showMessage("Decompiling " + ref.display() + "...");
        setEditingEnabled(false);
        new SwingWorker<ClientScriptCs2Service.DecompiledScript, Void>() {

            @Override

            protected ClientScriptCs2Service.DecompiledScript doInBackground() {
                return service.decompile(ref);
            }

            @Override

            protected void done() {
                try {
                    applyDecompiled(ref, get());
                } catch (Exception ex) {
                    showMessage("Failed to decompile " + ref.display() + ": " + ex.getMessage());
                } finally {
                    setEditingEnabled(true);
                }
            }
        }.execute();
    }

    public void saveScript() {
        if (currentRef == null) {
            return;
        }
        byte[] data = parseHex(rawHex.getText());
        writeScript(currentRef, data);
        appendLog("Saved raw hex for " + currentRef.display() + ".");
        loadScript(currentRef);
    }

    public void clear() {
        currentRef = null;
        scriptRef.setText("");
        byteLength.setText("");
        scriptName.setText("");
        locals.setText("");
        arguments.setText("");
        notes.setText("");
        summary.setText("");
        sourceCode.setText("");
        assembly.setText("");
        log.setText("");
        rawHex.setText("");
        opcodeTableModel.setInstructions(Collections.emptyList());
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        fields.add(new JLabel("Script:"), c);
        c.gridx = 1;
        c.weightx = 1;
        scriptRef.setEditable(false);
        fields.add(scriptRef, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        fields.add(new JLabel("Bytes:"), c);
        c.gridx = 1;
        c.weightx = 1;
        byteLength.setEditable(false);
        fields.add(byteLength, c);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        fields.add(new JLabel("Name:"), c);
        c.gridx = 1;
        c.weightx = 1;
        scriptName.setEditable(false);
        fields.add(scriptName, c);
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        fields.add(new JLabel("Locals:"), c);
        c.gridx = 1;
        c.weightx = 1;
        locals.setEditable(false);
        fields.add(locals, c);
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        fields.add(new JLabel("Args:"), c);
        c.gridx = 1;
        c.weightx = 1;
        arguments.setEditable(false);
        fields.add(arguments, c);
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setRows(4);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.add(reload);
        actions.add(saveSource);
        actions.add(saveRaw);
        panel.add(fields, BorderLayout.NORTH);
        panel.add(new JScrollPane(notes), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 6));
        footer.add(actions, BorderLayout.NORTH);
        footer.add(initializationProgress, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildCenter() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Source", new JScrollPane(sourceCode));
        tabs.addTab("Assembly", new JScrollPane(assembly));
        tabs.addTab("Summary", new JScrollPane(summary));
        tabs.addTab("Decoded", new JScrollPane(opcodeTable));
        tabs.addTab("Raw Hex", new JScrollPane(rawHex));
        tabs.addTab("Log", new JScrollPane(log));
        return tabs;
    }

    private String buildNotes(ClientScriptDecoder.ScriptInfo info) {
        if (!info.isValid()) {
            return info.error + "\nRaw hex is still editable and savable.";
        }
        return "Decoded from cache.\n"
                + "Instruction count: " + info.instructionCount + "\n"
                + "Decoded rows: " + info.instructions.size() + "\n"
                + "Switch tables: " + info.switches.size() + "\n"
                + "Long operands are disabled for this revision.\n"
                + "Source tab decompiles and Save Source compiles back to raw cache bytes.";
    }

    private String buildSummary(ClientScriptDecoder.ScriptInfo info) {
        if (!info.isValid()) {
            return info.error == null ? "" : info.error;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("script ").append(info.scriptId);
        if (info.scriptName != null && !info.scriptName.isEmpty()) {
            builder.append(" name=\"").append(info.scriptName).append('"');
        }
        builder.append(System.lineSeparator());
        builder.append("revision=").append(info.revision)
                .append(" bytes=").append(info.byteLength)
                .append(" code_size=").append(info.instructionCount)
                .append(System.lineSeparator());
        builder.append("locals: int=").append(info.intLocals)
                .append(", string=").append(info.stringLocals)
                .append(System.lineSeparator());
        builder.append("args: int=").append(info.intArgs)
                .append(", string=").append(info.stringArgs)
                .append(System.lineSeparator());
        if (info.switches.isEmpty()) {
            builder.append("switches: none");
        } else {
            builder.append("switches:").append(System.lineSeparator());
            for (ClientScriptDecoder.SwitchTable table : info.switches) {
                builder.append("  [").append(table.index).append("] ");
                boolean first = true;
                for (java.util.Map.Entry<Integer, Integer> entry : table.cases.entrySet()) {
                    if (!first) {
                        builder.append(", ");
                    }
                    first = false;
                    builder.append(entry.getKey()).append("->").append(entry.getValue());
                }
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    public void showMessage(String message) {
        notes.setText(message);
    }

    public void showInitializationProgress(String message, int completed, int total) {
        notes.setText(message);
        initializationProgress.setVisible(true);
        initializationProgress.setMinimum(0);
        initializationProgress.setMaximum(Math.max(1, total));
        initializationProgress.setValue(Math.min(completed, total));
        initializationProgress.setString(completed >= total ? "Ready" : completed + " / " + total);
    }

    public void hideInitializationProgress() {
        initializationProgress.setVisible(false);
    }

    private void saveSourceScript() {
        if (currentRef == null || service == null) {
            return;
        }
        showMessage("Compiling " + currentRef.display() + "...");
        setEditingEnabled(false);
        new SwingWorker<ClientScriptCs2Service.CompileResult, Void>() {

            @Override

            protected ClientScriptCs2Service.CompileResult doInBackground() {
                return service.compile(currentRef, sourceCode.getText());
            }

            @Override

            protected void done() {
                try {
                    ClientScriptCs2Service.CompileResult result = get();
                    if (result.error != null) {
                        appendLog(result.error);
                        showMessage("Compile failed for " + currentRef.display() + ".");
                        return;
                    }
                    writeScript(currentRef, result.bytes);
                    assembly.setText(result.assembly);
                    rawHex.setText(toHex(result.bytes));
                    appendLog("Saved source for " + currentRef.display() + ".");
                    loadScript(currentRef);
                } catch (Exception ex) {
                    appendLog(ex.toString());
                    showMessage("Compile failed for " + currentRef.display() + ".");
                } finally {
                    setEditingEnabled(true);
                }
            }
        }.execute();
    }

    private void applyDecompiled(ClientScriptRef ref, ClientScriptCs2Service.DecompiledScript result) {
        currentRef = ref;
        byte[] data = readScript(ref);
        ClientScriptDecoder.ScriptInfo info = result.summary;
        scriptRef.setText(ref.display());
        byteLength.setText(data == null ? "0" : String.valueOf(data.length));
        scriptName.setText(info.scriptName == null ? "" : info.scriptName);
        locals.setText(info.intLocals + " int, " + info.stringLocals + " string");
        arguments.setText(info.intArgs + " int, " + info.stringArgs + " string");
        rawHex.setText(toHex(data));
        sourceCode.setText(result.source);
        assembly.setText(result.assembly);
        notes.setText(buildNotes(info));
        summary.setText(buildSummary(info));
        opcodeTableModel.setInstructions(info.instructions);
        if (result.error != null) {
            appendLog(result.error);
        }
    }

    private void writeScript(ClientScriptRef ref, byte[] data) {
        if (ref.fileId < 0) {
            CacheManager.writeIndexData(12, ref.archiveId, data);
        } else {
            CacheManager.writeIndexData(12, ref.archiveId, ref.fileId, data);
        }
    }

    private byte[] readScript(ClientScriptRef ref) {
        return ref.fileId < 0
                ? CacheManager.getIndexData(12, ref.archiveId)
                : CacheManager.getIndexData(12, ref.archiveId, ref.fileId);
    }

    private void appendLog(String line) {
        log.setText(line + System.lineSeparator() + log.getText());
    }

    private void setEditingEnabled(boolean enabled) {
        sourceCode.setEditable(enabled && service != null);
        rawHex.setEditable(enabled);
        reload.setEnabled(service != null);
        saveSource.setEnabled(enabled && service != null);
        saveRaw.setEnabled(enabled);
    }

    private void configureEditor(JTextArea area, boolean wrap) {
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setLineWrap(wrap);
        area.setWrapStyleWord(wrap);
    }

    private String toHex(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(data.length * 3);
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            int value = data[i] & 0xFF;
            if (value < 16) {
                builder.append('0');
            }
            builder.append(Integer.toHexString(value).toUpperCase());
        }
        return builder.toString();
    }

    private byte[] parseHex(String text) {
        if (text == null) {
            return new byte[0];
        }
        String normalized = text.replaceAll("[^0-9A-Fa-f]", "");
        if (normalized.isEmpty()) {
            return new byte[0];
        }
        if ((normalized.length() & 1) == 1) {
            normalized = "0" + normalized;
        }
        OutputStream out = new OutputStream(normalized.length() / 2);
        for (int i = 0; i < normalized.length(); i += 2) {
            out.writeByte(Integer.parseInt(normalized.substring(i, i + 2), 16));
        }
        return out.toByteArray();
    }
}
