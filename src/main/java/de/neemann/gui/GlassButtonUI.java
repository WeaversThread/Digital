/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import com.formdev.flatlaf.ui.FlatButtonUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Button UI of the glass look: on hover the content grows to 1.08 with a spring
 * (0.2s, bounce 0.3) and brightens. A button with the client property {@link #ACTIVE}
 * set to true is drawn on a capsule, its icon in the focus accent.
 */
public class GlassButtonUI extends FlatButtonUI {
    /**
     * client property marking a button as the indicator of an active state
     */
    public static final String ACTIVE = "glass.active";

    private static final double HOVER_SCALE = 1.08;
    private static final double DURATION = 0.2;
    private static final double BOUNCE = 0.3;
    private static final double STIFFNESS = Math.pow(2 * Math.PI / DURATION, 2);
    private static final double DAMPING = 4 * Math.PI * (1 - BOUNCE) / DURATION;
    private static final int FRAME_MS = 1000 / 60;
    private static final int SUB_STEPS = 4;

    private AbstractButton button;
    private MouseAdapter hoverListener;
    private Timer timer;
    private double scale = 1;
    private double velocity;
    private double target = 1;

    /**
     * Creates the UI for the given component. Every button gets its own instance,
     * because the instance holds the animation state.
     *
     * @param c the component
     * @return the UI
     */
    public static ComponentUI createUI(JComponent c) {
        return new GlassButtonUI();
    }

    /**
     * Creates a new instance
     */
    public GlassButtonUI() {
        super(false);
    }

    @Override
    protected void installListeners(AbstractButton b) {
        super.installListeners(b);
        button = b;
        hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateTo(button.isEnabled() ? HOVER_SCALE : 1);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateTo(1);
            }
        };
        b.addMouseListener(hoverListener);
    }

    @Override
    protected void uninstallListeners(AbstractButton b) {
        super.uninstallListeners(b);
        b.removeMouseListener(hoverListener);
        if (timer != null)
            timer.stop();
        timer = null;
        scale = 1;
        velocity = 0;
        target = 1;
        button = null;
    }

    private void animateTo(double newTarget) {
        target = newTarget;
        if (timer == null) {
            timer = new Timer(FRAME_MS, e -> step());
            timer.setCoalesce(true);
        }
        if (!timer.isRunning())
            timer.start();
    }

    private void step() {
        double dt = FRAME_MS / 1000.0 / SUB_STEPS;
        for (int i = 0; i < SUB_STEPS; i++) {
            double force = -STIFFNESS * (scale - target) - DAMPING * velocity;
            velocity += force * dt;
            scale += velocity * dt;
        }
        if (Math.abs(velocity) < 0.002 && Math.abs(scale - target) < 0.0005) {
            scale = target;
            velocity = 0;
            timer.stop();
        }
        if (button != null)
            button.repaint();
    }

    @Override
    public void update(Graphics g, JComponent c) {
        if (Boolean.TRUE.equals(c.getClientProperty(ACTIVE))) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Glass.white(0.14f));
            float inset = 1;
            float h = c.getHeight() - 2 * inset;
            g2.fill(new RoundRectangle2D.Float(inset, inset, c.getWidth() - 2 * inset, h, h, h));
            g2.dispose();
        }
        super.update(g, c);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (scale == 1) {
            super.paint(g, c);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        double cx = c.getWidth() / 2.0;
        double cy = c.getHeight() / 2.0;
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.translate(-cx, -cy);
        super.paint(g2, c);
        g2.dispose();
    }
}
