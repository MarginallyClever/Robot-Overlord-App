package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.*;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PoseMoveEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class TranslateEntityToolTwoAxis implements EditorTool {
    private final double padSize = 1;

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

    @Override
    public void activate(SelectedItems selectedItems) {
        this.selectedItems = selectedItems;
        if (selectedItems.isEmpty()) return;

        setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));
    }

    public void setPivotMatrix(Matrix4d pivot) {
        pivotMatrix = new Matrix4d(pivot);
        translationPlane.set(EditorUtils.getXYPlane(pivot));
        translationAxisX.set(MatrixHelper.getXAxis(pivot));
        translationAxisY.set(MatrixHelper.getYAxis(pivot));
    }

    @Override
    public void deactivate() {
        dragging = false;
        selectedItems = null;
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if(selectedItems!=null) setPivotMatrix(EditorUtils.getLastItemSelectedMatrix(selectedItems));

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
            startPoint = EditorUtils.getPointOnPlane(translationPlane,viewport,event.getX(), event.getY());
            selectedItems.savePose();
        }
    }

    public void mouseDragged(MouseEvent event) {
        if(!dragging) return;

        Point3d currentPoint = EditorUtils.getPointOnPlane(translationPlane,viewport,event.getX(), event.getY());
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
            entity.findFirstComponent(PoseComponent.class).setWorld(pose);
        }
    }

    public void mouseReleased(MouseEvent event) {
        dragging = false;
        if(selectedItems!=null) {
            EditorUtils.updateUndoState(this,selectedItems);
            selectedItems.savePose();
        }
    }

    private boolean isCursorOverPad(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = EditorUtils.getPointOnPlane(translationPlane,viewport,x, y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d();
        diff.sub(point, MatrixHelper.getPosition(pivotMatrix));

        double dx = diff.dot(translationAxisX);
        if( dx<0 || dx>=padSize ) return false;

        double dy = diff.dot(translationAxisY);
        if( dy<0 || dy>=padSize ) return false;

        return true;
    }

    @Override
    public void handleKeyEvent(KeyEvent event) {}

    @Override
    public void update(double deltaTime) {}

    @Override
    public void render(GL2 gl2) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        // Render the translation pad on the plane
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        gl2.glPushMatrix();

        MatrixHelper.applyMatrix(gl2, pivotMatrix);

        float [] colors = new float[4];
        gl2.glGetFloatv(GL2.GL_CURRENT_COLOR, colors, 0);
        double colorScale = hovering? 1:0.8;

        gl2.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 0.5);
        drawQuad(gl2,GL2.GL_TRIANGLE_FAN);
        gl2.glColor4d(colors[0]*colorScale, colors[1]*colorScale, colors[2]*colorScale, 1.0);
        drawQuad(gl2,GL2.GL_LINE_LOOP);

        gl2.glPopMatrix();

        OpenGLHelper.disableLightingEnd(gl2, light);
    }

    private void drawQuad(GL2 gl2,int mode) {
        gl2.glBegin(mode);
        gl2.glVertex3d( 0, 0,0);
        gl2.glVertex3d(padSize, 0,0);
        gl2.glVertex3d(padSize, padSize,0);
        gl2.glVertex3d(0, padSize,0);
        gl2.glEnd();
        gl2.glBegin(mode);
        gl2.glVertex3d( 0, 0,0);
        gl2.glVertex3d(0, padSize,0);
        gl2.glVertex3d(padSize, padSize,0);
        gl2.glVertex3d(padSize, 0,0);
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
}
