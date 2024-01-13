package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.nodes.pose.MeshInstance;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>{@link DHParameter} is a node that translates a sibling {@link Pose} to and from
 * <a href="https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters">Denavit-Hartenberg parameters</a> for a
 * joint.  It describes the relative pose of the next joint.</p>
 * <p>The DH parameters can be derived by finding the common normals between two consecutive Z axes.  The new X axis
 * points along the common normal.  The intersection point of the two normals may be outside the physical structure
 * being described.</p>
 * <p>This class provides several functionalities:</p>
 * <ul>
 * <li>It can convert a Pose to Denavit-Hartenberg parameters.</li>
 * <li>It can convert Denavit-Hartenberg parameters to a Pose.</li>
 * <li>It can adjust the local transformations of all MeshInstance siblings.</li>
 * <li>It can serialize and deserialize itself to and from JSON format.</li>
 * </ul>
 * This class also provides several properties:
 * <ul>
 * <li><b>d</b>: the distance along the previous z to the common normal.</li>
 * <li><b>r</b>: the length of the common normal. Assuming a revolute joint, this is the radius about the previous z.</li>
 * <li><b>alpha</b>: the angle about the common normal to align the previous z to the new z.</li>
 * <li><b>theta</b>: the angle about the previous z for the common normal.</li>
 * </ul>
 */
public class DHParameter extends Node {
    private transient double d=0, r=0, alpha=0, theta=0;

    public DHParameter() {
        super("DH Parameter");
    }

    private void toPose() {
        Pose pose = findFirstSibling(Pose.class);
        if (pose != null) pose.setLocal(getDHMatrix());
    }

    private Matrix4d getDHMatrix() {
        Matrix4d m = new Matrix4d();
        double rt = Math.toRadians(theta);
        double ra = Math.toRadians(alpha);
        double ct = Math.cos(rt);
        double ca = Math.cos(ra);
        double st = Math.sin(rt);
        double sa = Math.sin(ra);
        m.m00 = ct;		m.m01 = -st*ca;		m.m02 = st*sa;		m.m03 = r*ct;
        m.m10 = st;		m.m11 = ct*ca;		m.m12 = -ct*sa;		m.m13 = r*st;
        m.m20 = 0;		m.m21 = sa;			m.m22 = ca;			m.m23 = d;
        m.m30 = 0;		m.m31 = 0;			m.m32 = 0;			m.m33 = 1;
        return m;
    }

    public void fromPose() {
        Pose pose = findFirstSibling(Pose.class);
        if (pose != null) setDHMatrix(pose.getLocal());
    }

    private void setDHMatrix(Matrix4d m) {
        // Extract the elements of the matrix
        double m00 = m.m00;
        double m01 = m.m01;
        double m02 = m.m02;
        double m03 = m.m03;
        double m10 = m.m10;
        double m11 = m.m11;
        double m12 = m.m12;
        double m13 = m.m13;
        double m20 = m.m20;
        double m21 = m.m21;
        double m22 = m.m22;
        double m23 = m.m23;

        // Check if the pose is DH compatible
        if (m00 * m10 + m01 * m11 + m02 * m12 != 0 || m20 != 0 || m21 * m21 + m22 * m22 != 1) {
            throw new IllegalArgumentException("The pose is not DH compatible.  pose="+ m);
        }

        // Calculate the DH parameters
        r = Math.sqrt(m03 * m03 + m13 * m13);
        d = m23;
        theta = Math.toDegrees(Math.atan2(m13, m03));
        alpha = Math.toDegrees(Math.atan2(m21, m22));
    }

    /**
     * Adjust the local transformations of all {@link MeshInstance} siblings.
     */
    public void toPoseAndAdjustMeshes() {
        toPose();
        adjustMeshes();
    }

    private void adjustMeshes() {
        Pose pose = findFirstSibling(Pose.class);
        if(pose==null) return;

        List<Node> toScan = new ArrayList<>(pose.getChildren());
        while(!toScan.isEmpty()) {
            Node n = toScan.remove(0);
            if(n instanceof MeshInstance mi) {
                mi.adjustLocal();
            }
            toScan.addAll(n.getChildren());
        }
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new DHParameterPanel(this));
        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("d",d);
        json.put("theta",theta);
        json.put("r",r);
        json.put("alpha",alpha);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("d")) d = from.getDouble("d");
        if(from.has("theta")) theta = from.getDouble("theta");
        if(from.has("r")) r = from.getDouble("r");
        if(from.has("alpha")) alpha = from.getDouble("alpha");
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getD() {
        return d;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getR() {
        return r;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getTheta() {
        return theta;
    }
}
