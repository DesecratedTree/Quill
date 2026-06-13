package com.desecratedtree.quill.ui.clientscript;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class ClientScriptOpcodeTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"PC", "Offset", "Opcode", "Name", "Operand"};

    private List<ClientScriptDecoder.Instruction> instructions = new ArrayList<>();
    void setInstructions(List<ClientScriptDecoder.Instruction> instructions) {
        this.instructions = instructions;
        fireTableDataChanged();
    }

    @Override

    public int getRowCount() {
        return instructions.size();
    }

    @Override

    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override

    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override

    public Object getValueAt(int rowIndex, int columnIndex) {
        ClientScriptDecoder.Instruction instruction = instructions.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return instruction.index;
            case 1:
                return String.format("0x%04X", instruction.byteOffset);
            case 2:
                return instruction.opcode;
            case 3:
                return instruction.name;
            case 4:
                return formatOperand(instruction.operand);
            default:
                return "";
        }
    }

    private String formatOperand(ClientScriptDecoder.Operand operand) {
        if (operand.stringValue != null) {
            return "\"" + operand.stringValue.replace("\"", "\\\"") + "\"";
        }
        if (operand.switchIndex != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("switch[").append(operand.switchIndex).append("]");
            if (!operand.switchCases.isEmpty()) {
                builder.append(' ');
                boolean first = true;
                for (java.util.Map.Entry<Integer, Integer> entry : operand.switchCases.entrySet()) {
                    if (!first) {
                        builder.append(", ");
                    }
                    first = false;
                    builder.append(entry.getKey()).append("->").append(entry.getValue());
                }
            }
            return builder.toString();
        }
        if (operand.jumpTarget != null) {
            return operand.intValue + " -> " + operand.jumpTarget;
        }
        return operand.intValue != null ? String.valueOf(operand.intValue) : "";
    }
}
