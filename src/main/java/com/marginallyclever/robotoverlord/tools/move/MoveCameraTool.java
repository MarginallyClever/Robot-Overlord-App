package com.marginallyclever.robotoverlord.tools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.tools.EditorTool;

import javax.swing.*;
import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

/**
 * Moves the active camera around the scene based on mouse and keyboard input.
 * Hold the middle mouse button to move the camera.
 * Hold the SHIFT key to move the camera in the XY plane (strafe aka truck/pedestal relative to camera).
 * Hold the CTRL key to move the camera in the Z direction (dolly).
 */
public class MoveCameraTool implements EditorTool {
    private Viewport viewport;

    /**
     * Must be greater than or equal to zero.
     */
    private final DoubleParameter snapDeadZone = new DoubleParameter("Snap dead zone",100);

    /**
     * Must be greater than or equal to zero.
     */
    private final DoubleParameter snapDegrees = new DoubleParameter("Snap degrees",45);

    /**
     * Must be greater than one.
     */
    private final DoubleParameter wheelScale = new DoubleParameter("Zoom scale",1.25);

    private boolean isMoving=false;
    private boolean isControlDown =false;
    private boolean isShiftDown =false;
    private double previousX, previousY;


    @Override
    public void activate(List<Entity> list) {}

    @Override
    public void deactivate() {}

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if(event.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        } else if(event.getID() == MouseEvent.MOUSE_DRAGGED) {
            mouseDragged(event);
        } else if(event.getID() == MouseEvent.MOUSE_WHEEL) {
            mouseWheelMoved((MouseWheelEvent)event);
        }
    }

    private void updatePrevious(MouseEvent event) {
        previousX = event.getX();
        previousY = event.getY();
    }

    @Override
    public void mouseMoved(MouseEvent event) {}

    @Override
    public void mousePressed(MouseEvent event) {
        if (SwingUtilities.isMiddleMouseButton(event)) {
            isMoving = true;
            updatePrevious(event);
        }
    }
    @Override
    public void mouseDragged(MouseEvent event) {
        if(!SwingUtilities.isMiddleMouseButton(event)) return;

        CameraComponent cameraComponent = viewport.getCamera();
        if(cameraComponent==null) return;
        if(!isMoving) return;

        cameraComponent.setCurrentlyMoving(true);

        double dx = event.getX() - previousX;
        double dy = event.getY() - previousY;
        if(dx==0 && dy==0) return;

        updatePrevious(event);

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
    public void mouseReleased(MouseEvent event) {
        if(SwingUtilities.isMiddleMouseButton(event)) {
            isMoving=false;
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        CameraComponent cameraComponent = viewport.getCamera();
        if(cameraComponent==null) return;

        if(e.getWheelRotation()>0) {
            cameraComponent.setOrbitDistance(cameraComponent.getOrbitDistance() / wheelScale.get());
        } else {
            cameraComponent.setOrbitDistance(cameraComponent.getOrbitDistance() * wheelScale.get());
        }
    }

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
    public void handleKeyEvent(KeyEvent event) {
        if(event.getID() == KeyEvent.KEY_PRESSED) {
            // remember if SHIFT is down
            if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                isShiftDown = true;
            }
            // remember if CTRL is down
            if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
                isControlDown = true;
            }
        } else if(event.getID() == KeyEvent.KEY_RELEASED) {
            // remember if SHIFT is down
            if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                isShiftDown = false;
            }
            // remember if CTRL is down
            if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
                isControlDown = false;
            }
        }
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public boolean isInUse() {
        return isMoving;
    }

    @Override
    public void cancelUse() {
        isMoving=false;
    }

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(int index) {

    }
}
