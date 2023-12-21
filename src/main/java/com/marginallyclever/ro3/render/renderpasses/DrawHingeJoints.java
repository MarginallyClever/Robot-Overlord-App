package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class DrawHingeJoints implements RenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawHingeJoints.class);
    private int activeStatus = ALWAYS;
    private final Mesh mesh = new Mesh();
    private final Mesh circleFan = new Mesh();
    private ShaderProgram shader;
    private int canvasWidth,canvasHeight;
    private final float ringScale = 3;

    public DrawHingeJoints() {
        super();

        mesh.setRenderStyle(GL3.GL_LINES);
        mesh.addColor(1.0f,1.0f,1.0f,1);  mesh.addVertex(0,0,0);  // origin
        mesh.addColor(1.0f,1.0f,1.0f,1);  mesh.addVertex(0,0,0);  // angle unit line

        circleFan.setRenderStyle(GL3.GL_TRIANGLE_FAN);
        circleFan.addColor(1.0f,1.0f,0.0f,0.25f);
        circleFan.addVertex(0,0,0);  // origin
        for(int i=0;i<=360;++i) {
            float x = (float)Math.cos(Math.toRadians(i)) * ringScale;
            float y = (float)Math.sin(Math.toRadians(i)) * ringScale;
            circleFan.addColor(1.0f,1.0f,0.0f,0.25f);
            circleFan.addVertex(x,y,0);
        }
    }

    @Override
    public int getActiveStatus() {
        return activeStatus;
    }

    @Override
    public void setActiveStatus(int status) {
        activeStatus = status;
    }

    @Override
    public String getName() {
        return "Hinge joints";
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "mesh.vert"),
                    ResourceHelper.readResource(this.getClass(), "mesh.frag"));
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
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
        Camera camera = Registry.getActiveCamera();
        if(camera==null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setVector3d(gl3,"lightColor",new Vector3d(1,1,1));  // Light color
        shader.set4f(gl3,"objectColor",1,1,1,1);
        shader.setVector3d(gl3,"specularColor",new Vector3d(0.5,0.5,0.5));
        shader.setVector3d(gl3,"ambientLightColor",new Vector3d(0.2,0.2,0.2));
        shader.set1i(gl3,"useVertexColor",1);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        gl3.glDisable(GL3.GL_CULL_FACE);

        List<Node> toScan = new ArrayList<>();
        toScan.add(Registry.getScene());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if(node instanceof HingeJoint joint) {
                double angle = joint.getAngle()-joint.getMinAngle();
                float x = (float)Math.cos(Math.toRadians(angle)) * ringScale;
                float y = (float)Math.sin(Math.toRadians(angle)) * ringScale;
                mesh.setVertex(1,x,y,0);
                mesh.updateVertexBuffers(gl3);

                Pose pose = joint.findParent(Pose.class);
                Matrix4d w = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();
                Matrix4d rZ = new Matrix4d();
                rZ.rotZ(Math.toRadians(joint.getMinAngle()));
                w.mul(rZ);
                w.transpose();
                shader.setMatrix4d(gl3,"modelMatrix",w);
                mesh.render(gl3);
                int range = Math.max(0, (int)(joint.getMaxAngle()-joint.getMinAngle()) );
                circleFan.render(gl3,1+range,0);
            }
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glEnable(GL3.GL_CULL_FACE);
    }
}
