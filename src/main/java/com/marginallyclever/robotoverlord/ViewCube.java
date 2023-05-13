package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A cube that shows the user where the camera is looking.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ViewCube {
	protected ShapeComponent model = new MeshFromFile("/viewCube.obj");
	protected MaterialComponent mat = new MaterialComponent();
	protected DoubleParameter cubeSize = new DoubleParameter("size",25);
	
    public ViewCube() {
    	super();

    	mat.setTextureFilename("/images/viewCube.png");
    	mat.setDiffuseColor(1, 1, 1, 1);
    	mat.setAmbientColor(1, 1, 1, 1);
    	mat.setLit(false);
    }

	@Deprecated
	public void render(GL2 gl2,Viewport viewport) {
		startProjection(gl2,viewport);
		
		gl2.glPushMatrix();
			positionCubeModel(gl2,viewport);
			renderCubeModel(gl2);
			renderMajorAxies(gl2);
		gl2.glPopMatrix();

		endProjection(gl2);
	}
    
    private void startProjection(GL2 gl2,Viewport viewport) {
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		viewport.renderPerspective(gl2);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
    }
    
    private void endProjection(GL2 gl2) {
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPopMatrix();
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
    }
	
	private void positionCubeModel(GL2 gl2, Viewport viewport) {
		cubeSize.set(25d);
		double distance = cubeSize.get();
		double c = 0.25;
		double ar = viewport.getAspectRatio();
		double fov = Math.cos(Math.toRadians(viewport.getFieldOfView()));

        double w2 = distance * fov * ar -c;
        double h2 = distance * fov      -c;
		MatrixHelper.setMatrix(gl2, MatrixHelper.createIdentityMatrix4());
		gl2.glTranslated(w2,h2,-distance);
		MatrixHelper.applyMatrix(gl2, getInverseCameraMatrix(viewport.getCamera()));
	}

	private Matrix4d getInverseCameraMatrix(CameraComponent camera) {
		Matrix4d m = camera.getEntity().getComponent(PoseComponent.class).getWorld();
		m.invert();
		m.setTranslation(new Vector3d(0,0,0));
		return m;
	}

	private void renderCubeModel(GL2 gl2) {
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);
		mat.render(gl2);
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
}
