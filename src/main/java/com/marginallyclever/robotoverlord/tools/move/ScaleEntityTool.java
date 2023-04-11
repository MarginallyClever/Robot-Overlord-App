package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Viewport;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectedItems;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ScaleEntityTool implements EditorTool {
    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param selectedItems The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(SelectedItems selectedItems) {

    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {

    }

    @Override
    public void handleMouseEvent(MouseEvent e) {

    }

    @Override
    public void handleKeyEvent(KeyEvent e) {

    }

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    @Override
    public void update(double deltaTime) {

    }

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl2
     */
    @Override
    public void render(GL2 gl2) {

    }

    @Override
    public void setViewport(Viewport viewport) {

    }
}
