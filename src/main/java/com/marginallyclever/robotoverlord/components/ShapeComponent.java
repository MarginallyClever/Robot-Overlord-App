package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

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
