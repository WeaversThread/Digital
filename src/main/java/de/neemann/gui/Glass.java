/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import java.awt.*;

/**
 * The color tokens of the glass design: white at varying opacities on a dark surface,
 * plus the functional accents. All tokens are pre-blended onto {@link #BASE} so they are opaque.
 */
public final class Glass {
    /**
     * the dark glass surface every token is blended onto
     */
    public static final Color BASE = new Color(0x1E, 0x1E, 0x1E);

    /**
     * white @ 0.85: text, active icons
     */
    public static final Color PRIMARY = white(0.85f);
    /**
     * white @ 0.7: icons, buttons
     */
    public static final Color SECONDARY = white(0.7f);
    /**
     * white @ 0.45: labels
     */
    public static final Color TERTIARY = white(0.45f);
    /**
     * white @ 0.35: hints, status
     */
    public static final Color QUATERNARY = white(0.35f);
    /**
     * white @ 0.25: disabled icons
     */
    public static final Color MUTED = white(0.25f);
    /**
     * white @ 0.12: borders
     */
    public static final Color SUBTLE = white(0.12f);
    /**
     * white @ 0.08: dividers, input and button backgrounds
     */
    public static final Color SURFACE = white(0.08f);
    /**
     * white @ 0.06: sections, stripes
     */
    public static final Color SURFACE_DIM = white(0.06f);

    /**
     * the focus accent, Braun orange: a high signal, a running simulation
     */
    public static final Color ORANGE = new Color(0xFF, 0x6B, 0x1A);
    /**
     * the rest accent, blue: selection
     */
    public static final Color BLUE = new Color(0x0A, 0x84, 0xFF);
    /**
     * functional red: errors, failed tests
     */
    public static final Color RED = new Color(0xFF, 0x45, 0x3A);
    /**
     * functional green: passed tests
     */
    public static final Color GREEN = new Color(0x32, 0xD7, 0x4B);

    private Glass() {
    }

    /**
     * Returns white at the given opacity, blended onto the base surface.
     *
     * @param alpha the opacity
     * @return the opaque color
     */
    public static Color white(float alpha) {
        return tint(Color.WHITE, alpha);
    }

    /**
     * Returns the given color at the given opacity, blended onto the base surface.
     *
     * @param color the color
     * @param alpha the opacity
     * @return the opaque color
     */
    public static Color tint(Color color, float alpha) {
        return new Color(
                blend(color.getRed(), BASE.getRed(), alpha),
                blend(color.getGreen(), BASE.getGreen(), alpha),
                blend(color.getBlue(), BASE.getBlue(), alpha));
    }

    private static int blend(int c, int base, float alpha) {
        return Math.round(c * alpha + base * (1 - alpha));
    }
}
