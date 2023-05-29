package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.convenience.swing.LineGraph;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.OriginAdjustComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;

import javax.swing.*;
import javax.vecmath.Matrix4d;
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
        ViewElementButton bCurve = view.addButton("Torque curve");
        bCurve.addActionEventListener(e -> MotorSystem.editCurve(bCurve,motor) );
    }

    public static void editCurve(JComponent parent, MotorComponent motor) {
        LineGraph graph = new LineGraph();
        TreeMap<Integer,Double> curve = motor.getTorqueCurve();
        List<Integer> keys = new ArrayList<>(curve.keySet());
        for (Integer key : keys) {
            graph.add(key, curve.get(key));
        }
        graph.setPreferredSize(new Dimension(400,300));
        EntitySystemUtils.makePanel(graph, parent,"Torque curve");
    }
}
