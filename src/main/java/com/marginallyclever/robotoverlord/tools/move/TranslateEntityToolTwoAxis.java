package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A tool to translate entities in the editor.  It translates in a given plane.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class TranslateEntityToolTwoAxis implements EditorTool {
    private final double padSize = 1;
    private double toolScale = 0.035;
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

    private boolean hovering = false;
    private int frameOfReference = EditorTool.FRAME_WORLD;

    @Override
    public void activate(List<Entity> list) {
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
        setPivotMatrix(EditorUtils.getPivotMatrix(frameOfReference,viewport,selectedItems));
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
        hovering = isCursorOverPad(event.getX(), event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (isCursorOverPad(event.getX(), event.getY())) {
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

        Vector3d translation = new Vector3d();
        translation.sub(currentPoint, startPoint);

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

    private boolean isCursorOverPad(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = EditorUtils.getPointOnPlaneFromCursor(translationPlane,viewport,x, y);
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
        Vector3d cameraPoint = viewport.getCamera().getPosition();
        Vector3d pivotPoint = MatrixHelper.getPosition(pivotMatrix);
        pivotPoint.sub(cameraPoint);
        localScale = pivotPoint.length() * toolScale;
    }

    @Override
    public void render(GL3 gl) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the translation pad on the plane
        boolean light = OpenGLHelper.disableLightingStart(gl);

        gl.glPushMatrix();

        MatrixHelper.applyMatrix(gl, pivotMatrix);

        float [] colors = new float[4];
        gl.glGetFloatv(GL3.GL_CURRENT_COLOR, colors, 0);
        double colorScale = hovering? 1:0.8;

        gl.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 0.5);
        drawQuad(gl,GL3.GL_TRIANGLE_FAN);
        gl.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 1.0);
        drawQuad(gl,GL3.GL_LINE_LOOP);

        gl.glPopMatrix();

        OpenGLHelper.disableLightingEnd(gl, light);
    }

    private void drawQuad(GL3 gl,int mode) {
        double ps = getPadSizeScaled();
        gl.glBegin(mode);
        gl.glVertex3d( 0, 0,0);
        gl.glVertex3d(ps, 0,0);
        gl.glVertex3d(ps, ps,0);
        gl.glVertex3d(0, ps,0);
        gl.glEnd();
        gl.glBegin(mode);
        gl.glVertex3d( 0, 0,0);
        gl.glVertex3d(0, ps,0);
        gl.glVertex3d(ps, ps,0);
        gl.glVertex3d(ps, 0,0);
        gl.glEnd();
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
    private double getToolScale() {
        return toolScale;
    }
    private void setToolScale(double toolScale) {
        this.toolScale = toolScale;
    }

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(int index) {
        frameOfReference = index;
        updatePivotMatrix();
    }
}
