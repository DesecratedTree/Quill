package com.desecratedtree.quill.ui.component;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface

public interface SimpleDocumentListener extends DocumentListener {
    static SimpleDocumentListener onChange(Runnable runnable) {
        return new SimpleDocumentListener() {

            @Override

            public void update(DocumentEvent e) {
                runnable.run();
            }
        };
    }
    void update(DocumentEvent e);

    @Override
    default void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        update(e);
    }
}
