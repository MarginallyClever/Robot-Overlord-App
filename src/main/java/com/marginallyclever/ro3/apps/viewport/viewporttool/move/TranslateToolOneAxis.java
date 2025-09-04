package com.marginallyclever.ro3.apps.viewport.viewporttool.move;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.FrameOfReference;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import com.marginallyclever.ro3.apps.viewport.renderpass.RenderPassHelper;
import com.marginallyclever.ro3.apps.viewport.viewporttool.SelectedItems;
import com.marginallyclever.ro3.apps.viewport.viewporttool.ViewportTool;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.texture.TextureWithMetadata;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A tool for moving {@link Pose} nodes in the {@link Viewport} along a single axis.
 *
 */
public class TranslateToolOneAxis implements ViewportTool {
    private static double handleLength = 5;
    private static double gripRadius = 0.5;
    private double localScale = 1;

    /**
     * The viewport to which this tool is attached.
     */
    private Viewport viewport;

    /**
     * The list of entities to adjust.
     */
    private SelectedItems selectedItems;

    /**
     * Is the user dragging the mouse after successfully picking the handle?
     */
    private boolean dragging = false;

    /**
     * The point on the translation plane where the handle was clicked.
     */
    private Point3d startPoint;

    private final Point3d previousPoint = new Point3d();

    /**
     * The plane on which the user is picking.
     */
    private final Plane translationPlane = new Plane();

    /**
     * The axis along which the user is translating.
     */
    private final Vector3d translationAxis = new Vector3d();
    private final Matrix4d pivotMatrix = MatrixHelper.createIdentityMatrix4();
    private boolean cursorOverHandle = false;
    private FrameOfReference frameOfReference = FrameOfReference.WORLD;
    private final Color color;
    private TextureWithMetadata texture;
    private final Mesh handleLineMesh = new Mesh(GL3.GL_LINES);
    private final Sphere handleSphere = new Sphere();
    private final Mesh quad = new Mesh(GL3.GL_QUADS);

