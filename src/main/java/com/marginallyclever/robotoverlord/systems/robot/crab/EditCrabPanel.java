package com.marginallyclever.robotoverlord.systems.robot.crab;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Cylinder;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;

/**
 * This panel is used to edit the dog robot.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class EditCrabPanel extends JPanel {
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final CrabRobotComponent crab;
    private final RobotComponent[] legs = new RobotComponent[6];

    public EditCrabPanel(Entity rootEntity, EntityManager entityManager) {
        super(new BorderLayout());
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        this.crab = rootEntity.getComponent(CrabRobotComponent.class);
        createComponents();
        setupPanel();
    }

    private boolean firstChildHasNoMesh(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()==0) return true;
        Entity firstChild = children.get(0);
        MeshFromFile mesh = firstChild.getComponent(MeshFromFile.class);
        return mesh == null;
    }

    private boolean secondChildHasDHComponent(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()<2) return false;
        Entity secondChild = children.get(1);
        DHComponent dh = secondChild.getComponent(DHComponent.class);
        return dh!=null;
    }

    private void createComponents() {
        PoseComponent myPose = rootEntity.getComponent(PoseComponent.class);
        //myPose.setPosition(new Vector3d(0, 0, 5.4));
        //myPose.setRotation(new Vector3d(90, 0, 0));

        // torso
        if (firstChildHasNoMesh(rootEntity)) {
            // Add Entity with MeshFromFile for the torso
            Entity mesh = createMesh("/robots/Spidee/body.stl", new ColorRGB(0x3333FF));
            entityManager.addEntityToParent(mesh, rootEntity);
            PoseComponent meshPose = mesh.getComponent(PoseComponent.class);
        }

        if (rootEntity.getChildren().size() != 7) {
            buildLegs();
        } else {
            legs[0] = rootEntity.getChildren().get(1).getComponent(RobotComponent.class);
            legs[1] = rootEntity.getChildren().get(2).getComponent(RobotComponent.class);
            legs[2] = rootEntity.getChildren().get(3).getComponent(RobotComponent.class);
            legs[3] = rootEntity.getChildren().get(4).getComponent(RobotComponent.class);
            legs[4] = rootEntity.getChildren().get(5).getComponent(RobotComponent.class);
            legs[5] = rootEntity.getChildren().get(6).getComponent(RobotComponent.class);
        }
    }

    private void buildLegs() {
        // 0   5
        // 1 x 4
        // 2   3
        legs[0] = createLimb(entityManager,crab,"LF",0,false,  135);
        legs[1] = createLimb(entityManager,crab,"LM",1,false,  180);
        legs[2] = createLimb(entityManager,crab,"LB",2,false, -135);
        legs[3] = createLimb(entityManager,crab,"RB",3,true,   -45);
        legs[4] = createLimb(entityManager,crab,"RM",4,true,     0);
        legs[5] = createLimb(entityManager,crab,"RF",5,true,    45);

        int i=0;
        for(RobotComponent leg : legs) {
            crab.setLeg(i,leg);
            entityManager.addEntityToParent(leg.getEntity(),rootEntity);
            crab.setInitialPointOfContact(leg.getEntity(),i);
            i++;
        }
    }

    private RobotComponent createLimb(EntityManager entityManager, CrabRobotComponent crab, String name, int index, boolean isRight, float degrees) {
        DHComponent[] dh = new DHComponent[3];
        for(int i=0;i<dh.length;++i) {
            dh[i] = new DHComponent();
            dh[i].setVisible(false);
        }
        Entity limb = new Entity(name);
        limb.addComponent(new PoseComponent());

        Entity hip = new Entity(CrabRobotComponent.HIP);
        entityManager.addEntityToParent(hip,limb);
        if(isRight) entityManager.addEntityToParent(createMesh("/robots/Spidee/shoulder_right.obj",new ColorRGB(0x9999FF)),hip);
        else        entityManager.addEntityToParent(createMesh("/robots/Spidee/shoulder_left.obj",new ColorRGB(0x9999FF)),hip);
        dh[0].set(0,2.2,90,0,60,-60,true);
        hip.addComponent(dh[0]);

        Entity thigh = new Entity(CrabRobotComponent.THIGH);
        entityManager.addEntityToParent(thigh,hip);
        entityManager.addEntityToParent(createMesh("/robots/Spidee/thigh.obj",new ColorRGB(0xFFFFFF)),thigh);
        dh[1].set( 0,8.5,0,0,106,-72,true);
        thigh.addComponent(dh[1]);

        Entity calf = new Entity(CrabRobotComponent.CALF);
        entityManager.addEntityToParent(calf,thigh);
        if(isRight) entityManager.addEntityToParent(createMesh("/robots/Spidee/calf_right.obj",new ColorRGB(0xFFFF99)),calf);
        else		entityManager.addEntityToParent(createMesh("/robots/Spidee/calf_left.obj",new ColorRGB(0xFFFF99)),calf);
        dh[2].set(0,10.5,0,0,15,-160,true);
        calf.addComponent(dh[2]);

        Entity foot = new Entity(CrabRobotComponent.FOOT);
        entityManager.addEntityToParent(foot,calf);
        foot.addComponent(new ArmEndEffectorComponent());

        // set pose
        PoseComponent limbPose = limb.getComponent(PoseComponent.class);
        double r = Math.toRadians(degrees);
        limbPose.setPosition(new Vector3d(Math.cos(r)*10,Math.sin(r)*10,2.6));
        limbPose.setRotation(new Vector3d(0,0,degrees));

        // Done at the end so RobotComponent can find all bones DHComponents.
        RobotComponent robot = new RobotComponent();
        limb.addComponent(robot);

        crab.setInitialPointOfContact(limb,index);

        return robot;
    }

    private Entity createMesh(String filename, ColorRGB color) {
        Entity mesh = new Entity("Mesh");

        mesh.addComponent(new MeshFromFile(filename));

        MaterialComponent mc = mesh.getComponent(MaterialComponent.class);
        mc.setDiffuseColor(color.red/255.0,color.green/255.0,color.blue/255.0,1);

        OriginAdjustSystem.adjustOne(mesh);

        return mesh;
    }

    private void setupPanel() {
    }
}
