package com.marginallyclever.ro3.apps.viewport.viewporttool.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.apps.viewport.viewporttool.SelectedItems;
import com.marginallyclever.ro3.apps.viewport.viewporttool.ViewportTool;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.shader.ShaderProgram;

import javax.swing.*;
import javax.vecmath.Point3d;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A Viewport tool that combines translation and rotation on all axes.
 */
public class MoveMultiTool implements ViewportTool {
    private final RotateToolMulti rotateToolMulti = new RotateToolMulti();
    private final TranslateToolMulti translateToolMulti = new TranslateToolMulti();
    private final List<ViewportTool> tools = new ArrayList<>();
    private SelectedItems selectedItems;
    private FrameOfReference frameOfReference = FrameOfReference.WORLD;
    private Viewport viewport;

    public MoveMultiTool() {
        super();

        tools.add(rotateToolMulti);
        tools.add(translateToolMulti);
    }

    @Override
    public void activate(SelectedItems list) {
        this.selectedItems = list;

        for (ViewportTool t : tools) t.activate(list);

        if (selectedItems.isEmpty()) return;
    }

    @Override
    public void deactivate() {
        for(ViewportTool t : tools) t.deactivate();
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        switch(event.getID()) {
            case MouseEvent.MOUSE_MOVED   : mouseMoved   (event); break;
            case MouseEvent.MOUSE_PRESSED : mousePressed (event); break;
            case MouseEvent.MOUSE_DRAGGED : mouseDragged (event); break;
            case MouseEvent.MOUSE_RELEASED: mouseReleased(event); break;
            default: break;
        }
    }

    @Override
    public void update(double deltaTime) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        for(ViewportTool t : tools) t.update(deltaTime);
    }

    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if (selectedItems == null || selectedItems.isEmpty()) return;
        if( !MoveUtils.listContainsAPose(selectedItems.getNodes()) ) return;

        int i = getIndexInUse();
        for(ViewportTool t : tools) {
            // if no tools is in use or this tool is in use, render it.
            if(i==-1 || tools.indexOf(t)==i) {
                t.render(gl, shaderProgram);
            }
        }
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
        for(ViewportTool t : tools) t.setViewport(viewport);
    }

    @Override
    public boolean isInUse() {
        return rotateToolMulti.isInUse() || translateToolMulti.isInUse();
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

    @Override
    public void setFrameOfReference(FrameOfReference index) {
        frameOfReference = index;
        for (ViewportTool t : tools) t.setFrameOfReference(index);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        for (ViewportTool t : tools) t.getComponents(list);
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

    private void cancelFurthestTool() {
        // find nearest tool
        ViewportTool nearestTool = null;
        double nearestDistance = Double.MAX_VALUE;

        Camera camera = viewport.getActiveCamera();
        assert camera != null;
        Point3d cameraPosition = new Point3d(MatrixHelper.getPosition(camera.getWorld()));
        for(ViewportTool t : tools) {
            if(t.isInUse()) {
                Point3d point = t.getStartPoint();
                if(point==null) continue;
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
}
