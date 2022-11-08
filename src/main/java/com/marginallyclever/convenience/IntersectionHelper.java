package com.marginallyclever.convenience;

import com.marginallyclever.robotoverlord.mesh.Mesh;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


/**
 * Convenience methods for detecting intersection of various shapes.
 * @author Dan Royer
 */
public class IntersectionHelper {
	static final float SMALL_NUM = 0.001f;
	/**
	 * test intersection of two cylinders.  From http://geomalgorithms.com/a07-_distance.html
	 * @param cA cylinder A
	 * @param cB cylinder B
	 * @return true if intersect
	 */
	static public boolean cylinderCylinder(Cylinder cA,Cylinder cB) {
	    Vector3d   u = new Vector3d(cA.GetP2());  u.sub(cA.GetP1());
	    Vector3d   v = new Vector3d(cB.GetP2());  v.sub(cB.GetP1());
	    Vector3d   w = new Vector3d(cA.GetP1());  w.sub(cB.GetP1());
	    double    a = u.dot(u);         // always >= 0
	    double    b = u.dot(v);
	    double    c = v.dot(v);         // always >= 0
	    double    d = u.dot(w);
	    double    e = v.dot(w);
	    double    D = a*c - b*b;        // always >= 0
	    double    sc, sN, sD = D;       // sc = sN / sD, default sD = D >= 0
	    double    tc, tN, tD = D;       // tc = tN / tD, default tD = D >= 0

	    // compute the line parameters of the two closest points
	    if (D < SMALL_NUM) { // the lines are almost parallel
	        sN = 0.0f;         // force using point P0 on segment S1
	        sD = 1.0f;         // to prevent possible division by 0.0 later
	        tN = e;
	        tD = c;
	    }
	    else {                 // get the closest points on the infinite lines
	        sN = (b*e - c*d);
	        tN = (a*e - b*d);
	        if (sN < 0.0) {        // sc < 0 => the s=0 edge is visible
	            sN = 0.0f;
	            tN = e;
	            tD = c;
	        }
	        else if (sN > sD) {  // sc > 1  => the s=1 edge is visible
	            sN = sD;
	            tN = e + b;
	            tD = c;
	        }
	    }

	    if (tN < 0.0) {            // tc < 0 => the t=0 edge is visible
	        tN = 0.0f;
	        // recompute sc for this edge
	        if (-d < 0.0)
	            sN = 0.0f;
	        else if (-d > a)
	            sN = sD;
	        else {
	            sN = -d;
	            sD = a;
	        }
	    }
	    else if (tN > tD) {      // tc > 1  => the t=1 edge is visible
	        tN = tD;
	        // recompute sc for this edge
	        if ((-d + b) < 0.0)
	            sN = 0;
	        else if ((-d + b) > a)
	            sN = sD;
	        else {
	            sN = (-d +  b);
	            sD = a;
	        }
	    }

	    // finally do the division to get sc and tc
	    sc = Math.abs(sN) < SMALL_NUM ? 0.0f : sN / sD;
	    tc = Math.abs(tN) < SMALL_NUM ? 0.0f : tN / tD;

	    // get the difference of the two closest points
	    //Vector   dP = w + (sc * u) - (tc * v);  // =  L1(sc) - L2(tc)
	    u.scale(sc);
	    v.scale(tc);
	    Vector3d dP = new Vector3d(w);
	    dP.add(u);
	    dP.sub(v);

	    //Log.message(ca.getRadius()+"\t"+cb.getRadius()+"\t("+(ca.getRadius()+cb.getRadius())+") >=\t"+dP.length()+"\n");

	    return dP.length() <= (cA.getRadius()+cB.getRadius());   // return the closest distance
	}
	
	
	/**
	 * Find the time of the closest point of approach
	 * @param dp distance between positions
	 * @param dv relative velocities
	 * @return time
	 */
	static protected double CPATime(Vector3d dp,Vector3d dv) {
		double dv2 = dv.dot(dv);
		if(dv2 < SMALL_NUM) return 0;  // parallel, all times are the same.
		return -dv.dot(dp) / dv2;
	}
	
