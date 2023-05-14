package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.*;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.entityManager.EntityManager;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;

public class OpenGLTestPerspective extends OpenGLTestOrthographic {
    public OpenGLTestPerspective(EntityManager entityManager, UpdateCallback updateCallback) {
        super(entityManager, updateCallback);
    }

    @Override
    protected void setProjectionMatrix(GL2 gl2, ShaderProgram program) {
        double w = (double)glCanvas.getSurfaceWidth();
        double h = (double)glCanvas.getSurfaceHeight();
        Matrix4d perspectiveMatrix = MatrixHelper.perspectiveMatrix4d(
                60, w/h, 1f, 1000.0f);
        program.setMatrix4d(gl2,"projectionMatrix",perspectiveMatrix);
    }

    /**
     * Compare the way the master branch and the new branch calculate the projection, view, and model matrices.
     * @param gl2 the GL2 context
     */
    private void compareMatrices(GL2 gl2) {
        // get old style
        Entity cameraEntity = new Entity("Camera");
        CameraComponent camera = new CameraComponent();
        cameraEntity.addComponent(camera);
        viewport.setCamera(camera);
        camera.setOrbitDistance(5);
        viewport.renderChosenProjection(gl2);

        double [] oldProjectionMatrix = new double[16];
        gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX,oldProjectionMatrix,0);

        double [] oldViewMatrix = new double[16];
        gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX,oldViewMatrix,0);

        // get new style
        Matrix4d modelMatrix = new Matrix4d();
        modelMatrix.rotZ(0.25 * Math.PI);
        modelMatrix.setTranslation(new Vector3d(0,0,3));
        MatrixHelper.applyMatrix(gl2,modelMatrix);
        modelMatrix.transpose();

        double [] oldModelMatrix = new double[16];
        gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX,oldModelMatrix,0);

        double w = glCanvas.getSurfaceWidth();
        double h = glCanvas.getSurfaceHeight();
        Matrix4d projectionMatrix = MatrixHelper.perspectiveMatrix4d(
                60, w/h, 0.1f, 100.0f);

        Matrix4d viewMatrix = MatrixHelper.createIdentityMatrix4();
        viewMatrix.setTranslation(new Vector3d(0,0,-5));
        viewMatrix.invert();

        // good place to put a breakpoint.
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();
        gl2.glMatrixMode(GL2.GL_MODELVIEW_MATRIX);
        gl2.glLoadIdentity();
    }

    public static void main(String[] args) {
        // make a frame
        JFrame frame = new JFrame( OpenGLTestPerspective.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        OpenGLTestPerspective opengl = new OpenGLTestPerspective(null,null);
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setVisible(true);
    }
}
