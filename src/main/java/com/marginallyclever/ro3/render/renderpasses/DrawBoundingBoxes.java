package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.AABB;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Draw the bounding box of each {@link MeshInstance} in the scene.
 */
public class DrawBoundingBoxes implements RenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawBoundingBoxes.class);
    private int activeStatus = ALWAYS;
    private ShaderProgram shader;

    private final Mesh mesh = new Mesh();
    private int canvasWidth, canvasHeight;

    public DrawBoundingBoxes() {
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
        return "Bounding Boxes";
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
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
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
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setVector3d(gl3,"lightColor",new Vector3d(1,1,1));  // Light color
        shader.set4f(gl3,"objectColor",1,1,1,0.25f);
        shader.setVector3d(gl3,"specularColor",new Vector3d(0.5,0.5,0.5));
        shader.setVector3d(gl3,"ambientLightColor",new Vector3d(0.2,0.2,0.2));
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        shader.set1i(gl3,"useTexture",0);
        OpenGLHelper.checkGLError(gl3,logger);
        gl3.glDisable(GL3.GL_TEXTURE_2D);
        gl3.glDisable(GL3.GL_DEPTH_TEST);

        // find all MeshInstance nodes in Registry
        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
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
                //Pose pose = meshInstance.findParent(Pose.class);
                //Matrix4d w = (pose==null) ? MatrixHelper.createIdentityMatrix4() : pose.getWorld();

                Matrix4d w = meshInstance.getWorld();
                w.transpose();
                shader.setMatrix4d(gl3,"modelMatrix",w);
                // draw it
                mesh.render(gl3);

                OpenGLHelper.checkGLError(gl3,logger);
            }

            toScan.addAll(node.getChildren());
        }
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }
}
