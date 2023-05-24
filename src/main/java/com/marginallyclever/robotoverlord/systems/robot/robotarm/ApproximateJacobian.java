package com.marginallyclever.robotoverlord.systems.robot.robotarm;

public interface ApproximateJacobian {
    double ANGLE_STEP_SIZE_DEGREES = 0.1; // degrees

    double[] getCartesianForceFromJointForce(double[] jointForce);

    double[] getJointForceFromCartesianForce(double[] cartesianVelocity) throws Exception;

    double[][] getJacobian();
}
