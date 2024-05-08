package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import com.marginallyclever.ro3.physics.CollisionListener;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DGeom;

import javax.swing.*;
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
    private final Brain brain = new Brain();

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
        brain.setNumInputs(bodies.size()+1);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        // any bodies that are marked isTouching must be because onCollision says so.

        // get the matrix for the torso, probably always bodies[0]?  Not guaranteed.
        var torsoMatrix = bodies.get(0).getWorld();
        torsoMatrix.invert();
        // multiply all brain matrices by the inverse of torsoMatrix
        int i=0;
        for (ODEBody b : bodies) {
            // I'm concerned this is not the correct matrix.
            // the translation values are not normalized.
            var m = b.getWorld();
            m.mul(torsoMatrix);
            brain.setMatrix(i,m);
            brain.setTouching(i,b.isTouchingSomething());
            System.out.print(b.isTouchingSomething()?"1":"0");
            ++i;
        }
        System.out.println();

        brain.setMatrix(i,torsoMatrix);

        // I have all the input for the robot dog, normalized.
        // some magic happens here
        brain.update(dt);

        // I want the system to output torque values for each hinge.
        i=0;
        for (ODEHinge h : hinges) {
            h.addTorque(brain.getOutput(i));
            ++i;
        }

        // reset the isTouching flag on all bodies
        for (ODEBody b : bodies) {
            b.setTouchingSomething(false);
        }
    }

    public List<ODEHinge> getHinges() {
        return hinges;
    }
    public List<ODEBody> getBodies() {
        return bodies;
    }

    public Brain getBrain() {
        return brain;
    }
}
