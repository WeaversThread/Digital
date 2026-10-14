/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Glass;
import de.neemann.gui.GlassIcons;
import de.neemann.gui.Screen;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * The quick add palette: a search field with the matching components, opened right where
 * the user is working. Without a query it shows the recently used and the basic components.
 * Enter or a click picks a component, which then follows the mouse until it is placed.
 */
public final class QuickAdd {
    private static final String[] BASICS = {"In", "Out", "And", "Or", "Not", "XOr", "NAnd", "NOr",
            "Clock", "Button", "LED", "Const", "Splitter", "Tunnel", "Probe"};
    private static final int MAX_RESULTS = 60;
    private static final int VISIBLE_ROWS = 8;
    private static final int WIDTH = 320;
    private static final int ROW_HEIGHT = 36;
    private static final int ICON_BOX = 26;
    private static final int PAD = 8;
    private static final int ARC = 20;

    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final CircuitComponent circuitComponent;
    private final InsertHistory insertHistory;
    private JDialog dialog;

    /**
     * Creates a new instance
     *
     * @param library          the library
     * @param shapeFactory     the shape factory
     * @param circuitComponent the circuit to add to
     * @param insertHistory    the insert history
     */
    public QuickAdd(ElementLibrary library, ShapeFactory shapeFactory, CircuitComponent circuitComponent, InsertHistory insertHistory) {
        this.library = library;
        this.shapeFactory = shapeFactory;
        this.circuitComponent = circuitComponent;
        this.insertHistory = insertHistory;
    }

