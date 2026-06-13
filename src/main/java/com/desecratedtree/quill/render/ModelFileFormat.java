package com.desecratedtree.quill.render;

public enum ModelFileFormat {
    DAT("dat", ".dat"),
    MQO("mqo", ".mqo");

    private final String label;

    private final String extension;
    ModelFileFormat(String label, String extension) {
        this.label = label;
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }

    @Override

    public String toString() {
        return label;
    }
}
