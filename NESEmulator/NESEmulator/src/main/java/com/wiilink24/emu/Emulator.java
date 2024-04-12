package com.wiilink24.emu;

import com.wiilink24.emu.ui.SplashScreen;
import com.wiilink24.emu.ui.UI;

import javax.swing.*;

public class       Emulator {

    private static UI ui;

    private static NES nes;

    public static void main(String[] args) throws InterruptedException {
        SwingUtilities.invokeLater(SplashScreen::new);
        Thread.sleep(3*1000);
        nes = new NES();
        ui = new UI(nes);
        ui.run();

        // Blocks the main thread until a ROM is loaded.
        nes.run();
    }

    public static UI getUI() {
        return ui;
    }

    public static NES getNES() {
        return nes;
    }
}