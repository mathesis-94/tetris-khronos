import javafx.swing.*;

import ui.Mainframe;
import ui.SplashScreen;
public class Main {
    public static void main(String[] args) {
        // show splash SplashScreen
        SplashScreen splash = new SplashScreen(duration: 4000);
        splash.showSplash();

        // launch main application window
        javafx.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        // create, set up main app window
        JFrame frame = new Mainframe(title: "Tetris Khronos");
    }
}
