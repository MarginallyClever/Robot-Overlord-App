package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.RayHit;
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
 */
@ComponentDependency(components={PoseComponent.class, MaterialComponent.class})
@Deprecated
public class ShapeComponent extends RenderComponent {
    public ShapeComponent() {
        super();
    }

    public void setModel(Mesh m) {    }

    public Mesh getModel() {
        return null;
    }

    public void render(GL3 gl) {    }

    public void unload(GL3 gl) {    }
}
