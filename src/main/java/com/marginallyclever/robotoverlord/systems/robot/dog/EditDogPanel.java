package com.marginallyclever.robotoverlord.systems.robot.dog;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Cylinder;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;

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
public class EditDogPanel extends JPanel {
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final DogRobotSystem system;
    private final DogRobotComponent dog;
    private final RobotComponent[] legs = new RobotComponent[4];

    public EditDogPanel(Entity rootEntity, EntityManager entityManager,DogRobotSystem system) {
        super(new BorderLayout());
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        this.system = system;
        this.dog = rootEntity.getComponent(DogRobotComponent.class);
        createComponents();
        setupPanel();
    }

    private boolean firstChildHasNoMesh(Entity entity) {
        java.util.List<Entity> children = entity.getChildren();
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
        myPose.setPosition(new Vector3d(0, 0, 5.4));
        myPose.setRotation(new Vector3d(90, 0, 0));

        // torso
        if (firstChildHasNoMesh(rootEntity)) {
            // Add Entity with MeshFromFile for the torso
            Entity mesh = createMesh("/robots/SpotMicro/torso.obj", new ColorRGB(0xffffff));
            entityManager.addEntityToParent(mesh, rootEntity);
            PoseComponent meshPose = mesh.getComponent(PoseComponent.class);
            meshPose.setRotation(new Vector3d(90, 180, 180));
            meshPose.setPosition(new Vector3d(-0.7, 4.1, 7));
        }

        if (rootEntity.getChildren().size() != 5) {
            buildLegs();
        } else {
            legs[0] = rootEntity.getChildren().get(1).getComponent(RobotComponent.class);
            legs[1] = rootEntity.getChildren().get(2).getComponent(RobotComponent.class);
            legs[2] = rootEntity.getChildren().get(3).getComponent(RobotComponent.class);
            legs[3] = rootEntity.getChildren().get(4).getComponent(RobotComponent.class);
        }
    }

    private void buildLegs() {
        // head
        // 0   2
        // 1   1
        double w = DogRobotComponent.KINEMATIC_BODY_WIDTH/2;
        double h = DogRobotComponent.KINEMATIC_BODY_HEIGHT/2;
        int i=0;
        legs[i] = createLimb(entityManager,dog,"RF",i, true, -w, h, 1);  i++;
        legs[i] = createLimb(entityManager,dog,"RB",i, true, -w,-h, 1);  i++;
        legs[i] = createLimb(entityManager,dog,"LF",i,false,  w, h, 1);  i++;
        legs[i] = createLimb(entityManager,dog,"LB",i,false,  w,-h, 1);  i++;

        i=0;
        for(RobotComponent leg : legs) {
            dog.setLeg(i,leg);
            entityManager.addEntityToParent(leg.getEntity(),rootEntity);
            system.setInitialPointOfContact(dog,leg.getEntity(),i);
            i++;
        }
    }

    private RobotComponent createLimb(EntityManager entityManager, DogRobotComponent dog, String name, int index, boolean isRight, double r, double d, double s) {
        DHComponent[] dh = new DHComponent[4];
        for(int i=0;i<dh.length;++i) {
            dh[i] = new DHComponent();
            dh[i].setVisible(true);
        }
        Entity limb = new Entity(name);
        limb.addComponent(new PoseComponent());
        PoseComponent limbPose = limb.getComponent(PoseComponent.class);
        limbPose.setPosition(new Vector3d(r,0,d));

        entityManager.addEntityToParent(createCylinder(4,2.1,new ColorRGB(0x9999FF)),limb);

        Entity hip = new Entity(DogRobotComponent.HIP);
        entityManager.addEntityToParent(hip,limb);
        dh[0].set( 0, 0, 90*(isRight?1:-1), 90, 360, -360,true);
        entityManager.addEntityToParent(createCylinder(5,2,new ColorRGB(0xFFFFFF)),hip);
        hip.addComponent(dh[0]);

        Entity thigh = new Entity(DogRobotComponent.THIGH);
        entityManager.addEntityToParent(thigh,hip);
        dh[1].set(-3.5 * s, 11.5, 0, 135*(isRight?-1:1), 360, -360,true);
        entityManager.addEntityToParent(createBox(dh[1].getR(),1,new ColorRGB(0xFFFF99)),thigh);
        thigh.addComponent(dh[1]);

        Entity calf = new Entity(DogRobotComponent.CALF);
        entityManager.addEntityToParent(calf,thigh);
        dh[2].set(0, 13, 0, 90*(isRight?-1:1), 360, -360,true);
        entityManager.addEntityToParent(createBox(dh[2].getR(),0.7,new ColorRGB(0xFFFF66)),calf);
        calf.addComponent(dh[2]);

        Entity foot = new Entity(DogRobotComponent.FOOT);
        entityManager.addEntityToParent(foot,calf);
        foot.addComponent(new ArmEndEffectorComponent());

        // Done at the end so RobotComponent can find all bones DHComponents.
        RobotComponent robot = new RobotComponent();
        limb.addComponent(robot);

        system.setInitialPointOfContact(dog,limb,index);

        return robot;
    }

    private Entity createBox(double r, double v,ColorRGB color) {
        Entity result = new Entity("Mesh");

        PoseComponent pose = new PoseComponent();
        result.addComponent(pose);
        pose.setPosition(new Vector3d(-r/2,0,0));
        pose.setScale(new Vector3d(r,v*2,v*2));

        MaterialComponent material = new MaterialComponent();
        result.addComponent(material);
        material.setDiffuseColor(color.red/255.0f, color.green/255.0f, color.blue/255.0f,1.0);

        result.addComponent(new Box());

        return result;
    }

    private Entity createCylinder(double r,double v,ColorRGB color) {
        Entity result = new Entity("Mesh");

        PoseComponent pose = new PoseComponent();
        result.addComponent(pose);
        pose.setScale(new Vector3d(v*2,v*2,r));

        MaterialComponent material = new MaterialComponent();
        result.addComponent(material);
        material.setDiffuseColor(color.red/255.0f, color.green/255.0f, color.blue/255.0f,1.0);

        result.addComponent(new Cylinder());

        return result;
    }

    private Entity createMesh(String filename, ColorRGB color) {
        Entity mesh = new Entity("Mesh");

        MaterialComponent mc = new MaterialComponent();
        mc.setDiffuseColor(color.red/255.0,color.green/255.0,color.blue/255.0,1);
        mesh.addComponent(mc);

        MeshFromFile mff = new MeshFromFile();
        mff.setFilename(filename);
        mesh.addComponent(mff);

        return mesh;
    }

    private void setupPanel() {
    }
}
