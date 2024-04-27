package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Vector3d;

/**
 * <p>{@link Decal} is a {@link Mesh}. It is a width x height quad in the XY plane centered on the local origin.
 * The one-sided version only faces +Z.
 * </p>
 */
public class Decal extends Mesh {
	public float height = 1;
	public float width = 1;
	public int wParts = 1;
	public int hParts = 1;
	public float textureScale = 1;

	public Decal() {
		super();
		updateModel();
	}

	/**
	 * Procedurally generate a list of triangles that form a box, subdivided by some
	 * amount.
	 */
	public void updateModel() {
		clear();
		setRenderStyle(GL3.GL_TRIANGLES);
		//createTwoSidedDecal();
		createOneSidedDecal();
	}

	/**
	 * Create a rectangle in the XY plane, facing +Z.
	 */
	private void createOneSidedDecal() {
		Vector3d n = new Vector3d();
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d p2 = new Vector3d();
		Vector3d p3 = new Vector3d();

		// face
		n.set(0, 0, 1);
		p0.set( width/2.0f,  height/2.0f, 0);
		p1.set(-width/2.0f,  height/2.0f, 0);
		p2.set(-width/2.0f, -height/2.0f, 0);
		p3.set( width/2.0f, -height/2.0f, 0);
		addSubdividedPlane(n, p0, p1, p2, p3, wParts, hParts);
	}

	private void createTwoSidedDecal() {
		int wParts = (int) (width);
		int hParts = (int) (height);

		Vector3d n = new Vector3d();
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d p2 = new Vector3d();
		Vector3d p3 = new Vector3d();

		// bottom
		n.set(0, 0, -1);
		p0.set(-width/2.0f,  height/2.0f, -0.01);
		p1.set( width/2.0f,  height/2.0f, -0.01);
		p2.set( width/2.0f, -height/2.0f, -0.01);
		p3.set(-width/2.0f, -height/2.0f, -0.01);
		addSubdividedPlane(n, p0, p1, p2, p3, wParts, hParts);

		// top
		n.set(0, 0, 1);
		p0.set( width/2.0f,  height/2.0f, 0.01);
		p1.set(-width/2.0f,  height/2.0f, 0.01);
		p2.set(-width/2.0f, -height/2.0f, 0.01);
		p3.set( width/2.0f, -height/2.0f, 0.01);
		addSubdividedPlane(n, p0, p1, p2, p3, wParts, hParts);
	}

	/**
	 * Subdivide a plane into triangles.
	 * 
	 * @param n      plane normal
	 * @param p0     northwest corner
	 * @param p1     northeast corner
	 * @param p2     southeast corner
	 * @param p3     southwest corner
	 * @param xParts east/west divisions
	 * @param yParts north/south divisions
	 */
	protected void addSubdividedPlane(Vector3d n,
			Vector3d p0,
			Vector3d p1,
			Vector3d p2,
			Vector3d p3,
			int xParts,
			int yParts) {
		xParts = Math.max(xParts, 1);
		yParts = Math.max(yParts, 1);

		Vector3d pA = new Vector3d();
		Vector3d pB = new Vector3d();
		Vector3d pC = new Vector3d();
		Vector3d pD = new Vector3d();
		Vector3d pE = new Vector3d();
		Vector3d pF = new Vector3d();
		Vector3d pG = new Vector3d();
		Vector3d pH = new Vector3d();

		for (int x = 0; x < xParts; x++) {
			pA.set(MathHelper.interpolate(p0, p1, (double) (x) / (double) xParts));
			pB.set(MathHelper.interpolate(p0, p1, (double) (x + 1) / (double) xParts));
			pC.set(MathHelper.interpolate(p3, p2, (double) (x) / (double) xParts));
			pD.set(MathHelper.interpolate(p3, p2, (double) (x + 1) / (double) xParts));

			for (int y = 0; y < yParts; y++) {
				pE.set(MathHelper.interpolate(pA, pC, (double) (y) / (double) yParts));
				pF.set(MathHelper.interpolate(pB, pD, (double) (y) / (double) yParts));
				pG.set(MathHelper.interpolate(pA, pC, (double) (y + 1) / (double) yParts));
				pH.set(MathHelper.interpolate(pB, pD, (double) (y + 1) / (double) yParts));

				if (getRenderStyle() == GL3.GL_TRIANGLES) {
					addVertex((float) pE.x, (float) pE.y, (float) pE.z);
					addVertex((float) pF.x, (float) pF.y, (float) pF.z);
					addVertex((float) pH.x, (float) pH.y, (float) pH.z);

					addVertex((float) pE.x, (float) pE.y, (float) pE.z);
					addVertex((float) pH.x, (float) pH.y, (float) pH.z);
					addVertex((float) pG.x, (float) pG.y, (float) pG.z);
				}
			}
		}

		for(int i=0;i<getNumVertices();++i) {
			addNormal((float) n.x, (float) n.y, (float) n.z);
			addColor(1, 1, 1, 1);
			Vector3d v = getVertex(i);
			// texture coordinates are based on the distance from the top left corner
			float x = (float) (v.x - p2.x);
			float y = (float) (v.y - p2.y);
			addTexCoord(x/textureScale, y/textureScale);
		}
	}
/*
	@Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		jo.put("width", width.toJSON(context));
		jo.put("height", height.toJSON(context));

		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
		super.parseJSON(jo, context);
		width.parseJSON(jo.getJSONObject("width"), context);
		height.parseJSON(jo.getJSONObject("height"), context);
	}
*/
}
