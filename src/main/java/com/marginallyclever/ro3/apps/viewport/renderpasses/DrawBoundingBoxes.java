package com.marginallyclever.ro3.apps.viewport.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.mesh.AABB;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;

/**
 * Draw the bounding box of each {@link MeshInstance} in the scene.
 */
public class DrawBoundingBoxes extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawBoundingBoxes.class);
    private ShaderProgram shader;
    private final Mesh mesh = new Mesh();

    public DrawBoundingBoxes() {
        super("Bounding Boxes");

        mesh.setRenderStyle(GL3.GL_LINES);
        // add 8 points of a unit cube centered on the origin
        mesh.addVertex(-0.5f, 0.5f, 0.5f);
        mesh.addVertex( 0.5f, 0.5f, 0.5f);
        mesh.addVertex( 0.5f,-0.5f, 0.5f);
        mesh.addVertex(-0.5f,-0.5f, 0.5f);
        mesh.addVertex(-0.5f, 0.5f,-0.5f);
        mesh.addVertex( 0.5f, 0.5f,-0.5f);
        mesh.addVertex( 0.5f,-0.5f,-0.5f);
        mesh.addVertex(-0.5f,-0.5f,-0.5f);
        // add index values
        mesh.addIndex(0);        mesh.addIndex(1);
        mesh.addIndex(1);        mesh.addIndex(2);
        mesh.addIndex(2);        mesh.addIndex(3);
        mesh.addIndex(3);        mesh.addIndex(0);

        mesh.addIndex(4);        mesh.addIndex(5);
        mesh.addIndex(5);        mesh.addIndex(6);
        mesh.addIndex(6);        mesh.addIndex(7);
        mesh.addIndex(7);        mesh.addIndex(4);

        mesh.addIndex(0);        mesh.addIndex(4);
        mesh.addIndex(1);        mesh.addIndex(5);
        mesh.addIndex(2);        mesh.addIndex(6);
        mesh.addIndex(3);        mesh.addIndex(7);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        try {
            shader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/viewport/default.vert"),
                    ResourceHelper.readResource(this.getClass(), "/com/marginallyclever/ro3/apps/viewport/default.frag"));
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
        Camera camera = viewport.getActiveCamera();
        if(camera==null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.GRAY);
        shader.setColor(gl3,"ambientColor",new Color(255/5,255/5,255/5,255));
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        shader.set1i(gl3,"useTexture",0);
        OpenGLHelper.checkGLError(gl3,logger);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        gl3.glDisable(GL3.GL_DEPTH_TEST);

        Color unselected = new Color(255,255,255,64);
        Color selected = new Color(226, 115, 42,128);

        var list = Registry.selection.getList();
        var toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if(!(node instanceof MeshInstance meshInstance)) continue;
            if(getActiveStatus()==SOMETIMES && !list.contains(meshInstance)) continue;

            // if they have a mesh, draw it.
            Mesh mesh2 = meshInstance.getMesh();
            if(mesh2==null) continue;

            AABB boundingBox = mesh2.getBoundingBox();
            Point3d max = boundingBox.getBoundsTop();
            Point3d min = boundingBox.getBoundsBottom();
            mesh.setVertex(0, min.x, max.y, max.z);
            mesh.setVertex(1, max.x, max.y, max.z);
            mesh.setVertex(2, max.x, min.y, max.z);
            mesh.setVertex(3, min.x, min.y, max.z);
            mesh.setVertex(4, min.x, max.y, min.z);
            mesh.setVertex(5, max.x, max.y, min.z);
            mesh.setVertex(6, max.x, min.y, min.z);
            mesh.setVertex(7, min.x, min.y, min.z);
            mesh.updateVertexBuffers(gl3);

            // set the model matrix
            Matrix4d w = meshInstance.getWorld();
            w.transpose();
            shader.setMatrix4d(gl3,"modelMatrix",w);

            // highlight selected items
            shader.setColor(gl3,"diffuseColor", list.contains(meshInstance) ? selected : unselected );
            // draw it
            mesh.render(gl3);
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
