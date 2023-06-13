package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A tool to translate entities in the editor.  It is a combination of three {@link TranslateEntityToolOneAxis} tools.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class TranslateEntityMultiTool implements EditorTool {
    private Viewport viewport;
    private final TranslateEntityToolOneAxis toolX = new TranslateEntityToolOneAxis(new ColorRGB(255,0,0));
    private final TranslateEntityToolOneAxis toolY = new TranslateEntityToolOneAxis(new ColorRGB(0,255,0));
    private final TranslateEntityToolOneAxis toolZ = new TranslateEntityToolOneAxis(new ColorRGB(0,0,255));
    private final TranslateEntityToolTwoAxis toolXY = new TranslateEntityToolTwoAxis(new ColorRGB(255,255,0));
    private final TranslateEntityToolTwoAxis toolXZ = new TranslateEntityToolTwoAxis(new ColorRGB(255,0,255));
    private final TranslateEntityToolTwoAxis toolYZ = new TranslateEntityToolTwoAxis(new ColorRGB(0,255,255));

    private final List<EditorTool> tools = new ArrayList<>();

    private SelectedItems selectedItems;
    private int frameOfReference = EditorTool.FRAME_WORLD;

    public TranslateEntityMultiTool() {
        super();
        tools.add(toolX);
        tools.add(toolY);
        tools.add(toolZ);
        tools.add(toolXY);
        tools.add(toolXZ);
        tools.add(toolYZ);
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

        for (EditorTool t : tools) t.activate(list);

        if (selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }

    private void setPivotMatrix(Matrix4d pivot) {
        toolX.setPivotMatrix(pivot);

        Matrix4d rot = new Matrix4d();

        Matrix4d pivotY = new Matrix4d(pivot);
        rot.rotZ(Math.toRadians(90));
        pivotY.mul(rot);
        toolY.setPivotMatrix(pivotY);

        Matrix4d pivotZ = new Matrix4d(pivot);
        rot.rotY(Math.toRadians(-90));
        pivotZ.mul(rot);
        toolZ.setPivotMatrix(pivotZ);

        Matrix4d pivotYZ = new Matrix4d(pivot);
        rot.rotY(Math.toRadians(-90));
        pivotYZ.mul(rot);
        toolYZ.setPivotMatrix(pivotYZ);

        Matrix4d pivotXY = new Matrix4d(pivot);
        rot.rotZ(Math.toRadians(0));
        pivotXY.mul(rot);
        toolXY.setPivotMatrix(pivotXY);

        Matrix4d pivotXZ = new Matrix4d(pivot);
        rot.rotX(Math.toRadians(-90));
        pivotXZ.mul(rot);
        rot.rotZ(Math.toRadians(-90));
        pivotXZ.mul(rot);
        toolXZ.setPivotMatrix(pivotXZ);
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        for (EditorTool t : tools) t.deactivate();
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

    private boolean twoToolsInUseAtOnce() {
        boolean foundOne = false;
        for (EditorTool t : tools) {
            if (t.isInUse()) {
                if (foundOne) return true;
                foundOne = true;
            }
        }
        return false;
    }

    private void cancelFurthestTool() {
        // find nearest tool
        EditorTool nearestTool = null;
        double nearestDistance = Double.MAX_VALUE;

        PoseComponent cameraPose = viewport.getCamera().getEntity().getComponent(PoseComponent.class);
        Point3d cameraPosition = new Point3d(MatrixHelper.getPosition(cameraPose.getWorld()));
        for (EditorTool t : tools) {
            if (t.isInUse()) {
                Point3d point = t.getStartPoint();
                double d = point.distance(cameraPosition);
                if (nearestDistance > d) {
                    nearestDistance = d;
                    nearestTool = t;
                }
            }
        }

        // cancel all others.
        for (EditorTool t : tools) {
            if (t != nearestTool) {
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

        for (EditorTool t : tools) t.handleKeyEvent(event);
    }

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    @Override
    public void update(double deltaTime) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        for (EditorTool t : tools) t.update(deltaTime);

        updatePivotMatrix();
    }

    private void updatePivotMatrix() {
        setPivotMatrix(EditorUtils.getPivotMatrix(frameOfReference,viewport,selectedItems));
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
        if (0 == i || -1 == i) toolX.render(gl,shaderProgram);
        if (1 == i || -1 == i) toolY.render(gl,shaderProgram);
        if (2 == i || -1 == i) toolZ.render(gl,shaderProgram);
        if (3 == i || -1 == i) toolXY.render(gl,shaderProgram);
        if (4 == i || -1 == i) toolXZ.render(gl,shaderProgram);
        if (5 == i || -1 == i) toolYZ.render(gl,shaderProgram);
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
        for (EditorTool t : tools) t.setViewport(viewport);
    }

    /**
     * Returns the index of the tool in use, or -1 if no tool is in use.
     *
     * @return the index of the tool in use, or -1 if no tool is in use.
     */
    private int getIndexInUse() {
        int i = 0;
        for (EditorTool t : tools) {
            if (t.isInUse()) return i;
            ++i;
        }
        return -1;
    }

    @Override
    public boolean isInUse() {
        return getIndexInUse() >= 0;
    }

    @Override
    public void cancelUse() {
        for (EditorTool t : tools) t.cancelUse();
    }

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        for (EditorTool t : tools) t.mouseMoved(event);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        for (EditorTool t : tools) t.mousePressed(event);
        if (twoToolsInUseAtOnce()) cancelFurthestTool();
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        for (EditorTool t : tools) t.mouseDragged(event);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        for (EditorTool t : tools) t.mouseReleased(event);
    }

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(int index) {
        frameOfReference = index;
        for (EditorTool t : tools) t.setFrameOfReference(index);
        updatePivotMatrix();
    }
}
