package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.node.nodefactory.NodeFactory;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.behavior.BehaviorTreeRunner;
import com.marginallyclever.ro3.node.nodes.behavior.Fallback;
import com.marginallyclever.ro3.node.nodes.behavior.Sequence;
import com.marginallyclever.ro3.node.nodes.behavior.actions.LimbMoveToTarget;
import com.marginallyclever.ro3.node.nodes.behavior.decorators.*;
import com.marginallyclever.ro3.node.nodes.limbplanner.LimbPlanner;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.marlinrobot.linearstewartplatform.LinearStewartPlatform;
import com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.MarlinRobotArm;
import com.marginallyclever.ro3.node.nodes.networksession.NetworkSession;
import com.marginallyclever.ro3.node.nodes.odenode.*;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODEBox;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODECapsule;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODECylinder;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.odebodies.ODESphere;
import com.marginallyclever.ro3.node.nodes.pose.poses.*;
import com.marginallyclever.ro3.physics.ODEPhysics;
import com.marginallyclever.ro3.node.nodes.pose.*;
import com.marginallyclever.ro3.texture.TextureFactory;

import javax.swing.event.EventListenerList;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Registry} is a place to store global variables.
 */
public class Registry {
    public static final EventListenerList listeners = new EventListenerList();
    public static final TextureFactory textureFactory = new TextureFactory();
    public static final MeshFactory meshFactory = new MeshFactory();
    public static final NodeFactory nodeFactory = new NodeFactory();
    private static Node scene = new Node("Scene");
    public static final ListWithEvents<Camera> cameras = new ListWithEvents<>();
    private static Camera activeCamera = null;
    public static final ListWithEvents<Node> selection = new ListWithEvents<>();
    private static final ODEPhysics physics = new ODEPhysics();

    public static void start() {
        nodeFactory.clear();
        NodeFactory.Category node = nodeFactory.getRoot();
        {
            NodeFactory.Category behavior = node.add("Behavior", null);
            {
                NodeFactory.Category action = behavior.add("Action", null);
                {
                    action.add("LimbMoveToTarget", LimbMoveToTarget::new);
                }
                NodeFactory.Category control = behavior.add("Control", null);
                NodeFactory.Category decorator = behavior.add("Decorator", null);
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
            node.add("DHParameter", DHParameter::new);
            node.add("HingeJoint", HingeJoint::new);
            node.add("LimbPlanner", LimbPlanner::new);
            node.add("LimbSolver", LimbSolver::new);
            node.add("LinearStewartPlatform", LinearStewartPlatform::new);
            node.add("LinearJoint", LinearJoint::new);
            node.add("MarlinRobotArm", MarlinRobotArm::new);
            node.add("Material", Material::new);
            node.add("Motor", Motor::new);
            node.add("NetworkSession", NetworkSession::new);
            NodeFactory.Category pose = node.add("Pose", Pose::new);
            {
                pose.add("AttachmentPoint", AttachmentPoint::new);
                pose.add("Camera", Camera::new);
                pose.add("Limb", Limb::new);
                pose.add("LookAt", LookAt::new);
                pose.add("MeshInstance", MeshInstance::new);
            }
            NodeFactory.Category physics = node.add("Physics", null);
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
        }
        reset();
    }

    public static void reset() {
        selection.removeAll();

        // reset camera
        List<Camera> toRemove = new ArrayList<>(cameras.getList());
        for(Camera c : toRemove) cameras.remove(c);

        Camera first = new Camera("Camera 1");
        cameras.add(first);
        setActiveCamera(first);
        double v = Math.sqrt(Math.pow(50,2)/3d); // match the viewport default orbit distance.
        first.setPosition(new Vector3d(v,v,v));
        first.lookAt(new Vector3d(0,0,0));

        // reset scene
        List<Node> toRemove2 = new ArrayList<>(scene.getChildren());
        for(Node n : toRemove2) {
            scene.removeChild(n);
        }

        textureFactory.reset();
        meshFactory.reset();
        physics.reset();

        scene = new Node("Scene");
    }

    public static void addSceneChangeListener(SceneChangeListener listener) {
        listeners.add(SceneChangeListener.class,listener);
    }

    public static void removeSceneChangeListener(SceneChangeListener listener) {
        listeners.remove(SceneChangeListener.class,listener);
    }

    public static void setScene(Node newScene) {
        fireBeforeSceneChange(newScene);
        scene = newScene;
        fireAfterSceneChange(newScene);
    }

    private static void fireBeforeSceneChange(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.beforeSceneChange(newScene);
        }
    }

    private static void fireAfterSceneChange(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.afterSceneChange(newScene);
        }
    }

    public static Node getScene() {
        return scene;
    }

    public static Camera getActiveCamera() {
        if(cameras.getList().isEmpty()) throw new RuntimeException("No cameras available.");
        return activeCamera;
    }

    public static void setActiveCamera(Camera camera) {
        activeCamera = camera;
    }

    public static ODEPhysics getPhysics() {
        return physics;
    }
}
