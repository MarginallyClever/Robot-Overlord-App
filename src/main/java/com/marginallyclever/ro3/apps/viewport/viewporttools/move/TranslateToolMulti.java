package com.marginallyclever.ro3.apps.viewport.viewporttools.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.KSPDirections;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.apps.viewport.viewporttools.SelectedItems;
import com.marginallyclever.ro3.apps.viewport.viewporttools.ViewportTool;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.texture.TextureWithMetadata;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A tool to translate {@link Pose} nodes in the {@link Viewport}.  It is a
 * combination of three {@link TranslateToolOneAxis}.</p>
 */
public class TranslateToolMulti implements ViewportTool {
    private final TranslateToolOneAxis toolRadialIn = new TranslateToolOneAxis(new ColorRGB(255,0,0));
    private final TranslateToolOneAxis toolNormal = new TranslateToolOneAxis(new ColorRGB(0,255,0));
    private final TranslateToolOneAxis toolPrograde = new TranslateToolOneAxis(new ColorRGB(0,0,255));
    private final TranslateToolTwoAxis toolXY = new TranslateToolTwoAxis(new ColorRGB(255,255,0));
    private final TranslateToolTwoAxis toolXZ = new TranslateToolTwoAxis(new ColorRGB(255,0,255));
    private final TranslateToolTwoAxis toolYZ = new TranslateToolTwoAxis(new ColorRGB(0,255,255));
    private final TranslateToolOneAxis toolRetrograde = new TranslateToolOneAxis(new ColorRGB(255,0,0));
    private final TranslateToolOneAxis toolAntiNormal = new TranslateToolOneAxis(new ColorRGB(0,255,0));
    private final TranslateToolOneAxis toolRadialOut = new TranslateToolOneAxis(new ColorRGB(0,0,255));
    private final List<ViewportTool> tools = new ArrayList<>();
    private SelectedItems selectedItems;
    private FrameOfReference frameOfReference = FrameOfReference.WORLD;
    private final TextureWithMetadata texture;
    private Viewport viewport;

    public TranslateToolMulti() {
        super();

        tools.add(toolRadialIn);
        tools.add(toolNormal);
        tools.add(toolPrograde);
        tools.add(toolRetrograde);
        tools.add(toolAntiNormal);
        tools.add(toolRadialOut);
        tools.add(toolXY);
        tools.add(toolXZ);
        tools.add(toolYZ);

        texture = Registry.textureFactory.load("/com/marginallyclever/ro3/apps/viewport/renderpasses/navball.png");
        texture.setDoNotExport(true);
    }