    /**
     * Shows the palette
     *
     * @param owner    the owning window
     * @param location the location on the screen
     */
    public void show(Window owner, Point location) {
        if (dialog != null)
            dialog.dispose();

        List<Entry> all = collect();
        DefaultListModel<Entry> model = new DefaultListModel<>();
        JList<Entry> list = new JList<>(model);
        list.setCellRenderer(new EntryRenderer());
        list.setFixedCellHeight(ROW_HEIGHT);
        list.setOpaque(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(VISIBLE_ROWS);

        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", Lang.get("msg_quickAddSearch"));
        search.putClientProperty("JTextField.leadingIcon", GlassIcons.get("search"));
        search.putClientProperty("JTextField.showClearButton", true);
        search.setFont(search.getFont().deriveFont(14f));

        Runnable refresh = () -> {
            model.clear();
            for (Entry e : filter(all, search.getText()))
                model.addElement(e);
            if (!model.isEmpty())
                list.setSelectedIndex(0);
        };
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh.run();
            }
        });
        refresh.run();

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int i = list.getSelectedIndex();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (i < model.size() - 1) select(list, i + 1);
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        if (i > 0) select(list, i - 1);
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        pick(list.getSelectedValue());
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        close();
                        e.consume();
                        break;
                    default:
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0)
                    pick(model.get(i));
            }
        });
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0 && i != list.getSelectedIndex())
                    list.setSelectedIndex(i);
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new GlassPanel(new BorderLayout(0, PAD));
        panel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        panel.add(search, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        dialog = new JDialog(owner);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setContentPane(panel);
        dialog.setSize(WIDTH, PAD * 3 + search.getPreferredSize().height + ROW_HEIGHT * VISIBLE_ROWS);
        dialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                close();
            }
        });
        Screen.setLocation(dialog, new Point(location.x - PAD, location.y - PAD), false);
        dialog.setVisible(true);
        search.requestFocusInWindow();
        SwingUtilities.invokeLater(() -> list.ensureIndexIsVisible(0));
    }

    private static void select(JList<Entry> list, int i) {
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    }

    private void close() {
        if (dialog != null) {
            JDialog d = dialog;
            dialog = null;
            d.dispose();
        }
    }

    private void pick(Entry entry) {
        close();
        if (entry != null)
            new InsertAction(entry.node, insertHistory, circuitComponent, shapeFactory).actionPerformed(null);
    }

    private List<Entry> collect() {
        ArrayList<Entry> entries = new ArrayList<>();
        collect(library.getRoot(), null, entries);
        return entries;
    }

    private static void collect(LibraryNode node, String category, ArrayList<Entry> entries) {
        for (LibraryNode child : node) {
            if (child.isHidden())
                continue;
            if (child.isLeaf()) {
                if (child.isUnique())
                    entries.add(new Entry(child, category));
            } else
                collect(child, child.getTranslatedName(), entries);
        }
    }

    private List<Entry> filter(List<Entry> all, String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        ArrayList<Entry> result = new ArrayList<>();
        if (q.isEmpty()) {
            HashSet<String> seen = new HashSet<>();
            ArrayList<String> names = new ArrayList<>(insertHistory.getRecentNames());
            java.util.Collections.addAll(names, BASICS);
            for (String n : names)
                for (Entry e : all)
                    if (e.node.getName().equals(n) && seen.add(n))
                        result.add(e);
            return result;
        }
        for (int rank = 0; rank < 3; rank++)
            for (Entry e : all)
                if (rank(e, q) == rank && result.size() < MAX_RESULTS)
                    result.add(e);
        return result;
    }

    /**
     * 0: the name starts with the query, 1: a word of it does, 2: it contains the query
     * or the category matches, 3: no match
     */
    private static int rank(Entry e, String q) {
        String name = e.name.toLowerCase(Locale.ROOT);
        String plain = e.node.getName().toLowerCase(Locale.ROOT);
        if (name.startsWith(q) || plain.startsWith(q))
            return 0;
        if (name.contains(" " + q) || name.contains("-" + q))
            return 1;
        if (name.contains(q) || plain.contains(q)
                || (e.category != null && e.category.toLowerCase(Locale.ROOT).contains(q)))
            return 2;
        return 3;
    }

    private static final class Entry {
        private final LibraryNode node;
        private final String name;
        private final String category;

        private Entry(LibraryNode node, String category) {
            this.node = node;
            this.name = node.getTranslatedName();
            this.category = category;
        }
    }

    /**
     * The rounded glass background of the palette
     */
    private static final class GlassPanel extends JPanel {
        private GlassPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(new Color(0x26, 0x26, 0x26));
            g2.fill(shape);
            g2.setColor(Glass.SUBTLE);
            g2.draw(shape);
            g2.dispose();
        }
    }

    /**
     * A row: the component icon, its name and its category.
     */
    private final class EntryRenderer extends JComponent implements ListCellRenderer<Entry> {
        private Entry entry;
        private boolean selected;

        private EntryRenderer() {
            setFont(UIManager.getFont("defaultFont"));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Entry> list, Entry value, int index, boolean isSelected, boolean cellHasFocus) {
            entry = value;
            selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int w = getWidth();
            int h = getHeight();
            if (selected) {
                g2.setColor(Glass.white(0.12f));
                g2.fill(new RoundRectangle2D.Float(0, 2, w, h - 4, ARC / 2f, ARC / 2f));
            }

            Icon icon = entry.node.getIconOrNull(shapeFactory);
            if (icon instanceof ImageIcon) {
                Image img = ((ImageIcon) icon).getImage();
                double s = Math.min(1, Math.min((double) ICON_BOX / icon.getIconWidth(), (double) ICON_BOX / icon.getIconHeight()));
                int iw = (int) Math.round(icon.getIconWidth() * s);
                int ih = (int) Math.round(icon.getIconHeight() * s);
                g2.drawImage(img, PAD + (ICON_BOX - iw) / 2, (h - ih) / 2, iw, ih, null);
            }

            Font font = getFont();
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int base = (h + fm.getAscent() - fm.getDescent()) / 2;
            int x = PAD * 2 + ICON_BOX;
            int right = w - PAD;
            if (entry.category != null) {
                Font small = font.deriveFont(11f);
                FontMetrics sm = g2.getFontMetrics(small);
                String cat = entry.category;
                int cw = sm.stringWidth(cat);
                g2.setFont(small);
                g2.setColor(Glass.QUATERNARY);
                g2.drawString(cat, right - cw, base);
                right -= cw + PAD * 2;
                g2.setFont(font);
            }
            g2.setColor(selected ? Color.WHITE : Glass.PRIMARY);
            g2.drawString(clip(entry.name, fm, right - x), x, base);
            g2.dispose();
        }

        private String clip(String s, FontMetrics fm, int width) {
            if (fm.stringWidth(s) <= width)
                return s;
            String t = s;
            while (t.length() > 1 && fm.stringWidth(t + "…") > width)
                t = t.substring(0, t.length() - 1);
            return t + "…";
        }
    }
}
