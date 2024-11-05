package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;

/**
 * <p>Base class for all {@link com.marginallyclever.ro3.node.Node} that implement ODE4J.</p>
 * <p>ODE Nodes like {@link ODEHinge} need to be able to find the subject nodes to which the hinge connects.  These
 * subjects are not guaranteed to exist during load.  They *are* guaranteed at the first call to {@link #update(double)}
 * after loading.</p>
 * <p>This class is responsible for calling {@link #onFirstUpdate()} once at the start of the first
 * {@link #update(double)}.  No physics calculations should be done in the constructor or in {@link #onAttach()}.</p>
 * <p>the flag to run {@link #onFirstUpdate()} again will be reset if:</p>
 * <ul>
 *     <li>the node is detached</li>
 *     <li>the node is deserialized</li>
 * </ul>
 */
public class ODENode extends Pose {
    /**
     * Should not be serialized, should be reset on every deserialize (such as {@link #fromJSON(JSONObject)})
     */
    private boolean runFirstUpdate=true;

    public ODENode() {
        this(ODENode.class.getSimpleName());
    }

    public ODENode(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        runFirstUpdate=true;
        fireODEAttach();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        fireODEDetach();
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if(runFirstUpdate) {
            runFirstUpdate=false;
            onFirstUpdate();
        }
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        runFirstUpdate=true;
    }

    /**
     * Called once at the start of the first {@link #update(double)}.
     * Override this to do any setup that requires all subjects to be present.
     */
    protected void onFirstUpdate() {
        // override me
    }

    boolean getRunFirstUpdate() {
        return runFirstUpdate;
    }

    /**
     * @param runFirstUpdate Set true to run {@link #onFirstUpdate()} again.
     */
    void setRunFirstUpdate(boolean runFirstUpdate) {
        this.runFirstUpdate=runFirstUpdate;
    }

    /**
     * Fired after an ODENode is attached to the scene.
     */
    private void fireODEAttach() {
        for(ODELinkAttachListener listener : listeners.getListeners(ODELinkAttachListener.class)) {
            listener.linkAttached(this);
        }
    }

    /**
     * Fired after an ODENode is detached from the scene.
     */
    private void fireODEDetach() {
        for(ODELinkDetachListener listener : listeners.getListeners(ODELinkDetachListener.class)) {
            listener.linkDetached(this);
        }
    }

    public void addODEDetachListener(ODELinkDetachListener o) {
        listeners.add(ODELinkDetachListener.class,o);
    }

    public void removeODEDetachListener(ODELinkDetachListener o) {
        listeners.remove(ODELinkDetachListener.class,o);
    }

    public void addODEAttachListener(ODELinkAttachListener o) {
        listeners.add(ODELinkAttachListener.class,o);
    }

    public void removeODEAttachListener(ODELinkAttachListener o) {
        listeners.remove(ODELinkAttachListener.class,o);
    }
}
