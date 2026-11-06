import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VehicleForm extends JPanel {

    private final VehicleDAO dao = new VehicleDAO();
    private final Runnable onSaved;

    private JTextField idField, registrationField;
    private JComboBox<String> typeCombo, statusCombo;
    private JTable table;

    private static final String[] TYPES = {
            "Pumper Fire Truck",
            "Ladder Truck",
            "Water Tanker",
            "Brush Engine",
            "Rescue / Squad Truck"
    };

    public VehicleForm(Runnable onSaved) {
        this.onSaved = onSaved;

        setLayout(new BorderLayout());
        setBackground(UI.BG);

        buildUI();
        loadTable();
        clearForm();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(UI.BG);
        root.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JPanel form = UI.card();
        form.setLayout(new GridBagLayout());

        idField = UI.field();
        registrationField = UI.field();

        typeCombo = new JComboBox<>(TYPES);
        statusCombo = new JComboBox<>(
                new String[]{"Available", "Maintenance"}
        );

        UI.styleCombo(typeCombo);
        UI.styleCombo(statusCombo);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 7, 7, 7);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addField(form, g, 0, "Vehicle ID", idField);
        addField(form, g, 1, "Vehicle Type", typeCombo);
        addField(form, g, 2, "Registration Number", registrationField);
        addField(form, g, 3, "Status", statusCombo);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton add = UI.primaryButton("Add Vehicle");
        JButton update = UI.secondaryButton("Update Selected");
        JButton delete = UI.dangerButton("Delete Selected");
        JButton clear = UI.secondaryButton("Clear");

        add.addActionListener(e -> addVehicle());
        update.addActionListener(e -> updateVehicle());
        delete.addActionListener(e -> deleteVehicle());
        clear.addActionListener(e -> clearForm());

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(clear);

        g.gridx = 1;
        g.gridy = 4;
        g.gridwidth = 2;
        g.insets = new Insets(12, 7, 5, 7);
        form.add(buttons, g);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(UI.title("Vehicle Management"), BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(
                new Object[]{
                        "Vehicle ID", "Vehicle Type",
                        "Registration Number", "Status"
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

        JPanel tableCard = UI.card();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(
                UI.sectionTitle("Registered Vehicles"),
                BorderLayout.NORTH
        );
        tableCard.add(UI.scroll(table), BorderLayout.CENTER);

        root.add(tableCard, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void addField(
            JPanel p, GridBagConstraints g,
            int row, String label, Component c) {

        g.gridy = row;
        g.gridx = 0;
        g.gridwidth = 1;
        p.add(UI.label(label), g);

        g.gridx = 1;
        g.gridwidth = 2;
        p.add(c, g);
    }

    private Vehicle data() {
        String id = idField.getText().trim();
        String registration = registrationField.getText().trim();

        if (id.isEmpty() || registration.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter the vehicle ID and registration number."
            );
        }

        return new Vehicle(
                id,
                (String) typeCombo.getSelectedItem(),
                registration,
                (String) statusCombo.getSelectedItem()
        );
    }

    private void addVehicle() {
        try {
            dao.add(data());

            JOptionPane.showMessageDialog(
                    this, "Vehicle added successfully."
            );

            loadTable();
            clearForm();
            onSaved.run();

        } catch (IllegalArgumentException e) {
            warn(e.getMessage());
        } catch (Exception e) {
            error(e);
        }
    }

    private void updateVehicle() {
        int r = table.getSelectedRow();

        if (r < 0) {
            warn("Select a vehicle from the table first.");
            return;
        }

        try {
            Vehicle v = data();

            if (!v.getId().equals(table.getValueAt(r, 0).toString())) {
                warn("Vehicle ID cannot be changed.");
                return;
            }

            dao.update(v);

            JOptionPane.showMessageDialog(
                    this, "Vehicle updated successfully."
            );

            loadTable();
            onSaved.run();

        } catch (IllegalArgumentException e) {
            warn(e.getMessage());
        } catch (Exception e) {
            error(e);
        }
    }

    private void deleteVehicle() {
        int r = table.getSelectedRow();

        if (r < 0) {
            warn("Select a vehicle from the table first.");
            return;
        }

        String id = table.getValueAt(r, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete vehicle " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            dao.delete(id);

            JOptionPane.showMessageDialog(
                    this, "Vehicle deleted successfully."
            );

            loadTable();
            clearForm();
            onSaved.run();

        } catch (Exception e) {
            error(e);
        }
    }

    private void fillSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        idField.setText(table.getValueAt(r, 0).toString());
        idField.setEditable(false);

        typeCombo.setSelectedItem(table.getValueAt(r, 1).toString());
        registrationField.setText(table.getValueAt(r, 2).toString());

        String status = table.getValueAt(r, 3).toString();

        if ("Dispatched".equals(status)
                && statusCombo.getItemCount() == 2) {
            statusCombo.addItem("Dispatched");
        }

        statusCombo.setSelectedItem(status);
        statusCombo.setEnabled(!"Dispatched".equals(status));
    }

    private void clearForm() {
        idField.setText("");
        idField.setEditable(true);

        registrationField.setText("");
        typeCombo.setSelectedIndex(0);

        if (statusCombo.getItemCount() == 3) {
            statusCombo.removeItem("Dispatched");
        }

        statusCombo.setSelectedItem("Available");
        statusCombo.setEnabled(true);

        table.clearSelection();
    }

    private void loadTable() {
        try {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);

            for (Vehicle v : dao.getAll()) {
                m.addRow(new Object[]{
                        v.getId(),
                        v.getType(),
                        v.getRegistrationNumber(),
                        v.getStatus()
                });
            }

        } catch (Exception e) {
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
                this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}