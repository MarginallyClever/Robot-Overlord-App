package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

public class CircleXY extends Mesh {
    public CircleXY() {
        super();
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLE_FAN);

        setRenderStyle(GL3.GL_TRIANGLE_FAN);
        addVertex(0,0,0);  // origin
        for(int i=0;i<=360;++i) {
            float x = (float)Math.cos(Math.toRadians(i));
            float y = (float)Math.sin(Math.toRadians(i));
            addVertex(x,y,0);
        }
    }
}
