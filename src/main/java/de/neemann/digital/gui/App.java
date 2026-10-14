/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.gui.GlassLaf;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.QuitStrategy;
import java.io.File;

/**
 * Entry point of the application. Installs the glass look and feel before any GUI class
 * is loaded and connects Digital to the desktop: files opened in the Finder, the about
 * menu item and quitting, which closes every window so unsaved circuits are not lost.
 */
public final class App {

    private App() {
    }

    /**
     * Starts the app
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        if (Screen.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "Digital");
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
        }
        int fontScaling = Settings.getInstance().get(de.neemann.digital.core.element.Keys.SETTINGS_FONT_SCALING);
        if (fontScaling != 100 && System.getProperty("flatlaf.uiScale") == null)
            System.setProperty("flatlaf.uiScale", Float.toString(fontScaling / 100f));
        GlassLaf.setup();

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE))
                desktop.setOpenFileHandler(e -> SwingUtilities.invokeLater(() -> {
                    for (File f : e.getFiles())
                        Main.openFromSystem(f);
                }));
            if (desktop.isSupported(Desktop.Action.APP_ABOUT))
                desktop.setAboutHandler(e -> Main.showAbout(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()));
            if (desktop.isSupported(Desktop.Action.APP_QUIT_STRATEGY))
                desktop.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
        }

        Main.main(args);
    }
}
