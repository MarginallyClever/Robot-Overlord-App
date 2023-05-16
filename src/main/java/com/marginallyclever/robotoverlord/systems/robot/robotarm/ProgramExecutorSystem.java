package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.systems.EntitySystem;

import java.util.LinkedList;
import java.util.List;

public class ProgramExecutorSystem  implements EntitySystem {
    private final EntityManager entityManager;

    public ProgramExecutorSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if( component instanceof ProgramComponent) decorateProgram(view,component);
    }

    private void decorateProgram(ComponentPanelFactory view, Component component) {
        final ProgramComponent program = (ProgramComponent)component;
        view.add(program.programEntity).addPropertyChangeListener((evt) -> {
            program.setRunning(false);
            program.stepEntity.set((String)null);
        });
        view.add(program.stepEntity).setReadOnly(true);

        ViewElementButton bRun = view.addButton(program.getRunning()?"Pause":"Play");
        program.addRunningPropertyChangeListener( (evt) -> bRun.setText(program.getRunning()?"Pause":"Play") );
        bRun.addActionEventListener( (evt) -> program.setRunning(!program.getRunning()) );
        view.addComboBox(program.mode, ProgramComponent.MODE_NAMES);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while(!list.isEmpty()) {
            Entity e = list.remove(0);
            ProgramComponent found = e.getComponent(ProgramComponent.class);
            if( found!=null ) updateProgram(found,dt);
            list.addAll(e.getChildren());
        }
    }

    private void updateProgram(ProgramComponent program, double dt) {
        if( !program.getRunning() ) return;

        // ProgramComponent must be adjacent to a RobotComponent.
        RobotComponent robot = program.getEntity().getComponent(RobotComponent.class);
        if( robot == null ) return;

        // must have program to run.
        Entity programRoot = entityManager.findEntityByUniqueID(program.programEntity.get());
        if(programRoot==null) return;

        Entity programStep = getProgramStep(program,programRoot);
        if(programStep==null) return;
        // TODO check if programStep is a child of programRoot?

        int mode = program.mode.get();

        boolean done = executeStep(robot,program,programStep,dt);
        if(done) {
            endStep(programStep);

            Entity nextStep = getNextStep(programStep,programRoot);
            if(nextStep==null) {
                // no more steps to run.
                if (mode == ProgramComponent.RUN_LOOP) {
                    // go back to start
                    nextStep = programRoot.getChildren().get(0);
                } else if(mode == ProgramComponent.RUN_TO_END) {
                    program.setRunning(false);
                    return;
                }
            }
            program.stepEntity.set(nextStep.getUniqueID());
            beginStep(nextStep);
        }

        if( mode == ProgramComponent.RUN_STEP ) {
            // step mode always stops after one step.
            program.setRunning(false);
        }
    }

    /**
     * Get the current step in the program.
     * @param program the program to run.
     * @param programRoot the root of the program tree.
     * @return the current step in the program or null.
     */
    private Entity getProgramStep(ProgramComponent program,Entity programRoot) {
        // find step to run.  if no step, assume program start.
        Entity programStep = entityManager.findEntityByUniqueID(program.stepEntity.get());
        if(programStep==null) {
            List<Entity> children = programRoot.getChildren();
            if(children.isEmpty()) {
                // program is empty.
                program.setRunning(false);
                return null;
            }
            // get first child.
            programStep = programRoot.getChildren().get(0);
            beginStep(programStep);
        }
        return programStep;
    }

    private void beginStep(Entity programStep) {
    }

    private void endStep(Entity programStep) {
    }

    /**
     * a program is a tree of entities.  walk the tree one step.
     * @param programStep the current step in the program tree.
     * @param programRoot the root of the program tree
     * @return the next step in the program or null.
     */
    public Entity getNextStep(Entity programStep, Entity programRoot) {
        // we've just executed programStep, now what?
        if(!programStep.getChildren().isEmpty()) {
            // push the stack.
            return programStep.getChildren().get(0);
        }

        // no children, go to next sibling
        Entity nextStep = programStep.getNextSibling();
        if (nextStep != null) return nextStep;

        // no next sibling, pop the stack
        while(true) {
            Entity parent = programStep.getParent();

            if( parent==null ) return null;
            if( parent == programRoot ) return null;

            // go to parent's next sibling
            nextStep = parent.getNextSibling();
            if (nextStep != null) {
                return nextStep;
            }

            // parent has no next sibling, keep poppin'
            programStep = parent;
        }
    }

    /**
     * Execute the current step in the program.
     * @param robot the robot to move.
     * @param program the program that owns the step.
     * @param programStep the step to execute.
     * @param dt the time step in seconds.
     * @return true if the step is finished.
     */
    private boolean executeStep(RobotComponent robot, ProgramComponent program, Entity programStep, double dt) {
        ProgramEventComponent event = programStep.getComponent(ProgramEventComponent.class);
        if(event!=null) return executeEvent(robot,program,programStep,event,dt);

        ProgramPathComponent path = programStep.getComponent(ProgramPathComponent.class);
        if(path!=null) return executePath(robot, program, path, dt);

        // TODO handle unrecognized component type.
        return true;
    }

    private boolean executeEvent(RobotComponent robot, ProgramComponent program, Entity programStep, ProgramEventComponent event, double dt) {
        // assume instant finish
        return true;
    }

    /**
     * A {@link ProgramPathComponent} is made of a series of {@link PoseComponent}s.
     * @param robot the robot to move.
     * @param program the program that owns the path.
     * @param path the path to move.
     * @param dt the time step in seconds.
     * @return true if the path is finished.
     */
    private boolean executePath(RobotComponent robot, ProgramComponent program, ProgramPathComponent path, double dt) {
        PoseComponent pathPose = path.getEntity().getComponent(PoseComponent.class);

        if (path.moveType.get() == ProgramPathComponent.MOVE_LINEAR) {
            // linear
            robot.set(Robot.END_EFFECTOR_TARGET, pathPose.getWorld());
        } else {
            // rapid
            robot.set(Robot.END_EFFECTOR_TARGET, pathPose.getWorld());
        }

        // assume instant finish
        return true;
    }
}
