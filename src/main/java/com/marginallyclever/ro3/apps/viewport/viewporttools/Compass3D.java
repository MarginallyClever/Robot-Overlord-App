package com.marginallyclever.ro3.apps.viewport.viewporttools;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.shapes.CircleXY;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.texture.TextureWithMetadata;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * <p>Draw a compass to show the orientation of the camera.  If the user clicks on the handles for the compasss,
 * orbit the camera to face that world axis.</p>
 */
public class Compass3D implements ViewportTool {
    /**
     * The viewport to which this tool is attached.
     */
    private Viewport viewport;
    private final int compassRadius = 50;
    private final int handleLength = 30;
    private final int handleRadius = 8;
    private final Mesh gizmoMesh = MatrixHelper.createMesh();
    private final Mesh circleMesh = new CircleXY();
    private final Mesh quadMesh = new Mesh();
    private final TextureWithMetadata texture;
    private int handleUnderCursor = -1;
    private final Point3d [] handleList = new Point3d[] {
            new Point3d( handleLength,0,0),  // x+
            new Point3d(-handleLength,0,0),  // x-
            new Point3d(0, handleLength,0),  // y+
            new Point3d(0,-handleLength,0),  // y-
            new Point3d(0,0, handleLength),  // z+
            new Point3d(0,0,-handleLength)  // z-
    };

    public Compass3D() {
        super();
        texture = Registry.textureFactory.load("/com/marginallyclever/ro3/apps/viewport/viewporttools/axisLetters.png");
        texture.setDoNotExport(true);

        createQuadMesh();
    }

    private void createQuadMesh() {
        quadMesh.setRenderStyle(GL3.GL_QUADS);
        int gridSize = 4;
        float step = 1.0f / gridSize;

        for (int j = 0; j < gridSize; j++) {
            float v = j * step;
            for (int i = 0; i < gridSize; i++) {
                float u = i * step;

                // Add vertices
                quadMesh.addVertex(-1, -1, 0);
                quadMesh.addVertex(1, -1, 0);
                quadMesh.addVertex(1, 1, 0);
                quadMesh.addVertex(-1, 1, 0);

                // Add texture coordinates
                quadMesh.addTexCoord(u, v);
                quadMesh.addTexCoord(u + step, v);
                quadMesh.addTexCoord(u + step, v + step);
                quadMesh.addTexCoord(u, v + step);
            }
        }
    }

    @Override
    public void activate(List<Node> list) {}

    @Override
    public void deactivate() {}

    @Override
    public void handleMouseEvent(MouseEvent event) {
        handleUnderCursor = getHandleUnderCursor(event);
        if (event.getID() == MouseEvent.MOUSE_CLICKED && event.getClickCount()==1 ) {
            turnCameraAccordingToHandle();
        }
    }

    @Override
    public void update(double deltaTime) {}

    @Override
    public void render(GL3 gl3, ShaderProgram shaderProgram) {
        shaderProgram.setColor(gl3,"lightColor", Color.WHITE);
        shaderProgram.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shaderProgram.setColor(gl3,"ambientColor",Color.BLACK);
        shaderProgram.set1i(gl3,"useLighting",0);
        shaderProgram.set1i(gl3,"diffuseTexture",0);

        // set the projection matrix such that the drawing area is the top right corner of the viewport.
        double w = viewport.getWidth()/2d;
        double h = viewport.getHeight()/2d;
        double px = -w + compassRadius;
        double py = -h + compassRadius;
        Matrix4d projection = MatrixHelper.orthographicMatrix4d( px-w, px+w, py-h, py+h, -compassRadius*2, compassRadius*2);
        shaderProgram.setMatrix4d(gl3,"projectionMatrix",projection);

        // set the view matrix to be the inverse of the camera matrix and without translation.
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        Matrix4d view = camera.getWorld();
        Vector3d z = MatrixHelper.getZAxis(view);
        z.scale(-compassRadius*1.5);

        view.setTranslation(new Vector3d(0,0,0));
        view.invert();
        view.transpose();
        shaderProgram.setMatrix4d(gl3,"viewMatrix",view);

        gl3.glClear(GL3.GL_DEPTH_BUFFER_BIT);
        gl3.glEnable(GL3.GL_DEPTH_TEST);

        drawWhiteCircle(gl3,shaderProgram,z);

        // for the gizmo, set the model matrix to be the identity matrix.
        Matrix4d model = MatrixHelper.createScaleMatrix4(handleLength-handleRadius);
        model.transpose();
        shaderProgram.setColor(gl3,"objectColor",Color.WHITE);
        shaderProgram.setMatrix4d(gl3,"modelMatrix",model);
        // and use vertex colors.
        shaderProgram.set1i(gl3,"useVertexColor",1);
        gizmoMesh.render(gl3);

        // for the handles, do not use vertex color.
        shaderProgram.set1i(gl3,"useVertexColor",0);

        drawHandle(gl3,shaderProgram,new Vector3d( handleLength,0,0),12, 0);  // x+
        drawHandle(gl3,shaderProgram,new Vector3d(-handleLength,0,0),14, 1);  // x-
        drawHandle(gl3,shaderProgram,new Vector3d(0, handleLength,0), 8, 2);  // y+
        drawHandle(gl3,shaderProgram,new Vector3d(0,-handleLength,0),10, 3);  // y-
        drawHandle(gl3,shaderProgram,new Vector3d(0,0, handleLength), 4, 4);  // z+
        drawHandle(gl3,shaderProgram,new Vector3d(0,0,-handleLength), 6, 5);  // z-
    }

