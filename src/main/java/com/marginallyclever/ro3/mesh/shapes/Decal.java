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
public class Decal extends ProceduralMesh {
	public float height = 1;
	public float width = 1;
	public int wParts = 1;
	public int hParts = 1;
	public float textureScale = 1;

	public Decal() {
		super();
		updateModel();
	}

	@Override
	public String getEnglishName() {
		return "Decal";
	}

	@Override
	public void updateModel() {
		clear();
		setRenderStyle(GL3.GL_TRIANGLES);
		//createTwoSidedDecal();
		createOneSidedDecal();

		fireMeshChanged();
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

		// Calculate all points on the subdivided plane
		for (int x = 0; x <= xParts; x++) {
			double xFraction = (double) x / xParts;
			Vector3d a = MathHelper.interpolate(p0, p1, xFraction);
			Vector3d b = MathHelper.interpolate(p3, p2, xFraction);
			for (int y = 0; y <= yParts; y++) {
				var v = MathHelper.interpolate(a, b, (double) y / yParts);
				addVertex((float) v.x, (float) v.y, (float) v.z);
				addNormal((float) n.x, (float) n.y, (float) n.z);
				addColor(1, 1, 1, 1);
				addTexCoord(
						(float) (v.x - p2.x) / textureScale,
						(float) (v.y - p2.y) / textureScale
				);
			}
		}

		// Connect points with triangles
		int height = yParts + 1;
		for (int x = 0; x < xParts; x++) {
			for (int y = 0; y < yParts; y++) {
				int a = (x    ) * height + (y    );
				int b = (x + 1) * height + (y    );
				int c = (x    ) * height + (y + 1);
				int d = (x + 1) * height + (y + 1);

				addIndex(a);
				addIndex(b);
				addIndex(d);

				addIndex(a);
				addIndex(d);
				addIndex(c);
			}
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
