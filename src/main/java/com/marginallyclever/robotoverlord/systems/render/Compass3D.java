package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A 3D compass that shows the orientation of the camera.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class Compass3D {
	protected ShapeComponent model = new MeshFromFile("/viewCube.obj");
	protected MaterialComponent mat = new MaterialComponent();
	protected DoubleParameter cubeSize = new DoubleParameter("size",25);
	
    public Compass3D() {
    	super();
		mat.setTextureFilename("/images/viewCube.png");
		mat.setDiffuseColor(1, 1, 1, 1);
		mat.setAmbientColor(1, 1, 1, 1);
		mat.setLit(false);
    }

	@Deprecated
	public void render(GL2 gl2,Viewport viewport,ShaderProgram program) {
		program.use(gl2);
		program.set4f(gl2,"objectColor",1,1,1,1);
		program.set1f(gl2,"useVertexColor",0);
		boolean lit = OpenGLHelper.disableLightingStart(gl2);
		startProjection(gl2,viewport,program);
		positionCubeModel(gl2,viewport,program);
		renderCubeModel(gl2,program);
		gl2.glUseProgram(0);
		renderMajorAxies(gl2,program);
		endProjection(gl2);

		OpenGLHelper.disableLightingEnd(gl2,lit);
	}

	private void startProjection(GL2 gl2,Viewport viewport,ShaderProgram program) {
		program.setMatrix4d(gl2,"projectionMatrix",viewport.getPerspectiveFrustum());
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPushMatrix();
		gl2.glLoadIdentity();
		viewport.renderPerspective(gl2);
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
	}

	private void endProjection(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glPopMatrix();
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	private void positionCubeModel(GL2 gl2, Viewport viewport,ShaderProgram program) {
		cubeSize.set(25d);
		double distance = cubeSize.get();
		double c = 0.25;
		double ar = viewport.getAspectRatio();
		double fov = Math.cos(Math.toRadians(viewport.getFieldOfView()));

        double w2 = distance * fov * ar -c;
        double h2 = distance * fov      -c;
		// ff pipeline
		gl2.glLoadIdentity();
		gl2.glTranslated(w2,h2,-distance);
		MatrixHelper.applyMatrix(gl2, getInverseCameraMatrix(viewport.getCamera()));

		// programmable pipeline
		Matrix4d viewMatrix = new Matrix4d();
		viewMatrix.setIdentity();
		viewMatrix.setTranslation(new Vector3d(w2,h2,-distance));
		viewMatrix.transpose();
		program.setMatrix4d(gl2,"viewMatrix",viewMatrix);

		Matrix4d inverseCamera = getInverseCameraMatrix(viewport.getCamera());
		inverseCamera.transpose();
		// tell the shaders about our modelMatrix.
		program.setMatrix4d(gl2,"modelMatrix",inverseCamera);
		program.set1f(gl2,"useLighting",0);
		program.set1f(gl2,"useTexture",1);
	}

	private Matrix4d getInverseCameraMatrix(CameraComponent camera) {
		Matrix4d m = camera.getEntity().getComponent(PoseComponent.class).getWorld();
		m.invert();
		m.setTranslation(new Vector3d(0,0,0));
		return m;
	}

	private void renderCubeModel(GL2 gl2,ShaderProgram program) {
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_CULL_FACE);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl2.glEnable(GL2.GL_COLOR_MATERIAL);
		mat.render(gl2);
		gl2.glColor4d(1,1,1,1);
		model.render(gl2);
	}

	private void renderMajorAxies(GL2 gl2,ShaderProgram program) {
		boolean tex = OpenGLHelper.disableTextureStart(gl2);
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
		OpenGLHelper.disableTextureEnd(gl2,tex);
	}
}
