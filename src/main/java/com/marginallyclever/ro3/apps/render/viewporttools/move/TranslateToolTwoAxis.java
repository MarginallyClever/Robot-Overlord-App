package com.marginallyclever.ro3.apps.render.viewporttools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.viewporttools.SelectedItems;
import com.marginallyclever.ro3.apps.render.viewporttools.ViewportTool;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A tool to translate {@link Pose} nodes in the {@link Viewport}.  It translates in a given plane.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class TranslateToolTwoAxis implements ViewportTool {
    private double padSize = 1;
    private double localScale = 1;

    /**
     * The viewport to which this tool is attached.
     */
    private Viewport viewport;

    /**
     * The list of entities to adjust.
     */
    private SelectedItems selectedItems;

    /**
     * Is the user dragging the mouse after successfully picking the handle?
     */
    private boolean dragging = false;

    /**
     * The point on the translation plane where the handle was clicked.
     */
    private Point3d startPoint;

    /**
     * The plane on which the user is picking.
     */
    private final Plane translationPlane = new Plane();

    /**
     * The axes along which the user is translating.
     */
    private final Vector3d translationAxisX = new Vector3d();
    private final Vector3d translationAxisY = new Vector3d();
    private Matrix4d pivotMatrix;

    private boolean cursorOverHandle = false;
    private int frameOfReference = ViewportTool.FRAME_WORLD;
    private final ColorRGB color;

    public TranslateToolTwoAxis(ColorRGB color) {
        super();
        this.color = color;
    }

    @Override
    public void activate(List<Node> list) {
        this.selectedItems = new SelectedItems(list);
        if (selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }

    @Override
    public void deactivate() {
        dragging = false;
        selectedItems = null;
    }

    private void updatePivotMatrix() {
        setPivotMatrix(MoveUtils.getPivotMatrix(frameOfReference,viewport,selectedItems));
    }

    public void setPivotMatrix(Matrix4d pivot) {
        pivotMatrix = new Matrix4d(pivot);
        translationPlane.set(MatrixHelper.getXYPlane(pivot));
        translationAxisX.set(MatrixHelper.getXAxis(pivot));
        translationAxisY.set(MatrixHelper.getYAxis(pivot));
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if(selectedItems!=null) updatePivotMatrix();

        if (event.getID() == MouseEvent.MOUSE_MOVED) {
            mouseMoved(event);
        } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
            mouseDragged(event);
        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        cursorOverHandle = isCursorOverPad(event.getX(), event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (isCursorOverPad(event.getX(), event.getY())) {
            dragging = true;
            cursorOverHandle = true;
            startPoint = MoveUtils.getPointOnPlaneFromCursor(translationPlane,viewport,event.getX(), event.getY());
            selectedItems.savePose();
        }
    }

    public void mouseDragged(MouseEvent event) {
        if(!dragging) return;

        Point3d currentPoint = MoveUtils.getPointOnPlaneFromCursor(translationPlane,viewport,event.getX(), event.getY());
        if(currentPoint==null) return;

        Vector3d translation = new Vector3d();
        translation.sub(currentPoint, startPoint);

        // Apply the translation to the selected items
        for (Node node : selectedItems.getEntities()) {
            if(node instanceof Pose pose) {
                Matrix4d before = selectedItems.getWorldPoseAtStart(node);
                Matrix4d m = new Matrix4d(before);
                m.m03 += translation.x;
                m.m13 += translation.y;
                m.m23 += translation.z;
                pose.setWorld(m);
            }
        }
    }

    public void mouseReleased(MouseEvent event) {
        if(!dragging) return;

        dragging = false;
        if(selectedItems!=null) {
            MoveUtils.updateUndoState(selectedItems);
            selectedItems.savePose();
        }
    }

    private boolean isCursorOverPad(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = MoveUtils.getPointOnPlaneFromCursor(translationPlane,viewport,x, y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d();
        diff.sub(point, MatrixHelper.getPosition(pivotMatrix));

        double dx = diff.dot(translationAxisX);
        if( dx<0 || dx>=getPadSizeScaled() ) return false;

        double dy = diff.dot(translationAxisY);
        if( dy<0 || dy>=getPadSizeScaled() ) return false;

        return true;
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {}

    @Override
    public void update(double deltaTime) {
        updateLocalScale();
    }

    private void updateLocalScale() {
        Vector3d cameraPoint = Registry.getActiveCamera().getPosition();
        Vector3d pivotPoint = MatrixHelper.getPosition(pivotMatrix);
        pivotPoint.sub(cameraPoint);
        localScale = pivotPoint.length() * InteractionPreferences.toolScale;;
    }

    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the translation pad on the plane
        shaderProgram.set1i(gl,"useTexture",0);
        shaderProgram.set1i(gl,"useLighting",0);
        shaderProgram.set1i(gl,"useVertexColor",0);

        Matrix4d m = new Matrix4d(pivotMatrix);
        m.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m);

        float colorScale = cursorOverHandle ? 1:0.5f;
        float red   = color.red   * colorScale / 255f;
        float green = color.green * colorScale / 255f;
        float blue  = color.blue  * colorScale / 255f;
        shaderProgram.set4f(gl, "objectColor", red, green, blue, 0.5f);
        drawQuad(gl,GL3.GL_TRIANGLE_FAN);
        shaderProgram.set4f(gl, "objectColor", red, green, blue, 1.0f);
        drawQuad(gl,GL3.GL_LINE_LOOP);
    }

    private void drawQuad(GL3 gl,int mode) {
        float ps = (float)getPadSizeScaled();

        Mesh mesh = new Mesh();
        mesh.setRenderStyle(mode);
        mesh.addVertex( 0, 0,0);
        mesh.addVertex(ps, 0,0);
        mesh.addVertex(ps, ps,0);
        mesh.addVertex(0, ps,0);
        mesh.render(gl);

        mesh = new Mesh();
        mesh.setRenderStyle(mode);
        mesh.addVertex( 0, 0,0);
        mesh.addVertex(0, ps,0);
        mesh.addVertex(ps, ps,0);
        mesh.addVertex(ps, 0,0);
        mesh.render(gl);
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public boolean isInUse() {
        return dragging;
    }

    @Override
    public void cancelUse() {
        dragging = false;
    }

    @Override
    public Point3d getStartPoint() {
    	return startPoint;
    }

    private double getPadSizeScaled() {
        return padSize * localScale;
    }

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(int index) {
        frameOfReference = index;
        if(selectedItems!=null) {
            updatePivotMatrix();
        }
    }
}
