package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.raypicking.RayHit;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

/**
 * <p>A {@link MeshInstance} is a {@link Pose} containing a {@link Mesh}.</p>
 * <p>The local {@link Pose} information can be used to adjust the center of rotation.</p>
 * <p>MeshInstance fires a {@link PropertyChangeEvent} to all {@link PropertyChangeListener}s when the {@link Mesh} is
 * changed.</p>
 */
public class MeshInstance extends Pose {
    private Mesh mesh;

    public MeshInstance() {
        super("MeshInstance");
    }

    public MeshInstance(String name) {
        super(name);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new MeshInstancePanel(this));
        super.getComponents(list);
    }

    /**
     * Set the mesh for this instance.
     * @param mesh the mesh to set.
     */
    public void setMesh(Mesh mesh) {
        if (this.mesh == mesh) return;
        if(mesh!=null) {
            mesh.removePropertyChangeListener((e)->fireMeshChanged());
        }
        this.mesh = mesh;
        mesh.addPropertyChangeListener((e)->fireMeshChanged());
        fireMeshChanged();
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        if(mesh!=null) {
            json.put("mesh", mesh.getSourceName());
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("mesh")) {
            mesh = Registry.meshFactory.load(from.getString("mesh"));
        }
    }

    public void adjustLocal() {
        Pose pose = findParent(Pose.class);
        Matrix4d m = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();
        m.invert();
        setLocal(m);
    }

    /**
     * transform the ray into local space and test for intersection.
     * @param ray the ray in world space
     * @return the ray hit in world space, or null if no hit.
     */
    public RayHit intersect(Ray ray) {
        if( mesh==null ) return null;

        Ray localRay = transformRayToLocalSpace(ray);
        RayHit localHit = mesh.intersect(localRay);
        if(localHit!=null && localHit.distance()<Double.MAX_VALUE) {
            Vector3d normal = transformNormalToWorldSpace(localHit.normal());
            return new RayHit(this,localHit.distance(),normal);
        } else {
            return null;
        }
    }

    /**
     * transform the ray into local space.
     * @param ray the ray in world space
     * @return the ray in local space
     */
    private Ray transformRayToLocalSpace(Ray ray) {
        Matrix4d m = getWorld();
        Point3d o = new Point3d(ray.getOrigin());
        Vector3d d = new Vector3d(ray.getDirection());

        m.invert();
        m.transform(o);
        m.transform(d);

        return new Ray(o,d,ray.getMaxDistance());
    }

    /**
     * transform the ray into local space.
     * @param normal the normal in local space
     * @return the ray in world space
     */
    private Vector3d transformNormalToWorldSpace(Vector3d normal) {
        Vector3d d = new Vector3d(normal);
        getWorld().transform(d);
        return d;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/pose/poses/icons8-mesh-16.png")));
    }

    public void addPropertyChangedListener(PropertyChangeListener listener) {
        listeners.add(PropertyChangeListener.class,listener);
    }

    public void removePropertyChangedListener(PropertyChangeListener listener) {
        listeners.remove(PropertyChangeListener.class,listener);
    }

    private void fireMeshChanged() {
        PropertyChangeEvent p = null;
        for( var v : listeners.getListeners(PropertyChangeListener.class)) {
            if(p==null) p = new PropertyChangeEvent(this,"mesh",null,mesh);
            v.propertyChange(p);
        }
    }
}
