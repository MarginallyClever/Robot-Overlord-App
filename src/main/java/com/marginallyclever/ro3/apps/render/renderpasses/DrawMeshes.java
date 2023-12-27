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
    private ShaderProgram meshShader, shadowShader;
    private final Mesh shadowQuad = new Mesh();
    private final int [] shadowFBO = new int[1];  // Frame Buffer Object
    private final int [] depthMap = new int[1];  // texture for the FBO
    private final int shadowMapUnit = 1;
    public static final int SHADOW_WIDTH = 1024;
    public static final int SHADOW_HEIGHT = 1024;
    public static final Vector3d fromLightUnit = new Vector3d(0,0,-1);

    public DrawMeshes() {
        super("Meshes");

        shadowQuad.setRenderStyle(GL3.GL_QUADS);
        float v = 100;
        shadowQuad.addVertex(-v,-v,0);  shadowQuad.addTexCoord(0,0);
        shadowQuad.addVertex( v,-v,0);  shadowQuad.addTexCoord(1,0);
        shadowQuad.addVertex( v, v,0);  shadowQuad.addTexCoord(1,1);
        shadowQuad.addVertex(-v, v,0);  shadowQuad.addTexCoord(0,1);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();

        try {
            meshShader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "mesh.vert"),
                    ResourceHelper.readResource(this.getClass(), "mesh.frag"));
        } catch (Exception e) {
            logger.error("Failed to load shader", e);
        }

        try {
            shadowShader = new ShaderProgram(gl3,
                    ResourceHelper.readResource(this.getClass(), "shadow.vert"),
                    ResourceHelper.readResource(this.getClass(), "shadow.frag"));
        } catch (Exception e) {
            logger.error("Failed to load shader", e);
        }

        createShadowFBOandDepthMap(gl3);
    }

    private void createShadowFBOandDepthMap(GL3 gl3) {
        logger.debug("Creating shadow FBO");
        gl3.glGenFramebuffers(1, shadowFBO, 0);
        OpenGLHelper.checkGLError(gl3,logger);

        logger.debug("Creating depth map");
        gl3.glGenTextures(1, depthMap,0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, depthMap[0]);
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D,0,GL3.GL_DEPTH_COMPONENT,SHADOW_WIDTH,SHADOW_HEIGHT,0,GL3.GL_DEPTH_COMPONENT,GL3.GL_FLOAT,null);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MIN_FILTER,GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MAG_FILTER,GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_S,GL3.GL_CLAMP_TO_BORDER);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_T,GL3.GL_CLAMP_TO_BORDER);
        float [] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        gl3.glTexParameterfv(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_BORDER_COLOR, borderColor,0);
        OpenGLHelper.checkGLError(gl3,logger);
        logger.debug("Depth map created {}",depthMap[0]);

        logger.debug("Binding depth map to shadow FBO");
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, shadowFBO[0]);
        gl3.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, depthMap[0], 0);
        gl3.glDrawBuffer(GL3.GL_NONE);
        gl3.glReadBuffer(GL3.GL_NONE);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER,0);
        OpenGLHelper.checkGLError(gl3,logger);
    }

    private void generateDepthMap(GL3 gl3, List<MeshInstance> meshes) {
        // before, set up the shadow FBO
        gl3.glViewport(0,0,SHADOW_WIDTH,SHADOW_HEIGHT);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, shadowFBO[0]);
        gl3.glClear(GL3.GL_DEPTH_BUFFER_BIT);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glCullFace(GL3.GL_FRONT);
        // setup shader and render to depth map
        shadowShader.use(gl3);
        shadowShader.setMatrix4d(gl3,"lightSpaceMatrix", getLightSpaceMatrix());

        for(MeshInstance meshInstance : meshes) {
            Mesh mesh = meshInstance.getMesh();
            Matrix4d w = meshInstance.getWorld();
            w.transpose();
            shadowShader.setMatrix4d(gl3,"modelMatrix",w);
            mesh.render(gl3);
        }
        // render scene as normal with shadow mapping (using depth map)
        gl3.glCullFace(GL3.GL_BACK);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER,0);
        gl3.glViewport(0,0,canvasWidth,canvasHeight);
        // bind the shadow map to texture unit 1
        gl3.glActiveTexture(GL3.GL_TEXTURE0 + shadowMapUnit);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D,depthMap[0]);
        gl3.glActiveTexture(GL3.GL_TEXTURE0);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        unloadAllMeshes(gl3);
        meshShader.delete(gl3);
        shadowShader.delete(gl3);
        shadowQuad.unload(gl3);

        gl3.glDeleteFramebuffers(1, shadowFBO,0);
        gl3.glDeleteTextures(1, depthMap,0);
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
        if (camera == null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        List<MeshInstance> meshes = collectAllMeshes();
        generateDepthMap(gl3,meshes);
        drawAllMeshes(gl3,meshes,camera);
        //drawShadowQuad(gl3,camera);
    }

    private void drawShadowQuad(GL3 gl3, Camera camera) {
        meshShader.use(gl3);
        meshShader.set1i(gl3,"shadowMap",shadowMapUnit);
        meshShader.setMatrix4d(gl3, "lightSpaceMatrix", getLightSpaceMatrix());

        meshShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix());
        meshShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        meshShader.setVector3d(gl3, "cameraPos", cameraWorldPos);  // Camera position in world space
        meshShader.setVector3d(gl3, "lightPos", cameraWorldPos);  // Light position in world space

        meshShader.setColor(gl3, "lightColor", Color.WHITE);
        meshShader.setColor(gl3, "objectColor", Color.WHITE);
        meshShader.setColor(gl3, "specularColor", Color.WHITE);
        meshShader.setColor(gl3,"ambientColor",Color.BLACK);

        meshShader.set1i(gl3, "useVertexColor", 0);
        meshShader.set1i(gl3, "useLighting", 0);
        meshShader.set1i(gl3, "useTexture",1);

        gl3.glActiveTexture(GL3.GL_TEXTURE0 + shadowMapUnit);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D,0);
        gl3.glActiveTexture(GL3.GL_TEXTURE0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D,depthMap[0]);


        Matrix4d w = MatrixHelper.createIdentityMatrix4();
        //w.rotY(Math.PI/2);
        w.setTranslation(new Vector3d(0,0,-20));
        w.transpose();
        meshShader.setMatrix4d(gl3,"modelMatrix",w);
        shadowQuad.render(gl3);
    }

    // find all MeshInstance nodes in Registry
    private List<MeshInstance> collectAllMeshes() {
        List<MeshInstance> meshes = new ArrayList<>();

        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);
            toScan.addAll(node.getChildren());

            if (node instanceof MeshInstance meshInstance) {
                // if they have a mesh, draw it.
                Mesh mesh = meshInstance.getMesh();
                if (mesh != null) meshes.add(meshInstance);
            }
        }
        return meshes;
    }

    private void drawAllMeshes(GL3 gl3, List<MeshInstance> meshes, Camera camera) {
        meshShader.use(gl3);
        meshShader.set1i(gl3,"shadowMap",shadowMapUnit);
        meshShader.setMatrix4d(gl3, "lightSpaceMatrix", getLightSpaceMatrix());

        meshShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix());
        meshShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        meshShader.setVector3d(gl3, "cameraPos", cameraWorldPos);  // Camera position in world space
        meshShader.setVector3d(gl3, "lightPos", cameraWorldPos);  // Light position in world space

        meshShader.setColor(gl3, "lightColor", Color.WHITE);
        meshShader.setColor(gl3, "objectColor", Color.WHITE);
        meshShader.setColor(gl3, "specularColor", Color.WHITE);
        meshShader.setColor(gl3,"ambientColor",Color.LIGHT_GRAY);

        meshShader.set1i(gl3, "useVertexColor", 0);
        meshShader.set1i(gl3, "useLighting", 1);
        meshShader.set1i(gl3, "diffuseTexture", 0);
        OpenGLHelper.checkGLError(gl3, logger);

        for(MeshInstance meshInstance : meshes) {
            Mesh mesh = meshInstance.getMesh();

            TextureWithMetadata texture = null;
            // set the texture to the first sibling that is a material and has a texture
            Material material = meshInstance.findFirstSibling(Material.class);
            if(material!=null) {
                if(material.getTexture()!=null) {
                    texture = material.getTexture();
                }
                meshShader.setColor(gl3,"objectColor",material.getDiffuseColor());
                meshShader.setColor(gl3,"specularColor",material.getSpecularColor());
                meshShader.set1i(gl3,"useLighting",material.isLit() ? 1 : 0);
                meshShader.set1i(gl3,"shininess",material.getShininess());
            } else {
                meshShader.setColor(gl3,"objectColor",Color.WHITE);
                meshShader.setColor(gl3,"specularColor",Color.WHITE);
                meshShader.set1i(gl3,"useLighting",1);
                meshShader.set1i(gl3,"shininess",32);
            }
            if(texture == null) {
                gl3.glDisable(GL3.GL_TEXTURE_2D);
                meshShader.set1i(gl3,"useTexture",0);
            } else {
                texture.use(meshShader);
            }

            // set the model matrix
            Matrix4d w = meshInstance.getWorld();
            w.transpose();
            meshShader.setMatrix4d(gl3,"modelMatrix",w);
            // draw it
            mesh.render(gl3);

            OpenGLHelper.checkGLError(gl3,logger);
        }
    }

    // https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping
    private Matrix4d getLightSpaceMatrix() {
        // orthographic projection from the light's point of view
        Matrix4d lightProjection = MatrixHelper.orthographicMatrix4d(-10,10,-10,10,1.0,100.0);
        // look at the scene from the light's point of view
        Matrix4d lightView = MatrixHelper.lookAt(fromLightUnit,  // from
                                                new Vector3d(0,0,0),  // to
                                                new Vector3d(0.0f, 1.0f,  0.0f));  // up
        // combine the two
        Matrix4d lightSpaceMatrix = new Matrix4d();
        lightSpaceMatrix.mul(lightProjection,lightView);
        return lightSpaceMatrix;
    }
}