    public TranslateToolOneAxis(Color color) {
        super();
        this.color = color;

        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "TranslateToolOneAxis.handleLineMesh", handleLineMesh);
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "TranslateToolOneAxis.handleSphere", handleSphere);
        Registry.meshFactory.addToPool(Lifetime.APPLICATION, "TranslateToolOneAxis.quad", quad);

        // handle line
        handleLineMesh.addVertex(0, 0, 0);
        handleLineMesh.addVertex((float)1.0, 0, 0);

        quad.addVertex(-1, -1, 0);
        quad.addVertex(1, -1, 0);
        quad.addVertex(1, 1, 0);
        quad.addVertex(-1, 1, 0);
        quad.addTexCoord(0, 0);
        quad.addTexCoord(1, 0);
        quad.addTexCoord(1, 1);
        quad.addTexCoord(0, 1);
    }

    @Override
    public void activate(SelectedItems list) {
        selectedItems = list;

        if(selectedItems.isEmpty()) return;

        updatePivotMatrix();
    }


    public void setPivotMatrix(Matrix4d pivot) {
        pivotMatrix.set(pivot);
        translationPlane.set(MatrixHelper.getXYPlane(pivot));
        translationAxis.set(MatrixHelper.getXAxis(pivot));
    }

    @Override
    public void deactivate() {
        dragging = false;
        selectedItems = null;
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (event.getID() == MouseEvent.MOUSE_MOVED) {
            mouseMoved(event);
        } else if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if (event.getID() == MouseEvent.MOUSE_DRAGGED && dragging) {
            mouseDragged(event);
        } else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        cursorOverHandle = isCursorOverHandle(event.getX(),event.getY());
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if(cursorOverHandle) {
            startPoint = MoveUtils.getPointOnPlaneFromCursor(translationPlane,viewport,event.getX(), event.getY());
            if(startPoint==null) return;
            dragging = true;
            previousPoint.set(startPoint);
            selectedItems.savePose();
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if(!dragging) return;

        Point3d currentPoint = MoveUtils.getPointOnPlaneFromCursor(translationPlane,viewport,event.getX(), event.getY());
        if(currentPoint==null) return;

        Point3d nearestPoint = getNearestPointOnAxis(currentPoint);

        Vector3d delta = new Vector3d();
        delta.sub(nearestPoint, previousPoint);
        previousPoint.set(nearestPoint);

        var poses = new ArrayList<Pose>();
        for(Node node : selectedItems.getNodes()) {
            if(node instanceof Pose pose) {
                poses.add(pose);
            }
        }

        // Apply the delta to the selected items.
        UndoSystem.addEvent(new TranslatePoseCommand(poses,delta));
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if(!dragging) return;

        dragging = false;
        if(selectedItems!=null) {
            MoveUtils.updateUndoState(selectedItems);
            selectedItems.savePose();
        }
    }

    /**
     * Sets the frame of reference for the tool.
     *
     * @param index 0 for world, 1 for local, 2 for camera.
     */
    @Override
    public void setFrameOfReference(FrameOfReference index) {
        frameOfReference = index;
        if(selectedItems!=null) {
            updatePivotMatrix();
        }
    }

    private void updatePivotMatrix() {
        setPivotMatrix(MoveUtils.getPivotMatrix(frameOfReference,selectedItems,viewport.getActiveCamera()));
    }

    private Point3d getNearestPointOnAxis(Point3d currentPoint) {
        // get the cross product of the translationAxis and the translationPlane's normal
        Vector3d orthogonal = new Vector3d();
        orthogonal.cross(translationAxis, translationPlane.normal);
        orthogonal.normalize();
        // diff is the vector from the start point to the current point.  it may drift off the axis.
        Vector3d diff = new Vector3d();
        diff.sub(currentPoint,startPoint);
        double d = diff.dot(orthogonal);
        // remove the component of diff that is orthogonal to the translationAxis so the motion stays on-axis.
        orthogonal.scale(d);
        diff.sub(orthogonal);
        diff.add(startPoint);

        return new Point3d(diff);
    }

    private boolean isCursorOverHandle(int x,int y) {
        if(selectedItems==null || selectedItems.isEmpty()) return false;

        Point3d point = MoveUtils.getPointOnPlaneFromCursor(translationPlane,viewport,x,y);
        if (point == null) return false;

        // Check if the point is within the handle's bounds
        Vector3d diff = new Vector3d(translationAxis);
        diff.scaleAdd(getHandleLengthScaled(), MatrixHelper.getPosition(pivotMatrix));
        diff.sub(point);
        var isIn = diff.lengthSquared() < getGripRadiusScaled()*getGripRadiusScaled();
        //System.out.println(diff.lengthSquared()+" "+getGripRadiusScaled()*getGripRadiusScaled());
        return isIn;
    }

    @Override
    public void update(double deltaTime) {
        // Update the tool's state, if necessary
        if(selectedItems!=null) updatePivotMatrix();

        updateLocalScale();
    }

    private void updateLocalScale() {
        Camera cam = viewport.getActiveCamera();
        if(cam!=null) {
            Vector3d cameraPoint = cam.getPosition();
            Vector3d pivotPoint = MatrixHelper.getPosition(pivotMatrix);
            pivotPoint.sub(cameraPoint);
            localScale = pivotPoint.length() * 0.035;  // TODO * InteractionPreferences.toolScale;
        }
    }

    // Render the translation handle on the axis
    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {
        if (selectedItems == null || selectedItems.isEmpty()) return;
        if( !MoveUtils.listContainsAPose(selectedItems.getNodes()) ) return;

        float colorScale = cursorOverHandle ? 1.0f : 0.75f;
        var r = color.getRed()/255.0f;
        var g = color.getGreen()/255.0f;
        var b = color.getBlue()/255.0f;
        var a = color.getAlpha()/255.0f;
        Color c2 = new Color(r * colorScale, g * colorScale, b * colorScale, a);
        shaderProgram.setColor(gl, "diffuseColor", c2);
        shaderProgram.set1i(gl,"useTexture",0);
        shaderProgram.set1i(gl,"useLighting",0);
        shaderProgram.set1i(gl,"useVertexColor",0);

        Camera camera = viewport.getActiveCamera();
        var originShift = viewport.isOriginShift();
        var cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());

        // handle
        var m = new Matrix4d(pivotMatrix);
        m.mul(m,MatrixHelper.createScaleMatrix4(getHandleLengthScaled()));
        if(originShift) m = RenderPassHelper.getOriginShiftedMatrix(m, cameraWorldPos);
        shaderProgram.setMatrix4d(gl,"modelMatrix",m);
        handleLineMesh.render(gl);

        // sphere at end of handle
        var m2 = MatrixHelper.createIdentityMatrix4();
        m2.m03 += getHandleLengthScaled();
        m2.mul(pivotMatrix,m2);
        m2.mul(m2,MatrixHelper.createScaleMatrix4(getGripRadiusScaled()));
        Matrix4d m2t = new Matrix4d(m2);
        if(originShift) m2t = RenderPassHelper.getOriginShiftedMatrix(m2t, cameraWorldPos);
        shaderProgram.setMatrix4d(gl,"modelMatrix",m2t);
        handleSphere.render(gl);
/*
        if(texture!=null) {
            quad.updateVertexBuffers(gl);
            // set the model matrix to be the camera matrix so the handle is always facing the camera.
            Matrix4d model = camera.getWorld();
            model.setTranslation(MatrixHelper.getPosition(m2));
            model.mul(model, MatrixHelper.createScaleMatrix4(getGripRadiusScaled()));

            if(originShift) model = RenderPassHelper.getOriginShiftedMatrix(model, cameraWorldPos);
            shaderProgram.setMatrix4d(gl,"modelMatrix",model);
            shaderProgram.set1i(gl,"diffuseTexture",0);
            shaderProgram.set1i(gl,"useTexture",1);
            texture.use(shaderProgram);
            quad.render(gl);
            shaderProgram.set1i(gl,"useTexture",0);
        } else */
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public boolean isInUse() {
        return dragging;
    }

    @Override
    public void cancelUse() {
        dragging = false;
    }

    @Override
    public Point3d getStartPoint() {
        return startPoint;
    }


    private double getHandleLengthScaled() {
        return handleLength * localScale;
    }

    private double getGripRadiusScaled() {
        return gripRadius * localScale;
    }

    public void setTexture(TextureWithMetadata texture, Rectangle2D textureBounds) {
        this.texture = texture;
        double u = textureBounds.getX();
        double v = textureBounds.getY();
        double w = textureBounds.getWidth();
        double h = textureBounds.getHeight();
        // update the quad texture coordinates.
        quad.setTexCoord(0,u,v);
        quad.setTexCoord(1,u+w,v);
        quad.setTexCoord(2,u+w,v+h);
        quad.setTexCoord(3,u,v+h);
    }

    @Override
    public void getComponents(List<JPanel> list) {}
}
