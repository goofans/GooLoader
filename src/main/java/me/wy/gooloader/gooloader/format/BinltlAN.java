package me.wy.gooloader.gooloader.format;

import me.wy.gooloader.gooloader.format.datatypes.*;
import me.wy.gooloader.gooloader.util.BinaryFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class BinltlAN {

    private boolean hasColor;
    private boolean hasAlpha;
    private boolean hasSound;
    private boolean hasTransform;
    private int numTransforms;
    private int numFrames;

    private TransformType[] transformTypes;
    private float[] frameTimes;
    private List<KeyFrame> keyFrames;
    private KeyFrameTransform[][] transformFrames;
    private KeyFrameAlpha[] alphaFrames;
    private KeyFrameColor[] colorFrames;
    private KeyFrameSound[] soundFrames;

    public BinltlAN(File file) throws IOException
    {
        byte[] contents;

        FileInputStream is = new FileInputStream(file);
        try {
            int fileLength = (int) file.length();
            contents = new byte[fileLength];
            if (is.read(contents) != fileLength) {
                throw new IOException("short read on anim " + file.getName());
            }
        }
        finally {
            is.close();
        }

        init(contents, 0);
    }

    private void init(byte[] contents, int offset)
    {
        hasColor = BinaryFormat.getInt(contents, offset + 0) != 0;
        hasAlpha = BinaryFormat.getInt(contents, offset + 4) != 0;
        hasSound = BinaryFormat.getInt(contents, offset + 8) != 0;
        hasTransform = BinaryFormat.getInt(contents, offset + 12) != 0;
        numTransforms = BinaryFormat.getInt(contents, offset + 16);
        numFrames = BinaryFormat.getInt(contents, offset + 20);

        int transformTypesOffset = offset + BinaryFormat.getInt(contents, offset + 24);
        int frameTimesOffset = offset + BinaryFormat.getInt(contents, offset + 28);
        int xformFramesOffset = offset + BinaryFormat.getInt(contents, offset + 32);
        int alphaFramesOffset = offset + BinaryFormat.getInt(contents, offset + 36);
        int colorFramesOffset = offset + BinaryFormat.getInt(contents, offset + 40);
        int soundFramesOffset = offset + BinaryFormat.getInt(contents, offset + 44);
        int stringTableOffset = offset + BinaryFormat.getInt(contents, offset + 48);

        frameTimes = new float[numFrames];
        for (int i = 0; i < numFrames; ++i) {
            frameTimes[i] = BinaryFormat.getFloat(contents, frameTimesOffset + (i * 4));
        }

        if (hasTransform) {
            loadTransformTypes(contents, transformTypesOffset);

            loadTransformFrames(contents, offset, xformFramesOffset, stringTableOffset);
        }

        if (hasAlpha) {
            loadAlphaFrames(contents, offset, alphaFramesOffset, stringTableOffset);
        }

        if (hasColor) {
            loadColorFrames(contents, offset, colorFramesOffset, stringTableOffset);
        }

        if (hasSound) {
            loadSoundFrames(contents, offset, soundFramesOffset, stringTableOffset);
        }

        validateFrames();
    }

    private void loadTransformTypes(byte[] contents, int transformTypesOffset) {
        transformTypes = new TransformType[numTransforms];
        int transformTypeOffset = transformTypesOffset;
        for (int i = 0; i < numTransforms; ++i, transformTypeOffset += 4) {
            transformTypes[i] = TransformType.getByValue(BinaryFormat.getInt(contents, transformTypeOffset));
        }
    }

    private void loadTransformFrames(byte[] contents, int offset, int xformFramesOffset, int stringTableOffset) {
        transformFrames = new KeyFrameTransform[numTransforms][];
        int transformOffset = xformFramesOffset;
        for (int i = 0; i < numTransforms; ++i, transformOffset += 4) {
            int frameOffset = BinaryFormat.getInt(contents, transformOffset);
            if (frameOffset > 0) {
                frameOffset += offset;
                transformFrames[i] = new KeyFrameTransform[numFrames];
                for (int j = 0; j < numFrames; ++j, frameOffset += 4) {
                    int framePointer = BinaryFormat.getInt(contents, frameOffset);
                    if (framePointer > 0) {
                        framePointer += offset;
                        transformFrames[i][j] = new KeyFrameTransform(contents, framePointer, stringTableOffset, transformTypes[i]);
                    }
                }
            }
        }
    }

    private void loadAlphaFrames(byte[] contents, int offset, int alphaFramesOffset, int stringTableOffset) {
        alphaFrames = new KeyFrameAlpha[numFrames];
        int frameOffset = alphaFramesOffset;
        for (int i = 0; i < numFrames; ++i, frameOffset += 4) {
            int framePointer = BinaryFormat.getInt(contents, frameOffset);
            if (framePointer > 0) {
                framePointer += offset;
                alphaFrames[i] = new KeyFrameAlpha(contents, framePointer, stringTableOffset);
            }
        }
    }

    private void loadColorFrames(byte[] contents, int offset, int colorFramesOffset, int stringTableOffset) {
        colorFrames = new KeyFrameColor[numFrames];
        int frameOffset = colorFramesOffset;
        for (int i = 0; i < numFrames; ++i, frameOffset += 4) {
            int framePointer = BinaryFormat.getInt(contents, frameOffset);
            if (framePointer > 0) {
                framePointer += offset;
                colorFrames[i] = new KeyFrameColor(contents, framePointer, stringTableOffset);
            }
        }
    }

    private void loadSoundFrames(byte[] contents, int offset, int soundFramesOffset, int stringTableOffset) {
        soundFrames = new KeyFrameSound[numFrames];
        int frameOffset = soundFramesOffset;
        for (int i = 0; i < numFrames; ++i, frameOffset += 4) {
            int framePointer = BinaryFormat.getInt(contents, frameOffset);
            if (framePointer > 0) {
                framePointer += offset;
                soundFrames[i] = new KeyFrameSound(contents, framePointer, stringTableOffset);
            }
        }
    }

    public void validateFrames()
    {
        if (hasTransform) {
            for (int frame = 0; frame < numFrames; frame++) {
                boolean isNull = transformFrames[0][frame] == null;
                if (isNull) {
                }
                else {
                }
            }

            for (int i = 0; i < transformTypes.length; i++) {
                validateFrameList(transformFrames[i]);
                TransformType transformType = transformTypes[i];
                for (int j = 0; j < transformFrames[i].length; j++) {
                    KeyFrame frame = transformFrames[i][j];
                    if (frame != null) {
                        if (transformType == TransformType.SCALE && frame.x == -1) throw new AssertionError(frame);
                        if (transformType == TransformType.ROTATE && frame.x != -1) throw new AssertionError(frame);
                        if (transformType == TransformType.TRANSLATE && frame.x == -1) throw new AssertionError(frame);
                        if (transformType == TransformType.SCALE && frame.y == -1) throw new AssertionError(frame);
                        if (transformType == TransformType.ROTATE && frame.y != -1) throw new AssertionError(frame);
                        if (transformType == TransformType.TRANSLATE && frame.y == -1) throw new AssertionError(frame);
                        if (transformType == TransformType.ROTATE && frame.angle == -1) throw new AssertionError(frame);
                        if (transformType != TransformType.ROTATE && frame.angle != -1) throw new AssertionError(frame);
                        if (frame.alpha != -1) throw new AssertionError(frame);
                        if (frame.color != -1) throw new AssertionError(frame);
                        if (frame.soundStrIndex > 0) throw new AssertionError(frame);
                        if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
                    }
                }
            }
        }

        if (hasAlpha) {
            validateFrameList(alphaFrames);
            for (KeyFrame frame : alphaFrames) {
                if (frame != null) {
                    if (frame.x != -1) throw new AssertionError(frame);
                    if (frame.y != -1) throw new AssertionError(frame);
                    if (frame.angle != -1) throw new AssertionError(frame);
                    if (frame.alpha == -1) throw new AssertionError(frame);
                    if (frame.color != -1) throw new AssertionError(frame);
                    if (frame.soundStrIndex > 0) throw new AssertionError(frame);
                    if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
                }
            }
        }

        if (hasColor) {
            validateFrameList(colorFrames);
            for (KeyFrame frame : colorFrames) {
                if (frame != null) {
                    if (frame.x != -1) throw new AssertionError(frame);
                    if (frame.y != -1) throw new AssertionError(frame);
                    if (frame.angle != -1) throw new AssertionError(frame);
                    if (frame.alpha != -1) throw new AssertionError(frame);
//          if (frame.color == -1) throw new AssertionError(frame); //TODO
                    if (frame.soundStrIndex > 0) throw new AssertionError(frame);
//          if (frame.interpolationType != 0) throw new AssertionError(frame);
                    if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
                }
            }
        }

        if (hasSound) {
            validateFrameList(soundFrames);
            for (KeyFrame frame : soundFrames) {
                if (frame != null) {
                    if (frame.x != -1) throw new AssertionError(frame);
                    if (frame.y != -1) throw new AssertionError(frame);
                    if (frame.angle != -1) throw new AssertionError(frame);
                    if (frame.alpha != -1) throw new AssertionError(frame);
                    if (frame.color != -1) throw new AssertionError(frame);
                    if (frame.soundStrIndex < 1) throw new AssertionError(frame);
                    if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
                }
            }
        }
    }

    private void validateFrameList(KeyFrame[] frameList)
    {
        // assert that no skipped frames exist
        boolean[] used = new boolean[numFrames];
        int pos = 0;
        // locate the first frame
        while (frameList[pos] == null && pos < frameList.length) {
            pos++;
        }
        if (pos >= numFrames) {
            throw new AssertionError("No frames found!");
        }

        while (pos != -1) {
            used[pos] = true;
            pos = frameList[pos].nextFrameIndex;
        }
        for (int i = 0; i < numFrames; i++) {
            if (!used[i] && frameList[i] != null)
                throw new AssertionError("frame " + i + " exists but is unused");
            if (used[i] && frameList[i] == null)
                throw new AssertionError("frame " + i + " is used but doesn't exist");
        }
    }

    public float[] getFrameTimes() {
        return frameTimes;
    }

    public boolean isHasAlpha() {
        return hasAlpha;
    }

    public boolean isHasColor() {
        return hasColor;
    }

    public boolean isHasSound() {
        return hasSound;
    }

    public boolean isHasTransform() {
        return hasTransform;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public int getNumTransforms() {
        return numTransforms;
    }

    public KeyFrameAlpha[] getAlphaFrames() {
        return alphaFrames;
    }

    public KeyFrameColor[] getColorFrames() {
        return colorFrames;
    }

    public KeyFrameSound[] getSoundFrames() {
        return soundFrames;
    }

    public KeyFrameTransform[][] getTransformFrames() {
        return transformFrames;
    }

    public List<KeyFrame> getKeyFrames() {
        return keyFrames;
    }

    public TransformType[] getTransformTypes() {
        return transformTypes;
    }

    public void setAlphaFrames(KeyFrameAlpha[] alphaFrames) {
        this.alphaFrames = alphaFrames;
    }

    public void setColorFrames(KeyFrameColor[] colorFrames) {
        this.colorFrames = colorFrames;
    }

    public void setFrameTimes(float[] frameTimes) {
        this.frameTimes = frameTimes;
    }

    public void setHasAlpha(boolean hasAlpha) {
        this.hasAlpha = hasAlpha;
    }

    public void setHasColor(boolean hasColor) {
        this.hasColor = hasColor;
    }

    public void setHasSound(boolean hasSound) {
        this.hasSound = hasSound;
    }

    public void setHasTransform(boolean hasTransform) {
        this.hasTransform = hasTransform;
    }

    public void setKeyFrames(List<KeyFrame> keyFrames) {
        this.keyFrames = keyFrames;
    }

    public void setNumFrames(int numFrames) {
        this.numFrames = numFrames;
    }

    public void setNumTransforms(int numTransforms) {
        this.numTransforms = numTransforms;
    }

    public void setSoundFrames(KeyFrameSound[] soundFrames) {
        this.soundFrames = soundFrames;
    }

    public void setTransformFrames(KeyFrameTransform[][] transformFrames) {
        this.transformFrames = transformFrames;
    }

    public void setTransformTypes(TransformType[] transformTypes) {
        this.transformTypes = transformTypes;
    }
}
