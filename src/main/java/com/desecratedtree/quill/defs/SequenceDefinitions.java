package com.desecratedtree.quill.defs;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.codec.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SequenceDefinitions {

    private static final int SEQUENCE_INDEX = 20;

    private static final Map<Integer, SequenceDefinitions> CACHE = new ConcurrentHashMap<>();

    public final int id;

    public int[] frameDurations = new int[0];

    public int[] frameIds = new int[0];

    public int[] secondaryFrameIds;

    public boolean[] interleaveOrder;

    public int loopOffset = -1;

    public int priority = 5;

    public int maxLoops = 99;

    public int replayMode;

    public boolean tweened;

    public int leftHandItem = -1;

    public int rightHandItem = -1;

    public int expressionFrameCount;

    public int[] expressionFrames;

    public int[][] sounds;

    public int animatingPrecedence = -1;

    public int walkingPrecedence = -1;

    private SequenceDefinitions(int id) {
        this.id = id;
    }

    public static SequenceDefinitions get(int id) {
        if (id < 0) {
            return null;
        }
        return CACHE.computeIfAbsent(id, SequenceDefinitions::decode);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static SequenceDefinitions decode(int id) {
        SequenceDefinitions definition = new SequenceDefinitions(id);
        byte[] data = CacheManager.getIndexData(SEQUENCE_INDEX, id >>> 7, id & 0x7F);
        if (data == null || data.length == 0) {
            return definition;
        }
        InputStream stream = new InputStream(data);
        while (stream.getRemaining() > 0) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0) {
                break;
            }
            definition.decodeOpcode(stream, opcode);
        }
        return definition;
    }

    private static final DefinitionHandler HANDLER =
            DefinitionHandler.forType(SequenceDefinitions.class, "definitions/sequence.toml");

    private void decodeOpcode(InputStream stream, int opcode) {
        switch (opcode) {
            case 1: {
                int count = stream.readUnsignedShort();
                frameDurations = new int[count];
                frameIds = new int[count];
                for (int i = 0; i < count; i++) {
                    frameDurations[i] = stream.readUnsignedShort();
                }
                for (int i = 0; i < count; i++) {
                    frameIds[i] = stream.readUnsignedShort();
                }
                for (int i = 0; i < count; i++) {
                    frameIds[i] |= stream.readUnsignedShort() << 16;
                }
                break;
            }
            case 2: {
                int count = stream.readUnsignedByte();
                interleaveOrder = new boolean[256];
                for (int i = 0; i < count; i++) {
                    interleaveOrder[stream.readUnsignedByte()] = true;
                }
                break;
            }
            case 3: {
                loopOffset = stream.readUnsignedShort();
                break;
            }
            case 4: {
                stream.readUnsignedShort();
                break;
            }
            case 5: {
                priority = stream.readUnsignedByte();
                break;
            }
            case 6: {
                leftHandItem = stream.readUnsignedShort();
                break;
            }
            case 7: {
                rightHandItem = stream.readUnsignedShort();
                break;
            }
            case 8: {
                maxLoops = stream.readUnsignedByte();
                break;
            }
            case 9: {
                replayMode = stream.readUnsignedByte();
                break;
            }
            case 10: {
                tweened = stream.readUnsignedByte() == 1;
                break;
            }
            case 11: {
                expressionFrameCount = stream.readUnsignedByte();
                break;
            }
            case 12: {
                int count = stream.readUnsignedByte();
                int[] ids = new int[count];
                for (int i = 0; i < count; i++) ids[i] = stream.readUnsignedShort();
                for (int i = 0; i < count; i++) ids[i] |= stream.readUnsignedShort() << 16;
                secondaryFrameIds = ids;
                break;
            }
            case 13: {
                int count = stream.readUnsignedShort();
                sounds = new int[count][];
                for (int i = 0; i < count; i++) {
                    int subCount = stream.readUnsignedByte();
                    if (subCount > 0) {
                        int[] sound = new int[subCount];
                        sound[0] = stream.read24BitInt();
                        for (int j = 1; j < subCount; j++) {
                            sound[j] = stream.readUnsignedShort();
                        }
                        sounds[i] = sound;
                    }
                }
                break;
            }
            case 14: {
                animatingPrecedence = stream.readUnsignedByte();
                break;
            }
            case 15: {
                walkingPrecedence = stream.readUnsignedByte();
                break;
            }
            case 16: {
                stream.readUnsignedByte();
                break;
            }
            case 17: {
                stream.readUnsignedShort();
                break;
            }
            case 18: {
                stream.readUnsignedShort();
                break;
            }
            case 19: {
                stream.readUnsignedByte();
                stream.readUnsignedByte();
                break;
            }
            case 20: {
                stream.readUnsignedByte();
                stream.readUnsignedShort();
                stream.readUnsignedShort();
                break;
            }
            default:
                HANDLER.read(this, stream, opcode);
        }
    }

    public int primaryFrameId(int frameIndex) {
        if (frameIds.length == 0) {
            return -1;
        }
        return frameIds[Math.floorMod(frameIndex, frameIds.length)];
    }

    public int frameIndexAtElapsedMillis(long elapsedMillis) {
        if (frameIds.length == 0) {
            return 0;
        }
        int totalDuration = 0;
        for (int duration : frameDurations) {
            totalDuration += Math.max(1, duration);
        }
        if (totalDuration <= 0) {
            return 0;
        }
        long sequenceCycle = Math.floorMod(elapsedMillis / 20L, totalDuration);
        for (int i = 0; i < frameDurations.length; i++) {
            sequenceCycle -= Math.max(1, frameDurations[i]);
            if (sequenceCycle < 0) {
                return i;
            }
        }
        return frameDurations.length - 1;
    }

    public byte[] encode() {
        OutputStream out = new OutputStream();
        if (frameIds != null && frameDurations != null && frameIds.length > 0 && frameDurations.length == frameIds.length) {
            out.writeByte(1);
            out.writeShort(frameIds.length);
            for (int duration : frameDurations) {
                out.writeShort(duration);
            }
            for (int frameId : frameIds) {
                out.writeShort(frameId & 0xFFFF);
            }
            for (int frameId : frameIds) {
                out.writeShort((frameId >>> 16) & 0xFFFF);
            }
        }
        if (interleaveOrder != null) {
            int count = 0;
            for (int i = 0; i < 256; i++) {
                if (i < interleaveOrder.length && interleaveOrder[i]) {
                    count++;
                }
            }
            if (count > 0) {
                out.writeByte(2);
                out.writeByte(count);
                for (int i = 0; i < 256 && i < interleaveOrder.length; i++) {
                    if (interleaveOrder[i]) {
                        out.writeByte(i);
                    }
                }
            }
        }
        if (loopOffset != -1) {
            out.writeByte(3);
            out.writeShort(loopOffset);
        }
        if (priority != 5) {
            out.writeByte(5);
            out.writeByte(priority);
        }
        if (leftHandItem != -1) {
            out.writeByte(6);
            out.writeShort(leftHandItem);
        }
        if (rightHandItem != -1) {
            out.writeByte(7);
            out.writeShort(rightHandItem);
        }
        if (maxLoops != 99) {
            out.writeByte(8);
            out.writeByte(maxLoops);
        }
        if (replayMode != 0) {
            out.writeByte(9);
            out.writeByte(replayMode);
        }
        if (tweened) {
            out.writeByte(10);
            out.writeByte(1);
        }
        if (secondaryFrameIds != null && secondaryFrameIds.length > 0) {
            out.writeByte(12);
            out.writeByte(secondaryFrameIds.length);
            for (int frameId : secondaryFrameIds) {
                out.writeShort(frameId & 0xFFFF);
            }
            for (int frameId : secondaryFrameIds) {
                out.writeShort((frameId >>> 16) & 0xFFFF);
            }
        }
        out.writeByte(0);
        return out.toByteArray();
    }

    public void save() {
        CacheManager.writeIndexData(SEQUENCE_INDEX, id >>> 7, id & 0x7F, encode());
        CACHE.put(id, this);
    }
}
