package com.desecratedtree.quill.ui.clientscript;

import com.desecratedtree.quill.cache.CacheManager;
import dawn.cs2.CS2;
import dawn.cs2.CS2Compiler;
import dawn.cs2.CS2Decompiler;
import dawn.cs2.CS2Reader;
import dawn.cs2.CS2ScriptParser;
import dawn.cs2.CS2Type;
import dawn.cs2.CodePrinter;
import dawn.cs2.ast.FunctionNode;
import dawn.cs2.unscramble.UnscrambleUtils;
import dawn.cs2.util.FunctionDatabase;
import dawn.cs2.util.FunctionInfo;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

final class ClientScriptCs2Service {

    private static final int CONFIG_VERSION = 667;

    private static final String OPCODE_DATABASE = "/cs2/opcode/database/rs2_old.ini";

    private final HashMap<Integer, Integer> scrambled = new HashMap<>();

    private final HashMap<Integer, Integer> unscrambled = new HashMap<>();

    private final FunctionDatabase opcodesDatabase;

    private FunctionDatabase scriptsDatabase = new FunctionDatabase();

    private final Map<Integer, String> sourceCache = new ConcurrentHashMap<>();
    ClientScriptCs2Service() {
        UnscrambleUtils.read(CONFIG_VERSION, scrambled, unscrambled);
        opcodesDatabase = new FunctionDatabase(ClientScriptCs2Service.class.getResourceAsStream(OPCODE_DATABASE), false, scrambled);
    }
    void initialize(List<ClientScriptRef> refs, ProgressListener progressListener) {
        scriptsDatabase = generateScriptsDatabase(refs, 6);
        buildSourceCache(refs, progressListener);
    }
    DecompiledScript decompile(ClientScriptRef ref) {
        byte[] data = load(ref);
        ClientScriptDecoder.ScriptInfo summary = ClientScriptDecoder.decode(ref.scriptId(), data);
        if (data == null || data.length == 0) {
            return new DecompiledScript(summary, "", "", "No script data.");
        }
        try {
            CS2 script = readScript(ref, data);
            CS2Decompiler decompiler = new CS2Decompiler(script, opcodesDatabase, scriptsDatabase);
            try {
                decompiler.decompile();
            } catch (Throwable ignored) {
            }
            decompiler.optimize();
            CodePrinter printer = new CodePrinter();
            decompiler.getFunction().print(printer);
            String source = printer.toString();
            sourceCache.put(ref.scriptId(), source);
            return new DecompiledScript(summary, source, formatAssembly(script), null);
        } catch (Throwable t) {
            return new DecompiledScript(summary, "", "", t.toString());
        }
    }
    CompileResult compile(ClientScriptRef ref, String source) {
        try {
            FunctionNode function = CS2ScriptParser.parse(source, opcodesDatabase, scriptsDatabase);
            StringWriter assemblyWriter = new StringWriter();
            CS2Compiler compiler = new CS2Compiler(function, scrambled, false, true);
            byte[] compiled = compiler.compile(new PrintWriter(assemblyWriter));
            updateScriptInfo(ref.scriptId(), function);
            sourceCache.put(ref.scriptId(), source);
            return new CompileResult(compiled, assemblyWriter.toString(), null);
        } catch (Throwable t) {
            return new CompileResult(null, "", t.toString());
        }
    }
    String revisionSummary() {
        return "Using old RS2 CS2 config (switches enabled, longs disabled).\n"
                + "If it looks frozen, let all the scripts decompile.";
    }
    boolean matchesSearch(ClientScriptRef ref, String query) {
        String normalized = normalizeForSearch(query);
        if (normalized.isEmpty()) {
            return true;
        }
        if (ref.display().toLowerCase(Locale.ROOT).contains(normalized)) {
            return true;
        }
        String source = sourceCache.get(ref.scriptId());
        return source != null && normalizeForSearch(source).contains(normalized);
    }

    private byte[] load(ClientScriptRef ref) {
        return ref.fileId < 0
                ? CacheManager.getIndexData(12, ref.archiveId)
                : CacheManager.getIndexData(12, ref.archiveId, ref.fileId);
    }

    private CS2 readScript(ClientScriptRef ref, byte[] data) throws java.io.IOException {
        return CS2Reader.readCS2ScriptNewFormat(data, ref.scriptId(), unscrambled, false, true);
    }

