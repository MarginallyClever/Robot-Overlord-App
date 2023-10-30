package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;

/**
 * This class is used to calculate the Jacobian matrix for a robot arm.
 * Each implementation can derive this class and fill in the jacobian matrix.
 * @author Dan Royer
 */
public abstract class ApproximateJacobian {
    /**
     * a matrix that will be filled with the jacobian. The first three columns
     * are translation component. The last three columns are the rotation component.
     */
    protected final double[][] jacobian;
    protected final int DOF;

    protected ApproximateJacobian(int DOF) {
        this.DOF = DOF;
        jacobian = new double[6][DOF];
    }

    /**
     * Use the jacobian to get the cartesian velocity from the joint velocity.
     * @param jointForce joint velocity in degrees.
     * @return 6 doubles containing the XYZ translation and UVW rotation forces on the end effector.
     */
    public double[] getCartesianForceFromJointForce(final double[] jointForce) {
        // vector-matrix multiplication (y = x^T A)
        double[] cartesianVelocity = new double[DOF];
        for (int j = 0; j < DOF; ++j) {
            double sum = 0;
            for (int k = 0; k < 6; ++k) {
                sum += jacobian[k][j] * Math.toRadians(jointForce[j]);
            }
            cartesianVelocity[j] = sum;
        }
        return cartesianVelocity;
    }

    /**
     * See <a href="https://stackoverflow.com/a/53028167/1159440">5 DOF Inverse kinematics for Jacobian Matrices</a>.
     * @return the inverse Jacobian matrix.
     */
    private double[][] getInverseJacobian() {
        int rows = jacobian.length;
        int cols = jacobian[0].length;
        if(rows<cols) return getPseudoInverseOverdetermined();
        else if (rows>cols) return getPseudoInverseUnderdetermined();
        else {
            return getInverseDampedLeastSquares(0.0001);
            //return MatrixHelper.invert(jacobian);  // old way
        }
    }

    /**
     * Moore-Penrose pseudo-inverse for over-determined systems.
     * J_plus = J.transpose * (J*J.transpose()).inverse() // This is for
     * @return the pseudo-inverse of the jacobian matrix.
     */
    private double[][] getPseudoInverseOverdetermined() {
        double[][] jt = MatrixHelper.transpose(jacobian);
        double[][] mm = MatrixHelper.multiplyMatrices(jacobian, jt);
        double[][] ji = MatrixHelper.invert(mm);
        return MatrixHelper.multiplyMatrices(jt, ji);
    }

    /**
     * Moore-Penrose pseudo-inverse for under-determined systems.
     * J_plus = (J.transpose()*J).inverse() * J.transpose()
     * @return the pseudo-inverse of the jacobian matrix.
     */
    private double[][] getPseudoInverseUnderdetermined() {
        double[][] jt = MatrixHelper.transpose(jacobian);
        double[][] mm = MatrixHelper.multiplyMatrices(jt, jacobian);
        double[][] ji = MatrixHelper.invert(mm);
        return MatrixHelper.multiplyMatrices(ji, jt);
    }

    private double[][] getInverseDampedLeastSquares(double lambda) {
        double[][] jt = MatrixHelper.transpose(jacobian);
        double[][] jjt = MatrixHelper.multiplyMatrices(jacobian, jt);

        // Add lambda^2 * identity matrix to jjt
        for (int i = 0; i < jacobian.length; i++) {
            jjt[i][i] += lambda * lambda;
        }

        double[][] jjt_inv = MatrixHelper.invert(jjt);
        return MatrixHelper.multiplyMatrices(jt, jjt_inv);
    }

    /**
     * Use the Jacobian to get the joint velocity from the cartesian velocity.
     * @param cartesianVelocity 6 doubles - the XYZ translation and UVW rotation forces on the end effector.
     * @return jointVelocity joint velocity in degrees. Will be filled with the new velocity.
     * @throws Exception if joint velocities have NaN values
     */
    public double[] getJointForceFromCartesianForce(final double[] cartesianVelocity) throws Exception {
        double[][] inverseJacobian = getInverseJacobian();
        double[] jointVelocity = new double[DOF];

        // vector-matrix multiplication (y = x^T A)
        for (int j=0; j<DOF; ++j) {
            double sum = 0;
            for (int k=0; k<cartesianVelocity.length; ++k) {
                sum += inverseJacobian[j][k] * cartesianVelocity[k];
            }
            if (Double.isNaN(sum)) {
                throw new Exception("Bad inverse Jacobian.  Singularity?");
            }
            jointVelocity[j] = Math.toDegrees(sum);
        }

        return jointVelocity;
    }

    public double[][] getJacobian() {
        return jacobian;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jacobian:\n");
        for (double[] doubles : jacobian) {
            sb.append("[");
            for (int j = 0; j < doubles.length; j++) {
                sb.append(StringHelper.formatDouble(doubles[j]));
                if (j < doubles.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    /**
     * The time derivative is calculated by multiplying the jacobian by the joint velocities.
     * You can do this by taking the derivative of each element of the Jacobian matrix individually.
     * @param jointVelocities joint velocities in radians.  In classical notation this would be qDot.
     * @return the time derivative of the Jacobian matrix.
     */
    public double[][] getTimeDerivative(double [] jointVelocities) {
        if(jointVelocities.length!=DOF) throw new IllegalArgumentException("jointVelocities must be the same length as the number of joints.");

        double[][] jacobianDot = new double[6][DOF];  // Initialize the derivative of the Jacobian matrix

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < DOF; j++) {
                // Calculate the derivative of each element of the Jacobian
                double derivative = 0.0;
                for (int k = 0; k < DOF; k++) {
                    derivative += jacobian[i][k] * jointVelocities[k];  // Assuming qDot represents joint velocities
                }
                jacobianDot[i][j] = derivative;
            }
        }
        return jacobianDot;
    }

    /**
     * Calculate the coriolis term.
     * @param jointVelocities joint velocities in radians.  In classical notation this would be qDot.
     * @return the coriolis term.
     */
    public double[] getCoriolisTerm(double[] jointVelocities) {
        if(jointVelocities.length!=DOF) throw new IllegalArgumentException("jointVelocities must be the same length as the number of joints.");

        // Initialize the coriolis term vector with zeros
        double[] coriolisTerm = new double[DOF];

        for (int i = 0; i < DOF; i++) {
            for (int j = 0; j < DOF; j++) {
                for (int k = 0; k < DOF; k++) {
                    // Calculate the Coriolis term contribution for joint i
                    double v = jacobian[k][i] * jacobian[k][j];
                    coriolisTerm[i] += -0.5 * (
                            v * jointVelocities[i] +
                            v * jointVelocities[j] -
                            v * jointVelocities[k]);
                }
            }
        }

        return coriolisTerm;
    }
}
