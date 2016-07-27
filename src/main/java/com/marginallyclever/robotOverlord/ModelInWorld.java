package com.marginallyclever.robotOverlord;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class ModelInWorld extends ObjectInWorld {
	/**
	 * 
	 */
	private static final long serialVersionUID = 180224086839215506L;
	
	protected String filename = null;
	protected transient Model model;
	
	public ModelInWorld() {}

	
	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		if( this.filename!=filename ) {
			this.filename = filename;
			model=null;
		}
	}

	@Override
	public String getDisplayName() {
		return "Model";
	}
	
	
	public void render(GL2 gl2) {
		if( model==null && filename != null ) {
			model = ModelFactory.createModelFromFilename(filename);
		}
		if( model==null ) return;

		Vector3f p = getPosition();
		
		material.render(gl2);
		gl2.glPushMatrix();
			gl2.glTranslatef(p.x, p.y, p.z);
			model.render(gl2);
		gl2.glPopMatrix();
	}
	
	public void selectModelFile() {
		JFrame topFrame = null;
		//TODO something like topFrame = (JFrame)SwingUtilities.getWindowAncestor(this);
		
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("STL files", "STL", "abc");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(topFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getAbsolutePath();
    		this.setFilename( filename );
		}
	}
}
