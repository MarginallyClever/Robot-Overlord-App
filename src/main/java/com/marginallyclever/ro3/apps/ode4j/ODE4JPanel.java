package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.nodes.ode4j.BouncingBallDemo;
import com.marginallyclever.ro3.node.nodes.ode4j.FallingCubeDemo;

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
        addButtonByNameAndCallback(toolbar, "Start Bouncing Ball", (e)->{
            BouncingBallDemo found = guaranteeBouncingBallDemoExists();
            found.reset();
        });

        // demo 2
        addButtonByNameAndCallback(toolbar, "Start Falling Cube", (e)->{
            FallingCubeDemo found = guaranteeFallingCubeExists();
            found.reset();
        });
    }

    private void addButtonByNameAndCallback(JToolBar toolbar, String title, ActionListener actionListener) {
        JButton button = new JButton(title);
        button.addActionListener(actionListener);
        toolbar.add(button);
    }

    private BouncingBallDemo guaranteeBouncingBallDemoExists() {
        BouncingBallDemo found = Registry.getScene().findFirstChild(BouncingBallDemo.class);
        if(found==null) {
            found = new BouncingBallDemo();
            Registry.getScene().addChild(found);

        }
        return found;
    }

    private FallingCubeDemo guaranteeFallingCubeExists() {
        FallingCubeDemo found = Registry.getScene().findFirstChild(FallingCubeDemo.class);
        if(found==null) {
            found = new FallingCubeDemo();
            Registry.getScene().addChild(found);

        }
        return found;
    }
}
