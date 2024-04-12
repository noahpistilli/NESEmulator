package com.wiilink24.emu.core;

import com.wiilink24.emu.NES;

import java.util.Arrays;

// WRAM is the RAM that the CPU works within.
public class WRAM {
    private final int[] wram = new int[2048];

    private NES nes;

    public WRAM(NES nes) {
        this.nes = nes;
        // init memory
        Arrays.fill(wram, 0xff);
    }

    public final int read(int addr) {
        return _read(addr);
    }

    public final int _read(final int addr) {
        if (addr > 0x4018) {
            return nes.getMapper().cartRead(addr);
        } else if (addr <= 0x1fff) {
            return wram[addr & 0x7FF];
        } else if (addr <= 0x3fff) {
            // 8 byte ppu regs; mirrored lots
            return nes.getPPU().read(addr & 7);
        } else {
           return nes.getAPU().read(addr - 0x4000);
        }
    }

    public final void write(int addr, int data) {
        if (addr > 0x4018) {
            nes.getMapper().cartWrite(addr, data);
        } else if (addr <= 0x1fff) {
            wram[addr & 0x7FF] = data;
        } else if (addr <= 0x3fff) {
            // 8 byte ppu regs; mirrored lots
            nes.getPPU().write(addr & 7, data);
        } else {
            nes.getAPU().write(addr - 0x4000, data);
        }
    }
}
