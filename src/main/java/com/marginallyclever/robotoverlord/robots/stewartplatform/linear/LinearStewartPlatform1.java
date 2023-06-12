package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A linear stewart platform with 6 legs.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class LinearStewartPlatform1 extends LinearStewartPlatformCore {
    private final MeshFromFile baseModel;
    private final MeshFromFile eeModel;
    private final MeshFromFile armModel;

    public LinearStewartPlatform1() {
        super();

        // load models and fix scale/orientation.
        baseModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/linear/base.stl");
        //baseModel.setShapeScale(0.1);
        eeModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/linear/endEffector.stl");
        //eeModel.setShapeScale(0.1);
        //eeModel.setShapeRotation(new Vector3d(0,0,-30));
        armModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/linear/arm.stl");
        //armModel.setShapeScale(0.1);
    }

    @Override
    public void render(GL3 gl) {
        super.render(gl);

        PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

        gl.glPushMatrix();
            // draw the base
            MatrixHelper.applyMatrix(gl, myPose.getLocal());
            baseModel.render(gl);

            // draw the end effector
            gl.glPushMatrix();
                MatrixHelper.applyMatrix(gl, getEndEffectorPose());
                eeModel.render(gl);
            gl.glPopMatrix();

            drawArms(gl);

        gl.glPopMatrix();
    }

    // draw the arms (some work to get each matrix...)
    public void drawArms(GL3 gl) {
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

            gl.glPushMatrix();
            MatrixHelper.applyMatrix(gl, m);
            armModel.render(gl);
            gl.glPopMatrix();
        }
    }
}
