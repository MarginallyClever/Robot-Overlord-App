package com.marginallyclever.robotoverlord.components.demo;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Vector3d;

public class CrabRobotComponent extends Component {
    private final String[] modeNames = {
            "Calibrate",
            "Sit down",
            "Stand up",
            "Only body ",
            "Ripple",
            "Wave",
            "Tripod",
    };
    private final IntEntity modeSelector = new IntEntity("Mode", 0);

    public CrabRobotComponent() {
        super();

        createMesh(getEntity(),"/Spidee/body.stl");

        getEntity().addEntity(positionLimb(createLimb("RF",true ),   45));
        getEntity().addEntity(positionLimb(createLimb("RM",true ),   0));
        getEntity().addEntity(positionLimb(createLimb("RB",true ),  -45));
        getEntity().addEntity(positionLimb(createLimb("LF",false),  135));
        getEntity().addEntity(positionLimb(createLimb("LM",false),  180));
        getEntity().addEntity(positionLimb(createLimb("LB",false), -135));
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.addComboBox(modeSelector, modeNames);
    }

    @Override
    public void update(double dt) {
    }

    private void updateGaitForOneLeg() {

    }

    private Entity positionLimb(Entity limb, float degrees) {
        PoseComponent pose = limb.findFirstComponent(PoseComponent.class);
        double r = Math.toRadians(degrees);
        pose.setPosition(new Vector3d(Math.cos(r)*10,Math.sin(r)*10,2.6));
        pose.setRotation(new Vector3d(0,0,degrees));
        return limb;
    }

    private Entity createLimb(String name,boolean isRight) {
        DHComponent[] dh = new DHComponent[3];
        for(int i=0;i<dh.length;++i) {
            dh[i] = new DHComponent();
            dh[i].setVisible(true);
        }
        Entity limb = new Entity(name);
        limb.addComponent(new PoseComponent());
        limb.addComponent(new RobotComponent());

        Entity hip = new Entity("Hip");
        limb.addEntity(hip);
        Entity thigh = new Entity("Thigh");
        hip.addEntity(thigh);
        Entity calf = new Entity("Calf");
        thigh.addEntity(calf);
        Entity foot = new Entity("Foot");
        calf.addEntity(foot);

        hip.addComponent(dh[0]);
        dh[0].set(0,2.2,90,0,30,-30);
        if(isRight) createMesh(hip,"/Spidee/shoulder_right.obj");
        else        createMesh(hip,"/Spidee/shoulder_left.obj");

        thigh.addComponent(dh[1]);
        dh[1].set( 0,8.5,0,0,120,-120);
        createMesh(thigh,"/Spidee/thigh.obj");

        calf.addComponent(dh[2]);
        dh[2].set(0,10.5,0,0,120,-120);
        if(isRight) createMesh(calf,"/Spidee/calf_right.obj");
        else		createMesh(calf,"/Spidee/calf_left.obj");

        foot.addComponent(new PoseComponent());
        foot.addComponent(new ArmEndEffectorComponent());

        return limb;
    }

    private void createMesh(Entity parent,String filename) {
        MeshFromFile mff = new MeshFromFile();
        mff.setFilename(filename);

        Entity mesh = new Entity("Mesh");
        mesh.addComponent(new PoseComponent());
        mesh.addComponent(new MaterialComponent());
        mesh.addComponent(mff);

        parent.addEntity(mesh);

        OriginAdjustComponent oac = new OriginAdjustComponent();
        mesh.addComponent(oac);
        oac.adjust();
        mesh.removeComponent(oac);
    }
}
