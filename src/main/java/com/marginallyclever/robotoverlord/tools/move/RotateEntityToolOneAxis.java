package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A tool to rotate entities in the editor.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class RotateEntityToolOneAxis implements EditorTool {
    /**
     * visual "tick" marks for snapping.
     */
    private static final double TICK_RATIO_INSIDE_45 = 0.25;
    private static final double TICK_RATIO_OUTSIDE_45 = 0.75;
    private static final double TICK_RATIO_INSIDE_5 = 1.00;
    private static final double TICK_RATIO_OUTSIDE_5 = 1.05;
    private static final double TICK_RATIO_INSIDE_10 = 1.00;
    private static final double TICK_RATIO_OUTSIDE_10 = 1.10;
    /**
     * how close the cursor has to get to a tick for it to be considered "snapped"
     */
    private final double SNAP_RADIANS_5 = Math.toRadians(2);
    private final double SNAP_RADIANS_45 = Math.toRadians(3);

    /**
     * The size of the handle and ring.
     */
    private final double ringRadius = 5.0;
    private final double handleLength = 4.89898;
    private final double handleOffsetY = 1.0;
    private final double gripRadius = 0.5;

    /**
     * The number of segments to use when drawing the ring.
     */
    private final int ringResolution = 64;

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
     * The axes along which the user is translating.
     */
    private final Vector3d rotationAxisX = new Vector3d();
    private final Vector3d rotationAxisY = new Vector3d();

    private final Matrix4d pivotMatrix = new Matrix4d();
    private final Matrix4d startMatrix = new Matrix4d();

    /**
     * which axis of rotation will be used?  0,1,2 = x,y,z
     */
    private int rotation=2;
    private boolean hovering = false;
    private final Box handleBox = new Box();

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Entity> list) {
        this.selectedItems = new SelectedItems(list);
        if (selectedItems.isEmpty()) return;

        setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));
    }

    public void setPivotMatrix(Matrix4d pivot) {
        pivotMatrix.set(pivot);
        rotationAxisX.set(MatrixHelper.getXAxis(pivot));
        rotationAxisY.set(MatrixHelper.getYAxis(pivot));
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        dragging = false;
        selectedItems = null;
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if(selectedItems!=null) setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));

        if( event.getID() == MouseEvent.MOUSE_MOVED ) {
            mouseMoved(event);
        } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
            mouseDragged(event);
        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        }
    }

    public void mouseMoved(MouseEvent event) {
        hovering = isCursorOverHandle(event.getX(), event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        startMatrix.set(pivotMatrix);
        if (isCursorOverHandle(event.getX(), event.getY())) {
            dragging = true;
            hovering = true;
            startPoint = EditorUtils.getPointOnPlaneFromCursor(MatrixHelper.getXYPlane(startMatrix),viewport,event.getX(), event.getY());
            if(selectedItems!=null) selectedItems.savePose();
        }
    }

    private boolean isCursorOverHandle(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = EditorUtils.getPointOnPlaneFromCursor(MatrixHelper.getXYPlane(startMatrix),viewport,x, y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d();
        diff.sub(point, MatrixHelper.getPosition(startMatrix));

        double dx = diff.dot(rotationAxisX);
        if( Math.abs(dx-handleLength) > gripRadius ) return false;

        double dy = diff.dot(rotationAxisY);
        if( Math.abs(Math.abs(dy)-handleOffsetY) > gripRadius ) return false;

        return true;
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if(!dragging) return;

        Point3d currentPoint = EditorUtils.getPointOnPlaneFromCursor(MatrixHelper.getXYPlane(startMatrix),viewport,event.getX(), event.getY());
        if(currentPoint==null) return;

        currentPoint = snapToTicks(currentPoint);

        double startAngle = 0;//getAngleBetweenPoints(startPoint);
        double currentAngle = getAngleBetweenPoints(currentPoint);
        double rotationAngle = currentAngle - startAngle;
        Matrix4d rot = new Matrix4d();
        switch(rotation) {
            case 0 -> rot.rotX(rotationAngle);
            case 1 -> rot.rotY(rotationAngle);
            case 2 -> rot.rotZ(rotationAngle);
        }

        Vector3d pivotTranslation = MatrixHelper.getPosition(startMatrix);
        Matrix4d pivot = MatrixHelper.createIdentityMatrix4();
        pivot.m03=pivotTranslation.x;
        pivot.m13=pivotTranslation.y;
        pivot.m23=pivotTranslation.z;
        Matrix4d pivotInverse = MatrixHelper.createIdentityMatrix4();
        pivotInverse.m03=-pivotTranslation.x;
        pivotInverse.m13=-pivotTranslation.y;
        pivotInverse.m23=-pivotTranslation.z;


        for (Entity entity : selectedItems.getEntities()) {
            Matrix4d pose = new Matrix4d(selectedItems.getWorldPoseAtStart(entity));
            Vector3d translation = MatrixHelper.getPosition(pose);
            translation.sub(pivotTranslation);

            pose.mul(pivotInverse);
            pose.mul(rot);
            pose.mul(pivot);

            pose.transform(translation);
            translation.add(pivotTranslation);
            pose.setTranslation(translation);

            PoseComponent pc = entity.getComponent(PoseComponent.class);
            pc.setWorld(pose);
        }
    }

    private Point3d snapToTicks(Point3d currentPoint) {
        Vector3d diff = new Vector3d();
        Vector3d center = MatrixHelper.getPosition(startMatrix);
        diff.sub(currentPoint, center);
        double diffLength = diff.length()/ringRadius;
        Vector3d xAxis = MatrixHelper.getXAxis(startMatrix);
        Vector3d yAxis = MatrixHelper.getYAxis(startMatrix);
        double angle = Math.atan2(yAxis.dot(diff),xAxis.dot(diff));

        // if the diff length is within the TICK_RATIO_INSIDE_5 and TICK_RATIO_INSIDE_10 and
        // if angle is within SNAP_RADIANS_5 of every 5 degrees, snap to 5 degrees.
        if(diffLength > TICK_RATIO_INSIDE_5 && diffLength < TICK_RATIO_OUTSIDE_10) {
            angle = roundToNearestSnap(angle, SNAP_RADIANS_5,5);
            double angle5 = Math.round(angle / (Math.PI/36)) * (Math.PI/36);
            diff.scaleAdd(Math.cos(angle5), xAxis, center);
            diff.scaleAdd(Math.sin(angle5), yAxis, diff);
            currentPoint.set(diff);
            return currentPoint;
        }
        // if the diff length is within the TICK_RATIO_OUTSIDE_45 and TICK_RATIO_OUTSIDE_45 and
        // if angle is within SNAP_RADIANS_45 of every 45 degrees, snap to 45 degrees.
        if(diffLength > TICK_RATIO_INSIDE_45 && diffLength < TICK_RATIO_OUTSIDE_45) {
            angle = roundToNearestSnap(angle, SNAP_RADIANS_45,45);
            double angle5 = Math.round(angle / (Math.PI/4)) * (Math.PI/4);
            diff.scaleAdd(Math.cos(angle5), xAxis, center);
            diff.scaleAdd(Math.sin(angle5), yAxis, diff);
            currentPoint.set(diff);
            return currentPoint;
        }
        return currentPoint;
    }

    public static double roundToNearestSnap(double angleRadians, double snapRange,double nearest) {
        double nearestFive = Math.round(angleRadians / nearest) * nearest;
        double difference = Math.abs(angleRadians - nearestFive);

        if (difference <= snapRange) {
            return nearestFive;
        } else {
            return angleRadians;
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if(!dragging) return;

        dragging = false;
        if(selectedItems!=null) {
            EditorUtils.updateUndoState(this,selectedItems);
            selectedItems.savePose();
        }
    }

    /**
     * Return angle in radians between startPoint and currentPoint around the pivotMatrix origin.
     * @param currentPoint the point to measure the angle to.
     * @return the angle in radians.
     */
    private double getAngleBetweenPoints(Point3d currentPoint) {
        Vector3d v2 = new Vector3d(currentPoint);
        v2.sub(MatrixHelper.getPosition(startMatrix));
        double x = v2.dot(MatrixHelper.getXAxis(startMatrix));
        double y = v2.dot(MatrixHelper.getYAxis(startMatrix));
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
     * @param gl2 The OpenGL systems context.
     */
    @Override
    public void render(GL2 gl2) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the rotation handles on the plane
        boolean texture = OpenGLHelper.disableTextureStart(gl2);
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        drawMainRingAndHandles(gl2);
        if(dragging) drawWhileDragging(gl2);

        OpenGLHelper.disableLightingEnd(gl2, light);
        OpenGLHelper.disableTextureEnd(gl2, texture);
    }

    private void drawWhileDragging(GL2 gl2) {
        gl2.glColor3d(1,1,1);

        gl2.glPushMatrix();

        gl2.glTranslated(startMatrix.m03, startMatrix.m13, startMatrix.m23);
        Vector3d xAxis = MatrixHelper.getXAxis(startMatrix);
        Vector3d yAxis = MatrixHelper.getYAxis(startMatrix);

        gl2.glBegin(GL2.GL_LINES);
        // major line
        gl2.glVertex3d(0,0,0);
        gl2.glVertex3d(xAxis.x*ringRadius, xAxis.y*ringRadius, xAxis.z*ringRadius);

        double d0 = ringRadius * TICK_RATIO_INSIDE_45;
        double d1 = ringRadius * TICK_RATIO_OUTSIDE_45;
        // 45 degree lines
        for(int i=45;i<360;i+=45) {
            Vector3d a = new Vector3d(xAxis);
            Vector3d b = new Vector3d(yAxis);
            a.scale(Math.cos(Math.toRadians(i)));
            b.scale(Math.sin(Math.toRadians(i)));
            a.add(b);
            gl2.glVertex3d(a.x*d0, a.y*d0, a.z*d0);
            gl2.glVertex3d(a.x*d1, a.y*d1, a.z*d1);
        }

        // 5 and 10 degree lines
        for(int i=0;i<360;i+=5) {
            Vector3d a = new Vector3d(xAxis);
            Vector3d b = new Vector3d(yAxis);
            a.scale(Math.cos(Math.toRadians(i)));
            b.scale(Math.sin(Math.toRadians(i)));
            a.add(b);
            if(i%10==0) {
                d0 = ringRadius * TICK_RATIO_INSIDE_10;
                d1 = ringRadius * TICK_RATIO_OUTSIDE_10;
            } else {
                d0 = ringRadius * TICK_RATIO_INSIDE_5;
                d1 = ringRadius * TICK_RATIO_OUTSIDE_5;
            }
            gl2.glVertex3d(a.x*d0, a.y*d0, a.z*d0);
            gl2.glVertex3d(a.x*d1, a.y*d1, a.z*d1);
        }

        gl2.glEnd();
        gl2.glPopMatrix();

        gl2.glPushMatrix();
        MatrixHelper.applyMatrix(gl2, pivotMatrix);
        drawLine(gl2,new Vector3d(1,0,0),ringRadius);

        gl2.glPopMatrix();
    }

    private void drawMainRingAndHandles(GL2 gl2) {
        gl2.glPushMatrix();

        MatrixHelper.applyMatrix(gl2, pivotMatrix);

        float [] colors = new float[4];
        gl2.glGetFloatv(GL2.GL_CURRENT_COLOR, colors, 0);
        double colorScale = hovering? 1:0.5;
        gl2.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 1.0);

        PrimitiveSolids.drawCircleXY(gl2, ringRadius, ringResolution);

        gl2.glTranslated(handleLength,handleOffsetY,0);
        double v = gripRadius;
        gl2.glPushMatrix();
        gl2.glScaled(v, v, v);
        handleBox.render(gl2);
        gl2.glPopMatrix();

        gl2.glTranslated(0,-2*handleOffsetY,0);
        gl2.glPushMatrix();
        gl2.glScaled(v, v, v);
        handleBox.render(gl2);
        gl2.glPopMatrix();

        gl2.glPopMatrix();
    }

    private void drawLine(GL2 gl2, Tuple3d dest,double scale) {
        gl2.glBegin(GL2.GL_LINES);
        gl2.glVertex3d(0,0,0);
        gl2.glVertex3d(dest.x*scale,dest.y*scale,dest.z*scale);
        gl2.glEnd();
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

    public void setRotation(int i) {
        rotation=i;
    }
}
