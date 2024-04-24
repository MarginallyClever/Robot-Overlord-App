package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODEBody;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DHingeJoint;
import org.ode4j.ode.OdeHelper;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;

/**
 * <p>Wrapper for a hinge joint in ODE4J.  If one side of the hinge is null then it is attached to the world.</p>
 * <p>If the physics simulation is paused then then moving this Pose node will adjust the position and orientation
 * of the hinge, as well as it's relation to the attached parts.  If the simulation is NOT paused then the hinge
 * will behave as normal.</p>
 */
public class ODEHinge extends Pose {
    private DHingeJoint hinge;
    private final NodePath<ODEBody> partA = new NodePath<>(this,ODEBody.class);
    private final NodePath<ODEBody> partB = new NodePath<>(this,ODEBody.class);

    public ODEHinge() {
        this("ODE Hinge");
    }

    public ODEHinge(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEHingePanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        hinge = OdeHelper.createHingeJoint (physics.getODEWorld(),null);

        DBody a = partA.getSubject() == null? null : partA.getSubject().getODEBody();
        DBody b = partB.getSubject() == null? null : partB.getSubject().getODEBody();
        hinge.attach (a,b);

        hinge.setAnchor (0,0,1);
        hinge.setAxis (1,-1,1.41421356);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if(hinge!=null) {
            try {
                hinge.destroy();
            } catch(Exception ignored) {} // if physics is already destroyed, this will throw an exception.
        }
    }

    public NodePath<ODEBody> getPartA() {
        return partA;
    }

    public NodePath<ODEBody> getPartB() {
        return partB;
    }

    public DHingeJoint getHinge() {
        return hinge;
    }

    public void setPartA(ODEBody subject) {
        partA.setUniqueIDByNode(subject);
        updateHinge();
    }

    public void setPartB(ODEBody subject) {
        partB.setUniqueIDByNode(subject);
        updateHinge();
    }

    /**
     * Tell the physics engine who is connected to this hinge.
     */
    private void updateHinge() {
        DBody a = partA.getSubject() == null ? null : partA.getSubject().getODEBody();
        DBody b = partB.getSubject() == null ? null : partB.getSubject().getODEBody();
        hinge.attach(a, b);
    }

    @Override
    public void setLocal(Matrix4d m) {
        super.setLocal(m);
        updateHingePose();
    }

    private void updateHingePose() {
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        // only let the user move the hinge if the physics simulation is paused.
        if(physics.isPaused()) {
            // set the hinge reference point and axis.
            var mat = getWorld();
            var pos = MatrixHelper.getPosition(mat);
            hinge.setAnchor(pos.x, pos.y, pos.z);
            var axis = MatrixHelper.getZAxis(mat);
            hinge.setAxis(axis.x, axis.y, axis.z);
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        var physics = ODE4JHelper.guaranteePhysicsWorld();
        if(!physics.isPaused()) {
            // if the physics simulation is running then the hinge will behave as normal.
            DVector3 anchor = new DVector3();
            DVector3 axis = new DVector3();
            hinge.getAnchor(anchor);
            hinge.getAxis(axis);
            // use axis and anchor to set the world matrix.
            Matrix3d m3 = MatrixHelper.lookAt(
                    new Vector3d(0,0,0),
                    new Vector3d(axis.get0(),axis.get1(),axis.get2())
            );
            Matrix4d m4 = new Matrix4d();
            m4.set(m3);
            m4.setTranslation(new Vector3d(anchor.get0(),anchor.get1(),anchor.get2()));
            setWorld(m4);
        }
    }
}
