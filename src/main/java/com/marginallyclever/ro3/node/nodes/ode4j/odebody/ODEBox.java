package com.marginallyclever.ro3.node.nodes.ode4j.odebody;

import com.marginallyclever.ro3.mesh.shapes.Box;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.ode4j.ODE4JHelper;
import com.marginallyclever.ro3.node.nodes.ode4j.ODEWorldSpace;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;
import org.ode4j.ode.DBox;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.util.List;

import static org.ode4j.ode.OdeHelper.createBox;

/**
 * Wrapper for a ODE4J Box.
 */
public class ODEBox extends ODEBody {
    private final Vector3d size = new Vector3d(5,5,5);

    public ODEBox() {
        this("ODE Box");
    }

    public ODEBox(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new ODEBoxPanel(this));
        super.getComponents(list);
    }

    @Override
    protected void onFirstUpdate() {
        super.onFirstUpdate();

        ODEWorldSpace physics = ODE4JHelper.guaranteePhysicsWorld();
        geom = createBox(physics.getODESpace(), size.x, size.y, size.z);
        geom.setBody(body);

        mass.setBoxTotal(ODE4JHelper.volumeBox(size.x,size.y,size.z), size.x, size.y, size.z);
        body.setMass(mass);

        if(findFirstChild(MeshInstance.class)==null) addChild(new MeshInstance());
        if(findFirstChild(Material.class)==null) addChild(new Material());
        updateSize();
    }

    public double getSizeX() {
        return size.x;
    }

    public double getSizeY() {
        return size.y;
    }

    public double getSizeZ() {
        return size.z;
    }

    public void setSizeX(double size) {
        if(size<=0) throw new IllegalArgumentException("Size must be greater than zero.");
        this.size.x = size;
        updateSize();
    }

    public void setSizeY(double size) {
        if(size<=0) throw new IllegalArgumentException("Size must be greater than zero.");
        this.size.y = size;
        updateSize();
    }

    public void setSizeZ(double size) {
        if(size<=0) throw new IllegalArgumentException("Size must be greater than zero.");
        this.size.z = size;
        updateSize();
    }

    private void updateSize() {
        if(geom==null) return;

        ((DBox)geom).setLengths(size.x, size.y, size.z);
        geom.setBody(body);

        mass.setBoxTotal(mass.getMass(), size.x, size.y, size.z);
        body.setMass(mass);

        var meshInstance = findFirstChild(MeshInstance.class);
        if(meshInstance!=null) {
            meshInstance.setMesh(new Box(size.x,size.y,size.z));
        }
    }

    @Override
    public JSONObject toJSON() {
        var json= super.toJSON();
        json.put("sizeX", size.x);
        json.put("sizeY", size.y);
        json.put("sizeZ", size.z);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("sizeX")) size.x = json.getDouble("sizeX");
        if(json.has("sizeY")) size.y = json.getDouble("sizeY");
        if(json.has("sizeZ")) size.z = json.getDouble("sizeZ");
        updateSize();
    }
}
