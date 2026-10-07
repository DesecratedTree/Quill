package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.tools.ItemDefsDumpMain;
import com.desecratedtree.quill.tools.ItemSpriteDumpMain;
import com.desecratedtree.quill.tools.SpriteDumpMain;
import com.desecratedtree.quill.tools.TextureDumpMain;
import com.desecratedtree.quill.ui.clientscript.ClientScriptEditorFrame;
import com.desecratedtree.quill.ui.item.ItemEditorFrame;
import com.desecratedtree.quill.ui.npc.NpcEditorFrame;
import com.desecratedtree.quill.ui.object.ObjectEditorFrame;
import com.desecratedtree.quill.util.ProjectPaths;
import com.desecratedtree.quill.util.RuntimeRevision;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private static final Dimension DEFAULT_SIZE = new Dimension(850, 525);

    private static final Path CHANGELOG_FILE = Paths.get("changelog.md");

    private static final String CHANGELOG_FALLBACK =
            "## Changelog\n\n"
                    + "- Add updates in `changelog.md`.\n";

    public MainFrame() {
        setTitle("Quill Cache Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_SIZE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setContentPane(buildDashboard());
        setJMenuBar(createMenuBar());
    }

    private JPanel buildDashboard() {
        JPanel root = new JPanel(new BorderLayout(24, 24));
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.setBackground(UIManager.getColor("Panel.background"));
        JPanel hero = new JPanel(new BorderLayout(0, 10));
        hero.setOpaque(false);
        JLabel title = new JLabel("Quill Cache Suite");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        JLabel subtitle = new JLabel("All-in-one cache editor.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 15f));
        hero.add(title, BorderLayout.NORTH);
        JPanel heroBody = new JPanel(new BorderLayout(0, 6));
        heroBody.setOpaque(false);
        heroBody.add(subtitle, BorderLayout.NORTH);
        hero.add(heroBody, BorderLayout.CENTER);
        root.add(hero, BorderLayout.NORTH);
        JPanel content = new JPanel(new GridLayout(1, 3, 20, 0));
        content.setOpaque(false);
        JButton cs2Button = createLaunchButton("ClientScript Editor", this::openClientScriptEditor);
        if (RuntimeRevision.getRevision() != 634) {
            cs2Button.setEnabled(false);
            cs2Button.setToolTipText("CS2 Editor requires cache revision 634");
        }
        content.add(createSectionPane("Editors",
                createLaunchButton("Item Editor", this::openItemEditor),
                createLaunchButton("NPC Editor", this::openNpcEditor),
                createLaunchButton("Object Editor", this::openObjectEditor),
                createLaunchButton("Model Editor", this::openModelEditor),
                cs2Button,
                createLaunchButton("Texture Editor", this::openTextureEditor),
                createLaunchButton("Sprite Editor", this::openSpriteEditor)));
        content.add(createSectionPane("Dumpers",
                createLaunchButton("Item Defs Dumper", this::openItemDefsDumper),
                createLaunchButton("Model Dumper", () -> ModelDumper.show(this)),
                createLaunchButton("Inventory Sprite Dumper", this::openInventorySpriteDumper),
                createLaunchButton("Sprite Dumper", this::openSpriteDumper),
                createLaunchButton("Texture Dumper", this::openTextureDumper)));
        content.add(createChangelogPane());
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JPanel createSectionPane(String title, JButton... buttons) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(createSectionHeader(title), BorderLayout.NORTH);
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        for (JButton button : buttons) {
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
            actions.add(button);
            actions.add(Box.createVerticalStrut(6));
        }
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(actions, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createSectionHeader(String title) {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18f));
        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(0, 4));
        Color separator = UIManager.getColor("Separator.foreground");
        accentBar.setBackground(separator != null ? separator : heading.getForeground());
        header.add(heading, BorderLayout.NORTH);
        header.add(accentBar, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createChangelogPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(createSectionHeader("Changelog"), BorderLayout.NORTH);
        JEditorPane notes = new JEditorPane();
        notes.setEditorKit(new HTMLEditorKit());
        notes.setContentType("text/html");
        notes.setEditable(false);
        notes.setFocusable(false);
        notes.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        Font baseFont = uiBaseFont();
        notes.setFont(baseFont.deriveFont(Font.PLAIN, 11f));
        notes.setText(markdownToHtml(loadChangelogMarkdown()));
        notes.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(notes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton createLaunchButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setMargin(new Insets(4, 8, 4, 8));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
        button.addActionListener(e -> action.run());
        return button;
    }

    private String markdownToHtml(String markdown) {
        Font baseFont = uiBaseFont();
        StringBuilder html = new StringBuilder("<html><body style='font-family:")
                .append("'").append(baseFont.getFamily()).append("'")
                .append("; font-size: 11pt; margin: 0;'>");
        List<String> paragraph = new ArrayList<>();
        for (String rawLine : markdown.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (!paragraph.isEmpty()) {
                    html.append("<p>").append(escapeHtml(String.join(" ", paragraph))).append("</p>");
                    paragraph.clear();
                }
                continue;
            }
            if (line.startsWith("### ")) {
                flushParagraph(html, paragraph);
                html.append("<h3>").append(escapeHtml(line.substring(4))).append("</h3>");
                continue;
            }
            if (line.startsWith("## ")) {
                flushParagraph(html, paragraph);
                html.append("<h2>").append(escapeHtml(line.substring(3))).append("</h2>");
                continue;
            }
            if (line.startsWith("- ")) {
                flushParagraph(html, paragraph);
                html.append("<div style='margin: 0 0 2px 0;'>- ")
                        .append(escapeHtml(line.substring(2)))
                        .append("</div>");
                continue;
            }
            paragraph.add(line);
        }
        flushParagraph(html, paragraph);
        html.append("</body></html>");
        return html.toString();
    }

    private void flushParagraph(StringBuilder html, List<String> paragraph) {
        if (!paragraph.isEmpty()) {
            html.append("<p>").append(escapeHtml(String.join(" ", paragraph))).append("</p>");
            paragraph.clear();
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String loadChangelogMarkdown() {
        try {
            InputStream resource = MainFrame.class.getClassLoader().getResourceAsStream("changelog.md");
            if (resource != null) {
                try {
                    byte[] bytes = readAllBytes(resource);
                    return new String(bytes, StandardCharsets.UTF_8);
                } finally {
                    resource.close();
                }
            }
            if (Files.isRegularFile(CHANGELOG_FILE)) {
                return new String(Files.readAllBytes(CHANGELOG_FILE), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
        return CHANGELOG_FALLBACK;
    }

    private byte[] readAllBytes(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private Font uiBaseFont() {
        Font font = UIManager.getFont("defaultFont");
        if (font == null) {
            font = UIManager.getFont("Label.font");
        }
        return font != null ? font : new JLabel().getFont();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu tools = new JMenu("Tools");
        tools.add(createToolItem("Item Editor", this::openItemEditor));
        tools.add(createToolItem("NPC Editor", this::openNpcEditor));
        tools.add(createToolItem("Object Editor", this::openObjectEditor));
        tools.add(createToolItem("Model Editor", this::openModelEditor));
        JMenuItem cs2MenuItem = createToolItem("ClientScript Editor", this::openClientScriptEditor);
        if (RuntimeRevision.getRevision() != 634) {
            cs2MenuItem.setEnabled(false);
            cs2MenuItem.setToolTipText("CS2 Editor requires cache revision 634");
        }
        tools.add(cs2MenuItem);
        tools.add(createToolItem("Texture Editor", this::openTextureEditor));
        tools.add(createToolItem("Sprite Editor", this::openSpriteEditor));
        tools.addSeparator();
        JMenu dumpers = new JMenu("Dumpers");
        dumpers.add(createToolItem("Item Defs Dumper", this::openItemDefsDumper));
        dumpers.add(createToolItem("Model Dumper", () -> ModelDumper.show(this)));
        dumpers.add(createToolItem("Inventory Sprite Dumper", this::openInventorySpriteDumper));
        dumpers.add(createToolItem("Sprite Dumper", this::openSpriteDumper));
        dumpers.add(createToolItem("Texture Dumper", this::openTextureDumper));
        tools.add(dumpers);
        bar.add(tools);
        return bar;
    }

    private JMenuItem createToolItem(String label, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> action.run());
        return item;
    }

    private void openItemEditor() {
        ItemEditorFrame itemEditor = new ItemEditorFrame();
        itemEditor.setVisible(true);
    }

    private void openNpcEditor() {
        NpcEditorFrame npcEditor = new NpcEditorFrame();
        npcEditor.setVisible(true);
    }

    private void openObjectEditor() {
        ObjectEditorFrame objectEditor = new ObjectEditorFrame();
        objectEditor.setVisible(true);
    }

    private void openModelEditor() {
        int[] modelIds = CacheManager.getModelIds();
        if (modelIds == null || modelIds.length == 0) {
            JOptionPane.showMessageDialog(this, "No models found in cache.", "Model Editor", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int modelId = modelIds[0];
        ModelEditorDialog.showEditor(this, modelId, "Model Editor | ID: " + modelId, null);
    }

    private void openTextureEditor() {
        TextureEditorFrame frame = new TextureEditorFrame();
        frame.setVisible(true);
    }

    private void openClientScriptEditor() {
        ClientScriptEditorFrame frame = new ClientScriptEditorFrame();
        frame.setVisible(true);
    }

    private void openSpriteEditor() {
        SpriteEditorFrame frame = new SpriteEditorFrame();
        frame.setVisible(true);
    }

    private void openInventorySpriteDumper() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Inventory Sprite Dump Output");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(ProjectPaths.itemSpriteDumpDir().toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = chooser.getSelectedFile().toPath();
        runDumper(output, "inventory sprites", () -> ItemSpriteDumpMain.dumpTo(output));
    }

    private void openItemDefsDumper() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Item Def Dump Output");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(ProjectPaths.itemDefsDumpDir().toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = chooser.getSelectedFile().toPath();
        runDumper(output, "item defs", () -> ItemDefsDumpMain.dumpTo(output));
    }

    private void openSpriteDumper() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sprite Dump Output");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(ProjectPaths.spriteDumpDir().toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = chooser.getSelectedFile().toPath();
        runDumper(output, "sprites", () -> SpriteDumpMain.dumpTo(output));
    }

    private void openTextureDumper() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Texture Dump Output");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(ProjectPaths.texturePreviewDumpDir().toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = chooser.getSelectedFile().toPath();
        runDumper(output, "textures", () -> TextureDumpMain.dumpTo(output));
    }

    private void runDumper(Path output, String label, ThrowingRunnable action) {
        new SwingWorker<Void, Void>() {

            @Override

            protected Void doInBackground() throws Exception {
                action.run();
                return null;
            }

            @Override

            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MainFrame.this, "Dumped " + label + " to " + output + ".");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Failed to dump " + label + ": " + cause.getMessage(),
                            "Dumpers", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        CacheManager.initCache(null);
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
