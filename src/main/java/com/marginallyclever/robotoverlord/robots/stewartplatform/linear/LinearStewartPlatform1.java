package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.Serial;

public class LinearStewartPlatform1 extends LinearStewartPlatformCore {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ShapeEntity baseModel;
    private final ShapeEntity eeModel;
    private final ShapeEntity armModel;

    public LinearStewartPlatform1() {
        super("Linear Stewart Platform 1");

        // load models and fix scale/orientation.
        baseModel = new ShapeEntity("Base","/linearStewartPlatform/base.stl");
        baseModel.setShapeScale(0.1);
        eeModel = new ShapeEntity("ee","/linearStewartPlatform/endEffector.stl");
        eeModel.setShapeScale(0.1);
        eeModel.setShapeRotation(new Vector3d(0,0,-30));
        armModel = new ShapeEntity("arm","/linearStewartPlatform/arm.stl");
        armModel.setShapeScale(0.1);
    }

    @Override
    public void render(GL2 gl2) {
        super.render(gl2);

        gl2.glPushMatrix();
            // draw the base
            MatrixHelper.applyMatrix(gl2, myPose);
            baseModel.render(gl2);

            // draw the end effector
            gl2.glPushMatrix();
                MatrixHelper.applyMatrix(gl2, getEndEffectorPose());
                eeModel.render(gl2);
            gl2.glPopMatrix();

            drawArms(gl2);

        gl2.glPopMatrix();
    }

    // draw the arms (some work to get each matrix...)
    public void drawArms(GL2 gl2) {
        Matrix4d m = new Matrix4d();
        for (LinearStewartPlatformArm arm : arms) {
            // we need the pose of each bone to draw the mesh.
            // a matrix is 3 orthogonal (right angle) vectors and a position.
            // z (up) is from one ball to the next
            Vector3d z = new Vector3d(
                    arm.pEE2.x - arm.pSlide.x,
                    arm.pEE2.y - arm.pSlide.y,
                    arm.pEE2.z - arm.pSlide.z);
            z.normalize();
            // x is a vector that is guaranteed not parallel to z.
            Vector3d x = new Vector3d(
                    arm.pSlide.x,
                    arm.pSlide.y,
                    arm.pSlide.z);
            x.normalize();
            // y is orthogonal to x and z.
            Vector3d y = new Vector3d();
            y.cross(z, x);
            y.normalize();
            // x was not orthogonal to z.
            // y and z are orthogonal, so use them.
            x.cross(y, z);
            x.normalize();

            // fill in the matrix
            m.m00 = x.x;
            m.m10 = x.y;
            m.m20 = x.z;

            m.m01 = y.x;
            m.m11 = y.y;
            m.m21 = y.z;

            m.m02 = z.x;
            m.m12 = z.y;
            m.m22 = z.z;

            m.m03 = arm.pSlide.x;
            m.m13 = arm.pSlide.y;
            m.m23 = arm.pSlide.z;
            m.m33 = 1;

            gl2.glPushMatrix();
            MatrixHelper.applyMatrix(gl2, m);
            armModel.render(gl2);
            gl2.glPopMatrix();
        }
    }
}