    private FunctionDatabase generateScriptsDatabase(List<ClientScriptRef> refs, int loops) {
        FunctionDatabase database = new FunctionDatabase();
        for (int loop = 0; loop < loops; loop++) {
            for (ClientScriptRef ref : refs) {
                byte[] data = load(ref);
                if (data == null || data.length == 0) {
                    continue;
                }
                try {
                    CS2 script = readScript(ref, data);
                    CS2Decompiler decompiler = new CS2Decompiler(script, opcodesDatabase, database);
                    try {
                        decompiler.decompile();
                    } catch (Throwable ignored) {
                    }
                    FunctionNode function = decompiler.getFunction();
                    if (function.getReturnType() == CS2Type.UNKNOWN) {
                        continue;
                    }
                    updateScriptInfo(database, ref.scriptId(), function, true);
                } catch (Throwable ignored) {
                }
            }
        }
        return database;
    }

    private String formatAssembly(CS2 script) {
        StringBuilder builder = new StringBuilder();
        int width = String.valueOf(script.getInstructions().length).length();
        for (int i = 0; i < script.getInstructions().length; i++) {
            builder.append(String.format("[%0" + width + "d]: %s%n", i, script.getInstructions()[i]));
        }
        return builder.toString();
    }

    private void buildSourceCache(List<ClientScriptRef> refs, ProgressListener progressListener) {
        sourceCache.clear();
        int total = refs.size();
        for (int i = 0; i < refs.size(); i++) {
            ClientScriptRef ref = refs.get(i);
            String status = "Decompiling script " + ref.display() + " (" + (i + 1) + "/" + total + ")";
            if (progressListener != null) {
                progressListener.onProgress(status, i, total);
            }
            try {
                sourceCache.put(ref.scriptId(), decompileSourceOnly(ref));
            } catch (Throwable ignored) {
                sourceCache.put(ref.scriptId(), "");
            }
        }
        if (progressListener != null) {
            progressListener.onProgress("Finished decompiling scripts.", total, total);
        }
    }

    private void updateScriptInfo(int scriptId, FunctionNode function) {
        updateScriptInfo(scriptsDatabase, scriptId, function, false);
    }

    private void updateScriptInfo(FunctionDatabase database, int scriptId, FunctionNode function, boolean keepKnownReturnTypeOnly) {
        FunctionInfo info = database.getInfo(scriptId);
        if (info == null) {
            info = new FunctionInfo(scriptId, function);
            database.putInfo(scriptId, info);
        }
        info.name = function.getName();
        if (!keepKnownReturnTypeOnly || info.returnType == CS2Type.UNKNOWN) {
            info.returnType = function.getReturnType();
        }
        for (int i = 0; i < function.getArgumentLocals().length; i++) {
            info.argumentTypes[i] = function.getArgumentLocals()[i].getType();
            info.argumentNames[i] = function.getArgumentLocals()[i].getName();
        }
    }

    private String decompileSourceOnly(ClientScriptRef ref) throws java.io.IOException {
        byte[] data = load(ref);
        if (data == null || data.length == 0) {
            return "";
        }
        CS2 script = readScript(ref, data);
        CS2Decompiler decompiler = new CS2Decompiler(script, opcodesDatabase, scriptsDatabase);
        try {
            decompiler.decompile();
        } catch (Throwable ignored) {
        }
        decompiler.optimize();
        CodePrinter printer = new CodePrinter();
        decompiler.getFunction().print(printer);
        return printer.toString();
    }

    private String normalizeForSearch(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT);
    }
    interface ProgressListener {
        void onProgress(String status, int completed, int total);
    }
    static final class DecompiledScript {
        final ClientScriptDecoder.ScriptInfo summary;
        final String source;
        final String assembly;
        final String error;
        DecompiledScript(ClientScriptDecoder.ScriptInfo summary, String source, String assembly, String error) {
            this.summary = summary;
            this.source = source;
            this.assembly = assembly;
            this.error = error;
        }
    }
    static final class CompileResult {
        final byte[] bytes;
        final String assembly;
        final String error;
        CompileResult(byte[] bytes, String assembly, String error) {
            this.bytes = bytes;
            this.assembly = assembly;
            this.error = error;
        }
    }
}
