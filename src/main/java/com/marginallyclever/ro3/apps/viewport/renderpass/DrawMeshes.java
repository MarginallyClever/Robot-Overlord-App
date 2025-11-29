package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.TextureLayerIndex;
import com.marginallyclever.ro3.mesh.proceduralmesh.GenerativeMesh;
import com.marginallyclever.ro3.shader.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Draw each {@link MeshInstance} as a {@link Mesh}.  If the {@link MeshInstance} has a sibling {@link Material} with
 * a {@link com.jogamp.opengl.util.texture.Texture} then use it in the {@link ShaderProgram}.
 */
public class DrawMeshes extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawMeshes.class);
    private ShaderProgram meshShader;

    private ShaderProgram shadowShader;
    private final Mesh shadowQuad = new GenerativeMesh();
    private final int [] shadowFBO = new int[1];  // Frame Buffer Object
    private final int [] shadowTexture = new int[1];  // texture for the FBO
    private final int shadowMapUnit = 1;
    public static final int SHADOW_WIDTH = 1024;
    public static final int SHADOW_HEIGHT = 1024;

    public static final double DEPTH_BUFFER_LIMIT = Environment.SUN_DISTANCE*1.5;
    public static final Matrix4d lightProjection = new Matrix4d();
    public static final Matrix4d lightView = new Matrix4d();
    private Color sunlightColor = Color.WHITE;
    private final Vector3d sunlightSource = new Vector3d(50,150,350);
    private Color ambientColor = Color.BLACK;

    private ShaderProgram outlineShader;
    private int outlineThickness = 5;
    private final int [] stencilFBO = new int[1]; // Framebuffer for offscreen stencil rendering
    private final int [] stencilTexture = new int [1]; // Texture to capture stencil data
    private final Mesh fullScreenQuad = new GenerativeMesh();

    public DrawMeshes() {
        super("Meshes");
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "DrawMeshes.shadowQuad", shadowQuad);
        generateShadowQuad();
        generateFullscreenQuad();
        stencilFBO[0] = -1;
        stencilTexture[0] = -1;
    }

    private void generateShadowQuad() {
        shadowQuad.setRenderStyle(GL3.GL_QUADS);
        float v = 100;
        shadowQuad.addVertex(-v,-v,0);  shadowQuad.addTexCoord(0,0);
        shadowQuad.addVertex( v,-v,0);  shadowQuad.addTexCoord(1,0);
        shadowQuad.addVertex( v, v,0);  shadowQuad.addTexCoord(1,1);
        shadowQuad.addVertex(-v, v,0);  shadowQuad.addTexCoord(0,1);
    }

    private void generateFullscreenQuad() {
        float v = 1.0f;
        fullScreenQuad.setRenderStyle(GL3.GL_TRIANGLE_STRIP);
        fullScreenQuad.addVertex(-v,-v,0);  fullScreenQuad.addTexCoord(0,0);  // Bottom-left
        fullScreenQuad.addVertex( v,-v,0);  fullScreenQuad.addTexCoord(1,0);  // Bottom-right
        fullScreenQuad.addVertex(-v, v,0);  fullScreenQuad.addTexCoord(0,1);  // Top-left
        fullScreenQuad.addVertex( v, v,0);  fullScreenQuad.addTexCoord(1,1);  // Top-right
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();

        try {
            var sf = Registry.shaderFactory;
            var spf = Registry.shaderProgramFactory;
            meshShader = spf.get(Lifetime.APPLICATION,"meshShader",
                    sf.get(Lifetime.APPLICATION,GL3.GL_VERTEX_SHADER, ResourceHelper.readResource(this.getClass(),"mesh.vert")),
                    sf.get(Lifetime.APPLICATION,GL3.GL_FRAGMENT_SHADER, ResourceHelper.readResource(this.getClass(),"mesh.frag"))
            );
            shadowShader = spf.get(Lifetime.APPLICATION,"shadowShader",
                    sf.get(Lifetime.APPLICATION,GL3.GL_VERTEX_SHADER, ResourceHelper.readResource(this.getClass(),"shadow.vert")),
                    sf.get(Lifetime.APPLICATION,GL3.GL_FRAGMENT_SHADER, ResourceHelper.readResource(this.getClass(),"shadow.frag"))
            );
            outlineShader = spf.get(Lifetime.APPLICATION,"outlineShader",
                    sf.get(Lifetime.APPLICATION,GL3.GL_VERTEX_SHADER, ResourceHelper.readResource(this.getClass(),"outline_330.vert")),
                    sf.get(Lifetime.APPLICATION,GL3.GL_FRAGMENT_SHADER, ResourceHelper.readResource(this.getClass(),"outline_330.frag"))
            );
        } catch (Exception e) {
            logger.error("Failed to load shader", e);
        }

        createShadowFBOandDepthMap(gl3);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        super.reshape(glAutoDrawable, x, y, width, height);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        setupStencilFramebuffer(gl3,canvasWidth,canvasHeight);
    }

    private void setupStencilFramebuffer(GL3 gl3, int width, int height) {
        deleteStencilBuffer(gl3);
        // Create FBO if not already created
        gl3.glGenFramebuffers(1, stencilFBO, 0);
        // Create stencil texture if not created
        gl3.glGenTextures(1, stencilTexture, 0);

        gl3.glBindTexture(GL3.GL_TEXTURE_2D, stencilTexture[0]);
        // create a channel with a single 8-bit red channel for stencil data
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_R8, width, height, 0, GL3.GL_RED, GL3.GL_UNSIGNED_BYTE, null);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);

        // Bind the FBO and attach the stencil texture
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, stencilFBO[0]);
        gl3.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D, stencilTexture[0], 0);
        gl3.glDrawBuffer(GL3.GL_COLOR_ATTACHMENT0);

        // Check FBO status
        int status = gl3.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
        if (status != GL3.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Failed to setup stencil framebuffer: " + status);
        }

        // Unbind FBO
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
    }


    private void createShadowFBOandDepthMap(GL3 gl3) {
        //logger.debug("Creating shadow FBO");
        gl3.glGenFramebuffers(1, shadowFBO, 0);
        OpenGLHelper.checkGLError(gl3,logger);

        //logger.debug("Creating depth map");
        gl3.glGenTextures(1, shadowTexture,0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, shadowTexture[0]);
        gl3.glTexImage2D(GL3.GL_TEXTURE_2D,0,GL3.GL_DEPTH_COMPONENT,SHADOW_WIDTH,SHADOW_HEIGHT,0,GL3.GL_DEPTH_COMPONENT,GL3.GL_FLOAT,null);

        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MIN_FILTER,GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MAG_FILTER,GL3.GL_NEAREST);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_S,GL3.GL_CLAMP_TO_BORDER);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_T,GL3.GL_CLAMP_TO_BORDER);

        float [] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        gl3.glTexParameterfv(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_BORDER_COLOR, borderColor,0);
        OpenGLHelper.checkGLError(gl3,logger);

        //logger.debug("Binding depth map {} to shadow FBO",depthMap[0]);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, shadowFBO[0]);
        gl3.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D, shadowTexture[0], 0);
        gl3.glDrawBuffer(GL3.GL_NONE);
        gl3.glReadBuffer(GL3.GL_NONE);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER,0);
        OpenGLHelper.checkGLError(gl3,logger);
    }

    /**
     * @see <a href="https://learnopengl.com/Advanced-Lighting/Shadows/Shadow-Mapping">LearnOpenGL: Shadow Mapping</a>
     * @param camera the camera viewing the scene through the {@link Viewport} using this {@link RenderPass}.
     * @param originShift should we use origin shifting?
     */
    private void updateLightMatrix(Camera camera,boolean originShift) {
        // orthographic projection from the light's point of view
        double r = Math.max(50,camera.getOrbitRadius()*2.0);
        lightProjection.set(MatrixHelper.orthographicMatrix4d(-r,r,-r,r,1.0,DEPTH_BUFFER_LIMIT));

        Vector3d to = camera.getOrbitPoint();
        if(originShift) to.sub(MatrixHelper.getPosition(camera.getWorld()));
        Vector3d from = new Vector3d(sunlightSource);
        from.add(to);
        Vector3d up = Math.abs(sunlightSource.z)>0.99? new Vector3d(0,1,0) : new Vector3d(0,0,1);

        // look at the scene from the light's point of view
        lightView.set(lookAt(from, to, up));
        lightView.transpose();
    }

    private void updateShadowMap(GL3 gl3, List<MeshMaterialMatrix> meshes,Camera camera,boolean originShift) {
        var cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());

        // before, set up the shadow FBO
        gl3.glViewport(0,0,SHADOW_WIDTH,SHADOW_HEIGHT);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, shadowFBO[0]);
        // setup shader and viewport to depth map
        gl3.glClear(GL3.GL_DEPTH_BUFFER_BIT);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glCullFace(GL3.GL_FRONT);
        shadowShader.use(gl3);
        shadowShader.setMatrix4d(gl3, "lightProjectionMatrix", lightProjection);
        shadowShader.setMatrix4d(gl3, "lightViewMatrix", lightView);

        for(MeshMaterialMatrix meshMaterialMatrix : meshes) {
            MeshInstance meshInstance = meshMaterialMatrix.meshInstance();
            var w = meshMaterialMatrix.matrix();
            if(originShift) w = RenderPassHelper.getOriginShiftedMatrix(w,cameraWorldPos);
            shadowShader.setMatrix4d(gl3,"modelMatrix",w);
            meshInstance.getMesh().render(gl3);
        }
        // viewport scene as normal with shadow mapping (using depth map)
        gl3.glCullFace(GL3.GL_BACK);
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER,0);
        gl3.glViewport(0,0,canvasWidth,canvasHeight);
        // bind the shadow map to texture unit 1
        gl3.glActiveTexture(GL3.GL_TEXTURE0 + shadowMapUnit);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, shadowTexture[0]);
        gl3.glActiveTexture(GL3.GL_TEXTURE0);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        gl3.glDeleteFramebuffers(1, shadowFBO,0);
        gl3.glDeleteTextures(1, shadowTexture,0);

        deleteStencilBuffer(gl3);
    }

    private void deleteStencilBuffer(GL3 gl3) {
        if(stencilFBO[0]!=-1) {
            gl3.glDeleteFramebuffers(1, stencilFBO,0);
            stencilFBO[0] = -1;
        }
        if(stencilTexture[0]!=-1) {
            gl3.glDeleteTextures(1, stencilTexture,0);
            stencilTexture[0] = -1;
        }
    }

    @Override
    public void draw(Viewport viewport, GL3 gl3) {
        Camera camera = viewport.getActiveCamera();
        if (camera == null) return;

        getSunlight();

        boolean originShift = viewport.isOriginShift();

        var meshMaterial = collectAllMeshes();
        sortMeshMaterialList(meshMaterial);
        updateLightMatrix(camera,originShift);
        updateShadowMap(gl3,meshMaterial,camera,originShift);
        drawAllMeshes(gl3,meshMaterial,camera,originShift);
        //drawShadowMapOnQuad(gl3,camera,originShift);
        keepOnlySelectedMeshMaterials(meshMaterial);
        outlineSelectedMeshes(gl3,meshMaterial,camera,originShift);
    }

    private void getSunlight() {
        Environment env = Registry.getScene().findFirstChild(Environment.class);
        if(null==env) {
            env = new Environment();
            Registry.getScene().addChild(env);
        }

        sunlightSource.set(env.getSunlightSource());
        sunlightColor = env.getSunlightColor();
        ambientColor = env.getAmbientColor();
    }

    private void keepOnlySelectedMeshMaterials(List<MeshMaterialMatrix> list) {
        // remove from meshMaterial anything that is not in the list Registry.selected
        var toKeep = new ArrayList<MeshMaterialMatrix>();
        var selected = Registry.selection.getList();
        for(MeshMaterialMatrix mm : list) {
            // if node is parent of a meshInstance, keep it.
            var me = mm.meshInstance();
            var parent = me.getParent();
            if(selected.contains(parent) || selected.contains(me)) {
                toKeep.add(mm);
            }
        }
        list.retainAll(toKeep);
    }

    // sort meshMaterial list by material
    private void sortMeshMaterialList(List<MeshMaterialMatrix> meshMaterialMatrix) {
        meshMaterialMatrix.sort((o1, o2) -> {
            Material m1 = o1.material();
            Material m2 = o2.material();
            if(m1==null && m2==null) return 0;
            if(m1==null) return -1;
            if(m2==null) return 1;

            // sort opaque materials first
            var a1 = m1.getDiffuseColor().getAlpha();
            var a2 = m2.getDiffuseColor().getAlpha();
            if(a1!=255 || a2!=255) {
                return a2-a1;
            }

            // TODO sort transparent materials by distance from camera

            // sort by UniqueID so there's a consistent order
            return m1.getUniqueID().compareTo(m2.getUniqueID());
        });
    }

    // draw the shadow quad into the world for debugging.
    private void drawShadowMapOnQuad(GL3 gl3, Camera camera,boolean originShift) {
        meshShader.use(gl3);
        meshShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix(originShift));
        meshShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        var cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        meshShader.setVector3d(gl3, "cameraPos",originShift ? new Vector3d() : cameraWorldPos);  // Camera position in world space for specular lighting
        var lightPos = new Vector3d(sunlightSource);
        if(!originShift) lightPos.add(cameraWorldPos);
        meshShader.setVector3d(gl3, "lightPos", lightPos);  // Light position in world space
        meshShader.setColor(gl3, "lightColor", sunlightColor);
        meshShader.setColor(gl3, "diffuseColor", Color.WHITE);
        meshShader.setColor(gl3, "specularColor", Color.WHITE);
        meshShader.setColor(gl3,"ambientColor", ambientColor);
        meshShader.set1i(gl3, "useVertexColor", 0);
        meshShader.set1i(gl3, "useLighting", 0);
        meshShader.set1i(gl3, "useTexture",1);

        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glActiveTexture(GL3.GL_TEXTURE0 + shadowMapUnit);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D,0);

        gl3.glActiveTexture(GL3.GL_TEXTURE0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, shadowTexture[0]);

        var m = MatrixHelper.createIdentityMatrix4();
        m.setTranslation(new Vector3d(cameraWorldPos.x,cameraWorldPos.y,cameraWorldPos.z-50));
        if(originShift) m = RenderPassHelper.getOriginShiftedMatrix(m,cameraWorldPos);
        meshShader.setMatrix4d(gl3,"modelMatrix",m);
        shadowQuad.render(gl3);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }

    /**
     * find all MeshInstance nodes in the scene and the Material that is closest to the MeshInstance.
     * @return a list of MeshInstance and Material pairs.
     */
    private List<MeshMaterialMatrix> collectAllMeshes() {
        var meshMaterials = new ArrayList<MeshMaterialMatrix>();
        collAllMeshesRecursively(Registry.getScene(),meshMaterials,new Material());
        return meshMaterials;
    }

    /**
     * Recursively search the scene for MeshInstance nodes and the Material that is closest to the MeshInstance.
     * @param node the current node to search.
     * @param meshMaterialMatrices the list to add the MeshInstance and Material pairs to.
     * @param lastMaterialSeen the last Material found in the scene.
     */
    private void collAllMeshesRecursively(Node node, List<MeshMaterialMatrix> meshMaterialMatrices, Material lastMaterialSeen) {
        if (node instanceof MeshInstance meshInstance) {
            // if they have a mesh, collect it.
            Mesh mesh = meshInstance.getMesh();
            if (mesh != null && meshInstance.isActive()) {
                meshMaterialMatrices.add(new MeshMaterialMatrix(meshInstance,lastMaterialSeen,meshInstance.getWorld()));
            }
        }

        Material found = node.findFirstChild(Material.class);
        if(found != null) {
            lastMaterialSeen = found;
        }

        for(Node child : node.getChildren()) {
            collAllMeshesRecursively(child, meshMaterialMatrices,lastMaterialSeen);
        }
    }

    /**
     * Draw all the meshes in the list with the given camera.
     * @param gl3 the OpenGL context
     * @param m3 the list of {@link MeshInstance}, {@link Material}, and {@link Matrix4d} to draw.
     * @param camera
     * @param originShift
     */
    private void drawAllMeshes(GL3 gl3, List<MeshMaterialMatrix> m3, Camera camera,boolean originShift) {
        meshShader.use(gl3);
        meshShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix(originShift));
        meshShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        meshShader.setVector3d(gl3, "cameraPos",originShift ? new Vector3d() : cameraWorldPos);  // Camera position in world space

        // shadow map stuff
        meshShader.set1i(gl3, "shadowMap", shadowMapUnit);
        meshShader.setMatrix4d(gl3, "lightProjectionMatrix", lightProjection);
        meshShader.setMatrix4d(gl3, "lightViewMatrix", lightView);

        // coloring
        var lightPos = new Vector3d(sunlightSource);
        if(!originShift) lightPos.add(cameraWorldPos);
        meshShader.setVector3d(gl3, "lightPos", lightPos);  // Light position in world space
        meshShader.setColor(gl3, "lightColor", sunlightColor);
        meshShader.setColor(gl3, "diffuseColor", Color.WHITE);
        meshShader.setColor(gl3, "specularColor", Color.WHITE);
        meshShader.setColor(gl3, "ambientColor", ambientColor);

        meshShader.set1i(gl3, "useVertexColor", 0);
        meshShader.set1i(gl3, "useLighting", 1);

        //meshShader.set1i(gl3, "diffuseTexture", TextureLayerIndex.ALBEDO.getIndex());

        //OpenGLHelper.checkGLError(gl3, logger);

        Material lastSeen = null;

        for(MeshMaterialMatrix meshMaterialMatrix : m3) {
            MeshInstance meshInstance = meshMaterialMatrix.meshInstance();
            Material material = meshMaterialMatrix.material();

            // set the texture to the first sibling that is a material and has a texture
            if( material != lastSeen ) {
                lastSeen = material;
                meshShader.setColor(gl3,"diffuseColor",material.getDiffuseColor());
                meshShader.setColor(gl3,"specularColor",material.getSpecularColor());
                meshShader.setColor(gl3,"emissionColor",material.getEmissionColor());
                meshShader.set1i(gl3,"useLighting",material.isLit() ? 1 : 0);
                meshShader.set1i(gl3,"shininess",material.getShininess());
                meshShader.set1f(gl3, "specularStrength", (float)material.getSpecularStrength()*10);
                // TODO add material settings for texture filters and apply them here.
                gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MIN_FILTER,GL3.GL_LINEAR);
                gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_MAG_FILTER,GL3.GL_LINEAR);
                gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_S,GL3.GL_CLAMP_TO_BORDER);
                gl3.glTexParameteri(GL3.GL_TEXTURE_2D,GL3.GL_TEXTURE_WRAP_T,GL3.GL_CLAMP_TO_BORDER);
            }
            if(lastSeen == null) {
                gl3.glDisable(GL3.GL_TEXTURE_2D);
                meshShader.set1i(gl3,"useTexture",0);
            } else {
                //texture.use(meshShader);
                meshShader.set1i(gl3,"useTexture",1);

                try {
                    for (TextureLayerIndex tli : TextureLayerIndex.values()) {
                        int i = tli.getIndex();
                        meshShader.set1i(gl3, tli.getName(), i);
                        gl3.glActiveTexture(GL3.GL_TEXTURE0 + i);

                        var tex = lastSeen.getTexture(i);
                        if(tex!=null) {
                            tex.use(meshShader,i);
                            gl3.glBindTexture(GL3.GL_TEXTURE_2D, tex.getTexture().getTextureObject());
                        } else {
                            gl3.glBindTexture(GL3.GL_TEXTURE_2D, 0);
                        }
                        OpenGLHelper.checkGLError(gl3, logger);
                    }
                } catch(Exception e) {
                    logger.error("Failed to set texture layer indices in shader",e);
                }

            }

            Mesh mesh = meshInstance.getMesh();
            meshShader.set1i(gl3, "useVertexColor", mesh.getHasColors()?1:0);
            // set the model matrix
            var m = meshMaterialMatrix.matrix();
            if(originShift) m = RenderPassHelper.getOriginShiftedMatrix(m,cameraWorldPos);
            meshShader.setMatrix4d(gl3,"modelMatrix",m);
            // draw it
            mesh.render(gl3);
            OpenGLHelper.checkGLError(gl3,logger);
        }
    }

    private void outlineSelectedMeshes(GL3 gl3, List<MeshMaterialMatrix> selectedM3, Camera camera,boolean originShift) {
        // Step 1: Render the stencil into an offscreen texture using the FBO
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, stencilFBO[0]);
        gl3.glViewport(0, 0, canvasWidth, canvasHeight);
        // Clear stencil texture
        gl3.glClearColor(0,0,0,0);
        gl3.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        // draw all meshes in white
        meshShader.use(gl3);
        meshShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix(originShift));
        meshShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        meshShader.set1i(gl3, "useVertexColor", 0);
        meshShader.set1i(gl3, "useLighting", 0);
        meshShader.set1i(gl3, "useTexture", 0);
        meshShader.setColor(gl3,"diffuseColor",Color.WHITE);

        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());

        for(MeshMaterialMatrix meshMaterialMatrix : selectedM3) {
            MeshInstance meshInstance = meshMaterialMatrix.meshInstance();
            Mesh mesh = meshInstance.getMesh();
            var m = meshMaterialMatrix.matrix();
            if(originShift) m = RenderPassHelper.getOriginShiftedMatrix(m,cameraWorldPos);
            meshShader.setMatrix4d(gl3,"modelMatrix",m);
            // draw it
            mesh.render(gl3);
        }

        // resume editing the color buffer, do not change the depth mask or the stencil buffer.
        gl3.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);

        // Step 2: Render outlines using the stencil texture
        gl3.glActiveTexture(GL3.GL_TEXTURE0);
        gl3.glBindTexture(GL3.GL_TEXTURE_2D, stencilTexture[0]);

        //captureTextureData(gl3,canvasWidth,canvasHeight);

        outlineShader.use(gl3);
        outlineShader.set1i(gl3, "stencilTexture", 0); // Texture unit 0
        outlineShader.set2f(gl3, "canvasSize", canvasWidth, canvasHeight);
        outlineShader.setColor(gl3, "outlineColor", Color.GREEN);
        outlineShader.set1f(gl3, "outlineSize", outlineThickness);

        // Render the quad with the stencil texture to the screen
        gl3.glDisable(GL3.GL_CULL_FACE);
        fullScreenQuad.render(gl3);
        gl3.glEnable(GL3.GL_CULL_FACE);
    }

    /**
     * Capture the texture data from the active texture for potential saving or processing.
     * @param gl3 the OpenGL context
     * @param width the width of the texture
     * @param height the height of the texture
     */
    private void captureTextureData(GL3 gl3,int width,int height) {
        // Allocate a buffer to read texture data
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height);
        gl3.glGetTexImage(GL3.GL_TEXTURE_2D, 0, GL3.GL_RED, GL3.GL_UNSIGNED_BYTE, buffer);
        // Create a BufferedImage (grayscale image for single-channel data)
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        // Get the raw byte array from the BufferedImage
        byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        // Copy the buffer data into the BufferedImage
        buffer.get(imageData);
    }

    // not the same as MatrixHelper.lookAt().  This is for the light source.
    public static Matrix4d lookAt(Vector3d eye, Vector3d center, Vector3d up) {
        org.joml.Matrix4d jm = new org.joml.Matrix4d();
        jm.lookAt(eye.x,eye.y,eye.z,center.x,center.y,center.z,up.x,up.y,up.z);
        double [] list = new double[16];
        jm.get(list);
        return new Matrix4d(list);
    }
}
