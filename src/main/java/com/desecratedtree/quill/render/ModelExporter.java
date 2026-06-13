package com.desecratedtree.quill.render;

import io.blurite.cache.model.Model;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class ModelExporter {

    private ModelExporter() {
    }

    public static byte[] exportMqo(int id, byte[] data) {
        Model model = decode(id, data);
        model.removeFullyTransparentTriangles();
        model.removeNonSolidTriangles();
        return writeMqo(model).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] export(int id, byte[] data, ModelFileFormat fileFormat) {
        if (fileFormat == ModelFileFormat.MQO) {
            return exportMqo(id, data);
        }
        return data;
    }

    private static Model decode(int id, byte[] data) {
        return Model.Companion.decode(id, Unpooled.wrappedBuffer(data));
    }

    private static byte[] toByteArray(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    private static String writeMqo(Model model) {
        int vertexCount = model.getVertexCount();
        int triangleCount = model.getTriangleCount();
        int[] x = model.getVertexPositionsX();
        int[] y = model.getVertexPositionsY();
        int[] z = model.getVertexPositionsZ();
        int[] a = model.getTriangleVertex1();
        int[] b = model.getTriangleVertex2();
        int[] c = model.getTriangleVertex3();
        StringBuilder out = new StringBuilder(Math.max(4096, vertexCount * 40 + triangleCount * 32));
        out.append("Metasequoia Document\n");
        out.append("Format Text Ver 1.0\n\n");
        out.append("Scene {\n");
        out.append("\tpos 0.0000 0.0000 1500.0000\n");
        out.append("\tlookat 0.0000 0.0000 0.0000\n");
        out.append("}\n");
        out.append("Material 1 {\n");
        out.append("\t\"default\" col(0.800 0.800 0.800 1.000) dif(1.000) amb(0.250) emi(0.000) spc(0.000) power(5.00)\n");
        out.append("}\n");
        out.append("Object \"model_").append(model.getId()).append("\" {\n");
        out.append("\tvisible 15\n");
        out.append("\tlocking 0\n");
        out.append("\tshading 1\n");
        out.append("\tfacet 59.5\n");
        out.append("\tvertex ").append(vertexCount).append(" {\n");
        for (int i = 0; i < vertexCount; i++) {
            out.append(String.format(Locale.US, "\t\t%d %d %d\n", x[i], -y[i], z[i]));
        }
        out.append("\t}\n");
        out.append("\tface ").append(triangleCount).append(" {\n");
        for (int i = 0; i < triangleCount; i++) {
            out.append("\t\t3 V(").append(a[i]).append(' ').append(b[i]).append(' ').append(c[i]).append(") M(0)\n");
        }
        out.append("\t}\n");
        out.append("}\n");
        out.append("Eof\n");
        return out.toString();
    }
}
