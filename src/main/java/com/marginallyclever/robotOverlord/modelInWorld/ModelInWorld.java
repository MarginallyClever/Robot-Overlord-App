package com.marginallyclever.robotOverlord.modelInWorld;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;


public class ModelInWorld extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 180224086839215506L;
	
	protected String filename = null;
	protected transient Model model;
	protected transient ModelInWorldPanel modelPanel;
	protected Material material;
	
	// model render scale
	protected float scale=1;
	// model adjusted origin
	protected Vector3f originAdjust;
	
	
	public ModelInWorld() {
		super();
		originAdjust = new Vector3f();
		material = new Material();
	}

	
	public String getFilename() {
		return filename;
	}
	
	
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		modelPanel = new ModelInWorldPanel(gui,this);
		list.add(modelPanel);
		
		list.addAll(material.getContextPanel(gui));
		
		return list;
	}


	public void setFilename(String newFilename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		if( this.filename != newFilename ) {
			this.filename = newFilename;
			model=null;
		}
	}

	public void setScale(float arg0) {
		scale=arg0;
		if(model!=null) {
			model.setScale(arg0);
		}
	}
	
	public float getScale() {
		return scale;
	}

	public void adjustOrigin(float x,float y,float z) {
		originAdjust.x=x;
		originAdjust.y=y;
		originAdjust.z=z;
		if(model!=null) model.adjustOrigin(originAdjust);
	}
	
	public Vector3f getAdjustOrigin() {
		return new Vector3f(originAdjust);
	}
	
	@Override
	public String getDisplayName() {
		return "Model";
	}
	
	
	public void render(GL2 gl2) {
		if( model==null && filename != null ) {
			try {
				model = ModelFactory.createModelFromFilename(filename);
				model.setScale(scale);
				model.adjustOrigin(originAdjust);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		Vector3f p = getPosition();
		
		gl2.glPushMatrix();
		
		gl2.glTranslatef(p.x, p.y, p.z);
		
		// TODO: this should probably be an option that can be toggled.
		// It is here to fix scaling of the entire model.  It won't work when the model is scaled unevenly.
		gl2.glEnable(GL2.GL_NORMALIZE);

		if( model==null ) {
			// draw placeholder
			PrimitiveSolids.drawStar(gl2,new Vector3f(0,0,0),10f);
		} else {
			material.render(gl2);
			model.render(gl2);
		}
		
		gl2.glPopMatrix();
	}
}
