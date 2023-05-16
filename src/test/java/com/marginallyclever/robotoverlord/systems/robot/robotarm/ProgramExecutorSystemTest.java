package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.components.ProgramComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Test;

public class ProgramExecutorSystemTest {
    @Test
    public void testWalkATree() {
        EntityManager entityManager = new EntityManager();
        // build a program tree
        Entity programRoot = new Entity("Program");
        entityManager.addEntityToParent(programRoot,entityManager.getRoot());
        addChildrenToNode(entityManager,programRoot,10);
        programRoot.getChildren().forEach((child)->{
            addChildrenToNode(entityManager,child,3);
        });
        addChildrenToNode(entityManager,programRoot.getChildren().get(0).getChildren().get(0),3);

        // add a robot
        Entity myRobot = new Entity("My Robot");
        entityManager.addEntityToParent(myRobot,entityManager.getRoot());
        RobotComponent robot = new RobotComponent();
        ProgramComponent program = new ProgramComponent();
        myRobot.addComponent(robot);
        myRobot.addComponent(program);
        // assign program to robot
        program.programEntity.set(programRoot.getUniqueID());

        // run the program
        ProgramExecutorSystem executor = new ProgramExecutorSystem(entityManager);
        Entity programStep = programRoot.getChildren().get(0);
        for(int i=0;i<100;++i) {
            System.out.print("Step "+i);
            if(programStep!=null) {
                System.out.println(" "+programStep.getName());
            } else {
                System.out.println(" null");
            }
            Entity nextStep = executor.getNextStep(programStep,programRoot);
            if(nextStep==null) break;
            programStep = nextStep;
        }
    }

    private void addChildrenToNode(EntityManager manager,Entity node,int count) {
        for(int i=0;i<count;++i) {
            manager.addEntityToParent(new Entity(node.getName()+"-" + i), node);
        }
    }
}
