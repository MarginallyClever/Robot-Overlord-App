package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.shapes.Decal;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.Objects;

/**
 * Wrapper for a ODE4J Plane.
 */
public class ODEPlane extends ODENode {
    private DPlane plane;
    private final Decal decal = new Decal();

    public ODEPlane() {
        this("ODE Plane");
    }

    public ODEPlane(String name) {
        super(name);
        // setup decal
        decal.width = 1000;
        decal.height = 1000;
        decal.wParts = 20;
        decal.hParts = 20;
        decal.textureScale = 10;
        decal.updateModel();
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        plane = OdeHelper.createPlane(physics.getODESpace(), 0, 0, 1, 0);

        MeshInstance mesh = findFirstChild(MeshInstance.class);
        if(mesh==null) {
            mesh = new MeshInstance();
            addChild(mesh);
        }
        mesh.setMesh(decal);

        Material material = findFirstChild(Material.class);
        if(material==null) {
            material = new Material();
            addChild(material);
        }
        material.setTexture(Registry.textureFactory.load("/com/marginallyclever/ro3/shared/checkerboard.png"));

        updatePhysicsFromPose();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        destroyPlane();
    }

    private void destroyPlane() {
        if(plane!=null) {
            plane.destroy();
            plane = null;
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        // adjust the position of the Node to match the body.
        if(plane == null) return;

        var odeNormal = plane.getNormal();
        double distance = plane.getDepth();

        Vector3d normal = new Vector3d(odeNormal.get0(), odeNormal.get1(), odeNormal.get2());
        Matrix3d m3 = MatrixHelper.getMatrixFromAxisAndRotation(normal,0);
        Matrix4d m4 = new Matrix4d();
        m4.set(m3);
        // set translation along normal
        normal.scale(distance);
        m4.setTranslation(normal);
        super.setWorld(m4);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/ode4j/icons8-mechanics-16.png")));
    }

    @Override
    public JSONObject toJSON() {
        return super.toJSON();
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        updatePhysicsFromPose();
    }

    protected void updatePhysicsFromPose() {
        if (plane == null) return;

        var world = getWorld();
        Vector3d normal = MatrixHelper.getZAxis(world);
        Vector3d position = MatrixHelper.getPosition(world);
        double depth = normal.dot(position);
        plane.setParams(normal.x, normal.y, normal.z, depth);
    }
}
