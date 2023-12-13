package me.wy.gooloader.gooloader.format.datatypes;

public enum TransformType {
    SCALE(0),
    ROTATE(1),
    TRANSLATE(2);

    private final int value;

    TransformType(int value)
    {
        this.value = value;
    }

    public static TransformType getByValue(int value) {
        for (TransformType tt : TransformType.values()) {
            if (tt.value == value) return tt;
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
