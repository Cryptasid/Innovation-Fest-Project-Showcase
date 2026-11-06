import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;

public class DispatchForm extends JPanel {

    private final FirefighterDAO firefighterDAO = new FirefighterDAO();
    private final IncidentDAO incidentDAO = new IncidentDAO();
    private final DispatchDAO dispatchDAO = new DispatchDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    private Integer preselectedIncidentId;
    private final Runnable onSaved;

    private JComboBox<FirefighterItem> firefighterCombo;
    private JComboBox<IncidentItem> incidentCombo;
    private JComboBox<VehicleItem> vehicleCombo;
    private JDateChooser dateChooser;
    private JTable table;

    public DispatchForm(Runnable onSaved) {
        this(onSaved, null);
    }

    public DispatchForm(Runnable onSaved, Integer incidentId) {
        this.onSaved = onSaved;
        this.preselectedIncidentId = incidentId;

        setLayout(new BorderLayout());
        setBackground(UI.BG);

        buildUI();
        loadChoices();
        loadTable();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UI.BG);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel form = UI.card();
        form.setLayout(new GridBagLayout());

        firefighterCombo = new JComboBox<>();
        incidentCombo = new JComboBox<>();
        vehicleCombo = new JComboBox<>();

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd-MM-yyyy");
        dateChooser.setDate(new java.util.Date());
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        UI.styleCombo(firefighterCombo);
        UI.styleCombo(incidentCombo);
        UI.styleCombo(vehicleCombo);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addField(form, g, 0, "Available Firefighter", firefighterCombo);
        addField(form, g, 1, "Incident", incidentCombo);
        addField(form, g, 2, "Vehicle (Optional)", vehicleCombo);
        addField(form, g, 3, "Dispatch Date", dateChooser);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton assign = UI.primaryButton("Assign Firefighter");
        JButton remove = UI.dangerButton("Remove Selected");
        JButton refresh = UI.secondaryButton("Refresh");

        assign.addActionListener(e -> assign());
        remove.addActionListener(e -> remove());

        refresh.addActionListener(e -> refreshData());

        buttons.add(assign);
        buttons.add(remove);
        buttons.add(refresh);

        g.gridx = 1;
        g.gridy = 4;
        g.gridwidth = 2;
        g.insets = new Insets(16, 8, 4, 8);
        form.add(buttons, g);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(UI.title("Dispatch Management"), BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);

        JPanel tableSection = UI.card();
        tableSection.setLayout(new BorderLayout(0, 12));

        tableSection.add(
                UI.sectionTitle("Active Dispatches  (double-click to complete)"),
                BorderLayout.NORTH
        );

        table = new JTable(new DefaultTableModel(
                new Object[]{
                        "ID", "Firefighter", "Threat", "City",
                        "Severity", "Vehicle", "Status", "Dispatched At"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        UI.styleTable(table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2
                        && table.getSelectedRow() != -1) {
                    completeSelected();
                }
            }
        });

        tableSection.add(UI.scroll(table), BorderLayout.CENTER);
        root.add(tableSection, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }

    private void addField(
            JPanel panel, GridBagConstraints g,
            int row, String label, Component component) {

        g.gridy = row;
        g.gridx = 0;
        g.gridwidth = 1;
        panel.add(UI.label(label), g);

        g.gridx = 1;
        g.gridwidth = 2;
        panel.add(component, g);
    }

    private void loadChoices() {
        try {
            firefighterCombo.removeAllItems();

            for (Firefighter f : firefighterDAO.getAvailable()) {
                firefighterCombo.addItem(
                        new FirefighterItem(
                                f.getId(), f.getName(), f.getExpertise()
                        )
                );
            }

            incidentCombo.removeAllItems();

            for (Incident i : incidentDAO.getAllObjects()) {
                IncidentItem item = new IncidentItem(
                        i.getId(), i.getThreat(), i.getCity(), i.getSeverity()
                );

                incidentCombo.addItem(item);

                if (preselectedIncidentId != null
                        && i.getId() == preselectedIncidentId) {
                    incidentCombo.setSelectedItem(item);
                }
            }

            vehicleCombo.removeAllItems();
            vehicleCombo.addItem(new VehicleItem(null, "No Vehicle", ""));

            for (Vehicle v : vehicleDAO.getAvailable()) {
                vehicleCombo.addItem(
                        new VehicleItem(
                                v.getId(), v.getType(), v.getRegistrationNumber()
                        )
                );
            }

        } catch (Exception e) {
            error(e);
        }
    }

