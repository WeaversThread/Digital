/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The line icons of the glass look. They replace the PNG icons of the same name.
 * Icons are drawn with thin round strokes; their color follows the state of the
 * component they are painted on: secondary at rest, primary on hover, muted when disabled
 * and the focus accent when the component is the indicator of an active state.
 */
public final class GlassIcons {
    private static final HashMap<String, Icon> ICONS = new HashMap<>();
    private static final int TOOL = 20;
    private static final float STROKE = 1.4f;

    static {
        tool("document-new", "M5.5 2.75 H11.5 L15 6.25 V17.25 H5.5 Z M11.5 2.75 V6.25 H15 M10.25 9.25 V14.25 M7.75 11.75 H12.75");
        tool("document-new-sub", "M5.5 2.75 H11.5 L15 6.25 V17.25 H5.5 Z M11.5 2.75 V6.25 H15 M8 9.5 H12.5 V13.75 H8 Z");
        tool("document-open", "M2.75 5.5 Q2.75 4.25 4 4.25 H7.5 L9.25 6 H16 Q17.25 6 17.25 7.25 V14.75 Q17.25 16 16 16 H4 Q2.75 16 2.75 14.75 Z M2.75 8.5 H17.25");
        tool("document-open-new", "M2.75 5.5 Q2.75 4.25 4 4.25 H7.5 L9.25 6 H16 Q17.25 6 17.25 7.25 V14.75 Q17.25 16 16 16 H4 Q2.75 16 2.75 14.75 Z M10 9.75 V13.75 M8 11.75 H12");
        tool("document-save", "M10 3 V12 M6.5 8.5 L10 12 L13.5 8.5 M3.75 11.5 V15.75 Q3.75 16.75 4.75 16.75 H15.25 Q16.25 16.75 16.25 15.75 V11.5");
        tool("document-save-as", "M3.75 11.5 V15.75 Q3.75 16.75 4.75 16.75 H15.25 Q16.25 16.75 16.25 15.75 V11.5 M7.75 12.75 L8.25 10.25 L14 4.5 L16 6.5 L10.25 12.25 Z");
        tool("edit-undo", "M7 4.75 L3.75 8 L7 11.25 M3.75 8 H12 Q16.25 8 16.25 12 Q16.25 16 12 16 H9");
        tool("edit-redo", "M13 4.75 L16.25 8 L13 11.25 M16.25 8 H8 Q3.75 8 3.75 12 Q3.75 16 8 16 H11");
        tool("edit-copy", "M7.25 8.25 Q7.25 7 8.5 7 H15 Q16.25 7 16.25 8.25 V15.75 Q16.25 17 15 17 H8.5 Q7.25 17 7.25 15.75 Z M12.75 4.25 Q12.75 3 11.5 3 H5 Q3.75 3 3.75 4.25 V11.75 Q3.75 13 5 13");
        tool("delete", "M3.75 5.75 H16.25 M8 5.75 V4 Q8 3.25 8.75 3.25 H11.25 Q12 3.25 12 4 V5.75 M5.5 5.75 L6.4 16 Q6.5 16.75 7.25 16.75 H12.75 Q13.5 16.75 13.6 16 L14.5 5.75");
        tool("View-zoom-in", "O 8.75 8.75 5.5 M12.75 12.75 L16.75 16.75 M8.75 6.5 V11 M6.5 8.75 H11");
        tool("View-zoom-out", "O 8.75 8.75 5.5 M12.75 12.75 L16.75 16.75 M6.5 8.75 H11");
        tool("View-zoom-fit", "M3.5 7 V3.5 H7 M13 3.5 H16.5 V7 M16.5 13 V16.5 H13 M7 16.5 H3.5 V13");
        tool("help", "O 10 10 7.25 M7.75 8 Q7.75 5.75 10 5.75 Q12.25 5.75 12.25 7.75 Q12.25 9.25 10.6 9.9 Q10 10.15 10 10.9 V11.5 D 10 14 0.9");
        tool("media-playback-start", "F M6.5 4.5 L15.5 10 L6.5 15.5 Z");
        tool("media-playback-start-2", "M6.5 4.5 L15.5 10 L6.5 15.5 Z");
        tool("media-playback-start-T", "M4.5 10.5 L8.25 14.25 L15.75 6");
        tool("media-seek-forward", "F M5 5 L12.5 10 L5 15 Z S M15.5 5 V15");
        tool("media-seek-forward-f", "F M3.5 5.5 L9 10 L3.5 14.5 Z M9.5 5.5 L15 10 L9.5 14.5 Z S M17 5 V15");
        tool("media-skip-forward", "F M3.75 5 L10 10 L3.75 15 Z M10.25 5 L16.5 10 L10.25 15 Z");
        tool("media-playback-stop", "F M5.5 5.5 H14.5 V14.5 H5.5 Z");
        tool("measurement-graph", "M2.75 13.5 H5.75 V6.5 H10 V13.5 H13.25 V6.5 H17.25");
        ICONS.put("media-playback-toggle", new ToggleIcon(ICONS.get("media-playback-start"), ICONS.get("media-playback-stop")));
        ICONS.put("media-playback-toggle-micro", new ToggleIcon(ICONS.get("media-playback-start-2"),
                new LineIcon(TOOL, "M5.5 5.5 H14.5 V14.5 H5.5 Z", null)));
        ICONS.put("testPassed", new LineIcon(16, "M3.25 8.25 L6.5 11.5 L12.75 4.75", Glass.GREEN));
        ICONS.put("testFailed", new LineIcon(16, "M4.5 4.5 L11.5 11.5 M11.5 4.5 L4.5 11.5", Glass.RED));
        ICONS.put("dialog-error", new LineIcon(28, "O 14 14 11 M14 8.5 V15 D 14 19 1.2", Glass.RED));
    }

