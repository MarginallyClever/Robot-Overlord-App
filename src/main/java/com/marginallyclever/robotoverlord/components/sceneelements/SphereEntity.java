package com.marginallyclever.robotoverlord.components.sceneelements;

import java.io.Serial;
import java.util.ArrayList;

import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.robotoverlord.Collidable;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;

/**
 * {@code Sphere} can be turned any way in space.  {@code Cuboid} can only be aligned to world axies.
 * @author aggra
 *
 */
@Deprecated
public class SphereEntity extends ShapeEntity implements Collidable {
	@Serial
	private static final long serialVersionUID = 8418101632870526950L;
	
	protected DoubleEntity diameter = new DoubleEntity("Diameter",1.0);
	
	private final Cuboid cuboid = new Cuboid();
	
	public SphereEntity() {
		super();
		setName(SphereEntity.class.getSimpleName());
		addChild(diameter);
		
		diameter.addPropertyChangeListener((evt)-> updateModel() );
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
		double r = diameter.get()/2;
		Point3d _boundBottom = new Point3d(-r,-r,-r);
		Point3d _boundTop    = new Point3d( r, r, r);
		cuboid.setBounds(_boundTop, _boundBottom);
	}
	
	// Procedurally generate a list of triangles that form a box, subdivided by some amount.
	private void updateModel() {
		myMesh.clear();
		myMesh.renderStyle=GL2.GL_TRIANGLES;

		float r = (float)( diameter.get()/2.0 );

		int width = 64;
		int height = 32;
		
		double theta, phi;
		int i, j;
		
		for( j=1; j<height-1; j++ ) {
			for(i=0; i<width; i++ )  {
				double tx = (double)(i)/(double)(width-1 );
				double ty = (double)(j)/(double)(height-1);
				phi   = tx * Math.PI*2;
				theta = ty * Math.PI;

				float x = (float)( Math.sin(theta) * Math.cos(phi));
				float y = (float)( Math.cos(theta));
				float z = (float)(-Math.sin(theta) * Math.sin(phi));
				myMesh.addVertex(r*x,r*y,r*z);
				myMesh.addNormal(x,y,z);
				myMesh.addTexCoord((float)tx, (float)ty);

			}
		}
		myMesh.addVertex(0,r*1,0);
		myMesh.addNormal(0,1,0);
		myMesh.addTexCoord(0.5f,0);

		myMesh.addVertex(0,r*-1,0);
		myMesh.addNormal(0,-1,0);
		myMesh.addTexCoord(0.5f,1);

		for( j=0; j<height-3; j++ ) {
			for( i=0; i<width-1; i++ )  {
				myMesh.addIndex( (j  )*width + i  );
				myMesh.addIndex( (j+1)*width + i+1);
				myMesh.addIndex( (j  )*width + i+1);
				myMesh.addIndex( (j  )*width + i  );
				myMesh.addIndex( (j+1)*width + i  );
				myMesh.addIndex( (j+1)*width + i+1);
			}
		}
		for( i=0; i<width-1; i++ )  {
			myMesh.addIndex( (height-2)*width );
			myMesh.addIndex( i );
			myMesh.addIndex( i+1 );
			myMesh.addIndex( (height-2)*width+1 );
			myMesh.addIndex( (height-3)*width + i+1 );
			myMesh.addIndex( (height-3)*width + i );
		}

		updateCuboid();
	}
	
	public void setDiameter(double v) {
		diameter.set(v);
		updateModel();
	}
	
	public void setRadius(double v) {
		diameter.set(v*2);
	}
	
	public double getDiameter () { return diameter.get(); }
	public double getRadius() { return diameter.get()/2; }

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sp", "Sphere");
		view.add(diameter);
		view.popStack();
		material.getView(view);
		super.getView(view);
	}
}
