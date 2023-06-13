package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A tool to scale entities in the editor.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ScaleEntityTool implements EditorTool {
    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Entity> list) {}

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {}

    @Override
    public void handleMouseEvent(MouseEvent event) {}

    @Override
    public void handleKeyEvent(KeyEvent event) {}

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
     * @param gl
     */
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {}

    @Override
    public void setViewport(Viewport viewport) {}

    @Override
    public boolean isInUse() {
        return false;
    }

    @Override
    public void cancelUse() {}

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {}

    @Override
    public void mousePressed(MouseEvent event) {}

    @Override
    public void mouseDragged(MouseEvent event) {}

    @Override
    public void mouseReleased(MouseEvent event) {}

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(int index) {}
}
