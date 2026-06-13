package com.desecratedtree.quill.texture;

import com.desecratedtree.quill.render.RenderModel;
import java.util.Arrays;

public final class TextureUvMapper {

    private static final int SIMPLE_TEXTURE = 0;

    private static final int CYLINDRICAL_TEXTURE = 1;

    private static final int CUBE_TEXTURE = 2;

    private static final int SPHERICAL_TEXTURE = 3;

    private TextureUvMapper() {
    }
    public static UvTriangle map(RenderModel model, int face) {
        int coordinate = textureCoordinate(model, face);
        if (coordinate < 0) {
            return UvTriangle.DEFAULT;
        }
        int renderType = value(model.textureRenderTypes, coordinate, SIMPLE_TEXTURE) & 0xFF;
        if (renderType == SIMPLE_TEXTURE) {
            return mapSimple(model, face, coordinate);
        }
        if (renderType == CYLINDRICAL_TEXTURE || renderType == CUBE_TEXTURE || renderType == SPHERICAL_TEXTURE) {
            return mapComplex(model, face, coordinate, renderType);
        }
        return UvTriangle.DEFAULT;
    }

    private static UvTriangle mapSimple(RenderModel model, int face, int coordinate) {
        if (!hasTextureVertices(model, coordinate)) {
            return UvTriangle.DEFAULT;
        }
        int faceA = model.faceA[face];
        int faceB = model.faceB[face];
        int faceC = model.faceC[face];
        float[] uvA = projectSimple(
                model,
                faceA,
                model.textureVertexA[coordinate],
                model.textureVertexB[coordinate],
                model.textureVertexC[coordinate]
        );
        float[] uvB = projectSimple(
                model,
                faceB,
                model.textureVertexA[coordinate],
                model.textureVertexB[coordinate],
                model.textureVertexC[coordinate]
        );
        float[] uvC = projectSimple(
                model,
                faceC,
                model.textureVertexA[coordinate],
                model.textureVertexB[coordinate],
                model.textureVertexC[coordinate]
        );
        return new UvTriangle(uvA[0], uvA[1], uvB[0], uvB[1], uvC[0], uvC[1]);
    }

    private static float[] projectSimple(RenderModel model, int vertex, int textureA, int textureB, int textureC) {
        double px = model.verticesX[textureA];
        double py = model.verticesY[textureA];
        double pz = model.verticesZ[textureA];
        double mx = model.verticesX[textureB] - px;
        double my = model.verticesY[textureB] - py;
        double mz = model.verticesZ[textureB] - pz;
        double nx = model.verticesX[textureC] - px;
        double ny = model.verticesY[textureC] - py;
        double nz = model.verticesZ[textureC] - pz;
        double vx = model.verticesX[vertex] - px;
        double vy = model.verticesY[vertex] - py;
        double vz = model.verticesZ[vertex] - pz;
        double mm = mx * mx + my * my + mz * mz;
        double mn = mx * nx + my * ny + mz * nz;
        double nn = nx * nx + ny * ny + nz * nz;
        double vm = vx * mx + vy * my + vz * mz;
        double vn = vx * nx + vy * ny + vz * nz;
        double determinant = mm * nn - mn * mn;
        if (Math.abs(determinant) < 1.0e-9) {
            return new float[]{0f, 0f};
        }
        double inverse = 1.0 / determinant;
        float u = (float) ((vm * nn - vn * mn) * inverse);
        float v = (float) ((vn * mm - vm * mn) * inverse);
        return new float[]{u, v};
    }

