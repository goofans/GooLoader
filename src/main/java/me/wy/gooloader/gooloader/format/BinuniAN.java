package me.wy.gooloader.gooloader.format;

import me.wy.gooloader.gooloader.format.datatypes.*;
import me.wy.gooloader.gooloader.util.BinaryFormat;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

// Huge thanks to David Croft for his work that made this possible.
public class BinuniAN {

    private String header;

    private boolean hasColor;
    private boolean hasAlpha;
    boolean hasSound;
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

    public BinuniAN(File file) throws IOException {
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

    public BinuniAN(BinltlAN binltlAN) {
        this.header = "BINUNIAN";
        this.hasColor = binltlAN.isHasColor();
        this.hasAlpha = binltlAN.isHasAlpha();
        this.hasSound = binltlAN.isHasSound();
        this.hasTransform = binltlAN.isHasTransform();
        this.numTransforms = binltlAN.getNumTransforms();
        this.numFrames = binltlAN.getNumFrames();

        this.transformTypes = binltlAN.getTransformTypes();
        this.frameTimes = binltlAN.getFrameTimes();
        this.transformFrames = binltlAN.getTransformFrames();
        this.alphaFrames = binltlAN.getAlphaFrames();
        this.colorFrames = binltlAN.getColorFrames();
        this.soundFrames = binltlAN.getSoundFrames();
    }

    private void init(byte[] contents, int offset) {
        this.header = BinaryFormat.getString(contents, offset);
        if (!header.equals("BINUNIAN")) {
            System.out.println("The file you selected is not an animation");
            return;
        }
        this.hasColor = BinaryFormat.getInt(contents, offset + 0x08) != 0;
        this.hasAlpha = BinaryFormat.getInt(contents, offset + 0x0C) != 0;
        this.hasSound = BinaryFormat.getInt(contents, offset + 0x10) != 0;
        this.hasTransform = BinaryFormat.getInt(contents, offset + 0x14) != 0;
        this.numTransforms = BinaryFormat.getInt(contents, offset + 0x18);
        this.numFrames = BinaryFormat.getInt(contents, offset + 0x1C);

        int transformTypesOffset = offset + BinaryFormat.getInt(contents, offset + 0x20);
        int frameTimesOffset = offset + BinaryFormat.getInt(contents, offset + 0x28);
        int xformFramesOffset = offset + BinaryFormat.getInt(contents, offset + 0x30);
        int alphaFramesOffset = offset + BinaryFormat.getInt(contents, offset + 0x38);
        int colorFramesOffset = offset + BinaryFormat.getInt(contents, offset + 0x40);
        int soundFramesOffset = offset + BinaryFormat.getInt(contents, offset + 0x48);
        int stringTableOffset = offset + BinaryFormat.getInt(contents, offset + 0x50);

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

        //validateFrames();


        System.out.println("hascolor "+ this.hasColor);
        System.out.println("hasalpha "+this.hasAlpha);
        System.out.println("hassound "+this.hasSound);
        System.out.println("hastransforms "+this.hasTransform);
        System.out.println("numtransforms "+this.numTransforms);
        System.out.println("numframes "+this.numFrames);
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

    public byte[] encode() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.writeBytes(header.getBytes());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hasColor ? 1 : 0).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hasAlpha ? 1 : 0).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hasSound ? 1 : 0).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(hasTransform ? 1 : 0).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(numTransforms).array());
        os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(numFrames).array());
        int offset = 88;
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // transformTypesOffset
        ByteArrayOutputStream tsTypes = new ByteArrayOutputStream();
        for (TransformType transformType : transformTypes) {
            tsTypes.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(transformType.getValue()).array());
        }
        tsTypes.close();
        offset = 88 + tsTypes.size();
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // frameTimesOffset
        ByteArrayOutputStream frameTimesArray = new ByteArrayOutputStream();
        for (float f : frameTimes) {
            frameTimesArray.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(f).array());
        }
        offset = 88 + frameTimesArray.size() + tsTypes.size();
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // xFormFramesOffset

        ByteArrayOutputStream frames = new ByteArrayOutputStream();

        offset = 88 + frameTimesArray.size() + tsTypes.size() + (numFrames * 8) + 8;
        System.out.println(tsTypes.size());
        System.out.println(frameTimesArray.size());
        System.out.println(offset);
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // alphaFramesOffset
        for (int i = 0; i < numFrames; i++) {
            if (alphaFrames != null) {
                if (alphaFrames[i] != null) {
                    frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(alphaFrames[i].alpha).array());
                } else {
                    frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
                }
            } else {
                frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
            }
        }

        offset = 88 + tsTypes.size() + frames.size() + frameTimesArray.size() + (numFrames * 8) + 8;
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // colorFramesOffset
        for (int i = 0; i < numFrames; i++) {
            if (colorFrames != null) {
                if (colorFrames[i] != null) {
                    frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(colorFrames[i].color).array());
                } else {
                    frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
                }
            } else {
                frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
            }
        }

        offset = 88 + tsTypes.size() + frames.size() + frameTimesArray.size() + (numFrames * 8) + 8;
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // soundFramesOffset
        for (int i = 0; i < numFrames; i++) {
            if (soundFrames != null) {
                if (soundFrames[i] != null) {
                    frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(soundFrames[i].color).array());
                } else {
                    frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
                }
            } else {
                frames.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
            }
        }

        offset = 88 + tsTypes.size() + frames.size() + frameTimesArray.size() + (numFrames * 8) + 8;
        os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array()); // stringTableOffset

        frameTimesArray.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(os.size() + tsTypes.size() + frameTimesArray.size() + 8).array());

        ByteArrayOutputStream stringTable = new ByteArrayOutputStream();
        ByteArrayOutputStream finalFrames = new ByteArrayOutputStream();

        if (hasTransform) {
            for (KeyFrameTransform[] keyFrameTransforms : transformFrames) {
                for (KeyFrameTransform keyFrameTransform : keyFrameTransforms) {
                    frameTimesArray.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset + finalFrames.size() + 8).array());
                    if (keyFrameTransform != null) {
                        if (keyFrameTransform.soundStr != null)
                            stringTable.write(keyFrameTransform.soundStr.getBytes());
                        finalFrames.write(keyFrameTransform.encode());
                    }
                }
            }
        }

        if (hasAlpha) {
            for (KeyFrameAlpha keyFrameAlpha : alphaFrames) {
                frameTimesArray.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset + finalFrames.size() + 8).array());
                if (keyFrameAlpha != null) {
                    if (keyFrameAlpha.soundStr != null)
                        stringTable.write(keyFrameAlpha.soundStr.getBytes());
                    finalFrames.write(keyFrameAlpha.encode());
                }
            }
        }

        if (hasColor) {
            for (KeyFrameColor keyFrameColor : colorFrames) {
                frameTimesArray.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset + finalFrames.size() + 8).array());
                if (keyFrameColor != null) {
                    if (keyFrameColor.soundStr != null)
                        stringTable.write(keyFrameColor.soundStr.getBytes());
                    finalFrames.write(keyFrameColor.encode());
                }
            }
        }

        if (hasSound) {
            for (KeyFrameSound keyFrameSound : soundFrames) {
                frameTimesArray.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(offset + finalFrames.size() + 8).array());
                if (keyFrameSound != null) {
                    if (keyFrameSound.soundStr != null)
                        stringTable.write(keyFrameSound.soundStr.getBytes());
                    finalFrames.write(keyFrameSound.encode());
                }
            }
        }

        if (stringTable.size() == 0) {
            stringTable.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());
        }

        os.write(tsTypes.toByteArray());
        os.write(frameTimesArray.toByteArray());
        os.write(frames.toByteArray());
        os.write(stringTable.toByteArray());
        os.write(finalFrames.toByteArray());

        frameTimesArray.close();
        frames.close();
        stringTable.close();
        finalFrames.close();

        os.close();
        return os.toByteArray();
    }
}
