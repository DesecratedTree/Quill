package com.desecratedtree.quill.texture.synth;

/**
 * Layer factory, port of {@code Class59_Sub1_Sub1.method557}.
 * Maps the on-disk type byte (0..39) to the concrete layer class.
 */
public final class TextureLayerType {

    private TextureLayerType() {
    }

    public static TextureLayer create(int type) {
        switch (type) {
            case 0:
                return new LayerSub15();
            case 1:
                return new LayerSub18();
            case 2:
                return new LayerSub19();
            case 3:
                return new LayerSub4();
            case 4:
                return new LayerSub22();
            case 5:
                return new LayerSub37();
            case 6:
                return new LayerSub38();
            case 7:
                return new LayerSub16();
            case 8:
                return new LayerSub14();
            case 9:
                return new LayerSub7();
            case 10:
                return new LayerSub12();
            case 11:
                return new LayerSub26();
            case 12:
                return new LayerSub36();
            case 13:
                return new LayerSub20();
            case 14:
                return new LayerSub11();
            case 15:
                return new LayerSub5();
            case 16:
                return new LayerSub2();
            case 17:
                return new LayerSub30();
            case 18:
                return new LayerSub17Sub1();
            case 19:
                return new LayerSub6();
            case 20:
                return new LayerSub31();
            case 21:
                return new LayerSub27();
            case 22:
                return new LayerSub32();
            case 23:
                return new LayerSub33();
            case 24:
                return new LayerSub13();
            case 25:
                return new LayerSub1();
            case 26:
                return new LayerSub3();
            case 27:
                return new LayerSub24();
            case 28:
                return new LayerSub23();
            case 29:
                return new LayerSub39();
            case 30:
                return new LayerSub10();
            case 31:
                return new LayerSub25();
            case 32:
                return new LayerSub35();
            case 33:
                return new LayerSub34();
            case 34:
                return new LayerSub8();
            case 35:
                return new LayerSub9();
            case 36:
                return new LayerSub29();
            case 37:
                return new LayerSub21();
            case 38:
                return new LayerSub28();
            case 39:
                return new LayerSub17();
            default:
                return null;
        }
    }
}