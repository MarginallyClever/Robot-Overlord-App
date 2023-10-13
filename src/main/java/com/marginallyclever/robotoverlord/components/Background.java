package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A background component is always rendered first and is always behind everything else.
 * The transform is always adjusted to the active camera.
 * @author Dan Royer
 */
@ComponentDependency(components = {PoseComponent.class})
public class Background extends Component {
	private static final Logger logger = LoggerFactory.getLogger(Background.class);
	private transient final TextureParameter textureXPos = new TextureParameter("XPos","/skybox/cube-x-pos.png");
	private transient final TextureParameter textureXNeg = new TextureParameter("XNeg","/skybox/cube-x-neg.png");
	private transient final TextureParameter textureYPos = new TextureParameter("YPos","/skybox/cube-y-pos.png");
	private transient final TextureParameter textureYNeg = new TextureParameter("YNeg","/skybox/cube-y-neg.png");
	private transient final TextureParameter textureZPos = new TextureParameter("ZPos","/skybox/cube-z-pos.png");
	private transient final TextureParameter textureZNeg = new TextureParameter("ZNeg","/skybox/cube-z-neg.png");

	private transient final Mesh [] meshes = new Mesh[6];

	public Background() {
		super();

		// build meshes once
		Mesh mesh0 = new Mesh();
		mesh0.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		mesh0.addTexCoord(0,1);	mesh0.addVertex(10, 10, 10);
		mesh0.addTexCoord(0,0);	mesh0.addVertex(10, 10, -10);
		mesh0.addTexCoord(1,0);	mesh0.addVertex(10, -10, -10);
		mesh0.addTexCoord(1,1);	mesh0.addVertex(10, -10, 10);

		Mesh mesh1 = new Mesh();
		mesh1.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		mesh1.addTexCoord(0,1);	mesh1.addVertex(-10, -10, 10);
		mesh1.addTexCoord(0,0);	mesh1.addVertex(-10, -10, -10);
		mesh1.addTexCoord(1,0);	mesh1.addVertex(-10, 10, -10);
		mesh1.addTexCoord(1,1);	mesh1.addVertex(-10, 10, 10);

		Mesh mesh2 = new Mesh();
		mesh2.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		mesh2.addTexCoord(0,1);	mesh2.addVertex(-10, 10, 10);
		mesh2.addTexCoord(0,0);	mesh2.addVertex(-10, 10, -10);
		mesh2.addTexCoord(1,0);	mesh2.addVertex(10, 10, -10);
		mesh2.addTexCoord(1,1);	mesh2.addVertex(10, 10, 10);

		Mesh mesh3 = new Mesh();
		mesh3.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		mesh3.addTexCoord(0,1);	mesh3.addVertex(10, -10, 10);
		mesh3.addTexCoord(0,0);	mesh3.addVertex(10, -10, -10);
		mesh3.addTexCoord(1,0);	mesh3.addVertex(-10, -10, -10);
		mesh3.addTexCoord(1,1);	mesh3.addVertex(-10, -10, 10);

		Mesh mesh4 = new Mesh();
		mesh4.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		mesh4.addTexCoord(0,0);	mesh4.addVertex(-10, 10, 10);
		mesh4.addTexCoord(1,0);	mesh4.addVertex( 10, 10, 10);
		mesh4.addTexCoord(1,1);	mesh4.addVertex( 10,-10, 10);
		mesh4.addTexCoord(0,1);	mesh4.addVertex(-10,-10, 10);

		Mesh mesh5 = new Mesh();
		mesh5.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		mesh5.addTexCoord(0,0);	mesh5.addVertex(-10,-10, -10);
		mesh5.addTexCoord(1,0);	mesh5.addVertex( 10,-10, -10);
		mesh5.addTexCoord(1,1);	mesh5.addVertex( 10, 10, -10);
		mesh5.addTexCoord(0,1);	mesh5.addVertex(-10, 10, -10);

		meshes[0] = mesh0;
		meshes[1] = mesh1;
		meshes[2] = mesh2;
		meshes[3] = mesh3;
		meshes[4] = mesh4;
		meshes[5] = mesh5;
	}

	public void render(GL3 gl, Viewport viewport, ShaderProgram program) {
		PoseComponent cameraPose = viewport.getCamera().getEntity().getComponent(PoseComponent.class);

		Matrix4d m1 = MatrixHelper.createIdentityMatrix4();
		Vector3d cameraPosition = new Vector3d(cameraPose.getPosition());
		m1.setTranslation(cameraPosition);
		m1.transpose();
		program.setMatrix4d(gl,"modelMatrix",m1);

		program.setMatrix4d(gl,"projectionMatrix",viewport.getChosenProjectionMatrix());

		Matrix4d viewMatrix = viewport.getViewMatrix();
		viewMatrix.transpose();
		program.setMatrix4d(gl,"viewMatrix",viewMatrix);

		program.set1i(gl,"useVertexColor",0);
		program.set1i(gl,"useLighting",0);
		program.set1i(gl,"useTexture",1);
		program.set1i(gl,"diffuseTexture",0);

		program.setVector3d(gl,"cameraPos",new Vector3d(0,0,0));  // Camera position in world space
		program.setVector3d(gl,"lightColor",new Vector3d(1,1,1));  // Light color
		program.set4f(gl,"objectColor",1,1,1,1);
		program.setVector3d(gl,"specularColor",new Vector3d(0.5,0.5,0.5));
		program.setVector3d(gl,"ambientLightColor",new Vector3d(0.2,0.2,0.2));

		gl.glDepthMask(true);
		gl.glColorMask(true,true,true,true);
		gl.glStencilMask(0x00);
		gl.glStencilFunc(GL.GL_ALWAYS,1,0xFF);
		gl.glStencilOp(GL.GL_KEEP,GL.GL_KEEP,GL.GL_REPLACE);
		gl.glEnable(GL.GL_TEXTURE_2D);

		textureXPos.render(gl);		meshes[0].render(gl);
		textureXNeg.render(gl);		meshes[1].render(gl);
		textureYPos.render(gl);		meshes[2].render(gl);
		textureYNeg.render(gl);		meshes[3].render(gl);
		textureZPos.render(gl);		meshes[4].render(gl);
		textureZNeg.render(gl);		meshes[5].render(gl);

		OpenGLHelper.checkGLError(gl,logger);
		// Clear the depth buffer
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
	}
}
