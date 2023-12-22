package com.marginallyclever.ro3.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
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
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Draw each {@link MeshInstance} as a {@link Mesh}.  If the {@link MeshInstance} has a sibling {@link Material} with
 * a {@link com.jogamp.opengl.util.texture.Texture} then use it in the {@link ShaderProgram}.
 */
public class DrawMeshes implements RenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawMeshes.class);
    private int activeStatus = ALWAYS;
    private ShaderProgram shader;
    private int canvasWidth, canvasHeight;

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
        return "Meshes";
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
        unloadAllMeshes(gl3);
        shader.delete(gl3);
    }

    private void unloadAllMeshes(GL3 gl3) {
        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
                Mesh mesh = meshInstance.getMesh();
                if(mesh==null) continue;
                mesh.unload(gl3);
            }

            toScan.addAll(node.getChildren());
        }
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
        shader.set4f(gl3,"objectColor",1,1,1,1);
        shader.setVector3d(gl3,"specularColor",new Vector3d(0.5,0.5,0.5));
        shader.setVector3d(gl3,"ambientLightColor",new Vector3d(0.2,0.2,0.2));
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",1);
        shader.set1i(gl3,"diffuseTexture",0);
        OpenGLHelper.checkGLError(gl3,logger);

        // find all MeshInstance nodes in Registry
        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
                // if they have a mesh, draw it.
                Mesh mesh = meshInstance.getMesh();
                if(mesh==null) continue;

                TextureWithMetadata texture = null;
                // set the texture to the first sibling that is a material and has a texture
                Material material = meshInstance.findFirstSibling(Material.class);
                if(material!=null) {
                    if(material.getTexture()!=null) {
                        texture = material.getTexture();
                    }
                }
                if(texture == null) {
                    gl3.glDisable(GL3.GL_TEXTURE_2D);
                    shader.set1i(gl3,"useTexture",0);
                } else {
                    texture.use(shader);
                }

                // set the model matrix
                Matrix4d w = meshInstance.getWorld();
                w.transpose();
                shader.setMatrix4d(gl3,"modelMatrix",w);
                // draw it
                mesh.render(gl3);

                OpenGLHelper.checkGLError(gl3,logger);
            }

            toScan.addAll(node.getChildren());
        }
    }
}
