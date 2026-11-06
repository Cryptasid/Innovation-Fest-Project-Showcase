import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;

public class IncidentForm extends JPanel {

    private final IncidentDAO dao = new IncidentDAO();
    private final Runnable onSaved;

    private JTextField threatField, streetField, cityField;
    private JDateChooser dateChooser;
    private JComboBox<IncidentSeverity> severityCombo;
    private JComboBox<IncidentStatus> statusCombo;
    private JTable table;

    private JLabel selectedLocationLabel;

    private Double selectedLatitude;
    private Double selectedLongitude;
    private String selectedAddress;

    public IncidentForm(Runnable onSaved) {
        this.onSaved = onSaved;

        setLayout(new BorderLayout());
        setBackground(UI.BG);

        buildUI();
        loadTable();
        clearForm();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UI.BG);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel form = UI.card();
        form.setLayout(new GridBagLayout());

        threatField = UI.field();
        streetField = UI.field();
        cityField = UI.field();

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd-MM-yyyy");
        dateChooser.setDate(new java.util.Date());

        severityCombo = new JComboBox<>(IncidentSeverity.values());
        statusCombo = new JComboBox<>(IncidentStatus.values());
        statusCombo.setSelectedItem(IncidentStatus.ACTIVE);

        styleCombo(severityCombo);
        styleCombo(statusCombo);

        selectedLocationLabel =
                UI.mutedLabel("No map location selected");

        JButton chooseLocation =
                UI.primaryButton("Select Location on Map");

        JButton clearLocation =
                UI.secondaryButton("Clear Map Location");

        chooseLocation.addActionListener(e -> chooseLocation());
        clearLocation.addActionListener(e -> clearLocation());

        JPanel locationPanel = new JPanel(new BorderLayout(10, 0));
        locationPanel.setOpaque(false);
        locationPanel.add(selectedLocationLabel, BorderLayout.CENTER);

        JPanel locationButtons =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        locationButtons.setOpaque(false);
        locationButtons.add(chooseLocation);
        locationButtons.add(clearLocation);

