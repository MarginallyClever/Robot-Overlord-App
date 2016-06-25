package com.marginallyclever.robotOverlord;
import javax.vecmath.Vector3f;


public class IntersectionTester {
	static final float SMALL_NUM = 0.001f;
	/**
	 * test intersection of two cylinders.  From http://geomalgorithms.com/a07-_distance.html
	 * @param a1 one end of cylinder a
	 * @param a2 other end of cylinder a
	 * @param ar radius of cylinder a
	 * @param b1 one end of cylinder b
	 * @param b2 other end of cylinder b
	 * @param br radius of cylinder b
	 * @return true if intersect
	 */
	static public boolean CylinderCylinder(Cylinder cA,Cylinder cB) {
	    Vector3f   u = new Vector3f(cA.GetP2());  u.sub(cA.GetP1());
	    Vector3f   v = new Vector3f(cB.GetP2());  v.sub(cB.GetP1());
	    Vector3f   w = new Vector3f(cA.GetP1());  w.sub(cB.GetP1());
	    float    a = u.dot(u);         // always >= 0
	    float    b = u.dot(v);
	    float    c = v.dot(v);         // always >= 0
	    float    d = u.dot(w);
	    float    e = v.dot(w);
	    float    D = a*c - b*b;        // always >= 0
	    float    sc, sN, sD = D;       // sc = sN / sD, default sD = D >= 0
	    float    tc, tN, tD = D;       // tc = tN / tD, default tD = D >= 0

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
	    Vector3f dP = new Vector3f(w);
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
	static float CPATime(Vector3f dp,Vector3f dv) {
		float dv2 = dv.dot(dv);
		if(dv2 < SMALL_NUM) return 0;  // parallel, all times are the same.
		return -dv.dot(dp) / dv2;
	}
	
	static float CPADistance(Vector3f a,Vector3f b,Vector3f da,Vector3f db) {
		// find CPA time
		Vector3f dp = new Vector3f(b);
		dp.sub(a);
		Vector3f dv = new Vector3f(db);
		db.sub(da);		
		float t = CPATime(dp,dv);

		// get both points
		Vector3f pa = new Vector3f(da);
		pa.scale(t);
		pa.add(a);
		Vector3f pb = new Vector3f(db);
		pb.scale(t);
		pb.add(b);
		// find difference
		pb.sub(pa);
		return pb.length();
	}
}
