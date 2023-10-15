package com.marginallyclever.robotoverlord.tools;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.SelectEdit;
import com.marginallyclever.robotoverlord.systems.RayPickSystem;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SelectionTool extends MouseAdapter implements EditorTool {
    private static final Logger logger = LoggerFactory.getLogger(SelectionTool.class);
    public static final String PICK_POINT_NAME = "pick point";
    private final EntityManager entityManager;
    private final Viewport viewport;
    private boolean isActive=false;
    private boolean isShiftDown=false;

    public SelectionTool(EntityManager entityManager,Viewport viewport) {
        super();
        this.entityManager = entityManager;
        this.viewport = viewport;
    }

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Entity> list) {
        isActive=true;
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        isActive=false;
    }

    /**
     * Handles mouse input events for the tool.
     *
     * @param event The MouseEvent object representing the input event.
     */
    @Override
    public void handleMouseEvent(MouseEvent event) {
        if(!isActive) return;

        // if they dragged the cursor around before releasing the mouse button, don't pick.
        if (event.getClickCount() == 1) {  // 2 for double click
            pickItemUnderCursor();
        }
    }

    /**
     * Handles keyboard input events for the tool.
     *
     * @param event The KeyEvent object representing the input event.
     */
    @Override
    public void handleKeyEvent(KeyEvent event) {
        if(event.getID() == KeyEvent.KEY_PRESSED) {
            handleKeyPressed(event);
        } else if(event.getID() == KeyEvent.KEY_RELEASED) {
            handleKeyReleased(event);
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.VK_SHIFT) isShiftDown=true;
    }

    private void handleKeyReleased(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.VK_SHIFT) isShiftDown=false;
    }


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
     * @param gl The OpenGL context.
     */
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {}

    @Override
    public void setViewport(Viewport viewport) {}

    /**
     * Returns true if the tool is active (was clicked correctly and could be dragged)
     *
     * @return true if the tool is active (was clicked correctly and could be dragged)
     */
    @Override
    public boolean isInUse() {
        return false;
    }

    /**
     * Force cancel the tool.  useful if two tools are activated at once.
     */
    @Override
    public void cancelUse() {}

    /**
     * Returns the point on the tool clicked by the user.  This is used to determine which tool is closer to the user.
     *
     * @return the point on the tool clicked by the user.
     */
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
    public void setFrameOfReference(int index) {}

    private void pickItemUnderCursor() {
        Entity found = findEntityUnderCursor();
        logger.debug((found==null)?"found=null":"found=" + found.getName());

        List<Entity> list = new ArrayList<>();
        // shift + select to grow selection
        if(isShiftDown) {
            list.addAll(Clipboard.getSelectedEntities());
            if(found==null) return;  // no change.

            if(list.contains(found)) {
                list.remove(found);
            } else {
                list.add(found);
            }
        } else {
            if (found != null) list.add(found);
        }

        UndoSystem.addEvent(new SelectEdit(Clipboard.getSelectedEntities(),list));
    }

    /**
     * Use ray tracing to find the Entity at the cursor position closest to the camera.
     * @return the name of the item under the cursor, or -1 if nothing was picked.
     */
    private Entity findEntityUnderCursor() {
        CameraComponent camera = viewport.getCamera();
        if(camera==null) return null;

        Ray ray = viewport.getRayThroughCursor();

        RayPickSystem rayPickSystem = new RayPickSystem(entityManager);
        RayHit rayHit = rayPickSystem.getFirstHit(ray);
        if(rayHit == null) return null;

        setPickPoint(ray,rayHit);
        return rayHit.target.getEntity();
    }

    private void createPickPoint() {
        Entity pickPoint = entityManager.getRoot().findChildNamed(PICK_POINT_NAME);
        if(pickPoint == null) {
            pickPoint = new Entity(PICK_POINT_NAME);
            entityManager.addEntityToParent(pickPoint,entityManager.getRoot());
        }
    }

    private void setPickPoint(Ray ray, RayHit rayHit) {
        createPickPoint();
        Entity pickPoint = entityManager.getRoot().findChildNamed(PICK_POINT_NAME);

        Vector3d from = ray.getPoint(rayHit.distance);
        Vector3d to = new Vector3d(from);
        to.add(rayHit.normal);
        Matrix4d m = MatrixHelper.createIdentityMatrix4();
        Matrix4d m2 = new Matrix4d();
        Matrix3d lookAt = MatrixHelper.lookAt(from,to);
        m2.set(lookAt);
        m2.setTranslation(from);
        pickPoint.getComponent(PoseComponent.class).setWorld(m2);
    }
}
