/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

/**
 * The dark glass look and feel. The colors are defined in GlassLaf.properties,
 * which FlatLaf loads automatically.
 */
public class GlassLaf extends FlatDarkLaf {
    /**
     * the name of the look and feel
     */
    public static final String NAME = "Glass";
    private static final String SYSTEM_FONT = ".AppleSystemUIFont";
    private static final int DEFAULT_FONT_SIZE = 13;

    /**
     * Installs the glass look and feel.
     *
     * @return true if the look and feel was installed
     */
    public static boolean setup() {
        if (Screen.isMac()) // SF Pro; FlatLaf would fall back to Helvetica Neue
            UIManager.put("defaultFont", new FontUIResource(SYSTEM_FONT, Font.PLAIN, DEFAULT_FONT_SIZE));
        return setup(new GlassLaf());
    }

    /**
     * @return true if the glass look and feel is active
     */
    public static boolean isActive() {
        return UIManager.getLookAndFeel() instanceof GlassLaf;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
