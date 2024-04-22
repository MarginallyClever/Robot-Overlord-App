package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.nodes.ode4j.ODESphere;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEBox;
import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Actions in this window will control the contents of the {@link com.marginallyclever.ro3.Registry#scene}.
 */
public class ODE4JPanel extends App {
    public ODE4JPanel() {
        super(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        add(toolbar, BorderLayout.NORTH);


        // demo 1
        addButtonByNameAndCallback(toolbar, "Add Sphere", (e)->{
            ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
            ODE4JHelper.guaranteeFloor(physics);
            Registry.getScene().addChild(new ODESphere());
        });

        // demo 2
        addButtonByNameAndCallback(toolbar, "Add Box", (e)->{
            ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
            ODE4JHelper.guaranteeFloor(physics);
            Registry.getScene().addChild(new ODEBox());
        });
    }

    private void addButtonByNameAndCallback(JToolBar toolbar, String title, ActionListener actionListener) {
        JButton button = new JButton(title);
        button.addActionListener(actionListener);
        toolbar.add(button);
    }
}
