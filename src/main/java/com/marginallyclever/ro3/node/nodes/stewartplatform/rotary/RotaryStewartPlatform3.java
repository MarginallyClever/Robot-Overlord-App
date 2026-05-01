package com.marginallyclever.ro3.node.nodes.stewartplatform.rotary;

import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.mesh.proceduralmesh.Cylinder;
import com.marginallyclever.ro3.mesh.proceduralmesh.ProceduralMeshFactory;
import com.marginallyclever.ro3.mesh.proceduralmesh.Waldo;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.List;

/**
 * <p>A Stewart Platform node with rotary actuators.  Automatically adds a "top" Pose node and a "bottom" Pose node as
 * children.  The top and bottom poses each have six connection points.  The top and
 * bottom also get a {@link MeshInstance} child to visualize the platform.
 *
 * A single Material is added to the root node and applied to all MeshInstances.
 *
 * The bottom connection points (shoulders) have six rotating "biceps".  each bicep has a MeshInstance.
 * The top connection points (wrists) have ball joint links to "forearms".  each forearms has a MeshInstance.
 * The biceps and the forearms meet at the ball joint "elbows".</p>
 *
 * <p>Several parameters can be tweaked at run time:</p>
 * <ul>
 *     <li>the xyz offset of the first bottom connection point relative to the base.  the second is a mirror of the
 *     first; the rest are 120 and 240 degree offset repeats of the first two.</li>
 *     <li>the xyz offset of the top connection point relative to the end effector.  the second is a mirror of the
 *     first; the rest are 120 and 240 degree offset repeats of the first two.</li>
 *     <li>The length of the bicep</li>
 *     <li>The length of the forearm</li>
 *     <li>The radius of the ball joints</li>
 * </ul>
 * <p>The system should be able to generate approximate jacobians at any given pose.  When the top is moved the system
 * should generate a list of angle values for each motor and display that value as gcode in the control panel.</p>
 * <p>When the system is homed the biceps rest on the z=0 plane of the base.  The offset of the
 * first bottom connection point minus the radius of the ball joint gives the opposite of the right angle triangle.  the
 * hypotenuse is the length of the bicep.  The home angle of the bicep can be found with this information.</p>
 * <p>Given the homed position of the bottom connection point; the offset of the top connection point; and the fact
 * that, when homed, the end effector's origin is at x=0 and y=0... it should be possible to calculate the initial z
 * height of the end effector.</p>
 */
public class RotaryStewartPlatform3 extends Node {
    public static final int NUM_ACTUATORS = 6;
    public static final int NUM_DOF = 6;
    private static final int [] BOTTOM_CARDINALITY = {0,5,2,1,4,3};
    private static final int [] TOP_CARDINALITY = {5,4,1,0,3,2};

    private Pose bottom = null;
    private Pose top = null;
    private final Vector3d topOffset = new Vector3d(4.4452, .70,1.75);
    private final Vector3d bottomOffset = new Vector3d(8.7, 2.2, 2.4);
    private double bicepLength=5.0;
    private double forearmLength=18.3;

    private class Arm {
        public Pose shoulder;
        public Pose wrist;
    }
    private final Arm [] arms = new Arm[NUM_ACTUATORS];

    public RotaryStewartPlatform3() {
        super("RotaryStewartPlatform3");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new RotaryStewartPlatform3Panel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        attachBottomMesh();
        attachTopMesh();
        attachAllArmMeshes();
        // add a material for the entire platform if one does not already exist
        if(!this.hasChild(Material.class)) {
            this.addChild(new Material("Material"));
        }
        refreshShape();
    }

    private void attachAllArmMeshes() {
        for(int i=0;i<NUM_ACTUATORS;i++) {
            if(arms[i]==null) {
                attachOneArmMesh(i);
            }
        }
    }

    private void attachOneArmMesh(int i) {
        arms[i] = new Arm();
        var shoulder = new Pose("shoulder"+(i+1));
        arms[i].shoulder = shoulder;
        bottom.addChild(shoulder);
        var bicepMesh = new MeshInstance();
        shoulder.addChild(bicepMesh);
        bicepMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/bicep.obj"));

        var wrist =  new Pose("wrist"+(i+1));
        arms[i].wrist = wrist;
        top.addChild(wrist);
        var forearmMesh = new MeshInstance();
        wrist.addChild(forearmMesh);
        forearmMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/forearm.obj"));
    }

