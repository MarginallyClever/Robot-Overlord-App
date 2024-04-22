package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.mesh.shapes.Grid;
import com.marginallyclever.ro3.mesh.shapes.Sphere;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DGeom.DNearCallback;
import org.ode4j.ode.*;

import javax.vecmath.Matrix4d;
import java.util.ArrayList;
import java.util.List;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.*;

/**
 * ODE4J Bouncing Ball Demo.
 */
public class BouncingBallDemo extends Node {
    private static final double BALL_RADIUS = 5.0;
    private static final double BALL_MASS = 23.0;
    private static final int ITERS = 20;
    private final int N = 4;
    private final DContactBuffer contacts = new DContactBuffer(N);

    private DWorld world;
    private DSpace space;
    private DBody ballBody;
    private DGeom ballGeom;
    private DJointGroup contactGroup;

    private Pose ballNode;

    public BouncingBallDemo() {
        super("Bouncing Ball");
    }

    public BouncingBallDemo(String name) {
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

        world = createWorld();
        world.setGravity(0, 0, -9.81);
        world.setCFM (1e-5);
        world.setERP (0.8);
        world.setQuickStepNumIterations (ITERS);

        space = //OdeHelper.createSapSpace( null, DSapSpace.AXES.XYZ );
                OdeHelper.createSimpleSpace();

        ballBody = OdeHelper.createBody(world);
        ballGeom = createSphere(space, BALL_RADIUS);
        ballGeom.setBody(ballBody);
        DMass m = OdeHelper.createMass();
        m.setSphereTotal(BALL_MASS, BALL_RADIUS);
        ballBody.setMass(m);

        ballBody.setPosition(0, 0, BALL_RADIUS*3);

        OdeHelper.createPlane (space,0,0,1,0);
    }

    private void resetScene() {
        // remove all my children.
        List<Node> kids = new ArrayList<>(getChildren());
        while(!kids.isEmpty()) {
            removeChild(kids.remove(0));
        }

        // add a Node with a MeshInstance to represent the ball.
        ballNode = new Pose("Ball");
        addChild(ballNode);
        MeshInstance ballMesh = new MeshInstance("Sphere");
        ballNode.addChild(ballMesh);
        ballMesh.setMesh(new Sphere());
        Matrix4d m = ballMesh.getLocal();
        m.setScale(BALL_RADIUS);
        ballMesh.setLocal(m);

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

        setBallPose();
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


    private void setBallPose() {
        // adjust the position of the ballNode to match the ballBody.
        if(ballNode==null || ballBody==null) return;

        DVector3C translation = ballBody.getPosition();
        DMatrix3C rotation = ballBody.getRotation();
        Matrix4d m = ODE4JHelper.assembleMatrix(translation, rotation);
        ballNode.setWorld(m);
    }
}
