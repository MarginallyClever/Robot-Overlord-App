package com.marginallyclever.convenience;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


public class IntersectionTester {
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
	 * @param a
	 * @param b
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
		n[0] = new Vector3d(a.poseWorld.m00, a.poseWorld.m10, a.poseWorld.m20);
		n[1] = new Vector3d(a.poseWorld.m01, a.poseWorld.m11, a.poseWorld.m21);
		n[2] = new Vector3d(a.poseWorld.m02, a.poseWorld.m12, a.poseWorld.m22);
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
}