        locationPanel.add(locationButtons, BorderLayout.EAST);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addField(form, g, 0, "Threat", threatField);
        addField(form, g, 1, "Street", streetField);
        addField(form, g, 2, "City", cityField);
        addField(form, g, 3, "Map Location", locationPanel);
        addField(form, g, 4, "Incident Date", dateChooser);
        addField(form, g, 5, "Severity", severityCombo);
        addField(form, g, 6, "Status", statusCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton save = UI.button("Save Incident", UI.ACCENT);
        JButton update = UI.button("Update Selected", UI.PANEL);
        JButton del = UI.button("Delete Selected", UI.ACCENT_DARK);
        JButton clear = UI.button("Clear", UI.PANEL);

        save.addActionListener(e -> save());
        update.addActionListener(e -> update());
        del.addActionListener(e -> delete());
        clear.addActionListener(e -> clearForm());

        buttons.add(save);
        buttons.add(update);
        buttons.add(del);
        buttons.add(clear);

        g.gridx = 1;
        g.gridy = 7;
        g.gridwidth = 2;
        form.add(buttons, g);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(UI.title("Incident Management"), BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(
                new Object[]{
                        "ID", "Severity", "Threat", "Street", "City",
                        "Date", "Status", "Reported", "Latitude", "Longitude"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        UI.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillSelected();
            }
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void styleCombo(JComboBox<?> c) {
    UI.styleCombo(c);
}

    private void addField(
            JPanel p, GridBagConstraints g,
            int row, String label, Component c) {

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        p.add(UI.label(label), g);

        g.gridx = 1;
        g.gridwidth = 2;
        p.add(c, g);
    }

    private void chooseLocation() {
        MapLocationPicker.open(
                SwingUtilities.getWindowAncestor(this),
                (latitude, longitude, address) -> {

                    selectedLatitude = latitude;
                    selectedLongitude = longitude;
                    selectedAddress = address;

                    selectedLocationLabel.setText(String.format(
                            "%s  (%.6f, %.6f)",
                            address == null || address.isBlank()
                                    ? "Selected location" : address,
                            latitude, longitude
                    ));
                }
        );
    }

    private void clearLocation() {
        selectedLatitude = null;
        selectedLongitude = null;
        selectedAddress = null;
        selectedLocationLabel.setText("No map location selected");
    }

    private Incident data(int id) {
        String threat = threatField.getText().trim();
        String street = streetField.getText().trim();
        String city = cityField.getText().trim();

        if (threat.isEmpty() || street.isEmpty() || city.isEmpty()
                || dateChooser.getDate() == null) {
            throw new IllegalArgumentException(
                    "Please complete all incident fields."
            );
        }

        LocalDate d = dateChooser.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return new Incident(
                id,
                threat,
                street,
                city,
                new Date(d),
                (IncidentSeverity) severityCombo.getSelectedItem(),
                (IncidentStatus) statusCombo.getSelectedItem(),
                selectedLatitude,
                selectedLongitude
        );
    }

    private void save() {
        try {
            int id = dao.add(data(0));

            JOptionPane.showMessageDialog(
                    this, "Incident #" + id + " added successfully."
            );

            loadTable();
            clearForm();
            onSaved.run();

        } catch (IllegalArgumentException e) {
            warn(e.getMessage());
        } catch (java.sql.SQLException e) {
            error(e);
        }
    }

    private void update() {
        int r = table.getSelectedRow();

        if (r < 0) {
            warn("Select an incident from the table first.");
            return;
        }

        try {
            int id = ((Number) table.getValueAt(r, 0)).intValue();
            dao.update(data(id));

            JOptionPane.showMessageDialog(
                    this, "Incident updated successfully."
            );

            loadTable();
            onSaved.run();

        } catch (IllegalArgumentException e) {
            warn(e.getMessage());
        } catch (java.sql.SQLException e) {
            error(e);
        }
    }

    private void delete() {
        int r = table.getSelectedRow();

        if (r < 0) {
            warn("Select an incident from the table first.");
            return;
        }

        int id = ((Number) table.getValueAt(r, 0)).intValue();

        int c = JOptionPane.showConfirmDialog(
                this,
                "Delete incident #" + id
                        + "? Related dispatch records will also be removed.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (c != JOptionPane.YES_OPTION) return;

        try {
            dao.delete(id);

            JOptionPane.showMessageDialog(
                    this, "Incident deleted successfully."
            );

            loadTable();
            clearForm();
            onSaved.run();

        } catch (java.sql.SQLException e) {
            error(e);
        }
    }

    private void fillSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        threatField.setText(table.getValueAt(r, 2).toString());
        streetField.setText(table.getValueAt(r, 3).toString());
        cityField.setText(table.getValueAt(r, 4).toString());

        severityCombo.setSelectedItem(
                IncidentSeverity.fromString(table.getValueAt(r, 1).toString())
        );

        statusCombo.setSelectedItem(
                IncidentStatus.fromString(table.getValueAt(r, 6).toString())
        );

        LocalDate d = (LocalDate) table.getValueAt(r, 5);

        dateChooser.setDate(java.util.Date.from(
                d.atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));

        Object lat = table.getValueAt(r, 8);
        Object lng = table.getValueAt(r, 9);

        if (lat instanceof Number && lng instanceof Number) {
            selectedLatitude = ((Number) lat).doubleValue();
            selectedLongitude = ((Number) lng).doubleValue();
        } else {
            selectedLatitude = null;
            selectedLongitude = null;
        }

        if (selectedLatitude != null && selectedLongitude != null) {
            selectedAddress = streetField.getText() + ", " + cityField.getText();

            selectedLocationLabel.setText(String.format(
                    "%s  (%.6f, %.6f)",
                    selectedAddress, selectedLatitude, selectedLongitude
            ));
        } else {
            clearLocation();
        }
    }

    private void clearForm() {
        threatField.setText("");
        streetField.setText("");
        cityField.setText("");

        dateChooser.setDate(new java.util.Date());
        severityCombo.setSelectedItem(IncidentSeverity.MEDIUM);
        statusCombo.setSelectedItem(IncidentStatus.ACTIVE);

        clearLocation();
        table.clearSelection();
    }

    private void loadTable() {
        try {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);

            for (Object[] row : dao.getAll()) {
                m.addRow(row);
            }

        } catch (java.sql.SQLException e) {
            error(e);
        }
    }

    public void refreshData() {
        loadTable();
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
}