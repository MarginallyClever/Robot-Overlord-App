package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import java.util.List;

public class SpaceShip extends Pose {
    public final Vector3d velocity = new Vector3d();
    public final Vector3d acceleration = new Vector3d();
    public final Vector3d angularVelocity = new Vector3d();
    public final Vector3d torque = new Vector3d();

    public SpaceShip() {
        this("SpaceShip");

        var mesh = new MeshInstance();
        this.addChild(mesh);
        mesh.setMesh(Registry.meshFactory.load("C:/Users/aggra/Desktop/RO3 test scenes/serenity.obj"));
        var mat = new Material();
        this.addChild(mat);
    }

    public SpaceShip(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new SpaceShipPanel(this));
        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        var m = getWorld();
        var p = MatrixHelper.getPosition(m);
        var rot = new Matrix3d();
        m.get(rot);

        // apply linear acceleration
        velocity.scaleAdd(dt, acceleration, velocity);
        // apply linear velocity
        p.scaleAdd(dt, velocity, p);

        // apply angular acceleration
        angularVelocity.scaleAdd(dt, torque, angularVelocity);
        // apply angular velocity
        var rotDelta = new Matrix3d();
        rotDelta.rotX(angularVelocity.x*dt);        rot.mul(rotDelta);
        rotDelta.rotY(angularVelocity.y*dt);        rot.mul(rotDelta);
        rotDelta.rotZ(angularVelocity.z*dt);        rot.mul(rotDelta);

        //build a new matrix4d from rotation and position.
        m.set(rot, p, 1);
        setWorld(m);

        // reset forces
        torque.set(0,0,0);
        acceleration.set(0,0,0);
    }
}
