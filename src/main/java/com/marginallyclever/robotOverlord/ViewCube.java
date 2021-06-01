package com.marginallyclever.robotOverlord;


import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;

public class ViewCube extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2625823417579183587L;
	protected Shape model = new Shape();
	protected DoubleEntity cubeSize = new DoubleEntity("size",6);
	
    public ViewCube() {
    	super();
    	setName("ViewCube");
    	addChild(cubeSize);
    	model.setShapeFilename("/viewCube.obj");
    	model.getMaterial().setTextureFilename("/images/viewCube.png");
    	model.getMaterial().setDiffuseColor(1, 1, 1, 1);
    	model.getMaterial().setAmbientColor(1, 1, 1, 1);
    	model.getMaterial().setLit(false);
    }
		
	public void render(GL2 gl2) {
		RobotOverlord ro = (RobotOverlord)getRoot();
		Viewport cameraView = ro.viewport;
		
		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);

		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_CULL_FACE);
		
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);

    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPushMatrix();
		cameraView.renderOrtho(gl2,1);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		
		gl2.glPushMatrix();			
			double c = cubeSize.get();			
			PoseEntity camera = cameraView.getAttachedTo();
			Matrix4d m = new Matrix4d();
			camera.getPoseWorld(m);
			Vector3d p = camera.getPosition();
			Vector3d vx = MatrixHelper.getXAxis(m);
			Vector3d vy = MatrixHelper.getYAxis(m);
			Vector3d vz = MatrixHelper.getZAxis(m);
		
			vz.scale(-100);
			vx.scale(cameraView.getCanvasWidth() /10 -c*2);
			vy.scale(cameraView.getCanvasHeight()/10 -c*2);
			p.add(vx);
			p.add(vy);
			p.add(vz);
			
			gl2.glTranslated(p.x, p.y, p.z);
			gl2.glScaled(c,c,c);

			model.render(gl2);

			gl2.glDisable(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_COLOR_MATERIAL);
			gl2.glDisable(GL2.GL_TEXTURE_2D);
			
			// the big lines
			gl2.glLineWidth(4);
			gl2.glPushMatrix();
				gl2.glTranslated(-1.05,-1.05,-0.95);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3d(1, 0, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(2.5, 0, 0);
				gl2.glColor3d(0, 1, 0);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 2.5, 0);
				gl2.glColor3d(0, 0, 1);		gl2.glVertex3d(0, 0, 0);		gl2.glVertex3d(0, 0, 2.5);
				gl2.glEnd();
			gl2.glPopMatrix();
			gl2.glLineWidth(1);
						
		gl2.glPopMatrix();

    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPopMatrix();
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("VC", "View Cube");
		view.add(cubeSize);
		view.popStack();
		super.getView(view);
	}
}
