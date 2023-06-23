package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Sphere;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.preferences.InteractionPreferences;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A tool for moving entities along a single axis.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class TranslateEntityToolOneAxis implements EditorTool {
    private final double handleLength = 5;
    private final double gripRadius = 0.5;
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
     * The axis along which the user is translating.
     */
    private final Vector3d translationAxis = new Vector3d();

    private Matrix4d pivotMatrix;

    private boolean cursorOverHandle = false;

    private final Sphere handleSphere = new Sphere();
    private int frameOfReference = EditorTool.FRAME_WORLD;
    private final ColorRGB color;

    public TranslateEntityToolOneAxis(ColorRGB color) {
        super();
        this.color = color;
    }

    @Override
    public void activate(List<Entity> list) {
        this.selectedItems = new SelectedItems(list);
        if(selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }

    public void setPivotMatrix(Matrix4d pivot) {
        pivotMatrix = new Matrix4d(pivot);
        translationPlane.set(MatrixHelper.getXYPlane(pivot));
        translationAxis.set(MatrixHelper.getXAxis(pivot));
    }

    @Override
    public void deactivate() {
        dragging = false;
        selectedItems = null;
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_MOVED) {
            mouseMoved(event);
        } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED && dragging) {
            mouseDragged(event);
        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        cursorOverHandle = isCursorOverHandle(event.getX(), event.getY());
    }

    public void mousePressed(MouseEvent event) {
        if (isCursorOverHandle(event.getX(), event.getY())) {
            dragging = true;
            cursorOverHandle = true;
            startPoint = EditorUtils.getPointOnPlaneFromCursor(translationPlane,viewport,event.getX(), event.getY());
            selectedItems.savePose();
        }
    }

    public void mouseDragged(MouseEvent event) {
        if(!dragging) return;

        Point3d currentPoint = EditorUtils.getPointOnPlaneFromCursor(translationPlane,viewport,event.getX(), event.getY());
        if(currentPoint==null) return;

        Point3d nearestPoint = getNearestPointOnAxis(currentPoint);

        Vector3d translation = new Vector3d();
        translation.sub(nearestPoint, startPoint);

        // Apply the translation to the selected items
        for (Entity entity : selectedItems.getEntities()) {
            Matrix4d before = selectedItems.getWorldPoseAtStart(entity);
            Matrix4d pose = new Matrix4d(before);
            pose.m03 += translation.x;
            pose.m13 += translation.y;
            pose.m23 += translation.z;
            entity.getComponent(PoseComponent.class).setWorld(pose);
        }
    }

    public void mouseReleased(MouseEvent event) {
        if(!dragging) return;

        dragging = false;
        if(selectedItems!=null) {
            EditorUtils.updateUndoState(selectedItems);
            selectedItems.savePose();
        }
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

    private void updatePivotMatrix() {
        setPivotMatrix(EditorUtils.getPivotMatrix(frameOfReference,viewport,selectedItems));
    }

    private Point3d getNearestPointOnAxis(Point3d currentPoint) {
        // get the cross product of the translationAxis and the translationPlane's normal
        Vector3d orthogonal = new Vector3d();
        orthogonal.cross(translationAxis, translationPlane.normal);
        orthogonal.normalize();
        Vector3d diff = new Vector3d();
        diff.sub(currentPoint,startPoint);
        double d = diff.dot(orthogonal);
        // remove the component of diff that is orthogonal to the translationAxis
        orthogonal.scale(d);
        diff.sub(orthogonal);
        diff.add(startPoint);

        return new Point3d(diff);
    }

    private boolean isCursorOverHandle(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = EditorUtils.getPointOnPlaneFromCursor(translationPlane,viewport,x, y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d(translationAxis);
        diff.scaleAdd(getHandleLengthScaled(), MatrixHelper.getPosition(pivotMatrix));
        diff.sub(point);
        return (diff.lengthSquared() < getGripRadiusScaled()*getGripRadiusScaled());
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {
        // Handle keyboard events, if necessary
    }

    @Override
    public void update(double deltaTime) {
        // Update the tool's state, if necessary
        if(selectedItems!=null) updatePivotMatrix();

        updateLocalScale();
    }

    private void updateLocalScale() {
        Vector3d cameraPoint = viewport.getCamera().getPosition();
        Vector3d pivotPoint = MatrixHelper.getPosition(pivotMatrix);
        pivotPoint.sub(cameraPoint);
        localScale = pivotPoint.length() * InteractionPreferences.toolScale.get();
    }

    // Render the translation handle on the axis
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        shaderProgram.set1i(gl, "useTexture", 0);
        shaderProgram.set1i(gl, "useLighting", 0);
        shaderProgram.set1i(gl, "useVertexColor", 0);

        float colorScale = cursorOverHandle ? 1:0.5f;
        float red   = color.red   * colorScale / 255f;
        float green = color.green * colorScale / 255f;
        float blue  = color.blue  * colorScale / 255f;
        shaderProgram.set4f(gl,"objectColor",red, green, blue, 1.0f);

        drawHandleOnAxis(gl, shaderProgram);
    }

    private void drawHandleOnAxis(GL3 gl, ShaderProgram shaderProgram) {
        Matrix4d m = new Matrix4d(pivotMatrix);
        m.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m);

        // handle line
        Mesh mesh = new Mesh(GL3.GL_LINES);
        mesh.addVertex(0, 0, 0);
        mesh.addVertex((float)getHandleLengthScaled(), 0, 0);
        mesh.render(gl);

        // ball at end of handle
        Matrix4d m2 = MatrixHelper.createIdentityMatrix4();
        m2.m03 += getHandleLengthScaled();
        m2.mul(pivotMatrix,m2);
        m2.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m2);
        handleSphere.radius.set(getGripRadiusScaled());
        handleSphere.render(gl);
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


    private double getHandleLengthScaled() {
        return handleLength * localScale;
    }

    private double getGripRadiusScaled() {
        return gripRadius * localScale;
    }
}
