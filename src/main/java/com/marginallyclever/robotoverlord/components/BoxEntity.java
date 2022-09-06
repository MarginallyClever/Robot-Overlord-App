package com.marginallyclever.robotoverlord.components;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotoverlord.Collidable;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;

/**
 * {@code Box} can be turned any way in space.  {@code Cuboid} can only be aligned to world axies.
 * @author aggra
 *
 */
@Deprecated
public class BoxEntity extends ShapeEntity implements Collidable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8418101632870526950L;
	
	protected DoubleEntity width = new DoubleEntity("Width",1.0);
	protected DoubleEntity height = new DoubleEntity("Height",1.0);
	protected DoubleEntity depth = new DoubleEntity("Depth",1.0);
	
	private Cuboid cuboid = new Cuboid();
	
	public BoxEntity() {
		super();
		setName("Box");
		addChild(width);
		addChild(height);
		addChild(depth);
		
		width .addPropertyChangeListener((evt)-> updateModel() );
		height.addPropertyChangeListener((evt)-> updateModel() );
		depth .addPropertyChangeListener((evt)-> updateModel() );
		myMesh = new Mesh();
		updateModel();
	}

	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> list = new ArrayList<Cuboid>();
		cuboid.setPose(getPoseWorld());
		list.add(cuboid);
		return list;
	}

	@Override
	public void updateCuboid() {
		Point3d _boundBottom = new Point3d(-width.get()/2,-depth.get()/2,-height.get()/2);
		Point3d _boundTop    = new Point3d( width.get()/2, depth.get()/2, height.get()/2);
		cuboid.setBounds(_boundTop, _boundBottom);
	}
	
	// Procedurally generate a list of triangles that form a box, subdivided by some amount.
	private void updateModel() {
		myMesh.clear();
		myMesh.renderStyle=GL2.GL_TRIANGLES;
		//shape.renderStyle=GL2.GL_LINES;  // set to see the wireframe
		
		float w = width.get().floatValue()/2;
		float d = depth.get().floatValue()/2;
		float h = height.get().floatValue()/2;
		
		int wParts = width.get().intValue();     
		int hParts = depth.get().intValue();     
		int dParts = height.get().intValue();    
		
		Vector3d n=new Vector3d();
		Vector3d p0=new Vector3d();
		Vector3d p1=new Vector3d();
		Vector3d p2=new Vector3d();
		Vector3d p3=new Vector3d();

		// top
		n.set( 0, 0, 1);
		p0.set( w, d,h);
		p1.set(-w, d,h);
		p2.set(-w,-d,h);
		p3.set( w,-d,h);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);
		
		// bottom
		n.set( 0, 0,-1);
		p0.set(-w, d,-h);
		p1.set( w, d,-h);
		p2.set( w,-d,-h);
		p3.set(-w,-d,-h);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,dParts);
		
		// sides
		n.set( 0, 1, 0);
		p0.set(-w, d,h);
		p1.set( w, d,h);
		p2.set( w, d,-h);
		p3.set(-w, d,-h);
		addSubdividedPlane(n,p0,p1,p2,p3,wParts,hParts);

		n.set( 0,-1, 0);
		p0.set( w,-d,h);
		p1.set(-w,-d,h);
		p2.set(-w,-d,-h);
		p3.set( w,-d,-h);
		addSubdividedPlane(n,p0,p1,p2,p3,(int)(w/10),hParts);
		
		n.set( 1, 0, 0);
		p0.set( w, d,-h);
		p1.set( w, d,h);
		p2.set( w,-d,h);
		p3.set( w,-d,-h);
		addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);
	
		n.set(-1, 0, 0);
		p0.set(-w,-d,h);
		p1.set(-w, d,h);
		p2.set(-w, d,-h);
		p3.set(-w,-d,-h);
		addSubdividedPlane(n,p0,p1,p2,p3,dParts,hParts);

		updateCuboid();
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
	private void addSubdividedPlane(Vector3d n,
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

				if(myMesh.renderStyle == GL2.GL_TRIANGLES) {
					for(int i=0;i<6;++i) {
						myMesh.addNormal((float)n.x, (float)n.y, (float)n.z);
					}			
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					
				} else if(myMesh.renderStyle == GL2.GL_LINES) {
					myMesh.addVertex((float)pF.x, (float)pF.y, (float)pF.z);
					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);

					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);

					myMesh.addVertex((float)pH.x, (float)pH.y, (float)pH.z);
					myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					
					myMesh.addVertex((float)pG.x, (float)pG.y, (float)pG.z);
					myMesh.addVertex((float)pE.x, (float)pE.y, (float)pE.z);
				}
			}
		}
	}
	
	public void setWidth(double v) {
		width.set(v);
	}
	
	public void setHeight(double v) {
		height.set(v);
	}
	
	public void setDepth(double v) {
		depth.set(v);
	}

	public void setSize(double w, double d, double h) {
		width.set(w);
		depth.set(d);
		height.set(h);
	}
	
	public double getWidth () { return width .get(); }
	public double getHeight() { return height.get(); }
	public double getDepth () { return depth .get(); }

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Bx", "Box");
		view.add(width);
		view.add(height);
		view.add(depth);
		view.popStack();
		super.getView(view);
	}
}
