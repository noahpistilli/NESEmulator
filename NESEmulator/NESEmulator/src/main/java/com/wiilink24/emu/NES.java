package com.wiilink24.emu;

import com.wiilink24.emu.Emulator;
import com.wiilink24.emu.Utilities;
import com.wiilink24.emu.core.*;
import com.wiilink24.emu.mappers.AbstractMapper;
import com.wiilink24.emu.ui.FrameLimiter;
import com.wiilink24.emu.ui.GameController;
import com.wiilink24.emu.ui.UI;

public class NES {

    private ROM rom;

    private CPU cpu;

    private PPU ppu;

    private WRAM wram;

    private APU apu;

    private AbstractMapper mapper;

    private GameController controller1;

    private GameController controller2;

    public boolean runEmulation = false;

    public long framecount;

    public long frameStartTime;

    private long frameDoneTime;

    private boolean dontSleep = false;

    private boolean frameLimiterOn = true;

    private final FrameLimiter limiter = new FrameLimiter(this, 16639267);

    public NES() {}

    public void run() {
        while (true) {
            if (runEmulation) {
                frameStartTime = System.nanoTime();
                runFrame();
                if (frameLimiterOn && !dontSleep) {
                    limiter.sleep();
                }
                frameDoneTime = System.nanoTime() - frameStartTime;
            } else {
                limiter.sleepFixed();
                if (ppu != null && framecount > 1) {
                    Emulator.getUI().render();
                }
            }
        }
    }

    public void pause() {
        // Probably will cause desyncs when we unpause but whatever
        apu.pause();

        // Read the stack for fun
        runEmulation = false;
    }

    private synchronized void runFrame() {
        //run cpu, ppu for a whole frame
        ppu.runFrame();

        //do end of frame stuff
        dontSleep = apu.bufferHasLessThan(1000);
        //if the audio buffer is completely drained, don't sleep for this frame
        //this is to prevent the emulator from getting stuck sleeping too much
        //on a slow system or when the audio buffer runs dry.

        apu.finishframe();
        cpu.modcycles();

        //render the frame
        ppu.renderFrame();
        if ((framecount & 2047) == 0) {
            //save sram every 30 seconds or so
            saveSRAM(true);
        }
        ++framecount;
        //System.err.println(framecount);
    }

    public GameController getController1() {
        return controller1;
    }

    public GameController getController2() {
        return controller2;
    }

    public synchronized void loadROM(String filePath) {
        try {
            rom = new ROM(filePath);
        } catch (Exception e) {
            UI.showErrorMessage("Failed to open ROM.");
            return;
        }

        // ROM header is already read, get a valid mapper.
        ppu = new PPU(this);
        mapper = rom.getMapper();
        try {
            mapper.loadROM(rom);
        } catch (Exception e) {
            UI.showErrorMessage("Failed to load ROM into mapper.");
            return;
        }


        wram = new WRAM(this);
        cpu = new CPU(this);
        apu = new APU(this);
        loadSRAM();
        cpu.powerUp();
        mapper.init();
        setParameters();
        runEmulation = true;
    }

    public synchronized void setParameters() {
        if (apu != null) {
            apu.setParameters();
        }

        limiter.setInterval(16639267);

    }

    public AbstractMapper getMapper() {
        return mapper;
    }

    public CPU getCPU() {
        return cpu;
    }

    public WRAM getWRAM() {
        return wram;
    }

    public PPU getPPU() {
        return ppu;
    }

    public APU getAPU() {
        return apu;
    }

    public long getFrameTime() {
        return frameDoneTime;
    }

    private void saveSRAM(final boolean async) {
        if (mapper != null) {
            if (async) {
                Utilities.asyncwritetofile(mapper.getPRGRam(), "zelda.sav");
            } else {
                Utilities.writetofile(mapper.getPRGRam(), "zelda.sav");
            }
        }
    }

    private void loadSRAM() {
        final String name = "zelda.sav";
        if (Utilities.exists(name)) {
            mapper.setPRGRAM(Utilities.readfromfile(name));
        }
    }

    public void setControllers(GameController controller1, GameController controller2) {
        this.controller1 = controller1;
        this.controller2 = controller2;
    }
}