    private static UvTriangle mapComplex(RenderModel model, int face, int coordinate, int renderType) {
        ComplexState state = buildComplexState(model, coordinate);
        if (state == null) {
            return UvTriangle.DEFAULT;
        }
        int faceA = model.faceA[face];
        int faceB = model.faceB[face];
        int faceC = model.faceC[face];
        float[] uvA = new float[2];
        float[] uvB = new float[2];
        float[] uvC = new float[2];
        if (renderType == CYLINDRICAL_TEXTURE) {
            float wrap = state.scaleSpeed / 1024.0f;
            mapCylindrical(state, model, faceA, uvA, wrap);
            mapCylindrical(state, model, faceB, uvB, wrap);
            mapCylindrical(state, model, faceC, uvC, wrap);
            fixWrapSeam(uvA, uvB, uvC, (state.directionByte & 0x1) != 0, wrap * 0.5f, wrap);
        } else if (renderType == CUBE_TEXTURE) {
            int cubeFace = dominantAxis(
                    state.normalX / state.scaleSpeed,
                    state.normalY / state.scaleZ,
                    state.normalZ / state.scaleX
            );
            mapCube(state, model, faceA, uvA, cubeFace);
            mapCube(state, model, faceB, uvB, cubeFace);
            mapCube(state, model, faceC, uvC, cubeFace);
        } else {
            mapSpherical(state, model, faceA, uvA);
            mapSpherical(state, model, faceB, uvB);
            mapSpherical(state, model, faceC, uvC);
            fixWrapSeam(uvA, uvB, uvC, (state.directionByte & 0x1) != 0, 0.5f, 1.0f);
        }
        return new UvTriangle(uvA[0], uvA[1], uvB[0], uvB[1], uvC[0], uvC[1]);
    }

    private static void mapCylindrical(ComplexState state, RenderModel model, int vertex, float[] out, float wrapScale) {
        double dx = model.verticesX[vertex] - state.centerX;
        double dy = model.verticesY[vertex] - state.centerY;
        double dz = model.verticesZ[vertex] - state.centerZ;
        double uBase = state.matrix[0] * dx + state.matrix[1] * dy + state.matrix[2] * dz;
        double vBase = state.matrix[3] * dx + state.matrix[4] * dy + state.matrix[5] * dz;
        double wBase = state.matrix[6] * dx + state.matrix[7] * dy + state.matrix[8] * dz;
        float u = 0.5f + (float) (Math.atan2(uBase, wBase) / (Math.PI * 2.0));
        if (wrapScale != 1.0f) {
            u *= wrapScale;
        }
        float v = 0.5f + (float) vBase + state.translateDirection;
        rotateUv(state.directionByte, u, v, out);
    }

    private static void mapCube(ComplexState state, RenderModel model, int vertex, float[] out, int cubeFace) {
        double dx = model.verticesX[vertex] - state.centerX;
        double dy = model.verticesY[vertex] - state.centerY;
        double dz = model.verticesZ[vertex] - state.centerZ;
        float x = (float) (state.matrix[0] * dx + state.matrix[1] * dy + state.matrix[2] * dz);
        float y = (float) (state.matrix[3] * dx + state.matrix[4] * dy + state.matrix[5] * dz);
        float z = (float) (state.matrix[6] * dx + state.matrix[7] * dy + state.matrix[8] * dz);
        float u;
        float v;
        if (cubeFace == 0) {
            u = 0.5f + state.translateV + x;
            v = 0.5f + state.translateDirection - z;
        } else if (cubeFace == 1) {
            u = 0.5f + state.translateV + x;
            v = 0.5f + state.translateU + z;
        } else if (cubeFace == 2) {
            u = 0.5f + state.translateV - x;
            v = 0.5f + state.translateDirection - y;
        } else if (cubeFace == 3) {
            u = 0.5f + state.translateV + x;
            v = 0.5f + state.translateDirection - y;
        } else if (cubeFace == 4) {
            u = 0.5f + state.translateDirection + z;
            v = 0.5f + state.translateU - y;
        } else {
            u = 0.5f + state.translateDirection - z;
            v = 0.5f + state.translateU - y;
        }
        rotateUv(state.directionByte, u, v, out);
    }

