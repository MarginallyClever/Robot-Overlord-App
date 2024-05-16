package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.odenode.brain.BrainManager;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import com.marginallyclever.ro3.physics.CollisionListener;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DJoint;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>A controller for a creature made of ODE4J hinges and bodies.</p>
 * <p>This controller registers itself to receive collision events from the physics world.</p>
 *
 */
public class CreatureController extends ODENode implements CollisionListener {
    private final List<ODEHinge> hinges = new ArrayList<>();
    private final List<ODEBody> bodies = new ArrayList<>();
    private final BrainManager brainManager = new BrainManager();
    // max experienced during simulation
    private double maxForce = 0;
    // max experienced during simulation
    private double maxTorque = 0;
    private double maxOutputTorque = 150000;  // magic numbers are fun!

    public CreatureController() {
        super("CreatureController");
    }

    public CreatureController(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new CreatureControllerPanel(this));
        super.getComponents(list);
    }

    /**
     * @return A list of all the hinges that are connected to this creature.
     */
    private List<ODEHinge> findHinges() {
        List<ODEHinge> hinges = new ArrayList<>();
        List<Node> toSearch = new ArrayList<>(getChildren());
        while(!toSearch.isEmpty()) {
            Node node = toSearch.remove(0);
            toSearch.addAll(node.getChildren());

            if(node instanceof ODEHinge hinge) {
                hinges.add(hinge);
            }
        }
        return hinges;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        Registry.getPhysics().addCollisionListener(this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        Registry.getPhysics().removeCollisionListener(this);
    }

    /**
     * @return A set of all the bodies that are connected to hinges of this creature.
     */
    private Set<ODEBody> findBodies() {
        Set<ODEBody> bodies = new HashSet<>();
        List<ODEHinge> hinges = findHinges();
        for(ODEHinge hinge : hinges) {
            bodies.add(hinge.getPartA().getSubject());
            bodies.add(hinge.getPartB().getSubject());
        }
        return bodies;
    }

    @Override
    public void onCollision(DGeom g1, DGeom g2, DContact contact) {
        var bodies = findBodies();
        for(ODEBody b : bodies) {
            if(b.getGeom() == g1 || b.getGeom() == g2) {
                // b is touching something
                // store the hinge/is-touching relationship in c
                b.setTouchingSomething(true);
            }
        }
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();
        // store a list of the hinges once and keep it until the end of time or someone forces a refresh.
        hinges.clear();
        hinges.addAll(findHinges());
        bodies.clear();
        bodies.addAll(findBodies());
        brainManager.setNumInputs(bodies.size()+hinges.size()+1);
        brainManager.setNumOutputs(hinges.size());
        brainManager.createInitialConnections();

        // add feedback to hinges
        for(ODEHinge h : hinges) {
            h.getHinge().setFeedback(new DJoint.DJointFeedback());
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        sendSensoryInputToBrain(dt);

        // perform magic
        brainManager.update(dt);

        sendBrainOutputToHinges();

        // reset the isTouching flag on all bodies
        for (ODEBody b : bodies) {
            b.setTouchingSomething(false);
        }
    }

    private void sendSensoryInputToBrain(double dt) {
        // get the matrix for the torso, probably always bodies[0]?  Not guaranteed.
        var torsoMatrix = bodies.get(0).getWorld();
        var iTorso = new Matrix4d();
        iTorso.invert(torsoMatrix);
        // multiply all brain matrices by the inverse of torsoMatrix
        var t = new Vector3d();
        int i=0;
        for (ODEBody b : bodies) {
            var m = b.getWorld();
            // normalize rotation.
            m.mul(iTorso);
            // normalize translation
            m.get(t);
            t.scale(0.1);
            m.setTranslation(t);
            // set to brain
            brainManager.setMatrix(i++, m);
        }

        // any bodies that are marked isTouching must be because onCollision says so.
        // onCollision happens before update, so this is the right place to check the flag.
        // add the isTouching flag to the brain sensory input
        i=0;
        for (ODEBody b : bodies) {
            brainManager.setTouching(i++,b.isTouchingSomething());
        }

        // add the hinge feedback to the brain sensory input
        Matrix4d hm = new Matrix4d();
        for( ODEHinge hinge : hinges) {
            var internalHinge = hinge.getHinge();
            if(internalHinge==null) {
                hm.setIdentity();
            } else {
                convertHingeFeedbackToMatrix(internalHinge.getFeedback(),hm);
            }
            brainManager.setMatrix(i++,hm);
        }

        // add the torso matrix.  Good for world up, world north, height above flat ground.
        brainManager.setMatrix(i,torsoMatrix);

        //System.out.println("f"+fmax+" t"+tmax);
    }

    private void convertHingeFeedbackToMatrix(DJoint.DJointFeedback feedback, Matrix4d hm) {
        hm.m00 = calcMaxF(feedback.f1.get0());
        hm.m10 = calcMaxF(feedback.f1.get1());
        hm.m20 = calcMaxF(feedback.f1.get2());
        hm.m01 = calcMaxT(feedback.t1.get0());
        hm.m11 = calcMaxT(feedback.t1.get1());
        hm.m21 = calcMaxT(feedback.t1.get2());
        hm.m02 = calcMaxF(feedback.f2.get0());
        hm.m12 = calcMaxF(feedback.f2.get1());
        hm.m22 = calcMaxF(feedback.f2.get2());
        hm.m03 = calcMaxT(feedback.t2.get0());
        hm.m13 = calcMaxT(feedback.t2.get1());
        hm.m23 = calcMaxT(feedback.t2.get2());
    }

    private double calcMaxF(double f22) {
        maxForce = Math.max(maxForce,Math.abs(f22));
        return maxForce >0 ? (f22 / maxForce) : f22;
    }

    private double calcMaxT(double t22) {
        maxTorque = Math.max(maxTorque,Math.abs(t22));
        return maxTorque >0 ? (t22 / maxTorque) : t22;
    }

    private void sendBrainOutputToHinges() {
        // I want the system to output torque values for each hinge.
        int i=0;
        for (ODEHinge h : hinges) {
            var force = brainManager.getOutput(i++);
            //System.out.print(force+"\t");
            h.addTorque(force * maxOutputTorque);
        }
        //System.out.println();
    }

    public List<ODEHinge> getHinges() {
        return hinges;
    }
    public List<ODEBody> getBodies() {
        return bodies;
    }

    public BrainManager getBrain() {
        return brainManager;
    }
}
