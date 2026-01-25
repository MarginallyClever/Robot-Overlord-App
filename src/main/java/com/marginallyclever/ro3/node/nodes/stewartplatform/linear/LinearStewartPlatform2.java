package com.marginallyclever.ro3.node.nodes.stewartplatform.linear;

import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
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
 * <p>A Stewart Platform node with linear actuators.  Automatically adds a "top" Pose node and a "bottom" Pose node as
 * children.  The top and bottom poses each have six connection points for the six linear actuators.  The top and
 * bottom also get a {@link com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance} child to visualize the platform.
 * A single Material is added to this node and applied to both MeshInstances.</p>
 * <p>Several paramters can be tweaked at run time:</p>
 * <ul>
 *     <li>The XY offset of the base and top platform connection points</li>
 *     <li>The minium and maximum actuator length</li>
 * </ul>
 * <p>The system should be able to generate approximate jacobians at any given pose.</p>
 */
public class LinearStewartPlatform2 extends Node {
    public static final int NUM_ACTUATORS = 6;
    public static final int NUM_DOF = 6;
    private static final int [] BOTTOM_CARDINALITY = {0,5,2,1,4,3};
    private static final int [] TOP_CARDINALITY = {5,4,1,0,3,2};

    private Pose bottom = null;
    private Pose top = null;
    private final Vector2d topOffset = new Vector2d(25.0, 5.36);
    private final Vector2d bottomOffset = new Vector2d(35.0, 7.511);
    private double minActuatorLength = 38.0;
    private double maxActuatorLength = 60.0;

    public LinearStewartPlatform2() {
        super("LinearStewartPlatform2");
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new LinearStewartPlatform2Panel(this));
        super.getComponents(list);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        bottom = this.findNodeByPath("Bottom", Pose.class);
        if(bottom==null) {
            bottom = new Pose("Bottom");
            this.addChild(bottom);
            // add a mesh instance to visualize the bottom plate
            var bottomMesh = new MeshInstance();
            bottom.addChild(bottomMesh);
            bottomMesh.setMesh(ProceduralMeshFactory.createMesh("Cylinder"));
        }

