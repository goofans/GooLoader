package me.wy.gooloader.gooloader.util;

// Original code by David Croft (davidc@goofans.com)
public class BinaryFormat
{
    public static float getFloat(byte[] arr, int offset)
    {
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, offset++) {
            accum |= ((long) (arr[offset] & 0xff)) << shiftBy;
        }
        return Float.intBitsToFloat(accum);
    }

    public static int getInt(byte[] arr, int offset)
    {

        int accum = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, offset++) {
            accum |= ((long) (arr[offset] & 0xff)) << shiftBy;
        }
        return accum;
    }

    public static long getLong(byte[] arr, int offset)
    {

        long accum = 0;
        for (int shiftBy = 0; shiftBy < 64; shiftBy += 8, offset++) {
            accum |= ((long) (arr[offset] & 0xff)) << shiftBy;
        }
        return accum;
    }

    public static String getString(byte[] contents, int i)
    {
        StringBuilder sb = new StringBuilder();
        while (contents[i] != 0) {
            sb.append((char) contents[i]);
            ++i;
        }
        return sb.toString();
    }
}
