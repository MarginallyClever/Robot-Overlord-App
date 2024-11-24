package com.marginallyclever.ro3.node.nodes.pose.poses.space;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Objects;

public class SpaceShip extends Pose {
    public final Vector3d linearVelocity = new Vector3d();
    public final Vector3d acceleration = new Vector3d();
    public final Vector3d angularVelocity = new Vector3d();
    public final Vector3d torque = new Vector3d();
    public double deltaV=0;

    public SpaceShip() {
        this("Spaceship");

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
        rot.setIdentity();
        m.get(rot);

        // apply linear acceleration
        linearVelocity.scaleAdd(acceleration.x*dt, MatrixHelper.getXAxis(m), linearVelocity);
        linearVelocity.scaleAdd(acceleration.y*dt, MatrixHelper.getYAxis(m), linearVelocity);
        linearVelocity.scaleAdd(acceleration.z*dt, MatrixHelper.getZAxis(m), linearVelocity);
        // apply linear velocity
        p.scaleAdd(dt, linearVelocity, p);
        // track deltaV
        if(acceleration.lengthSquared()>0) {
            deltaV += acceleration.length() * dt;
            System.out.println("deltaV="+deltaV);
        }

        // apply angular acceleration
        angularVelocity.scaleAdd(dt, torque, angularVelocity);
        // apply angular velocity
        var rotDelta = new Matrix3d();
        rotDelta.rotX(angularVelocity.x*dt);        rot.mul(rotDelta);
        rotDelta.rotY(angularVelocity.y*dt);        rot.mul(rotDelta);
        rotDelta.rotZ(angularVelocity.z*dt);        rot.mul(rotDelta);

        //build a new Matrix4d from rotation and position.
        m.set(rot, p, 1);
        setWorld(m);

        // reset forces
        torque.set(0,0,0);
        acceleration.set(0,0,0);
    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON();
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-spaceship-16.png")));
    }
}
