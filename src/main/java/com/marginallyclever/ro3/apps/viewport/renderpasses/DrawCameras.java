package com.marginallyclever.ro3.apps.viewport.renderpasses;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
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
    private final Mesh pyramidMesh = new Mesh();
    private final Mesh rayMesh = new Mesh();
    private ShaderProgram shader;
    private final double cameraConeRatio = 50;
    private final double coneScale = cameraConeRatio * 0.0001;


    public DrawCameras() {
        super("Cameras");
        setupMeshCone();
        setupMeshRay();
    }

    private void setupMeshRay() {
        rayMesh.setRenderStyle(GL3.GL_LINES);
        rayMesh.addVertex(0,0,0);
        rayMesh.addVertex(0,0,0);
    }

    private void setupMeshCone() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        pyramidMesh.setRenderStyle(GL3.GL_LINES);
        Vector3d a = new Vector3d(-1,-1,-1);
        Vector3d b = new Vector3d( 1,-1,-1);
        Vector3d c = new Vector3d( 1, 1,-1);
        Vector3d d = new Vector3d(-1, 1,-1);
        pyramidMesh.addVertex(0,0,0);
        pyramidMesh.addVertex((float)a.x, (float)a.y, (float)a.z);
        pyramidMesh.addVertex((float)b.x, (float)b.y, (float)b.z);
        pyramidMesh.addVertex((float)c.x, (float)c.y, (float)c.z);
        pyramidMesh.addVertex((float)d.x, (float)d.y, (float)d.z);

        pyramidMesh.addIndex(0);
        pyramidMesh.addIndex(1);
        pyramidMesh.addIndex(0);
        pyramidMesh.addIndex(2);
        pyramidMesh.addIndex(0);
        pyramidMesh.addIndex(3);
        pyramidMesh.addIndex(0);
        pyramidMesh.addIndex(4);
        pyramidMesh.addIndex(1);
        pyramidMesh.addIndex(2);
        pyramidMesh.addIndex(2);
        pyramidMesh.addIndex(3);
        pyramidMesh.addIndex(3);
        pyramidMesh.addIndex(4);
        pyramidMesh.addIndex(4);
        pyramidMesh.addIndex(1);
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
        rayMesh.unload(gl3);
        pyramidMesh.unload(gl3);
        shader.delete(gl3);
    }

    @Override
    public void draw(Viewport viewport) {
        Camera camera = viewport.getActiveCamera();
        if(camera==null) return;

        GL3 gl3 = GLContext.getCurrentGL().getGL3();
        shader.use(gl3);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix());
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        shader.setVector3d(gl3,"cameraPos",cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shader.setColor(gl3,"ambientColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        var list = Registry.selection.getList();
        var normalizedCoordinates = viewport.getCursorAsNormalized();

        for(Camera cam : Registry.cameras.getList() ) {
            boolean selected = list.contains(cam);
            if(getActiveStatus()==SOMETIMES && !selected) continue;

            // position and draw the ray from the camera.
            Matrix4d w = MatrixHelper.createIdentityMatrix4();
            shader.setMatrix4d(gl3, "modelMatrix", w);
            shader.setColor(gl3,"diffuseColor",selected ? Color.GREEN : Color.DARK_GRAY);
            Ray ray = viewport.getRayThroughPoint(cam,normalizedCoordinates.x,normalizedCoordinates.y);
            changeRayMesh(gl3, ray);
            rayMesh.render(gl3);

            // scale and draw the view cones
            shader.setColor(gl3,"diffuseColor",selected ? Color.WHITE : Color.BLACK);
            w = cam.getWorld();
            Matrix4d scale = MatrixHelper.createIdentityMatrix4();
            scale.m00 *= canvasWidth * coneScale;
            scale.m11 *= canvasHeight * coneScale;
            scale.m22 *= canvasHeight * coneScale / Math.tan(Math.toRadians(camera.getFovY()) / 2);
            w.mul(w, scale);
            w.transpose();
            shader.setMatrix4d(gl3, "modelMatrix", w);
            pyramidMesh.render(gl3);
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
