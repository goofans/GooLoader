package me.wy.gooloader.gooloader.format.datatypes;

public class KeyFrameTransform extends KeyFrame {
    private final TransformType transformType;

    public KeyFrameTransform(byte[] contents, int offset, int stringTableOffset, TransformType transformType) {
        super(contents, offset, stringTableOffset);
        this.transformType = transformType;
    }

    public TransformType getTransformType() {
        return transformType;
    }
}
