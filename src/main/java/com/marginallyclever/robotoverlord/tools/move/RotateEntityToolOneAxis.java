package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class RotateEntityToolOneAxis implements EditorTool {
    private final double handleLength = 4.89898;
    private final double handleOffsetY = 1.0;
    private final double gripRadius = 0.5;

    private final int ringResolution = 32;

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
    private final Plane rotationPlane = new Plane();

    /**
     * The axes along which the user is translating.
     */
    private final Vector3d rotationAxisX = new Vector3d();
    private final Vector3d rotationAxisY = new Vector3d();

    private Matrix4d pivotMatrix;
    private Matrix4d toolTransform;

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param selectedItems The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(SelectedItems selectedItems) {
        this.selectedItems = selectedItems;
        if (selectedItems.isEmpty()) return;

        setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));
        setToolTransform(MatrixHelper.createIdentityMatrix4());
    }

    public void setPivotMatrix(Matrix4d pivot) {
        //if(dragging) return;  // don't change the pivot while dragging (it's confusing)
        pivotMatrix = new Matrix4d(pivot);
        rotationPlane.set(EditorUtils.getXYPlane(pivot));
        rotationAxisX.set(MatrixHelper.getXAxis(pivot));
        rotationAxisY.set(MatrixHelper.getYAxis(pivot));
    }

    public void setToolTransform(Matrix4d rotation) {
        toolTransform = rotation;
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        dragging = false;
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED && dragging) {
            mouseDragged(event);
        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            dragging = false;
            selectedItems.savePose();
        }
    }

    private void mousePressed(MouseEvent event) {
        if (isHandleClicked(event.getX(), event.getY())) {
            dragging = true;
            startPoint = EditorUtils.getPointOnPlane(rotationPlane,viewport,event.getX(), event.getY());
        }
    }

    private boolean isHandleClicked(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = EditorUtils.getPointOnPlane(rotationPlane,viewport,x, y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d();
        diff.sub(point, MatrixHelper.getPosition(pivotMatrix));

        double dx = diff.dot(rotationAxisX);
        if( Math.abs(dx-handleLength) > gripRadius ) return false;

        double dy = diff.dot(rotationAxisY);
        if( Math.abs(Math.abs(dy)-handleOffsetY) > gripRadius ) return false;

        return true;
    }

    private void mouseDragged(MouseEvent event) {
        Point3d currentPoint = EditorUtils.getPointOnPlane(rotationPlane,viewport,event.getX(), event.getY());
        if(currentPoint==null) return;

        double rotation = getAngleBetweenPoints(currentPoint) - getAngleBetweenPoints(startPoint);
        Matrix4d rot = new Matrix4d();
        rot.rotZ(rotation);

        Matrix4d toolTransformInverse = new Matrix4d(toolTransform);
        toolTransformInverse.invert();

        Matrix4d pivotInverse = new Matrix4d(pivotMatrix);
        pivotInverse.invert();

        // Apply the translation to the selected items
        for (Entity entity : selectedItems.getEntities()) {
            Matrix4d pose = new Matrix4d(selectedItems.getWorldPoseAtStart(entity));
            pose.mul(pivotInverse);
            pose.mul(toolTransformInverse);
            pose.mul(rot);
            pose.mul(toolTransform);
            pose.mul(pivotMatrix);
            entity.findFirstComponent(PoseComponent.class).setWorld(pose);
        }
    }

    /**
     * Return angle in radians between startPoint and currentPoint around the pivotMatrix origin.
     * @param currentPoint the point to measure the angle to.
     * @return the angle in radians.
     */
    private double getAngleBetweenPoints(Point3d currentPoint) {
        Vector3d v2 = new Vector3d(currentPoint);
        v2.sub(MatrixHelper.getPosition(pivotMatrix));
        double x = v2.dot(rotationAxisX);
        double y = v2.dot(rotationAxisY);
        return Math.atan2(y,x);
    }

    @Override
    public void handleKeyEvent(KeyEvent e) {}

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    @Override
    public void update(double deltaTime) {}

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl2 The OpenGL render context.
     */
    @Override
    public void render(GL2 gl2) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the rotation handles on the plane
        boolean texture = OpenGLHelper.disableTextureStart(gl2);
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        gl2.glPushMatrix();

        MatrixHelper.applyMatrix(gl2, pivotMatrix);

        PrimitiveSolids.drawCircleXY(gl2, handleLength, ringResolution);
/*
        gl2.glBegin(GL2.GL_LINES);
        gl2.glVertex3d(0,0,0);
        gl2.glVertex3d(handleLength,0,0);  // x axis
        gl2.glVertex3d(0,0,0);
        gl2.glVertex3d(0,handleLength,0);  // y axis
        gl2.glEnd();*/

        gl2.glTranslated(handleLength,handleOffsetY,-gripRadius*0.5);
        PrimitiveSolids.drawBox(gl2, gripRadius, gripRadius, gripRadius);
        gl2.glTranslated(0,-2*handleOffsetY,0);
        PrimitiveSolids.drawBox(gl2, gripRadius, gripRadius, gripRadius);

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
