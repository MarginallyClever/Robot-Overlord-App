package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.shader.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.CircleXY;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.LinearJoint;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Draw a ring around each hinge joint to show the range of motion.</p>
 * <p>Draw a line to show the current angle.</p>
 */
public class DrawJoints extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawJoints.class);

    private final float ringScale = 3;

    private final Mesh currentAngleMesh = new Mesh();
    private final Mesh circleFanMesh = new CircleXY();
    private final Mesh linearRangeMesh = new Mesh();
    private ShaderProgram shader;
    private final Material defaultMaterial = new Material();

    public DrawJoints() {
        super("Joints");
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "DrawJoints.currentAngleMesh", currentAngleMesh);
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "DrawJoints.circleFanMesh", circleFanMesh);
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "DrawJoints.linearRangeMesh", linearRangeMesh);
        defaultMaterial.setLit(false);

        currentAngleMesh.setRenderStyle(GL3.GL_LINES);
        currentAngleMesh.addColor(1.0f,1.0f,1.0f,1);  currentAngleMesh.addVertex(0,0,0);  // origin
        currentAngleMesh.addColor(1.0f,1.0f,1.0f,1);  currentAngleMesh.addVertex(1,0,0);  // angle unit line

        linearRangeMesh.setRenderStyle(GL3.GL_LINES);
        linearRangeMesh.addColor(1.0f,1.0f,1.0f,1);  linearRangeMesh.addVertex(0,0,0);  // origin
        linearRangeMesh.addColor(1.0f,1.0f,1.0f,1);  linearRangeMesh.addVertex(0,0,1);  // angle unit line
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        try {
            var sf = Registry.shaderFactory;
            var spf = Registry.shaderProgramFactory;
            shader = spf.get(Lifetime.APPLICATION,"jointShader",
                    sf.get(Lifetime.APPLICATION,GL3.GL_VERTEX_SHADER, ResourceHelper.readResource(this.getClass(),"/com/marginallyclever/ro3/apps/viewport/default.vert")),
                    sf.get(Lifetime.APPLICATION,GL3.GL_FRAGMENT_SHADER, ResourceHelper.readResource(this.getClass(),"/com/marginallyclever/ro3/apps/viewport/default.frag"))
            );
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
    }

    @Override
    public void draw(Viewport viewport, GL3 gl3) {
        Camera camera = viewport.getActiveCamera();
        if(camera==null) return;

        boolean originShift = viewport.isOriginShift();
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());

        shader.use(gl3);
        defaultMaterial.use(gl3,shader);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix(originShift));
        shader.setVector3d(gl3,"cameraPos",originShift ? new Vector3d() : cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",originShift ? new Vector3d() : cameraWorldPos);  // Light position in world space
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_CULL_FACE);

        var list = Registry.selection.getList();

        var toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.removeFirst();
            toScan.addAll(node.getChildren());

            if( getActiveStatus() == SOMETIMES && !list.contains(node) ) continue;

            boolean isSelected = list.contains(node);
            if(node instanceof HingeJoint hinge) {
                renderHinge(gl3,hinge,originShift,cameraWorldPos,isSelected);
            } else if(node instanceof LinearJoint linear) {
                renderLinear(gl3,linear,originShift,cameraWorldPos,isSelected);
            }
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glEnable(GL3.GL_CULL_FACE);
    }

    private void renderLinear(GL3 gl3, LinearJoint joint, boolean originShift, Vector3d cameraWorldPos, boolean isSelected) {
        // make brighter if selected
        double min = joint.getMinPosition();
        double range = joint.getMaxPosition()-min;

        Pose pose = joint.findParent(Pose.class);
        Matrix4d world = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();
        Matrix4d modelMatrix = MatrixHelper.createIdentityMatrix4();
        modelMatrix.setTranslation(new Vector3d(0,0,min));
        modelMatrix.mul(world,modelMatrix);
        modelMatrix.mul(MatrixHelper.createScaleMatrix4(range));
        if(originShift) modelMatrix = RenderPassHelper.getOriginShiftedMatrix(modelMatrix,cameraWorldPos);
        shader.setColor(gl3,"diffuseColor",new Color(255,255,0,isSelected ? 255 : 64));
        shader.setMatrix4d(gl3,"modelMatrix",modelMatrix);
        linearRangeMesh.render(gl3);
    }

    private void renderHinge(GL3 gl3, HingeJoint joint,boolean originShift,Vector3d cameraWorldPos,boolean isSelected) {
        // make bigger if selected
        double scale = ringScale * (isSelected ? 2 : 1);

        // adjust the position of the mesh based on the joint's minimum angle.
        Pose pose = joint.findParent(Pose.class);
        Matrix4d world = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();

        Matrix4d modelMatrix = new Matrix4d();
        // draw the range fan
        modelMatrix.rotZ(Math.toRadians(joint.getMinAngle()-joint.getAngle()));
        modelMatrix.mul(world,modelMatrix);
        modelMatrix.mul(modelMatrix,MatrixHelper.createScaleMatrix4(scale));
        shader.setColor(gl3,"diffuseColor",new Color(255,255,0,isSelected ? 128 : 64));
        if(originShift) modelMatrix = RenderPassHelper.getOriginShiftedMatrix(modelMatrix,cameraWorldPos);
        shader.setMatrix4d(gl3,"modelMatrix",modelMatrix);
        int range = Math.max(0, (int)(joint.getMaxAngle()-joint.getMinAngle()) );
        circleFanMesh.render(gl3,0,1+range);

        // draw the current angle line
        modelMatrix.rotZ(0);
        modelMatrix.mul(world,modelMatrix);
        modelMatrix.mul(modelMatrix,MatrixHelper.createScaleMatrix4(scale));
        shader.setColor(gl3,"diffuseColor",new Color(255,255,255,isSelected ? 255 : 64));
        if(originShift) modelMatrix = RenderPassHelper.getOriginShiftedMatrix(modelMatrix,cameraWorldPos);
        shader.setMatrix4d(gl3,"modelMatrix",modelMatrix);
        currentAngleMesh.render(gl3);

        int v = (int)joint.getVelocity();
        if(v!=0) {
            int vAbs = Math.abs(v);
            if(v>0) v=0;
            modelMatrix.rotZ(Math.toRadians(joint.getAngle()+v));
            modelMatrix.mul(world, modelMatrix);
            modelMatrix.mul(modelMatrix, MatrixHelper.createScaleMatrix4(scale));
            shader.setColor(gl3, "diffuseColor", new Color(255, 0, 0, isSelected ? 255 : 64));
            if(originShift) modelMatrix = RenderPassHelper.getOriginShiftedMatrix(modelMatrix,cameraWorldPos);
            shader.setMatrix4d(gl3,"modelMatrix",modelMatrix);
            circleFanMesh.render(gl3, 0, 1+vAbs);
        }
    }
}
