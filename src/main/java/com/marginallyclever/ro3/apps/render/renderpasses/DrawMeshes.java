package com.marginallyclever.ro3.apps.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Draw each {@link MeshInstance} as a {@link Mesh}.  If the {@link MeshInstance} has a sibling {@link Material} with
 * a {@link com.jogamp.opengl.util.texture.Texture} then use it in the {@link ShaderProgram}.
 */
public class DrawMeshes extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawMeshes.class);
    private ShaderProgram shader;

    public DrawMeshes() {
        super("Meshes");
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
    public void draw(Viewport viewport) {
        Camera camera = Registry.getActiveCamera();
        if(camera==null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space

        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"objectColor",Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.WHITE);
        shader.setColor(gl3,"ambientLightColor",Color.BLACK);

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
                    shader.setColor(gl3,"objectColor",material.getDiffuseColor());
                    shader.setColor(gl3,"specularColor",material.getSpecularColor());
                    shader.setColor(gl3,"ambientLightColor",material.getAmbientColor());
                    shader.set1i(gl3,"useLighting",material.isLit() ? 1 : 0);
                    shader.set1i(gl3,"shininess",material.getShininess());
                } else {
                    shader.setColor(gl3,"objectColor",Color.WHITE);
                    shader.setColor(gl3,"specularColor",Color.WHITE);
                    shader.setColor(gl3,"ambientLightColor",Color.BLACK);
                    shader.set1i(gl3,"useLighting",1);
                    shader.set1i(gl3,"shininess",32);
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
