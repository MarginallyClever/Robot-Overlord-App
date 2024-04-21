package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.nodes.BouncingBallDemo;

import javax.swing.*;
import java.awt.*;

/**
 * Actions in this window will control the contents of the {@link com.marginallyclever.ro3.Registry#scene}.
 */
public class ODE4JPanel extends App {
    public ODE4JPanel() {
        super(new BorderLayout());

        // add a button to setup the first ODE4J simulation.
        JButton button = new JButton("Create ODE4J Simulation");
        button.addActionListener((e)->{
            BouncingBallDemo found = guaranteeBouncingBallDemoExists();
            found.reset();

        });
        add(button, BorderLayout.NORTH);

        //add(editorPane, BorderLayout.CENTER);
    }

    private BouncingBallDemo guaranteeBouncingBallDemoExists() {
        BouncingBallDemo found = Registry.getScene().findFirstChild(BouncingBallDemo.class);
        if(found==null) {
            found = new BouncingBallDemo();
            Registry.getScene().addChild(found);

        }
        return found;
    }
}
