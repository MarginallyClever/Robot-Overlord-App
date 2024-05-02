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
public class CreatureController extends Node implements CollisionListener {
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
    public List<ODEHinge> findHinges() {
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
    protected void onDetach() {
        super.onDetach();
        Registry.getPhysics().removeCollisionListener(this);
    }

    /**
     * @return A set of all the bodies that are connected to hinges of this creature.
     */
    public Set<ODEBody> findBodies() {
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
                // TODO finish me!
            }
        }
    }
}
