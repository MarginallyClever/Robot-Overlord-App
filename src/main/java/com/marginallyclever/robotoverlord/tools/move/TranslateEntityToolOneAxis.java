package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Sphere;
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

    private boolean hovering = false;

    private Sphere handleSphere = new Sphere();

    @Override
    public void activate(List<Entity> list) {
        this.selectedItems = new SelectedItems(list);
        if(selectedItems.isEmpty()) return;

        setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));
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
        hovering = isCursorOverHandle(event.getX(), event.getY());
    }

    public void mousePressed(MouseEvent event) {
        if (isCursorOverHandle(event.getX(), event.getY())) {
            dragging = true;
            hovering = true;
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
            EditorUtils.updateUndoState(this,selectedItems);
            selectedItems.savePose();
        }
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
        diff.scaleAdd(handleLength, MatrixHelper.getPosition(pivotMatrix));
        diff.sub(point);
        return (diff.lengthSquared() < gripRadius*gripRadius);
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {
        // Handle keyboard events, if necessary
    }

    @Override
    public void update(double deltaTime) {
        // Update the tool's state, if necessary
        if(selectedItems!=null) setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));

    }

    @Override
    public void render(GL2 gl2) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the translation handle on the axis
        boolean texture = OpenGLHelper.disableTextureStart(gl2);
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        gl2.glPushMatrix();

        MatrixHelper.applyMatrix(gl2, pivotMatrix);

        float [] colors = new float[4];
        gl2.glGetFloatv(GL2.GL_CURRENT_COLOR, colors, 0);
        double colorScale = hovering? 1:0.5;
        gl2.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 1.0);

        gl2.glBegin(GL2.GL_LINES);
        gl2.glVertex3d(0, 0, 0);
        gl2.glVertex3d(handleLength, 0, 0);
        gl2.glEnd();

        gl2.glTranslated(handleLength, 0, 0);

        gl2.glScaled(gripRadius, gripRadius, gripRadius);
        handleSphere.render(gl2);

        gl2.glPopMatrix();

        OpenGLHelper.disableLightingEnd(gl2, light);
        OpenGLHelper.disableTextureEnd(gl2, texture);
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
}
