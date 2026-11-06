import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class UI {

    public enum Theme {
        LIGHT, DARK, FIERY
    }

    // ================= THEME COLORS =================

    public static Color BG;
    public static Color SIDEBAR;
    public static Color PANEL;
    public static Color CARD;
    public static Color ACCENT;
    public static Color ACCENT_DARK;
    public static Color AMBER;
    public static Color TEXT;
    public static Color MUTED;
    public static Color BORDER;
    public static Color SELECTION;
    public static Color GREEN;
    public static Color ORANGE;
    public static Color BLUE;

    // ================= FONTS =================

    public static final Font FONT_REGULAR =
            new Font("Segoe UI", Font.PLAIN, 13);

    public static final Font FONT_BOLD =
            new Font("Segoe UI", Font.BOLD, 13);

    public static final Font FONT_TITLE =
            new Font("Segoe UI", Font.BOLD, 27);

    public static final Font FONT_SMALL =
            new Font("Segoe UI", Font.BOLD, 11);

    private static Theme currentTheme = Theme.DARK;

    static {
        applyTheme(Theme.DARK);
    }

    // ================= THEME DEFINITIONS =================

    public static void applyTheme(Theme theme) {

        currentTheme = theme;

        switch (theme) {

            case LIGHT -> {
                BG = new Color(244, 246, 249);
                SIDEBAR = new Color(232, 235, 240);
                PANEL = new Color(224, 228, 234);
                CARD = new Color(255, 255, 255);

                ACCENT = new Color(198, 38, 54);
                ACCENT_DARK = new Color(155, 27, 39);
                AMBER = new Color(203, 116, 12);

                // Darker text for better readability
                TEXT = new Color(10, 12, 16);
                MUTED = new Color(55, 63, 76);

                BORDER = new Color(198, 204, 214);
                SELECTION = new Color(250, 214, 218);

                GREEN = new Color(22, 125, 73);
                ORANGE = new Color(181, 100, 10);
                BLUE = new Color(42, 101, 170);
            }

            case DARK -> {
                BG = new Color(13, 16, 21);
                SIDEBAR = new Color(19, 23, 30);
                PANEL = new Color(24, 29, 37);
                CARD = new Color(30, 36, 45);

                ACCENT = new Color(214, 51, 68);
                ACCENT_DARK = new Color(154, 35, 48);
                AMBER = new Color(240, 151, 36);

                TEXT = new Color(242, 245, 249);
                MUTED = new Color(158, 166, 178);

                BORDER = new Color(50, 57, 69);
                SELECTION = new Color(82, 37, 48);

                GREEN = new Color(55, 180, 110);
                ORANGE = new Color(232, 145, 48);
                BLUE = new Color(64, 143, 221);
            }

            case FIERY -> {
                BG = new Color(38, 8, 12);
                SIDEBAR = new Color(48, 10, 16);
                PANEL = new Color(58, 14, 22);
                CARD = new Color(73, 18, 27);

                ACCENT = new Color(255, 70, 76);
                ACCENT_DARK = new Color(194, 32, 43);
                AMBER = new Color(255, 150, 43);

                TEXT = new Color(255, 244, 244);
                MUTED = new Color(214, 177, 181);

                BORDER = new Color(102, 34, 45);
                SELECTION = new Color(122, 30, 41);

                GREEN = new Color(94, 201, 139);
                ORANGE = new Color(255, 150, 43);
                BLUE = new Color(100, 169, 235);
            }
        }
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    // ================= BUTTONS =================

    public static JButton button(String text, Color bg) {

        JButton b = new JButton(text);

        b.setForeground(isLightColor(bg) ? TEXT : Color.WHITE);
        b.setBackground(bg);
        b.setFont(FONT_BOLD);

        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);

        b.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        b.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );

        Color original = bg;

        b.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e) {

                        if (b.isEnabled()) {
                            b.setBackground(
                                    brighter(original, 14)
                            );
                        }
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e) {

                        b.setBackground(original);
                    }
                }
        );

        return b;
    }

    public static JButton primaryButton(String text) {
        return button(text, ACCENT);
    }

    public static JButton secondaryButton(String text) {
        return button(text, PANEL);
    }

    public static JButton dangerButton(String text) {
        return button(text, ACCENT_DARK);
    }

    // ================= SIDEBAR =================

    public static JButton sidebarButton(String text, boolean selected) {

    JButton b = new JButton(text);

    b.setFont(FONT_BOLD);
    b.setForeground(selected ? TEXT : MUTED);
    b.setBackground(selected ? CARD : SIDEBAR);

    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setContentAreaFilled(true);
    b.setOpaque(true);

    b.setHorizontalAlignment(SwingConstants.LEFT);
    b.setHorizontalTextPosition(SwingConstants.RIGHT);
    b.setIconTextGap(12);

    b.setBorder(
            new EmptyBorder(
                    11,
                    14,
                    11,
                    14
            )
    );

    b.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
    );

    return b;
}

    public static JButton sidebarButton(
        String text,
        boolean selected,
        Icon icon) {

    JButton b = new JButton(text);

    b.setFont(FONT_BOLD);

    b.setForeground(
            selected ? TEXT : MUTED
    );

    b.setBackground(
            selected ? CARD : SIDEBAR
    );

    b.setFocusPainted(false);
    b.setBorderPainted(false);
    b.setContentAreaFilled(true);
    b.setOpaque(true);

    b.setHorizontalAlignment(
            SwingConstants.LEFT
    );

    b.setHorizontalTextPosition(
            SwingConstants.RIGHT
    );

    b.setIconTextGap(12);

    b.setIcon(icon);

    b.setBorder(
            new EmptyBorder(
                    11,
                    14,
                    11,
                    14
            )
    );

    b.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
    );

    return b;
}
// =========================================================
// SIDEBAR ICONS
// =========================================================