    // attach the top mesh
    private void attachTopMesh() {
        top = this.findNodeByPath("Top", Pose.class);
        if(top!=null) return;
        top = new Pose("Top");
        this.addChild(top);
        // add a mesh instance to visualize the top plate
        var topMesh = new MeshInstance();
        top.addChild(topMesh);
        topMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/top.obj"));
    }

    // attach the bottom mesh
    private void attachBottomMesh() {
        bottom = this.findNodeByPath("Bottom", Pose.class);
        if(bottom!=null) return;
        bottom = new Pose("Bottom");
        this.addChild(bottom);
        // add a mesh instance to visualize the bottom plate
        var bottomMesh = new MeshInstance();
        bottom.addChild(bottomMesh);
        bottomMesh.setMesh(Registry.meshFactory.get(Lifetime.SCENE,"/com/marginallyclever/ro3/node/nodes/rotarystewartplatform3/base.obj"));
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("topOffsetX", topOffset.x);
        json.put("topOffsetY", topOffset.y);
        json.put("topOffsetZ", topOffset.z);
        json.put("bottomOffsetX", bottomOffset.x);
        json.put("bottomOffsetY", bottomOffset.y);
        json.put("bottomOffsetZ", bottomOffset.z);
        json.put("bicepLength", bicepLength);
        json.put("forearmLength", forearmLength);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        topOffset.x = json.optDouble("topOffsetX", topOffset.x);
        topOffset.y = json.optDouble("topOffsetY", topOffset.y);
        topOffset.z = json.optDouble("topOffsetZ", topOffset.z);
        bottomOffset.x = json.optDouble("bottomOffsetX", bottomOffset.x);
        bottomOffset.y = json.optDouble("bottomOffsetY", bottomOffset.y);
        bottomOffset.z = json.optDouble("bottomOffsetZ", bottomOffset.z);
        bicepLength = json.optDouble("bicepLength", bicepLength);
        forearmLength = json.optDouble("forearmLength", forearmLength);
        refreshShape();
    }

    // Getters and setters
    public Vector3d getTopOffset() {
        return new Vector3d(topOffset);
    }
    public void setTopOffset(Vector3d v) {
        this.topOffset.set(v);
        refreshShape();
    }

    public Vector3d getBottomOffset() {
        return new Vector3d(bottomOffset);
    }
    public void setBottomOffset(Vector3d v) {
        this.bottomOffset.set(v);
        refreshShape();
    }

    public double getBicepLength() {
        return bicepLength;
    }
    public void setBicepLength(double v) {
        this.bicepLength = v;
    }

    public double getForearmLength() {
        return forearmLength;
    }
    public void setForearmLength(double v) {
        this.forearmLength = v;
    }


    /**
     * Find or add 6 MeshInstances with Waldos into the top and bottom.
     * Then adjust the position of each Waldo according to the offsets.
     * Remember the top plate is rotated 60 degrees from the bottom and each "arm" is 120 degrees from the previous.
     */
    private void refreshShape() {
        if(bottom!=null) {
            setWaldoPositions(bottom,bottomOffset,0,BOTTOM_CARDINALITY);
        }

        if(top!=null) {
            setWaldoPositions(top,topOffset,60,TOP_CARDINALITY);
        }
        adjustTopToMinimumHeight();
    }

    private void adjustTopToMinimumHeight() {
    }

    /**
     * <p>Calculate the Jacobian matrix at the current pose using damped least squares inverse of the inverse jacobian.</p>
     * @return A 6x6 Jacobian matrix
     */
    private double [][] getJacobian() {
        double lambda = 1e-6;
        double [][] ij = getApproximateInverseJacobian();

        double[][] jt = BigMatrixHelper.transpose(ij);
        double[][] jjt = BigMatrixHelper.multiplyMatrices(ij, jt);

        // Add lambda^2 * identity matrix to jjt
        for (int i = 0; i < ij.length; i++) {
            jjt[i][i] += lambda * lambda;
        }

        double[][] jjt_inv = BigMatrixHelper.invert(jjt);
        return BigMatrixHelper.multiplyMatrices(jt, jjt_inv);
    }

