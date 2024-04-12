/*
 * HalfNES by Andrew Hoffman
 * Licensed under the GNU GPL Version 3. See LICENSE file
 */
package com.wiilink24.emu;

import java.util.prefs.Preferences;

/**
 *
 * @author Andrew
 */
public class PreferencesInstance {

    private static Preferences instance = null;

    protected PreferencesInstance() {
        // Exists only to defeat instantiation.
    }

    public synchronized static Preferences get() {
        if (instance == null) {
            instance = Preferences.userNodeForPackage(NES.class);
        }

        return instance;
    }
}
