package com.desecratedtree.quill;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.ui.MainFrame;
import com.desecratedtree.quill.util.RuntimeRevision;
import com.desecratedtree.quill.util.Utilities;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        RuntimeRevision.configure();
        configureLookAndFeel();
        CacheManager.initCache(null);
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        FlatMacDarkLaf.setup();
    }
}
