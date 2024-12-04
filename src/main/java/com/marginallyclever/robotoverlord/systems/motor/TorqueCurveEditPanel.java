package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.convenience.swing.graph.SingleLineGraph;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Deprecated public class TorqueCurveEditPanel extends JPanel {
    public TorqueCurveEditPanel(MotorComponent motor) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SingleLineGraph graph = new SingleLineGraph();
        RPMToTorqueTable table = new RPMToTorqueTable();

        TreeMap<Double, Double> curve = motor.getTorqueCurve();
        List<Double> keys = new ArrayList<>(curve.keySet());
        for (Double key : keys) {
            graph.addValue(key, curve.get(key));
            table.addValue(key, curve.get(key));
        }
        graph.setBoundsToData();
        graph.setXMin(0);
        graph.setYMin(0);
        graph.setGridSpacingX(10);
        graph.setGridSpacingY(1);

        table.addDataChangeListener((evt) -> {
            graph.clear();
            motor.getTorqueCurve().clear();

            TableModel model = (TableModel) evt.getSource();
            int rows = model.getRowCount();
            for(int i=0;i<rows;++i) {
                try {
                    double rpm = Double.parseDouble(model.getValueAt(i, 0).toString());
                    double torque = Double.parseDouble(model.getValueAt(i, 1).toString());
                    graph.addValue(rpm, torque);
                    motor.setTorqueAtRPM(rpm, torque);
                } catch(NumberFormatException ignored) {}
            }
            graph.setBoundsToData();
            graph.setXMin(0);
            graph.setYMin(0);
            graph.repaint();
        });

        graph.setPreferredSize(new Dimension(300, 200));
        table.setPreferredSize(new Dimension(300, 200));
        add(graph, BorderLayout.CENTER);
        add(table, BorderLayout.SOUTH);
    }
}
