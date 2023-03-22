package com.marginallyclever.robotoverlord.tools;

import com.jogamp.opengl.GL2;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Interface for tools that can be used to visually edit the scene.
 * @since 2.3.0
 * @author Dan Royer
 */
public interface EditorTool {
    public void mousePressed(MouseEvent e);
    public void mouseReleased(MouseEvent e);
    public void mouseDragged(MouseEvent e);
    public void mouseMoved(MouseEvent e);
    public void keyPressed(KeyEvent e);
    public void keyReleased(KeyEvent e);

    void mouseWheelMoved(MouseWheelEvent e);
}
