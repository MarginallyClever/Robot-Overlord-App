package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.Vector3dEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

/**
 * A Pose component contains the local transform of an Entity - its position, rotation, and scale relative to its
 * parent.
 * @author Dan Royer
 * @since 2022-08-04
 */
public class PoseComponent extends Component implements PropertyChangeListener {
    // pose relative to my parent (aka local pose).
    private final Matrix4d local = new Matrix4d();
    private final Vector3dEntity position = new Vector3dEntity("position",new Vector3d());
    private final Vector3dEntity rotation = new Vector3dEntity("rotation",new Vector3d());
    private final Vector3dEntity scale = new Vector3dEntity("scale",new Vector3d(1,1,1));

    public PoseComponent() {
        super();
        local.setIdentity();
        position.addPropertyChangeListener(this);
        rotation.addPropertyChangeListener(this);
        scale.addPropertyChangeListener(this);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("position",position.toJSON());
        jo.put("rotation",rotation.toJSON());
        jo.put("scale",scale.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        position.parseJSON(jo.getJSONObject("position"));
        rotation.parseJSON(jo.getJSONObject("rotation"));
        scale.parseJSON(jo.getJSONObject("scale"));
        refreshLocalMatrix();
    }

    /**
     * @return local position
     */
    public Vector3d getPosition() {
        return new Vector3d(this.position.get());
    }

    /**
     * Set local position
     * @param position the new local position
     */
    public void setPosition(Vector3d position) {
        this.position.set(position);
        refreshLocalMatrix();
    }

    /**
     * Convert Euler rotations to a matrix.
     * See also <a href="https://www.learnopencv.com/rotation-matrix-to-euler-angles/">...</a>
     * Euler rotations are using the ZYX convention.
     * @param arg0 a {@link Vector3d} with three angles (in radians)
     */
    public void setRotation(Vector3d arg0) {
        this.rotation.set(arg0);
        refreshLocalMatrix();
    }

    public void setLocalMatrix3(Matrix3d mat) {
        Vector3d euler = MatrixHelper.matrixToEuler(mat);
        euler.scale(Math.toDegrees(1));
        setRotation(euler);
    }

    public void setLocalMatrix4(Matrix4d m) {
        Vector3d euler = MatrixHelper.matrixToEuler(m);
        euler.scale(Math.toDegrees(1));
        setRotation(euler);
        setPosition(MatrixHelper.getPosition(m));
    }

    /**
     * Convert a matrix to Euler rotations.  There are many valid solutions.
     * See also <a href="https://www.learnopencv.com/rotation-matrix-to-euler-angles/">...</a>
     * Euler rotations are using the ZYX convention.
     * @return a {@link Vector3d} with degree rotations.
     */
    public Vector3d getRotation() {
        return new Vector3d(this.rotation.get());
    }

    public Matrix4d getLocal() {
        return new Matrix4d(local);
    }

    /**
     *
     * @return the cumulative pose in the hierarchy of entities.
     */
    public Matrix4d getWorld() {
        Matrix4d result = new Matrix4d(local);

        Entity entity = getEntity();
        if(entity!=null) {
            PoseComponent parent = getEntity().findFirstComponentInParents(PoseComponent.class);
            if(parent!=null) {
                result.mul(parent.getWorld(),result);
            }
        }

        return result;
    }

    @Override
    public void getView(ViewPanel view) {
        view.add(position);
        view.add(rotation);
        view.add(scale);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshLocalMatrix();
    }

    public void setScale(Vector3d v) {
        scale.set(v);
        refreshLocalMatrix();
    }

    private Vector3d getScale() {
        return new Vector3d(this.scale.get());
    }

    private void refreshLocalMatrix() {
        Matrix4d m4 = new Matrix4d();
        Vector3d rot = getRotation();
        rot.x = Math.toRadians(rot.x);
        rot.y = Math.toRadians(rot.y);
        rot.z = Math.toRadians(rot.z);
        m4.set(MatrixHelper.eulerToMatrix(rot));
        m4.setTranslation(getPosition());
        Vector3d s = getScale();
        m4.m00 *= s.x;
        m4.m11 *= s.y;
        m4.m22 *= s.z;
        local.set(m4);
    }

    @Override
    public String toString() {
        return super.toString()+",local="+ Arrays.toString(MatrixHelper.matrixtoArray(local))+",\n";
    }

    /**
     * if this component is attached to an entity and any parent entity has a PoseComponent then this local matrix is
     * relative to that PoseComponent.  Make the adjustment if necessary.
     * @param worldMatrix the matrix to set.
     */
    public void setWorld(Matrix4d worldMatrix) {
        Entity entity = getEntity();
        if(entity!=null) {
            PoseComponent parentPose = getEntity().findFirstComponentInParents(PoseComponent.class);
            if (parentPose != null) {
                Matrix4d inverseParentPose = parentPose.getWorld();
                inverseParentPose.invert();
                worldMatrix.mul(inverseParentPose, worldMatrix);
            }
        }
        setLocalMatrix4(worldMatrix);
    }
}
