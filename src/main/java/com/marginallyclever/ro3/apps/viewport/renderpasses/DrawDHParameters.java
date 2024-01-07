package com.marginallyclever.ro3.apps.viewport.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.DHParameter;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws each {@link DHParameter} as two lines from the previous joint to the current joint.
 */
public class DrawDHParameters extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawDHParameters.class);
    private final Mesh mesh = new Mesh();
    private ShaderProgram shader;

    public DrawDHParameters() {
        super("DH Parameters");

        // add mesh to a list that can be unloaded and reloaded as needed.
        mesh.setRenderStyle(GL3.GL_LINES);
        // line d
        mesh.addColor(0,0,1,1);        mesh.addVertex(0,0,0);
        mesh.addColor(0,0,1,1);        mesh.addVertex(0,0,0);
        // line r
        mesh.addColor(1,0,0,1);        mesh.addVertex(0,0,0);
        mesh.addColor(1,0,0,1);        mesh.addVertex(0,0,0);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/viewport/default.vert"),
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/viewport/default.frag"));
        } catch(IOException e) {
            logger.error("Failed to create default shader.",e);
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
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        shader.use(gl3);
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"objectColor",Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shader.setColor(gl3,"ambientColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",1);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        shader.set1i(gl3,"useTexture",0);
        OpenGLHelper.checkGLError(gl3,logger);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        gl3.glDisable(GL3.GL_DEPTH_TEST);

        var list = Registry.selection.getList();

        List<Node> toScan = new ArrayList<>();
        toScan.add(Registry.getScene());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if(!(node instanceof DHParameter parameter)) continue;
            if(getActiveStatus()==SOMETIMES && !list.contains(parameter)) continue;

            shader.setColor(gl3,"objectColor",list.contains(parameter) ? Color.WHITE : Color.GRAY);

            double d = parameter.getD();
            mesh.setVertex(1,0,0,d);
            mesh.setVertex(2,0,0,d);
            double s = Math.sin(Math.toRadians(parameter.getTheta()));
            double c = Math.cos(Math.toRadians(parameter.getTheta()));
            double r = parameter.getR();
            mesh.setVertex(3,c*r,s*r,d);
            mesh.updateVertexBuffers(gl3);

            // set modelView to world
            Pose parentPose = parameter.findParent(Pose.class);
            Matrix4d w = (parentPose==null) ? MatrixHelper.createIdentityMatrix4() : parentPose.getWorld();
            w.transpose();
            shader.setMatrix4d(gl3,"modelMatrix",w);
            mesh.render(gl3);
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
