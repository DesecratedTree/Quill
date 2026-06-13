package com.desecratedtree.quill.defs;

import com.desecratedtree.quill.cache.CacheManager;
import com.desecratedtree.quill.codec.InputStream;
import com.desecratedtree.quill.codec.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RenderAnimationDefinitions {

    private static final int BAS_GROUP = 32;

    private static final Map<Integer, RenderAnimationDefinitions> CACHE = new ConcurrentHashMap<>();

    public final int id;

    public int idleSequenceId = -1;

    public int walkSequenceId = -1;

    public int[][] modelTransforms;

    private RenderAnimationDefinitions(int id) {
        this.id = id;
    }

    public static RenderAnimationDefinitions get(int id) {
        if (id < 0) {
            return null;
        }
        return CACHE.computeIfAbsent(id, RenderAnimationDefinitions::decode);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static RenderAnimationDefinitions decode(int id) {
        RenderAnimationDefinitions definition = new RenderAnimationDefinitions(id);
        byte[] data = CacheManager.getConfigData(BAS_GROUP, id);
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
            DefinitionHandler.forType(RenderAnimationDefinitions.class, "definitions/render_animation.toml");

    private void decodeOpcode(InputStream stream, int opcode) {
        HANDLER.read(this, stream, opcode);
    }

    public byte[] encode() {
        OutputStream out = new OutputStream();
        if (idleSequenceId != -1 || walkSequenceId != -1) {
            out.writeByte(1);
            out.writeShort(idleSequenceId == -1 ? 65535 : idleSequenceId);
            out.writeShort(walkSequenceId == -1 ? 65535 : walkSequenceId);
        }
        if (modelTransforms != null) {
            for (int index = 0; index < modelTransforms.length; index++) {
                int[] transform = modelTransforms[index];
                if (transform == null || transform.length < 6) {
                    continue;
                }
                out.writeByte(27);
                out.writeByte(index);
                for (int value : transform) {
                    out.writeShort(value);
                }
            }
        }
        out.writeByte(0);
        return out.toByteArray();
    }

    public void save() {
        CacheManager.writeIndexData(2, BAS_GROUP, id, encode());
        CACHE.put(id, this);
    }
}
