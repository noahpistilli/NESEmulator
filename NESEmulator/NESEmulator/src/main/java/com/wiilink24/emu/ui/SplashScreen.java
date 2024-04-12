package com.wiilink24.emu.ui;

import javax.swing.*;

public class SplashScreen extends JFrame {

    public SplashScreen() {
        // Set up the frame
        this.setSize(800, 700); // Set the size of the frame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true); // Remove frame decorations (optional)

        // Create an ImageIcon from the GIF file
        ImageIcon imageIcon = new ImageIcon("NESEmulator\\SplashScreen\\Comp1-ezgif.com-speed.gif");

        // Create a label and set the icon
        JLabel label = new JLabel(imageIcon);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        this.add(label); // Add the label to the frame

        // Display the frame
        this.setVisible(true);

        // Timer to close the splash screen after some time
        new Timer(2520, e -> dispose()).start(); // Change 5000 to the display time in milliseconds
    }

    public static void main(String[] args) {
        // Run the splash screen
        SwingUtilities.invokeLater(SplashScreen::new);
    }
}


