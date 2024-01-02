package com.marginallyclever.ro3.apps.render.viewporttools;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.render.ShaderProgram;
import com.marginallyclever.ro3.apps.render.Viewport;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.shapes.CircleXY;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class Compass3D implements ViewportTool {
    /**
     * The viewport to which this tool is attached.
     */
    private Viewport viewport;
    private final int handleLength = 30;
    private final int handleRadius = 8;
    private final Mesh gizmoMesh = MatrixHelper.createMesh(handleLength);
    private final Mesh circleMesh = new CircleXY();
    private final Mesh quadMesh = new Mesh();
    private final TextureWithMetadata texture;

    public Compass3D() {
        super();
        texture = Registry.textureFactory.load("/com/marginallyclever/ro3/apps/render/viewporttools/axisLetters.png");

        quadMesh.setRenderStyle(GL3.GL_QUADS);

        int gridSize = 4;
        float step = 1.0f / gridSize;

        for (int j = 0; j < gridSize; j++) {
            float v = j * step;
            for (int i = 0; i < gridSize; i++) {
                float u = i * step;

                // Add vertices
                quadMesh.addVertex(-1, -1, 0);
                quadMesh.addVertex(1, -1, 0);
                quadMesh.addVertex(1, 1, 0);
                quadMesh.addVertex(-1, 1, 0);

                // Add texture coordinates
                quadMesh.addTexCoord(u, v);
                quadMesh.addTexCoord(u + step, v);
                quadMesh.addTexCoord(u + step, v + step);
                quadMesh.addTexCoord(u, v + step);
            }
        }
    }

    @Override
    public void activate(List<Node> list) {}

    @Override
    public void deactivate() {}

    @Override
    public void handleMouseEvent(MouseEvent event) {}

    @Override
    public void handleKeyEvent(KeyEvent event) {}

    @Override
    public void update(double deltaTime) {}

    @Override
    public void render(GL3 gl3, ShaderProgram shaderProgram) {
        shaderProgram.setColor(gl3,"lightColor", Color.WHITE);
        shaderProgram.setColor(gl3,"specularColor",Color.DARK_GRAY);
        shaderProgram.setColor(gl3,"ambientColor",Color.BLACK);
        shaderProgram.set1i(gl3,"useLighting",0);
        shaderProgram.set1i(gl3,"diffuseTexture",0);

        int compassRadius = 50;
        // set the projection matrix such that the drawing area is the top right corner of the viewport.
        double w = viewport.getWidth()/2d;
        double h = viewport.getHeight()/2d;
        double px = -w + compassRadius;
        double py = -h + compassRadius;
        Matrix4d projection = MatrixHelper.orthographicMatrix4d( px-w, px+w, py-h, py+h, -50.0, 50);
        shaderProgram.setMatrix4d(gl3,"projectionMatrix",projection);

        // set the view matrix to be the inverse of the camera matrix and without translation.
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        Matrix4d view = camera.getWorld();
        Vector3d z = MatrixHelper.getZAxis(view);
        z.scale(-20);

        view.setTranslation(new Vector3d(0,0,0));
        view.invert();
        view.transpose();
        shaderProgram.setMatrix4d(gl3,"viewMatrix",view);

        // for the background circle, set the model matrix to be the camera matrix so the handle is always facing the camera.
        Matrix4d model = camera.getWorld();
        model.setTranslation(z);
        model.mul(model, MatrixHelper.createScaleMatrix4(handleLength+handleRadius+5));
        model.transpose();
        shaderProgram.setMatrix4d(gl3,"modelMatrix",model);
        shaderProgram.set1i(gl3,"useVertexColor",0);
        shaderProgram.setColor(gl3,"objectColor",new Color(255,255,255,64));
        circleMesh.render(gl3);

        // for the gizmo, set the model matrix to be the identity matrix.
        model = MatrixHelper.createIdentityMatrix4();
        model.transpose();
        shaderProgram.setColor(gl3,"objectColor",Color.WHITE);
        shaderProgram.setMatrix4d(gl3,"modelMatrix",model);
        // and use vertex colors.
        shaderProgram.set1i(gl3,"useVertexColor",1);
        gizmoMesh.render(gl3);

        // for the handles, do not use vertex color.
        shaderProgram.set1i(gl3,"useVertexColor",0);

        //gl3.glEnable(GL3.GL_DEPTH_TEST);
        drawHandle(gl3,shaderProgram,new Vector3d( handleLength,0,0),12);  // x+
        drawHandle(gl3,shaderProgram,new Vector3d(-handleLength,0,0),13+1);  // x-
        drawHandle(gl3,shaderProgram,new Vector3d(0, handleLength,0),8);  // y+
        drawHandle(gl3,shaderProgram,new Vector3d(0,-handleLength,0),9+1);  // y-
        drawHandle(gl3,shaderProgram,new Vector3d(0,0, handleLength),4);  // z+
        drawHandle(gl3,shaderProgram,new Vector3d(0,0,-handleLength),5+1);  // z-
    }

    private void drawHandle(GL3 gl3, ShaderProgram shaderProgram, Vector3d offset,int index) {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;

        // set the model matrix to be the camera matrix so the handle is always facing the camera.
        Matrix4d model = camera.getWorld();
        model.setTranslation(offset);
        model.mul(model, MatrixHelper.createScaleMatrix4(handleRadius));
        model.transpose();
        shaderProgram.setMatrix4d(gl3,"modelMatrix",model);
        circleMesh.render(gl3);

        texture.use(shaderProgram);
        quadMesh.render(gl3,index*4,4);
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public boolean isInUse() {
        return false;
    }

    @Override
    public void cancelUse() {}

    @Override
    public Point3d getStartPoint() {
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent event) {}

    @Override
    public void mousePressed(MouseEvent event) {}

    @Override
    public void mouseDragged(MouseEvent event) {}

    @Override
    public void mouseReleased(MouseEvent event) {}

    @Override
    public void setFrameOfReference(int index) {}

    @Override
    public void init(GL3 gl3) {}

    @Override
    public void dispose(GL3 gl3) {
        texture.unload();
        circleMesh.unload(gl3);
        gizmoMesh.unload(gl3);
    }
}
