package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import org.json.JSONObject;
import org.ode4j.ode.DBody;
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
        connectInternal();
    }

    public void setPartB(ODEBody subject) {
        stopListeningTo(partB);
        partB.setUniqueIDByNode(subject);
        listenTo(partB);
        connectInternal();
    }

    private void stopListeningTo(NodePath<ODEBody> path) {
        var s = path.getSubject();
        if(s!=null) {
            s.removeODEDetachListener(this);
            s.removeODEAttachListener(this);
        }
    }

    private void listenTo(NodePath<ODEBody> path) {
        var s = path.getSubject();
        if(s!=null) {
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
        connectInternal();
    }

    @Override
    public void linkAttached(ODENode body) {
        connectInternal();
    }

    @Override
    public void linkDetached(ODENode body) {
        connectInternal();
    }

    /**
     * Examines the bodies of this link and connects them if possible.
     */
    protected void connectInternal() {
        var as = partA.getSubject();
        var bs = partB.getSubject();
        if(as==null && bs==null) {
            connect(null,null);
            return;
        }
        if(as==null) {
            as=bs;
            bs=null;
        }
        DBody a = as.getODEBody();
        DBody b = bs == null ? null : bs.getODEBody();

        //logger.debug(this.getName()+" connect "+ as.getName() +" to "+(bs == null ?"null":bs.getName()));
        connect(a,b);
    }

    /**
     * <p>Override this method to handle connecting the two parts.  if only one body exists it will be guaranteed to be
     * body a.</p>
     * <p>This method should not be called directly. Instead, call {@link #connectInternal()}.</p>
     */
    protected void connect(DBody a, DBody b) {}

}
