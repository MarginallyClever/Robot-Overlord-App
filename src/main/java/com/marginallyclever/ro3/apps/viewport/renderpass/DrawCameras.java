package com.marginallyclever.ro3.apps.viewport.renderpass;

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
    private final Mesh frustumMesh = new Mesh();
    private final Mesh rayMesh = new Mesh();
    private ShaderProgram shader;
    private final Color DARK_GREEN = new Color(0,64,0,128);


    public DrawCameras() {
        super("Cameras");
        setupMeshFrustum();
        setupMeshRay();
    }

    private void setupMeshRay() {
        rayMesh.setRenderStyle(GL3.GL_LINES);
        rayMesh.addVertex(0,0,0);
        rayMesh.addVertex(0,0,0);
    }

    private void setupMeshFrustum() {
        // add mesh to a list that can be unloaded and reloaded as needed.
        frustumMesh.setRenderStyle(GL3.GL_LINES);
        // add lines to form the edges of a box.  top circle, bottom circle, and the lines connecting them.
        // bottom
        frustumMesh.addVertex(-2,-2,-2);
        frustumMesh.addVertex( 2,-2,-2);
        frustumMesh.addVertex( 2, 2,-2);
        frustumMesh.addVertex(-2, 2,-2);
        // top
        frustumMesh.addVertex(-1,-1, -1);
        frustumMesh.addVertex( 1,-1, -1);
        frustumMesh.addVertex( 1, 1, -1);
        frustumMesh.addVertex(-1, 1, -1);
        // connecting lines
        frustumMesh.addIndex(0);        frustumMesh.addIndex(1);
        frustumMesh.addIndex(1);        frustumMesh.addIndex(2);
        frustumMesh.addIndex(2);        frustumMesh.addIndex(3);
        frustumMesh.addIndex(3);        frustumMesh.addIndex(0);

        frustumMesh.addIndex(4);        frustumMesh.addIndex(5);
        frustumMesh.addIndex(5);        frustumMesh.addIndex(6);
        frustumMesh.addIndex(6);        frustumMesh.addIndex(7);
        frustumMesh.addIndex(7);        frustumMesh.addIndex(4);

        frustumMesh.addIndex(0);        frustumMesh.addIndex(4);
        frustumMesh.addIndex(1);        frustumMesh.addIndex(5);
        frustumMesh.addIndex(2);        frustumMesh.addIndex(6);
        frustumMesh.addIndex(3);        frustumMesh.addIndex(7);
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
        frustumMesh.unload(gl3);
        shader.delete(gl3);
    }

    @Override
    public void draw(Viewport viewport) {
        Camera camera = viewport.getActiveCamera();
        if(camera==null) return;

        boolean originShift = viewport.isOriginShift();
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        GL3 gl3 = GLContext.getCurrentGL().getGL3();

        shader.use(gl3);
        shader.setMatrix4d(gl3,"projectionMatrix",camera.getChosenProjectionMatrix(canvasWidth,canvasHeight));
        shader.setMatrix4d(gl3,"viewMatrix",camera.getViewMatrix(originShift));
        shader.setVector3d(gl3,"cameraPos",originShift ? new Vector3d() : cameraWorldPos);  // Camera position in world space
        shader.setVector3d(gl3,"lightPos",originShift ? new Vector3d() : cameraWorldPos);  // Light position in world space
        shader.setColor(gl3,"lightColor", Color.WHITE);
        shader.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shader.setColor(gl3,"ambientColor",Color.BLACK);
        shader.set1i(gl3,"useVertexColor",0);
        shader.set1i(gl3,"useLighting",0);
        shader.set1i(gl3,"useTexture",0);
        shader.set1i(gl3,"diffuseTexture",0);
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        gl3.glDisable(GL3.GL_TEXTURE_2D);

        var list = Registry.selection.getList();
        var normalizedCoordinates = viewport.getCursorAsNormalized();

        for( Camera otherCamera : Registry.cameras.getList() ) {
            boolean selected = list.contains(otherCamera);
            if( getActiveStatus() == SOMETIMES && !selected ) continue;
            Matrix4d w = otherCamera.getWorld();
            if(originShift) w = RenderPassHelper.getOriginShiftedMatrix(w,cameraWorldPos);
            shader.setMatrix4d(gl3, "modelMatrix", w);

            // the frustum (outer limits of the camera's view)
            changeFrustumMesh(gl3,otherCamera);
            shader.setColor(gl3,"diffuseColor",selected ? Color.WHITE : Color.BLACK);
            frustumMesh.render(gl3);

            // the pick ray through the cursor, out the camera, and into the scene, in world space.
            // Should always be inside the frustum.
            Ray ray = viewport.getRayThroughPointUntransformed(otherCamera,normalizedCoordinates.x,normalizedCoordinates.y);
            changeRayMesh(gl3, ray,otherCamera.getFarZ());
            shader.setColor(gl3,"diffuseColor",selected ? Color.GREEN : DARK_GREEN);
            rayMesh.render(gl3);
        }

        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }

    /**
     * Update the frustum mesh to match the camera's view.
     * @param gl3 the OpenGL context
     * @param camera the camera to draw
     */
    private void changeFrustumMesh(GL3 gl3, Camera camera) {
        if(camera.getDrawOrthographic()) {
            // orthographic
            float far = (float)camera.getFarZ();
            float near = (float)camera.getNearZ();
            float h = canvasHeight/2.0f;
            float w = canvasWidth/2.0f;

            // bottom
            frustumMesh.setVertex(0,-w,-h,-far);
            frustumMesh.setVertex(1, w,-h,-far);
            frustumMesh.setVertex(2, w, h,-far);
            frustumMesh.setVertex(3,-w, h,-far);
            // top
            frustumMesh.setVertex(4,-w,-h,-near);
            frustumMesh.setVertex(5, w,-h,-near);
            frustumMesh.setVertex(6, w, h,-near);
            frustumMesh.setVertex(7,-w, h,-near);
        } else {
            // perspective
            float far = (float)camera.getFarZ();
            float near = (float)camera.getNearZ();
            float aspectRatio = (float)canvasWidth / canvasHeight;
            double ratio = Math.tan(Math.toRadians(camera.getFovY()) / 2.0);// Assuming FoV is in radians

            // Calculate half heights and widths of the near and far planes
            float hNear = (float)(ratio * near);
            float wNear = hNear * aspectRatio;
            float hFar = (float)(ratio * far);
            float wFar = hFar * aspectRatio;

            // bottom
            frustumMesh.setVertex(0,-wFar,-hFar,-far);
            frustumMesh.setVertex(1, wFar,-hFar,-far);
            frustumMesh.setVertex(2, wFar, hFar,-far);
            frustumMesh.setVertex(3,-wFar, hFar,-far);
            // top
            frustumMesh.setVertex(4,-wNear,-hNear, -near);
            frustumMesh.setVertex(5, wNear,-hNear, -near);
            frustumMesh.setVertex(6, wNear, hNear, -near);
            frustumMesh.setVertex(7,-wNear, hNear, -near);
        }
        frustumMesh.updateVertexBuffers(gl3);
    }

    private void changeRayMesh(GL3 gl3, Ray ray,double farZ) {
        Point3d origin = ray.origin();
        Vector3d direction = ray.direction();

        direction.scale(farZ);
        rayMesh.setVertex(0,origin.x,origin.y,origin.z);
        rayMesh.setVertex(1,origin.x+direction.x,origin.y+direction.y,origin.z+direction.z);
        rayMesh.updateVertexBuffers(gl3);
    }
}
