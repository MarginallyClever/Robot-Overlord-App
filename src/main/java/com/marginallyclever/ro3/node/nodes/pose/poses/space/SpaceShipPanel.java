package com.marginallyclever.ro3.node.nodes.pose.poses.space;

import com.marginallyclever.convenience.helpers.MatrixHelper;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel for editing a SpaceShip.
 */
public class SpaceShipPanel extends JPanel {
    private final SpaceShip ship;
    boolean relative=true;

    public SpaceShipPanel() {
        this(new SpaceShip());
    }

    public SpaceShipPanel(SpaceShip ship) {
        super(new GridBagLayout());
        this.ship = ship;

        var c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        JComboBox<String> modes = new JComboBox<>(new String[]{"Relative","Absolute"});
        modes.addActionListener(e -> {
            var mode = (String)modes.getSelectedItem();
            relative = (mode != null) && mode.equals("Relative");
        });

        // add buttons for linear acceleration
        c.gridwidth=3;
        add(modes,c);  c.gridy++;
        add(new JSeparator(),c);  c.gridy++;
        add(new JLabel("linear velocity"),c);  c.gridy++;
        add(createButton("Stop", e -> ship.linearVelocity.set(0,0,0)),c);  c.gridy++;
        c.gridwidth=1;
        add(createButton("+X", e -> addAcceleration(1,0,0)),c);  c.gridx++;
        add(createButton("+Y", e -> addAcceleration(0,1,0)),c);  c.gridx++;
        add(createButton("+Z", e -> addAcceleration(0,0,1)),c);  c.gridx++;
        c.gridx=0; c.gridy++;
        add(createButton("-X", e -> addAcceleration(-1,0,0)),c);  c.gridx++;
        add(createButton("-Y", e -> addAcceleration(0,-1,0)),c);  c.gridx++;
        add(createButton("-Z", e -> addAcceleration(0,0,-1)),c);  c.gridx++;
        c.gridx=0; c.gridy++;

        // add buttons for angular torque
        c.gridwidth=3;
        add(new JSeparator(),c);  c.gridy++;
        add(new JLabel("angular velocity"),c);  c.gridy++;
        add(createButton("Stop", e -> ship.angularVelocity.set(0,0,0)),c);  c.gridy++;
        c.gridwidth=1;
        add(createButton("+X", e -> addTorque(1,0,0)),c);  c.gridx++;
        add(createButton("+Y", e -> addTorque(0,1,0)),c);  c.gridx++;
        add(createButton("+Z", e -> addTorque(0,0,1)),c);  c.gridx++;
        c.gridx=0; c.gridy++;
        add(createButton("-X", e -> addTorque(-1,0,0)),c);  c.gridx++;
        add(createButton("-Y", e -> addTorque(0,-1,0)),c);  c.gridx++;
        add(createButton("-Z", e -> addTorque(0,0,-1)),c);  c.gridx++;
        c.gridx=0; c.gridy++;
    }

    private JButton createButton(String label, ActionListener action) {
        JButton bXpos = new JButton(label);
        bXpos.addActionListener(action);
        return bXpos;
    }

    private void addAcceleration(double x,double y,double z) {
        var m = getMatrix();

        ship.acceleration.scaleAdd(x, MatrixHelper.getXAxis(m), ship.acceleration);
        ship.acceleration.scaleAdd(y, MatrixHelper.getYAxis(m), ship.acceleration);
        ship.acceleration.scaleAdd(z, MatrixHelper.getZAxis(m), ship.acceleration);
    }

    private void addTorque(double x,double y,double z) {
        var m = getMatrix();
        ship.torque.scaleAdd(x, MatrixHelper.getXAxis(m), ship.torque);
        ship.torque.scaleAdd(y, MatrixHelper.getYAxis(m), ship.torque);
        ship.torque.scaleAdd(z, MatrixHelper.getZAxis(m), ship.torque);
    }

    private Matrix4d getMatrix() {
        Matrix4d m;
        if(!relative) {
            m = ship.getWorld();
            m.invert();
        } else {
            m = MatrixHelper.createIdentityMatrix4();
        }
        return m;
    }
}
