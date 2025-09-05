package com.marginallyclever.ro3.apps.viewport.renderpass;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.node.nodes.environment.Environment;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * <p>Draw the background.  This may be a skybox or a solid color.</p>
 */
public class DrawBackground extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawBackground.class);
    private final ColorRGB clearColor = new ColorRGB(64,64,128);
    private ShaderProgram shader;

    public DrawBackground() {
        super("Sky/Background");
    }

    /**
     * @return the localized name
     */
    @Override
    public String getName() {
        return "Sky/Background";
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        try {
            var sf = Registry.shaderFactory;
            var spf = Registry.shaderProgramFactory;
            shader = spf.get(Lifetime.APPLICATION,"BackgroundShader",
                    sf.get(Lifetime.APPLICATION,GL3.GL_VERTEX_SHADER, ResourceHelper.readResource(this.getClass(),"/com/marginallyclever/ro3/apps/viewport/default.vert")),
                    sf.get(Lifetime.APPLICATION,GL3.GL_FRAGMENT_SHADER, ResourceHelper.readResource(this.getClass(),"/com/marginallyclever/ro3/apps/viewport/default.frag"))
            );
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
    }

    @Override
    public void draw(Viewport viewport,GL3 gl3) {
        gl3.glClearColor(clearColor.red / 255.0f,
                        clearColor.green / 255.0f,
                        clearColor.blue / 255.0f,
                        1);
        gl3.glDepthMask(true);
        gl3.glColorMask(true, true, true, true);
        gl3.glStencilMask(0xFF);

        gl3.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

        var env = Registry.getScene().findFirstChild(Environment.class);
        if(env==null) return;
        if(env.getSkyTexture() == null) return;
        if(env.getSkyMesh() == null) return;

        Camera camera = viewport.getActiveCamera();
        if (camera != null) {
            gl3.glDisable(GL3.GL_DEPTH_TEST);
            drawSkyMesh(gl3, camera, env);
            gl3.glEnable(GL3.GL_DEPTH_TEST);
        }
    }

    /**
     * Assumes camera, texture, and environment are not null.
     * @param gl3 the OpenGL context
     * @param camera the camera
     * @param env the environment (for sky texture and mesh)
     */
    private void drawSkyMesh(GL3 gl3, Camera camera, Environment env) {
        shader.use(gl3);
        Matrix4d inverseCamera = camera.getWorld();
        inverseCamera.setTranslation(new Vector3d(0,0,0));
        inverseCamera.invert();
        shader.setMatrix4d(gl3,"viewMatrix",inverseCamera);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        Vector3d cameraWorldPos = new Vector3d(0,0,0);
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"diffuseColor",Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.BLACK);
        shader.setColor(gl3,"ambientColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        shader.set1i(gl3,"useTexture",1);

        shader.setMatrix4d(gl3,"modelMatrix",MatrixHelper.createIdentityMatrix4());
        env.getSkyTexture().use(shader);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
        gl3.glDisable(GL3.GL_CULL_FACE);
        env.getSkyMesh().render(gl3);
        gl3.glEnable(GL3.GL_CULL_FACE);
    }

    public ColorRGB getClearColor() {
        return clearColor;
    }

    public void setClearColor(ColorRGB c) {
        clearColor.set(c);
    }
}
