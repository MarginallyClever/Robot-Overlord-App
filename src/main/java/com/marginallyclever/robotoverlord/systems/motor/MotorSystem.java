package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.convenience.swing.LineGraph;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>For motors</p>
 *
 * @author Dan Royer
 * @since 2.6.2
 */
public class MotorSystem implements EntitySystem {
    private final EntityManager entityManager;

    public MotorSystem(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if(component instanceof MotorComponent) decorateMotor(view, component);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}

    private void decorateMotor(ComponentPanelFactory view, Component component) {
        MotorComponent motor = (MotorComponent)component;
        view.add(motor.gearRatio);
        ViewElementButton bCurve = view.addButton("Torque curve");
        bCurve.addActionEventListener(e -> MotorSystem.editCurve(bCurve,motor) );
    }

    public static void editCurve(JComponent parent, MotorComponent motor) {
        LineGraph graph = new LineGraph();
        RPMToTorqueTable table = new RPMToTorqueTable();

        TreeMap<Integer,Double> curve = motor.getTorqueCurve();
        List<Integer> keys = new ArrayList<>(curve.keySet());
        for (Integer key : keys) {
            graph.addValue(key, curve.get(key));
            table.addValue(key, curve.get(key));
        }
        graph.setBoundsToData();
        graph.setXMin(0);
        graph.setYMin(0);

        table.addDataChangeListener((evt)->{
            int row = evt.getFirstRow();
            int col = evt.getColumn();
            TableModel model = (TableModel)evt.getSource();

            if(evt.getType() == TableModelEvent.DELETE) {
                try {
                    int rpm = (int) Integer.parseInt((String) model.getValueAt(row, 0));
                    graph.removeValue(rpm);
                    motor.removeTorqueAtRPM(rpm);
                } catch (NumberFormatException ignore) {}

            } else if(evt.getType() == TableModelEvent.UPDATE ||
                    evt.getType() == TableModelEvent.INSERT) {
                try {
                    int rpm = (int) Integer.parseInt((String)model.getValueAt(row, 0));
                    double torque = (double) Double.parseDouble((String)model.getValueAt(row, 1));
                    graph.removeValue(rpm);
                    graph.addValue(rpm, torque);
                    motor.setTorqueAtRPM(rpm, torque);
                } catch (NumberFormatException ignore) {}
            }
            graph.setBoundsToData();
            graph.setXMin(0);
            graph.setYMin(0);
            graph.repaint();
        });

        graph.setPreferredSize(new Dimension(300,200));
        table.setPreferredSize(new Dimension(300,200));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(graph,BorderLayout.CENTER);
        panel.add(table,BorderLayout.SOUTH);


        EntitySystemUtils.makePanel(panel, parent,"Torque curve");
    }
}
