package com.marginallyclever.convenience;

import com.marginallyclever.convenience.SphericalMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test the {@link SphericalMap} class.
 * @author Dan Royer
 * @since 2023-10-11
 */
public class SphericalMapTest {

    private static final double EPSILON = 1e-9;

    @Test
    public void testPlaneToPanTilt() {
        double[] result = SphericalMap.planeToPanTilt(0.5, 0.5);
        Assertions.assertEquals(0.0, result[0], EPSILON);
        Assertions.assertEquals(0.0, result[1], EPSILON);
    }

    @Test
    public void testPlaneToSphere() {
        double[] result = SphericalMap.planeToSphere(0.5, 0.5);
        Assertions.assertEquals(1.0, result[0], EPSILON);
        Assertions.assertEquals(0.0, result[1], EPSILON);
        Assertions.assertEquals(0.0, result[2], EPSILON);
    }

    @Test
    public void testPlaneToSphere2() {
        double stepSize=0.1;
        for(double u=0;u<=1;u+=stepSize) {
            for(double v=0;v<=1;v+=stepSize) {
                double [] results = SphericalMap.sphereToPlane(SphericalMap.planeToSphere(u,v));
                Assertions.assertEquals(u, results[0], EPSILON," u failed for "+u+","+v);
                Assertions.assertEquals(v, results[1], EPSILON," v failed for "+u+","+v);
            }
        }
    }

    @Test
    public void testSphereToCube() {
        double[] sphereCoords = {0, 0, 1};
        SphericalMap.CubeCoordinate result = SphericalMap.sphereToCube(sphereCoords);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.TOP, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);

        sphereCoords = new double[]{0, 0, -1};
        result = SphericalMap.sphereToCube(sphereCoords);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.BOTTOM, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);

        sphereCoords = new double[]{0, 1, 0};
        result = SphericalMap.sphereToCube(sphereCoords);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.FRONT, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);

        sphereCoords = new double[]{0, -1, 0};
        result = SphericalMap.sphereToCube(sphereCoords);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.BACK, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);

        sphereCoords = new double[]{1, 0, 0};
        result = SphericalMap.sphereToCube(sphereCoords);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.RIGHT, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);

        sphereCoords = new double[]{-1, 0, 0};
        result = SphericalMap.sphereToCube(sphereCoords);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.LEFT, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);
    }

    @Test
    public void testPlaneToCube() {
        SphericalMap.CubeCoordinate result = SphericalMap.planeToCube(0.5, 0.5);
        Assertions.assertEquals(SphericalMap.CubeCoordinate.RIGHT, result.face);
        Assertions.assertEquals(0.5, result.position.x, EPSILON);
        Assertions.assertEquals(0.5, result.position.y, EPSILON);
    }

    /**
     * Test sphere to cube and back.
     */
    @Test
    public void testToAndFrom() {
        double stepSize=0.1;
        for(double u=0;u<=1;u+=stepSize) {
            for(double v=0;v<=1;v+=stepSize) {

                double [] before = SphericalMap.planeToSphere(u,v);
                SphericalMap.CubeCoordinate cube = SphericalMap.sphereToCube(before);
                double [] after = SphericalMap.cubeToSphere(cube.face,cube.position.x,cube.position.y);
                //System.out.println(u+", "+v+", "+cube.face+", "+cube.position.x+", "+cube.position.y+"\n\t"+before[0]+", "+before[1]+", "+before[2]+"\n\t"+after[0]+", "+after[1]+", "+after[2]);
                for(int i=0;i<3;++i) {
                    Assertions.assertEquals(before[i], after[i], EPSILON," failed for "+u+","+v);
                }
            }
        }
    }
}
