package me.wy.gooloader.gooloader.format.datatypes;

import me.wy.gooloader.gooloader.util.BinaryFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

// Original code by David Croft (davidc@goofans.com)
public abstract class KeyFrame
{
    protected static final int INTERPOLATION_NONE = 0;
    protected static final int INTERPOLATION_LINEAR = 1;

    public float x;
    public float y;
    public float angle;
    public int alpha;
    public int color;
    public int nextFrameIndex;
    public int soundStrIndex;
    public int interpolationType;
    public String soundStr;

    public KeyFrame(byte[] contents, int offset, int stringTableOffset) {
        x = BinaryFormat.getFloat(contents, offset + 0);
        y = BinaryFormat.getFloat(contents, offset + 4);
        angle = BinaryFormat.getFloat(contents, offset + 8);
        alpha = BinaryFormat.getInt(contents, offset + 12);
        color = BinaryFormat.getInt(contents, offset + 16);
        nextFrameIndex = BinaryFormat.getInt(contents, offset + 20);
        soundStrIndex = BinaryFormat.getInt(contents, offset + 24);
        if (soundStrIndex > 0) {
            soundStr = BinaryFormat.getString(contents, stringTableOffset + soundStrIndex);
        }
        interpolationType = BinaryFormat.getInt(contents, offset + 28);
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(x).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(y).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(angle).array());
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(color).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(nextFrameIndex).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(soundStrIndex).array());
        if (soundStrIndex > 0) {
            os.write(soundStr.getBytes());
        }
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(interpolationType).array());
        os.close();
        return os.toByteArray();
    }

    @Override
    public String toString() {
        String itStr = (interpolationType == INTERPOLATION_LINEAR ? "LINEAR" : interpolationType == INTERPOLATION_NONE ? "NONE" : String.valueOf(interpolationType));

        return "KeyFrame{" +
                "x=" + x +
                ", y=" + y +
                ", angle=" + angle +
                ", alpha=" + alpha +
                ", color=" + color +
                ", nextFrameIndex=" + nextFrameIndex +
                ", soundStrIndex=" + soundStrIndex + (soundStrIndex > 0 ? "(" + soundStr + ")" : "") +
                ", interpolationType=" + itStr +
                '}';
    }
}
