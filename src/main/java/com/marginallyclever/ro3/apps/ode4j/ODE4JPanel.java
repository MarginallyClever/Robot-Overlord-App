package com.marginallyclever.ro3.apps.ode4j;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.odenode.*;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODEBox;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODECapsule;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODECylinder;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODESphere;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.physics.ODEPhysics;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * Actions in this window will control the contents of the {@link com.marginallyclever.ro3.Registry#scene}.
 */
public class ODE4JPanel extends App {
    private boolean randomColor = true;
    private boolean randomOrientation = false;
    private final JButton pauseButton;
    private final ODEPhysics physics;

    public ODE4JPanel() {
        super(new BorderLayout());
        setName("Physics");

        physics = Registry.getPhysics();

        pauseButton = addButtonByNameAndCallback("", (e)->{
            ODEPhysics physics = Registry.getPhysics();
            physics.setPaused(!physics.isPaused());
        });

        physics.addActionListener(e->{
            if(e.getActionCommand().equals("Physics Paused")) updatePauseButton();
            if(e.getActionCommand().equals("Physics Running")) updatePauseButton();
        });

        JToolBar toolbar = createToolBar();
        add(toolbar, BorderLayout.NORTH);

        JPanel container = new JPanel(new GridBagLayout());
        add(container, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth=2;

        container.add(addButtonByNameAndCallback("+Floor", (e)-> Registry.getScene().addChild(new ODEPlane()) ),gbc);
        gbc.gridy++;
        container.add(addButtonByNameAndCallback("+Sphere", (e)-> add(new ODESphere()) ),gbc);
        gbc.gridy++;
        container.add(addButtonByNameAndCallback("+Box", (e)-> add(new ODEBox()) ),gbc);
        gbc.gridy++;
        container.add(addButtonByNameAndCallback("+Cylinder", (e)-> add(new ODECylinder()) ),gbc);
        gbc.gridy++;
        container.add(addButtonByNameAndCallback("+Capsule", (e)->add(new ODECapsule()) ),gbc);
        gbc.gridy++;
        container.add(addButtonByNameAndCallback("+Hinge", (e)-> add(new ODEHinge()) ),gbc);
        gbc.gridy++;
        container.add(addButtonByNameAndCallback("+Creature controller", (e)-> add(new CreatureController()) ),gbc);
        gbc.gridy++;

        gbc.gridwidth=1;

        NumberFormatter formatter = NumberFormatHelper.getNumberFormatter();

        // cfm
        JFormattedTextField cfm = new JFormattedTextField(formatter);
        cfm.setValue(physics.getCFM());
        PanelHelper.addLabelAndComponent(container, "CFM",cfm,gbc);
        cfm.addPropertyChangeListener("value", evt -> setCFM((Double) evt.getNewValue()));
        gbc.gridy++;

        // erp
        JFormattedTextField erp = new JFormattedTextField(formatter);
        erp.setValue(physics.getERP());
        PanelHelper.addLabelAndComponent(container, "ERP",erp,gbc);
        erp.addPropertyChangeListener("value", evt -> setERP((Double) evt.getNewValue()));
        gbc.gridy++;

        // gravity
        JFormattedTextField gravity = new JFormattedTextField(formatter);
        gravity.setValue(physics.getGravity());
        PanelHelper.addLabelAndComponent(container, "Gravity",gravity,gbc);
        gravity.addPropertyChangeListener("value", evt ->setGravity((Double) evt.getNewValue()));
    }

    private JToolBar createToolBar() {
        var toolbar = new JToolBar();

        toolbar.add(pauseButton);
        // I want to call this next line, but the physics engine might not have been created yet.
        // updatePauseButton(pauseButton, ODE4JHelper.guaranteePhysicsWorld());

        // The workaround is to manually set the button.  Not pretty but it will have to do.
        pauseButton.setToolTipText("Play");
        pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))) );

        JToggleButton randomColorToggle = new JToggleButton("~Color");
        randomColorToggle.addActionListener(e->randomColor = !randomColor);
        toolbar.add(randomColorToggle);
        randomColorToggle.setSelected(randomColor);

        JToggleButton randomOrientationToggle = new JToggleButton("~Angle");
        randomOrientationToggle.addActionListener(e->randomOrientation = !randomOrientation);
        toolbar.add(randomOrientationToggle);
        randomColorToggle.setSelected(randomOrientation);

        return toolbar;
    }

    private void add(Node node) {
        if(node instanceof Pose body) {
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

    private JButton addButtonByNameAndCallback(String title, ActionListener actionListener) {
        JButton button = new JButton(title);
        button.addActionListener(actionListener);
        return button;
    }

    private void updatePauseButton() {
        if (physics.isPaused()) {
            pauseButton.setToolTipText("Unpause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-play-16.png"))));
        } else {
            pauseButton.setToolTipText("Pause");
            pauseButton.setIcon( new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/shared/icons8-pause-16.png"))));
        }
    }

    public void setCFM(double cfm) {
        physics.setCFM(cfm);
    }

    public void setERP(double erp) {
        physics.setERP(erp);
    }

    public void setGravity(double gravity) {
        physics.setGravity(gravity);
    }
}
