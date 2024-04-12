package com.wiilink24.emu.audio;

/**
 *
 * @author Andrew
 */
public interface ExpansionSoundChip {

    public void clock(final int cycles);

    public void write(int register, int data);

    public int getval();
}