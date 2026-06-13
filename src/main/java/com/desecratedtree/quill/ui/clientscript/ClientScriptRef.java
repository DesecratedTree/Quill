package com.desecratedtree.quill.ui.clientscript;

final class ClientScriptRef {
    final int archiveId;
    final int fileId;
    ClientScriptRef(int archiveId, int fileId) {
        this.archiveId = archiveId;
        this.fileId = fileId;
    }
    int scriptId() {
        return archiveId;
    }
    String display() {
        return String.valueOf(archiveId);
    }
}