    private void assign() {
        FirefighterItem f =
                (FirefighterItem) firefighterCombo.getSelectedItem();

        IncidentItem i =
                (IncidentItem) incidentCombo.getSelectedItem();

        VehicleItem v =
                (VehicleItem) vehicleCombo.getSelectedItem();

        if (f == null || i == null || v == null
                || dateChooser.getDate() == null) {
            warn("Please select a firefighter, incident and date.");
            return;
        }

        LocalDate local = dateChooser.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        try {
            dispatchDAO.add(f.id, i.id, new Date(local), v.id);

            JOptionPane.showMessageDialog(
                    this, "Dispatch created successfully."
            );

            loadChoices();
            loadTable();
            onSaved.run();

        } catch (Exception e) {
            error(e);
        }
    }

    private void remove() {
        int r = table.getSelectedRow();

        if (r < 0) {
            warn("Select a dispatch record first.");
            return;
        }

        int id = (int) table.getValueAt(r, 0);

        int c = JOptionPane.showConfirmDialog(
                this,
                "Remove this dispatch assignment?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (c != JOptionPane.YES_OPTION) return;

        try {
            dispatchDAO.delete(id);

            JOptionPane.showMessageDialog(
                    this, "Dispatch assignment removed."
            );

            loadChoices();
            loadTable();
            onSaved.run();

        } catch (Exception e) {
            error(e);
        }
    }

    private void completeSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        int id = (int) table.getValueAt(r, 0);
        String firefighter = table.getValueAt(r, 1).toString();
        String threat = table.getValueAt(r, 2).toString();

        String[] options = {
                "Mark as Completed",
                "Cancel"
        };

        int choice = JOptionPane.showOptionDialog(
                this,
                "Dispatch #" + id
                        + "\nFirefighter: " + firefighter
                        + "\nThreat: " + threat
                        + "\n\nMark this dispatch as Completed?\n"
                        + "The incident will be set to Resolved only "
                        + "when no active dispatches remain.",
                "Complete Dispatch",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            try {
                dispatchDAO.complete(id);

                JOptionPane.showMessageDialog(
                        this,
                        "Dispatch marked as Completed.\n"
                                + "Firefighter is now available.\n"
                                + "Incident is Resolved only if this was "
                                + "the last active assignment."
                );

                loadChoices();
                loadTable();
                onSaved.run();

            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void loadTable() {
        try {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);

            for (Object[] row : dispatchDAO.getActive()) {
                m.addRow(row);
            }

        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * Reloads dispatch data when the user opens this module.
     */
    public void refreshData() {
        loadChoices();
        loadTable();
    }

    /**
     * Reloads dispatch data and optionally selects a specific incident.
     * Used when opening Dispatch Management from a map marker.
     */
    public void prepareForDisplay(Integer incidentId) {
        preselectedIncidentId = incidentId;
        refreshData();
    }

    private void warn(String s) {
        JOptionPane.showMessageDialog(
                this, s, "Validation", JOptionPane.WARNING_MESSAGE
        );
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(
                this, e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class FirefighterItem {
        String id, name;
        Expertise expertise;

        FirefighterItem(String id, String name, Expertise expertise) {
            this.id = id;
            this.name = name;
            this.expertise = expertise;
        }

        @Override
        public String toString() {
            return id + "  •  " + name + "  (" + expertise + ")";
        }
    }

    private static class VehicleItem {
        String id, type, registration;

        VehicleItem(String id, String type, String registration) {
            this.id = id;
            this.type = type;
            this.registration = registration;
        }

        @Override
        public String toString() {
            if (id == null) return "No Vehicle";

            return id + "  •  " + type + "  (" + registration + ")";
        }
    }

    private static class IncidentItem {
        int id;
        String threat, city;
        IncidentSeverity severity;

        IncidentItem(
                int id, String threat, String city,
                IncidentSeverity severity) {
            this.id = id;
            this.threat = threat;
            this.city = city;
            this.severity = severity;
        }

        @Override
        public String toString() {
            return "#" + id + "  •  " + threat
                    + "  —  " + city + "  [" + severity + "]";
        }
    }
}