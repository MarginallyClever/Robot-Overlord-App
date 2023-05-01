package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RayHit;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

@ComponentDependency(components={PoseComponent.class, MaterialComponent.class})
public abstract class ShapeComponent extends RenderComponent {
    // a mesh from the pool of meshes
    protected transient Mesh myMesh;

    public transient final IntParameter numTriangles = new IntParameter("Triangles",0);
    public transient final BooleanParameter hasNormals = new BooleanParameter("Has normals",false);
    public transient final BooleanParameter hasColors = new BooleanParameter("Has colors",false);
    public transient final BooleanParameter hasUVs = new BooleanParameter("Has UVs",false);

    protected ShapeComponent() {
        super();
    }

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity!=null) {
            entity.addComponent(new PoseComponent());
            entity.addComponent(new MaterialComponent());
        }
    }

    public void setModel(Mesh m) {
        myMesh = m;
        if(m==null) {
            numTriangles.set(0);
            hasNormals.set(false);
            hasColors.set(false);
            hasUVs.set(false);
        } else {
            numTriangles.set(myMesh.getNumTriangles());
            hasNormals.set(myMesh.getHasNormals());
            hasColors.set(myMesh.getHasColors());
            hasUVs.set(myMesh.getHasUVs());
        }
    }

    public Mesh getModel() {
        return myMesh;
    }

    public void render(GL2 gl2) {
        if( !getEnabled() || !getVisible() || myMesh==null ) return;
        myMesh.render(gl2);
    }

    public RayHit intersect(Ray ray) {
        if(myMesh==null) return null;
        if(!getEnabled()) return null;
        if(!getVisible()) return null;

        Entity e = getEntity();
        if(e==null) return null;
        PoseComponent pose = e.findFirstComponent(PoseComponent.class);
        if(pose==null) return null;

        Ray localRay = transformRayToLocalSpace(pose, ray);
        double distance = myMesh.intersect(localRay);
        if(distance<Double.MAX_VALUE) {
            return new RayHit(this,distance);
        } else {
            return null;
        }
    }

    /**
     * transform the ray into local space.
     * @param pose the pose of the entity
     * @param ray the ray in world space
     * @return the ray in local space
     */
    private Ray transformRayToLocalSpace(PoseComponent pose,Ray ray) {
        Matrix4d m = pose.getWorld();
        Point3d o = new Point3d(ray.getOrigin());
        Vector3d d = new Vector3d(ray.getDirection());

        m.invert();
        m.transform(o);
        m.transform(d);

        return new Ray(o,d);
    }
}
