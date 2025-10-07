package com.marginallyclever.ro3.apps.viewport.viewporttool.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.shader.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.apps.viewport.renderpass.RenderPassHelper;
import com.marginallyclever.ro3.apps.viewport.viewporttool.SelectedItems;
import com.marginallyclever.ro3.apps.viewport.viewporttool.ViewportTool;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.CircleXY;
import com.marginallyclever.ro3.mesh.proceduralmesh.Waldo;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A tool to rotate {@link Pose} nodes in the {@link Viewport}.
 *
 */
public class RotateToolOneAxis implements ViewportTool {
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
    private static final double ringRadius = 5.0;
    private static final double gripRadius = 1.0;

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

    // The point on the translation plane where the handle was clicked.
    private Point3d startPoint;
    // The current point on the translation plane as the user drags the mouse.
    private Point3d currentDragPoint;
    // The current angle of rotation as the user drags the mouse.
    private double startingRotationAngleRadians;
    private double currentRotationAngleRadians;

    // The axes in the plane along which the user is translating.
    private final Vector3d rotationAxisX = new Vector3d();
    private final Vector3d rotationAxisY = new Vector3d();

    // The pivot matrix around which the rotation occurs.
    private final Matrix4d pivotMatrix = new Matrix4d();

    private boolean cursorOverHandle = false;
    private FrameOfReference frameOfReference = FrameOfReference.WORLD;
    private final Color color;
    // draws the snap tick marks
    private final Mesh tickMarkMesh = new Mesh();

    // draws the ring of rotation, the sweep of an active drag, and the white circle facing the camera.
    private final CircleXY ringMesh = new CircleXY();
    private final Waldo waldo = new Waldo();

    private boolean drawPivotPoint=true;

