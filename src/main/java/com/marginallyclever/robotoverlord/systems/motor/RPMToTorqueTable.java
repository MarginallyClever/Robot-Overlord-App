package com.marginallyclever.robotoverlord.systems.motor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A two-column sortable table of RPM to torque values for a motor.
 * @see RPMToTorqueTable.DataChangeListener
 *
 * @since 2.6.2
 * @author Dan Royer
 */
public class RPMToTorqueTable extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;

    public interface DataChangeListener {
        void onDataChange(TableModelEvent e);
    }

    private final List<DataChangeListener> listeners = new ArrayList<>();

    public RPMToTorqueTable() {
        model = new DefaultTableModel(new Object[]{"RPM", "Torque"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        model.addTableModelListener(e -> {
            for(DataChangeListener listener : listeners) {
                listener.onDataChange(e);
            }
        });

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            model.addRow(new Object[]{"", ""});
        });

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
            }
        });
        removeButton.setEnabled(false);

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        addDataChangeListener(e -> {
            if(e.getType() != TableModelEvent.UPDATE) return;

            // allow only doubles
            int row = e.getFirstRow();
            int column = e.getColumn();
            if(column<0) return;

            TableModel model = (TableModel)e.getSource();
            String data = (String)model.getValueAt(row, column);

            if(data==null || data.trim().isEmpty()) return;
            try {
                Double.parseDouble(data);
            } catch (NumberFormatException ex) {
                model.setValueAt("", row, column);
            }
        });

        ListSelectionModel selectionModel = table.getSelectionModel();

        selectionModel.addListSelectionListener(e -> {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            removeButton.setEnabled(!lsm.isSelectionEmpty());
        });
    }

    public void addValue(int rpm, double torque) {
        model.addRow(new Object[]{rpm, torque});
    }

    public void addDataChangeListener(DataChangeListener listener) {
        this.listeners.add(listener);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Editable Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        RPMToTorqueTable table = new RPMToTorqueTable();

        frame.setContentPane(table);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}