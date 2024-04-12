package com.wiilink24.emu.core;

import com.wiilink24.emu.Emulator;
import com.wiilink24.emu.FileReader;
import com.wiilink24.emu.Utilities;
import com.wiilink24.emu.exceptions.ROMException;
import com.wiilink24.emu.mappers.AbstractMapper;
import com.wiilink24.emu.mappers.MMC1;
import com.wiilink24.emu.mappers.NROM;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static com.wiilink24.emu.Utilities.byteArrayToIntArray;

public class ROM {

    private final FileReader reader;

    private int mapType;

    private int prgSize;

    private int chrSize;

    private AbstractMapper.MirrorType scrollType;

    public ROM(String filePath) throws Exception {
        reader = new FileReader(filePath);
        readHeader();
    }

    public void readHeader() throws Exception {
        byte[] header = reader.readBytes(16);

        if (header[0] == 'N' && header[1] == 'E' && header[2] == 'S' && header[3] == 0x1A) {
            mapType = header[6] >> 4;
            scrollType = ((header[6] & (Utilities.BIT3)) != 0)
                    ? AbstractMapper.MirrorType.FOUR_SCREEN_MIRROR
                    : ((header[6] & (Utilities.BIT0)) != 0)
                    ? AbstractMapper.MirrorType.V_MIRROR
                    : AbstractMapper.MirrorType.H_MIRROR;

            if (((header[7] >> 2) & 3) == 2) {
                mapType += ((header[7] >> 4) << 4);
                mapType += ((header[8] & 15) << 8);

                prgSize = Math.min(16384 * (header[4] + ((header[9] & 15) << 8)), reader.getFileSize()-14);
                if (prgSize == 0) {
                    throw new ROMException("no PRG ROM size in header");
                }

                chrSize = Math.min(8192 * (header[5] + ((header[9] >> 4) << 8)), reader.getFileSize()- 16-prgSize);
                return;
            }

            prgSize = Math.min(16384*header[4], reader.getFileSize()-14);
            if (prgSize == 0) {
                throw new ROMException("no PRG ROM size in header");
            }

            chrSize = Math.min(8192*header[5], reader.getFileSize()- 16-prgSize);
            if (header[11] + header[12] + header[13] + header[14]
                    + header[15] == 0) {
                mapType += ((header[7] >> 4) << 4);
            }
            return;
        }

        throw new ROMException("invalid ROM header");
    }

    public int[] readPRGROM() throws IOException {
        // This is extremely presumptuous but our flow should allow for this to work.
        // Praying BufferedInputStream doesn't scramble the reader position.
        return byteArrayToIntArray(reader.readBytes(prgSize));
    }

    public int[] readCHRROM() throws IOException {
        // Same logic for readPRGROM applies to this
        if (chrSize == 0)
        {
            chrSize = 8192;
            return new int[8192];
        }

        return byteArrayToIntArray(reader.readBytes(chrSize));
    }

    public int getPrgSize() {
        return prgSize;
    }

    public int getChrSize() {
        return chrSize;
    }

    public AbstractMapper.MirrorType getScrollType() {
        return scrollType;
    }

    public AbstractMapper getMapper() {
        return switch (mapType) {
            case 0 -> new NROM(Emulator.getNES());
            case 1 -> new MMC1(Emulator.getNES());
            default -> null;
        };
    }
}
