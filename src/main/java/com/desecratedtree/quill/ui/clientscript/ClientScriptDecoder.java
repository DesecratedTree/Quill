package com.desecratedtree.quill.ui.clientscript;

import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.util.RuntimeRevision;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ClientScriptDecoder {

    private static final int OP_PUSH_INT = 0;

    private static final int OP_LOAD_VARP = 1;

    private static final int OP_STORE_VARP = 2;

    private static final int OP_PUSH_STRING = 3;

    private static final int OP_GOTO = 6;

    private static final int OP_INT_NE = 7;

    private static final int OP_INT_EQ = 8;

    private static final int OP_INT_LT = 9;

    private static final int OP_INT_GT = 10;

    private static final int OP_RETURN = 21;

    private static final int OP_LOAD_VARPBIT = 25;

    private static final int OP_STORE_VARPBIT = 27;

    private static final int OP_INT_LE = 31;

    private static final int OP_INT_GE = 32;

    private static final int OP_LOAD_INT = 33;

    private static final int OP_STORE_INT = 34;

    private static final int OP_LOAD_STRING = 35;

    private static final int OP_STORE_STRING = 36;

    private static final int OP_MERGE_STRINGS = 37;

    private static final int OP_POP_INT = 38;

    private static final int OP_POP_STRING = 39;

    private static final int OP_CALL_CS2 = 40;

    private static final int OP_LOAD_VARC = 42;

    private static final int OP_STORE_VARC = 43;

    private static final int OP_ARRAY_NEW = 44;

    private static final int OP_ARRAY_LOAD = 45;

    private static final int OP_ARRAY_STORE = 46;

    private static final int OP_LOAD_VARCSTR = 47;

    private static final int OP_STORE_VARCSTR = 48;

    private static final int OP_SWITCH = 51;

    private static final Map<Integer, String> OPCODE_NAMES = createOpcodeNames();

    private ClientScriptDecoder() {
    }
    static ScriptInfo decode(int scriptId, byte[] data) {
        if (data == null || data.length == 0) {
            return ScriptInfo.empty(scriptId);
        }
        InputStream stream = new InputStream(data);
        int switchTrailerLength = readUnsignedShort(data, data.length - 2);
        int metadataLength = 12;
        int codeBlockEnd = data.length - switchTrailerLength - metadataLength - 2;
        if (codeBlockEnd <= 0 || codeBlockEnd >= data.length) {
            return ScriptInfo.invalid(scriptId, data.length, "Unable to locate the script metadata trailer.");
        }
        stream.setOffset(codeBlockEnd);
        int instructionCount = stream.readInt();
        int intLocals = stream.readUnsignedShort();
        int stringLocals = stream.readUnsignedShort();
        int intArgs = stream.readUnsignedShort();
        int stringArgs = stream.readUnsignedShort();
        int switchCount = stream.readUnsignedByte();
        List<SwitchTable> switches = new ArrayList<>();
        for (int i = 0; i < switchCount; i++) {
            int caseCount = stream.readUnsignedShort();
            LinkedHashMap<Integer, Integer> cases = new LinkedHashMap<>();
            for (int caseIndex = 0; caseIndex < caseCount; caseIndex++) {
                cases.put(stream.readInt(), stream.readInt());
            }
            switches.add(new SwitchTable(i, cases));
        }
        stream.setOffset(0);
        String scriptName = stream.readString();
        List<Instruction> instructions = new ArrayList<>();
        int pc = 0;
        while (stream.getRemaining() > 0 && stream.getOffset() < codeBlockEnd) {
            int opcodeOffset = stream.getOffset();
            int opcode = stream.readUnsignedShort();
            Operand operand = readOperand(stream, opcode, switches, pc);
            instructions.add(new Instruction(pc, opcodeOffset, opcode, opcodeName(opcode), operand));
            pc++;
        }
        return new ScriptInfo(
                scriptId,
                currentRevision(),
                data.length,
                scriptName,
                instructionCount,
                intLocals,
                stringLocals,
                intArgs,
                stringArgs,
                switches,
                instructions,
                null
        );
    }

    private static Operand readOperand(InputStream stream, int opcode, List<SwitchTable> switches, int pc) {
        if (opcode == OP_PUSH_STRING) {
            return Operand.string(stream.readString());
        }
        if (opcode == OP_SWITCH) {
            int switchIndex = stream.readInt();
            Map<Integer, Integer> cases = switchIndex >= 0 && switchIndex < switches.size()
                    ? switches.get(switchIndex).cases
                    : Collections.emptyMap();
            return Operand.switchTable(switchIndex, cases, pc);
        }
        if (isJumpOpcode(opcode)) {
            int delta = stream.readInt();
            return Operand.jump(delta, pc + delta + 1);
        }
        if (isByteOperandOpcode(opcode)) {
            return Operand.intValue(stream.readUnsignedByte());
        }
        return Operand.intValue(stream.readInt());
    }

    private static boolean isByteOperandOpcode(int opcode) {
        return opcode == OP_RETURN || opcode == OP_POP_INT || opcode == OP_POP_STRING || opcode >= 100;
    }

    private static boolean isJumpOpcode(int opcode) {
        return opcode == OP_GOTO
                || opcode == OP_INT_NE
                || opcode == OP_INT_EQ
                || opcode == OP_INT_LT
                || opcode == OP_INT_GT
                || opcode == OP_INT_LE
                || opcode == OP_INT_GE;
    }

    private static int readUnsignedShort(byte[] data, int offset) {
        if (offset < 0 || offset + 1 >= data.length) {
            return 0;
        }
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    private static String opcodeName(int opcode) {
        String name = OPCODE_NAMES.get(opcode);
        return name != null ? name : "OP_" + opcode;
    }

    private static Map<Integer, String> createOpcodeNames() {
        Map<Integer, String> names = new HashMap<>();
        names.put(OP_PUSH_INT, "PUSH_INT");
        names.put(OP_LOAD_VARP, "LOAD_VARP");
        names.put(OP_STORE_VARP, "STORE_VARP");
        names.put(OP_PUSH_STRING, "PUSH_STRING");
        names.put(OP_GOTO, "GOTO");
        names.put(OP_INT_NE, "INT_NE");
        names.put(OP_INT_EQ, "INT_EQ");
        names.put(OP_INT_LT, "INT_LT");
        names.put(OP_INT_GT, "INT_GT");
        names.put(OP_RETURN, "RETURN");
        names.put(OP_LOAD_VARPBIT, "LOAD_VARPBIT");
        names.put(OP_STORE_VARPBIT, "STORE_VARPBIT");
        names.put(OP_INT_LE, "INT_LE");
        names.put(OP_INT_GE, "INT_GE");
        names.put(OP_LOAD_INT, "LOAD_INT");
        names.put(OP_STORE_INT, "STORE_INT");
        names.put(OP_LOAD_STRING, "LOAD_STRING");
        names.put(OP_STORE_STRING, "STORE_STRING");
        names.put(OP_MERGE_STRINGS, "MERGE_STRINGS");
        names.put(OP_POP_INT, "POP_INT");
        names.put(OP_POP_STRING, "POP_STRING");
        names.put(OP_CALL_CS2, "CALL_CS2");
        names.put(OP_LOAD_VARC, "LOAD_VARC");
        names.put(OP_STORE_VARC, "STORE_VARC");
        names.put(OP_ARRAY_NEW, "ARRAY_NEW");
        names.put(OP_ARRAY_LOAD, "ARRAY_LOAD");
        names.put(OP_ARRAY_STORE, "ARRAY_STORE");
        names.put(OP_LOAD_VARCSTR, "LOAD_VARCSTR");
        names.put(OP_STORE_VARCSTR, "STORE_VARCSTR");
        names.put(OP_SWITCH, "SWITCH");
        return names;
    }
    static final class ScriptInfo {
        final int scriptId;
        final int revision;
        final int byteLength;
        final String scriptName;
        final int instructionCount;
        final int intLocals;
        final int stringLocals;
        final int intArgs;
        final int stringArgs;
        final List<SwitchTable> switches;
        final List<Instruction> instructions;
        final String error;

        private ScriptInfo(int scriptId, int revision, int byteLength, String scriptName, int instructionCount,
                           int intLocals, int stringLocals, int intArgs, int stringArgs,
                           List<SwitchTable> switches, List<Instruction> instructions, String error) {
            this.scriptId = scriptId;
            this.revision = revision;
            this.byteLength = byteLength;
            this.scriptName = scriptName;
            this.instructionCount = instructionCount;
            this.intLocals = intLocals;
            this.stringLocals = stringLocals;
            this.intArgs = intArgs;
            this.stringArgs = stringArgs;
            this.switches = switches;
            this.instructions = instructions;
            this.error = error;
        }
        static ScriptInfo empty(int scriptId) {
            return new ScriptInfo(scriptId, currentRevision(), 0, "", 0, 0, 0, 0, 0,
                    Collections.emptyList(), Collections.emptyList(), "No script data.");
        }
        static ScriptInfo invalid(int scriptId, int byteLength, String error) {
            return new ScriptInfo(scriptId, currentRevision(), byteLength, "", 0, 0, 0, 0, 0,
                    Collections.emptyList(), Collections.emptyList(), error);
        }
        boolean isValid() {
            return error == null;
        }
    }
    static final class Instruction {
        final int index;
        final int byteOffset;
        final int opcode;
        final String name;
        final Operand operand;

        private Instruction(int index, int byteOffset, int opcode, String name, Operand operand) {
            this.index = index;
            this.byteOffset = byteOffset;
            this.opcode = opcode;
            this.name = name;
            this.operand = operand;
        }
    }
    static final class Operand {
        final Integer intValue;
        final String stringValue;
        final Integer jumpTarget;
        final Integer switchIndex;
        final Map<Integer, Integer> switchCases;

        private Operand(Integer intValue, String stringValue, Integer jumpTarget, Integer switchIndex,
                        Map<Integer, Integer> switchCases) {
            this.intValue = intValue;
            this.stringValue = stringValue;
            this.jumpTarget = jumpTarget;
            this.switchIndex = switchIndex;
            this.switchCases = switchCases;
        }
        static Operand intValue(int value) {
            return new Operand(value, null, null, null, Collections.emptyMap());
        }
        static Operand string(String value) {
            return new Operand(null, value, null, null, Collections.emptyMap());
        }
        static Operand jump(int delta, int target) {
            return new Operand(delta, null, target, null, Collections.emptyMap());
        }
        static Operand switchTable(int switchIndex, Map<Integer, Integer> cases, int pc) {
            LinkedHashMap<Integer, Integer> targets = new LinkedHashMap<>();
            for (Map.Entry<Integer, Integer> entry : cases.entrySet()) {
                targets.put(entry.getKey(), pc + entry.getValue() + 1);
            }
            return new Operand(null, null, null, switchIndex, targets);
        }
    }
    static final class SwitchTable {
        final int index;
        final Map<Integer, Integer> cases;

        private SwitchTable(int index, Map<Integer, Integer> cases) {
            this.index = index;
            this.cases = cases;
        }
    }

    private static int currentRevision() {
        return RuntimeRevision.getRevision();
    }
}