    public RotateToolOneAxis(Color color) {
        super();
        this.color = color;
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "RotateToolOneAxis.markerMesh", tickMarkMesh);
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "RotateToolOneAxis.ringMesh", ringMesh);
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "RotateToolOneAxis.waldo", waldo);

        buildTickMarkMesh();
        ringMesh.updateModel();
        ringMesh.setRenderStyle(GL3.GL_LINE_LOOP);
    }

    public void setDrawPivotPoint(boolean b) {
        drawPivotPoint = b;
    }

    /**
     * Build the mesh used to show the tick marks on the ring.
     */
    private void buildTickMarkMesh() {
        tickMarkMesh.setRenderStyle(GL3.GL_LINES);

        // major line
        tickMarkMesh.addVertex(0,0,0);
        tickMarkMesh.addVertex((float)ringRadius,0,0);

        // 45 degree lines
        addTickMarkerGroup(45, TICK_RATIO_INSIDE_45, TICK_RATIO_OUTSIDE_45);
        // 10 degree lines
        addTickMarkerGroup(10, TICK_RATIO_INSIDE_10, TICK_RATIO_OUTSIDE_10);
        // 5 degree lines
        addTickMarkerGroup(5, TICK_RATIO_INSIDE_5, TICK_RATIO_OUTSIDE_5);

        tickMarkMesh.fireMeshChanged();
    }

    /**
     * Add a group of tick markers around the circle.
     * @param stepSize in degrees
     * @param id inner distance ratio
     * @param od outer distance ratio
     */
    private void addTickMarkerGroup(int stepSize,double id,double od) {
        float d0 = (float)(ringRadius * id);
        float d1 = (float)(ringRadius * od);
        for(int i=0;i<360;i+=stepSize) {
            Vector3d a = new Vector3d(Math.cos(Math.toRadians(i)),Math.sin(Math.toRadians(i)),0);
            addTickMarkerItem(a,d0,d1);
        }
    }

    /**
     * Add a single tick marker item to the mesh.
     * @param a a unit vector in the direction of the tick mark
     * @param d0 inner distance
     * @param d1 outer distance
     */
    private void addTickMarkerItem(Vector3d a, float d0, float d1) {
        tickMarkMesh.addVertex( (float)a.x*d0, (float)a.y*d0, (float)a.z*d0);
        tickMarkMesh.addVertex( (float)a.x*d1, (float)a.y*d1, (float)a.z*d1);
    }

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(SelectedItems list) {
        this.selectedItems = list;
        if (selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }

    public void setPivotMatrix(Matrix4d pivot) {
        if(!dragging) {
            rotationAxisX.set(MatrixHelper.getXAxis(pivot));
            rotationAxisY.set(MatrixHelper.getYAxis(pivot));
            pivotMatrix.set(pivot);
        }
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
        switch(event.getID()) {
            case MouseEvent.MOUSE_MOVED   : mouseMoved   (event);  break;
            case MouseEvent.MOUSE_PRESSED : mousePressed (event);  break;
            case MouseEvent.MOUSE_DRAGGED : mouseDragged (event);  break;
            case MouseEvent.MOUSE_RELEASED: mouseReleased(event);  break;
            default: break;
        }
    }

    public void mouseMoved(MouseEvent event) {
        cursorOverHandle = isCursorOverHandle(event.getX(), event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (!isCursorOverHandle(event.getX(), event.getY())) return;

        dragging = true;
        cursorOverHandle = true;
        startPoint = MoveUtils.getPointOnPlaneFromCursor(MatrixHelper.getXYPlane(pivotMatrix),viewport,event.getX(), event.getY());
        startingRotationAngleRadians = getAngleBetweenPoints(startPoint);
        if(selectedItems!=null) selectedItems.savePose();
    }

    private boolean isCursorOverHandle(int x, int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        // Get the cursor's position projected onto the plane of rotation
        Point3d cursorPoint = MoveUtils.getPointOnPlaneFromCursor(MatrixHelper.getXYPlane(pivotMatrix), viewport, x, y);
        if (cursorPoint == null) return false; // Cursor not on plane

        // Calculate the vector from pivot to cursor point
        Vector3d cursorVector = new Vector3d();
        cursorVector.sub(cursorPoint, MatrixHelper.getPosition(pivotMatrix));

        // Compute the distance from the pivot (radius of the cursor position relative to the pivot)
        double cursorRadius = cursorVector.length();
        double ringRadiusScaled = getRingRadiusScaled();

        // Check if the cursor is within the arc's radius range (handle radius ± grip radius)
        if (Math.abs(cursorRadius - ringRadiusScaled) > getGripRadiusScaled()) return false;

        double angle = getAngleOnPlaneDegrees(cursorVector);

        Camera camera = viewport.getActiveCamera();
        var cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        var lookAt = MatrixHelper.getPosition(pivotMatrix);
        lookAt.sub(cameraWorldPos);
        double [] angles = getArcAngles(lookAt);
        int start = (int)angles[0];
        int end = (int)angles[1];
        if(angle<start) angle+=360;

        // Check if the cursor's angle lies within the arc
        return angle >= start && angle <= end;
    }

    /**
     * called when the mouse is dragged after a successful mousePressed event.
     * pivotMatrix is already set to the frame of reference such that pivotMatrix Z axis is the axis aroun which we want
     * to rotate
     * @param event the mouse event
     */
    @Override
    public void mouseDragged(MouseEvent event) {
        if(!dragging) return;

        // get the current point on the plane
        currentDragPoint = MoveUtils.getPointOnPlaneFromCursor(MatrixHelper.getXYPlane(pivotMatrix),viewport,event.getX(), event.getY());
        if(currentDragPoint == null) return;  // if the plane is somehow edge-on, we can't do anything.

        currentRotationAngleRadians = getAngleBetweenPoints(snapToTicks(currentDragPoint));

        // build a rotation matrix for the current angle.
        Matrix4d rot = new Matrix4d();
        rot.rotZ(currentRotationAngleRadians - startingRotationAngleRadians);

        // use pivotMatrix to prevent accumulation of numerical errors.
        Matrix4d inversePivotMatrix = new Matrix4d(pivotMatrix);
        inversePivotMatrix.invert();

        for (Node node : selectedItems.getNodes()) {
            if(!(node instanceof Pose pc)) continue;
            Matrix4d pose = new Matrix4d(selectedItems.getWorldPoseAtStart(pc));
            pose.mul(inversePivotMatrix,pose);  // move to pivot space.
            pose.mul(rot,pose);  // apply the rotation.
            pose.mul(pivotMatrix,pose);  // move back to world space.
            // set the new world matrix.
            pc.setWorld(pose);
        }
    }

    /**
     * If the current point is near a tick, snap to the tick.
     * @param currentPoint
     * @return
     */
    private Point3d snapToTicks(Point3d currentPoint) {
        Vector3d diff = new Vector3d();
        Vector3d center = MatrixHelper.getPosition(pivotMatrix);
        diff.sub(currentPoint, center);
        double diffLength = diff.length()/getRingRadiusScaled();
        double angle = getAngleBetweenPoints(currentPoint) - startingRotationAngleRadians;

        // if the diff length is within the TICK_RATIO_INSIDE_5 and TICK_RATIO_INSIDE_10 and
        // if angle is within SNAP_RADIANS_5 of every 5 degrees, snap to 5 degrees.
        if(TICK_RATIO_INSIDE_5 < diffLength && diffLength < TICK_RATIO_OUTSIDE_10) {
            angle = roundToNearestSnap(angle, SNAP_RADIANS_5,5);
            double angle5 = Math.round(angle / (Math.PI/36)) * (Math.PI/36) + startingRotationAngleRadians;
            diff.scaleAdd(Math.cos(angle5), rotationAxisX, center);
            diff.scaleAdd(Math.sin(angle5), rotationAxisY, diff);
            currentPoint.set(diff);
            return currentPoint;
        }

        // if the diff length is within the TICK_RATIO_OUTSIDE_45 and TICK_RATIO_OUTSIDE_45 and
        // if angle is within SNAP_RADIANS_45 of every 45 degrees, snap to 45 degrees.
        if(TICK_RATIO_INSIDE_45 < diffLength && diffLength < TICK_RATIO_OUTSIDE_45) {
            angle = roundToNearestSnap(angle, SNAP_RADIANS_45,45);
            double angle5 = Math.round(angle / (Math.PI/4)) * (Math.PI/4) + startingRotationAngleRadians;
            diff.scaleAdd(Math.cos(angle5), rotationAxisX, center);
            diff.scaleAdd(Math.sin(angle5), rotationAxisY, diff);
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
            MoveUtils.updateUndoState(selectedItems);
            // update the saved pose to the new pose so further drags are relative to the new position.
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
        v2.sub(MatrixHelper.getPosition(pivotMatrix));
        return Math.atan2(
                rotationAxisY.dot(v2),
                rotationAxisX.dot(v2));
    }

    /**
     * Get the angle in degrees of the given point on the plane defined by rotationAxisX and rotationAxisY.
     * @param pointOnPlane a vector from the pivot to a point on the plane.
     * @return the angle in degrees.
     */
    private double getAngleOnPlaneDegrees(Vector3d pointOnPlane) {
        // Determine the angle of the cursor point relative to the pivot
        double angle = Math.atan2(
                pointOnPlane.dot(rotationAxisY),
                pointOnPlane.dot(rotationAxisX)); // Angle in radians

        // Ensure angle is normalized to [0, 2π] for comparison with the arc's bounds
        if (angle < 0) angle += 2 * Math.PI;
        return Math.toDegrees(angle);
    }

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
        setPivotMatrix(MoveUtils.getPivotMatrix(frameOfReference,selectedItems,viewport.getActiveCamera()));
    }

    private void updateLocalScale() {
        Camera camera = viewport.getActiveCamera();
        assert camera!= null;
        Vector3d cameraPoint = camera.getPosition();
        Vector3d pivotPoint = MatrixHelper.getPosition(pivotMatrix);
        pivotPoint.sub(cameraPoint);
        localScale = pivotPoint.length() * 0.035;  // TODO * InteractionPreferences.toolScale;
    }

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl The OpenGL systems context.
     */
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if(selectedItems==null || selectedItems.isEmpty()) return;
        if( !MoveUtils.listContainsAPose(selectedItems.getNodes()) ) return;

        drawMainRingAndHandles(gl,shaderProgram);
        if(dragging) {
            drawWhileDragging(gl,shaderProgram);
        }

        if(drawPivotPoint) drawPivotPoint(gl,shaderProgram);
    }

    // draw waldo at the pivot point
    public void drawPivotPoint(GL3 gl,ShaderProgram shaderProgram) {
        Matrix4d m = new Matrix4d();
        m.set(pivotMatrix);
        m.mul(MatrixHelper.createScaleMatrix4(localScale));
        if (viewport.isOriginShift()) {
            var cameraWorldPos = MatrixHelper.getPosition(viewport.getActiveCamera().getWorld());
            m = RenderPassHelper.getOriginShiftedMatrix(m, cameraWorldPos);
        }
        shaderProgram.setMatrix4d(gl, "modelMatrix", m);
        drawWaldo(gl, shaderProgram);
    }

    private void drawWhileDragging(GL3 gl,ShaderProgram shaderProgram) {
        Camera camera = viewport.getActiveCamera();
        var originShift = viewport.isOriginShift();
        var cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());

        // Draw the start and end angle of the movement.
        if(currentDragPoint != null && startPoint != null) {
            int currentDegrees = (int)Math.toDegrees(currentRotationAngleRadians);
            int startingDegrees = (int)Math.toDegrees(startingRotationAngleRadians);
            int start = Math.min(currentDegrees, startingDegrees);
            int diff = Math.abs(currentDegrees - startingDegrees);

            Matrix4d mt = new Matrix4d();
            Matrix4d rot = new Matrix4d();
            rot.rotZ(Math.toRadians(start));
            mt.mul(pivotMatrix, rot);  // turn it so 0 degrees faces the start angle
            mt.mul(mt, MatrixHelper.createScaleMatrix4(getRingRadiusScaled()));  // make it big
            if(originShift) mt = RenderPassHelper.getOriginShiftedMatrix(mt, cameraWorldPos);

            shaderProgram.setMatrix4d(gl,"modelMatrix",mt);
            ringMesh.render(gl, 0, diff);
        }

        // draw the snap markers
        Matrix4d mt = new Matrix4d();
        Matrix4d rot = new Matrix4d();
        rot.rotZ(startingRotationAngleRadians);
        mt.mul(pivotMatrix, rot);  // turn it so 0 degrees faces the start angle
        mt.mul(mt, MatrixHelper.createScaleMatrix4(localScale));
        if(originShift) mt = RenderPassHelper.getOriginShiftedMatrix(mt, cameraWorldPos);

        shaderProgram.set4f(gl, "diffuseColor", 1,1,1,1);
        shaderProgram.setMatrix4d(gl,"modelMatrix",mt);
        tickMarkMesh.render(gl);
    }

    private void drawWaldo(GL3 gl,ShaderProgram shaderProgram) {
        shaderProgram.set1i(gl,"useLighting",0);
        shaderProgram.set1i(gl,"useVertexColor",1);
        shaderProgram.set4f(gl, "diffuseColor", 1,1,1,1);
        waldo.render(gl);
        shaderProgram.set1i(gl,"useVertexColor",0);
    }

    private void drawMainRingAndHandles(GL3 gl,ShaderProgram shaderProgram) {
        shaderProgram.set1i(gl,"useLighting",0);
        shaderProgram.set1i(gl,"useVertexColor",0);
        shaderProgram.set1i(gl,"useTexture",0);
        float colorScale = cursorOverHandle ? 1:0.75f;
        Color c2 = new Color(
                Math.clamp(color.getRed()/255.0f*colorScale, 0, 1),
                Math.clamp(color.getGreen()/255.0f*colorScale, 0, 1),
                Math.clamp(color.getBlue()/255.0f*colorScale, 0, 1));
        shaderProgram.setColor(gl, "diffuseColor", c2);

        Camera camera = viewport.getActiveCamera();
        var originShift = viewport.isOriginShift();
        var cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        var p = MatrixHelper.getPosition(pivotMatrix);
        var lookAtMatrix = MatrixHelper.lookAt(p,cameraWorldPos);

        drawWhiteCircleFacingCamera(gl,shaderProgram,lookAtMatrix,cameraWorldPos,p,originShift);
        shaderProgram.setColor(gl, "diffuseColor", c2);

        drawArcOfHandle(gl,shaderProgram,lookAtMatrix,cameraWorldPos,originShift);
    }

    private void drawWhiteCircleFacingCamera(GL3 gl, ShaderProgram shaderProgram, Matrix3d lookAtMatrix, Vector3d cameraWorldPos, Vector3d p, boolean originShift) {
        // draw a white circle facing the camera
        Matrix4d m2 = new Matrix4d();
        m2.set(lookAtMatrix);
        m2.setTranslation(p);
        m2.mul(MatrixHelper.createScaleMatrix4(getRingRadiusScaled() *1.05));
        if (originShift) m2 = RenderPassHelper.getOriginShiftedMatrix(m2, cameraWorldPos);
        shaderProgram.setMatrix4d(gl, "modelMatrix", m2);
        shaderProgram.setColor(gl, "diffuseColor", new Color(1,1,1,0.25f));
        ringMesh.render(gl, 1, 360);
        // put the color back where it was.
    }

    /**
     * Draw the arc of the handle that is facing the camera.  That is to say the half of the ring that is nearest to the camera.
     * @param gl the GL context
     * @param shaderProgram the shader program
     * @param lookAtMatrix the matrix that looks at the camera
     * @param cameraWorldPos the camera world position
     * @param originShift is origin shift enabled
     */
    private void drawArcOfHandle(GL3 gl, ShaderProgram shaderProgram, Matrix3d lookAtMatrix, Vector3d cameraWorldPos, boolean originShift) {
        Matrix4d m2 = MatrixHelper.createScaleMatrix4(getRingRadiusScaled());
        m2.mul(pivotMatrix, m2);
        if (originShift) m2 = RenderPassHelper.getOriginShiftedMatrix(m2, cameraWorldPos);
        shaderProgram.setMatrix4d(gl, "modelMatrix", m2);

        Vector3d lookAt = new Vector3d(-lookAtMatrix.m02, -lookAtMatrix.m12, -lookAtMatrix.m22);
        // are we straight at the camera?  if yes, don't bother.
        if( Math.abs(MatrixHelper.getZAxis(pivotMatrix).dot(lookAt)) >= 0.9 ) return;

        double [] angles = getArcAngles(lookAt);
        int start = (int)angles[0];
        int end = (int)angles[1];

        int before = ringMesh.getRenderStyle();
        ringMesh.setRenderStyle(GL3.GL_LINE_STRIP);

        // the start and end angles must be in the range 0...360 for ringMesh.render to work.
        // this means we may have to do two renders if the arc crosses 0 degrees.
        if(end>360) {
            int start2 = 1;
            int end2 = end-360;
            //shaderProgram.setColor(gl, "diffuseColor", Color.ORANGE);
            ringMesh.render(gl, start2, end2-start2+1);
            end=360;
        }
        if(start<1) start=1;
        ringMesh.render(gl, start, end-start+2);
        ringMesh.setRenderStyle(before);
    }

    /**
     * Given a lookAt vector, return the start and end angles of the arc that is facing the camera.
     * @param lookAt the vector from the pivot to the camera.
     * @return an array of two doubles, the start and end angles in degrees.
     */
    private double[] getArcAngles(Vector3d lookAt) {
        var r = Math.atan2(
                rotationAxisY.dot(lookAt),
                rotationAxisX.dot(lookAt));
        int start = (int)Math.toDegrees(r)+90;
        if(start>360) start-=360;
        else if(start<0) start+=360;
        int end = start + 180;
        return new double[]{start,end};
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

    private double getGripRadiusScaled() {
        return gripRadius * localScale;
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
    public void setFrameOfReference(FrameOfReference index) {
        frameOfReference = index;
        if(selectedItems!=null) {
            updatePivotMatrix();
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {}
}
