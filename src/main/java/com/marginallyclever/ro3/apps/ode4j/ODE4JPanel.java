package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.ode4j.*;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.*;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.physics.ODEPhysics;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * Actions in this window will control the contents of the {@link com.marginallyclever.ro3.Registry#scene}.
 */
public class ODE4JPanel extends App {
    boolean randomColor = true;
    boolean randomOrientation = false;

    public ODE4JPanel() {
        super(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        add(toolbar, BorderLayout.NORTH);

        JPanel container = new JPanel(new GridLayout(0,2));
        add(container, BorderLayout.CENTER);

        JButton pauseButton = addButtonByNameAndCallback(toolbar, "", (e)->{
            ODEPhysics physics = ODE4JHelper.guaranteePhysicsWorld();
            physics.setPaused(!physics.isPaused());
            updatePauseButton((JButton)e.getSource(), physics);
        });

        JToggleButton randomColorToggle = new JToggleButton("~Color");
        randomColorToggle.addActionListener(e->randomColor = !randomColor);
        toolbar.add(randomColorToggle);
        randomColorToggle.setSelected(randomColor);

        JToggleButton randomOrientationToggle = new JToggleButton("~Angle");
        randomOrientationToggle.addActionListener(e->randomOrientation = !randomOrientation);
        toolbar.add(randomOrientationToggle);
        randomColorToggle.setSelected(randomOrientation);

        // I cannot call this here because the physics world has not been created yet.
        // updatePauseButton(pauseButton, ODE4JHelper.guaranteePhysicsWorld());
        // The workaround is to manually set the button.  Not pretty but it will have to do.
        pauseButton.setToolTipText("Play");
        pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))) );

        addButtonByNameAndCallback(container, "+Floor", (e)-> Registry.getScene().addChild(new ODEPlane()) );
        addButtonByNameAndCallback(container, "+Sphere", (e)-> add(new ODESphere()) );
        addButtonByNameAndCallback(container, "+Box", (e)-> add(new ODEBox()) );
        addButtonByNameAndCallback(container, "+Cylinder", (e)-> add(new ODECylinder()) );
        addButtonByNameAndCallback(container, "+Capsule", (e)->add(new ODECapsule()) );
        addButtonByNameAndCallback(container, "+Hinge", (e)-> add(new ODEHinge()) );
        addButtonByNameAndCallback(container, "+Creature controller", (e)-> add(new CreatureController()) );
    }

    private void add(Node node) {
        if(node instanceof Pose body) {
            ODE4JHelper.guaranteePhysicsWorld();
            Registry.getScene().addChild(body);
            placeBodyAbovePlane(body);
            if(randomColor) giveRandomColor(body);
        } else {
            Registry.getScene().addChild(node);
        }
    }

    private void placeBodyAbovePlane(Pose body) {
        Matrix4d m = new Matrix4d();
        if(randomOrientation) {
            // set a random orientation
            double x = Math.random() * 180;
            double y = Math.random() * 180;
            double z = Math.random() * 180;
            m.set(MatrixHelper.eulerToMatrix(new Vector3d(x, y, z), MatrixHelper.EulerSequence.XYZ));
        } else {
            m.setIdentity();
        }

        // set above world
        m.setTranslation(new Vector3d(0, 0, 15));

        body.setWorld(m);
    }

    private void giveRandomColor(Pose body) {
        Material material = body.findFirstChild(Material.class);
        if(material!=null) {
            material.setDiffuseColor(new Color(
                    (int) (Math.random() * 255.0),
                    (int) (Math.random() * 255.0),
                    (int) (Math.random() * 255.0)));
        }
    }

    private JButton addButtonByNameAndCallback(JComponent parent, String title, ActionListener actionListener) {
        JButton button = new JButton(title);
        button.addActionListener(actionListener);
        parent.add(button);
        return button;
    }

    private void updatePauseButton(JButton pauseButton, ODEPhysics worldSpace) {
        if (worldSpace.isPaused()) {
            pauseButton.setToolTipText("Unpause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))));
        } else {
            pauseButton.setToolTipText("Pause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-pause-16.png"))));
        }
    }
}
