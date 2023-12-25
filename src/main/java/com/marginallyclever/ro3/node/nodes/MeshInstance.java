package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.apps.dialogs.MeshFactoryDialog;
import com.marginallyclever.ro3.CollapsiblePanel;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.MeshSmoother;
import com.marginallyclever.ro3.mesh.load.MeshFactory;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * <p>A {@link MeshInstance} is a {@link Pose} containing a {@link Mesh}.</p>
 * <p>The local {@link Pose} information can be used to adjust the center of rotation.</p>
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
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(MeshInstance.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0,2));

        JButton select = new JButton();
        setMeshButtonLabel(select);
        select.addActionListener(e -> {
            MeshFactoryDialog meshFactoryDialog = new MeshFactoryDialog();
            int result = meshFactoryDialog.run();
            if(result == JFileChooser.APPROVE_OPTION) {
                mesh = meshFactoryDialog.getMesh();
                setMeshButtonLabel(select);
            }
        });
        addLabelAndComponent(pane,"Mesh",select);

        if(mesh!=null) {
            addLabelAndComponent(pane,"Vertices",new JLabel(""+mesh.getNumVertices()));
            addLabelAndComponent(pane,"Triangles",new JLabel(""+mesh.getNumTriangles()));

            JButton smooth = new JButton("Smooth");
            smooth.addActionListener(e -> MeshSmoother.smoothNormals(mesh,0.01f,0.25f) );
            addLabelAndComponent(pane,"Normals",smooth);

            JButton adjust = new JButton("Adjust");
            adjust.addActionListener(e -> adjustLocal());
            addLabelAndComponent(pane,"Local origin",adjust);

            JButton reload = new JButton("Reload");
            reload.addActionListener(e-> MeshFactory.reload(mesh) );
            addLabelAndComponent(pane,"Source",reload);
        }

        super.getComponents(list);
    }

    private void setMeshButtonLabel(JButton button) {
        button.setText((mesh==null) ? "..." : mesh.getSourceName().substring(mesh.getSourceName().lastIndexOf(File.separatorChar)+1));
    }

    /**
     * Set the mesh for this instance.
     * @param mesh the mesh to set.
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
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
            mesh = MeshFactory.load(from.getString("mesh"));
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
        if(localHit!=null && localHit.distance<Double.MAX_VALUE) {
            Vector3d normal = transformNormalToWorldSpace(localHit.normal);
            return new RayHit(this,localHit.distance,normal);
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
        //Pose pose = findParent(Pose.class);
        //Matrix4d m = pose==null ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();
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
}
