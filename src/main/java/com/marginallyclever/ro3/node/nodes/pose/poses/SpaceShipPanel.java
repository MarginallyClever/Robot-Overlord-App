package com.marginallyclever.ro3.node.nodes.pose.poses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SpaceShipPanel extends JPanel {
    private final SpaceShip ship;

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

        // add buttons for linear acceleration
        c.gridwidth=3;
        add(new JLabel("Linear Velocity"),c);  c.gridy++;
        add(createButton("Stop", e -> ship.velocity.set(0,0,0)),c);  c.gridy++;
        c.gridwidth=1;
        add(createButton("+X", e -> ship.acceleration.x++),c);  c.gridx++;
        add(createButton("+Y", e -> ship.acceleration.y++),c);  c.gridx++;
        add(createButton("+Z", e -> ship.acceleration.z++),c);  c.gridx++;
        c.gridx=0; c.gridy++;
        add(createButton("-X", e -> ship.acceleration.x--),c);  c.gridx++;
        add(createButton("-Y", e -> ship.acceleration.y--),c);  c.gridx++;
        add(createButton("-Z", e -> ship.acceleration.z--),c);  c.gridx++;
        c.gridx=0; c.gridy++;


        // add buttons for torque
        c.gridwidth=3;
        add(new JSeparator(),c);  c.gridy++;
        add(new JLabel("Angular Velocity"),c);  c.gridy++;
        add(createButton("Stop", e -> ship.angularVelocity.set(0,0,0)),c);  c.gridy++;
        c.gridwidth=1;
        add(createButton("+X", e -> ship.torque.x++),c);  c.gridx++;
        add(createButton("+Y", e -> ship.torque.y++),c);  c.gridx++;
        add(createButton("+Z", e -> ship.torque.z++),c);  c.gridx++;
        c.gridx=0; c.gridy++;
        add(createButton("-X", e -> ship.torque.x--),c);  c.gridx++;
        add(createButton("-Y", e -> ship.torque.y--),c);  c.gridx++;
        add(createButton("-Z", e -> ship.torque.z--),c);  c.gridx++;
        c.gridx=0; c.gridy++;
    }

    private JButton createButton(String label, ActionListener action) {
        JButton bXpos = new JButton(label);
        bXpos.addActionListener(action);
        return bXpos;
    }
}
