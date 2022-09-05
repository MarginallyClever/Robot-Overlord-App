package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.robots.robotarm.ApproximateJacobian;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmFK;
import com.marginallyclever.robotoverlord.robots.robotarm.implementations.Sixi3_5axis;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MiscTests {
    static final double ANGLE_STEP_SIZE = 30.0000;

	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    @Test
    public void testChecksums() {
        //>>G0 X0.000 Y-86.789 Z27.498 U0.000 V-30.692 W0.000*78
        //<<G0 X0.000 Y-86.789 Z27.4U0.000 V-30.692 W0.000*78
        //<<BADCHECKSUM calc=111 sent=78

        String a = StringHelper.generateChecksum("G0 X0.000 Y-86.789 Z27.498 U0.000 V-30.692 W0.000");
        String b = StringHelper.generateChecksum("G0 X0.000 Y-86.789 Z27.4U0.000 V-30.692 W0.000");
        Log.message("a=" + a);
        Log.message("b=" + b);
        assert (a.equals("*78"));
        Log.message("test a passed");
        assert (b.equals("*111"));
        Log.message("test b passed");
    }

    /**
     * See https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
     */
    @Test
    public void solveLinearEquations() {
        // we have 6 linear equation and six unknowns
        // p0 = a + b*t0 +  c*t0^2 +  d*t0^3 +  e*t0^4 +   f*t0^5
        // pf = a + b*tf +  c*tf^2 +  d*tf^3 +  e*tf^4 +   f*tf^5
        // v0 =     b    + 2c*t0   + 3d*t0^2 + 4e*t0^3 +  5f*t0^4
        // vf =     b    + 2c*tf   + 3d*tf^2 + 4e*tf^3 +  5f*tf^4
        // a0 =          + 2c      + 6d*t0   + 12e*t0^2 + 20f*t0^3
        // af =          + 2c      + 6d*tf   + 12e*tf^2 + 20f*tf^3
        // or expressed as a matrix, Q = M*N
        // [p0]=[ 1 t0  t0^2  t0^3   t0^4   t0^5][a]
        // [pf]=[ 1 tf  tf^2  tf^3   tf^4   tf^5][b]
        // [v0]=[ 0  1 2t0   3t0^2  4t0^3  5t0^4][c]
        // [vf]=[ 0  1 2tf   3tf^2  4tf^3  5tf^4][d]
        // [a0]=[ 0  0 2     6t0   12t0^2 20t0^3][e]
        // [af]=[ 0  0 2     6tf   12tf^2 20tf^3][f]
        // I know Q and M.  I can Q * inverse(M) to get N.
        // then I can solve the original polynomials for any t betwen t0 and tf.

        double t0 = 0, tf = 100;
        double p0 = 0, pf = 90;
        double v0 = 0, vf = 0;
        double a0 = 0, af = 0;

        double[] q = new double[6];
        q[0] = p0;
        q[1] = pf;
        q[2] = v0;
        q[3] = vf;
        q[4] = a0;
        q[5] = af;

        long start = System.currentTimeMillis();

        double[][] m = buildMatrix(t0, tf);
        double[][] mInv = MatrixHelper.invertMatrix(m);
        double[] n = MatrixHelper.multiply(mInv, q);

        long end = System.currentTimeMillis();

        double a = n[0];
        double b = n[1];
        double c = n[2];
        double d = n[3];
        double e = n[4];
        double f = n[5];

        Log.message("time=" + (end - start) + "ms");
        //MatrixOperations.printMatrix(m, 1);
        //MatrixOperations.printMatrix(mInv, 1);
        Log.message("t\tp\tv\ta\t" + a + "\t" + b + "\t" + c + "\t" + d + "\t" + e + "\t" + f);
        for (double t = t0; t <= tf; t++) {
            // p0 = a + b*t0 +  c*t0^2 +  d*t0^3 +   e*t0^4 +   f*t0^5
            // v0 =     b    + 2c*t0   + 3d*t0^2 +  4e*t0^3 +  5f*t0^4
            // a0 =          + 2c      + 6d*t0   + 12e*t0^2 + 20f*t0^3
            double t2 = t * t;
            double t3 = t * t * t;
            double t4 = t * t * t * t;
            double t5 = t * t * t * t * t;
            double pt = a * b * t + c * t2 + d * t3 + e * t4 + f * t5;
            double vt = b + 2 * c * t + 3 * d * t2 + 4 * e * t3 + 5 * f * t4;
            double at = +2 * c + 6 * d * t + 12 * e * t2 + 20 * f * t3;
            Log.message(t + "\t" + pt + "\t" + vt + "\t" + at);
        }
    }

    public double[][] buildMatrix(double t0, double tf) {
        double t02 = t0 * t0;
        double tf2 = tf * tf;
        double t03 = t02 * t0;
        double tf3 = tf2 * tf;
        double t04 = t03 * t0;
        double tf4 = tf3 * tf;
        double t05 = t04 * t0;
        double tf5 = tf4 * tf;

        double[][] matrix = new double[6][6];

        // [p0]=[ 1 t0  t0^2  t0^3   t0^4   t0^5][a]
        // [pf]=[ 1 tf  tf^2  tf^3   tf^4   tf^5][b]
        // [v0]=[ 0  1 2t0   3t0^2  4t0^3  5t0^4][c]
        // [vf]=[ 0  1 2tf   3tf^2  4tf^3  5tf^4][d]
        // [a0]=[ 0  0 2     6t0   12t0^2 20t0^3][e]
        // [af]=[ 0  0 2     6tf   12tf^2 20tf^3][f]
        matrix[0][0] = 1;
        matrix[0][1] = t0;
        matrix[0][2] = t02;
        matrix[0][3] = t03;
        matrix[0][4] = t04;
        matrix[0][5] = t05;
        matrix[1][0] = 1;
        matrix[1][1] = tf;
        matrix[1][2] = tf2;
        matrix[1][3] = tf3;
        matrix[1][4] = tf4;
        matrix[1][5] = tf5;
        matrix[2][0] = 0;
        matrix[2][1] = 1;
        matrix[2][2] = 2 * t0;
        matrix[2][3] = 3 * t02;
        matrix[2][4] = 4 * t03;
        matrix[2][5] = 5 * t04;
        matrix[3][0] = 0;
        matrix[3][1] = 1;
        matrix[3][2] = 2 * tf;
        matrix[3][3] = 3 * tf2;
        matrix[3][4] = 4 * tf3;
        matrix[3][5] = 5 * tf4;
        matrix[4][0] = 0;
        matrix[4][1] = 0;
        matrix[4][2] = 2;
        matrix[4][3] = 6 * t0;
        matrix[4][4] = 12 * t02;
        matrix[4][5] = 20 * t03;
        matrix[5][0] = 0;
        matrix[5][1] = 0;
        matrix[5][2] = 2;
        matrix[5][3] = 6 * tf;
        matrix[5][4] = 12 * tf2;
        matrix[5][5] = 20 * tf3;

        return matrix;
    }

    /**
     * Plot points along the workspace boundary for the sixi robot in the XZ plane.
     */
    //@Test
    public void plotXZ() {
        Log.message("plotXZ()");
        RobotArmFK model = new RobotArmFK();
        int numLinks = model.getNumBones();
        assert (numLinks > 0);

        // Find the min/max range for each joint
        RobotArmBone link0 = model.getBone(0);
        double bottom0 = link0.getAngleMin();
        double top0 = link0.getAngleMax();
        double mid0 = (top0 + bottom0) / 2;
        RobotArmBone link1 = model.getBone(1);
        double bottom1 = link1.getAngleMin();
        double top1 = link1.getAngleMax();
        double mid1 = (top1 + bottom1) / 2;
        RobotArmBone link2 = model.getBone(2);
        double bottom2 = link2.getAngleMin();
        double top2 = link2.getAngleMax();//double mid2 = (top2+bottom2)/2;
        // link3 does not bend
        RobotArmBone link4 = model.getBone(4);
        double bottom4 = link4.getAngleMin();//double top4 = link4.getRangeMax();  double mid4 = (top4+bottom4)/2;
        RobotArmBone link5 = model.getBone(5);
        double bottom5 = link5.getAngleMin();
        double top5 = link5.getAngleMax();
        double mid5 = (top5 + bottom5) / 2;
        RobotArmBone link6 = model.getBone(6);
        double bottom6 = link6.getAngleMin();//double top6 = link6.getRangeMax();  double mid6 = (top6+bottom6)/2;

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/plotxz.csv")));
            out.write("X\tY\tZ\n");

            // go through the entire range of motion of the sixi 2 robot arm
            double ANGLE_STEP_SIZE2 = 1;

            double x = mid0;
            double y = bottom1;
            double z = bottom2;
            double u = bottom4;
            double v = bottom5;
            double w = bottom6;

            for (v = bottom5; v < mid5; v += ANGLE_STEP_SIZE2)
                plot(x, y, z, u, v, w, out, model);  // picasso box to middle
            for (y = bottom1; y < mid1; y += ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model); // shoulder forward
            // skip j0 to keep things on the XZ plane.
            for (; y < top1; y += ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);  // shoulder forward
            for (; z < top2; z += ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);  // elbow forward
            for (; v < top5; v += ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);  // picasso box forward

            for (; y > bottom1; y -= ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);  // shoulder back
            for (; z > bottom2; z -= ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);  // elbow back
            for (; v < bottom5; v -= ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);  // picasso box back

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.flush();
                if (out != null) out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Plot points along the workspace boundary for the sixi robot in the XY plane.
     */
    //@Test
    public void plotXY() {
        Log.message("plotXY()");
        RobotArmFK model = new RobotArmFK();
        int numLinks = model.getNumBones();
        assert (numLinks > 0);

        // Find the min/max range for each joint
        RobotArmBone link0 = model.getBone(0);
        double bottom0 = link0.getAngleMin();
        double top0 = link0.getAngleMax();//double mid0 = (top0+bottom0)/2;
        RobotArmBone link1 = model.getBone(1);/*double bottom1 = link1.getThetaMin();*/
        double top1 = link1.getAngleMax();//double mid1 = (top1+bottom1)/2;
        RobotArmBone link2 = model.getBone(2);
        double bottom2 = link2.getAngleMin();
        double top2 = link2.getAngleMax();//double mid2 = (top2+bottom2)/2;
        // link3 does not bend
        RobotArmBone link4 = model.getBone(4);
        double bottom4 = link4.getAngleMin();
        double top4 = link4.getAngleMax();
        double mid4 = (top4 + bottom4) / 2;
        RobotArmBone link5 = model.getBone(5);
        double bottom5 = link5.getAngleMin();
        double top5 = link5.getAngleMax();
        double mid5 = (top5 + bottom5) / 2;
        RobotArmBone link6 = model.getBone(6);
        double bottom6 = link6.getAngleMin();
        double top6 = link6.getAngleMax();
        double mid6 = (top6 + bottom6) / 2;

        double ANGLE_STEP_SIZE2 = 1;

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/plotxy.csv")));
            out.write("X\tY\tZ\n");

            // go through the entire range of motion of the sixi 2 robot arm
            // stretch arm forward as much as possible.
            double x = bottom0;
            double y = top1;
            double z = bottom2;
            double u = mid4;
            double v = mid5;
            double w = mid6;

            for (x = bottom0; x < top0; x += ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);
            for (; z < top2; z += ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);
            //for(;v<top5;v+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,model);

            for (x = top0; x > bottom0; x -= ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);
            //for(;v>mid5;v-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,model);
            for (; z > bottom2; z -= ANGLE_STEP_SIZE2) plot(x, y, z, u, v, w, out, model);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.flush();
                if (out != null) out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Used by plotXY() and plotXZ()
     *
     * @param x
     * @param y
     * @param z
     * @param u
     * @param v
     * @param w
     * @param out
     * @throws IOException
     */
    private void plot(double x, double y, double z, double u, double v, double w, BufferedWriter out, RobotArmFK model) throws IOException {
        double[] keyframe0 = model.getAngles();
        Matrix4d m0 = new Matrix4d();

        keyframe0[0] = x;
        keyframe0[1] = y;
        keyframe0[2] = z;
        keyframe0[3] = u;
        keyframe0[4] = v;
        keyframe0[5] = w;

        // use forward kinematics to find the endMatrix of the pose
        model.setAngles(keyframe0);
        m0.set(model.getToolCenterPoint());

        String message = StringHelper.formatDouble(m0.m03) + "\t"
                + StringHelper.formatDouble(m0.m13) + "\t"
                + StringHelper.formatDouble(m0.m23) + "\n";
        out.write(message);
    }

    /**
     * Report Jacobian results for a given pose
     */
    //@Test
    public void reportApproximateJacobianMatrix(String outputPath) {
        Log.message("approximateJacobianMatrix() start");
        RobotArmFK model = new Sixi3_5axis();

        // Find the min/max range for each joint
        int numBones = model.getNumBones();
        double[] top = new double[numBones];
        double[] bottom = new double[numBones];
        double[] mid = new double[numBones];
        RobotArmBone[] link = new RobotArmBone[numBones];

        for (int i = 0; i < numBones; ++i) {
            link[i] = model.getBone(i);
            bottom[i] = link[i].getAngleMin();
            top[i] = link[i].getAngleMax();
            mid[i] = (top[i] + bottom[i]) / 2;
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputPath)));){

            // set the pose with fk
            model.setAngles(mid);

            ApproximateJacobian aj = new ApproximateJacobian(model);
            int i, j;
            for (i = 0; i < 6; ++i) {
                for (j = 0; j < 6; ++j) {
                    out.write(aj.jacobian[i][j] + "\t");
                }
                out.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.message("approximateJacobianMatrix() end");
    }
}
