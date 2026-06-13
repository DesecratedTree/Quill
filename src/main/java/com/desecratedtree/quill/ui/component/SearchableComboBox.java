package com.desecratedtree.quill.ui.component;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SearchableComboBox {

    private static final String LISTENER_PROPERTY = "quill.searchable.listener";

    private static final String FILTERING_PROPERTY = "quill.searchable.filtering";

    private SearchableComboBox() {
    }

    public static <T> void install(JComboBox<T> box, List<T> choices) {
        box.setEditable(true);
        box.setMaximumRowCount(30);
        JTextComponent editor = editor(box);
        Object existing = box.getClientProperty(LISTENER_PROPERTY);
        if (existing instanceof DocumentListener) {
            DocumentListener listener = (DocumentListener) existing;
            editor.getDocument().removeDocumentListener(listener);
        }
        List<T> allChoices = new ArrayList<>(choices);
        DocumentListener listener = SimpleDocumentListener.onChange(() -> filterLater(box, allChoices));
        editor.getDocument().addDocumentListener(listener);
        box.putClientProperty(LISTENER_PROPERTY, listener);
    }

    public static boolean isFiltering(JComboBox<?> box) {
        return Boolean.TRUE.equals(box.getClientProperty(FILTERING_PROPERTY));
    }

    private static <T> void filterLater(JComboBox<T> box, List<T> choices) {
        if (isFiltering(box)) {
            return;
        }
        SwingUtilities.invokeLater(() -> filter(box, choices));
    }

    private static <T> void filter(JComboBox<T> box, List<T> choices) {
        JTextComponent editor = editor(box);
        String text = editor.getText();
        String query = text.toLowerCase(Locale.ROOT);
        int caret = editor.getCaretPosition();
        box.putClientProperty(FILTERING_PROPERTY, Boolean.TRUE);
        try {
            box.removeAllItems();
            for (T choice : choices) {
                if (query.trim().isEmpty() || choice.toString().toLowerCase(Locale.ROOT).contains(query)) {
                    box.addItem(choice);
                }
            }
            editor.setText(text);
            editor.setCaretPosition(Math.min(caret, text.length()));
            if (editor.hasFocus() && box.getItemCount() > 0) {
                box.showPopup();
            }
        } finally {
            box.putClientProperty(FILTERING_PROPERTY, Boolean.FALSE);
        }
    }

    private static JTextComponent editor(JComboBox<?> box) {
        return (JTextComponent) box.getEditor().getEditorComponent();
    }
}
