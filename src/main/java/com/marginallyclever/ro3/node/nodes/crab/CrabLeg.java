package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Internal description of each leg for calculating kinematics.
 */
public class CrabLeg {
    private final Crab crab;
    public Pose coxa = new Pose();
    public Pose femur = new Pose();
    public Pose tibia = new Pose();
    public Pose toe = new Pose();
    public Pose targetPosition = new Pose();

    public boolean flipCoxa = false;
    public double coxaAdjust = 0;

    public double angleCoxa = 0;
    public double angleFemur = 0;
    public double angleTibia = 0;

    public final Vector3d pointOfContactLast = new Vector3d();
    public final Vector3d pointOfContactNext = new Vector3d();

    public CrabLeg(Crab crab,String legName) {
        this.crab = crab;

        coxa.setName(legName + " coxa");
        femur.setName("femur");
        tibia.setName("tibia");
        toe.setName("toe");
        targetPosition.setName(legName+" targetPosition");

        coxa.addChild(femur);
        femur.addChild(tibia);
        tibia.addChild(toe);
        crab.addChild(targetPosition);

        femur.setPosition(new Vector3d(Crab.COXA, 0, 0));
        tibia.setPosition(new Vector3d(Crab.FEMUR, 0, 0));
        toe.setPosition(new Vector3d(Crab.TIBIA, 0, 0));

        Crab.addMeshAndMaterial(coxa);
        Crab.addMeshAndMaterial(femur);
        Crab.addMeshAndMaterial(tibia);
        Crab.addMeshAndMaterial(toe);
    }

    // set the FK for one leg
    public void setAngles(double coxa, double femur, double tibia) {
        // turn coxa
        {
            var m3 = this.coxa.getLocal();
            var p = new Vector3d();
            m3.get(p);
            m3.rotZ(Math.toRadians(coxa));
            m3.setTranslation(p);
            this.coxa.setLocal(m3);
            this.angleCoxa = coxa;
        }
        // turn femur
        {
            var m3 = this.femur.getLocal();
            var p = new Vector3d();
            m3.get(p);
            m3.rotY(Math.toRadians(femur+90));
            m3.setTranslation(p);
            this.femur.setLocal(m3);
            this.angleFemur = femur;
        }
        // turn tibia
        {
            var m3 = this.tibia.getLocal();
            var p = new Vector3d();
            m3.get(p);
            m3.rotY(Math.toRadians(tibia));
            m3.setTranslation(p);
            this.tibia.setLocal(m3);
            this.angleTibia = tibia;
        }
    }
}
