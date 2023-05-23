package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

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
		positionCubeModel(gl2,viewport,program);
		renderCubeModel(gl2,program);
		renderMajorAxies(gl2,program);
	}
	
	private void positionCubeModel(GL2 gl2, Viewport viewport,ShaderProgram program) {
		double scale = 25.0;
		double c = 2.0;
        double w2 = viewport.getCanvasWidth() /(2.0*scale) - c;
        double h2 = viewport.getCanvasHeight()/(2.0*scale) - c;

		program.setMatrix4d(gl2,"projectionMatrix",viewport.getOrthographicMatrix(scale));
		program.setMatrix4d(gl2,"viewMatrix",MatrixHelper.createIdentityMatrix4());

		Matrix4d modelMatrix = getInverseCameraMatrix(viewport.getCamera());
		modelMatrix.setTranslation(new Vector3d(w2,h2,-5));
		modelMatrix.transpose();
		program.setMatrix4d(gl2,"modelMatrix",modelMatrix);

		program.setVector3d(gl2,"lightPos",new Vector3d(w2,h2,10));  // Light position in world space
		program.setVector3d(gl2,"ambientLightColor",new Vector3d(0.6,0.6,0.6));
		program.setVector3d(gl2,"specularColor",new Vector3d(0,0,0));
		program.setVector3d(gl2,"lightColor",new Vector3d(0.5,0.5,0.5));
		program.set1f(gl2,"useLighting",1);
		program.set4f(gl2,"objectColor",1,1,1,1);
		program.set1f(gl2,"useVertexColor",0);
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
		model.render(gl2);
	}

	private void renderMajorAxies(GL2 gl2,ShaderProgram program) {
		program.set1f(gl2,"useTexture",0);
		program.set1f(gl2,"useLighting",0);
		program.set1f(gl2,"useVertexColor",1);

		Mesh mesh = new Mesh();
		mesh.clear();
		mesh.setRenderStyle(GL2.GL_LINES);
		float x=-1.05f;
		float y=-1.05f;
		float z=-0.95f;

		gl2.glLineWidth(4);

		mesh.addColor(1, 0, 0,1);	mesh.addVertex(x, y, z);
		mesh.addColor(1, 0, 0,1);	mesh.addVertex(x+2.5f, y+0.0f, z+0.0f);
		mesh.addColor(0, 1, 0,1);	mesh.addVertex(x, y, z);
		mesh.addColor(0, 1, 0,1);	mesh.addVertex(x+0.0f, y+2.5f, z+0.0f);
		mesh.addColor(0, 0, 1,1);	mesh.addVertex(x, y, z);
		mesh.addColor(0, 0, 1,1);	mesh.addVertex(x+0.0f, y+0.0f, z+2.5f);
		mesh.render(gl2);

		gl2.glLineWidth(1);
	}
}
