package com.wiilink24.emu.ui;


import com.wiilink24.emu.NES;

/**
 *
 * @author Andrew
 */
public class FrameLimiter {

    NES nes;
    private long sleepingtest = 0;
    public long FRAME_NS;

    public FrameLimiter(NES nes, long framens) {
        this.nes = nes;
        FRAME_NS = framens;
        //forceHighResolutionTimer();
    }

    public void setInterval(long ns) {
        FRAME_NS = ns;
    }

    public void sleep() {
        final long timeleft = System.nanoTime() - nes.frameStartTime;
        if (timeleft < FRAME_NS) {
            final long sleepytime = (FRAME_NS - timeleft + sleepingtest);
            if (sleepytime < 0) {
                return;
                //don't sleep at all.
            }
            sleepingtest = System.nanoTime();
            try {
                //System.err.println(sleepytime/ 1000000.);
                Thread.sleep(sleepytime / 1000000);
                // sleep for rest of the time until the next frame
            } catch (InterruptedException ex) {
            }
            sleepingtest = System.nanoTime() - sleepingtest;
            //now sleeping test has how many ns the sleep *actually* was
            sleepingtest = sleepytime - sleepingtest;
            //now sleepingtest has how much the next frame needs to be delayed by to make things match
        }
    }

    public void sleepFixed() {
        try {
            //sleep for 16 ms
            Thread.sleep(16);
        } catch (InterruptedException ex) {
        }

    }

    public static void forceHighResolutionTimer() {
        //UGLY HACK ALERT: Just realized why sleep() rounds to nearest
        //multiple of 10: it's because no other program is using high resolution timer.
        //This should, hopefully, fix that.
        final Thread daemon = new Thread("ForceHighResolutionTimer") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(99999999999L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        daemon.setDaemon(true);
        daemon.start();
    }

}