    private static void mapSpherical(ComplexState state, RenderModel model, int vertex, float[] out) {
        double dx = model.verticesX[vertex] - state.centerX;
        double dy = model.verticesY[vertex] - state.centerY;
        double dz = model.verticesZ[vertex] - state.centerZ;
        float x = (float) (state.matrix[0] * dx + state.matrix[1] * dy + state.matrix[2] * dz);
        float y = (float) (state.matrix[3] * dx + state.matrix[4] * dy + state.matrix[5] * dz);
        float z = (float) (state.matrix[6] * dx + state.matrix[7] * dy + state.matrix[8] * dz);
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length == 0f) {
            out[0] = 0f;
            out[1] = 0f;
            return;
        }
        float u = 0.5f + (float) (Math.atan2(x, z) / (Math.PI * 2.0));
        float v = state.translateDirection + 0.5f + (float) (Math.asin(y / length) / Math.PI);
        rotateUv(state.directionByte, u, v, out);
    }

    private static void rotateUv(int direction, float u, float v, float[] out) {
        int rotation = direction & 0x3;
        if (rotation == 1) {
            out[0] = -v;
            out[1] = u;
        } else if (rotation == 2) {
            out[0] = -u;
            out[1] = -v;
        } else if (rotation == 3) {
            out[0] = v;
            out[1] = -u;
        } else {
            out[0] = u;
            out[1] = v;
        }
    }

    private static void fixWrapSeam(float[] uvA, float[] uvB, float[] uvC, boolean vertical, float threshold, float wrap) {
        if (!vertical) {
            if (uvB[0] - uvA[0] > threshold) {
                uvB[0] -= wrap;
            } else if (uvA[0] - uvB[0] > threshold) {
                uvB[0] += wrap;
            }
            if (uvC[0] - uvA[0] > threshold) {
                uvC[0] -= wrap;
            } else if (uvA[0] - uvC[0] > threshold) {
                uvC[0] += wrap;
            }
        } else {
            if (uvB[1] - uvA[1] > threshold) {
                uvB[1] -= wrap;
            } else if (uvA[1] - uvB[1] > threshold) {
                uvB[1] += wrap;
            }
            if (uvC[1] - uvA[1] > threshold) {
                uvC[1] -= wrap;
            } else if (uvA[1] - uvC[1] > threshold) {
                uvC[1] += wrap;
            }
        }
    }

    private static ComplexState buildComplexState(RenderModel model, int coordinate) {
        if (model.textureVertexA == null
                || model.textureVertexB == null
                || model.textureVertexC == null
                || coordinate >= model.textureVertexA.length
                || coordinate >= model.textureVertexB.length
                || coordinate >= model.textureVertexC.length) {
            return null;
        }
        int[] bounds = coordinateBounds(model, coordinate);
        if (bounds == null) {
            return null;
        }
        int centerX = (bounds[1] + bounds[0]) / 2;
        int centerY = (bounds[3] + bounds[2]) / 2;
        int centerZ = (bounds[5] + bounds[4]) / 2;
        int renderType = value(model.textureRenderTypes, coordinate, SIMPLE_TEXTURE) & 0xFF;
        float scaleSpeed = scaleSpeed(model, coordinate);
        float scaleX = scaleX(model, coordinate);
        float scaleZ = scaleZ(model, coordinate);
        float matrixScale0;
        float matrixScale1;
        float matrixScale2;
        if (renderType == CYLINDRICAL_TEXTURE) {
            int speed = value(model.textureSpeed, coordinate, 0);
            if (speed == 0) {
                matrixScale0 = 1.0f;
                matrixScale1 = 1.0f;
            } else if (speed <= 0) {
                matrixScale0 = 1.0f;
                matrixScale1 = -speed / 1024.0f;
            } else {
                matrixScale1 = 1.0f;
                matrixScale0 = speed / 1024.0f;
            }
            matrixScale2 = 64.0f / scaleSpeed;
        } else if (renderType == CUBE_TEXTURE) {
            matrixScale2 = 64.0f / scaleSpeed;
            matrixScale0 = 64.0f / scaleX;
            matrixScale1 = 64.0f / scaleZ;
        } else {
            matrixScale2 = scaleSpeed / 1024.0f;
            matrixScale0 = scaleX / 1024.0f;
            matrixScale1 = scaleZ / 1024.0f;
        }
        float[] matrix = buildMatrix(
                model.textureVertexC[coordinate],
                model.textureVertexB[coordinate],
                matrixScale0,
                matrixScale1,
                matrixScale2,
                model.textureVertexA[coordinate],
                unsignedByte(value(model.textureRotation, coordinate, 0))
        );
        return new ComplexState(
                centerX,
                centerY,
                centerZ,
                matrix,
                value(model.textureScaleY, coordinate, 0),
                value(model.textureDirection, coordinate, 0) / 256.0f,
                value(model.textureTransU, coordinate, 0) / 256.0f,
                value(model.textureTransV, coordinate, 0) / 256.0f,
                scaleSpeed,
                scaleX,
                scaleZ
        );
    }

    private static int[] coordinateBounds(RenderModel model, int coordinate) {
        if (model.textureCoordinates == null) {
            return null;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        boolean found = false;
        for (int face = 0; face < model.faceCount; face++) {
            int faceCoordinate = textureCoordinate(model, face);
            if (faceCoordinate != coordinate) {
                continue;
            }
            found = true;
            int[] vertices = {model.faceA[face], model.faceB[face], model.faceC[face]};
            for (int vertex : vertices) {
                minX = Math.min(minX, model.verticesX[vertex]);
                maxX = Math.max(maxX, model.verticesX[vertex]);
                minY = Math.min(minY, model.verticesY[vertex]);
                maxY = Math.max(maxY, model.verticesY[vertex]);
                minZ = Math.min(minZ, model.verticesZ[vertex]);
                maxZ = Math.max(maxZ, model.verticesZ[vertex]);
            }
        }
        if (!found) {
            return null;
        }
        return new int[]{minX, maxX, minY, maxY, minZ, maxZ};
    }

    private static float[] buildMatrix(int vertexC, int vertexB, float scale0, float scale1, float scale2, int vertexA, int rotation) {
        float[] rotateY = new float[9];
        float angleCos = (float) Math.cos(rotation * 0.024543693f);
        float angleSin = (float) Math.sin(rotation * 0.024543693f);
        rotateY[0] = angleCos;
        rotateY[1] = 0.0f;
        rotateY[2] = angleSin;
        rotateY[3] = 0.0f;
        rotateY[4] = 1.0f;
        rotateY[5] = 0.0f;
        rotateY[6] = -angleSin;
        rotateY[7] = 0.0f;
        rotateY[8] = angleCos;
        float y = vertexB / 32767.0f;
        float oneMinusY = 1.0f - y;
        float xzLength = (float) Math.sqrt((float) vertexA * vertexA + (float) vertexC * vertexC);
        float axisX = 1.0f;
        float axisZ = 0.0f;
        if (xzLength != 0.0f) {
            axisX = (float) -vertexC / xzLength;
            axisZ = (float) vertexA / xzLength;
        }
        float axisY = -(float) Math.sqrt(Math.max(0.0f, 1.0f - y * y));
        float[] axisRotate = new float[9];
        if (xzLength == 0.0f && y == 0.0f) {
            axisRotate = rotateY;
        } else {
            axisRotate[0] = oneMinusY * axisX * axisX + y;
            axisRotate[1] = axisY * axisZ;
            axisRotate[2] = axisX * axisZ * oneMinusY;
            axisRotate[3] = -axisZ * axisY;
            axisRotate[4] = y;
            axisRotate[5] = axisX * axisY;
            axisRotate[6] = axisZ * axisX * oneMinusY;
            axisRotate[7] = -axisX * axisY;
            axisRotate[8] = y + oneMinusY * axisZ * axisZ;
            float[] combined = new float[9];
            combined[0] = axisRotate[0] * rotateY[0] + rotateY[1] * axisRotate[3] + axisRotate[6] * rotateY[2];
            combined[1] = axisRotate[1] * rotateY[0] + axisRotate[4] * rotateY[1] + axisRotate[7] * rotateY[2];
            combined[2] = axisRotate[2] * rotateY[0] + axisRotate[5] * rotateY[1] + axisRotate[8] * rotateY[2];
            combined[3] = axisRotate[0] * rotateY[3] + axisRotate[3] * rotateY[4] + axisRotate[6] * rotateY[5];
            combined[4] = axisRotate[1] * rotateY[3] + axisRotate[4] * rotateY[4] + axisRotate[7] * rotateY[5];
            combined[5] = axisRotate[2] * rotateY[3] + axisRotate[5] * rotateY[4] + axisRotate[8] * rotateY[5];
            combined[6] = axisRotate[0] * rotateY[6] + axisRotate[3] * rotateY[7] + axisRotate[6] * rotateY[8];
            combined[7] = axisRotate[1] * rotateY[6] + axisRotate[4] * rotateY[7] + axisRotate[7] * rotateY[8];
            combined[8] = axisRotate[2] * rotateY[6] + axisRotate[5] * rotateY[7] + axisRotate[8] * rotateY[8];
            axisRotate = combined;
        }
        axisRotate[0] *= scale1;
        axisRotate[1] *= scale1;
        axisRotate[2] *= scale1;
        axisRotate[3] *= scale2;
        axisRotate[4] *= scale2;
        axisRotate[5] *= scale2;
        axisRotate[6] *= scale0;
        axisRotate[7] *= scale0;
        axisRotate[8] *= scale0;
        return axisRotate;
    }

    private static int dominantAxis(float x, float y, float z) {
        float ax = Math.abs(x);
        float ay = Math.abs(y);
        float az = Math.abs(z);
        if (ax > ay && ax > az) {
            return x > 0.0f ? 0 : 1;
        }
        if (az > ax && ay < az) {
            return z > 0.0f ? 2 : 3;
        }
        return y > 0.0f ? 4 : 5;
    }

    private static boolean hasTextureVertices(RenderModel model, int coordinate) {
        return model.textureVertexA != null
                && model.textureVertexB != null
                && model.textureVertexC != null
                && coordinate < model.textureVertexA.length
                && coordinate < model.textureVertexB.length
                && coordinate < model.textureVertexC.length;
    }

    private static int textureCoordinate(RenderModel model, int face) {
        if (model.textureCoordinates == null || face >= model.textureCoordinates.length) {
            return -1;
        }
        int coordinate = model.textureCoordinates[face];
        if (coordinate < 0) {
            return -1;
        }
        return coordinate & 0xFF;
    }

    private static int value(int[] values, int index, int fallback) {
        if (values == null || index < 0 || index >= values.length) {
            return fallback;
        }
        return values[index];
    }

    private static int unsignedByte(int value) {
        return value & 0xFF;
    }

    private static float scaleSpeed(RenderModel model, int coordinate) {
        return sanitizeScale(value(model.textureScaleZ, coordinate, 128));
    }

    private static float scaleX(RenderModel model, int coordinate) {
        return sanitizeScale(value(model.textureScaleX, coordinate, 128));
    }

    private static float scaleZ(RenderModel model, int coordinate) {
        return sanitizeScale(value(model.textureSpeed, coordinate, 128));
    }

    private static float sanitizeScale(int value) {
        return value == 0 ? 128.0f : Math.abs((float) value);
    }
    public static final class UvTriangle {
        static final UvTriangle DEFAULT = new UvTriangle(0f, 0f, 1f, 0f, 0f, 1f);
        public final float u1;
        public final float v1;
        public final float u2;
        public final float v2;
        public final float u3;
        public final float v3;
        UvTriangle(float u1, float v1, float u2, float v2, float u3, float v3) {
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
            this.u3 = u3;
            this.v3 = v3;
        }
    }

    private static final class ComplexState {

        private final int centerX;

        private final int centerY;

        private final int centerZ;

        private final float[] matrix;

        private final int directionByte;

        private final float translateDirection;

        private final float translateU;

        private final float translateV;

        private final float scaleSpeed;

        private final float scaleX;

        private final float scaleZ;

        private final float normalX;

        private final float normalY;

        private final float normalZ;

        private ComplexState(int centerX, int centerY, int centerZ, float[] matrix, int directionByte,
                             float translateDirection, float translateU, float translateV,
                             float scaleSpeed, float scaleX, float scaleZ) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.matrix = Arrays.copyOf(matrix, matrix.length);
            this.directionByte = directionByte;
            this.translateDirection = translateDirection;
            this.translateU = translateU;
            this.translateV = translateV;
            this.scaleSpeed = scaleSpeed;
            this.scaleX = scaleX;
            this.scaleZ = scaleZ;
            this.normalX = matrix[0];
            this.normalY = matrix[1];
            this.normalZ = matrix[2];
        }
    }
}
