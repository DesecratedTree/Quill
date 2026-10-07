package com.desecratedtree.quill.ui;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.tools.ItemDefsDumpMain;
import com.desecratedtree.quill.tools.ItemSpriteDumpMain;
import com.desecratedtree.quill.tools.SpriteDumpMain;
import com.desecratedtree.quill.tools.TextureDumpMain;
import com.desecratedtree.quill.ui.clientscript.ClientScriptEditorFrame;
import com.desecratedtree.quill.ui.item.ItemEditorPanel;
import com.desecratedtree.quill.ui.item.ItemListPanel;
import com.desecratedtree.quill.ui.npc.NpcEditorPanel;
import com.desecratedtree.quill.ui.npc.NpcListPanel;
import com.desecratedtree.quill.ui.object.ObjectEditorPanel;
import com.desecratedtree.quill.ui.object.ObjectListPanel;
import com.desecratedtree.quill.util.ProjectPaths;
import com.desecratedtree.quill.util.RuntimeRevision;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final Dimension DEFAULT_SIZE = new Dimension(1280, 820);
    private static final Path CHANGELOG_FILE = Paths.get("changelog.md");
    private static final String CHANGELOG_FALLBACK = "## Changelog\n\n- Add updates in `changelog.md`.\n";

    private final JTabbedPane workspace = new JTabbedPane();
    private final JLabel status = UiStyles.muted("Ready");
    private final JLabel cacheStatus = UiStyles.muted("Cache ready");

    public MainFrame() {
        setTitle("Quill");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(DEFAULT_SIZE);
        setLocationRelativeTo(null);
        setContentPane(buildShell());
        setJMenuBar(createMenuBar());
        openHomeTab();
    }

    private JComponent buildShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        UiStyles.styleRoot(root);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildWorkspace(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 18));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(24, 18, 18, 18));
        sidebar.setBackground(UiStyles.SURFACE);

        JPanel brand = new JPanel(new BorderLayout(0, 4));
        brand.setOpaque(false);
        JLabel mark = new JLabel("Q");
        mark.setForeground(UiStyles.ACCENT);
        mark.setFont(mark.getFont().deriveFont(Font.BOLD, 30f));
        brand.add(mark, BorderLayout.WEST);
        JPanel names = new JPanel(new GridLayout(2, 1));
        names.setOpaque(false);
        JLabel name = new JLabel("QUILL");
        name.setFont(name.getFont().deriveFont(Font.BOLD, 16f));
        names.add(name);
        JLabel subtitle = UiStyles.muted("Cache workspace");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11f));
        names.add(subtitle);
        brand.add(names, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.add(UiStyles.eyebrow("Workspace"));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Overview", this::openHomeTab, true));
        nav.add(Box.createVerticalStrut(4));
        nav.add(UiStyles.eyebrow("Editors"));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Items", () -> openItemEditorTab(), false));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navButton("NPCs", () -> openNpcEditorTab(), false));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navButton("Objects", () -> openObjectEditorTab(), false));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navButton("ClientScripts", this::openClientScriptEditor, false));
        nav.add(Box.createVerticalStrut(14));
        nav.add(UiStyles.eyebrow("Assets & export"));
        nav.add(Box.createVerticalStrut(8));
        nav.add(navButton("Models", this::openModelEditor, false));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navButton("Textures", this::openTextureEditor, false));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navButton("Sprites", this::openSpriteEditor, false));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navButton("Dumpers", this::openDumpers, false));
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 5));
        footer.setOpaque(false);
        footer.add(cacheStatus, BorderLayout.NORTH);
        JLabel revision = UiStyles.muted("Revision " + RuntimeRevision.getRevision());
        revision.setFont(revision.getFont().deriveFont(Font.PLAIN, 11f));
        footer.add(revision, BorderLayout.SOUTH);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton navButton(String text, Runnable action, boolean selected) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 12, 9, 12));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.putClientProperty("JButton.buttonType", selected ? "roundRect" : "borderless");
        if (selected) {
            button.setBackground(UiStyles.ACCENT_SOFT);
            button.setForeground(Color.WHITE);
        }
        button.addActionListener(e -> action.run());
        return button;
    }

    private JComponent buildWorkspace() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UiStyles.SURFACE_SUBTLE);
        workspace.setBorder(new EmptyBorder(12, 12, 0, 12));
        workspace.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        center.add(workspace, BorderLayout.CENTER);
        return center;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(7, 18, 7, 18));
        bar.setBackground(UiStyles.SURFACE);
        status.setFont(status.getFont().deriveFont(Font.PLAIN, 11f));
        bar.add(status, BorderLayout.WEST);
        JLabel hint = UiStyles.muted("Ctrl/Cmd+S to save  •  Right-click lists for actions");
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    private void openHomeTab() {
        int existing = tabIndex("Overview");
        if (existing >= 0) {
            workspace.setSelectedIndex(existing);
            return;
        }
        addTab("Overview", buildHome(), false);
    }

    private JComponent buildHome() {
        JPanel page = new JPanel(new BorderLayout(0, 22));
        page.setBorder(new EmptyBorder(28, 28, 28, 28));
        page.setBackground(UiStyles.SURFACE_SUBTLE);

        JPanel hero = new JPanel(new BorderLayout(0, 8));
        hero.setOpaque(false);
        hero.add(UiStyles.eyebrow("Cache workspace"), BorderLayout.NORTH);
        hero.add(UiStyles.title("Shape your cache with confidence."), BorderLayout.CENTER);
        JLabel intro = UiStyles.muted("Browse definitions, inspect models, and export assets from one focused workspace.");
        intro.setFont(intro.getFont().deriveFont(Font.PLAIN, 14f));
        hero.add(intro, BorderLayout.SOUTH);
        page.add(hero, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.add(homeCard("Definitions", "Edit items, NPCs, and objects with live previews.", "Open editor", this::openItemEditorTab));
        cards.add(homeCard("Assets", "Inspect models, textures, and sprites without leaving Quill.", "Browse assets", this::openTextureEditor));
        cards.add(homeCard("Export", "Batch dump definitions and rendered cache assets.", "Open dumpers", this::openDumpers));
        page.add(cards, BorderLayout.NORTH);

        JPanel changelog = new JPanel(new BorderLayout(0, 10));
        UiStyles.styleCard(changelog);
        changelog.add(UiStyles.eyebrow("Recent changes"), BorderLayout.NORTH);
        JTextArea notes = new JTextArea(loadChangelogMarkdown());
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setForeground(UiStyles.TEXT_MUTED);
        notes.setBackground(UiStyles.SURFACE_RAISED);
        notes.setBorder(new EmptyBorder(0, 0, 0, 0));
        changelog.add(new JScrollPane(notes), BorderLayout.CENTER);
        page.add(changelog, BorderLayout.CENTER);
        return page;
    }

    private JPanel homeCard(String heading, String description, String actionText, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        UiStyles.styleCard(card);
        JLabel title = new JLabel(heading);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        card.add(title, BorderLayout.NORTH);
        JLabel body = UiStyles.muted(description);
        body.setVerticalAlignment(SwingConstants.TOP);
        card.add(body, BorderLayout.CENTER);
        JButton actionButton = UiStyles.secondaryButton(actionText + "  →");
        actionButton.setHorizontalAlignment(SwingConstants.LEFT);
        actionButton.addActionListener(e -> action.run());
        card.add(actionButton, BorderLayout.SOUTH);
        return card;
    }

    private void openItemEditorTab() {
        ItemListPanel list = new ItemListPanel();
        ItemEditorPanel editor = new ItemEditorPanel();
        list.setListener(editor::loadItem);
        editor.setSaveListener(savedId -> { list.refreshData(); list.selectItem(savedId); setStatus("Saved item " + savedId); });
        list.setContextActions(
                () -> { int id = com.desecratedtree.quill.cache.DefinitionActions.addItem(); list.refreshData(); editor.loadItem(id); },
                id -> { int next = com.desecratedtree.quill.cache.DefinitionActions.duplicateItem(id); list.refreshData(); editor.loadItem(next); },
                id -> { com.desecratedtree.quill.cache.DefinitionActions.deleteItem(id); list.refreshData(); editor.loadItem(id); }
        );
        JPanel content = editorWorkspace("Item definitions", list, editor);
        addTab("Items", content, true);
    }

    private void openNpcEditorTab() {
        NpcListPanel list = new NpcListPanel();
        NpcEditorPanel editor = new NpcEditorPanel();
        list.setListener(editor::loadNpc);
        editor.setSaveListener(savedId -> { list.refreshData(); list.selectNpc(savedId); setStatus("Saved NPC " + savedId); });
        list.setContextActions(
                () -> { int id = com.desecratedtree.quill.cache.DefinitionActions.addNpc(); list.refreshData(); editor.loadNpc(id); },
                id -> { int next = com.desecratedtree.quill.cache.DefinitionActions.duplicateNpc(id); list.refreshData(); editor.loadNpc(next); },
                id -> { com.desecratedtree.quill.cache.DefinitionActions.deleteNpc(id); list.refreshData(); editor.loadNpc(id); }
        );
        addTab("NPCs", editorWorkspace("NPC definitions", list, editor), true);
    }

    private void openObjectEditorTab() {
        ObjectListPanel list = new ObjectListPanel();
        ObjectEditorPanel editor = new ObjectEditorPanel();
        list.setListener(editor::loadObject);
        list.setContextActions(
                () -> { int id = com.desecratedtree.quill.cache.DefinitionActions.addObject(); list.refreshData(); editor.loadObject(id); },
                id -> { int next = com.desecratedtree.quill.cache.DefinitionActions.duplicateObject(id); list.refreshData(); editor.loadObject(next); },
                id -> { com.desecratedtree.quill.cache.DefinitionActions.deleteObject(id); list.refreshData(); editor.loadObject(id); }
        );
        addTab("Objects", editorWorkspace("Object definitions", list, editor), true);
    }

    private JPanel editorWorkspace(String title, JComponent list, JComponent editor) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(12, 4, 12, 4));
        panel.setBackground(UiStyles.SURFACE_SUBTLE);
        JPanel listCard = new JPanel(new BorderLayout());
        UiStyles.styleCard(listCard);
        listCard.setPreferredSize(new Dimension(270, 0));
        listCard.add(list, BorderLayout.CENTER);
        panel.add(listCard, BorderLayout.WEST);
        JPanel editorCard = new JPanel(new BorderLayout());
        UiStyles.styleCard(editorCard);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UiStyles.eyebrow(title), BorderLayout.WEST);
        header.add(UiStyles.muted("Select a definition to begin"), BorderLayout.EAST);
        editorCard.add(header, BorderLayout.NORTH);
        editorCard.add(editor, BorderLayout.CENTER);
        panel.add(editorCard, BorderLayout.CENTER);
        return panel;
    }

    private void addTab(String title, JComponent component, boolean closable) {
        int existing = tabIndex(title);
        if (existing >= 0) {
            workspace.setSelectedIndex(existing);
            return;
        }
        workspace.addTab(title, component);
        int index = workspace.getTabCount() - 1;
        workspace.setSelectedIndex(index);
        if (closable) {
            workspace.setTabComponentAt(index, closableTab(title, component));
        }
        setStatus("Opened " + title);
    }

    private JComponent closableTab(String title, Component component) {
        JPanel tab = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tab.setOpaque(false);
        tab.add(new JLabel(title));
        JButton close = new JButton("×");
        close.setBorderPainted(false);
        close.setContentAreaFilled(false);
        close.setMargin(new Insets(0, 2, 0, 2));
        close.addActionListener(e -> workspace.remove(component));
        tab.add(close);
        return tab;
    }

    private int tabIndex(String title) {
        for (int i = 0; i < workspace.getTabCount(); i++) {
            if (title.equals(workspace.getTitleAt(i))) return i;
        }
        return -1;
    }

    private void openModelEditor() {
        int[] ids = CacheManager.getModelIds();
        if (ids == null || ids.length == 0) {
            JOptionPane.showMessageDialog(this, "No models found in cache.", "Model Editor", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ModelEditorDialog.showEditor(this, ids[0], "Model Editor | ID: " + ids[0], () -> setStatus("Model saved"));
    }

    private void openTextureEditor() { new TextureEditorFrame().setVisible(true); }
    private void openSpriteEditor() { new SpriteEditorFrame().setVisible(true); }
    private void openClientScriptEditor() {
        if (RuntimeRevision.getRevision() != 634) {
            JOptionPane.showMessageDialog(this, "CS2 Editor requires cache revision 634.", "ClientScripts", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new ClientScriptEditorFrame().setVisible(true);
    }

    private void openDumpers() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createToolItem("Item definitions", this::openItemDefsDumper));
        menu.add(createToolItem("Model dumper", () -> ModelDumper.show(this)));
        menu.add(createToolItem("Inventory sprites", this::openInventorySpriteDumper));
        menu.add(createToolItem("Sprites", this::openSpriteDumper));
        menu.add(createToolItem("Textures", this::openTextureDumper));
        menu.show(getContentPane(), 245, 180);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.add(createToolItem("Overview", this::openHomeTab));
        file.addSeparator();
        file.add(createToolItem("Close current tab", () -> { int i = workspace.getSelectedIndex(); if (i > 0) workspace.removeTabAt(i); }));
        JMenu tools = new JMenu("Tools");
        tools.add(createToolItem("Item Editor", this::openItemEditorTab));
        tools.add(createToolItem("NPC Editor", this::openNpcEditorTab));
        tools.add(createToolItem("Object Editor", this::openObjectEditorTab));
        tools.add(createToolItem("Model Editor", this::openModelEditor));
        tools.add(createToolItem("Texture Editor", this::openTextureEditor));
        tools.add(createToolItem("Sprite Editor", this::openSpriteEditor));
        tools.add(createToolItem("ClientScript Editor", this::openClientScriptEditor));
        bar.add(file);
        bar.add(tools);
        return bar;
    }

    private JMenuItem createToolItem(String label, Runnable action) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> action.run());
        return item;
    }

    private void setStatus(String text) { status.setText(text); }

    private String loadChangelogMarkdown() {
        try {
            if (Files.isRegularFile(CHANGELOG_FILE)) return new String(Files.readAllBytes(CHANGELOG_FILE), StandardCharsets.UTF_8);
        } catch (IOException ignored) { }
        return CHANGELOG_FALLBACK;
    }

    private void chooseOutputAndRun(String title, String label, PathRunnable action) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path output = chooser.getSelectedFile().toPath();
        runDumper(output, label, () -> action.run(output));
    }

    private void openInventorySpriteDumper() { chooseOutputAndRun("Inventory Sprite Dump Output", "inventory sprites", ItemSpriteDumpMain::dumpTo); }
    private void openItemDefsDumper() { chooseOutputAndRun("Item Definition Dump Output", "item definitions", ItemDefsDumpMain::dumpTo); }
    private void openSpriteDumper() { chooseOutputAndRun("Sprite Dump Output", "sprites", SpriteDumpMain::dumpTo); }
    private void openTextureDumper() { chooseOutputAndRun("Texture Dump Output", "textures", TextureDumpMain::dumpTo); }

    private void runDumper(Path output, String label, ThrowingRunnable action) {
        setStatus("Dumping " + label + "…");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception { action.run(); return null; }
            protected void done() {
                try { get(); setStatus("Dumped " + label); JOptionPane.showMessageDialog(MainFrame.this, "Dumped " + label + "."); }
                catch (Exception ex) { setStatus("Dump failed"); JOptionPane.showMessageDialog(MainFrame.this, "Failed to dump " + label + ": " + ex.getMessage(), "Dumpers", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }

    private interface ThrowingRunnable { void run() throws Exception; }
    private interface PathRunnable { void run(Path output) throws Exception; }
}