    /**
     * This method is called when the tool is activated. It receives the SelectedItems object containing the selected
     * entities and their initial world poses.
     *
     * @param list The selected items to be manipulated by the tool.
     */
    @Override
    public void activate(List<Node> list) {
        this.selectedItems = new SelectedItems(list);

        for (ViewportTool t : tools) t.activate(list);

        if (selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }

    /**
     * Sets the pivot matrix for the tool.
     * @param pivot The pivot matrix, in world space.
     */
    private void setPivotMatrix(Matrix4d pivot) {
        Camera camera = viewport.getActiveCamera();
        assert camera != null;

        toolRadialIn.setPivotMatrix(createPivotPlaneMatrix(pivot,KSPDirections.RADIAL_IN));
        toolNormal.setPivotMatrix(createPivotPlaneMatrix(pivot,KSPDirections.NORMAL));
        toolPrograde.setPivotMatrix(createPivotPlaneMatrix(pivot,KSPDirections.PROGRADE));

        toolRetrograde.setPivotMatrix(createPivotPlaneMatrix(pivot,KSPDirections.RETROGRADE));
        toolAntiNormal.setPivotMatrix(createPivotPlaneMatrix(pivot,KSPDirections.ANTINORMAL));
        toolRadialOut.setPivotMatrix(createPivotPlaneMatrix(pivot,KSPDirections.RADIAL_OUT));

        Matrix4d rot = new Matrix4d();

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
     * Creates a matrix for the pivot plane.  The pivot plane has a major axis that is different in each case.
     * The Z axis of each plane always faces the active camera.
     * @param pivot The pivot matrix, in world space.
     * @param axis 0 for x, 1 for y, 2 for z.
     * @return The pivot plane matrix.
     */
    private Matrix4d createPivotPlaneMatrix(Matrix4d pivot, KSPDirections axis) {
        Camera camera = viewport.getActiveCamera();
        assert camera != null;

        // the pivot plane shares the same origin as pivot.
        Vector3d o = MatrixHelper.getPosition(pivot);
        // the pivot plane has a major axis that is different in each case.
        Vector3d v;
        switch (axis) {
            case RADIAL_IN: v = MatrixHelper.getXAxis(pivot);  break;
            case NORMAL: v = MatrixHelper.getYAxis(pivot);  break;
            case PROGRADE: v = MatrixHelper.getZAxis(pivot);  break;
            case RADIAL_OUT: v = MatrixHelper.getXAxis(pivot);  v.scale(-1);  break;
            case ANTINORMAL: v = MatrixHelper.getYAxis(pivot);  v.scale(-1);  break;
            case RETROGRADE: v = MatrixHelper.getZAxis(pivot);  v.scale(-1);  break;
            default: throw new InvalidParameterException("axis must be 0...5.");
        };
        // the pivot plane has a z axis that points at the camera.
        Vector3d z = new Vector3d(camera.getPosition());
        z.sub(o);
        double diff = z.dot(v);
        z.x -= v.x * diff;
        z.y -= v.y * diff;
        z.z -= v.z * diff;
        z.normalize();
        // the pivot plane has a y-axis that is perpendicular to v and z.
        Vector3d y = new Vector3d();
        y.cross(z,v);
        // build the matrix for the pivot plane
        Matrix4d result = new Matrix4d();
        result.m00 = v.x;   result.m10 = v.y;   result.m20 = v.z;
        result.m01 = y.x;   result.m11 = y.y;   result.m21 = y.z;
        result.m02 = z.x;   result.m12 = z.y;   result.m22 = z.z;
        result.m03 = o.x;   result.m13 = o.y;   result.m23 = o.z;
        result.m33 = 1;
        return result;
    }

    /**
     * This method is called when the tool is deactivated. It allows the tool to perform any necessary cleanup
     * actions before another tool takes over.
     */
    @Override
    public void deactivate() {
        for (ViewportTool t : tools) t.deactivate();
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
        for (ViewportTool t : tools) {
            if (t.isInUse()) {
                if (foundOne) return true;
                foundOne = true;
            }
        }
        return false;
    }

    private void cancelFurthestTool() {
        // find nearest tool
        ViewportTool nearestTool = null;
        double nearestDistance = Double.MAX_VALUE;

        Camera cameraPose = viewport.getActiveCamera();
        assert cameraPose != null;
        Point3d cameraPosition = new Point3d(cameraPose.getPosition());
        for (ViewportTool t : tools) {
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
        for (ViewportTool t : tools) {
            if (t != nearestTool) {
                t.cancelUse();
            }
        }
    }

    /**
     * Updates the tool's internal state, if necessary.
     *
     * @param deltaTime Time elapsed since the last update.
     */
    @Override
    public void update(double deltaTime) {
        if (selectedItems == null || selectedItems.isEmpty()) return;

        for (ViewportTool t : tools) t.update(deltaTime);

        updatePivotMatrix();
    }

    private void updatePivotMatrix() {
        setPivotMatrix(MoveUtils.getPivotMatrix(frameOfReference,selectedItems,viewport.getActiveCamera()));
    }

    /**
     * Renders any tool-specific visuals to the 3D scene.
     *
     * @param gl The OpenGL context to systems to.
     */
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if( selectedItems == null || selectedItems.isEmpty() ) return;
        if( !MoveUtils.listContainsAPose(selectedItems.getNodes()) ) return;

        // RADIAL_IN
        toolRadialIn.setTexture(texture,new Rectangle2D.Double(0.50,0.75,0.25,0.25));
        // NORMAL
        toolNormal.setTexture(texture,new Rectangle2D.Double(0,0.25,0.25,0.25));
        // PROGRADE
        toolPrograde.setTexture(texture,new Rectangle2D.Double(0,0.75,0.25,0.25));

        toolRetrograde.setTexture(texture, new Rectangle2D.Double(0,0.50,0.25,0.25));
        toolAntiNormal.setTexture(texture, new Rectangle2D.Double(0,0,0.25,0.25));
        toolRadialOut.setTexture(texture, new Rectangle2D.Double(0.5,0.5,0.25,0.25));

        int i = getIndexInUse();
        if(-1==i) {
            for(ViewportTool t : tools) {
                t.render(gl, shaderProgram);
            }
        } else {
            tools.stream().filter(ViewportTool::isInUse).forEach(t -> t.render(gl, shaderProgram));
        }
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
        for (ViewportTool t : tools) t.setViewport(viewport);
    }

    /**
     * Returns the index of the tool in use, or -1 if no tool is in use.
     *
     * @return the index of the tool in use, or -1 if no tool is in use.
     */
    private int getIndexInUse() {
        int i = 0;
        for (ViewportTool t : tools) {
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
        for (ViewportTool t : tools) t.cancelUse();
    }

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        for (ViewportTool t : tools) t.mouseMoved(event);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        for (ViewportTool t : tools) t.mousePressed(event);
        if (twoToolsInUseAtOnce()) cancelFurthestTool();
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        for (ViewportTool t : tools) t.mouseDragged(event);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        for (ViewportTool t : tools) t.mouseReleased(event);
    }

    /**
     * Sets the frame of reference for the tool.
     * @param index a {@link FrameOfReference}.
     */
    @Override
    public void setFrameOfReference(FrameOfReference index) {
        frameOfReference = index;
        for (ViewportTool t : tools) t.setFrameOfReference(index);
        updatePivotMatrix();
    }

    @Override
    public void init(GL3 gl3) {
        for (ViewportTool t : tools) t.init(gl3);
    }

    @Override
    public void dispose(GL3 gl3) {
        for (ViewportTool t : tools) t.dispose(gl3);
        texture.unload();
    }
}
