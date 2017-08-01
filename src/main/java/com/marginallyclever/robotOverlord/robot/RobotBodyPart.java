package com.marginallyclever.robotOverlord.robot;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.Material;
import com.marginallyclever.robotOverlord.model.Model;

/**
 * Segmented robots should be made of RobotBodyParts.  Each part has a location, an orientation, a model, and linkages to other body parts.
 * @author Dan Royer
 *
 */
public class RobotBodyPart {
	Model model;
	Material material;
	
	Vector3f position;
	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glTranslatef(position.x, position.y, position.z);
		material.render(gl2);
		model.render(gl2);
		gl2.glPopMatrix();
	}
}
