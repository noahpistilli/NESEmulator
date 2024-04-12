package com.wiilink24.emu.mappers;

import com.wiilink24.emu.Utilities;
import com.wiilink24.emu.NES;
import com.wiilink24.emu.core.ROM;

import java.io.IOException;

public class MMC1 extends AbstractMapper {
    
    private int chrSize;
    
    private int prgSize;

    private int mmc1shift = 0;

    private int mmc1latch = 0;

    private int mmc1ctrl = 0xc;

    private int mmc1chr0 = 0;

    private int mmc1chr1 = 0;

    private int mmc1prg = 0;

    private boolean soromlatch = false;

    private long frameCountPrevious = 0;

    private double cpuCyclePrevious = 0; // for Bill and Ted fix

    public MMC1(NES nes) {
        this.nes = nes;
    }

    @Override
    public void loadROM(ROM rom) throws IOException {
        super.loadROM(rom);
        
        prgSize = rom.getPrgSize();
        chrSize = rom.getChrSize();

        setBanks();
    }

    @Override
    public final void cartWrite(int addr, int data) {
        if (addr < 0x8000 || addr > 0xffff) {
            super.cartWrite(addr, data);
            return;
        }

        if (nes.getCPU().clocks == cpuCyclePrevious && nes.framecount == frameCountPrevious) {
            return; //bill and ted fix - prevents 2 writes too close together
            //from being acknowledged
            //if I ever go to a cycle based core instead of opcode based this needs to change.
        }

        frameCountPrevious = nes.framecount;
        cpuCyclePrevious = nes.getCPU().clocks;

        if (((data & (Utilities.BIT7)) != 0)) {
            // reset shift register
            mmc1shift = 0;
            mmc1latch = 0;
            mmc1ctrl |= 0xc;
            setBanks();
            return;
        }

        mmc1shift = (mmc1shift >> 1) + (data & 1) * 16;
        ++mmc1latch;

        if (mmc1latch < 5) {
            // no need to do anything
        } else {
            if (addr <= 0x9fff) {
                // mmc1control
                mmc1ctrl = mmc1shift & 0x1f;
                MirrorType mirtype = switch (mmc1ctrl & 3) {
                    case 0 -> MirrorType.SS_MIRROR0;
                    case 1 -> MirrorType.SS_MIRROR1;
                    case 2 -> MirrorType.V_MIRROR;
                    default -> MirrorType.H_MIRROR;
                };
                setMirroring(mirtype);

            } else if (addr <= 0xbfff) {
                // mmc1chr0
                mmc1chr0 = mmc1shift & 0x1f;
                if (prgSize > 262144) {
                    //SOROM boards use the high bit of CHR to switch between 1st and last
                    //256k of the PRG ROM
                    mmc1chr0 &= 0xf;
                    soromlatch = ((mmc1shift & (Utilities.BIT4)) != 0);
                }
            } else if (addr <= 0xdfff) {
                // mmc1chr1
                mmc1chr1 = mmc1shift & 0x1f;
                if (prgSize > 262144) {
                    mmc1chr1 &= 0xf;
                }
            } else {
                // mmc1prg
                mmc1prg = mmc1shift & 0xf;
            }

            setBanks();
            mmc1latch = 0;
            mmc1shift = 0;
        }
    }

    private void setBanks() {
        if (((mmc1ctrl & (Utilities.BIT4)) != 0)) {
            // 4k bank mode
            for (int i = 0; i < 4; ++i) {
                chrMap[i] = (1024 * (i + 4 * mmc1chr0)) % chrSize;
            }
            for (int i = 0; i < 4; ++i) {
                chrMap[i + 4] = (1024 * (i + 4 * mmc1chr1)) % chrSize;
            }
        } else {
            // 8k bank mode
            for (int i = 0; i < 8; ++i) {
                chrMap[i] = (1024 * (i + 8 * (mmc1chr0 >> 1))) % chrSize;
            }
        }

        // prg bank
        if ((mmc1ctrl & (Utilities.BIT3)) == 0) {
            // 32k switch
            // ignore low bank bit
            for (int i = 0; i < 32; ++i) {
                prgMap[i] = (1024 * i + 32768 * (mmc1prg >> 1)) % prgSize;
            }

        } else if ((mmc1ctrl & (Utilities.BIT2)) == 0) {
            // fix 1st bank, 16k switch 2nd bank
            for (int i = 0; i < 16; ++i) {
                prgMap[i] = (1024 * i);
            }
            for (int i = 0; i < 16; ++i) {
                prgMap[i + 16] = (1024 * i + 16384 * mmc1prg) % prgSize;
            }
        } else {
            // fix last bank, switch 1st bank
            for (int i = 0; i < 16; ++i) {
                prgMap[i] = (1024 * i + 16384 * mmc1prg) % prgSize;
            }
            for (int i = 1; i <= 16; ++i) {
                prgMap[32 - i] = (prgSize - (1024 * i));
                if ((prgMap[32 - i]) > 262144) {
                    prgMap[32 - i] -= 262144;
                }
            }
        }
        //if more thn 256k ROM AND SOROM latch is on
        if (soromlatch && (prgSize > 262144)) {
            //add 256k to all the prg bank #s
            for (int i = 0; i < prgMap.length; ++i) {
                prgMap[i] += 262144;
            }
        }
    }
}
