package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

/**
 * <p>{@link CircleXY} is a {@link Mesh} in the XY plane.  The first vertex is the center so that it can be
 * drawn as a triangle fan.</p>
 */
public class CircleXY extends ProceduralMesh {
    public CircleXY() {
        super();
    }

    @Override
    public String getEnglishName() {
        return "CircleXY";
    }

    @Override
    public void updateModel() {
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLE_FAN);

        setRenderStyle(GL3.GL_TRIANGLE_FAN);
        addVertex(0,0,0);  // origin
        for(int i=0;i<=360;++i) {
            float x = (float)Math.cos(Math.toRadians(i));
            float y = (float)Math.sin(Math.toRadians(i));
            addVertex(x,y,0);
        }

        fireMeshChanged();
    }
}
