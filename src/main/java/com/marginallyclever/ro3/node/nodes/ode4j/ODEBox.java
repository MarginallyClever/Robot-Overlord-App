package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Box;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;

import javax.swing.*;
import javax.vecmath.Matrix4d;

import java.awt.*;
import java.util.Objects;

import static org.ode4j.ode.OdeHelper.*;

/**
 * Wrapper for a ODE4J Box.
 */
public class ODEBox extends ODEBody {
    private double sizeX=5.0, sizeY=5.0, sizeZ=5.0;
    private double massQty = Math.sqrt(sizeX*sizeY*sizeZ);

    public ODEBox() {
        this("ODE Box");
    }

    public ODEBox(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        // add scene elements
        geom = createBox(physics.getODESpace(), sizeX, sizeY, sizeZ);
        geom.setBody(body);

        mass.setBoxTotal(massQty, sizeX, sizeY, sizeZ);
        body.setMass(mass);

        // add a Node with a MeshInstance to represent the cube.
        MeshInstance meshInstance = new MeshInstance();
        meshInstance.setMesh(new Box(sizeX,sizeY,sizeZ));
        addChild(meshInstance);

        addChild(new Material());
    }
}
