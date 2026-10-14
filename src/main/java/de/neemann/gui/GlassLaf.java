/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

/**
 * The dark glass look and feel. The colors are defined in GlassLaf.properties,
 * which FlatLaf loads automatically.
 */
public class GlassLaf extends FlatDarkLaf {
    /**
     * the name of the look and feel
     */
    public static final String NAME = "Glass";

    /**
     * Installs the glass look and feel.
     *
     * @return true if the look and feel was installed
     */
    public static boolean setup() {
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
