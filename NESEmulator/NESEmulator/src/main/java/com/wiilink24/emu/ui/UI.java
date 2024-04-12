package com.wiilink24.emu.ui;

import com.wiilink24.emu.NES;
import com.wiilink24.emu.Utilities;
import com.wiilink24.emu.video.RGBRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class UI extends JFrame {

    private Canvas canvas;

    private final int WIDTH = 400;

    private final int HEIGHT = 350;

    private BufferStrategy buffer;

    private final long[] frametimes = new long[60];

    private int frametimeptr = 0;

    private final UIEventListener listener = new UIEventListener();

    private com.wiilink24.emu.video.Renderer renderer = new RGBRenderer();

    NES nes;

    private final GameController padController1, padController2;

    String currentROMName;

    public UI(NES nes) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.nes = nes;
        padController1 = new GameController(this, 0);
        padController2 = new GameController(this, 1);
        nes.setControllers(padController1, padController2);
        padController1.startEventQueue();
        padController2.startEventQueue();
        padController1.setButtons();
        padController2.setButtons();
    }

    public synchronized void run() {
        this.setTitle("NES Emulator");
        this.setResizable(false);
        createMenuItems();
        startRenderer();

        this.getRootPane().registerKeyboardAction(listener, "Escape",
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        this.addWindowListener(listener);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.setVisible(true);
    }

    private void createMenuItems() {
        JMenuBar menus = new JMenuBar();

        // File menu
        JMenu file = new JMenu("File");

        // Open ROM
        JMenuItem item = new JMenuItem("Open ROM");
        item.addActionListener(listener);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        file.add(item);
       // file.addSeparator();

        // NES Menu
        JMenu menu = new JMenu("Menu");
        item = new JMenuItem("Pause");
        item.addActionListener(listener);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        menu.add(item);

        // Debugger menu
        JMenu debugger = new JMenu("Debugger");

        // View the stack
        item = new JMenuItem("Stack");
        item.addActionListener(listener);
        debugger.add(item);

        item = new JMenuItem("Code");
        item.addActionListener(listener);
        debugger.add(item);

        item = new JMenuItem("Memory");
        item.addActionListener(listener);
        debugger.add(item);

        menus.add(file);
        menus.add(menu);
        menus.add(debugger);
        this.setJMenuBar(menus);
    }

    private void startRenderer() {
        if (canvas != null) {
            this.remove(canvas);
        }

        renderer.setClip(8);
        canvas = new Canvas();
        canvas.setSize(WIDTH*2, HEIGHT*2);
        canvas.setEnabled(false);
        this.add(canvas);
        this.pack();
        canvas.createBufferStrategy(2);
        buffer = canvas.getBufferStrategy();
    }

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
    }

    int bgcolor;
    BufferedImage frame;
    double fps;
    int frameskip = 0;

    public final synchronized void setFrame(final int[] nextframe, final int[] bgcolors, boolean dotcrawl) {
        frametimes[frametimeptr] = nes.getFrameTime();
        ++frametimeptr;
        frametimeptr %= frametimes.length;

        if (frametimeptr == 0) {
            long averageframes = 0;
            for (long l : frametimes) {
                averageframes += l;
            }
            averageframes /= frametimes.length;
            fps = 1E9 / averageframes;
            this.setTitle(String.format("NES Emulator %s - %s, %2.2f fps"
                            + ((frameskip > 0) ? " frameskip " + frameskip : ""),
                    0.1,
                    currentROMName,
                    fps));
        }
        if (nes.framecount % (frameskip + 1) == 0) {
            frame = renderer.render(nextframe, bgcolors, dotcrawl);
            render();
        }
    }


    public synchronized void render() {
        Graphics graphics = buffer.getDrawGraphics();
        graphics.drawImage(frame, 0, 0, WIDTH*2, HEIGHT*2, null);

        graphics.dispose();
        buffer.show();
    }

    public void setCurrentROMName(String path) {
         currentROMName = Utilities.getFileName(path);
    }
}
