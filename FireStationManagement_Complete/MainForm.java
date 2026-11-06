import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainForm extends JFrame {

    private final FirefighterDAO firefighterDAO =
            new FirefighterDAO();

    private final IncidentDAO incidentDAO =
            new IncidentDAO();

    private final DispatchDAO dispatchDAO =
            new DispatchDAO();

    private final VehicleDAO vehicleDAO =
            new VehicleDAO();

    private JLabel activeIncidentCount;
    private JLabel availableCount;
    private JLabel dispatchCount;
    private JLabel criticalCount;
    private JLabel availableVehicleCount;

    private DefaultTableModel incidentModel;
    private DefaultTableModel firefighterModel;

    private JTable incidentTable;
    private JTable firefighterTable;

    private JPanel activityPanel;
    private JPanel contentCards;
    private JPanel mapContentSlot;
    private MapViewPanel mapViewPanel;
    private FirefighterForm firefighterManagementPanel;
    private IncidentForm incidentManagementPanel;
    private DispatchForm dispatchManagementPanel;
    private VehicleForm vehicleManagementPanel;
    // ===== Search / Filter (Firefighters) =====
    private JTextField ffSearchField;
    private JComboBox<String> ffExpertiseFilter;
    private JComboBox<String> ffStatusFilter;
    private List<Object[]> allFirefighterData = new ArrayList<>();

    public MainForm() {

        setTitle(
                "Fire Command "
        );

        setSize(1280, 800);

        setMinimumSize(
                new Dimension(1080, 700)
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        getContentPane()
                .setBackground(UI.BG);

        buildUI();

        refreshDashboard();
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private void buildUI() {

        JPanel root =
                new JPanel(
                        new BorderLayout()
                );

        root.setBackground(UI.BG);

        root.add(
                buildSidebar(),
                BorderLayout.WEST
        );

        contentCards = new JPanel(new CardLayout());
        contentCards.add(buildContent(), "DASHBOARD");
        contentCards.add(buildMapContent(), "MAP_VIEW");

        root.add(
                contentCards,
                BorderLayout.CENTER
        );

        setContentPane(root);
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private JPanel buildSidebar() {

        JPanel side =
                new JPanel(
                        new BorderLayout()
                );

        side.setPreferredSize(
                new Dimension(220, 0)
        );

        side.setBackground(
                UI.SIDEBAR
        );

        side.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        0,
                        0,
                        1,
                        UI.BORDER
                )
        );

        // =====================================================
        // BRAND
        // =====================================================

        JPanel brand =
                new JPanel();

        brand.setBackground(
                UI.SIDEBAR
        );

        brand.setBorder(
                new EmptyBorder(
                        20,
                        18,
                        18,
                        18
                )
        );

        brand.setLayout(
                new BoxLayout(
                        brand,
                        BoxLayout.Y_AXIS
                )
        );

        // ---------- Logo + FIRE COMMAND ----------

        JPanel brandTop =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                0,
                                0
                        )
                );

        brandTop.setBackground(
                UI.SIDEBAR
        );

        // Load logo
        java.net.URL logoURL =
                getClass().getResource(
                        "/firecommand.png"
                );

        ImageIcon logo =
                new ImageIcon(
                        logoURL
                );

        // Resize logo
        Image logoImage =
                logo.getImage().getScaledInstance(
                        34,
                        34,
                        Image.SCALE_SMOOTH
                );

        JLabel logoLabel =
                new JLabel(
                        new ImageIcon(logoImage)
                );

        // FIRE COMMAND text
        JLabel mark =
                new JLabel(
                        "FIRE COMMAND"
                );

        mark.setForeground(
                UI.TEXT
        );

        mark.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        brandTop.add(
                logoLabel
        );

        brandTop.add(
                Box.createHorizontalStrut(10)
        );

        brandTop.add(
                mark
        );

        brand.add(
                brandTop
        );

        // ---------- Subtitle ----------

        JLabel sub =
                new JLabel(
                        "Station Management"
                );

        sub.setForeground(
                UI.MUTED
        );

        sub.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        10
                )
        );

        sub.setBorder(
                new EmptyBorder(
                        4,
                        0,
                        0,
                        0
                )
        );

        brand.add(
                sub
        );

        // =====================================================
        // NAVIGATION
        // =====================================================

        JPanel nav =
                new JPanel();

        nav.setBackground(
                UI.SIDEBAR
        );

        nav.setBorder(
                new EmptyBorder(
                        8,
                        12,
                        8,
                        12
                )
        );

        nav.setLayout(
                new BoxLayout(
                        nav,
                        BoxLayout.Y_AXIS
                )
        );

        JButton dashboard =
                UI.sidebarButton(
                        "Dashboard",
                        true,
                        UI.sidebarIcon(
                                UI.SidebarIcon.DASHBOARD
                        )
                );

        JButton firefighters =
                UI.sidebarButton(
                        "Firefighters",
                        false,
                        UI.sidebarIcon(
                                UI.SidebarIcon.FIREFIGHTER
                        )
                );

        JButton incidents =
                UI.sidebarButton(
                        "Incidents",
                        false,
                        UI.sidebarIcon(
                                UI.SidebarIcon.INCIDENT
                        )
                );

        JButton dispatches =
                UI.sidebarButton(
                        "Dispatches",
                        false,
                        UI.sidebarIcon(
                                UI.SidebarIcon.DISPATCH
                        )
                );

        JButton vehicles =
                UI.sidebarButton(
                        "Vehicles",
                        false,
                        UI.sidebarIcon(
                                UI.SidebarIcon.VEHICLE
                        )
                );

        JButton mapView =
                UI.sidebarButton(
                        "Map View",
                        false,
                        UI.sidebarIcon(
                                UI.SidebarIcon.MAP
                        )
                );

        dashboard.addActionListener(
                e -> showDashboard()
        );

        firefighters.addActionListener(e -> showFirefighters());

        incidents.addActionListener(e -> showIncidents());

        dispatches.addActionListener(e -> showDispatches(null));

        vehicles.addActionListener(e -> showVehicles());

        mapView.addActionListener(e -> showMapView());

        for (JButton b :
                new JButton[]{
                        dashboard,
                        firefighters,
                        incidents,
                        dispatches,
                        vehicles,
                        mapView
                }) {

            b.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            46
                    )
            );

            nav.add(b);

            nav.add(
                    Box.createVerticalStrut(4)
            );
        }

        JSeparator separator =
                new JSeparator();

        separator.setForeground(
                UI.BORDER
        );

        nav.add(
                Box.createVerticalStrut(10)
        );

        nav.add(separator);

        nav.add(
                Box.createVerticalStrut(10)
        );

        // ---------- Settings ----------

        JButton settings =
                UI.sidebarButton(
                        "Settings",
                        false,
                        UI.sidebarIcon(
                                UI.SidebarIcon.SETTINGS
                        )
                );

        settings.addActionListener(
                e -> openSettings()
        );

        settings.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        46
                )
        );

        nav.add(settings);

        // =====================================================
        // ADD BRAND + NAVIGATION TO SIDEBAR
        // =====================================================

        side.add(
                brand,
                BorderLayout.NORTH
        );

        side.add(
                nav,
                BorderLayout.CENTER
        );

        // =====================================================
        // STATION STATUS
        // =====================================================

        JPanel bottom =
                new JPanel(
                        new BorderLayout()
                );

        bottom.setBackground(
                UI.SIDEBAR
        );

        bottom.setBorder(
                new EmptyBorder(
                        12,
                        16,
                        16,
                        16
                )
        );

        JLabel status =
                new JLabel(
                        "●  Station Online"
                );

        status.setForeground(
                UI.GREEN
        );

        status.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        bottom.add(
                status,
                BorderLayout.WEST
        );

        side.add(
                bottom,
                BorderLayout.SOUTH
        );

        return side;
    }

    private JPanel buildMapContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(UI.BG);
        content.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UI.title("Incident Map View"));
        heading.add(Box.createVerticalStrut(4));
        heading.add(UI.mutedLabel("Geographic overview of registered incidents with saved coordinates"));
        header.add(heading, BorderLayout.WEST);

        JLabel live = new JLabel("● MAP VIEW");
        live.setForeground(UI.GREEN);
        live.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.add(live, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        mapContentSlot = new JPanel(new BorderLayout());
        mapContentSlot.setBackground(UI.CARD);
        mapContentSlot.setBorder(BorderFactory.createLineBorder(UI.BORDER));
        content.add(mapContentSlot, BorderLayout.CENTER);
        return content;
    }

    private void showDashboard() {
        ((CardLayout) contentCards.getLayout()).show(contentCards, "DASHBOARD");
        refreshDashboard();
    }

    private void showMapView() {
        if (mapViewPanel == null) {
            mapViewPanel = new MapViewPanel(this::openDispatchForIncident);
            mapContentSlot.add(mapViewPanel, BorderLayout.CENTER);
            mapContentSlot.revalidate();
            mapContentSlot.repaint();
        } else {
            mapViewPanel.refreshIncidents();
        }
        ((CardLayout) contentCards.getLayout()).show(contentCards, "MAP_VIEW");
    }
        private void showFirefighters() {
        if (firefighterManagementPanel == null) {
                firefighterManagementPanel =
                        new FirefighterForm(this::refreshAfterModuleChange);

                contentCards.add(firefighterManagementPanel, "FIREFIGHTERS");
        } else {
                firefighterManagementPanel.refreshData();
        }

        ((CardLayout) contentCards.getLayout())
                .show(contentCards, "FIREFIGHTERS");
        }

        private void showIncidents() {
        if (incidentManagementPanel == null) {
                incidentManagementPanel =
                        new IncidentForm(this::refreshAfterModuleChange);

                contentCards.add(incidentManagementPanel, "INCIDENTS");
        } else {
                incidentManagementPanel.refreshData();
        }

        ((CardLayout) contentCards.getLayout())
                .show(contentCards, "INCIDENTS");
        }

        private void showDispatches(Integer incidentId) {
        if (dispatchManagementPanel == null) {
                dispatchManagementPanel =
                        new DispatchForm(
                                this::refreshAfterModuleChange,
                                incidentId
                        );

                contentCards.add(dispatchManagementPanel, "DISPATCHES");
        } else {
                dispatchManagementPanel.prepareForDisplay(incidentId);
        }

        ((CardLayout) contentCards.getLayout())
                .show(contentCards, "DISPATCHES");
        }

        private void showVehicles() {
        if (vehicleManagementPanel == null) {
                vehicleManagementPanel =
                        new VehicleForm(this::refreshAfterModuleChange);

                contentCards.add(vehicleManagementPanel, "VEHICLES");
        } else {
                vehicleManagementPanel.refreshData();
        }

        ((CardLayout) contentCards.getLayout())
                .show(contentCards, "VEHICLES");
        }
                private void refreshAfterModuleChange() {

        // Dashboard statistics and dashboard tables
        refreshDashboard();

        // Map markers
        if (mapViewPanel != null) {
                mapViewPanel.refreshIncidents();
        }

        // Management module tables
        if (firefighterManagementPanel != null) {
                firefighterManagementPanel.refreshData();
        }

        if (incidentManagementPanel != null) {
                incidentManagementPanel.refreshData();
        }

        if (dispatchManagementPanel != null) {
                dispatchManagementPanel.refreshData();
        }

        if (vehicleManagementPanel != null) {
                vehicleManagementPanel.refreshData();
        }
        }

         private void openDispatchForIncident(int incidentId) {
         showDispatches(incidentId);
        }

        

    // =========================================================
    // CONTENT
    // =========================================================

    private JPanel buildContent() {

        JPanel content =
                new JPanel(
                        new BorderLayout(
                                0,
                                18
                        )
                );

        content.setBackground(
                UI.BG
        );

        content.setBorder(
                new EmptyBorder(
                        24,
                        26,
                        24,
                        26
                )
        );

        // ---------- Header ----------

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setOpaque(false);

        JPanel heading =
                new JPanel();

        heading.setOpaque(false);

        heading.setLayout(
                new BoxLayout(
                        heading,
                        BoxLayout.Y_AXIS
                )
        );

        heading.add(
                UI.title(
                        "Fire Station Command Center"
                )
        );

        JLabel desc =
                UI.mutedLabel(
                        "Emergency response • Personnel • Operations"
                );

        heading.add(
                Box.createVerticalStrut(4)
        );

        heading.add(desc);

        header.add(
                heading,
                BorderLayout.WEST
        );

        JLabel online =
                new JLabel(
                        "● LIVE"
                );

        online.setForeground(
                UI.GREEN
        );

        online.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        header.add(
                online,
                BorderLayout.EAST
        );

        content.add(
                header,
                BorderLayout.NORTH
        );

        // ---------- Body ----------

        JPanel body =
                new JPanel(
                        new BorderLayout(
                                0,
                                16
                        )
                );

        body.setOpaque(false);

        // ---------- Statistics ----------

        JPanel stats =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                12,
                                0
                        )
                );

        stats.setOpaque(false);

        activeIncidentCount =
                new JLabel("0");

        availableCount =
                new JLabel("0");

        dispatchCount =
                new JLabel("0");

        criticalCount =
                new JLabel("0");

        availableVehicleCount = new JLabel("0");

        stats.add(
                UI.statCard(
                        "ACTIVE INCIDENTS",
                        activeIncidentCount,
                        "Require current attention"
                )
        );

        stats.add(
                UI.statCard(
                        "AVAILABLE FIREFIGHTERS",
                        availableCount,
                        "Ready for dispatch"
                )
        );

        stats.add(
                UI.statCard(
                        "ACTIVE DISPATCHES",
                        dispatchCount,
                        "Currently deployed"
                )
        );

        stats.add(
                UI.statCard(
                        "CRITICAL INCIDENTS",
                        criticalCount,
                        "Highest priority"
                )
        );

        stats.add(
                UI.statCard(
                        "AVAILABLE VEHICLES",
                        availableVehicleCount,
                        "Ready for dispatch"
                )
        );

        body.add(
                stats,
                BorderLayout.NORTH
        );

        // ---------- Main Grid ----------

        JPanel mainGrid =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                16,
                                0
                        )
                );

        mainGrid.setOpaque(false);

        mainGrid.add(
                buildIncidentPanel()
        );

        mainGrid.add(
                buildFirefighterPanel()
        );

        body.add(
                mainGrid,
                BorderLayout.CENTER
        );

        // ---------- Activity ----------

        JPanel activity =
                UI.card();

        activity.setLayout(
                new BorderLayout(
                        0,
                        8
                )
        );

        JLabel activityTitle =
                UI.sectionTitle(
                        "Recent Activity"
                );

        activity.add(
                activityTitle,
                BorderLayout.NORTH
        );

        activityPanel =
                new JPanel();

        activityPanel.setOpaque(false);

        activityPanel.setLayout(
                new BoxLayout(
                        activityPanel,
                        BoxLayout.Y_AXIS
                )
        );

        activity.add(
                activityPanel,
                BorderLayout.CENTER
        );

        // Keep the activity log slightly narrower so it does not dominate
        // the dashboard width. The incident and firefighter panels above
        // continue to use the full available width.
        activity.setPreferredSize(new Dimension(0, 118));

        JPanel activityRow = new JPanel(new GridBagLayout());
        activityRow.setOpaque(false);

        GridBagConstraints activityConstraints = new GridBagConstraints();
        activityConstraints.gridy = 0;
        activityConstraints.fill = GridBagConstraints.HORIZONTAL;
        activityConstraints.anchor = GridBagConstraints.WEST;
        activityConstraints.weightx = 0.84;
        activityConstraints.gridx = 0;
        activityRow.add(activity, activityConstraints);

        JPanel activitySpacer = new JPanel();
        activitySpacer.setOpaque(false);
        activityConstraints.gridx = 1;
        activityConstraints.weightx = 0.16;
        activityRow.add(activitySpacer, activityConstraints);

        body.add(
                activityRow,
                BorderLayout.SOUTH
        );

        content.add(
                body,
                BorderLayout.CENTER
        );

        return content;
    }

    // =========================================================
    // INCIDENT PANEL
    // =========================================================

    private JPanel buildIncidentPanel() {

        JPanel panel =
                UI.card();

        panel.setLayout(
                new BorderLayout(
                        0,
                        10
                )
        );

        JPanel head =
                new JPanel(
                        new BorderLayout()
                );

        head.setOpaque(false);

        JLabel title =
                UI.sectionTitle(
                        "Active Incidents"
                );

        JButton add =
                UI.button(
                        "+ Add Incident",
                        UI.ACCENT
                );

        add.addActionListener(e -> showIncidents());

        head.add(
                title,
                BorderLayout.WEST
        );

        head.add(
                add,
                BorderLayout.EAST
        );

        panel.add(
                head,
                BorderLayout.NORTH
        );

        incidentModel =
                new DefaultTableModel(
                        new Object[]{
                                "Severity",
                                "Incident",
                                "Location",
                                "Status",
                                "Firefighters"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int r,
                            int c) {

                        return false;
                    }
                };

        incidentTable =
                new JTable(
                        incidentModel
                );

        UI.styleTable(incidentTable);

        // Color the Status column (Resolved = green, others = red)
        incidentTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(new StatusRenderer());

        panel.add(
                UI.scroll(incidentTable),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // FIREFIGHTER PANEL  (Search + Expertise + Status + Double-click)
    // =========================================================

    private JPanel buildFirefighterPanel() {

        JPanel panel =
                UI.card();

        panel.setLayout(
                new BorderLayout(
                        0,
                        10
                )
        );

        // ----- Header -----
        JPanel head =
                new JPanel(
                        new BorderLayout()
                );

        head.setOpaque(false);

        JLabel title =
                UI.sectionTitle(
                        "Firefighters"
                );

        JButton add =
                UI.button(
                        "+ Add Firefighter",
                        UI.PANEL
                );

        add.addActionListener(e -> showFirefighters());

        head.add(
                title,
                BorderLayout.WEST
        );

        head.add(
                add,
                BorderLayout.EAST
        );

        // ----- Search + Expertise + Status Filter -----
        JPanel filterRow =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                8,
                                0
                        )
                );

        filterRow.setOpaque(false);

        ffSearchField = UI.field();
        ffSearchField.setPreferredSize(
                new Dimension(150, 32)
        );
        ffSearchField.putClientProperty(
                "JTextField.placeholderText",
                "Search name or ID..."
        );

        ffExpertiseFilter =
                new JComboBox<>(
                        new String[]{
                                "All Expertise",
                                "Rescuer",
                                "Medic",
                                "Electric Technician",
                                "Chemical Technician"
                        }
                );
        UI.styleCombo(ffExpertiseFilter);
        ffExpertiseFilter.setPreferredSize(
                new Dimension(140, 32)
        );

        ffStatusFilter =
                new JComboBox<>(
                        new String[]{
                                "All Status",
                                "Available",
                                "Dispatched"
                        }
                );
        UI.styleCombo(ffStatusFilter);
        ffStatusFilter.setPreferredSize(
                new Dimension(120, 32)
        );

        JButton searchBtn =
                UI.button(
                        "Search",
                        UI.PANEL
                );

        searchBtn.addActionListener(
                e -> applyFirefighterFilter()
        );

        ffSearchField.addActionListener(
                e -> applyFirefighterFilter()
        );

        ffExpertiseFilter.addActionListener(
                e -> applyFirefighterFilter()
        );

        ffStatusFilter.addActionListener(
                e -> applyFirefighterFilter()
        );

        filterRow.add(ffSearchField);
        filterRow.add(ffExpertiseFilter);
        filterRow.add(ffStatusFilter);
        filterRow.add(searchBtn);

        // Combine header + filter
        JPanel north =
                new JPanel(
                        new BorderLayout(
                                0,
                                8
                        )
                );

        north.setOpaque(false);
        north.add(head, BorderLayout.NORTH);
        north.add(filterRow, BorderLayout.SOUTH);

        panel.add(
                north,
                BorderLayout.NORTH
        );

        // ----- Table -----
        firefighterModel =
                new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Name",
                                "Expertise",
                                "Status"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(
                            int r,
                            int c) {

                        return false;
                    }
                };

        firefighterTable =
                new JTable(
                        firefighterModel
                );

        UI.styleTable(firefighterTable);

        // Double-click → Edit / Delete / Cancel
        firefighterTable.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            MouseEvent e) {

                        if (e.getClickCount() == 2
                                && firefighterTable
                                .getSelectedRow() != -1) {

                            onFirefighterDoubleClick(
                                    firefighterTable
                                            .getSelectedRow()
                            );
                        }
                    }
                }
        );

        panel.add(
                UI.scroll(firefighterTable),
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void openSettings() {

        JDialog dialog =
                new JDialog(
                        this,
                        "Settings",
                        true
                );

        dialog.setSize(
                500,
                430
        );

        dialog.setLocationRelativeTo(this);

        dialog.getContentPane()
                .setBackground(UI.BG);

        JPanel root =
                new JPanel(
                        new BorderLayout(
                                16,
                                16
                        )
                );

        root.setBackground(
                UI.BG
        );

        root.setBorder(
                new EmptyBorder(
                        24,
                        24,
                        24,
                        24
                )
        );

        root.add(
                UI.title("Settings"),
                BorderLayout.NORTH
        );

        JPanel card =
                UI.card();

        card.setLayout(
                new BorderLayout(
                        0,
                        14
                )
        );

        card.add(
                UI.sectionTitle(
                        "Appearance"
                ),
                BorderLayout.NORTH
        );

        // ---------- Theme choices ----------

        JPanel themePanel =
                new JPanel();

        themePanel.setBackground(
                UI.CARD
        );

        themePanel.setLayout(
                new BoxLayout(
                        themePanel,
                        BoxLayout.Y_AXIS
                )
        );

        ButtonGroup group =
                new ButtonGroup();

        JRadioButton light =
                createThemeRadio(
                        "Light",
                        UI.Theme.LIGHT
                );

        JRadioButton dark =
                createThemeRadio(
                        "Dark",
                        UI.Theme.DARK
                );

        JRadioButton fiery =
                createThemeRadio(
                        "Fiery",
                        UI.Theme.FIERY
                );

        group.add(light);
        group.add(dark);
        group.add(fiery);

        UI.Theme current =
                UI.getCurrentTheme();

        if (current == UI.Theme.LIGHT) {

            light.setSelected(true);

        } else if (current == UI.Theme.FIERY) {

            fiery.setSelected(true);

        } else {

            dark.setSelected(true);
        }

        themePanel.add(light);

        themePanel.add(
                Box.createVerticalStrut(10)
        );

        themePanel.add(dark);

        themePanel.add(
                Box.createVerticalStrut(10)
        );

        themePanel.add(fiery);

        card.add(
                themePanel,
                BorderLayout.CENTER
        );

        JLabel hint =
                UI.mutedLabel(
                        "Light mode uses near-black text for stronger readability."
                );

        card.add(
                hint,
                BorderLayout.SOUTH
        );

        root.add(
                card,
                BorderLayout.CENTER
        );

        // ---------- Buttons ----------

        JPanel actions =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        actions.setOpaque(false);

        JButton cancel =
                UI.button(
                        "Cancel",
                        UI.PANEL
                );

        JButton apply =
                UI.button(
                        "Apply Theme",
                        UI.ACCENT
                );

        cancel.addActionListener(
                e -> dialog.dispose()
        );

        apply.addActionListener(
                e -> {

                    UI.Theme selected;

                    if (light.isSelected()) {

                        selected =
                                UI.Theme.LIGHT;

                    } else if (fiery.isSelected()) {

                        selected =
                                UI.Theme.FIERY;

                    } else {

                        selected =
                                UI.Theme.DARK;
                    }

                    applyThemeAndRestart(
                            selected,
                            dialog
                    );
                }
        );

        actions.add(cancel);
        actions.add(apply);

        root.add(
                actions,
                BorderLayout.SOUTH
        );

        dialog.setContentPane(root);

        dialog.setVisible(true);
    }

    private JRadioButton createThemeRadio(
            String text,
            UI.Theme theme) {

        JRadioButton r =
                new JRadioButton(text);

        r.setBackground(
                UI.CARD
        );

        r.setForeground(
                UI.TEXT
        );

        r.setFont(
                UI.FONT_REGULAR
        );

        r.setFocusPainted(false);

        r.putClientProperty(
                "theme",
                theme
        );

        return r;
    }

    // =========================================================
    // APPLY THEME
    // =========================================================

    private void applyThemeAndRestart(
            UI.Theme theme,
            JDialog dialog) {

        if (theme == UI.getCurrentTheme()) {

            dialog.dispose();
            return;
        }

        UI.applyTheme(theme);

        dialog.dispose();

        /*
         * Rebuilding the main window is intentional.
         * It guarantees every component gets the
         * colors of the newly selected theme.
         */
        dispose();

        SwingUtilities.invokeLater(
                () -> new MainForm()
                        .setVisible(true)
        );
    }

    // =========================================================
    // REFRESH DASHBOARD
    // =========================================================

    private void refreshDashboard() {

        try {

            activeIncidentCount.setText(
                    String.valueOf(
                            incidentDAO.countActive()
                    )
            );

            availableCount.setText(
                    String.valueOf(
                            firefighterDAO.countAvailable()
                    )
            );

            dispatchCount.setText(
                    String.valueOf(
                            dispatchDAO.countActive()
                    )
            );

            criticalCount.setText(
                    String.valueOf(
                            incidentDAO.countCritical()
                    )
            );

            availableVehicleCount.setText(String.valueOf(vehicleDAO.count("Available")));

            loadIncidents();
            loadFirefighters();
            loadActivity();

        } catch (java.sql.SQLException e) {

            showError(e);
        }
    }

    // =========================================================
    // LOAD INCIDENTS
    // =========================================================

    private void loadIncidents()
            throws java.sql.SQLException {

        incidentModel.setRowCount(0);

        String sql =
                "SELECT " +
                "i.in_severity, " +
                "i.in_threat, " +
                "CONCAT(i.in_street, ', ', i.in_city) AS location, " +
                "i.in_status, " +
                "COALESCE((" +
                "SELECT COUNT(*) " +
                "FROM tbl_dispatch d " +
                "WHERE d.incident_id=i.incident_id " +
                "AND d.dispatch_status='Active'" +
                "),0) AS assigned " +
                "FROM tbl_incident i " +
                "WHERE i.in_status <> 'Resolved' " +
                "ORDER BY FIELD(" +
                "i.in_severity," +
                "'Critical','High','Medium','Low'" +
                "), i.reported_at DESC " +
                "LIMIT 10";

        try (
                java.sql.Connection con =
                        DBConnection.getConnection();

                java.sql.PreparedStatement ps =
                        con.prepareStatement(sql);

                java.sql.ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                incidentModel.addRow(
                        new Object[]{
                                severityBadge(
                                        rs.getString(1)
                                ),

                                rs.getString(2),

                                rs.getString(3),

                                rs.getString(4),

                                rs.getInt(5)
                                        + " assigned"
                        }
                );
            }
        }
    }

    private String severityBadge(
            String severity) {

        if ("Critical".equalsIgnoreCase(
                severity)) {

            return "● Critical";
        }

        if ("High".equalsIgnoreCase(
                severity)) {

            return "● High";
        }

        if ("Medium".equalsIgnoreCase(
                severity)) {

            return "● Medium";
        }

        return "● Low";
    }

    // =========================================================
    // LOAD FIREFIGHTERS + SEARCH / FILTER
    // =========================================================

    private void loadFirefighters()
            throws java.sql.SQLException {

        allFirefighterData =
                firefighterDAO.getAll();

        applyFirefighterFilter();
    }

    private void applyFirefighterFilter() {

                String search =
                        ffSearchField != null
                                ? ffSearchField.getText()
                                .trim()
                                .toLowerCase()
                                : "";

                String expertise =
                        ffExpertiseFilter != null
                                ? (String) ffExpertiseFilter
                                .getSelectedItem()
                                : "All Expertise";

                String statusFilter =
                        ffStatusFilter != null
                                ? (String) ffStatusFilter
                                .getSelectedItem()
                                : "All Status";

                firefighterModel.setRowCount(0);

                for (Object[] row : allFirefighterData) {

                String id =
                        row[0].toString()
                                .toLowerCase();

                String name =
                        row[1].toString()
                                .toLowerCase();

                String exp =
                        row[3].toString();

                String status =
                        row[4].toString();

                boolean matchesSearch =
                        search.isEmpty()
                                || id.contains(search)
                                || name.contains(search);

                boolean matchesExp =
                        expertise.equals(
                                "All Expertise"
                        )
                                || exp.equalsIgnoreCase(
                                expertise
                        );

                boolean matchesStatus =
                        statusFilter.equals("All Status")
                                || status.equalsIgnoreCase(statusFilter);

                if (matchesSearch && matchesExp && matchesStatus) {

                        firefighterModel.addRow(
                                new Object[]{
                                        row[0],
                                        row[1],
                                        exp,
                                        status
                                }
                        );
                }
                }
        }

        // =========================================================
        // DOUBLE-CLICK FIREFIGHTER → Edit / Delete / Cancel
        // =========================================================

        private void onFirefighterDoubleClick(int row) {

        if (row < 0 || row >= firefighterTable.getRowCount()) {
                return;
        }

        String id = firefighterTable
                .getValueAt(row, 0)
                .toString();

        // Open the full Firefighter Management module
        showFirefighters();

        // Automatically select the firefighter
        firefighterManagementPanel.selectFirefighter(id);
        }

    // =========================================================
    // RECENT ACTIVITY
    // =========================================================

    private void loadActivity()
            throws java.sql.SQLException {

        activityPanel.removeAll();

        String sql =
                "SELECT activity, activity_time " +
                "FROM (" +

                "SELECT " +
                "CONCAT(" +
                "CONVERT('New incident: ' USING utf8mb4) COLLATE utf8mb4_unicode_ci, " +
                "CONVERT(in_threat USING utf8mb4) COLLATE utf8mb4_unicode_ci, " +
                "CONVERT(' — ' USING utf8mb4) COLLATE utf8mb4_unicode_ci, " +
                "CONVERT(in_city USING utf8mb4) COLLATE utf8mb4_unicode_ci" +
                ") AS activity, " +
                "reported_at AS activity_time " +
                "FROM tbl_incident " +

                "UNION ALL " +

                "SELECT " +
                "CONCAT(" +
                "CONVERT('Dispatch: ' USING utf8mb4) COLLATE utf8mb4_unicode_ci, " +
                "CONVERT(f.ff_name USING utf8mb4) COLLATE utf8mb4_unicode_ci, " +
                "CONVERT(' → ' USING utf8mb4) COLLATE utf8mb4_unicode_ci, " +
                "CONVERT(i.in_threat USING utf8mb4) COLLATE utf8mb4_unicode_ci" +
                ") AS activity, " +
                "d.dispatched_at AS activity_time " +

                "FROM tbl_dispatch d " +
                "JOIN tbl_firefighter f ON d.firefighter_id=f.firefighter_id " +
                "JOIN tbl_incident i ON d.incident_id=i.incident_id" +
                ") x " +
                "ORDER BY activity_time DESC " +
                "LIMIT 5";

        try (
                java.sql.Connection con =
                        DBConnection.getConnection();

                java.sql.PreparedStatement ps =
                        con.prepareStatement(sql);

                java.sql.ResultSet rs =
                        ps.executeQuery()
        ) {

            SimpleDateFormat fmt =
                    new SimpleDateFormat(
                            "HH:mm"
                    );

            while (rs.next()) {

                JLabel line =
                        new JLabel(
                                "●  "
                                        + fmt.format(
                                        rs.getTimestamp(2)
                                )
                                        + "   "
                                        + rs.getString(1)
                        );

                line.setForeground(
                        UI.MUTED
                );

                line.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                12
                        )
                );

                line.setBorder(
                        new EmptyBorder(
                                4,
                                0,
                                4,
                                0
                        )
                );

                activityPanel.add(line);
            }

            if (activityPanel
                    .getComponentCount() == 0) {

                JLabel empty =
                        new JLabel(
                                "No recent activity yet."
                        );

                empty.setForeground(
                        UI.MUTED
                );

                empty.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                12
                        )
                );

                activityPanel.add(empty);
            }
        }

        activityPanel.revalidate();
        activityPanel.repaint();
    }

    // =========================================================
    // STATUS RENDERER (red / green)
    // =========================================================

    private static class StatusRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            Component c =
                    super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                    );

            String status =
                    value != null
                            ? value.toString()
                            : "";

            if ("Resolved"
                    .equalsIgnoreCase(status)) {

                c.setForeground(
                        new Color(40, 180, 80)
                );   // green

            } else {

                c.setForeground(
                        new Color(230, 60, 70)
                );   // red
            }

            if (!isSelected) {

                c.setBackground(
                        table.getBackground()
                );
            }

            return c;
        }
    }

    // =========================================================
    // ERROR DIALOG
    // =========================================================

    private void showError(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(
                    UIManager
                            .getSystemLookAndFeelClassName()
            );

        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(
                () -> new MainForm()
                        .setVisible(true)
        );
    }
}
