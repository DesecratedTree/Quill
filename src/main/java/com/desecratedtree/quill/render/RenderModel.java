package com.desecratedtree.quill.render;

public final class RenderModel {

    public final int id;

    public final int vertexCount;

    public final int faceCount;

    public final int[] verticesX;

    public final int[] verticesY;

    public final int[] verticesZ;

    public final int[] faceA;

    public final int[] faceB;

    public final int[] faceC;

    public final short[] faceColors;

    public final int[] faceAlphas;

    public final int[] faceRenderTypes;

    public final int[] faceTextures;

    public final int[] textureCoordinates;

    public final int[] textureRenderTypes;

    public final int[] textureVertexA;

    public final int[] textureVertexB;

    public final int[] textureVertexC;

    public final int[] textureScaleX;

    public final int[] textureScaleY;

    public final int[] textureScaleZ;

    public final int[] textureRotation;

    public final int[] textureDirection;

    public final int[] textureSpeed;

    public final int[] textureTransU;

    public final int[] textureTransV;

    public final int minX;

    public final int maxX;

    public final int minY;

    public final int maxY;

    public final int minZ;

    public final int maxZ;

    public int[] vertexSkins;

    public int[] faceGroupMajority;

    public RenderModel(
            int id,
            int vertexCount,
            int faceCount,
            int[] verticesX,
            int[] verticesY,
            int[] verticesZ,
            int[] faceA,
            int[] faceB,
            int[] faceC,
            short[] faceColors,
            int[] faceAlphas,
            int[] faceRenderTypes,
            int[] faceTextures,
            int[] textureCoordinates,
            int[] textureRenderTypes,
            int[] textureVertexA,
            int[] textureVertexB,
            int[] textureVertexC,
            int[] textureScaleX,
            int[] textureScaleY,
            int[] textureScaleZ,
            int[] textureRotation,
            int[] textureDirection,
            int[] textureSpeed,
            int[] textureTransU,
            int[] textureTransV,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ
    ) {
        this.id = id;
        this.vertexCount = vertexCount;
        this.faceCount = faceCount;
        this.verticesX = verticesX;
        this.verticesY = verticesY;
        this.verticesZ = verticesZ;
        this.faceA = faceA;
        this.faceB = faceB;
        this.faceC = faceC;
        this.faceColors = faceColors;
        this.faceAlphas = faceAlphas;
        this.faceRenderTypes = faceRenderTypes;
        this.faceTextures = faceTextures;
        this.textureCoordinates = textureCoordinates;
        this.textureRenderTypes = textureRenderTypes;
        this.textureVertexA = textureVertexA;
        this.textureVertexB = textureVertexB;
        this.textureVertexC = textureVertexC;
        this.textureScaleX = textureScaleX;
        this.textureScaleY = textureScaleY;
        this.textureScaleZ = textureScaleZ;
        this.textureRotation = textureRotation;
        this.textureDirection = textureDirection;
        this.textureSpeed = textureSpeed;
        this.textureTransU = textureTransU;
        this.textureTransV = textureTransV;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    public int getDepth() {
        return maxZ - minZ;
    }

    public RenderModel copyWithColors(short[] newFaceColors) {
        RenderModel copy = new RenderModel(id, vertexCount, faceCount,
                verticesX, verticesY, verticesZ,
                faceA, faceB, faceC, newFaceColors, faceAlphas, faceRenderTypes,
                faceTextures, textureCoordinates, textureRenderTypes,
                textureVertexA, textureVertexB, textureVertexC,
                textureScaleX, textureScaleY, textureScaleZ,
                textureRotation, textureDirection, textureSpeed,
                textureTransU, textureTransV,
                minX, maxX, minY, maxY, minZ, maxZ);
        copy.vertexSkins = vertexSkins;
        copy.faceGroupMajority = faceGroupMajority;
        return copy;
    }
}
