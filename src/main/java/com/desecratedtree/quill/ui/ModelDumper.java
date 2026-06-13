package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.render.ModelExporter;
import com.desecratedtree.quill.render.ModelFileFormat;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CancellationException;

final class ModelDumper {

    private ModelDumper() {
    }
    static void show(Component parent) {
        JComboBox<ModelFileFormat> fileTypeBox = new JComboBox<>(ModelFileFormat.values());
        JPanel options = new JPanel(new GridLayout(0, 2, 6, 6));
        options.add(new JLabel("File type:"));
        options.add(fileTypeBox);
        if (JOptionPane.showConfirmDialog(
                parent,
                options,
                "Model Dumper",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }
        ModelFileFormat fileType = (ModelFileFormat) fileTypeBox.getSelectedItem();
        if (fileType == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose Model Dump Folder");
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        dump(parent, chooser.getSelectedFile().toPath(), fileType);
    }

    private static void dump(Component parent, Path folder, ModelFileFormat fileType) {
        int[] ids = CacheManager.getModelIds();
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Model Dumper", Dialog.ModalityType.APPLICATION_MODAL);
        JProgressBar progress = new JProgressBar(0, Math.max(1, ids.length));
        JLabel status = new JLabel("Preparing dump...");
        JButton cancel = new JButton("Cancel");
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(status, BorderLayout.NORTH);
        content.add(progress, BorderLayout.CENTER);
        content.add(cancel, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setSize(420, 125);
        dialog.setLocationRelativeTo(parent);
        SwingWorker<DumpResult, DumpProgress> worker = new SwingWorker<DumpResult, DumpProgress>() {

            @Override
            protected DumpResult doInBackground() throws Exception {
                Files.createDirectories(folder);
                int dumped = 0;
                int failed = 0;
                for (int i = 0; i < ids.length; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    int id = ids[i];
                    try {
                        byte[] data = CacheManager.getModelData(id);
                        if (data == null || data.length == 0) {
                            failed++;
                        } else {
                            byte[] exported = ModelExporter.export(id, data, fileType);
                            Files.write(folder.resolve(id + fileType.extension()), exported);
                            dumped++;
                        }
                    } catch (RuntimeException | IOException ex) {
                        failed++;
                    }
                    publish(new DumpProgress(i + 1, id));
                    setProgress((int) Math.round(((i + 1) * 100.0) / Math.max(1, ids.length)));
                }
                return new DumpResult(dumped, failed, isCancelled());
            }

            @Override
            protected void process(List<DumpProgress> chunks) {
                DumpProgress latest = chunks.get(chunks.size() - 1);
                progress.setValue(latest.done);
                status.setText("Dumping model " + latest.modelId + " (" + latest.done + " / " + ids.length + ")");
            }

            @Override
            protected void done() {
                dialog.dispose();
                try {
                    DumpResult result = get();
                    String message = result.cancelled
                            ? "Model dump cancelled. Dumped " + result.dumped + " models; failed/skipped " + result.failed + "."
                            : "Model dump complete. Dumped " + result.dumped + " models; failed/skipped " + result.failed + ".";
                    JOptionPane.showMessageDialog(parent, message, "Model Dumper", JOptionPane.INFORMATION_MESSAGE);
                } catch (CancellationException ex) {
                    JOptionPane.showMessageDialog(parent, "Model dump cancelled.", "Model Dumper", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "Model dump failed: " + ex.getMessage(),
                            "Model Dumper", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        cancel.addActionListener(e -> worker.cancel(true));
        worker.execute();
        dialog.setVisible(true);
    }

    private static final class DumpProgress {

        private final int done;

        private final int modelId;

        private DumpProgress(int done, int modelId) {
            this.done = done;
            this.modelId = modelId;
        }
    }

    private static final class DumpResult {

        private final int dumped;

        private final int failed;

        private final boolean cancelled;

        private DumpResult(int dumped, int failed, boolean cancelled) {
            this.dumped = dumped;
            this.failed = failed;
            this.cancelled = cancelled;
        }
    }
}
