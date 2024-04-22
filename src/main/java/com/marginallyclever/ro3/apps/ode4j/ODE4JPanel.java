package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.nodes.ode4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * Actions in this window will control the contents of the {@link com.marginallyclever.ro3.Registry#scene}.
 */
public class ODE4JPanel extends App {
    public ODE4JPanel() {
        super(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        add(toolbar, BorderLayout.NORTH);

        JButton pauseButton = addButtonByNameAndCallback(toolbar, "", (e)->{
            ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
            physics.setPaused(!physics.isPaused());
            updatePauseButton((JButton)e.getSource(), physics);
        });
        // I cannot call this here because the physics world has not been created yet.
        // updatePauseButton(pauseButton, ODE4JHelper.guaranteePhysicsWorld());
        // The workaround is to manually set the button.  Not pretty but it will have to do.
        pauseButton.setToolTipText("Play");
        pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))) );

        addButtonByNameAndCallback(toolbar, "+Floor", (e)->{
            ODE4JHelper.guaranteePhysicsWorld();
            Registry.getScene().addChild(new ODEPlane());
        });

        addButtonByNameAndCallback(toolbar, "+Sphere", (e)->{
            ODE4JHelper.guaranteePhysicsWorld();
            Registry.getScene().addChild(new ODESphere());
        });

        addButtonByNameAndCallback(toolbar, "+Box", (e)->{
            ODE4JHelper.guaranteePhysicsWorld();
            Registry.getScene().addChild(new ODEBox());
        });

        addButtonByNameAndCallback(toolbar, "+Cylinder", (e)->{
            ODE4JHelper.guaranteePhysicsWorld();
            Registry.getScene().addChild(new ODECylinder());
        });

        addButtonByNameAndCallback(toolbar, "+Capsule", (e)->{
            ODE4JHelper.guaranteePhysicsWorld();
            Registry.getScene().addChild(new ODECapsule());
        });
    }

    private JButton addButtonByNameAndCallback(JToolBar toolbar, String title, ActionListener actionListener) {
        JButton button = new JButton(title);
        button.addActionListener(actionListener);
        toolbar.add(button);
        return button;
    }

    private void updatePauseButton(JButton pauseButton, ODEWorldSpace worldSpace) {
        if (worldSpace.isPaused()) {
            pauseButton.setToolTipText("Unpause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))));
        } else {
            pauseButton.setToolTipText("Pause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-pause-16.png"))));
        }
    }
}