	static public double CPADistance(Vector3d a,Vector3d b,Vector3d da,Vector3d db) {
		// find CPA time
		Vector3d dp = new Vector3d(b);
		dp.sub(a);
		Vector3d dv = new Vector3d(db);
		db.sub(da);		
		double t = CPATime(dp,dv);

		// get both points
		Vector3d pa = new Vector3d(da);
		pa.scale(t);
		pa.add(a);
		Vector3d pb = new Vector3d(db);
		pb.scale(t);
		pb.add(b);
		// find difference
		pb.sub(pa);
		return pb.length();
	}
	
	/**
	 * separation of axies theorem used to find intersection of two arbitrary boxes.
	 * @param a first cuboid
	 * @param b second cuboid
	 * @return true if cuboids intersect
	 */
	static public boolean cuboidCuboid(Cuboid a,Cuboid b) {
		// infinitely small cuboids will incorrectly report hitting everything.
		if(a.boundTop.epsilonEquals(a.boundBottom, 1e-6)) {
			return false;
		}
		if(b.boundTop.epsilonEquals(b.boundBottom, 1e-6)) {
			return false;
		}

		// only does the second test if the first test succeeds.
		return cuboidCuboidInternal(a,b) &&
				cuboidCuboidInternal(b,a);
	}
	
	
	static protected boolean cuboidCuboidInternal(Cuboid a,Cuboid b) {
		// get the normals for A
		Vector3d[] n = new Vector3d[3];
		n[0] = new Vector3d(a.pose.m00, a.pose.m10, a.pose.m20);
		n[1] = new Vector3d(a.pose.m01, a.pose.m11, a.pose.m21);
		n[2] = new Vector3d(a.pose.m02, a.pose.m12, a.pose.m22);
		// Log.message("aMatrix="+a.poseWorld);
		
		a.updatePoints();
		b.updatePoints();

		for (int i = 0; i < n.length; ++i) {
			// SATTest the normals of A against the 8 points of box A.
			// SATTest the normals of A against the 8 points of box B.
			// points of each box are a combination of the box's top/bottom values.
			double[] aLim = SATTest(n[i], a.p);
			double[] bLim = SATTest(n[i], b.p);
			// Log.message("Lim "+axis[i]+" > "+n[i].x+"\t"+n[i].y+"\t"+n[i].z+" :
			// "+aLim[0]+","+aLim[1]+" vs "+bLim[0]+","+bLim[1]);

			// if the two box projections do not overlap then there is no chance of a
			// collision.
			if (!overlaps(aLim[0], aLim[1], bLim[0], bLim[1])) {
				// Log.message("Miss");
				return false;
			}
		}

		// intersect!
		// Log.message("Hit");
		return true;
	}
	
	static protected boolean isBetween(double val, double bottom, double top) {
		return bottom <= val && val <= top;
	}

	static protected boolean overlaps(double a0, double a1, double b0, double b1) {
		return isBetween(b0, a0, a1) || isBetween(a0, b0, b1);
	}

	static protected double[] SATTest(Vector3d normal, Point3d[] corners) {
		double[] values = new double[2];
		values[0] =  Double.MAX_VALUE; // min value
		values[1] = -Double.MAX_VALUE; // max value

		for (int i = 0; i < corners.length; ++i) {
			double dotProduct = corners[i].x * normal.x + corners[i].y * normal.y + corners[i].z * normal.z;
			if (values[0] > dotProduct) values[0] = dotProduct;
			if (values[1] < dotProduct) values[1] = dotProduct;
		}

		return values;
	}
	