    /**
     * <p>Calculate the approximate inverse Jacobian matrix at the current pose.  This is done by making a copy
     * of the current top pose, then perturbing each degree of freedom slightly and measuring the change in actuator
     * lengths.</p>
     * @return A 6x6 Jacobian matrix
     */
    private double [][] getApproximateInverseJacobian() {
        // save original state
        double [][] iJacobian = new double[NUM_ACTUATORS][NUM_DOF];
        Matrix4d original = top.getLocal();
        Matrix4d after = new Matrix4d();
        double [] originalLength = getActuatorLengths();
        final double stepSize = 0.1;
        final double stepRadians = Math.toRadians(stepSize);

        try {
            // for each degree of freedom
            for(int i=0;i<NUM_DOF;++i) {
                // perturb the DOF,
                after.set(original);
                switch(i) {
                    case 0 -> after.m03 += stepSize; // x
                    case 1 -> after.m13 += stepSize; // y
                    case 2 -> after.m23 += stepSize; // z
                    case 3 -> after.mul(new Matrix4d(new double[]{
                            1,0,0,0,
                            0,Math.cos(stepRadians), -Math.sin(stepRadians),0,
                            0,Math.sin(stepRadians),  Math.cos(stepRadians),0,
                            0,0,0,1
                    })); // roll
                    case 4 -> after.mul(new Matrix4d(new double[]{
                             Math.cos(stepRadians),0,Math.sin(stepRadians),0,
                            0,1,0,0,
                            -Math.sin(stepRadians),0,Math.cos(stepRadians),0,
                            0,0,0,1
                    })); // pitch
                    case 5 -> after.mul(new Matrix4d(new double[]{
                            Math.cos(stepRadians),-Math.sin(stepRadians),0,0,
                            Math.sin(stepRadians), Math.cos(stepRadians),0,0,
                            0,0,1,0,
                            0,0,0,1
                    })); // yaw
                }
                // set the new pose,
                top.setLocal(after);
                // measure new lengths,
                double [] length = getActuatorLengths();
                // and approximate the derivative.
                for(int j=0;j<NUM_ACTUATORS;++j) {
                    iJacobian[j][i] = (length[j] - originalLength[j]) / stepSize;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        // restore state and return inverse jacobian.
        top.setLocal(original);
        return iJacobian;
    }

    // measure actuator lengths
    private double [] getActuatorLengths() {
        double [] lengths = new double[NUM_ACTUATORS];
        List<Pose> bottomWaldos = getOrCreateWaldos(bottom);  // lazy and expensive
        List<Pose> topWaldos = getOrCreateWaldos(top);  // lazy and expensive
        for(int j=0;j<NUM_ACTUATORS;++j) {
            var bl = MatrixHelper.getPosition(bottomWaldos.get(j).getWorld());
            var tl = MatrixHelper.getPosition(topWaldos.get(j).getWorld());
            bl.sub(tl);
            lengths[j] = bl.length();
        }
        return lengths;
    }

    private void setWaldoPositions(Pose pose, Vector3d v,double offsetAngleDegrees,int [] cardinality) {
        List<Pose> waldoes = getOrCreateWaldos(pose);
        for(int i=0;i<NUM_ACTUATORS;i+=2) {
            var angle = Math.toRadians(i * 120 + offsetAngleDegrees);
            var px = new Vector3d(Math.cos(angle), Math.sin(angle),0);
            var py = new Vector3d(-Math.sin(angle), Math.cos(angle),0);
            var sum = new Vector3d(0,0,v.z);
            sum.scaleAdd(v.x, px, sum);
            sum.scaleAdd(-v.y, py, sum);
            waldoes.get(cardinality[i]).setPosition(sum);
            sum.set(0,0,v.z);
            sum.scaleAdd(v.x, px, sum);
            sum.scaleAdd(v.y, py, sum);
            waldoes.get(cardinality[i+1]).setPosition(sum);
        }
        var mi = pose.findFirstChild(MeshInstance.class);
        if(mi!=null) {
            if(mi.getMesh() instanceof Cylinder c) {
                c.setRadius(Math.sqrt(v.x * v.x + v.y * v.y));
                c.updateModel();
            }
        }
    }

    private List<Pose> getOrCreateWaldos(Pose parent) {
        List<Pose> waldoes = new java.util.ArrayList<>();
        int found=0;
        // find or create Waldo nodes under the given MeshInstance
        for(Node n : parent.getChildren()) {
            if(n instanceof Pose child && !(n instanceof MeshInstance)) {
                found++;
                waldoes.add(child);
                if(found==NUM_ACTUATORS) break;
            }
        }
        while(waldoes.size()<NUM_ACTUATORS) {
            var p = new Pose("joint"+waldoes.size());
            parent.addChild(p);
            waldoes.add(p);

            var mi = new MeshInstance();
            p.addChild(mi);
            Waldo waldo = (Waldo)ProceduralMeshFactory.createMesh("Waldo");
            assert waldo != null;
            waldo.setRadius(15);
            waldo.updateModel();
            mi.setMesh(waldo);
            mi.setHasShadow(false);
        }
        return waldoes;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        // make all bottom waldoes lookAt the matching top waldoes
        List<Pose> bottomWaldos = getOrCreateWaldos(bottom);  // lazy and expensive
        List<Pose> topWaldos = getOrCreateWaldos(top);  // lazy and expensive
        for(int i=0;i<NUM_ACTUATORS;++i) {
            var bw = bottomWaldos.get(i).getWorld();
            var tw = topWaldos.get(i).getWorld();
            // make bottom waldo look at top waldo
            var bp = MatrixHelper.getPosition(bw);
            var tp = MatrixHelper.getPosition(tw);
            var m3 = MatrixHelper.lookAt(bp, tp);
            Matrix4d m4 = new Matrix4d();
            m4.set(m3);
            m4.setTranslation(bp);
            bottomWaldos.get(i).setWorld(m4);
            // make the top look at the bottom waldo
            m3 = MatrixHelper.lookAt(tp, bp);
            m4 = new Matrix4d();
            m4.set(m3);
            m4.setTranslation(tp);
            topWaldos.get(i).setWorld(m4);
        }
    }

    private double frobeniusNorm(double[][] m) {
        double s = 0.0;
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[i].length; ++j) {
                double v = m[i][j];
                s += v * v;
            }
        }
        return Math.sqrt(s);
    }

    /**
     * Compute condition number of the 6x6 Jacobian using Frobenius norm:
     * cond = ||J||_F * ||J^{-1}||_F. Returns Double.POSITIVE_INFINITY if J is singular.
     */
    public double evaluateJacobianConditionNumber() {
        return evaluateJacobianUsingSVD();
        //return evaluateJacobianUsingFrobeniusNorm();
    }

    /**
     * Evaluate the condition number using Single Value Decomposition (SVD).
     * @return condition number of the Jacobian matrix.
     */
    private double evaluateJacobianUsingSVD() {
        double[][] J = getJacobian();
        scaleByAverageRadius(J);
        double[] s = BigMatrixHelper.singularValues(J);
        if (s[s.length - 1] < 1e-12) {
            return Double.POSITIVE_INFINITY;
        }
        return s[0] / s[s.length - 1];
    }

    private double evaluateJacobianUsingFrobeniusNorm() {
        double[][] J = getJacobian();

        scaleByAverageRadius(J);

        try {
            double[][] inv = BigMatrixHelper.invert(J);
            double nJ = frobeniusNorm(J);
            double nInv = frobeniusNorm(inv);
            return nJ * nInv;
        } catch (Exception e) {
            return Double.POSITIVE_INFINITY;
        }
    }

    private void scaleByAverageRadius(double[][] J) {
        // scale J by average radius to make condition number more meaningful.
        // Jnormalized = J * diag(1/L, 1/L, 1/L, 1, 1, 1)
        double L = getAverageRadius();
        for (int i = 0; i < J.length; ++i) {
            for (int j = 0; j < J[i].length; ++j) {
                if (j < 3) {
                    J[i][j] /= L;
                }
            }
        }
    }

    private double getAverageRadius() {
        return (topOffset.length()+bottomOffset.length())/2.0;
    }

    /**
     * Compute numeric rank of a matrix using Gaussian elimination with partial pivoting.
     * tol is the pivot threshold (e.g. 1e-6).
     */
    private int matrixRank(double[][] in, double tol) {
        int m = in.length;
        int n = in[0].length;
        double[][] a = new double[m][n];
        for (int i = 0; i < m; ++i) System.arraycopy(in[i], 0, a[i], 0, n);

        int rank = 0;
        int row = 0;
        for (int col = 0; col < n && row < m; ++col) {
            // find pivot
            int sel = row;
            double max = Math.abs(a[sel][col]);
            for (int r = row + 1; r < m; ++r) {
                double v = Math.abs(a[r][col]);
                if (v > max) { max = v; sel = r; }
            }
            if (max < tol) continue;
            // swap
            if (sel != row) {
                double[] tmp = a[sel]; a[sel] = a[row]; a[row] = tmp;
            }
            // eliminate below
            double piv = a[row][col];
            for (int r = row + 1; r < m; ++r) {
                double factor = a[r][col] / piv;
                if (factor == 0.0) continue;
                for (int c = col; c < n; ++c) {
                    a[r][c] -= factor * a[row][c];
                }
            }
            row++;
            rank++;
        }
        return rank;
    }

    /**
     * Evaluate rank of current Jacobian.
     */
    public int evaluateJacobianRank() {
        double[][] J = getJacobian();
        // tolerance chosen empirically; adjust if needed
        return matrixRank(J, 1e-6);
    }
}
