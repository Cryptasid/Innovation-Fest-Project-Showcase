import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FirefighterForm extends JPanel {

    private final FirefighterDAO dao = new FirefighterDAO();
    private final Runnable onSaved;

    private JTextField idField, nameField, contactField;
    private ButtonGroup group;
    private JRadioButton rescuer, medic, electric, chemical;
    private JTable table;

    public FirefighterForm(Runnable onSaved) {
        this.onSaved = onSaved;

        setLayout(new BorderLayout());
        setBackground(UI.BG);

        buildUI();
        loadTable();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UI.BG);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel form = UI.card();
        form.setLayout(new GridBagLayout());

        idField = UI.field();
        nameField = UI.field();
        contactField = UI.field();

        rescuer = radio("Rescuer");
        medic = radio("Medic");
        electric = radio("Electric Technician");
        chemical = radio("Chemical Technician");

        group = new ButtonGroup();

        for (JRadioButton r : new JRadioButton[]{
                rescuer, medic, electric, chemical
        }) {
            group.add(r);
        }

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addField(form, g, 0, "Firefighter ID", idField);
        addField(form, g, 1, "Full Name", nameField);
        addField(form, g, 2, "Contact", contactField);

        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 1;
        form.add(UI.label("Expertise"), g);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radios.setOpaque(false);
        radios.add(rescuer);
        radios.add(medic);
        radios.add(electric);
        radios.add(chemical);

        g.gridx = 1;
        g.gridwidth = 2;
        form.add(radios, g);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton save = UI.button("Save Firefighter", UI.ACCENT);
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
        g.gridy = 4;
        g.gridwidth = 2;
        form.add(buttons, g);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(UI.title("Firefighter Management"), BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);

        table = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Name", "Contact", "Expertise", "Status"}, 0
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

    private JRadioButton radio(String s) {
        JRadioButton r = new JRadioButton(s);
        r.setForeground(UI.TEXT);
        r.setBackground(UI.CARD);
        r.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return r;
    }

    private void addField(
            JPanel p, GridBagConstraints g,
            int row, String label, JTextField f) {

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        p.add(UI.label(label), g);

        g.gridx = 1;
        g.gridwidth = 2;
        p.add(f, g);
    }

    private Expertise expertise() {
        if (rescuer.isSelected()) return Expertise.RESCUER;
        if (medic.isSelected()) return Expertise.MEDIC;
        if (electric.isSelected()) return Expertise.ELECTRIC_TECHNICIAN;
        if (chemical.isSelected()) return Expertise.CHEMICAL_TECHNICIAN;
        return null;
    }

    private Firefighter data() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        Expertise e = expertise();

        if (id.length() != 8 || !id.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "Firefighter ID must be exactly 8 digits."
            );
        }

        if (name.isEmpty() || contact.isEmpty() || e == null) {
            throw new IllegalArgumentException(
                    "Please complete all fields and select an expertise."
            );
        }

        return new Firefighter(id, name, contact, e);
    }

    private void save() {
        try {
            dao.add(data());
            JOptionPane.showMessageDialog(this,
                    "Firefighter added successfully.");

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
        if (table.getSelectedRow() < 0) {
            warn("Select a firefighter from the table first.");
            return;
        }

        try {
            dao.update(data());

            JOptionPane.showMessageDialog(this,
                    "Firefighter updated successfully.");

            loadTable();
            clearForm();
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
            warn("Select a firefighter from the table first.");
            return;
        }

        String id = table.getValueAt(r, 0).toString();

        int c = JOptionPane.showConfirmDialog(
                this,
                "Delete firefighter " + id
                        + "? Any active dispatch assignment will also be removed.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (c != JOptionPane.YES_OPTION) return;

        try {
            dao.delete(id);

            JOptionPane.showMessageDialog(this,
                    "Firefighter deleted successfully.");

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

        idField.setText(table.getValueAt(r, 0).toString());
        idField.setEditable(false);
        nameField.setText(table.getValueAt(r, 1).toString());
        contactField.setText(table.getValueAt(r, 2).toString());

        group.clearSelection();

        String x = table.getValueAt(r, 3).toString();

        if (x.equalsIgnoreCase("Rescuer")) {
            rescuer.setSelected(true);
        } else if (x.equalsIgnoreCase("Medic")) {
            medic.setSelected(true);
        } else if (x.equalsIgnoreCase("Electric Technician")) {
            electric.setSelected(true);
        } else {
            chemical.setSelected(true);
        }
    }

    private void clearForm() {
        idField.setText("");
        idField.setEditable(true);
        nameField.setText("");
        contactField.setText("");
        group.clearSelection();
        table.clearSelection();
    }

    private void loadTable() {
        try {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);

            for (Object[] r : dao.getAll()) {
                m.addRow(r);
            }

        } catch (java.sql.SQLException e) {
            error(e);
        }
    }

    public void refreshData() {
        loadTable();
    }

    public void selectFirefighter(String id) {
        for (int r = 0; r < table.getRowCount(); r++) {
            if (table.getValueAt(r, 0).toString().equals(id)) {
                table.setRowSelectionInterval(r, r);
                table.scrollRectToVisible(table.getCellRect(r, 0, true));
                fillSelected();
                return;
            }
        }
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