	/**
	 * https://en.wikipedia.org/wiki/Line%E2%80%93plane_intersection
	 * @param r ray
	 * @param p plane
	 * @return single intersection point or null
	 */
	static Vector3d rayPlane(Ray r,Plane p) {
		Vector3d diff = new Vector3d();
		diff.sub(p.start,r.start);
		double denominator = r.direction.dot(p.normal);
		if(denominator<1e-6) {  // some tiny epsilon
			// ray is parallel to plane.
			return null;
		}
		double numerator = diff.dot(p.normal);
		double d = p.distanceToPlane();
		double t = (numerator +d) / denominator;
		if(t<0) {
			// plane behind ray aka ray heading away from plane
			return null;
		}

		Vector3d intersection = new Vector3d(r.direction);
		intersection.scale(t);
		intersection.add(r.start);
		
		return intersection;
	}
	
	/**
	 * First fine ray/plane intersection, then test if intersection is inside triangle.
	 * Uses the first three points of the polygon to construct a plane.
	 * https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-rendering-a-triangle/ray-triangle-intersection-geometric-solution
	 * @param r ray
	 * @param points 3+ points that make up a convex polygon in a plane
	 * @return single intersection point or null.
	 */
	static Vector3d rayConvexPolygon(Ray r, Point3d [] points) {
		assert(points.length>3);
		
		Plane plane = new Plane(points[0],points[1],points[2]);
		Vector3d planePoint = rayPlane(r,plane);
		if(planePoint==null) {
			// no intersection
			return null;
		}

		// test if point is inside convex polygon
		int s = points.length;
		for(int i=0;i<s;++i) {
			Vector3d e0 = new Vector3d(points[(i+1)%s]); e0.sub(points[i]);
			Vector3d c0 = new Vector3d(planePoint); c0.sub(points[i]);
			Vector3d temp = new Vector3d();
			temp.cross(e0,c0);	if(plane.normal.dot(temp)<=0) return null;
		}
		
		return planePoint;
	}

	/**
	 * TODO mesh / mesh intersection
	 * @return true if the two mesh intersect.
	 */
	public static boolean meshMesh(final Matrix4d ma, final Mesh sa, final Matrix4d mb, final Mesh sb) {

		return false;
	}


	/**
	 * ray/sphere intersection. @see <a href='https://viclw17.github.io/2018/07/16/raytracing-ray-sphere-intersection/'>reference</a>.
	 * @param ray
	 * @param center
	 * @param radius
	 * @return distance to first hit.  negative values for no hit/behind start. 
	 */
	static public double raySphere(final Ray ray,final Point3d center,final double radius) {
		Vector3d oc = new Vector3d();
		oc.sub(ray.start,center);
	    double a = ray.direction.dot(ray.direction);
	    double b = 2.0 * oc.dot(ray.direction);
	    double c = oc.dot(oc) - radius*radius;
	    double discriminant = b*b - 4*a*c;
	    if(discriminant >= 0) {
	        return (-b - Math.sqrt(discriminant)) / (2.0*a);
	    }
	    // no hit
        return -1.0;
	}
	
	/**
	 * find distance to box, if hit.
	 * https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection
	 * @param ray start & direction
	 * @param boxMin lower bounds
	 * @param boxMax upper bounds
	 * @return &gt;=0 for hit, negative numbers for hits behind camera and no hit.
	 */
	static public double rayBox(final Ray ray,final Point3d boxMin,final Point3d boxMax) {
	    double tmin = (boxMin.x - ray.start.x) / ray.direction.x; 
	    double tmax = (boxMax.x - ray.start.x) / ray.direction.x; 
	
	    if (tmin > tmax) {
	    	double temp = tmin;
	    	tmin=tmax;
	    	tmax=temp;
	    }
	
	    double tymin = (boxMin.y - ray.start.y) / ray.direction.y; 
	    double tymax = (boxMax.y - ray.start.y) / ray.direction.y; 
	
	    if (tymin > tymax) {
	    	double temp = tymin;
	    	tymin=tymax;
	    	tymax=temp;
	    }
	
	    if ((tmin > tymax) || (tymin > tmax)) 
	        return -1; 
	
	    if (tymin > tmin) 
	        tmin = tymin; 
	
	    if (tymax < tmax) 
	        tmax = tymax; 
	
	    double tzmin = (boxMin.z - ray.start.z) / ray.direction.z; 
	    double tzmax = (boxMax.z - ray.start.z) / ray.direction.z; 
	
	    if (tzmin > tzmax) {
	    	double temp = tzmin;
	    	tzmin=tzmax;
	    	tzmax=temp;
	    }
	
	    if ((tmin > tzmax) || (tzmin > tmax)) 
	        return -1; 
	
	    if (tzmin > tmin) 
	        tmin = tzmin; 
	
	    //if (tzmax < tmax) 
	    //    tmax = tzmax; 
	
	    return tmin; 
	}


