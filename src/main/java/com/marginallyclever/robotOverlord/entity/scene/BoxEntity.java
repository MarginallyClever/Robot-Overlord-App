package com.marginallyclever.robotOverlord.entity.scene;


import java.util.ArrayList;
import java.util.Observable;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.Model;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class BoxEntity extends ModelEntity {
	protected DoubleEntity width = new DoubleEntity("Width",1.0);
	protected DoubleEntity height = new DoubleEntity("Height",1.0);
	protected DoubleEntity depth = new DoubleEntity("Depth",1.0);
	
	public BoxEntity() {
		super();
		setName("Box");
		addChild(width);
		addChild(height);
		addChild(depth);
		
		width.addObserver(this);
		height.addObserver(this);
		depth.addObserver(this);
		
		model = new Model();
	}

	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		return super.getCuboidList();
	}

	@Override
	public void update(Observable o, Object arg) {
		updateCuboid();
		super.update(o, arg);
	}
	
	private void updateCuboid() {
		Point3d _boundBottom = new Point3d(-width.get()/2,-depth.get()/2,0           );
		Point3d _boundTop    = new Point3d( width.get()/2, depth.get()/2,height.get());
		cuboid.setBounds(_boundTop, _boundBottom);
		
		updateModel();
	}
	
	/**
	 * Procedurally generate a list of triangles that form a box, subdivided by some amount.
	 */
	protected void updateModel() {
		model.clear();
		model.renderStyle=GL2.GL_TRIANGLES;
		//model.renderStyle=GL2.GL_LINES;  // set to see the wireframe
		
		float w = (float)(width.get()/2);
		float d = (float)(depth.get()/2);
		float h = (float)(height.get()*1.0);
		
		int wParts = (int)(w/4)*2;
		int hParts = (int)(h/8)*2;
		int dParts = (int)(d/4)*2;
		
		Vector3d n=new Vector3d();
		Vector3d p0=new Vector3d();
		Vector3d p1=new Vector3d();
		Vector3d p2=new Vector3d();
		Vector3d p3=new Vector3d();
		
		// bottom
		n.set( 0, 0,-1);
		p0.set(-w, d,0);
		p1.set( w, d,0);
		p2.set( w,-d,0);
		p3.set(-w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);

		// top
		n.set( 0, 0, 1);
		p0.set( w, d,h);
		p1.set(-w, d,h);
		p2.set(-w,-d,h);
		p3.set( w,-d,h);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);
		
		// sides
		n.set( 0, 1, 0);
		p0.set(-w, d,h);
		p1.set( w, d,h);
		p2.set( w, d,0);
		p3.set(-w, d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

		n.set( 0,-1, 0);
		p0.set( w,-d,h);
		p1.set(-w,-d,h);
		p2.set(-w,-d,0);
		p3.set( w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,(int)(w/10),hParts);
		
		n.set( 1, 0, 0);
		p0.set( w, d,0);
		p1.set( w, d,h);
		p2.set( w,-d,h);
		p3.set( w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);
	
		n.set(-1, 0, 0);
		p0.set(-w,-d,h);
		p1.set(-w, d,h);
		p2.set(-w, d,0);
		p3.set(-w,-d,0);
		addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);
	}

	/**
	 * Subdivide a plane into triangles.
	 * @param n plane normal
	 * @param p0 northwest corner
	 * @param p1 northeast corner
	 * @param p2 southeast corner
	 * @param p3 southwest corner
	 * @param xParts east/west divisions
	 * @param yParts north/south divisions
	 */
	protected void addSubdividedPlane(Vector3d n,
			Vector3d p0,
			Vector3d p1,
			Vector3d p2,
			Vector3d p3,
			int xParts,
			int yParts) {
		xParts = Math.max(xParts, 1);
		yParts = Math.max(yParts, 1);

		Vector3d pA=new Vector3d();
		Vector3d pB=new Vector3d();
		Vector3d pC=new Vector3d();
		Vector3d pD=new Vector3d();
		Vector3d pE=new Vector3d();
		Vector3d pF=new Vector3d();
		Vector3d pG=new Vector3d();
		Vector3d pH=new Vector3d();

		for(int x=0;x<xParts;x++) {
			pA.set(MathHelper.interpolate(p0, p1, (double)(x  )/(double)xParts));
			pB.set(MathHelper.interpolate(p0, p1, (double)(x+1)/(double)xParts));
			pC.set(MathHelper.interpolate(p3, p2, (double)(x  )/(double)xParts));
			pD.set(MathHelper.interpolate(p3, p2, (double)(x+1)/(double)xParts));
			
			for(int y=0;y<yParts;y++) {
				pE.set(MathHelper.interpolate(pA, pC, (double)(y  )/(double)yParts));
				pF.set(MathHelper.interpolate(pB, pD, (double)(y  )/(double)yParts));
				pG.set(MathHelper.interpolate(pA, pC, (double)(y+1)/(double)yParts));
				pH.set(MathHelper.interpolate(pB, pD, (double)(y+1)/(double)yParts));

				if(model.renderStyle == GL2.GL_TRIANGLES) {
					model.hasNormals=true;
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					model.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					model.addNormal((float)n.x, (float)n.y, (float)n.z);
					
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					model.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
				} else if(model.renderStyle == GL2.GL_LINES) {
					model.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

					model.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					model.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					
					model.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					model.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
				}
			}
		}
	}
	
	public void setWidth(double v) {
		width.set(v);
		updateCuboid();
	}
	
	public void setHeight(double v) {
		height.set(v);
		updateCuboid();
	}
	
	public void setDepth(double v) {
		depth.set(v);
		updateCuboid();
	}

	public void setSize(double w, double h, double d) {
		width.set(w);
		height.set(h);
		depth.set(d);
		updateCuboid();
	}
	
	public double getWidth() { return width.get(); }
	public double getHeight() { return height.get(); }
	public double getDepth() { return depth.get(); }

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Bx", "Box");
		width.getView(view);
		height.getView(view);
		depth.getView(view);
		view.popStack();
		super.getView(view);
	}
}
