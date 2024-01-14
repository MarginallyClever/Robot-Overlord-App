package com.marginallyclever.ro3.apps.viewport.viewporttools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.viewporttools.SelectedItems;
import com.marginallyclever.ro3.apps.viewport.viewporttools.ViewportTool;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A tool to rotate {@link Pose} noes in the {@link Viewport}.  It is a
 * combination of three {@link RotateToolOneAxis} viewporttools.</p> *
 */
public class RotateToolMulti implements ViewportTool {
    private final RotateToolOneAxis toolX = new RotateToolOneAxis(new ColorRGB(255,0,0));
    private final RotateToolOneAxis toolY = new RotateToolOneAxis(new ColorRGB(0,255,0));
    private final RotateToolOneAxis toolZ = new RotateToolOneAxis(new ColorRGB(0,0,255));

    private final List<ViewportTool> tools = new ArrayList<>();

    private SelectedItems selectedItems;
    private int frameOfReference = ViewportTool.FRAME_WORLD;

    public RotateToolMulti() {
        super();

        tools.add(toolX);
        tools.add(toolY);
        tools.add(toolZ);
        toolX.setRotation(0);
        toolY.setRotation(1);
        toolZ.setRotation(2);
    }

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Node> list) {
        this.selectedItems = new SelectedItems(list);

        for (ViewportTool t : tools) t.activate(list);

        if (selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }

    private void updatePivotMatrix() {
        setPivotMatrix(MoveUtils.getPivotMatrix(frameOfReference,selectedItems));
    }

    private void setPivotMatrix(Matrix4d pivot) {
        Matrix4d rot = new Matrix4d();

        Matrix4d pivotZ = new Matrix4d(pivot);
        rot.rotZ(Math.toRadians(180));        pivotZ.mul(rot);
        toolZ.setPivotMatrix(pivotZ);

        Matrix4d pivotX = new Matrix4d(pivot);
        //rot.set(new AxisAngle4d(MatrixHelper.getYAxis(pivot), Math.toRadians( 90)));        pivotX.mul(rot);
        //rot.set(new AxisAngle4d(MatrixHelper.getZAxis(pivot), Math.toRadians( 90)));        pivotX.mul(rot);
        rot.rotY(Math.toRadians( 90));        pivotX.mul(rot);
        rot.rotZ(Math.toRadians(-90));        pivotX.mul(rot);
        toolX.setPivotMatrix(pivotX);

        Matrix4d pivotY = new Matrix4d(pivot);
        //rot.set(new AxisAngle4d(MatrixHelper.getXAxis(pivot), Math.toRadians( 90)));        pivotY.mul(rot);
        //rot.set(new AxisAngle4d(MatrixHelper.getZAxis(pivot), Math.toRadians( 90)));        pivotY.mul(rot);
        rot.rotX(Math.toRadians(-90));        pivotY.mul(rot);
        rot.rotZ(Math.toRadians( 90));        pivotY.mul(rot);
        toolY.setPivotMatrix(pivotY);
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        for(ViewportTool t : tools) t.deactivate();
    }

    /**
     * Handles mouse input events for the tool.
     *
     * @param event The MouseEvent object representing the input event.
     */
    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        updatePivotMatrix();

        if(event.getID() == MouseEvent.MOUSE_MOVED) {
            mouseMoved(event);
        } else if(event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if(event.getID() == MouseEvent.MOUSE_DRAGGED) {
            mouseDragged(event);
        } else if(event.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        }
    }

    private boolean twoToolsInUseAtOnce() {
        boolean foundOne=false;
        for(ViewportTool t : tools) {
            if(t.isInUse()) {
                if(foundOne) return true;
                foundOne=true;
            }
        }
        return false;
    }

    private void cancelFurthestTool() {
        // find nearest tool
        ViewportTool nearestTool = null;
        double nearestDistance = Double.MAX_VALUE;

        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        Point3d cameraPosition = new Point3d(MatrixHelper.getPosition(camera.getWorld()));
        for(ViewportTool t : tools) {
            if(t.isInUse()) {
                Point3d point = t.getStartPoint();
                double d = point.distance(cameraPosition);
                if(nearestDistance>d) {
                    nearestDistance=d;
                    nearestTool=t;
                }
            }
        }

        // cancel all others.
        for(ViewportTool t : tools) {
            if(t!=nearestTool) {
                t.cancelUse();
            }
        }
    }

    /**
     * Handles keyboard input events for the tool.
     *
     * @param event The KeyEvent object representing the input event.
     */
    @Override
    public void handleKeyEvent(KeyEvent event) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        updatePivotMatrix();

        for(ViewportTool t : tools) t.handleKeyEvent(event);
    }

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    @Override
    public void update(double deltaTime) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        for(ViewportTool t : tools) t.update(deltaTime);

        updatePivotMatrix();
    }

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl The OpenGL context to systems to.
     */
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        int i = getIndexInUse();
        if(0==i || -1==i) {
            toolX.render(gl,shaderProgram);
        }
        if(1==i || -1==i) {
            toolY.render(gl,shaderProgram);
        }
        if(2==i || -1==i) {
            toolZ.render(gl,shaderProgram);
        }
    }

    @Override
    public void setViewport(Viewport viewport) {
        for(ViewportTool t : tools) t.setViewport(viewport);
    }

    /**
     * Returns the index of the tool in use, or -1 if no tool is in use.
     * @return the index of the tool in use, or -1 if no tool is in use.
     */
    private int getIndexInUse() {
        int i=0;
        for(ViewportTool t : tools) {
            if(t.isInUse()) return i;
            ++i;
        }
        return -1;
    }

    @Override
    public boolean isInUse() {
        return getIndexInUse()>=0;
    }

    @Override
    public void cancelUse() {
        for(ViewportTool t : tools) t.cancelUse();
    }

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        for(ViewportTool t : tools) t.mouseMoved(event);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        for(ViewportTool t : tools) t.mousePressed(event);
        if (twoToolsInUseAtOnce()) cancelFurthestTool();
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        for(ViewportTool t : tools) t.mouseDragged(event);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        for(ViewportTool t : tools) t.mouseReleased(event);
    }

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(int index) {
        frameOfReference = index;
        for (ViewportTool t : tools) t.setFrameOfReference(index);
        updatePivotMatrix();
    }

    @Override
    public void init(GL3 gl3) {
        for (ViewportTool t : tools) t.init(gl3);
    }

    @Override
    public void dispose(GL3 gl3) {
        for (ViewportTool t : tools) t.dispose(gl3);
    }
}
