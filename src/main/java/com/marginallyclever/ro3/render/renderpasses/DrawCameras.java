package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Draws each {@link Camera} as a pyramid approximating the perspective view frustum.
 */
public class DrawCameras implements RenderPass {
    private int activeStatus = ALWAYS;
    private final Mesh mesh = new Mesh();

    public DrawCameras() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        mesh.setRenderStyle(GL3.GL_LINES);
        Vector3d a = new Vector3d(-1,-1,-1);
        Vector3d b = new Vector3d( 1,-1,-1);
        Vector3d c = new Vector3d( 1, 1,-1);
        Vector3d d = new Vector3d(-1, 1,-1);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
    }

    /**
     * @return NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public int getActiveStatus() {
        return activeStatus;
    }

    /**
     * @param status NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public void setActiveStatus(int status) {
        activeStatus = status;
    }

    /**
     * @return the localized name of this overlay
     */
    @Override
    public String getName() {
        return "Cameras";
    }

    @Override
    public void draw(ShaderProgram shader) {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.set1f(gl3,"useVertexColor",1);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"useTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        for(Camera camera : Registry.cameras.getList() ) {
            // set modelView to world
            Matrix4d w = camera.getWorld();
            w.transpose();
            shader.setMatrix4d(gl3,"modelMatrix",w);
            mesh.render(gl3);
        }

        shader.set1f(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",1);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
