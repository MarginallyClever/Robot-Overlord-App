package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.GCodePathComponent;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.robotpanel.RobotPanel;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.*;

/**
 * A system to manage robot arms.
 *
 * @author Dan Royer
 * @since 2.5.5
 */
public class ArmRobotSystem implements EntitySystem {
    private final EntityManager entityManager;

    public ArmRobotSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if( component instanceof RobotComponent ) decorateRobot(view,component);
        if( component instanceof DHComponent ) decorateDH(view,component);
    }

    private void decorateDH(ComponentPanelFactory view, Component component) {
        DHComponent dh = (DHComponent)component;
        view.add(dh.isRevolute);
        view.add(dh.myD);
        view.add(dh.myR);
        view.add(dh.alpha);
        view.add(dh.theta);
        view.add(dh.jointMax);
        view.add(dh.jointMin);
        view.add(dh.jointHome);
    }

    private void decorateRobot(ComponentPanelFactory view, Component component) {
        RobotComponent robotComponent = (RobotComponent)component;

        view.add(robotComponent.gcodePath);

        robotComponent.findBones();

        ViewElementButton bMake = view.addButton("Edit Arm 6");
        bMake.addActionEventListener((evt)-> makeRobotArm6(bMake,robotComponent));

        ViewElementButton bOpen = view.addButton(Translator.get("RobotROSystem.controlPanel"));
        bOpen.addActionEventListener((evt)-> showControlPanel(bOpen,robotComponent));

        ViewElementButton bHome = view.addButton("Go home");
        bHome.addActionEventListener((evt)-> robotComponent.goHome());
    }

    private void makeRobotArm6(JComponent parent, RobotComponent robotComponent) {
        makePanel(new EditArm6Panel(robotComponent.getEntity(), entityManager), parent,"Make Arm 6");
    }

    private void makePanel(JPanel panel, JComponent parent,String title) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(parent);

        try {
            JDialog frame = new JDialog(parentFrame, title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setPreferredSize(new Dimension(700,300));
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(parentFrame);
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(parentFrame, ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showControlPanel(JComponent parent,RobotComponent robotComponent) {
        makePanel(new RobotPanel(robotComponent,getGCodePath(robotComponent)), parent,Translator.get("RobotROSystem.controlPanel"));
    }

    private GCodePathComponent getGCodePath(RobotComponent robotComponent) {
        String entityUniqueID = robotComponent.getGCodePathEntityUUID();
        if(entityUniqueID==null) return null;
        Entity entity = entityManager.findEntityByUniqueID(entityUniqueID);
        if(entity==null) return null;
        return entity.getComponent(GCodePathComponent.class);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}
}
