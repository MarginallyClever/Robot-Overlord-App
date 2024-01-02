package com.marginallyclever.ro3.apps.render.viewporttools;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class Compass3D implements ViewportTool {
    private static final Logger logger = LoggerFactory.getLogger(Compass3D.class);

    /**
     * The viewport to which this tool is attached.
     */
    private Viewport viewport;
    private final Mesh circleMesh = new Mesh();

    public Compass3D() {
        super();
        //circleMesh.
    }

    @Override
    public void activate(List<Node> list) {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void handleMouseEvent(MouseEvent event) {

    }

    @Override
    public void handleKeyEvent(KeyEvent event) {

    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void render(GL3 gl, ShaderProgram shaderProgram) {

    }

    @Override
    public void setViewport(Viewport viewport) {

    }

    @Override
    public boolean isInUse() {
        return false;
    }

    @Override
    public void cancelUse() {

    }

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {

    }

    @Override
    public void mousePressed(MouseEvent event) {

    }

    @Override
    public void mouseDragged(MouseEvent event) {

    }

    @Override
    public void mouseReleased(MouseEvent event) {

    }

    @Override
    public void setFrameOfReference(int index) {

    }

    @Override
    public void init(GL3 gl3) {

    }

    @Override
    public void dispose(GL3 gl3) {

    }
}
