package com.marginallyclever.robotoverlord.tools.move;

import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Moves the active camera around the scene based on mouse and keyboard input.
 * Hold the middle mouse button to move the camera.
 * Hold the SHIFT key to move the camera in the XY plane (strafe aka truck/pedestal relative to camera).
 * Hold the CTRL key to move the camera in the Z direction (dolly).
 */
public class MoveCameraTool implements EditorTool {
    private CameraComponent cameraComponent;

    /**
     * Must be greater than or equal to zero.
     */
    private final DoubleEntity snapDeadZone = new DoubleEntity("Snap dead zone",100);

    /**
     * Must be greater than or equal to zero.
     */
    private final DoubleEntity snapDegrees = new DoubleEntity("Snap degrees",45);

    /**
     * Must be greater than one.
     */
    private final DoubleEntity wheelScale = new DoubleEntity("Zoom scale",1.25);

    private boolean isMoving=false;
    private boolean isControlDown =false;
    private boolean isShiftDown =false;
    private double previousX, previousY;


    @Override
    public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isMiddleMouseButton(e)) {
            isMoving=true;
            updatePrevious(e);
        }
    }

    private void updatePrevious(MouseEvent e) {
        previousX = e.getX();
        previousY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(SwingUtilities.isMiddleMouseButton(e)) {
            isMoving=false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(cameraComponent==null) return;
        if(!isMoving) return;

        cameraComponent.setCurrentlyMoving(true);

        double dx = e.getX() - previousX;
        double dy = e.getY() - previousY;
        if(dx==0 && dy==0) return;

        updatePrevious(e);

        if(isShiftDown) {
            cameraComponent.pedestalCamera(dy);
            cameraComponent.truckCamera(dx);
        } else if(isControlDown) {
            cameraComponent.dollyCamera(dy);
        } else {
            cameraComponent.orbitCamera(dx,dy);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(cameraComponent==null) return;

        if(e.getWheelRotation()>0) {
            cameraComponent.setOrbitDistance(cameraComponent.getOrbitDistance() / wheelScale.get());
        } else {
            cameraComponent.setOrbitDistance(cameraComponent.getOrbitDistance() * wheelScale.get());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // remember if SHIFT is down
        if(e.getKeyCode()==KeyEvent.VK_SHIFT) {
            isShiftDown =true;
        }
        // remember if CTRL is down
        if(e.getKeyCode()==KeyEvent.VK_CONTROL) {
            isControlDown =true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // remember if SHIFT is down
        if(e.getKeyCode()==KeyEvent.VK_SHIFT) {
            isShiftDown =false;
        }
        // remember if CTRL is down
        if(e.getKeyCode()==KeyEvent.VK_CONTROL) {
            isControlDown =false;
        }
    }

    public void setCamera(CameraComponent cameraComponent) {
        this.cameraComponent = cameraComponent;
    }
}