    private void drawWhiteCircle(GL3 gl3, ShaderProgram shaderProgram, Vector3d z) {
        // draw the circle when the cursor is over the compass.
        Camera camera = Registry.getActiveCamera();
        assert camera != null;

        double outerRadius = handleLength + handleRadius + 2;
        Point2d c = viewport.getCursorPosition();
        Point2d center = new Point2d(viewport.getWidth()-compassRadius,compassRadius);
        if(center.distanceSquared(c) < outerRadius*outerRadius) {
            // for the background circle, set the model matrix to be the camera matrix so the handle is always facing the camera.
            Matrix4d model = camera.getWorld();
            model.setTranslation(z);
            model.mul(model, MatrixHelper.createScaleMatrix4(handleLength + handleRadius + 2));
            model.transpose();
            shaderProgram.setMatrix4d(gl3, "modelMatrix", model);
            shaderProgram.set1i(gl3, "useVertexColor", 0);
            shaderProgram.setColor(gl3, "objectColor", new Color(255, 255, 255, 64));
            circleMesh.render(gl3);
        }
    }

    private void drawHandle(GL3 gl3, ShaderProgram shaderProgram, Vector3d offset,int tileIndex,int handleIndex) {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;

        // set the model matrix to be the camera matrix so the handle is always facing the camera.
        Matrix4d model = camera.getWorld();
        model.setTranslation(offset);
        model.mul(model, MatrixHelper.createScaleMatrix4(handleRadius));
        model.transpose();
        shaderProgram.setMatrix4d(gl3,"modelMatrix",model);

        if(handleUnderCursor==handleIndex) tileIndex++;

        texture.use(shaderProgram);
        quadMesh.render(gl3,tileIndex*4,4);
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public boolean isInUse() {
        return false;
    }

    @Override
    public void cancelUse() {}

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {}

    /**
     * Test if the cursor is over one of the handles.
     * @param event the mouse event
     * @return the index of the handle that the cursor is over, or -1 if none.
     */
    private int getHandleUnderCursor(MouseEvent event) {
        int cx = viewport.getWidth()-compassRadius;
        int cy = compassRadius;

        var origin = new Point3d(event.getX()-cx,cy-event.getY(),-compassRadius*1.5);
        var direction = new Vector3d(0,0,1);

        // set the view matrix to be the inverse of the camera matrix and without translation.
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        Matrix4d view = camera.getWorld();
        view.setTranslation(new Vector3d(0,0,0));
        //view.invert();
        //view.transpose();

        view.transform(origin);
        view.transform(direction);
        //System.out.println("origin="+origin+" direction="+direction);

        var ray = new Ray(origin,direction);
        // there are six spheres to test against.
        double nearest = Double.MAX_VALUE;
        int nearestIndex = -1;
        double d;
        for(int i=0;i<6;++i) {
            d = IntersectionHelper.raySphere(ray,handleList[i],handleRadius);
            if(d>=0 && d<nearest) {
                nearest = d;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    @Override
    public void mousePressed(MouseEvent event) {}

    @Override
    public void mouseDragged(MouseEvent event) {}

    @Override
    public void mouseReleased(MouseEvent event) {}

    /**
     * Turn the camera to face the direction of the handle under the cursor.
     */
    public void turnCameraAccordingToHandle() {
        if(handleUnderCursor>=0 && handleUnderCursor<6) {
            System.out.println("clicked on handle "+handleUnderCursor);
            Camera camera = Registry.getActiveCamera();
            assert camera != null;
            Matrix4d world = camera.getWorld();
            Vector3d orbit = camera.getOrbitPoint();
            world.rotX(Math.toRadians(90));
            Matrix4d rot = new Matrix4d();

            switch(handleUnderCursor) {
                case 0:  rot.rotZ(Math.toRadians(90));  break;
                case 1:  rot.rotZ(Math.toRadians(-90)); break;
                case 2:  rot.rotZ(Math.toRadians(180)); break;
                case 3:  rot.setIdentity();  break;
                case 4:  rot.rotX(Math.toRadians(-90)); break;
                case 5:  rot.rotX(Math.toRadians(90));  break;
            }
            world.mul(rot,world);

            Vector3d t = MatrixHelper.getZAxis(world);
            t.scaleAdd(camera.getOrbitRadius(),orbit);
            world.setTranslation(t);
            camera.setWorld(world);
        }
    }

    @Override
    public void setFrameOfReference(int index) {}

    @Override
    public void init(GL3 gl3) {}

    @Override
    public void dispose(GL3 gl3) {
        texture.unload();
        circleMesh.unload(gl3);
        gizmoMesh.unload(gl3);
        quadMesh.unload(gl3);
    }
}
