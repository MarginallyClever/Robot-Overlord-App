package com.marginallyclever.robotOverlord.components;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.Component;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * A Pose component contains the local transform of an Entity - its position, rotation, and scale relative to its
 * parent.
 * @author Dan Royer
 * @since 2022-08-04
 */
public class PoseComponent extends Component {
    // pose relative to my parent (aka local pose).
    private final Matrix4d local = new Matrix4d();

    public PoseComponent() {
        super();
        local.setIdentity();
    }

    public void save(BufferedWriter writer) throws IOException {
        MatrixHelper.save(local,writer);
    }

    public void load(BufferedReader reader) throws IOException {
        MatrixHelper.load(local,reader);
    }

    /**
     * @return local position
     */
    public Vector3d getPosition() {
        Vector3d position = new Vector3d();
        local.get(position);
        return position;
    }

    /**
     * Set local position
     * @param position the new local position
     */
    public void setPosition(Vector3d position) {
        local.setTranslation(position);
    }

    /**
     * Convert Euler rotations to a matrix.
     * See also https://www.learnopencv.com/rotation-matrix-to-euler-angles/
     * Eulers are using the ZYX convention.
     * @param arg0 a {@link Vector3d} with three radian rotation values
     */
    public void setRotation(Vector3d arg0) {
        Matrix4d m4 = new Matrix4d();
        Matrix3d m3 = MatrixHelper.eulerToMatrix(arg0);
        m4.set(m3);
        m4.setTranslation(getPosition());
        m3.setScale(getScale());
        local.set(m4);
    }

    /**
     * Convert a matrix to Euler rotations.  There are many valid solutions.
     * See also https://www.learnopencv.com/rotation-matrix-to-euler-angles/
     * Eulers are using the ZYX convention.
     * @return a vector3 with one possible combination of radian rotations.
     */
    public Vector3d getRotation() {
        Vector3d arg0 = new Vector3d();
        Matrix3d temp = new Matrix3d();
        local.get(temp);
        arg0.set(MatrixHelper.matrixToEuler(temp));
        return arg0;
    }

    private double getScale() {
        return local.getScale();
    }

    public Matrix4d getLocal() {
        return local;
    }

    /**
     *
     * @return the cumulative pose in the heirarchy of entities.
     */
    public Matrix4d getWorld() {
        Matrix4d result = new Matrix4d();

        PoseComponent parent = getEntity().findFirstComponentInParents(PoseComponent.class);
        if(parent==null) {
            result.set(local);
        } else {
            result.set(parent.getWorld());
            result.mul(local);
        }
        return result;
    }

    @Override
    public void getView(ViewPanel view) {

    }
}
