package com.marginallyclever.ro3.apps.render.renderpasses;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * <p>Draw the background.  This may be a skybox or a solid color.</p>
 * <p>TODO <a href="https://antongerdelan.net/opengl/cubemaps.html">use the OpenGL cube map texture</a>?</p>
 */
public class DrawBackground extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawBackground.class);
    private final ColorRGB eraseColor = new ColorRGB(64,64,128);
    private ShaderProgram shader;
    private final Mesh mesh = new Mesh();
    private final TextureWithMetadata texture;

    public DrawBackground() {
        super("Erase/Background");

        // build a box
        mesh.setRenderStyle(GL3.GL_QUADS);

        float adj = 1f/256f;
        float a=0.00f+adj;
        float b=0.25f;
        float c=0.50f;
        float d=0.75f;
        float e=1.00f-adj;

        float f=1f/3f+adj*3;
        float g=2f/3f-adj*3;
        int v = 100;
        // build the top face (z+)
        mesh.addTexCoord(b,g);  mesh.addVertex(-v, v, v);
        mesh.addTexCoord(c,g);  mesh.addVertex( v, v, v);
        mesh.addTexCoord(c,e);  mesh.addVertex( v,-v, v);
        mesh.addTexCoord(b,e);  mesh.addVertex(-v,-v, v);
        // build the bottom face (z-)
        mesh.addTexCoord(b,a);  mesh.addVertex(-v, v, -v);
        mesh.addTexCoord(c,a);  mesh.addVertex( v, v, -v);
        mesh.addTexCoord(c,f);  mesh.addVertex( v,-v, -v);
        mesh.addTexCoord(b,f);  mesh.addVertex(-v,-v, -v);
        // build north face (y+)
        mesh.addTexCoord(b,g);  mesh.addVertex(-v, v,  v);
        mesh.addTexCoord(c,g);  mesh.addVertex( v, v,  v);
        mesh.addTexCoord(c,f);  mesh.addVertex( v, v, -v);
        mesh.addTexCoord(b,f);  mesh.addVertex(-v, v, -v);
        // build south face (y-)
        mesh.addTexCoord(e,g);  mesh.addVertex(-v, -v,  v);
        mesh.addTexCoord(d,g);  mesh.addVertex( v, -v,  v);
        mesh.addTexCoord(d,f);  mesh.addVertex( v, -v, -v);
        mesh.addTexCoord(e,f);  mesh.addVertex(-v, -v, -v);

        // build east face (x+)
        mesh.addTexCoord(d,g);  mesh.addVertex(v, -v,  v);
        mesh.addTexCoord(c,g);  mesh.addVertex(v,  v,  v);
        mesh.addTexCoord(c,f);  mesh.addVertex(v,  v, -v);
        mesh.addTexCoord(d,f);  mesh.addVertex(v, -v, -v);
        // build west face (x-)
        mesh.addTexCoord(a,g);  mesh.addVertex(-v, -v,  v);
        mesh.addTexCoord(b,g);  mesh.addVertex(-v,  v,  v);
        mesh.addTexCoord(b,f);  mesh.addVertex(-v,  v, -v);
        mesh.addTexCoord(a,f);  mesh.addVertex(-v, -v, -v);

        texture = Registry.textureFactory.load("/skybox/skybox.png");
        texture.setDoNotExport(true);
    }

    /**
     * @return the localized name
     */
    @Override
    public String getName() {
        return "Erase/Background";
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
        mesh.unload(gl3);
        shader.delete(gl3);
    }

    @Override
    public void draw(Viewport viewport) {
        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        gl3.glClearColor(eraseColor.red / 255.0f,
                eraseColor.green / 255.0f,
                eraseColor.blue / 255.0f,
                1);
        gl3.glDepthMask(true);
        gl3.glColorMask(true, true, true, true);
        gl3.glStencilMask(0xFF);

        Camera camera = Registry.getActiveCamera();
        if (camera == null) {
            gl3.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
        } else {
            gl3.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
            gl3.glDisable(GL3.GL_DEPTH_TEST);
            drawSkybox(gl3, camera);
            gl3.glEnable(GL3.GL_DEPTH_TEST);
        }
    }

    private void drawSkybox(GL3 gl3, Camera camera) {
        shader.use(gl3);
        Matrix4d inverseCamera = camera.getWorld();
        inverseCamera.setTranslation(new Vector3d(0,0,0));
        inverseCamera.invert();
        inverseCamera.transpose();
        shader.setMatrix4d(gl3,"viewMatrix",inverseCamera);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        Vector3d cameraWorldPos = new Vector3d(0,0,0);
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"objectColor",Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.BLACK);
        shader.setColor(gl3,"ambientLightColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        shader.set1i(gl3,"useTexture",1);
        gl3.glDisable(GL3.GL_CULL_FACE);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);

        shader.setMatrix4d(gl3,"modelMatrix",MatrixHelper.createIdentityMatrix4());
        if(texture!=null) {
            texture.use(shader);
        }
        mesh.render(gl3);

        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT);
        gl3.glEnable(GL3.GL_CULL_FACE);
    }

    public ColorRGB getEraseColor() {
        return eraseColor;
    }

    public void setEraserColor(ColorRGB c) {
        eraseColor.set(c);
    }
}
