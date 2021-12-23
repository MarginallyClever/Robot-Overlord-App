package com.marginallyclever.robotOverlord;


import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

public class ViewCube extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2625823417579183587L;
	protected Shape model = new Shape();
	protected DoubleEntity cubeSize = new DoubleEntity("size",32);
	
    public ViewCube() {
    	super();
    	setName("ViewCube");
    	addChild(cubeSize);
    	model.setShapeFilename("/viewCube.obj");
    	MaterialEntity mat = model.getMaterial(); 
    	mat.setTextureFilename("/images/viewCube.png");
    	mat.setDiffuseColor(1, 1, 1, 1);
    	mat.setAmbientColor(1, 1, 1, 1);
    	mat.setLit(false);
    }
    
	@Override
	public void render(GL2 gl2) {
		Viewport viewport = ((RobotOverlord)getRoot()).getViewport();
		
		startProjection(gl2,viewport);
		
		gl2.glPushMatrix();
			positionCubeModel(gl2,viewport);
			renderCubeModel(gl2);
			renderMajorAxies(gl2);
		gl2.glPopMatrix();

		endProjection(gl2);
	}
		
    private Matrix4d getInverseCameraMatrix(PoseEntity camera) {
		Matrix4d m = camera.getPoseWorld();
		m.invert();
		m.setTranslation(new Vector3d(0,0,0));
		return m;
    }
    
    private void startProjection(GL2 gl2,Viewport viewport) {
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPushMatrix();
		MatrixHelper.setMatrix(gl2, MatrixHelper.createIdentityMatrix4());
		viewport.renderOrtho(gl2,0.2);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
    }
    
    private void endProjection(GL2 gl2) {
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPopMatrix();
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
    }
	
	private void positionCubeModel(GL2 gl2, Viewport viewport) {
		double c = cubeSize.get();
        double w2 = viewport.getCanvasWidth()/2;
        double h2 = viewport.getCanvasHeight()/2;
		MatrixHelper.setMatrix(gl2, MatrixHelper.createIdentityMatrix4());
		gl2.glTranslated(w2-c*2,h2-c*2,-c*2);
		gl2.glScaled(c,c,c);
		MatrixHelper.applyMatrix(gl2, getInverseCameraMatrix(viewport.getAttachedTo()));
	}

	private void renderCubeModel(GL2 gl2) {
		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);
		model.render(gl2);
	}

	private void renderMajorAxies(GL2 gl2) {
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
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
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("VC", "View Cube");
		view.add(cubeSize);
		view.popStack();
		super.getView(view);
	}
}