public enum SidebarIcon {
    DASHBOARD,
    FIREFIGHTER,
    INCIDENT,
    DISPATCH,
    VEHICLE,
    MAP,
    SETTINGS
}

public static Icon sidebarIcon(
        SidebarIcon type) {

    return new SidebarIconPainter(
            type,
            TEXT,
            20
    );
}

private static class SidebarIconPainter
        implements Icon {

    private final SidebarIcon type;
    private final Color color;
    private final int size;

    public SidebarIconPainter(
            SidebarIcon type,
            Color color,
            int size) {

        this.type = type;
        this.color = color;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(
            Component c,
            Graphics g,
            int x,
            int y) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );

        g2.setColor(color);

        g2.setStroke(
                new BasicStroke(
                        1.8f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        switch (type) {

            case DASHBOARD:
                drawDashboard(g2, x, y);
                break;

            case FIREFIGHTER:
                drawFirefighter(g2, x, y);
                break;

            case INCIDENT:
                drawIncident(g2, x, y);
                break;

            case DISPATCH:
                drawDispatch(g2, x, y);
                break;

            case VEHICLE:
                drawVehicle(g2, x, y);
                break;

            case MAP:
                drawMap(g2, x, y);
                break;

            case SETTINGS:
                drawSettings(g2, x, y);
                break;
        }

        g2.dispose();
    }
}
private static void drawDashboard(
        Graphics2D g,
        int x,
        int y) {

    int s = 6;

    g.drawRoundRect(
            x + 2,
            y + 2,
            s,
            s,
            2,
            2
    );

    g.drawRoundRect(
            x + 12,
            y + 2,
            s,
            s,
            2,
            2
    );

    g.drawRoundRect(
            x + 2,
            y + 12,
            s,
            s,
            2,
            2
    );

    g.drawRoundRect(
            x + 12,
            y + 12,
            s,
            s,
            2,
            2
    );
}
private static void drawFirefighter(
        Graphics2D g,
        int x,
        int y) {

    // Helmet
    

    // Head
    g.drawOval(
            x + 6,
            y + 4,
            8,
            8
    );

    // Body
    g.drawRoundRect(
            x + 4,
            y + 14,
            12,
            5,
            2,
            2
    );
}
private static void drawIncident(
        Graphics2D g,
        int x,
        int y) {

    Polygon triangle =
            new Polygon();

    triangle.addPoint(
            x + 10,
            y + 2
    );

    triangle.addPoint(
            x + 18,
            y + 18
    );

    triangle.addPoint(
            x + 2,
            y + 18
    );

    triangle.addPoint(
            x + 10,
            y + 2
    );

    g.drawPolygon(triangle);

    g.drawLine(
            x + 10,
            y + 7,
            x + 10,
            y + 12
    );

    g.fillOval(
            x + 9,
            y + 14,
            3,
            3
    );
}
private static void drawDispatch(
        Graphics2D g,
        int x,
        int y) {

    g.drawRoundRect(
            x + 4,
            y + 3,
            12,
            15,
            2,
            2
    );

    g.drawRoundRect(
            x + 7,
            y + 1,
            6,
            4,
            2,
            2
    );

    g.drawLine(
            x + 7,
            y + 8,
            x + 13,
            y + 8
    );

    g.drawLine(
            x + 7,
            y + 11,
            x + 13,
            y + 11
    );

    g.drawLine(
            x + 7,
            y + 14,
            x + 11,
            y + 14
    );
}
private static void drawVehicle(Graphics2D g, int x, int y) {
    g.drawRoundRect(x + 2, y + 5, 16, 9, 3, 3);
    g.drawLine(x + 5, y + 5, x + 8, y + 2);
    g.drawLine(x + 8, y + 2, x + 14, y + 2);
    g.drawLine(x + 14, y + 2, x + 17, y + 5);
    g.drawOval(x + 4, y + 12, 4, 4);
    g.drawOval(x + 13, y + 12, 4, 4);
}


private static void drawMap(Graphics2D g, int x, int y) {
    g.drawRoundRect(x + 2, y + 3, 16, 14, 3, 3);
    g.drawLine(x + 7, y + 3, x + 7, y + 17);
    g.drawLine(x + 13, y + 3, x + 13, y + 17);
    g.drawLine(x + 2, y + 8, x + 18, y + 8);
    g.drawLine(x + 2, y + 13, x + 18, y + 13);
}

private static void drawSettings(
        Graphics2D g,
        int x,
        int y) {

    int cx = x + 10;
    int cy = y + 10;

    g.drawOval(
            x + 5,
            y + 5,
            10,
            10
    );

    g.drawOval(
            x + 8,
            y + 8,
            4,
            4
    );

    for (int i = 0; i < 8; i++) {

        double angle =
                i * Math.PI / 4;

        int x1 =
                (int) (
                        cx +
                        Math.cos(angle) * 6
                );

        int y1 =
                (int) (
                        cy +
                        Math.sin(angle) * 6
                );

        int x2 =
                (int) (
                        cx +
                        Math.cos(angle) * 9
                );

        int y2 =
                (int) (
                        cy +
                        Math.sin(angle) * 9
                );

        g.drawLine(
                x1,
                y1,
                x2,
                y2
        );
    }
}
    // ================= LABELS =================

    public static JLabel label(String text) {

        JLabel l = new JLabel(text);

        l.setForeground(TEXT);
        l.setFont(FONT_REGULAR);

        return l;
    }

    public static JLabel mutedLabel(String text) {

        JLabel l = new JLabel(text);

        l.setForeground(MUTED);
        l.setFont(FONT_REGULAR);

        return l;
    }

    public static JLabel title(String text) {

        JLabel l = new JLabel(text);

        l.setForeground(TEXT);
        l.setFont(FONT_TITLE);

        return l;
    }

    public static JLabel sectionTitle(String text) {

        JLabel l = new JLabel(text);

        l.setForeground(TEXT);
        l.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        return l;
    }

    // ================= TEXT FIELD =================

    public static JTextField field() {

        JTextField f = new JTextField();

        f.setBackground(CARD);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);

        f.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        f.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1),
                        new EmptyBorder(
                                8,
                                10,
                                8,
                                10
                        )
                )
        );

        f.setOpaque(true);

        return f;
    }

    // ================= CARD =================

    public static JPanel card() {

        JPanel p = new JPanel();

        p.setBackground(CARD);

        p.setBorder(
                new EmptyBorder(
                        16,
                        16,
                        16,
                        16
                )
        );

        return p;
    }

    public static JPanel softCard() {

        JPanel p = new JPanel();

        p.setBackground(PANEL);

        p.setBorder(
                new LineBorder(BORDER, 1)
        );

        return p;
    }

    // ================= STAT CARD =================

    public static JPanel statCard(
            String title,
            JLabel value,
            String helper) {

        JPanel p = card();

        // Compact dashboard statistics while keeping the same visual style.
        p.setBorder(
                new EmptyBorder(
                        12,
                        14,
                        12,
                        14
                )
        );

        p.setLayout(
                new BorderLayout(
                        0,
                        5
                )
        );

        JLabel t = label(title);

        t.setForeground(MUTED);
        t.setFont(FONT_SMALL);

        value.setForeground(TEXT);

        value.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        26
                )
        );

        JLabel h = mutedLabel(helper);

        h.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        p.add(h, BorderLayout.SOUTH);

        return p;
    }

    // ================= TABLE =================

    public static void styleTable(JTable table) {

        table.setBackground(CARD);
        table.setForeground(TEXT);

        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT);

        table.setGridColor(BORDER);

        table.setRowHeight(36);

        table.setFont(FONT_REGULAR);

        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        table.setFillsViewportHeight(true);

        table.getTableHeader()
                .setBackground(PANEL);

        table.getTableHeader()
                .setForeground(MUTED);

        table.getTableHeader()
                .setFont(FONT_SMALL);

        table.getTableHeader()
                .setPreferredSize(
                        new Dimension(0, 38)
                );

        table.getTableHeader()
                .setReorderingAllowed(false);

        /*
         * Explicit renderer ensures that the
         * table body uses the active theme colors.
         */
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setForeground(TEXT);
        renderer.setBackground(CARD);

        table.setDefaultRenderer(
                Object.class,
                renderer
        );
    }

    // ================= SCROLL PANE =================

    public static JScrollPane scroll(
            Component view) {

        JScrollPane sp =
                new JScrollPane(view);

        sp.setBorder(
                new LineBorder(
                        BORDER,
                        1
                )
        );

        sp.getViewport()
                .setBackground(CARD);

        sp.setBackground(CARD);

        sp.getVerticalScrollBar()
                .setUnitIncrement(16);

        styleScrollBar(
                sp.getVerticalScrollBar()
        );

        styleScrollBar(
                sp.getHorizontalScrollBar()
        );

        return sp;
    }

    private static void styleScrollBar(
            JScrollBar bar) {

        bar.setBackground(PANEL);

        bar.setUI(
                new BasicScrollBarUI() {

                    @Override
                    protected void configureScrollBarColors() {

                        thumbColor = BORDER;
                        trackColor = PANEL;
                    }

                    @Override
                    protected JButton createDecreaseButton(
                            int orientation) {

                        return zeroButton();
                    }

                    @Override
                    protected JButton createIncreaseButton(
                            int orientation) {

                        return zeroButton();
                    }

                    private JButton zeroButton() {

                        JButton b = new JButton();

                        b.setPreferredSize(
                                new Dimension(0, 0)
                        );

                        return b;
                    }
                }
        );
    }

    // ================= RADIO BUTTON =================

    public static void styleRadio(
            JRadioButton r) {

        r.setForeground(TEXT);
        r.setBackground(CARD);

        r.setFont(FONT_REGULAR);

        r.setFocusPainted(false);
        r.setOpaque(true);
    }

    // ================= COMBO BOX =================

    public static void styleCombo(
            JComboBox<?> combo) {

        combo.setBackground(CARD);
        combo.setForeground(TEXT);
        combo.setFont(FONT_REGULAR);
        combo.setOpaque(true);

        /*
         * Use an explicit renderer so the selected value
         * and the popup options remain readable in every theme.
         */
        DefaultListCellRenderer renderer =
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus
                        );

                        setFont(FONT_REGULAR);

                        if (isSelected) {
                            setBackground(ACCENT);
                            setForeground(Color.WHITE);
                        } else {
                            setBackground(CARD);
                            setForeground(TEXT);
                        }

                        setOpaque(true);
                        setBorder(new EmptyBorder(4, 8, 4, 8));

                        return this;
                    }
                };

        combo.setRenderer(renderer);

        combo.setBorder(
                BorderFactory.createLineBorder(BORDER, 1)
        );

        combo.setFocusable(true);
    }

    // ================= COLORS =================

    private static boolean isLightColor(
            Color c) {

        int brightness =
                (
                        c.getRed() * 299
                                + c.getGreen() * 587
                                + c.getBlue() * 114
                ) / 1000;

        return brightness > 160;
    }

    private static Color brighter(
            Color c,
            int amount) {

        return new Color(
                Math.min(
                        255,
                        c.getRed() + amount
                ),

                Math.min(
                        255,
                        c.getGreen() + amount
                ),

                Math.min(
                        255,
                        c.getBlue() + amount
                )
        );
    }
        
}