package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A shape {@link Component} which can be rendered.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@ComponentDependency(components={PoseComponent.class, MaterialComponent.class})
public class ShapeComponent extends RenderComponent {
    // a mesh from the pool of meshes
    protected transient Mesh myMesh;

    public transient final IntParameter numVertices = new IntParameter("Vertices",0);
    public transient final BooleanParameter hasNormals = new BooleanParameter("Has normals",false);
    public transient final BooleanParameter hasColors = new BooleanParameter("Has colors",false);
    public transient final BooleanParameter hasUVs = new BooleanParameter("Has UVs",false);

    public ShapeComponent() {
        super();
    }

    public ShapeComponent(Mesh mesh) {
        super();
        setModel(mesh);
    }

    public void setModel(Mesh m) {
        myMesh = m;
        if(m==null) {
            numVertices.set(0);
            hasNormals.set(false);
            hasColors.set(false);
            hasUVs.set(false);
        } else {
            numVertices.set(myMesh.getNumVertices());
            hasNormals.set(myMesh.getHasNormals());
            hasColors.set(myMesh.getHasColors());
            hasUVs.set(myMesh.getHasTextures());
        }
    }

    public Mesh getModel() {
        return myMesh;
    }

    public void render(GL3 gl) {
        if( !getEnabled() || !getVisible() || myMesh==null ) return;
        myMesh.render(gl);
    }

    /**
     * transform the ray into local space and test for intersection.
     * @param ray the ray in world space
     * @return the ray hit in world space, or null if no hit.
     */
    public RayHit intersect(Ray ray) {
        if( !getEnabled() || !getVisible() || myMesh==null ) return null;

        Entity e = getEntity();
        if(e==null) return null;
        PoseComponent pose = e.getComponent(PoseComponent.class);
        if(pose==null) return null;

        Ray localRay = transformRayToLocalSpace(pose, ray);
        RayHit localHit = myMesh.intersect(localRay);
        if(localHit!=null && localHit.distance<Double.MAX_VALUE) {
            Vector3d normal = transformNormalToWorldSpace(pose,localHit.normal);
            return new RayHit(this,localHit.distance,normal);
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

        return new Ray(o,d,ray.getMaxDistance());
    }

    /**
     * transform the ray into local space.
     * @param pose the pose of the entity
     * @param normal the normal in local space
     * @return the ray in world space
     */
    private Vector3d transformNormalToWorldSpace(PoseComponent pose,Vector3d normal) {
        Vector3d d = new Vector3d(normal);
        pose.getWorld().transform(d);
        return d;
    }

    public void reload() {
        if(myMesh!=null) {
            myMesh.setDirty(true);
        }
    }

    public void unload(GL3 gl) {
        if(myMesh!=null) {
            myMesh.unload(gl);
        }
    }
}
