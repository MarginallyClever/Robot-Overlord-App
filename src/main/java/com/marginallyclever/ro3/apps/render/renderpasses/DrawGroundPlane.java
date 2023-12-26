package com.marginallyclever.ro3.apps.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Draw the ground plane.
 */
public class DrawGroundPlane extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawGroundPlane.class);
    private final Mesh mesh = MatrixHelper.createMesh(1.0);
    private ShaderProgram shader;

    public DrawGroundPlane() {
        super("Ground plane");

        mesh.setRenderStyle(GL3.GL_LINES);
        int v = 1000;
        int stepSize=100;
        for(int s=-v;s<v;s+=stepSize) {
            mesh.addVertex(s, -v, 0);
            mesh.addVertex(s,  v, 0);
            mesh.addVertex( -v,s, 0);
            mesh.addVertex(  v,s, 0);
        }
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "default.vert"),
                    ResourceHelper.readResource(this.getClass(), "default.frag"));
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        mesh.unload(gl3);
        shader.delete(gl3);
    }

    @Override
    public void draw(Viewport viewport) {
        Camera camera = Registry.getActiveCamera();
        if(camera==null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"objectColor",new Color(255,255,255,8));
        shader.setColor(gl3,"specularColor",Color.GRAY);
        shader.setColor(gl3,"ambientColor",new Color(32,32,32,255));
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        Matrix4d w = MatrixHelper.createIdentityMatrix4();
        w.transpose();
        shader.setMatrix4d(gl3,"modelMatrix",w);
        mesh.render(gl3);

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
