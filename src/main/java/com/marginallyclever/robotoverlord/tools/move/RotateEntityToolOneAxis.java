package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
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
    private double localScale = 1;

    /**
     * The size of the handle and ring.
     */
    private double ringRadius = 5.0;
    private double handleLength = 4.89898;
    private double handleOffsetY = 1.0;
    private double gripRadius = 0.5;

    /**
     * The number of segments to use when drawing the ring.
     */
    private int ringResolution = 64;

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
    private boolean cursorOverHandle = false;
    private final Box handleBox = new Box();
    private int frameOfReference = EditorTool.FRAME_WORLD;
    private final ColorRGB color;

    public RotateEntityToolOneAxis(ColorRGB color) {
        super();
        this.color = color;
    }

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

        updatePivotMatrix();
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
        cursorOverHandle = isCursorOverHandle(event.getX(), event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        startMatrix.set(pivotMatrix);
        if (isCursorOverHandle(event.getX(), event.getY())) {
            dragging = true;
            cursorOverHandle = true;
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
        if( Math.abs(dx-getHandleLengthScaled()) > getGripRadiusScaled() ) return false;

        double dy = diff.dot(rotationAxisY);
        if( Math.abs(Math.abs(dy)-getHandleOffsetYScaled()) > getGripRadiusScaled() ) return false;

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
        double diffLength = diff.length()/getRingRadiusScaled();
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
            EditorUtils.updateUndoState(selectedItems);
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
    public void update(double deltaTime) {
        if(selectedItems!=null) updatePivotMatrix();

        updateLocalScale();
    }

    private void updatePivotMatrix() {
        setPivotMatrix(EditorUtils.getPivotMatrix(frameOfReference,viewport,selectedItems));
    }

    private void updateLocalScale() {
        Vector3d cameraPoint = viewport.getCamera().getPosition();
        Vector3d pivotPoint = MatrixHelper.getPosition(pivotMatrix);
        pivotPoint.sub(cameraPoint);
        localScale = pivotPoint.length() * InteractionPreferences.toolScale.get();
    }

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl The OpenGL systems context.
     */
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if(selectedItems==null || selectedItems.isEmpty()) return;

        shaderProgram.set1i(gl,"useTexture",0);
        shaderProgram.set1i(gl,"useLighting",0);
        shaderProgram.set1i(gl,"useVertexColor",0);

        drawMainRingAndHandles(gl,shaderProgram);
        if(dragging) {
            shaderProgram.set4f(gl, "objectColor", 1,1,1,1);
            drawWhileDragging(gl,shaderProgram);
        }
    }

    private void drawWhileDragging(GL3 gl,ShaderProgram shaderProgram) {
        Matrix4d m = new Matrix4d(startMatrix);
        m.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m);

        float rr = (float)getRingRadiusScaled();

        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_LINES);
        
        // major line
        mesh.addVertex(0,0,0);
        mesh.addVertex(rr,0,0);

        float d0 = rr * (float)TICK_RATIO_INSIDE_45;
        float d1 = rr * (float)TICK_RATIO_OUTSIDE_45;
        // 45 degree lines
        for(int i=45;i<360;i+=45) {
            Vector3d a = new Vector3d(Math.cos(Math.toRadians(i)),Math.sin(Math.toRadians(i)),0);
            mesh.addVertex( (float)a.x*d0, (float)a.y*d0, (float)a.z*d0);
            mesh.addVertex( (float)a.x*d1, (float)a.y*d1, (float)a.z*d1);
        }

        // 5 and 10 degree lines
        for(int i=0;i<360;i+=5) {
            Vector3d a = new Vector3d(Math.cos(Math.toRadians(i)),Math.sin(Math.toRadians(i)),0);
            if(i%10==0) {
                d0 = rr * (float)TICK_RATIO_INSIDE_10;
                d1 = rr * (float)TICK_RATIO_OUTSIDE_10;
            } else {
                d0 = rr * (float)TICK_RATIO_INSIDE_5;
                d1 = rr * (float)TICK_RATIO_OUTSIDE_5;
            }
            mesh.addVertex((float)a.x*d0, (float)a.y*d0, (float)a.z*d0);
            mesh.addVertex((float)a.x*d1, (float)a.y*d1, (float)a.z*d1);
        }

        // current angle
        Matrix4d m2 = new Matrix4d(startMatrix);
        m2.invert();
        m2.mul(pivotMatrix);
        mesh.addVertex(0,0,0);
        mesh.addVertex((float)m.m00*rr, (float)m.m01*rr, (float)m.m02*rr);

        mesh.render(gl);
    }

    private void drawMainRingAndHandles(GL3 gl,ShaderProgram shaderProgram) {
        Matrix4d m = new Matrix4d(pivotMatrix);
        m.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m);

        float colorScale = cursorOverHandle ? 1:0.5f;
        float red   = color.red   * colorScale / 255f;
        float green = color.green * colorScale / 255f;
        float blue  = color.blue  * colorScale / 255f;
        shaderProgram.set4f(gl, "objectColor", red, green, blue, 1.0f);
        PrimitiveSolids.drawCircleXY(gl, getRingRadiusScaled(), ringResolution).render(gl);

        double v = getGripRadiusScaled();
        handleBox.height.set(v);
        handleBox.width.set(v);
        handleBox.length.set(v);

        Matrix4d m2 = MatrixHelper.createIdentityMatrix4();
        m2.m03 = getHandleLengthScaled();
        m2.m13 = getHandleOffsetYScaled();
        m2.mul(pivotMatrix,m2);
        m2.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m2);
        handleBox.render(gl);

        m2 = MatrixHelper.createIdentityMatrix4();
        m2.m03 = getHandleLengthScaled();
        m2.m13 = -getHandleOffsetYScaled();
        m2.mul(pivotMatrix,m2);
        m2.transpose();
        shaderProgram.setMatrix4d(gl,"modelMatrix",m2);
        handleBox.render(gl);
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

    private double getHandleLengthScaled() {
        return handleLength * localScale;
    }

    private double getGripRadiusScaled() {
        return gripRadius * localScale;
    }

    private double getHandleOffsetYScaled() {
        return handleOffsetY * localScale;
    }

    private double getRingRadiusScaled() {
        return ringRadius * localScale;
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
