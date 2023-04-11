package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.*;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

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
     * The origin of the translation plane.
     */
    private Point3d origin;

    /**
     * The plane on which the user is picking.
     */
    private final Plane translationPlane = new Plane();

    /**
     * The axis along which the user is translating.
     */
    private final Vector3d translationAxis = new Vector3d();

    @Override
    public void activate(SelectedItems selectedItems) {
        this.selectedItems = selectedItems;
        if(selectedItems.isEmpty()) return;

        // Set up the translationPlane and translationAxis based on the scene or selected items
        Matrix4d pivot = EditorUtils.getFirstItemSelectedMatrix(selectedItems);
        translationPlane.set(EditorUtils.getXYPlane(pivot));
        translationAxis.set(MatrixHelper.getXAxis(pivot));

        origin = getLastSelectedItemPosition();
    }

    @Override
    public void deactivate() {
        dragging = false;
    }

    private Point3d getLastSelectedItemPosition() {
        if(selectedItems==null || selectedItems.isEmpty()) return new Point3d();

        List<Entity> list = selectedItems.getEntities();
        Entity last = list.get(list.size()-1);
        PoseComponent pose = last.findFirstComponent(PoseComponent.class);
        if(pose==null) return new Point3d();

        return new Point3d(MatrixHelper.getPosition(pose.getWorld()));
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            origin = getLastSelectedItemPosition();
            mousePressed(event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED && dragging) {
            origin = getLastSelectedItemPosition();
            mouseDragged(event);
        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            dragging = false;
        }
    }

    private void mousePressed(MouseEvent event) {
        if (isHandleClicked(event.getX(), event.getY())) {
            dragging = true;
            startPoint = getPointOnPlane(event.getX(), event.getY());
        }
    }

    private void mouseDragged(MouseEvent event) {
        Point3d currentPoint = getPointOnPlane(event.getX(), event.getY());
        if(currentPoint==null) return;

        Point3d nearestPoint = getNearestPointOnAxis(currentPoint);

        Vector3d translation = new Vector3d();
        translation.sub(nearestPoint, startPoint);

        // Apply the translation to the selected items
        for (Entity entity : selectedItems.getEntities()) {
            Matrix4d pose = new Matrix4d(selectedItems.getWorldPose(entity));
            pose.m03 += translation.x;
            pose.m13 += translation.y;
            pose.m23 += translation.z;
            entity.findFirstComponent(PoseComponent.class).setWorld(pose);
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

    /**
     * Looks through the camera's viewport and returns the point on the translationPlane, if any.
     * @param x the x coordinate of the viewport, in screen coordinates [-1,1]
     * @param y the y coordinate of the viewport, in screen coordinates [-1,1]
     * @return the point on the translationPlane, or null if no intersection
     */
    private Point3d getPointOnPlane(double x, double y) {
        // get ray from camera through viewport
        Ray ray = viewport.getRayThroughPoint(x, y);

        // get intersection of ray with translationPlane
        double distance = IntersectionHelper.rayPlane(ray, translationPlane);
        if(distance == Double.MAX_VALUE) {
            return null;
        }
        return new Point3d(ray.getPoint(distance));
    }

    private boolean isHandleClicked(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = getPointOnPlane(x, y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d();
        diff.sub(point, origin);
        double d = diff.dot(translationAxis);
        return (Math.abs(d-handleLength) < gripRadius);
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {
        // Handle keyboard events, if necessary
    }

    @Override
    public void update(double deltaTime) {
        // Update the tool's state, if necessary
    }

    @Override
    public void render(GL2 gl2) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the translation handles on the plane
        boolean light = OpenGLHelper.disableLightingStart(gl2);
        int depth = OpenGLHelper.drawAtopEverythingStart(gl2);

        gl2.glPushMatrix();

        gl2.glTranslated(origin.x, origin.y, origin.z);

        gl2.glBegin(GL2.GL_LINES);
        gl2.glVertex3d(0, 0, 0);
        gl2.glVertex3d(handleLength, 0, 0);
        gl2.glEnd();
        gl2.glPushMatrix();
        gl2.glTranslated(handleLength, 0, 0);
        PrimitiveSolids.drawSphere(gl2, gripRadius);

        gl2.glPopMatrix();
        gl2.glPopMatrix();

        OpenGLHelper.drawAtopEverythingEnd(gl2, depth);
        OpenGLHelper.disableLightingEnd(gl2, light);
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}
