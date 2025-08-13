package com.marginallyclever.ro3.mesh;

import com.marginallyclever.convenience.BoundingVolume;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.io.Serializable;

/**
 * {@link AABB} is a bounding box aligned to the world axies.  Used for fast sorting and filtering.
 */
public class AABB implements BoundingVolume, Serializable {
	protected Point3d max = new Point3d();  // max limits
	protected Point3d min = new Point3d();  // min limits
	
	public Point3d [] corners = new Point3d[8];  // all 8 corners
	
	private boolean isDirty=false;
	private Mesh myShape;
	
	
	public AABB() {
		super();
		for(int i = 0; i< corners.length; ++i) corners[i] = new Point3d();
	}

	public void set(AABB b) {
		max.set(b.max);
		min.set(b.min);
		myShape = b.myShape;

		for(int i=0;i<8;++i) corners[i].set(b.corners[i]);
		
		isDirty=b.isDirty;
	}
	
	public void updatePoints() {
		if(!isDirty) return;
		isDirty=false;
		
		corners[0].set(min.x, min.y, min.z);
		corners[1].set(min.x, min.y, max.z);
		corners[2].set(min.x, max.y, min.z);
		corners[3].set(min.x, max.y, max.z);
		corners[4].set(max.x, min.y, min.z);
		corners[5].set(max.x, min.y, max.z);
		corners[6].set(max.x, max.y, min.z);
		corners[7].set(max.x, max.y, max.z);
	}

	public void setBounds(Point3d newMax, Point3d newMin) {
		if(!this.max.epsilonEquals(newMax, 1e-4))  {
			this.max.set(newMax);
			isDirty=true;
		}
		if(!this.min.epsilonEquals(newMin, 1e-4)) {
			this.min.set(newMin);
			isDirty=true;
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

	public void setDirty(boolean newState) {
		isDirty=newState;
	}

	public void setShape(Mesh shape) {
		myShape=shape;
	}
	
	public Mesh getShape() {
		return myShape;
	}

	public boolean intersect(Ray ray) {
		return IntersectionHelper.rayBox(ray, min, max)>=0;
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

		for(AABB child : children) {
			child.setDirty(true);
		}

		return children;
	}
}
