package com.marginallyclever.ro3.mesh;

import com.marginallyclever.convenience.BoundingVolume;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.RayAABBHit;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.io.Serializable;

/**
 * {@link AABB} is a bounding box aligned to the world axies.  Used for fast sorting and filtering.
 */
public class AABB implements BoundingVolume, Serializable {
	protected final Point3d max = new Point3d();  // max limits
	protected final Point3d min = new Point3d();  // min limits

	private Mesh myShape;
	
	
	public AABB() {
		super();
	}

	public void set(AABB b) {
		max.set(b.max);
		min.set(b.min);
		myShape = b.myShape;
	}

	public void setBounds(Point3d newMax, Point3d newMin) {
		if(!this.max.epsilonEquals(newMax, 1e-4))  {
			this.max.set(newMax);
		}
		if(!this.min.epsilonEquals(newMin, 1e-4)) {
			this.min.set(newMin);
		}
	}
	
	public Point3d getBoundsTop() {
		return this.max;
	}
	
	public Point3d getBoundsBottom() {
		return min;
	}
	
	public double getExtentX() {
		return max.x- min.x;
	}
	
	public double getExtentY() {
		return max.y- min.y;
	}
	
	public double getExtentZ() {
		return max.z- min.z;
	}

	public void setShape(Mesh shape) {
		myShape=shape;
	}
	
	public Mesh getShape() {
		return myShape;
	}

	public RayAABBHit intersect(Ray ray) {
		return IntersectionHelper.rayBox(ray, min, max);
	}

	/**
	 * Subdivide this AABB into 8 smaller AABBs.
	 * @return 8 new AABBs that are subdivisions of this AABB.
	 */
	public AABB [] subdivide() {
		AABB [] children = new AABB[8];
		for(int i=0;i<children.length;++i) {
			children[i] = new AABB();
			children[i].setShape(myShape);
		}

		Point3d mid = new Point3d(max);
		mid.add(min);
		mid.scale(0.5);

		children[0].setBounds(new Point3d(mid.x, mid.y, mid.z), new Point3d(min.x, min.y, min.z));
		children[1].setBounds(new Point3d(mid.x, mid.y, max.z), new Point3d(min.x, min.y, mid.z));
		children[2].setBounds(new Point3d(mid.x, max.y, mid.z), new Point3d(min.x, mid.y, min.z));
		children[3].setBounds(new Point3d(mid.x, max.y, max.z), new Point3d(min.x, mid.y, mid.z));
		children[4].setBounds(new Point3d(max.x, mid.y, mid.z), new Point3d(mid.x, min.y, min.z));
		children[5].setBounds(new Point3d(max.x, mid.y, max.z), new Point3d(mid.x, min.y, mid.z));
		children[6].setBounds(new Point3d(max.x, max.y, mid.z), new Point3d(mid.x, mid.y, min.z));
		children[7].setBounds(new Point3d(max.x, max.y, max.z), new Point3d(mid.x, mid.y, mid.z));

		return children;
	}

	/**
	 * Grow this AABB to include the point b.
	 * @param p the point to include in the AABB.
	 */
	public void grow(Point3d p) {
		if(p.x < min.x) min.x = p.x;
		if(p.y < min.y) min.y = p.y;
		if(p.z < min.z) min.z = p.z;
		if(p.x > max.x) max.x = p.x;
		if(p.y > max.y) max.y = p.y;
		if(p.z > max.z) max.z = p.z;
	}
}
