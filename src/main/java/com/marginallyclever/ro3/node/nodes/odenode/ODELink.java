package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A link between two ODEBodies such as a joint or motor.
 */
public class ODELink extends ODENode implements ODELinkDetachListener, ODELinkAttachListener {
    private static final Logger logger = LoggerFactory.getLogger(ODELink.class);
    protected final NodePath<ODEBody> partA = new NodePath<>(this,ODEBody.class);
    protected final NodePath<ODEBody> partB = new NodePath<>(this,ODEBody.class);

    public ODELink() {
        this(ODELink.class.getSimpleName());
    }

    public ODELink(String name) {
        super(name);
    }

    public NodePath<ODEBody> getPartA() {
        return partA;
    }

    public NodePath<ODEBody> getPartB() {
        return partB;
    }

    public void setPartA(ODEBody subject) {
        stopListeningTo(partA);
        partA.setUniqueIDByNode(subject);
        listenTo(partA);
        connect();
    }

    public void setPartB(ODEBody subject) {
        stopListeningTo(partB);
        partB.setUniqueIDByNode(subject);
        listenTo(partB);
        connect();
    }

    private void stopListeningTo(NodePath<ODEBody> path) {
        var s = path.getSubject();
        if(s!=null) {
            logger.debug("{} ignore {}", this.getName(), s.getName());
            s.removeODEDetachListener(this);
            s.removeODEAttachListener(this);
        }
    }

    private void listenTo(NodePath<ODEBody> path) {
        var s = path.getSubject();
        if(s!=null) {
            logger.debug("{} listen to {}", this.getName(), s.getName());
            s.addODEDetachListener(this);
            s.addODEAttachListener(this);
        }
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("partA",partA.getUniqueID());
        json.put("partB",partB.getUniqueID());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("partA")) partA.setUniqueID(from.getString("partA"));
        if(from.has("partB")) partB.setUniqueID(from.getString("partB"));
        listenTo(partA);
        listenTo(partB);
        connect();
    }

    /**
     * Override this method to handle connecting the two parts.
     */
    protected void connect() {}

    @Override
    public void linkAttached(ODENode body) {
        connect();
    }

    @Override
    public void linkDetached(ODENode body) {
        connect();
    }
}
