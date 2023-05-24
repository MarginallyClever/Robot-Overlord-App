package com.marginallyclever.misc;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.convenience.log.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;

public class MiscTests {
    private static final Logger logger = LoggerFactory.getLogger(MiscTests.class);
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
        logger.info("a=" + a);
        logger.info("b=" + b);
        assert (a.equals("*78"));
        logger.info("test a passed");
        assert (b.equals("*111"));
        logger.info("test b passed");
    }

    /**
     * @see <a href="https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf">York U</a>
     */
    @Test
    @Disabled
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

        logger.info("time=" + (end - start) + "ms");
        //MatrixOperations.printMatrix(m, 1);
        //MatrixOperations.printMatrix(mInv, 1);
        logger.info("t\tp\tv\ta\t" + a + "\t" + b + "\t" + c + "\t" + d + "\t" + e + "\t" + f);
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
            logger.info(t + "\t" + pt + "\t" + vt + "\t" + at);
        }
    }

    private double[][] buildMatrix(double t0, double tf) {
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

    @Test
    public void testFloatBufferAdd() {
        FloatBuffer a = FloatBuffer.allocate(3*3);
        a.put(-1.0f);
        a.put(-1.0f);
        a.put(0.0f);
        a.put(1.0f);
        a.put(-1.0f);
        a.put(0.0f);
        a.put(0.0f);
        a.put(1.0f);
        a.put(0.0f);
        a.rewind();

        FloatBuffer b = FloatBuffer.wrap(new float[]{
                -1f,-1f,0f,
                1f,-1f,0f,
                0f,1f,0f,
        });
        b.rewind();

        Assertions.assertTrue(a.equals(b));
    }
}
