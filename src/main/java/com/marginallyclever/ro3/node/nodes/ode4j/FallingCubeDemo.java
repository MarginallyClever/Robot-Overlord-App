package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.mesh.shapes.Box;
import com.marginallyclever.ro3.mesh.shapes.Grid;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;
import org.ode4j.ode.DGeom.DNearCallback;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.*;

/**
 *  * ODE4J Bouncing Cube Demo.
 */
public class FallingCubeDemo extends Node {
    private static final double CUBE_SIDE_LENGTH = 5.0;
    private static final double CUBE_MASS = 23.0;
    private static final int ITERS = 20;
    private final int N = 4;
    private final DContactBuffer contacts = new DContactBuffer(N);

    private DWorld world;
    private DSpace space;
    private DBody cubeBody;
    private DGeom cubeGeom;
    private DJointGroup contactGroup;

    private Pose cubeNode;

    public FallingCubeDemo() {
        super("Falling Cube");
    }

    public FallingCubeDemo(String name) {
        super(name);
    }

    protected void onAttach() {
        super.onAttach();

        OdeHelper.initODE2(0);
        reset();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        stopPhysics();
    }

    // reset demo scene.
    public void reset() {
        resetPhysics();
        resetScene();
    }

    private void stopPhysics() {
        if(world!=null) {
            world.destroy();
            world=null;
        }
        if(space!=null) {
            space.destroy();
            space=null;
        }
        if(contactGroup!=null) {
            contactGroup.destroy();
            contactGroup=null;
        }
    }

    private void resetPhysics() {
        stopPhysics();
        startPhysics();
    }

    private void startPhysics() {
        contactGroup = OdeHelper.createJointGroup();

        // build the world
        world = createWorld();
        world.setGravity(0, 0, -9.81);
        world.setCFM (1e-5);
        world.setERP (0.8);
        world.setQuickStepNumIterations (ITERS);

        // setup a space in the world
        space = //OdeHelper.createSapSpace( null, DSapSpace.AXES.XYZ );
                OdeHelper.createSimpleSpace();

        // add scene elements
        cubeBody = OdeHelper.createBody(world);
        cubeGeom = createBox(space, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH);
        cubeGeom.setBody(cubeBody);
        DMass m = OdeHelper.createMass();
        m.setBoxTotal(CUBE_MASS, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH, CUBE_SIDE_LENGTH);
        cubeBody.setMass(m);
        cubeBody.setPosition(0, 0, CUBE_SIDE_LENGTH *3);

        Matrix4d mat = new Matrix4d();
        mat.rotX(Math.toRadians(15));
        mat.rotY(Math.toRadians(22.5));
        DMatrix3C dMatrix3 = ODE4JHelper.convertRotationToODE(mat);

        cubeBody.setRotation(dMatrix3);

        OdeHelper.createPlane (space,0,0,1,0);
    }

    private void resetScene() {
        // remove all my children.
        List<Node> kids = new ArrayList<>(getChildren());
        while(!kids.isEmpty()) {
            removeChild(kids.remove(0));
        }

        // add a Node with a MeshInstance to represent the cube.
        cubeNode = new Pose("cube");
        addChild(cubeNode);
        MeshInstance cubeMesh = new MeshInstance("Cube");
        cubeNode.addChild(cubeMesh);
        cubeMesh.setMesh(new Box());
        Matrix4d m = cubeMesh.getLocal();
        m.setScale(CUBE_SIDE_LENGTH);
        cubeMesh.setLocal(m);

        // add a Node with a MeshInstance to represent the floor.
        MeshInstance floorNode = new MeshInstance("Floor");
        addChild(floorNode);
        floorNode.setMesh(new Grid());
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        try {
            OdeHelper.spaceCollide(space, null, new DNearCallback() {
                @Override
                public void call(Object data, DGeom o1, DGeom o2) {
                    nearCallback(data, o1, o2);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            world.step(dt);
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            contactGroup.empty();
        } catch(Exception e) {
            e.printStackTrace();
        }

        setCubePose();
    }

    // this is called by dSpaceCollide when two objects in space are
    // potentially colliding.

    private void nearCallback(Object data, DGeom o1, DGeom o2) {
        int i, n;

        DBody b1 = o1.getBody();
        DBody b2 = o2.getBody();
        if (b1 != null && b2 != null && OdeHelper.areConnected(b1, b2))
            return;

        try {
            n = OdeHelper.collide(o1, o2, N, contacts.getGeomBuffer());//[0].geom,sizeof(dContact));
            if (n > 0) {
                for (i = 0; i < n; i++) {
                    DContact contact = contacts.get(i);
                    contact.surface.mode = dContactSlip1 | dContactSlip2 | dContactSoftERP | dContactSoftCFM | dContactApprox1;
                    //if (o1 instanceof DSphere || o2 instanceof DSphere)
                    //    contact.surface.mu = 20;
                    //else
                        contact.surface.mu = 0.5;
                    contact.surface.slip1 = 0.0;
                    contact.surface.slip2 = 0.0;
                    contact.surface.soft_erp = 0.8;
                    contact.surface.soft_cfm = 0.01;
                    contact.surface.bounce = 0.9;
                    contact.surface.bounce_vel = 0.5;
                    DJoint contactJoint = OdeHelper.createContactJoint(world, contactGroup, contact);
                    contactJoint.attach(o1.getBody(), o2.getBody());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    private void setCubePose() {
        // adjust the position of the cubeNode to match the cubeBody.
        if(cubeNode==null || cubeBody==null) return;

        DVector3C translation = cubeBody.getPosition();
        DMatrix3C rotation = cubeBody.getRotation();

        Matrix4d m = ODE4JHelper.assembleMatrix(translation, rotation);
        cubeNode.setWorld(m);
    }
}
