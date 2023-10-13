package com.marginallyclever.convenience;

import javax.vecmath.Vector2d;

/**
 * map from a sphere to a plane and back; map from a cube to a sphere and back.
 * @author Dan Royer
 * @since 2023-10-11
 */
public class SphericalMap {
    public static class CubeCoordinate {
        public static final int TOP = 0;
        public static final int RIGHT = 1;
        public static final int FRONT = 2;
        public static final int LEFT = 3;
        public static final int BACK = 4;
        public static final int BOTTOM = 5;

        public int face;
        public Vector2d position = new Vector2d();

        public CubeCoordinate() {}

        public CubeCoordinate(int face,double x,double y) {
            this.face = face;
            this.position.x = x;
            this.position.y = y;
        }

        @Override
        public String toString() {
            return "{" + face +", " + position + "}";
        }
    }

    /**
     * map plane to sphere
     * @param u x position on plane, value 0...1
     * @param v y position on plane, value 0...1
     * @return pan/tilt position on sphere in radians
     */
    public static double [] planeToPanTilt(double u, double v) {
        return new double []{
                (u-0.5)*Math.PI*2.0,
                (v-0.5)*Math.PI,
        };
    }

    /**
     * map sphere to plane
     * @param sphere unit vector on sphere
     * @return uv coordinate on plane, values 0...1
     */
    public static double[] sphereToPlane(double[] sphere) {
        double pan = Math.atan2(sphere[1],sphere[0]);
        double tilt = Math.atan2(sphere[2],Math.sqrt(sphere[0]*sphere[0]+sphere[1]*sphere[1]));
        return new double[]{
                (pan/(Math.PI*2.0))+0.5,
                (tilt/Math.PI)+0.5,
        };
    }

    /**
     * @param u x position on plane, value 0...1
     * @param v y position on plane, value 0...1
     * @return unit vector on sphere
     */
    public static double [] planeToSphere(double u,double v) {
        double [] panTilt = planeToPanTilt(u,v);
        // convert pan/tilt to unit vector
        double [] xyz = new double[]{
            Math.cos(panTilt[1]) * Math.cos(panTilt[0]),
            Math.cos(panTilt[1]) * Math.sin(panTilt[0]),
            Math.sin(panTilt[1]),
        };

        return makeUnitVector(xyz);
    }

    /**
     * Cube index 0 is top, 1 is left, 2 is front, 3 is right, 4 is back, 5 is bottom
     * @param unitVector unit vector on sphere
     * @return CubeCoordinate face and position on face.
     */
    public static CubeCoordinate sphereToCube(double [] unitVector) {
        double x = unitVector[0];
        double y = unitVector[1];
        double z = unitVector[2];

        // convert unit vector to cube face and x,y
        CubeCoordinate result = new CubeCoordinate();
        double ax = Math.abs(x);
        double ay = Math.abs(y);
        double az = Math.abs(z);
        double u,v,maxAxis;

        // Determine which cube face the point lies on
        if (ax >= ay && ax >= az) { // X face
            if (x > 0) {
                result.face = CubeCoordinate.RIGHT;
                maxAxis = ax;
                v = z;
                u = y;
            } else {
                result.face = CubeCoordinate.LEFT;
                maxAxis = ax;
                v = z;
                u = -y;
            }
        } else if (ay >= ax && ay >= az) { // Y face
            if (y > 0) {
                result.face = CubeCoordinate.FRONT;
                maxAxis = ay;
                u =-x;
                v =z;
            } else {
                result.face = CubeCoordinate.BACK;
                maxAxis = ay;
                u = x;
                v = z;
            }
        } else { // Z face
            if (z > 0) {
                result.face = CubeCoordinate.TOP;
                maxAxis = az;
                v = -x;
                u = y;
            } else {
                result.face = CubeCoordinate.BOTTOM;
                maxAxis = az;
                v = x;
                u = y;
            }
        }

        // Rescale from [-1, 1] to [0, 1]
        result.position.x = (u/maxAxis + 1.0) * 0.5;
        result.position.y = (v/maxAxis + 1.0) * 0.5;

        return result;
    }

    /**
     * convert uv on plane to cube face and position on face.
     * @param u x position on plane, value 0...1
     * @param v y position on plane, value 0...1
     * @return CubeCoordinate face and position on face.
     */
    public static CubeCoordinate planeToCube(double u, double v) {
        double [] panTilt = planeToSphere(u,v);
        return sphereToCube(panTilt);
    }

    /**
     * convert uv on plane to unit vector on sphere
     * @param index cube face index.  See CubeCoordinate constants.
     * @param u x position on plane, value 0...1
     * @param v y position on plane, value 0...1
     * @return unit vector on sphere
     */
    public static double [] cubeToSphere(int index, double u, double v) {
        if(index<0||index>5) throw new RuntimeException("index must be 0..5");
        if(u>1||u<0) throw new RuntimeException("u must be 0..1");
        if(v>1||v<0) throw new RuntimeException("v must be 0..1");

        double uc = 2*u-1;
        double vc = 2*v-1;
        double x,y,z;
        switch(index) {
            case CubeCoordinate.TOP:   x=-vc; y= uc; z= 1; break;
            case CubeCoordinate.RIGHT: x= 1; y= uc; z= vc; break;
            case CubeCoordinate.FRONT: x=-uc; y= 1; z= vc; break;
            case CubeCoordinate.LEFT:  x=-1; y=-uc; z= vc; break;
            case CubeCoordinate.BACK:  x= uc; y=-1; z= vc; break;
            case CubeCoordinate.BOTTOM:x= vc; y= uc; z=-1; break;
            default: throw new RuntimeException("Invalid cube index "+index);
        }
        return makeUnitVector(new double []{ x,y,z });
    }

    private static double [] makeUnitVector(double [] xyz) {
        double len = Math.sqrt(xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
        return new double []{ xyz[0]/len, xyz[1]/len, xyz[2]/len };
    }
}