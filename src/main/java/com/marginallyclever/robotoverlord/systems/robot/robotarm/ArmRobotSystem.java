package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.components.ProgramComponent;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.GCodePathComponent;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.robotpanel.RobotPanel;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;

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
        if( component instanceof ProgramComponent ) decorateProgram(view,component);
    }

    private void decorateProgram(ComponentPanelFactory view, Component component) {
        final ProgramComponent program = (ProgramComponent)component;
        view.add(program.programEntity).addPropertyChangeListener((evt) -> {
            program.isRunning.set(false);
            program.stepEntity.set((String)null);
        });
        view.add(program.stepEntity).setReadOnly(true);

        ViewElementButton bRun = view.addButton(program.isRunning.get()?"Pause":"Play");
        program.isRunning.addPropertyChangeListener((evt) -> {
            bRun.setText(program.isRunning.get()?"Pause":"Play");
        });
        bRun.addActionEventListener((evt) -> {
            program.isRunning.set(!program.isRunning.get());
        });
        view.addComboBox(program.mode, ProgramComponent.MODE_NAMES);
    }

    private void decorateDH(ComponentPanelFactory view, Component component) {
        DHComponent dh = (DHComponent)component;
        view.add(dh.isRevolute).setReadOnly(true);
        view.add(dh.myD).setReadOnly(true);
        view.add(dh.myR).setReadOnly(true);
        view.add(dh.alpha).setReadOnly(true);
        view.add(dh.theta).setReadOnly(true);
        view.add(dh.jointMax).setReadOnly(true);
        view.add(dh.jointMin).setReadOnly(true);
        view.add(dh.jointHome).setReadOnly(true);
    }

    private void decorateRobot(ComponentPanelFactory view, Component component) {
        RobotComponent robotComponent = (RobotComponent)component;

        view.add(robotComponent.gcodePath);

        robotComponent.findBones();

        ViewElementButton bMake = view.addButton("Edit Arm");
        bMake.addActionEventListener((evt)-> makeRobotArm6(bMake,robotComponent,"Edit Arm"));

        ViewElementButton bOpenJog = view.addButton(Translator.get("RobotROSystem.controlPanel"));
        bOpenJog.addActionEventListener((evt)-> showControlPanel(bOpenJog,robotComponent));

        ViewElementButton bOpenProgram = view.addButton(Translator.get("RobotROSystem.controlPanel"));
        bOpenProgram.addActionEventListener((evt)-> showControlPanel(bOpenProgram,robotComponent));

        ViewElementButton bHome = view.addButton("Go home");
        bHome.addActionEventListener((evt)-> robotComponent.goHome());
    }

    private void makeRobotArm6(JComponent parent, RobotComponent robotComponent,String title) {
        EntitySystemUtils.makePanel(new EditArmPanel(robotComponent.getEntity(), entityManager), parent,title);
    }

    private void showControlPanel(JComponent parent,RobotComponent robotComponent) {
        EntitySystemUtils.makePanel(new RobotPanel(robotComponent,getGCodePath(robotComponent)), parent,Translator.get("RobotROSystem.controlPanel"));
    }

    private GCodePathComponent getGCodePath(RobotComponent robotComponent) {
        Entity entity = entityManager.findEntityByUniqueID(robotComponent.getGCodePathEntityUUID());
        if(entity==null) return null;
        return entity.getComponent(GCodePathComponent.class);
    }

    /**
     * Update the system over time.
     * @param dt the time step in seconds.
     */
    public void update(double dt) {}
}
