package com.wiilink24.emu.mappers;

import com.wiilink24.emu.NES;
import com.wiilink24.emu.core.ROM;

import java.io.IOException;
import java.util.Arrays;

/*
Unlike modern games, those on the NES used specific functions of the hardware.
Specific games have specific mappers; found inside their cartridge.
As we only have the ROM, we have to HLE the mapper.
 */
public abstract class AbstractMapper {

    protected int[] prgROM;

    protected int[] chrROM;

    protected int[] prgMap = new int[32];

    protected int[] chrMap = new int[8];

    // PPU (Picture Processing Unit) Memory Map as per https://www.nesdev.org/wiki/PPU_memory_map
    // 0x0 -> 0xFFF: Pattern Table 0
    // 0x1000 -> 0x1FFF: Pattern Table 1
    // Combine them for faster access
    protected int[] prgRAM = new int[8192];

    // 0x2000 -> 0x23FF: Name Table 0
    protected int[] nt0 = new int[1024];

    // 0x2400 -> 0x27FF: Name Table 1
    protected int[] nt1 = new int[1024];

    // 0x2800 -> 0x2BFF: Name Table 2
    protected int[] nt2 = new int[1024];

    // 0x2C00 -> 0: Name Table 3
    protected int[] nt3 = new int[1024];

    // A simple way to copy uninitialized memory into the name tables.
    protected final int[] pput0 = new int[0x400];
    protected final int[] pput1 = new int[0x400];
    protected final int[] pput2 = new int[0x400];
    protected final int[] pput3 = new int[0x400];

    // CPU Memory constants. Taken from https://www.nesdev.org/wiki/CPU_memory_map.
    // WRAM is the W(orking)RAM. It is also used for games such as The Legend of Zelda whom save data.
    protected int WRAM_LOW = 0x6000;
    protected int WRAM_HIGH = 0x7FFF;

    // On a real NES, this is the address space where the ROM would be loaded into.
    public static int ROM_LOW = 0x8000;
    public static int ROM_HIGH = 0xFFFF;

    public NES nes;

    public enum MirrorType {
        H_MIRROR,
        V_MIRROR,
        SS_MIRROR0,
        SS_MIRROR1,
        FOUR_SCREEN_MIRROR
    };


    public void loadROM(ROM rom) throws IOException {
        prgROM = rom.readPRGROM();
        chrROM = rom.readCHRROM();

        for (int i = 0; i < 32; ++i) {
            prgMap[i] = (1024 * i) & (rom.getPrgSize() - 1);
        }
        for (int i = 0; i < 8; ++i) {
            chrMap[i] = (1024 * i) & (rom.getChrSize() - 1);
        }

        Arrays.fill(pput0, 0xa0);
        Arrays.fill(pput1, 0xb0);
        Arrays.fill(pput2, 0xc0);
        Arrays.fill(pput3, 0xd0);
        setMirroring(rom.getScrollType());
    }

    public void cartWrite(int addr, int data) {
        // Default write to the pattern tables.
        if (addr >= WRAM_LOW && addr < ROM_LOW) {
            prgRAM[addr & 0x1fff] = data;
        }
    }

    public int cartRead(int addr) {
        if (addr >= ROM_LOW) {
            return prgROM[prgMap[((addr & 0x7fff)) >> 10] + (addr & 1023)];
        } else if (addr >= WRAM_LOW) {
            return prgRAM[addr & 0x1fff];
        }
        return addr >> 8;
    }

    public int ppuRead(int addr) {
        if (addr < 0x2000) {
            return chrROM[chrMap[addr >> 10] + (addr & 1023)];
        } else {
            switch (addr & 0xc00) {
                case 0:
                    return nt0[addr & 0x3ff];
                case 0x400:
                    return nt1[addr & 0x3ff];
                case 0x800:
                    return nt2[addr & 0x3ff];
                case 0xc00:
                default:
                    if (addr >= 0x3f00) {
                        addr &= 0x1f;
                        if (addr >= 0x10 && ((addr & 3) == 0)) {
                            addr -= 0x10;
                        }

                        return nes.getPPU().palette[addr];
                    } else {
                        return nt3[addr & 0x3ff];
                    }
            }
        }
    }

    public void ppuWrite(int addr, int data) {
        addr &= 0x3fff;
        if (addr < 0x2000) {
            chrROM[chrMap[addr >> 10] + (addr & 1023)] = data;
        } else {
            switch (addr & 0xc00) {
                case 0x0:
                    nt0[addr & 0x3ff] = data;
                    break;
                case 0x400:
                    nt1[addr & 0x3ff] = data;
                    break;
                case 0x800:
                    nt2[addr & 0x3ff] = data;
                    break;
                case 0xc00:
                    if (addr >= 0x3f00) {
                        addr &= 0x1f;
                        if (addr >= 0x10 && ((addr & 3) == 0)) {
                            addr -= 0x10;
                        }
                        nes.getPPU().palette[addr] = (data & 0x3f);
                    } else {
                        nt3[addr & 0x3ff] = data;
                    }
                    break;
                default:
                    System.err.println("where?");
            }
        }
    }

    public final void setMirroring(MirrorType type) {
        switch (type) {
            case H_MIRROR:
                nt0 = pput0;
                nt1 = pput0;
                nt2 = pput1;
                nt3 = pput1;
                break;
            case V_MIRROR:
                nt0 = pput0;
                nt1 = pput1;
                nt2 = pput0;
                nt3 = pput1;

                break;
            case SS_MIRROR0:
                nt0 = pput0;
                nt1 = pput0;
                nt2 = pput0;
                nt3 = pput0;
                break;
            case SS_MIRROR1:
                nt0 = pput1;
                nt1 = pput1;
                nt2 = pput1;
                nt3 = pput1;
                break;
            case FOUR_SCREEN_MIRROR:
            default:
                nt0 = pput0;
                nt1 = pput1;
                nt2 = pput2;
                nt3 = pput3;
                break;
        }
    }

    public void checkA12(int addr) {}

    public void cpucycle(int cycles) {}

    public void notifyscanline(final int scanline) {}

    public void init() {}

    public void setPRGRAM(final int[] newprgram) {
        prgRAM = newprgram.clone();

    }

    public int[] getPRGRam() {
        return prgRAM.clone();
    }
}
