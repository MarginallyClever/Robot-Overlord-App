package com.marginallyclever.convenience;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Cylinder;


public class IntersectionTester {
	static final float SMALL_NUM = 0.001f;
	/**
	 * test intersection of two cylinders.  From http://geomalgorithms.com/a07-_distance.html
	 * @param cA cylinder A
	 * @param cB cylinder B
	 * @return true if intersect
	 */
	static public boolean CylinderCylinder(Cylinder cA,Cylinder cB) {
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

	    //System.out.print(ca.getRadius()+"\t"+cb.getRadius()+"\t("+(ca.getRadius()+cb.getRadius())+") >=\t"+dP.length()+"\n");

	    return dP.length() <= (cA.getRadius()+cB.getRadius());   // return the closest distance
	}
	
	
	/**
	 * Find the time of the closest point of approach
	 * @param dp distanc between positions
	 * @param dv relative velocities
	 * @return time
	 */
	static double CPATime(Vector3d dp,Vector3d dv) {
		double dv2 = dv.dot(dv);
		if(dv2 < SMALL_NUM) return 0;  // parallel, all times are the same.
		return -dv.dot(dp) / dv2;
	}
	
	static double CPADistance(Vector3d a,Vector3d b,Vector3d da,Vector3d db) {
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
}
