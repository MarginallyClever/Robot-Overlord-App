package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import javax.vecmath.*;
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
     * The axes along which the user is translating.
     */
    private final Vector3d rotationAxisX = new Vector3d();
    private final Vector3d rotationAxisY = new Vector3d();

    private final Matrix4d pivotMatrix = new Matrix4d();
    private final Matrix4d startMatrix = new Matrix4d();

    private int rotation=2;

    private boolean hovering = false;

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
            startPoint = EditorUtils.getPointOnPlane(EditorUtils.getXYPlane(startMatrix),viewport,event.getX(), event.getY());
            if(selectedItems!=null) selectedItems.savePose();
        }
    }

    private boolean isCursorOverHandle(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = EditorUtils.getPointOnPlane(EditorUtils.getXYPlane(startMatrix),viewport,x, y);
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

        Point3d currentPoint = EditorUtils.getPointOnPlane(EditorUtils.getXYPlane(startMatrix),viewport,event.getX(), event.getY());
        if(currentPoint==null) return;

        double startAngle = getAngleBetweenPoints(startPoint);
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

            PoseComponent pc = entity.findFirstComponent(PoseComponent.class);
            pc.setWorld(pose);
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        dragging = false;
        if(selectedItems!=null) selectedItems.savePose();
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
     * @param gl2 The OpenGL render context.
     */
    @Override
    public void render(GL2 gl2) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the rotation handles on the plane
        boolean texture = OpenGLHelper.disableTextureStart(gl2);
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        gl2.glPushMatrix();

        if(dragging) {
            gl2.glPushMatrix();
            gl2.glTranslated(startMatrix.m03, startMatrix.m13, startMatrix.m23);
            drawLine(gl2,MatrixHelper.getXAxis(startMatrix), 4);
            drawLine(gl2,MatrixHelper.getYAxis(startMatrix), 4);
            //drawLine(gl2,MatrixHelper.getZAxis(startMatrix), 4);
            gl2.glPopMatrix();
        }

        MatrixHelper.applyMatrix(gl2, pivotMatrix);

        float [] colors = new float[4];
        gl2.glGetFloatv(GL2.GL_CURRENT_COLOR, colors, 0);
        double colorScale = hovering? 1:0.8;
        gl2.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 1.0);

        //drawLine(gl2, new Vector3d(1,0,0), handleLength);
        //drawLine(gl2, new Vector3d(0,1,0), handleLength);

        PrimitiveSolids.drawCircleXY(gl2, handleLength, ringResolution);

        gl2.glTranslated(handleLength,handleOffsetY,-gripRadius*0.5);
        PrimitiveSolids.drawBox(gl2, gripRadius, gripRadius, gripRadius);
        gl2.glTranslated(0,-2*handleOffsetY,0);
        PrimitiveSolids.drawBox(gl2, gripRadius, gripRadius, gripRadius);

        gl2.glPopMatrix();

        OpenGLHelper.disableLightingEnd(gl2, light);
        OpenGLHelper.disableTextureEnd(gl2, texture);
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
