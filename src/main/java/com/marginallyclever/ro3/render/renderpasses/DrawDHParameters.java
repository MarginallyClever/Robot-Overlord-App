package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.DHParameter;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws each {@link DHParameter} as two lines from the previous joint to the current joint.
 */
public class DrawDHParameters implements RenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawDHParameters.class);
    private int activeStatus = ALWAYS;
    private final Mesh mesh = new Mesh();
    private ShaderProgram shader;
    private int canvasWidth,canvasHeight;

    public DrawDHParameters() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        mesh.setRenderStyle(GL3.GL_LINES);
        // line d
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
        // line r
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);
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
        return "DH Parameters";
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(),"dhParameter.vert"),
                    ResourceHelper.readResource(this.getClass(),"dhParameter.frag"));
        } catch(IOException e) {
            logger.error("Failed to create default shader.",e);
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        shader.delete(gl3);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height)  {
        canvasWidth = width;
        canvasHeight = height;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void draw() {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        shader.use(gl3);
        Camera camera = Registry.cameras.getList().get(0);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());

        List<Node> toScan = new ArrayList<>();
        toScan.add(Registry.getScene());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if(node instanceof DHParameter parameter) {
                // set modelView to world
                Pose parentPose = parameter.findParent(Pose.class);
                Matrix4d w = (parentPose==null)
                    ? MatrixHelper.createIdentityMatrix4()
                    : parentPose.getWorld();
                w.transpose();
                shader.setMatrix4d(gl3,"modelMatrix",w);
                shader.set1f(gl3,"d",(float)parameter.getD());
                shader.set1f(gl3,"theta",(float)parameter.getTheta());
                shader.set1f(gl3,"r",(float)parameter.getR());
                //myShader.set1f(gl3,"alpha",(float)parameter.getAlpha());
                mesh.render(gl3);
            }
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
