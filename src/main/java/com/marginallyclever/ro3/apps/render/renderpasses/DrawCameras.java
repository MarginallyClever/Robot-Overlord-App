package com.marginallyclever.ro3.apps.render.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Draws each {@link Camera} as a pyramid approximating the perspective view frustum.
 */
public class DrawCameras extends AbstractRenderPass {
    private static final Logger logger = LoggerFactory.getLogger(DrawCameras.class);
    private final Mesh mesh = new Mesh();
    private final Mesh rayMesh = new Mesh();
    private ShaderProgram shader;
    private final double cameraConeRatio = 50;

    public DrawCameras() {
        super("Cameras");
        setupMeshCone();
        setupMeshRay();
    }

    private void setupMeshRay() {
        rayMesh.setRenderStyle(GL3.GL_LINES);
        rayMesh.addColor(1,1,1,1);        rayMesh.addVertex(0,0,0);
        rayMesh.addColor(1,1,1,1);        rayMesh.addVertex(0,0,0);
    }

    private void setupMeshCone() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        mesh.setRenderStyle(GL3.GL_LINES);
        Vector3d a = new Vector3d(-1,-1,-1);
        Vector3d b = new Vector3d( 1,-1,-1);
        Vector3d c = new Vector3d( 1, 1,-1);
        Vector3d d = new Vector3d(-1, 1,-1);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex(0,0,0);        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)d.x, (float)d.y, (float)d.z);
        mesh.addColor(0,0,0,1);        mesh.addVertex((float)a.x, (float)a.y, (float)a.z);
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
        rayMesh.unload(gl3);
        mesh.unload(gl3);
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
        shader.setColor(gl3,"objectColor",Color.GREEN);
        shader.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shader.setColor(gl3,"ambientColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",1);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        double coneScale = cameraConeRatio *0.0001;
        var normalizedCoordinates = viewport.getCursorAsNormalized();

        // position and draw the ray from the camera.
        Matrix4d w = MatrixHelper.createIdentityMatrix4();
        w.transpose();
        shader.setMatrix4d(gl3, "modelMatrix", w);
        for(Camera cam : Registry.cameras.getList() ) {
            Ray ray = viewport.getRayThroughPoint(cam,normalizedCoordinates.x,normalizedCoordinates.y);
            changeRayMesh(gl3, ray);
            rayMesh.render(gl3);
        }

        // scale and draw the view cones
        for(Camera cam : Registry.cameras.getList() ) {
            w = cam.getWorld();
            Matrix4d scale = new Matrix4d();
            scale.setIdentity();
            scale.m00 *= canvasWidth * coneScale;
            scale.m11 *= canvasHeight * coneScale;
            scale.m22 *= canvasHeight * coneScale / Math.tan(Math.toRadians(camera.getFovY()) / 2);
            w.mul(w, scale);
            w.transpose();
            shader.setMatrix4d(gl3, "modelMatrix", w);
            mesh.render(gl3);
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }

    private void changeRayMesh(GL3 gl3, Ray ray) {
        Point3d origin = ray.getOrigin();
        Vector3d direction = ray.getDirection();

        direction.scale(1000);
        rayMesh.setVertex(0,origin.x,origin.y,origin.z);
        rayMesh.setVertex(1,direction.x,direction.y,direction.z);
        rayMesh.updateVertexBuffers(gl3);
    }
}
