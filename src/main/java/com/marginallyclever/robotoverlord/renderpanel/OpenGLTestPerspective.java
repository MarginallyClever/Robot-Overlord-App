package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;

public class OpenGLTestPerspective extends OpenGLTestOrthographic {
    public OpenGLTestPerspective(EntityManager entityManager) {
        super(entityManager);
    }


    @Override
    protected void setProjectionMatrix(GL3 gl, ShaderProgram program) {
        double w = glCanvas.getSurfaceWidth();
        double h = glCanvas.getSurfaceHeight();
        Matrix4d perspectiveMatrix = MatrixHelper.perspectiveMatrix4d(
                60, w/h, 1f, 1000.0f);
        program.setMatrix4d(gl,"projectionMatrix",perspectiveMatrix);
    }

    public static void main(String[] args) {
        // make a frame
        JFrame frame = new JFrame( OpenGLTestPerspective.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        OpenGLTestPerspective opengl = new OpenGLTestPerspective(null);
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