    private GlassIcons() {
    }

    private static void tool(String name, String path) {
        ICONS.put(name, new LineIcon(TOOL, path, null));
    }

    /**
     * Returns the line icon which replaces the given PNG icon.
     *
     * @param pngName the name of the PNG icon, e.g. "document-open.png"
     * @return the icon or null if there is no line icon of this name
     */
    public static Icon get(String pngName) {
        String name = pngName;
        if (name.endsWith(".png"))
            name = name.substring(0, name.length() - 4);
        return ICONS.get(name);
    }

    /**
     * Returns the color an icon is painted in on the given component.
     *
     * @param c the component, maybe null
     * @return the color
     */
    public static Color colorFor(Component c) {
        if (c == null)
            return Glass.SECONDARY;
        if (!c.isEnabled())
            return Glass.MUTED;
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            if (Boolean.TRUE.equals(b.getClientProperty(GlassButtonUI.ACTIVE)))
                return Glass.ORANGE;
            ButtonModel m = b.getModel();
            if (m.isRollover() || m.isPressed() || m.isArmed())
                return Glass.PRIMARY;
        }
        return Glass.SECONDARY;
    }

    /**
     * Shows one icon at rest and another while its button is the indicator of an active state,
     * e.g. play while stopped and stop while running.
     */
    private static final class ToggleIcon implements Icon {
        private final Icon rest;
        private final Icon active;

        private ToggleIcon(Icon rest, Icon active) {
            this.rest = rest;
            this.active = active;
        }

        private Icon current(Component c) {
            if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty(GlassButtonUI.ACTIVE)))
                return active;
            return rest;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            current(c).paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return rest.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return rest.getIconHeight();
        }
    }

    private static final class Part {
        private final Shape shape;
        private final boolean fill;

        private Part(Shape shape, boolean fill) {
            this.shape = shape;
            this.fill = fill;
        }
    }

    private static final class LineIcon implements Icon {
        private final int size;
        private final Color color;
        private final ArrayList<Part> shapes;

        private LineIcon(int size, String path, Color color) {
            this.size = size;
            this.color = color;
            this.shapes = parse(path);
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            float s = getScaling();
            if (s != 1)
                g2.scale(s, s);
            Color col = color;
            if (col == null || (c != null && !c.isEnabled()))
                col = colorFor(c);
            g2.setColor(col);
            g2.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Part sh : shapes) {
                if (sh.fill)
                    g2.fill(sh.shape);
                g2.draw(sh.shape);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return Math.round(size * getScaling());
        }

        @Override
        public int getIconHeight() {
            return Math.round(size * getScaling());
        }

        private static float getScaling() {
            return Screen.getInstance().getScaling();
        }
    }

    /**
     * Parses a small subset of the SVG path syntax, all absolute:
     * M, L, H, V, Q, C, Z, plus "O cx cy r" for a circle and "D cx cy r" for a filled dot.
     * "F" starts filled shapes, "S" switches back to stroked shapes.
     */
    private static ArrayList<Part> parse(String path) {
        ArrayList<Part> shapes = new ArrayList<>();
        String[] t = path.replaceAll("([A-Z])", " $1 ").trim().split("\\s+");
        Path2D.Float p = new Path2D.Float();
        boolean fill = false;
        float cx = 0;
        float cy = 0;
        int i = 0;
        while (i < t.length) {
            String cmd = t[i++];
            switch (cmd) {
                case "F":
                case "S":
                    flush(shapes, p, fill);
                    p = new Path2D.Float();
                    fill = cmd.equals("F");
                    break;
                case "O":
                case "D":
                    float ox = Float.parseFloat(t[i++]);
                    float oy = Float.parseFloat(t[i++]);
                    float r = Float.parseFloat(t[i++]);
                    shapes.add(new Part(new Ellipse2D.Float(ox - r, oy - r, 2 * r, 2 * r), cmd.equals("D")));
                    break;
                case "M":
                    if (p.getCurrentPoint() != null) {
                        flush(shapes, p, fill);
                        p = new Path2D.Float();
                    }
                    cx = Float.parseFloat(t[i++]);
                    cy = Float.parseFloat(t[i++]);
                    p.moveTo(cx, cy);
                    break;
                case "L":
                    cx = Float.parseFloat(t[i++]);
                    cy = Float.parseFloat(t[i++]);
                    p.lineTo(cx, cy);
                    break;
                case "H":
                    cx = Float.parseFloat(t[i++]);
                    p.lineTo(cx, cy);
                    break;
                case "V":
                    cy = Float.parseFloat(t[i++]);
                    p.lineTo(cx, cy);
                    break;
                case "Q":
                    float qx = Float.parseFloat(t[i++]);
                    float qy = Float.parseFloat(t[i++]);
                    cx = Float.parseFloat(t[i++]);
                    cy = Float.parseFloat(t[i++]);
                    p.quadTo(qx, qy, cx, cy);
                    break;
                case "C":
                    float x1 = Float.parseFloat(t[i++]);
                    float y1 = Float.parseFloat(t[i++]);
                    float x2 = Float.parseFloat(t[i++]);
                    float y2 = Float.parseFloat(t[i++]);
                    cx = Float.parseFloat(t[i++]);
                    cy = Float.parseFloat(t[i++]);
                    p.curveTo(x1, y1, x2, y2, cx, cy);
                    break;
                case "Z":
                    p.closePath();
                    break;
                default:
                    throw new IllegalArgumentException("unknown path command " + cmd);
            }
        }
        flush(shapes, p, fill);
        return shapes;
    }

    private static void flush(ArrayList<Part> shapes, Path2D.Float p, boolean fill) {
        if (p.getCurrentPoint() != null)
            shapes.add(new Part(p, fill));
    }
}
