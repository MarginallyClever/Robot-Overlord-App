package arm5;
import javax.vecmath.Vector3f;


public class Cylinder extends BoundingVolume {
	private Vector3f p1 = new Vector3f(0,0,0);
	private Vector3f p2 = new Vector3f(0,0,0);
	private Vector3f n = new Vector3f(0,0,0);
	private Vector3f f = new Vector3f(0,0,0);
	private Vector3f r = new Vector3f(0,0,0);
	float radius;
	
	public void SetP1(Vector3f src) {
		p1.set(src);
		UpdateVectors();
	}
	public void SetP2(Vector3f src) {
		p2.set(src);
		UpdateVectors();
	}
	
	public Vector3f GetP1() {
		return p1;
	}
	
	public Vector3f GetP2() {
		return p2;
	}
	public Vector3f GetN() {
		return n;
	}
	public Vector3f GetF() {
		return f;
	}
	public Vector3f GetR() {
		return r;
	}
	
	public void UpdateVectors() {
		n.set(p2);
		n.sub(p1);
		n.normalize();
		
		if(n.x > n.y) {
			if(n.x > n.z) {
				// x major
				f.z=n.z;
				f.y=n.x;
				f.x=n.y;
			} else {
				// z major
				f.z=n.y;
				f.y=n.z;
				f.x=n.x;
			}
		} else {
			if(n.y > n.z) {
				// y major
				f.z=n.z;
				f.y=n.x;
				f.x=n.y;
			} else {
				// z major
				f.z=n.y;
				f.y=n.z;
				f.x=n.x;
			}
		}
		r.cross(f, n);
		r.normalize();
		f.cross(n, r);
		f.normalize();
	}
}
