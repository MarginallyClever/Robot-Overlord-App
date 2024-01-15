package com.marginallyclever.ro3.apps.viewport.viewporttools;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;

import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Interface for {@link Viewport} tools that can be used to visually edit the scene.
 */
public interface ViewportTool {
    int FRAME_WORLD = 0;
    int FRAME_LOCAL = 1;
    int FRAME_CAMERA = 2;
    int FRAME_COUNT = 3;

    /**
     * This method is called when the tool is activated. It receives a list containing the selected
     * nodes and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    void activate(List<Node> list);

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    void deactivate();

    /**
     * Handles mouse input events for the tool.
     *
     * @param event The MouseEvent object representing the input event.
     */
    void handleMouseEvent(MouseEvent event);

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    void update(double deltaTime);

    /**
     * Renders any tool-specific visuals to the 3D scene.
     * @param gl The OpenGL context.
     */
    void render(GL3 gl, ShaderProgram shaderProgram);

    void setViewport(Viewport viewport);

    /**
     * Returns true if the tool is active (was clicked correctly and could be dragged)
     * @return true if the tool is active (was clicked correctly and could be dragged)
     */
    boolean isInUse();

    /**
     * Force cancel the tool.  useful if two viewporttools are activated at once.
     */
    void cancelUse();

    /**
     * Returns the point on the tool clicked by the user.  This is used to determine which tool is closer to the user.
     * @return the point on the tool clicked by the user.
     */
    Point3d getStartPoint();

    void mouseMoved(MouseEvent event);
    void mousePressed(MouseEvent event);
    void mouseDragged(MouseEvent event);
    void mouseReleased(MouseEvent event);

    /**
     * Sets the frame of reference for the tool.
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    void setFrameOfReference(int index);

    /**
     * This is called when the OpenGL context is created.  It should create any resources.
     * @param gl3 the OpenGL context.
     */
    void init(GL3 gl3);

    /**
     * This is called when the OpenGL context is destroyed.  It should release any resources.
     * @param gl3 the OpenGL context.
     */
    void dispose(GL3 gl3);
}