        top = this.findNodeByPath("Top", Pose.class);
        if(top==null) {
            top = new Pose("Top");
            this.addChild(top);
            // add a mesh instance to visualize the top plate
            var topMesh = new MeshInstance();
            top.addChild(topMesh);
            topMesh.setMesh(ProceduralMeshFactory.createMesh("Cylinder"));
        }
        // add a material for the entire platform if one does not already exist
        if(!this.hasChild(Material.class)) {
            this.addChild(new Material("Material"));
        }
        refreshShape();
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("topOffsetX", topOffset.x);
        json.put("topOffsetY", topOffset.y);
        json.put("bottomOffsetX", bottomOffset.x);
        json.put("bottomOffsetY", bottomOffset.y);
        json.put("minActuatorLength", minActuatorLength);
        json.put("maxActuatorLength", maxActuatorLength);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        topOffset.x = json.optDouble("topOffsetX", topOffset.x);
        topOffset.y = json.optDouble("topOffsetY", topOffset.y);
        bottomOffset.x = json.optDouble("bottomOffsetX", bottomOffset.x);
        bottomOffset.y = json.optDouble("bottomOffsetY", bottomOffset.y);
        minActuatorLength = json.optDouble("minActuatorLength", minActuatorLength);
        maxActuatorLength = json.optDouble("maxActuatorLength", maxActuatorLength);
        refreshShape();
    }

    // Getters and setters
    public Vector2d getTopOffset() {
        return new Vector2d(topOffset);
    }
    public void setTopOffset(Vector2d v) {
        this.topOffset.set(v);
        refreshShape();
    }

    public Vector2d getBottomOffset() {
        return new Vector2d(bottomOffset);
    }
    public void setBottomOffset(Vector2d v) {
        this.bottomOffset.set(v);
        refreshShape();
    }

    public double getMinActuatorLength() {
        return minActuatorLength;
    }
    public void setMinActuatorLength(double v) {
        this.minActuatorLength = v;
    }

    public double getMaxActuatorLength() {
        return maxActuatorLength;
    }
    public void setMaxActuatorLength(double v) {
        this.maxActuatorLength = v;
    }


    // find or add 6 MeshInstances with Waldos into the top and bottom.
    // then adjust the position of each Waldo according to the offsets.
    // remember the top plate is rotated 60 degrees from the bottom and each "arm" is 120 degrees from the previous.
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
        // adjust the top pose to be near minActuatorLength above the bottom pose
        var bottomPos = bottom.getPosition();
        var topPos = top.getPosition();
        double minZ = bottomPos.z + minActuatorLength*0.8;
        if(topPos.z < minZ) {
            topPos.z = minZ;
            top.setPosition(topPos);
        }
        // because top has a smaller radius than bottom, our best guess so far is still too close.
        while(getShortestActuatorLength() < getMinActuatorLength()) {
            topPos.z += 0.1;
            top.setPosition(topPos);
        }
    }

    private double getShortestActuatorLength() {
        double [] len = getActuatorLengths();
        double min = Double.MAX_VALUE;
        for(int i=0;i<len.length;++i) {
            if(len[i]<min) min = len[i];
        }
        return min;
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

    private void setWaldoPositions(Pose pose, Vector2d v,double offsetAngleDegrees,int [] cardinality) {
        List<Pose> waldoes = getOrCreateWaldos(pose);
        for(int i=0;i<NUM_ACTUATORS;i+=2) {
            var angle = Math.toRadians(i * 120 + offsetAngleDegrees);
            var px = new Vector3d(Math.cos(angle), Math.sin(angle),0);
            var py = new Vector3d(-Math.sin(angle), Math.cos(angle),0);
            var sum = new Vector3d();
            sum.scaleAdd(v.x, px, sum);
            sum.scaleAdd(-v.y, py, sum);
            waldoes.get(cardinality[i]).setPosition(sum);
            sum.set(0,0,0);
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

    /**
     * Grid search scaling factors for top and bottom plate offsets.
     * - minScale...maxScale inclusive, using steps per axis.
     * Prints best found scales (closest condition to 1) and restores original offsets.
     */
    public void searchBestScale(double minScale, double maxScale, int steps) {
        if (steps < 2) steps = 2;
        var origTop = new Vector2d(topOffset);
        var origBottom = new Vector2d(bottomOffset);

        double bestScore = Double.POSITIVE_INFINITY;
        double bestTopScale = 1.0;
        double bestBottomScale = 1.0;
        double bestCond = Double.NaN;
        int bestRank = 0;

        for (int i = 0; i < steps; ++i) {
            double topScale = minScale + (maxScale - minScale) * i / (steps - 1);
            for (int j = 0; j < steps; ++j) {
                double bottomScale = minScale + (maxScale - minScale) * j / (steps - 1);
                // apply scales
                topOffset.set(origTop);
                bottomOffset.set(origBottom);
                topOffset.scale(topScale);
                bottomOffset.scale(bottomScale);
                refreshShape();

                double cond = evaluateJacobianConditionNumber();
                int rank = evaluateJacobianRank();
                double score = Double.isFinite(cond) ? Math.abs(cond - 1.0) : Double.POSITIVE_INFINITY;

                if (Double.isFinite(score) && (score < bestScore || (Math.abs(score - bestScore) < 1e-12 && rank > bestRank))) {
                    bestScore = score;
                    bestTopScale = topScale;
                    bestBottomScale = bottomScale;
                    bestCond = cond;
                    bestRank = rank;
                }
            }
        }

        // restore original offsets and shape
        topOffset.set(origTop);
        bottomOffset.set(origBottom);
        refreshShape();

        System.out.println("searchBestScale result: topScale=" + bestTopScale +
                " bottomScale=" + bestBottomScale +
                " cond=" + bestCond +
                " rank=" + bestRank +
                " score=" + bestScore);
    }

    /**
     * a gradient-descent based optimizer to adjust the four offset components (top.x, top.y, bottom.x, bottom.y).
     * The method uses a squared-error objective (cond - 1)^2 with a large penalty for non-finite condition numbers,
     * computes gradients by central finite differences, runs gradient steps until convergence or max iterations,
     * records the best found offsets, restores the original offsets, and prints the result.
     * @param learningRate
     * @param maxIter
     * @param tol
     */
    public void searchBestOffsetsByGradientDescent(double learningRate, int maxIter, double tol) {
        if (maxIter <= 0) maxIter = 100;
        if (learningRate <= 0) learningRate = 1e-2;
        if (tol <= 0) tol = 1e-6;

        // save originals
        var origTop = new Vector2d(topOffset);
        var origBottom = new Vector2d(bottomOffset);

        // parameter vector: [top.x, top.y, bottom.x, bottom.y]
        double[] p = { topOffset.x, topOffset.y, bottomOffset.x, bottomOffset.y };
        double[] bestP = p.clone();
        double bestScore = Double.POSITIVE_INFINITY;
        int bestIter = 0;

        final double fdStep = 1e-3; // finite difference step
        for (int iter = 0; iter < maxIter; ++iter) {
            // apply current params
            topOffset.set(p[0], p[1]);
            bottomOffset.set(p[2], p[3]);
            refreshShape();

            // objective: squared error (cond - 1)^2, large penalty if non-finite
            double cond = evaluateJacobianConditionNumber();
            double score = Double.isFinite(cond) ? (cond - 1.0) * (cond - 1.0) : 1e12;
            if (Double.isFinite(score) && score < bestScore) {
                bestScore = score;
                bestP = p.clone();
                bestIter = iter;
            }

            // compute gradient by central differences
            double[] grad = new double[4];
            double gradNormSq = 0.0;
            for (int k = 0; k < 4; ++k) {
                double orig = p[k];

                p[k] = orig + fdStep;
                topOffset.set(p[0], p[1]);
                bottomOffset.set(p[2], p[3]);
                refreshShape();
                double c1 = evaluateJacobianConditionNumber();
                double s1 = Double.isFinite(c1) ? (c1 - 1.0) * (c1 - 1.0) : 1e12;

                p[k] = orig - fdStep;
                topOffset.set(p[0], p[1]);
                bottomOffset.set(p[2], p[3]);
                refreshShape();
                double c2 = evaluateJacobianConditionNumber();
                double s2 = Double.isFinite(c2) ? (c2 - 1.0) * (c2 - 1.0) : 1e12;

                // restore
                p[k] = orig;

                grad[k] = (s1 - s2) / (2.0 * fdStep);
                gradNormSq += grad[k] * grad[k];
            }

            double gradNorm = Math.sqrt(gradNormSq);
            if (gradNorm < tol) {
                break;
            }

            // gradient descent update
            for (int k = 0; k < 4; ++k) {
                p[k] -= learningRate * grad[k];
            }
        }

        // restore original offsets/shape
        topOffset.set(origTop);
        bottomOffset.set(origBottom);
        refreshShape();

        System.out.println("result: bestIter=" + bestIter +
                " top=(" + bestP[0] + "," + bestP[1] + ")" +
                " bottom=(" + bestP[2] + "," + bestP[3] + ")" +
                " bestScore=" + bestScore +
                " learningRate="+learningRate +
                " maxIter="+maxIter +
                " tol="+tol);

    }
}