	/**
	 * 
	 * @param planePoint point on plane
	 * @param planeNormal normal of plane
	 * @param rayPoint origin of ray
	 * @param rayNormal direction of ray
	 * @return Double.POSITIVE_INFINITY if no collision (orthogonal).  otherwise, distance to plane.
	 */
	static public double rayPlane(final Vector3d planePoint,final Vector3d planeNormal,final Vector3d rayPoint,final Vector3d rayNormal) {
		Vector3d dp = new Vector3d(planePoint);
		dp.sub(rayPoint);
	
		double denominator = rayNormal.dot(planeNormal);
		if(denominator==0) {
			// rays are orthogonal, never collide.
			return Double.POSITIVE_INFINITY;
		} else {
			double numerator = dp.dot(planeNormal);
			return numerator/denominator;
		}
	}

	/**
	 * <a href="https://en.wikipedia.org/wiki/Circumscribed_circle">circumscribed circle</a>
	 * @param a point 1
	 * @param b point 2
	 * @param c point 3
	 * @return the center of the circumscribed circle.
	 */
	static public Vector3d centerOfCircumscribedCircle(Vector3d a, Vector3d b, Vector3d c) {
		// find the point between a and b.
		Vector3d ab = new Vector3d(b);
		ab.sub(a);
		// find the point between b and c.
		Vector3d ac = new Vector3d(c);
		ac.sub(a);

		// find the normal of the plane containing the three points.
		Vector3d n = new Vector3d();
		n.cross(ab,ac);

		// dot(p21, p21) * p31
		Vector3d t0 = new Vector3d();
		t0.set(ac);
		t0.scale(ab.dot(ab));
		// dot(p31, p31) * p21
		Vector3d t1 = new Vector3d();
		t1.set(ab);
		t1.scale(ac.dot(ac));
		// dot(p21, p21) * p31 - dot(p31, p31) * p21
		Vector3d t2 = new Vector3d();
		t2.sub(t0,t1);
		// cross( dot(p21, p21) * p31 - dot(p31, p31) * p21, n)
		Vector3d p0 = new Vector3d();
		p0.cross(t2,n);
		// / 2 / dot(n, n)
		p0.scale(1.0/(2.0*n.dot(n)));
		p0.add(a);

		return p0;
	}

	static public Vector3d centerOfCircumscribedSphere(Vector3d a, Vector3d b, Vector3d c,double r) {
		// find the point between a and b.
		Vector3d ab = new Vector3d(b);
		ab.sub(a);
		// find the point between b and c.
		Vector3d ac = new Vector3d(c);
		ac.sub(a);

		// find the normal of the plane containing the three points.
		Vector3d n = new Vector3d();
		n.cross(ab,ac);

		Vector3d d = centerOfCircumscribedCircle(a,b,c);
		Vector3d ad = new Vector3d(d);
		ad.sub(a);

		double t = -Math.sqrt( (r*r - ad.dot(ad)) / n.dot(n) );
		n.scale(t);
		d.add(n);

		return d;
	}
}
