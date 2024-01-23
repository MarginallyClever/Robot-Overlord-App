package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.node.nodefactory.NodeFactory;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.limbplanner.LimbPlanner;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.marlinrobotarm.MarlinRobotArm;
import com.marginallyclever.ro3.node.nodes.pose.*;
import com.marginallyclever.ro3.node.nodes.pose.poses.*;
import com.marginallyclever.ro3.texture.TextureFactory;

import javax.swing.event.EventListenerList;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public static void start() {
        nodeFactory.clear();
        NodeFactory.Category nodule = nodeFactory.getRoot();
            nodule.add("DHParameter", DHParameter::new);
            nodule.add("HingeJoint", HingeJoint::new);
            nodule.add("LimbPlanner", LimbPlanner::new);
            nodule.add("LimbSolver", LimbSolver::new);
            nodule.add("LinearJoint", LinearJoint::new);
            nodule.add("MarlinRobotArm", MarlinRobotArm::new);
            nodule.add("Material", Material::new);
            nodule.add("MeshInstance", MeshInstance::new);
            nodule.add("Motor", Motor::new);
            NodeFactory.Category pose = nodule.add("Pose", Pose::new);
                pose.add("AttachmentPoint", AttachmentPoint::new);
                pose.add("Camera", Camera::new);
                pose.add("Limb", Limb::new);
                pose.add("LookAt", LookAt::new);/*
        Factory.Category behavior = nodule.add("Behavior", null);
            Factory.Category action = behavior.add("Action",null);
            Factory.Category control = behavior.add("Control",null);
            Factory.Category decorator = behavior.add("Decorator",null);
                decorator.add("ForceFailure", ForceFailure::new);
                decorator.add("ForceSuccess", ForceSuccess::new);
                decorator.add("Inverter", Inverter::new);
                decorator.add("KeepRunningUntilFailure", KeepRunningUntilFailure::new);
                decorator.add("Repeat", Repeat::new);
                decorator.add("RetryUntilSuccessful", RetryUntilSuccessful::new);
            behavior.add("Fallback", Fallback::new);
            behavior.add("Sequence", Sequence::new);*/
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
}
