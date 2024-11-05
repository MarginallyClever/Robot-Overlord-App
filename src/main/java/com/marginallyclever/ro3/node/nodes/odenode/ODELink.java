package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;
import org.json.JSONObject;

/**
 * A link between two ODEBodies such as a joint or motor.
 */
public class ODELink extends ODENode {
    protected final NodePath<ODEBody> partA = new NodePath<>(this,ODEBody.class);
    protected final NodePath<ODEBody> partB = new NodePath<>(this,ODEBody.class);

    public ODELink() {
        this("ODELink");
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
        var s = partA.getSubject();
        if(s!=null) {
            s.removeDetachListener(e->connect());
            s.removeAttachListener(e->connect());
        }
        partA.setUniqueIDByNode(subject);
        s = partA.getSubject();
        if(s!=null) {
            s.addDetachListener(e->connect());
            s.addAttachListener(e->connect());
        }
        connect();
    }

    public void setPartB(ODEBody subject) {
        var s = partB.getSubject();
        if(s!=null) {
            s.removeDetachListener(e->connect());
            s.removeAttachListener(e->connect());
        }
        partB.setUniqueIDByNode(subject);
        s = partB.getSubject();
        if(s!=null) {
            s.addDetachListener(e->connect());
            s.addAttachListener(e->connect());
        }
        connect();
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
        connect();
    }

    /**
     * Override this method to handle connecting the two parts.
     */
    protected void connect() {}
}
