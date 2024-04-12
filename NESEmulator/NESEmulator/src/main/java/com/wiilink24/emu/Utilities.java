package com.wiilink24.emu;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.CRC32;

public class Utilities {

    public final static int BIT0 = 1;
    public final static int BIT1 = 1 << 1;
    public final static int BIT2 = 1 << 2;
    public final static int BIT3 = 1 << 3;
    public final static int BIT4 = 1 << 4;
    public final static int BIT5 = 1 << 5;
    public final static int BIT6 = 1 << 6;
    public final static int BIT7 = 1 << 7;
    public final static int BIT8 = 1 << 8;
    public final static int BIT9 = 1 << 9;
    public final static int BIT10 = 1 << 10;
    public final static int BIT11 = 1 << 11;
    public final static int BIT12 = 1 << 12;
    public final static int BIT13 = 1 << 13;
    public final static int BIT14 = 1 << 14;
    public final static int BIT15 = 1 << 15;


    public static int reverseByte(int nibble) {
        //reverses 8 bits packed into int.
        return (Integer.reverse(nibble) >> 24) & 0xff;
    }

    public static String hex(final int num) {
        String s = Integer.toHexString(num).toUpperCase(Locale.US);
        if ((s.length() & 1) == 1) {
            s = "0" + s;
        }
        return s;
    }

    public static int[] byteArrayToIntArray(byte[] byteArray) {
        int[] intArray = new int[byteArray.length];

        for (int i = 0; i < byteArray.length; i++) {
            // Convert each byte to an unsigned integer
            intArray[i] = byteArray[i] & 0xFF;
        }

        return intArray;
    }

    public static String getExtension(final String s) {
        if (s == null || s.equals("")) {
            return "";
        }
        int split = s.lastIndexOf('.');
        if (split < 0) {
            return "";
        }
        return s.substring(split);

    }

    public static String stripExtension(final File f) {
        String s = f.getName();
        if (s == null || s.equals("")) {
            return "";
        }
        int split = s.lastIndexOf('.');
        if (split < 0) {
            return "";
        }
        return s.substring(0, split);
    }

    public static String stripExtension(final String s) {
        if (s == null || s.equals("")) {
            return "";
        }
        int split = s.lastIndexOf('.');
        if (split < 0) {
            return "";
        }
        return s.substring(0, split);
    }

    public static String getFileName(String path) {
        return new File(path).getName();
    }

    public static void writetofile(final int[] array, final String path) {
        //note: does NOT write the ints directly to the file - only the low bytes.
        AsyncWriter writer = new AsyncWriter(array, path);
        writer.run();
    }

    public static void asyncwritetofile(final int[] array, final String path) {
        //now does the file writing in the dispatch thread
        //hopefully that will eliminate annoying hitches when file system's slow
        //and not do pathological stuff like threads are prone to
        AsyncWriter writer = new AsyncWriter(array, path);
        EventQueue.invokeLater(writer);
    }

    private record AsyncWriter(int[] a, String path) implements Runnable {

        @Override
            public void run() {
                if (a != null && path != null) {
                    try {
                        FileOutputStream b = new FileOutputStream(path);
                        byte[] buf = new byte[a.length];
                        for (int i = 0; i < a.length; ++i) {
                            buf[i] = (byte) (a[i] & 0xff);
                        }
                        b.write(buf);
                        b.flush();
                        b.close();
                    } catch (IOException e) {
                        System.err.print("Could not save. ");
                        System.err.println(e);
                    }
                }
            }
        }


    public static int[] readfromfile(final String path) {
        File f = new File(path);
        byte[] bytes = new byte[(int) f.length()];
        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            fis.read(bytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Failed to load file");
            e.printStackTrace();
        }
        int[] ints = new int[bytes.length];

        for (int i = 0;
             i < bytes.length;
             i++) {
            ints[i] = (short) (bytes[i] & 0xFF);
        }

        return ints;
    }

    public static boolean exists(final String path) {
        File f = new File(path);
        return f.canRead() && !f.isDirectory();
    }
}
