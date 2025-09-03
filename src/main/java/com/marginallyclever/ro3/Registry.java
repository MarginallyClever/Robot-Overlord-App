package com.marginallyclever.ro3;

import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodefactory.NodeFactory;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.physics.ODEPhysics;
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
    public static final ListWithEvents<Node> selection = new ListWithEvents<>();
    private static final ODEPhysics physics = new ODEPhysics();

    public static void start() {
        nodeFactory.clear();
        nodeFactory.scan("com.marginallyclever.ro3");
        nodeFactory.addKnownNodes();
        reset();
    }

    public static void reset() {
        selection.removeAll();

        // reset camera
        cameras.removeAll();
        Camera first = new Camera("Camera 1");
        cameras.add(first);
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

        setScene(new Node("Scene"));
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

    public static ODEPhysics getPhysics() {
        return physics;
    }
}