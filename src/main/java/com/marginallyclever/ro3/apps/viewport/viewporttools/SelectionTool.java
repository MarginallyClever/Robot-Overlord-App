package com.marginallyclever.ro3.apps.viewport.viewporttools;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.raypicking.RayHit;
import com.marginallyclever.ro3.raypicking.RayPickSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A selection tool allows the user to click on the 3D view and change the current {@link Registry#selection}.</p>
 */
public class SelectionTool extends MouseAdapter implements ViewportTool {
    private static final Logger logger = LoggerFactory.getLogger(SelectionTool.class);
    public static final String PICK_POINT_NAME = "pick point";
    private Viewport viewport;
    private boolean isActive=false;

    public SelectionTool() {
        super();
    }

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Node> list) {
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
            if (event.getID() == MouseEvent.MOUSE_CLICKED) {
                pickItemUnderCursor(event.isShiftDown());
            }
        }
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
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

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
     * Force cancel the tool.  useful if two viewporttools are activated at once.
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
    public void setFrameOfReference(FrameOfReference index) {}

    @Override
    public void init(GL3 gl3) {}

    @Override
    public void dispose(GL3 gl3) {}

    private void pickItemUnderCursor(boolean isShiftDown) {
        Node hit = findNodeUnderCursor();
        if(hit!=null) {
            // most MeshInstances are children of a Pose, unless they are in the root.
            Node parent = hit.getParent();
            if(parent instanceof Pose) hit = parent;
        }

        logger.debug((hit==null)?"hit = nothing":"hit = " + hit.getAbsolutePath());

        List<Node> selection = new ArrayList<>(Registry.selection.getList());

        // shift + select to grow/shrink selection
        // aka no shift remove everything except hit
        if(!isShiftDown) {
            // remove all except hit
            for(Node n : selection) {
                if(n!=hit) Registry.selection.remove(n);
            }
        }
        // toggle hit in/out of selection
        if(selection.contains(hit)) {
            Registry.selection.remove(hit);
        } else {
            if(hit!=null) Registry.selection.add(hit);
        }

        logger.debug("selection size {}", Registry.selection.getList().size());
        //TODO UndoSystem.addEvent(new SelectEdit(Clipboard.getSelectedEntities(),selection));
    }

    /**
     * Use ray tracing to find the Node at the cursor position closest to the camera.
     * @return the name of the item under the cursor, or -1 if nothing was picked.
     */
    private Node findNodeUnderCursor() {
        Camera camera = Registry.getActiveCamera();
        if(camera==null) return null;

        Point2d mouse = viewport.getCursorPosition();
        Ray ray = viewport.getRayThroughPoint(camera,mouse.x,mouse.y);
        RayPickSystem rayPickSystem = new RayPickSystem();
        RayHit rayHit = rayPickSystem.getFirstHit(ray);
        if(rayHit == null) return null;
        setPickPoint(ray,rayHit);
        return rayHit.target();
    }

    private void createPickPoint() {
        Node pickPoint = Registry.getScene().findChild(PICK_POINT_NAME);
        if(pickPoint == null) {
            pickPoint = new Pose(PICK_POINT_NAME);
            Registry.getScene().addChild(pickPoint);
        }
    }

    private void setPickPoint(Ray ray, RayHit rayHit) {
        createPickPoint();
        Pose pickPoint = (Pose)Registry.getScene().findChild(PICK_POINT_NAME);

        Vector3d from = ray.getPoint(rayHit.distance());
        Vector3d to = new Vector3d(from);
        to.add(rayHit.normal());
        Matrix4d m2 = new Matrix4d();
        Matrix3d lookAt = MatrixHelper.lookAt(from,to);
        m2.set(lookAt);
        m2.setTranslation(from);
        pickPoint.setLocal(m2);
    }
}
