package com.wiilink24.emu.mappers;

import com.wiilink24.emu.NES;
import com.wiilink24.emu.core.ROM;

import java.io.IOException;

public class NROM extends AbstractMapper {
    public NROM(NES nes) {
        this.nes = nes;
    }

    @Override
    public void loadROM(ROM rom) throws IOException {
        super.loadROM(rom);
        //copy the whole rom around so we need to do less math

        int[] shiftedprg = new int[65536];
        System.arraycopy(prgROM, 0, shiftedprg, 0x8000, prgROM.length);
        if (rom.getPrgSize() <= 16384) {
            //double up the rom if 16k
            System.arraycopy(prgROM, 0, shiftedprg, 0xc000, prgROM.length);
        }
        prgROM = shiftedprg;
    }

    @Override
    public int cartRead(int addr) {
        if (addr >= 0x8000) {
            return prgROM[addr];
        } else if (addr >= 0x6000) {
            return prgRAM[addr & 0x1fff];
        }
        return addr >> 8; //open bus
    }

    @Override
    public int ppuRead(int addr) {
        if (addr < 0x2000) {
            return chrROM[addr];
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
}
