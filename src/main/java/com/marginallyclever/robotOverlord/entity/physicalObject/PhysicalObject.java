package com.marginallyclever.robotOverlord.entity.physicalObject;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.EntityPanel;
import com.marginallyclever.robotOverlord.entity.world.World;

public abstract class PhysicalObject extends Entity {
	protected Matrix4d matrix;	// position and orientation
	protected Cuboid cuboid;	// physical limits
	private transient PhysicalObjectPanel physicalObjectControlPanel;
	
	public PhysicalObject() {
		super();
		matrix = new Matrix4d();
		matrix.setIdentity();
		cuboid=new Cuboid();
	}
	
	/**
	 * Get the {@link EntityPanel} for this class' superclass, then the physicalObjectControlPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of physicalObjectControlPanels 
	 */
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();

		physicalObjectControlPanel = new PhysicalObjectPanel(gui,this);
		list.add(physicalObjectControlPanel);

		return list;
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, getMatrix());
			for(Entity e : children ) {
				e.render(gl2);
			}
		gl2.glPopMatrix();
	}
	
	public Vector3d getPosition() {
		return new Vector3d(matrix.m03,matrix.m13,matrix.m23);
	}
	
	public void setPosition(Vector3d pos) {
		Matrix4d m = new Matrix4d(matrix);
		m.setTranslation(pos);
		setMatrix(m);
	}

	/**
	 * 
	 * @param arg0 fills the vector3 with one possible combination of radian rotations.
	 */
	public void getRotation(Vector3d arg0) {
		Matrix3d temp = new Matrix3d();
		matrix.get(temp);
		arg0.set(MatrixHelper.matrixToEuler(temp));
	}

	/**
	 * 
	 * @param arg0 Vector3d of radian rotation values
	 */
	public void setRotation(Vector3d arg0) {
		Matrix4d m4 = new Matrix4d();
		Matrix3d m3 = MatrixHelper.eulerToMatrix(arg0);
		m4.set(m3);
		m4.setTranslation(getPosition());
		setMatrix(m4);
	}
	
	public void setRotation(Matrix3d arg0) {
		Matrix4d m = new Matrix4d();
		m.set(arg0);
		m.setTranslation(getPosition());
		setMatrix(m);
	}
	
	public void getRotation(Matrix4d arg0) {
		arg0.set(matrix);
		arg0.setTranslation(new Vector3d(0,0,0));
	}
	
	public Matrix4d getMatrix() {
		return matrix;
	}
	
	public void setMatrix(Matrix4d arg0) {
		matrix.set(arg0);
		if(physicalObjectControlPanel!=null) {
			physicalObjectControlPanel.updateFields();	
		}
	}

	public World getWorld() {
		Entity p = parent;
		while (p != null) {
			if (p instanceof World) {
				return (World) p;
			}
			p=p.getParent();
		}
		return null;
	}

	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	public ArrayList<Cuboid> getCuboidList() {		
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();
		
		cuboid.setMatrix(this.getMatrix());
		cuboidList.add(cuboid);

		return cuboidList;
	}
}
