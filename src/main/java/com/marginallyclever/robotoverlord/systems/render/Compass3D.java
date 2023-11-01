package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.preferences.InteractionPreferences;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A 3D compass that shows the orientation of the camera.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class Compass3D {
	private static final Logger logger = LoggerFactory.getLogger(Compass3D.class);
	private final MeshFromFile cube = new MeshFromFile("/viewCube.obj");
	private final MaterialComponent mat = new MaterialComponent();
	private final Mesh axies = new Mesh();

    public Compass3D() {
    	super();
		mat.setTextureFilename("/images/viewCube.png");
		mat.setDiffuseColor(1, 1, 1, 1);
		mat.setAmbientColor(1, 1, 1, 1);
		mat.setLit(false);

		float x=-1.05f;
		float y=-1.05f;
		float z=-0.95f;
		float v=2.5f;

		axies.clear();
		axies.setRenderStyle(GL3.GL_LINES);
		axies.addColor(1, 0, 0,1);	axies.addVertex(x, y, z);
		axies.addColor(1, 0, 0,1);	axies.addVertex(x+v, y+0.0f, z+0.0f);
		axies.addColor(0, 1, 0,1);	axies.addVertex(x, y, z);
		axies.addColor(0, 1, 0,1);	axies.addVertex(x+0.0f, y+v, z+0.0f);
		axies.addColor(0, 0, 1,1);	axies.addVertex(x, y, z);
		axies.addColor(0, 0, 1,1);	axies.addVertex(x+0.0f, y+0.0f, z+v);

		cube.load();
		cube.getModel().setDirty(true);
	}

	public void render(GL3 gl,Viewport viewport,ShaderProgram program) {
		program.use(gl);
		positionCubeModel(gl,viewport,program);
		renderCubeModel(gl);
		renderMajorAxies(gl,program);
	}
	
	private void positionCubeModel(GL3 gl, Viewport viewport,ShaderProgram program) {
		double scale = InteractionPreferences.compassSize.get();
		double c = 2.0;
        double w2 = viewport.getCanvasWidth() /(2.0*scale) - c;
        double h2 = viewport.getCanvasHeight()/(2.0*scale) - c;

		program.setMatrix4d(gl,"projectionMatrix",viewport.getOrthographicMatrix(scale));
		program.setMatrix4d(gl,"viewMatrix",MatrixHelper.createIdentityMatrix4());

		Matrix4d modelMatrix = getInverseCameraMatrix(viewport.getCamera());
		modelMatrix.setTranslation(new Vector3d(w2,h2,-5));
		modelMatrix.transpose();
		program.setMatrix4d(gl,"modelMatrix",modelMatrix);
		program.setVector3d(gl,"lightPos",new Vector3d(w2,h2,10));  // Light position in world space
		program.setVector3d(gl,"ambientLightColor",new Vector3d(0.6,0.6,0.6));
		program.setVector3d(gl,"specularColor",new Vector3d(0,0,0));
		program.setVector3d(gl,"lightColor",new Vector3d(0.5,0.5,0.5));
		program.set1f(gl,"useLighting",1);
		program.set4f(gl,"objectColor",1,1,1,1);
		program.set1f(gl,"useVertexColor",0);
		program.set1f(gl,"useTexture",1);
	}

	private Matrix4d getInverseCameraMatrix(CameraComponent camera) {
		Matrix4d m = camera.getEntity().getComponent(PoseComponent.class).getWorld();
		m.invert();
		m.setTranslation(new Vector3d(0,0,0));
		return m;
	}

	private void renderCubeModel(GL3 gl) {
		gl.glEnable(GL3.GL_DEPTH_TEST);
		gl.glEnable(GL3.GL_CULL_FACE);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA,GL3.GL_ONE_MINUS_SRC_ALPHA);
		mat.render(gl);
		//cube.getModel().setDirty(true);
		cube.render(gl);
	}

	private void renderMajorAxies(GL3 gl,ShaderProgram program) {
		program.set1f(gl,"useTexture",0);
		program.set1f(gl,"useLighting",0);
		program.set1f(gl,"useVertexColor",1);

		gl.glLineWidth(4);
		axies.render(gl);
		gl.glLineWidth(1);
	}
}
