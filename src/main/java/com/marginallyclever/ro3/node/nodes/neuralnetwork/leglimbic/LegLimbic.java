package com.marginallyclever.ro3.node.nodes.neuralnetwork.leglimbic;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain;
import com.marginallyclever.ro3.node.nodes.odenode.ODEAngularMotor;

import javax.swing.*;
import java.util.List;

/**
 * <p>{@link LegLimbic} bonds together a Leg with a Brain.</p>
 * <p>It creates a {@link com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain} as a child node.  It sets
 * up the {@link com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain} with a number of input and output neurons.
 * When {@link com.marginallyclever.ro3.physics.ODEPhysics} is not paused it sends data from the Leg to the inputs and
 * reads data from the outputs to control the Leg.</p>
 * <p>{@link LegLimbic} looks for an existing Leg setup.  It is inflexible and only looks for one layout of limb:
 * <ul>
 *     <li>ODEPlane Floor</li>
 *     <li>ODESlider Slider</li>
 *     <li>Pose Leg
 *         <ul>
 *             <li>ODEHinge Joint Knee
 *                 <ul><li>ODEAngularMotor KneeMuscle</li></ul>
 *             </li>
 *             <li>ODEHinge Joint Hip
 *                 <ul><li>ODEAngularMotor HipMuscle </li></ul>
 *             </li>
 *             <li>ODEBody Waist</li>
 *             <li>ODEBody Thigh</li>
 *             <li>ODEBody Calf</li>
 *             <li>LegLimbic limbicSystem
 *                 <ul><li>Brain</li></ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul></p>
 * <p>if the brain is removed a new one is immediately created.  I'm not in love with this behavior.</p>
 */
public class LegLimbic extends Node {
    private Brain brain;

    public LegLimbic() {
        this("Leg Limbic");
    }
    public LegLimbic(String name) {
        super(name);
    }

    @Override
    protected void onReady() {
        super.onReady();
        // do I have a child of type Brain?
        brain = findFirstChild(Brain.class);
        if(brain==null) {
            // no, create one
            createBrain();
        }
        brain.addDetachListener(this::onBrainRemoved);
    }

    private void onBrainRemoved(Node node) {
        // if the brain is removed create a new one.
        createBrain();
    }

    private void createBrain() {
        brain = new Brain();
        brain.setInputs(2);
        brain.setOutputs(2);
        addChild(brain);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LegLimbicPanel(this));
        super.getComponents(list);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        if(!Registry.getPhysics().isPaused()) {
            // send data from the Leg to the Brain
            var leg = getParent();
            var knee = (ODEAngularMotor)leg.findByPath("Knee/KneeMuscle");
            if(knee!=null) {
                var v = brain.getOutput(0);
                knee.addTorque(v);
            }

            var hip = (ODEAngularMotor)leg.findByPath("Hip/HipMuscle");
            if(hip!=null) {
                var v = brain.getOutput(1);
                hip.addTorque(v);
            }

            // read data from the Brain to control the Leg
        }
    }
}
