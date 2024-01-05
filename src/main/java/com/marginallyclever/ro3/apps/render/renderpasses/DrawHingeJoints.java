package com.marginallyclever.ro3.apps.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.mesh.shapes.CircleXY;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawHingeJoints extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawHingeJoints.class);
    private final Mesh currentAngleMesh = new Mesh();
    private final Mesh circleFanMesh = new CircleXY();
    private ShaderProgram shader;
    private final float ringScale = 3;

    public DrawHingeJoints() {
        super("Hinge Joints");

        currentAngleMesh.setRenderStyle(GL3.GL_LINES);
        currentAngleMesh.addColor(1.0f,1.0f,1.0f,1);  currentAngleMesh.addVertex(0,0,0);  // origin
        currentAngleMesh.addColor(1.0f,1.0f,1.0f,1);  currentAngleMesh.addVertex(1,0,0);  // angle unit line
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/render/default.vert"),
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/render/default.frag"));
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        currentAngleMesh.unload(gl3);
        circleFanMesh.unload(gl3);
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
        shader.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shader.setColor(gl3,"ambientColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        gl3.glDisable(GL3.GL_CULL_FACE);

        var list = Registry.selection.getList();

        var toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if(!(node instanceof HingeJoint joint)) continue;
            if(getActiveStatus()==SOMETIMES && !list.contains(joint)) continue;

            // make bigger if selected
            double scale = ringScale;
            if(list.contains(joint)) scale*=2;

            // adjust the position of the mesh based on the joint's minimum angle.
            Pose pose = joint.findParent(Pose.class);
            Matrix4d w = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();

            Matrix4d rZ = new Matrix4d();
            rZ.rotZ(Math.toRadians(joint.getMinAngle()));
            w.mul(rZ);
            w.mul(w,MatrixHelper.createScaleMatrix4(scale));
            w.transpose();
            shader.setColor(gl3,"objectColor",new Color(255,255,0,64));
            shader.setMatrix4d(gl3,"modelMatrix",w);
            // draw the range fan
            int range = Math.max(0, (int)(joint.getMaxAngle()-joint.getMinAngle()) );
            circleFanMesh.render(gl3,0,1+range);

            // draw the current angle line
            w = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();
            rZ.rotZ(Math.toRadians(joint.getAngle()));
            w.mul(rZ);
            w.mul(w,MatrixHelper.createScaleMatrix4(scale));
            w.transpose();
            shader.setColor(gl3,"objectColor",Color.WHITE);
            shader.setMatrix4d(gl3,"modelMatrix",w);
            currentAngleMesh.render(gl3);
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glEnable(GL3.GL_CULL_FACE);
    }
}
