package com.marginallyclever.ro3.node.nodefactory;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.nodes.behavior.BehaviorTreeRunner;
import com.marginallyclever.ro3.node.nodes.behavior.Fallback;
import com.marginallyclever.ro3.node.nodes.behavior.Sequence;
import com.marginallyclever.ro3.node.nodes.behavior.actions.LimbMoveToTarget;
import com.marginallyclever.ro3.node.nodes.behavior.decorators.*;
import com.marginallyclever.ro3.node.nodes.crab.Crab;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.limbplanner.LimbPlanner;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.marlinrobot.linearstewartplatform.LinearStewartPlatform;
import com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.MarlinRobotArm;
import com.marginallyclever.ro3.node.nodes.networksession.NetworkSession;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Brain;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Neuron;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.Synapse;
import com.marginallyclever.ro3.node.nodes.neuralnetwork.leglimbic.LegLimbic;
import com.marginallyclever.ro3.node.nodes.odenode.*;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODEBox;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODECapsule;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODECylinder;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODESphere;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.*;
import com.marginallyclever.ro3.node.nodes.pose.poses.space.SpaceShip;
import com.marginallyclever.ro3.node.nodes.tests.RandomHemisphereTest;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A factory that can be used to create Nodes.  It does not manage the objects it creates.
 */
public class NodeFactory {
    private static final Logger logger = LoggerFactory.getLogger(NodeFactory.class);

    private final NodeFactoryCategory root = new NodeFactoryCategory("Node",Node::new);

    public NodeFactory() {
        super();
        //scan();
    }

    public NodeFactoryCategory getRoot() {
        return root;
    }

    /**
     * find the sub-factory that matches the given identifier.
     * @param identifier the name of type of {@link Node} the sub-factory produces.
     * @return the sub-factory that matches the given identifier, or null if not found.
     */
    public Supplier<Node> getSupplierFor(String identifier) {
        List<NodeFactoryCategory> toCheck = new ArrayList<>();
        toCheck.add(root);
        while(!toCheck.isEmpty()) {
            NodeFactoryCategory current = toCheck.removeFirst();
            if(identifier.equals(current.getName())) {
                return current.getSupplier();
            }
            toCheck.addAll(current.getChildren());
        }

        return null;
    }

    /**
     * Create a new {@link Node} of the given type.
     * @param identifier the type of Node to create.
     * @return a new instance of the Node, or null if the supplier is not found.
     */
    public Node create(String identifier) {
        Supplier<Node> supplier = getSupplierFor(identifier);
        if(supplier==null) return null;
        return supplier.get();
    }

    /**
     * Scan all classes in the package for classes that extend {@link Node}.
     * @param packageName the package to scan.
     */
    public void scan(String packageName) {
        // Create a new instance of Reflections
        try(ScanResult result = new io.github.classgraph.ClassGraph()
                .enableClassInfo()
                .acceptPackages(packageName)
                .scan()) {
            // Get all classes that extend T
            var classes = result.getClassesImplementing(Node.class.getName()).loadClasses();
            for (var clazz : classes) {
                // log it
                logger.info("Found " + clazz.getName());
            }
        }
    }

    public void clear() {
        root.clear();
    }

    public void addKnownNodes() {
        NodeFactoryCategory node = root;
        {
            NodeFactoryCategory behavior = node.add("Behavior", null);
            {
                NodeFactoryCategory action = behavior.add("Action", null);
                {
                    action.add("LimbMoveToTarget", LimbMoveToTarget::new);
                }
                behavior.add("Control", null);
                NodeFactoryCategory decorator = behavior.add("Decorator", null);
                {
                    decorator.add("ForceFailure", ForceFailure::new);
                    decorator.add("ForceSuccess", ForceSuccess::new);
                    decorator.add("Inverter", Inverter::new);
                    decorator.add("KeepRunningUntilFailure", KeepRunningUntilFailure::new);
                    decorator.add("Repeat", Repeat::new);
                    decorator.add("RetryUntilSuccessful", RetryUntilSuccessful::new);
                }
                behavior.add("Fallback", Fallback::new);
                behavior.add("Sequence", Sequence::new);
            }
            node.add("BehaviorTreeRunner", BehaviorTreeRunner::new);
            node.add("Crab", Crab::new);
            node.add("DHParameter", DHParameter::new);
            node.add("Environment", Environment::new);
            node.add("HingeJoint", HingeJoint::new);
            node.add("LimbPlanner", LimbPlanner::new);
            node.add("LimbSolver", LimbSolver::new);
            node.add("LinearStewartPlatform", LinearStewartPlatform::new);
            node.add("LinearJoint", LinearJoint::new);
            node.add("MarlinRobotArm", MarlinRobotArm::new);
            node.add("Material", Material::new);
            node.add("Motor", Motor::new);
            node.add("NetworkSession", NetworkSession::new);
            NodeFactoryCategory nn = node.add("NeuralNetwork", null);
            {
                nn.add("Brain", Brain::new);
                nn.add("LegLimbic", LegLimbic::new);
                nn.add("Neuron", Neuron::new);
                nn.add("Synapse", Synapse::new);
            }
            NodeFactoryCategory pose = node.add("Pose", Pose::new);
            {
                pose.add("AttachmentPoint", AttachmentPoint::new);
                pose.add("Camera", Camera::new);
                pose.add("Limb", Limb::new);
                pose.add("LookAt", LookAt::new);
                pose.add("MeshInstance", MeshInstance::new);
                pose.add("SpaceShip", SpaceShip::new);
            }
            NodeFactoryCategory physics = node.add("Physics", null);
            {
                physics.add("CreatureController", CreatureController::new);
                physics.add("ODEAngularMotor", ODEAngularMotor::new);
                physics.add("ODEBallSocket", ODEBallSocket::new);
                physics.add("ODEBox", ODEBox::new);
                physics.add("ODECapsule", ODECapsule::new);
                physics.add("ODECylinder", ODECylinder::new);
                physics.add("ODEHinge", ODEHinge::new);
                physics.add("ODEPlane", ODEPlane::new);
                physics.add("ODESlider", ODESlider::new);
                physics.add("ODESphere", ODESphere::new);
            }
            NodeFactoryCategory tests = node.add("Tests", null);
            {
                tests.add("RandomHemisphereTest", RandomHemisphereTest::new);
            }
        }
    }
}
